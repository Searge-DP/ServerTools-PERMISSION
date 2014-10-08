/*
 * Copyright 2014 ServerTools
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.servertools.permission;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import info.servertools.core.lib.Reference;
import info.servertools.core.util.FileUtils;
import info.servertools.core.util.SaveThread;
import info.servertools.core.util.ServerUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class PermissionManager {

    private static final Map<String, Group> groupMap = new HashMap<>();

    private static File groupDirectory;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Object lock = new Object();

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

    /**
     * Create a new group
     *
     * @param name
     *         the group's name
     *
     * @return {@code true} if the group was created, {@code false} if the group already existed
     */
    public static boolean createGroup(String name) {
        if (groupMap.containsKey(name)) {
            return false;
        }
        groupMap.put(name, new Group(name));
        saveGroup(name);
        return true;
    }

    /**
     * Forcifully put a group into the underlying map. <b>BE CAREFULL WHEN USING THIS</b>
     *
     * @param group
     *         the group
     */
    public static void putGroup(Group group) {
        checkNotNull(group);
        groupMap.put(group.getName(), group);
        saveGroup(group.getName());
    }

    /**
     * Remove a group
     *
     * @param name
     *         the group's name
     *
     * @return {@code true} if the group was removed, {@code false} if the group didn't exist or was the default group (Can never be deleted)
     */
    public static boolean removeGroup(String name) {
        if (name.equals(PermissionConfig.defaultGroup))
            return false;

        if (groupMap.remove(name) != null) {
            File file = new File(groupDirectory, name + ".json");
            file.delete();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check a permission
     *
     * @param node
     *         the permission node
     * @param uuid
     *         the UUID that is trying the permission
     *
     * @return {@code true} if the permission is allowed, {@code false} if not
     */
    public static boolean checkPerm(String node, UUID uuid) {
        for (Group group : getGroups(uuid)) {
            if (group.hasPerm(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a group
     *
     * @param name
     *         the group's name
     *
     * @return the group, or {@code null} if the group didn't exist
     */
    @Nullable
    public static Group getGroup(String name) {
        return groupMap.get(name);
    }

    /**
     * Get a collection of all groups
     *
     * @return the groups
     */
    public static Collection<Group> getAllGroups() {
        return groupMap.values();
    }

    /**
     * Get a collection of all group names
     *
     * @return the group names
     */
    public static Collection<String> getGroupNames() {
        return groupMap.keySet();
    }

    /**
     * Get all groups that a given {@link java.util.UUID UUID} is a member of
     *
     * @param uuid
     *         the UUID
     *
     * @return a Collection of groups
     */
    public static Collection<Group> getGroups(UUID uuid) {
        final Collection<Group> groups = new HashSet<>();

        for (final Group group : getAllGroups()) {
            if (group.isMember(uuid)) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * Save all groups to disk
     */
    public static void saveGroups() {
        for (Group group : getAllGroups())
            saveGroup(group.groupName);
    }

    /**
     * Save a given group to disk
     *
     * @param name
     *         the group's name
     */
    public static void saveGroup(String name) {
        Group group = groupMap.get(name);
        if (group == null) {
            ServerToolsPermission.log.warn("Tried to save group {} but it didn't exist!", name);
            return;
        }

        final File groupFile = new File(groupDirectory, group.getName() + ".json");

        new SaveThread(gson.toJson(group)) {

            @Override
            public void run() {
                synchronized (lock) {
                    try {
                        Files.write(data, groupFile, Reference.CHARSET);
                    } catch (IOException e) {
                        ServerToolsPermission.log.error("Failed to save group to disk", e);
                    }
                }
            }
        }.start();
    }

    /**
     * Load groups from disk
     */
    public static void loadGroups() {
        groupMap.clear();
        synchronized (lock) {

            final File[] groupFiles = groupDirectory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".json");
                }
            });

            final Type type = new TypeToken<Group>() {}.getType();

            if (groupFiles != null && groupFiles.length > 0) {
                for (final File groupFile : groupFiles) {
                    String groupName = groupFile.getName().substring(0, groupFile.getName().length() - 5);
                    if (Strings.isNullOrEmpty(groupName)) continue;

                    try {
                        String data = Files.toString(groupFile, Reference.CHARSET);
                        Group group = gson.fromJson(data, type);
                        if (!group.groupName.equals(groupName)) {
                            throw new RuntimeException(String.format("Group filename %s must match groupName in JSON", groupFile.getName()));
                        }
                        groupMap.put(group.groupName, group);
                    } catch (JsonSyntaxException e) {
                        ServerToolsPermission.log.error("Failed to parse groupfile " + groupFile.getName() + " as valid json", e);
                    } catch (IOException e) {
                        ServerToolsPermission.log.error("Failed to load groupfile " + groupFile.getName() + " from disk", e);
                    }
                }
            } else {
                genDefaultGroups();
            }


            if (!groupMap.containsKey(PermissionConfig.defaultGroup)) {
                groupMap.put(PermissionConfig.defaultGroup, new Group(PermissionConfig.defaultGroup));
                saveGroup(PermissionConfig.defaultGroup);
            }
        }
    }

    /**
     * Generate the default groups
     */
    private static void genDefaultGroups() {
        ServerToolsPermission.log.warn("Loading default groups, you should review the groups and make changes as necessary");

        final MinecraftServer server = MinecraftServer.getServer();

        final String ADMIN = "Admin";
        final String MODERATOR = "Moderator";
        final String DEFAULT = PermissionConfig.defaultGroup;

        createGroup(ADMIN);
        createGroup(MODERATOR);
        createGroup(DEFAULT);

        final Group adminGroup = getGroup(ADMIN);
        assert adminGroup != null;
        adminGroup.setChatColor(EnumChatFormatting.RED.getFriendlyName());

        final Group modGroup = getGroup(MODERATOR);
        assert modGroup != null;
        modGroup.setChatColor(EnumChatFormatting.BLUE.getFriendlyName());

        final Group defaultGroup = getGroup(DEFAULT);
        assert defaultGroup != null;
        defaultGroup.setChatColor(EnumChatFormatting.WHITE.getFriendlyName());

        adminGroup.setParent(modGroup);
        modGroup.setParent(defaultGroup);

        for (Object obj : server.getCommandManager().getCommands().values()) {

            if (obj instanceof CommandBase) {
                CommandBase cmdBase = (CommandBase) obj;
                switch (cmdBase.getRequiredPermissionLevel()) {
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


        for (String entry : server.getConfigurationManager().getOppedPlayers().getKeys()) {
            GameProfile profile = server.getConfigurationManager().getOppedPlayers().getGameProfileFromName(entry);
            adminGroup.addPlayer(profile.getId());
        }

        saveGroups();
    }

    /**
     * Assign a UUID to the default group
     *
     * @param uuid
     *         the UUID
     */
    public static void assignDefaultGroup(UUID uuid) {
        Group defaultGroup = getGroup(PermissionConfig.defaultGroup);
        assert defaultGroup != null;
        defaultGroup.addPlayer(uuid);
        saveGroup(PermissionConfig.defaultGroup);
    }

    /**
     * Refresh a player's display name
     *
     * @param uuid
     *         the player's {@link java.util.UUID UUID}
     */
    public static void refreshPlayerDisplayName(UUID uuid) {
        for (EntityPlayerMP player : ServerUtils.getAllPlayers()) {
            if (uuid.equals(player.getPersistentID())) {
                player.refreshDisplayName();
            }
        }
    }

    /**
     * Refresh all players' display names
     */
    public static void refreshAllPlayerDisplayNames() {
        for (EntityPlayerMP player : ServerUtils.getAllPlayers()) {
            player.refreshDisplayName();
        }
    }

    /**
     * Get the permission for a command
     *
     * @param command
     *         the command
     *
     * @return the permission
     */
    public static String getPermFromCommand(ICommand command) {
        return "cmd." + command.getCommandName();
    }
}
