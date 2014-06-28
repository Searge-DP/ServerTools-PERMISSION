package com.matthewprenger.servertools.permission.perms;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.matthewprenger.servertools.core.util.FileUtils;
import com.matthewprenger.servertools.permission.Group;
import com.matthewprenger.servertools.permission.ServerToolsPermission;
import com.matthewprenger.servertools.permission.config.PermissionConfig;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PermissionManager {

    private static final Map<String, Group> groupMap = new THashMap<>();

    private static File groupDirectory;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void init(File groupDir) {

        if (groupDir.exists() && !groupDir.isDirectory())
            throw new RuntimeException("A file with the same name as the group directory exists");

        groupDir.mkdirs();
        groupDirectory = groupDir;

        loadGroups();

        if (!groupMap.containsKey(PermissionConfig.defaultGroup)) {
            groupMap.put(PermissionConfig.defaultGroup, new Group(PermissionConfig.defaultGroup));
        }
    }

    public static boolean createGroup(String name) {

        if (groupMap.containsKey(name))
            return false;

        groupMap.put(name, new Group(name));
        saveGroup(name);

        return true;
    }

    public static boolean removeGroup(String name) {

        if (name.equals(PermissionConfig.defaultGroup))
            return false;

        if (groupMap.remove(name) != null) {
            File file = new File(groupDirectory, name + ".json");
            file.delete();
            return true;
        }

        return false;
    }

    public static boolean checkPerm(String node, UUID uuid) {

        for (Group group : getGroups(uuid)) {
            if (group.hasPerm(node))
                return true;
        }

        return false;
    }

    public static Group getGroup(String name) {

        return groupMap.get(name);
    }

    public static Collection<Group> getAllGroups() {

        return groupMap.values();
    }

    public static Collection<String> getGroupNames() {

        return groupMap.keySet();
    }

    public static Collection<Group> getGroups(UUID uuid) {

        Collection<Group> groups = new THashSet<>();

        for (Group group : groupMap.values()) {

            if (group.isMember(uuid)) {
                groups.add(group);
            }
        }

        return groups;
    }

    public static void saveGroups() {

        for (Group group : getAllGroups())
            saveGroup(group.groupName);
    }

    public static void saveGroup(String name) {

        Group group = groupMap.get(name);

        if (group == null) return;

        File groupFile = new File(groupDirectory, group.getName() + ".json");

        try {
            FileUtils.writeStringToFile(gson.toJson(group), groupFile);
        } catch (IOException e) {
            ServerToolsPermission.log.warn("Failed to save group: " + group.groupName, e);
            e.printStackTrace();
        }
    }

    public static void loadGroups() {

        groupMap.clear();

        File[] fileList = groupDirectory.listFiles(groupFileFilter);

        if (fileList == null || fileList.length == 0) {
            genDefaultGroups();
            return;
        }

        for (File file : fileList) {

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                String groupName = file.getName().substring(0, file.getName().length() - 5);

                Type type = new TypeToken<Group>() {
                }.getType();

                Group group = gson.fromJson(reader, type);

                groupMap.put(groupName, group);

            } catch (JsonSyntaxException e) {
                ServerToolsPermission.log.warn("Failed to parse group" + file.getName() + " as valid json", e);
            } catch (IOException e) {
                ServerToolsPermission.log.warn("Failed to load group " + file.getName(), e);
            }
        }

    }

    private static void genDefaultGroups() {

        ServerToolsPermission.log.warn("Loading default groups, you should review the groups and make changes as necessary");

        String ADMIN = "Admin";
        String MODERATOR = "Moderator";
        String DEFAULT = PermissionConfig.defaultGroup;

        createGroup(ADMIN);
        createGroup(MODERATOR);
        createGroup(DEFAULT);

        Group adminGroup = getGroup(ADMIN);
        adminGroup.setChatColor(EnumChatFormatting.RED.getFriendlyName());

        Group modGroup = getGroup(MODERATOR);
        modGroup.setChatColor(EnumChatFormatting.BLUE.getFriendlyName());

        Group defaultGroup = getGroup(DEFAULT);
        defaultGroup.setChatColor(EnumChatFormatting.WHITE.getFriendlyName());

        adminGroup.setParent(modGroup);
        modGroup.setParent(defaultGroup);

        for (Object obj : MinecraftServer.getServer().getCommandManager().getCommands().values()) {

            if (obj instanceof CommandBase) {
                CommandBase cmdBase = (CommandBase) obj;

                switch (cmdBase.getRequiredPermissionLevel()) {
                    // TODO refactor this when MCForge perms api gets patches
                    case 4:
                    case 3:
                        adminGroup.addPerm(getPermFromCommand(cmdBase));
                        break;
                    case 2:
                        modGroup.addPerm(getPermFromCommand(cmdBase));
                        break;
                    default:
                        defaultGroup.addPerm(getPermFromCommand(cmdBase));
                }
            } else if (obj instanceof ICommand) {
                ICommand iCmd = (ICommand) obj;
                adminGroup.addPerm(getPermFromCommand(iCmd));
            } else {
                ServerToolsPermission.log.warn("Non command detected in CommandMap: " + obj.getClass().getName());
            }
        }

        for (Object aPlayerEntityList : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            EntityPlayer entityplayer = (EntityPlayer) aPlayerEntityList;

            if (MinecraftServer.getServer().getConfigurationManager().func_152596_g(entityplayer.getGameProfile())) {
                adminGroup.addPlayerToGroup(entityplayer);
            }
        }
    }

    public static void assignDefaultGroup(EntityPlayer player) {

        Group defaultGroup = getGroup(PermissionConfig.defaultGroup);

        if (defaultGroup == null) return;

        defaultGroup.addPlayerToGroup(player);
    }

    private static void refreshPlayerDisplayName(String username) {

        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username); //getPlayerForUsername

        if (player != null)
            player.refreshDisplayName();
    }

    @SuppressWarnings("unchecked")
    private static void refreshAllPlayerDisplayNames() {

        for (EntityPlayer player : (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            player.refreshDisplayName();
        }
    }

    public static String getPermFromCommand(ICommand command) {

        return "cmd." + command.getCommandName();
    }

    private static final FilenameFilter groupFileFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".json");
        }
    };
}
