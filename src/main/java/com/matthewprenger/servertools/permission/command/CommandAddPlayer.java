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

package com.matthewprenger.servertools.permission.command;

import com.matthewprenger.servertools.core.command.ServerToolsCommand;
import com.matthewprenger.servertools.core.util.Util;
import com.matthewprenger.servertools.permission.GroupManager;
import com.matthewprenger.servertools.permission.elements.GroupException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;
import java.util.Set;

public class CommandAddPlayer extends ServerToolsCommand {

    public CommandAddPlayer(String defaultName) {
        super(defaultName);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {

        if (par2ArrayOfStr.length == 1) {
            return getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames());
        } else if (par2ArrayOfStr.length == 2) {
            Set<String> groupKeys = GroupManager.getGroups().keySet();
            return getListOfStringsMatchingLastWord(par2ArrayOfStr, groupKeys.toArray(new String[groupKeys.size()]));
        } else {
            return null;
        }
    }

    @Override
    public boolean isUsernameIndex(String[] par1ArrayOfStr, int par2) {

        return par2 == 0;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {

        return "/" + name + " [username] [groupname]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length != 2)
            throw new WrongUsageException(getCommandUsage(sender));

        try {
            GroupManager.addUserToGroup(args[0], args[1]);
        } catch (GroupException e) {
            sender.addChatMessage(Util.getChatComponent(e.toString(), EnumChatFormatting.RED));
            return;
        }

        notifyAdmins(sender, String.format("Added %s to the %s group", args[0], args[1]));
    }
}
