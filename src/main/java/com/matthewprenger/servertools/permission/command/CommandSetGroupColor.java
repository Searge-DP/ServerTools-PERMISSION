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
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CommandSetGroupColor extends ServerToolsCommand {

    public CommandSetGroupColor(String defaultName) {
        super(defaultName);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/" + name + " [groupName] [colorIndex]";
    }

    @SuppressWarnings("unchecked")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {

        if (args.length == 1) {
            Set<String> strings = GroupManager.getGroups().keySet();
            return getListOfStringsMatchingLastWord(args, strings.toArray(new String[strings.size()]));
        } else if (args.length == 2) {
            Collection<String> strings = EnumChatFormatting.getValidValues(true, false);
            return getListOfStringsMatchingLastWord(args, strings.toArray(new String[strings.size()]));
        }

        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length != 2)
            throw new WrongUsageException(getCommandUsage(sender));

        if (!GroupManager.getGroups().containsKey(args[0]))
            throw new PlayerNotFoundException("That group doesn't exist");

        if (EnumChatFormatting.getValueByName(args[1]) == null)
            throw new CommandException("That color doesn't exist");

        try {
            GroupManager.setGroupChatColor(args[0], args[1]);
        } catch (GroupException e) {
            throw new PlayerNotFoundException("That group doesn't exist");
        }

        sender.addChatMessage(Util.getChatComponent(String.format("Set %s color to %s", args[0], args[1]), EnumChatFormatting.GRAY));
    }
}
