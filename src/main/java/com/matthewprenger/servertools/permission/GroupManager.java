/*
 * Copyright 2014 Matthew Prenger
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

package com.matthewprenger.servertools.permission;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matthewprenger.servertools.core.util.FileUtils;
import com.matthewprenger.servertools.permission.elements.Group;
import com.matthewprenger.servertools.permission.elements.GroupException;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class GroupManager {

    private static final Map<String, Group> groups = new HashMap<>();
    private static final Map<String, File> groupFiles = new HashMap<>();
    private static File groupDir;

    private static Gson gson;

    private static boolean loadDefaultGroups = false;

    public static void init(File groupDirectory) {

        gson = new GsonBuilder().setPrettyPrinting().create();

        groupDir = groupDirectory;
        if (groupDir.mkdirs()) {
            ServerToolsPermission.log.trace(String.format("Creating Group directory at: %s", groupDir.getAbsolutePath()));
        }

        loadGroupsFromFile();

        if (!(groups.containsKey(PermissionConfig.defaultGroup)))
            try {
                createGroup(PermissionConfig.defaultGroup);
            } catch (GroupException e) {
                e.printStackTrace();
                ServerToolsPermission.log.warn("Tried to create the default group, but it already existed....This shouldn't happen");
            }
    }

    /**
     * Create a new permission group
     *
     * @param groupName the name of the new group
     * @throws GroupException if the group already exists
     */
    public static void createGroup(String groupName) throws GroupException {

        if (!groups.containsKey(groupName)) {
            groups.put(groupName, new Group(groupName));
            groupFiles.put(groupName, new File(groupDir, groupName + ".json"));
            saveGroupToFile(groupName);
        } else
            throw new GroupException("That group already exists");
    }

    /**
     * Removes a group and all members from the group
     *
     * @param groupName the name of the group
     * @throws GroupException if the group doesn't exist
     */
    public static void removeGroup(String groupName) throws GroupException {

        if (groups.containsKey(groupName)) {
            groups.remove(groupName);
            if (groupFiles.containsKey(groupName)) {
                groupFiles.get(groupName).delete();
                groupFiles.remove(groupName);
                refreshAllPlayerDisplayNames();
            }
        } else
            throw new GroupException("That group doesn't exist");
    }

    /**
     * Add a player to a permission group
     *
     * @param username  The username of the player to add
     * @param groupName The name of the group
     * @throws GroupException if the group doesn't exist
     */
    public static void addUserToGroup(String username, String groupName) throws GroupException {

        if (groups.containsKey(groupName)) {
            groups.get(groupName).addMember(username);
            refreshPlayerDisplayName(username);
            saveGroupToFile(groupName);
        } else
            throw new GroupException("That group doesn't exist");
    }

    /**
     * Remove a player from a permission group
     *
     * @param username  the username of the player to remove
     * @param groupName the name of the group
     * @throws GroupException if the group doesn't exist or the player isn't in the group
     */
    public static void removeUserFromGroup(String username, String groupName) throws GroupException {

        if (groups.containsKey(groupName)) {

            Group group = groups.get(groupName);
            if (!group.removeUser(username)) {
                throw new GroupException("That player isn't a member of the group");
            }
            refreshPlayerDisplayName(username);
            saveGroupToFile(groupName);
        } else
            throw new GroupException("That group doesn't exist");
    }

    /**
     * Add an allowed command to a group
     *
     * @param commandName the name of the command
     * @param groupName   the name of the group
     * @throws GroupException if the group doesn't exist
     */
    public static void addAllowedCommand(String commandName, String groupName) throws GroupException {

        if (groups.containsKey(groupName)) {
            groups.get(groupName).addAllowedCommand(commandName);
            saveGroupToFile(groupName);
        } else
            throw new GroupException("That group doesn't exist");
    }

    /**
     * Remove an allowed command from a group
     *
     * @param commandName the name of the command
     * @param groupName   the name of the group
     * @throws GroupException if the group doesn't exist or the command wasn't in the group
     */
    public static void removeAllowedCommand(String commandName, String groupName) throws GroupException {

        if (groups.containsKey(groupName)) {
            Group group = groups.get(groupName);
            if (!group.removeAllowedCommand(commandName))
                throw new GroupException("That command wasn't allowed for that group");

            saveGroupToFile(groupName);
        } else
            throw new GroupException("That group doesn't exist");
    }

    /**
     * Add a child group to another group. This adds permissions from the child group to the group
     *
     * @param childGroupName the name of the child group
     * @param groupName      the name of the group
     * @throws GroupException if the child group or the parent group doesn't exist
     */
    public static void addChildGroupToGroup(String childGroupName, String groupName) throws GroupException {

        if (groups.containsKey(groupName)) {
            if (groups.containsKey(childGroupName)) {

                groups.get(groupName).addChildGroup(childGroupName);
                saveGroupToFile(groupName);

            } else
                throw new GroupException("That child group doesn't exist");
        } else
            throw new GroupException("That parent group doesn't exist");
    }

    /**
     * Get a map of the current groups
     *
     * @return a {@link java.util.HashMap} of groups
     */
    public static Map<String, Group> getGroups() {

        return ImmutableMap.copyOf(groups);
    }

    /**
     * Get a list of allowed commands for a group
     *
     * @param groupName the name of the group
     * @return a {@link java.util.Set} of allowed commands or {@code null} if the group doesn't exist
     */
    public static Set<String> getAllowedCommands(String groupName) {

        if (groups.containsKey(groupName)) {

            return groups.get(groupName).getAllowedCommands();
        }

        return null;
    }

    /**
     * See if a user can use a command
     *
     * @param username    the username of the player
     * @param commandName the name of the command
     * @return {@code true} if the command is allowed, {@code false} if the command is not allowed
     */
    public static boolean canUseCommand(String username, String commandName) {

        for (Map.Entry<String, Group> entry : groups.entrySet()) {
            Group group = entry.getValue();
            if (group.isMember(username)) {
                if (getAllowedCommands(group.groupName).contains(commandName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * See if an {@link net.minecraft.command.ICommandSender} can use an {@link net.minecraft.command.ICommand}
     *
     * @param sender   the command sender
     * @param iCommand the command
     * @return if the sender can use the command
     */
    public static boolean canUseCommand(ICommandSender sender, ICommand iCommand) {

        if (!(sender instanceof EntityPlayer))
            return iCommand.canCommandSenderUseCommand(sender);
        else {
            return canUseCommand(sender.getCommandSenderName(), iCommand.getCommandName());
        }
    }

    /**
     * Set the chat prefix color for a group
     *
     * @param groupName the group name
     * @param color     the index of the {@link net.minecraft.util.EnumChatFormatting} color
     * @throws GroupException exception
     */
    public static void setGroupChatColor(String groupName, String color) throws GroupException {

        if (groups.containsKey(groupName)) {
            groups.get(groupName).setChatColor(color);
            saveGroupToFile(groupName);
            refreshAllPlayerDisplayNames();
        } else
            throw new GroupException("That group doesn't exist");
    }

    private static void saveGroupToFile(String groupName) {

        if (groups.containsKey(groupName)) {
            Group group = groups.get(groupName);
            File groupFile = groupFiles.get(groupName);

            try {
                FileUtils.writeStringToFile(gson.toJson(group), groupFile);
            } catch (IOException e) {
                e.printStackTrace();
                ServerToolsPermission.log.warn(String.format("Failed to save group file: %s", groupFile.getAbsolutePath()));
            }
        }
    }

    /**
     * Clears the map of groups and reloads it from file
     */
    private static void loadGroupsFromFile() {

        groups.clear();

        File[] fileList = groupDir.listFiles();

        if (fileList == null || fileList.length == 0) {

            loadDefaultGroups = true;
            return;
        }

        for (File file : fileList) {

            if (file.getName().endsWith(".json")) {

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String groupName = file.getName().substring(0, file.getName().length() - 5);

                    Type type = new TypeToken<Group>() {
                    }.getType();

                    Group group = gson.fromJson(reader, type);

                    groups.put(groupName, group);
                    groupFiles.put(groupName, file);

                } catch (Exception e) {
                    e.printStackTrace();
                    ServerToolsPermission.log.warn(String.format("Failed to load group %s from file", file.getName()));
                }
            }
        }
    }

    public static void loadDefaultGroups() {

        ServerToolsPermission.log.warn("Loading default groups, you should review the groups and make changes as necessary");

        String ADMIN = "Admin";
        String MODERATOR = "Moderator";
        String PLAYER = "Player";

        try {

            groups.clear();

            createGroup(ADMIN);
            createGroup(MODERATOR);
            createGroup(PLAYER);

            setGroupChatColor(ADMIN, EnumChatFormatting.RED.getFriendlyName());
            setGroupChatColor(MODERATOR, EnumChatFormatting.BLUE.getFriendlyName());
            setGroupChatColor(PLAYER, EnumChatFormatting.WHITE.getFriendlyName());

            addChildGroupToGroup(PLAYER, MODERATOR);
            addChildGroupToGroup(MODERATOR, ADMIN);

            for (Object obj : MinecraftServer.getServer().getCommandManager().getCommands().entrySet()) {

                Object value = ((Map.Entry) obj).getValue();

                if (value instanceof CommandBase) {
                    CommandBase base = (CommandBase) value;

                    ServerToolsPermission.log.debug(String.format("Found CommandBase: %s, Permission Level: %s", base.getCommandName(), base.getRequiredPermissionLevel()));

                    switch (base.getRequiredPermissionLevel()) {

                        case 4:
                        case 3:
                            addAllowedCommand(base.getCommandName(), ADMIN);
                            break;
                        case 2:
                            addAllowedCommand(base.getCommandName(), MODERATOR);
                            break;
                        default:
                            addAllowedCommand(base.getCommandName(), PLAYER);

                    }
                } else if (value instanceof ICommand) {
                    ICommand iCommand = (ICommand) value;

                    ServerToolsPermission.log.debug(String.format("Found ICommand: %s", iCommand.getCommandName()));

                    addAllowedCommand(iCommand.getCommandName(), ADMIN);
                } else
                    ServerToolsPermission.log.debug(String.format("Found Object: %s With Class: %s", value, value.getClass()));

                for (Object op : MinecraftServer.getServer().getConfigurationManager().getOps()) {

                    if (op instanceof String)
                        addUserToGroup(op.toString(), ADMIN);
                }

            }

        } catch (GroupException e) {
            e.printStackTrace();
            ServerToolsPermission.log.warn("Failed to load default groups");
        }
    }

    public static List<Group> getPlayerGroups(String username) {

        List<Group> playerGroups = new ArrayList<>();

        for (Map.Entry<String, Group> entry : groups.entrySet()) {
            if (entry.getValue().isMember(username)) {
                playerGroups.add(entry.getValue());
            }
        }

        return playerGroups;
    }

    public static void assignDefaultGroup(String username) {

        try {
            addUserToGroup(username, PermissionConfig.defaultGroup);
        } catch (GroupException e) {
            e.printStackTrace();
            ServerToolsPermission.log.warn("This should't ever happen... congratulations");
        }
    }

    public static boolean shouldLoadDefaultGroups() {

        return loadDefaultGroups;
    }

    private static void refreshPlayerDisplayName(String username) {

        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username);

        if (player != null)
            player.refreshDisplayName();
    }

    private static void refreshAllPlayerDisplayNames() {

        for (EntityPlayer player : (List<EntityPlayer>)MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            player.refreshDisplayName();
        }
    }
}
