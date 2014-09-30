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
package info.servertools.permission.command;

import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.permission.Group;
import info.servertools.permission.PermissionManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;

import java.util.List;

public class CommandSetParent extends ServerToolsCommand {

    public CommandSetParent(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.OP;
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/" + name + " [GROUP] [PARENT_GROUP]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length != 2)
            throw new WrongUsageException(getCommandUsage(sender));

        if (args[0].equals(args[1]))
            throw new CommandException("Can't set a groups parent to itself");

        Group group = PermissionManager.getGroup(args[0]);

        if (group == null)
            throw new PlayerNotFoundException("That group doesn't exist");

        Group parent = PermissionManager.getGroup(args[1]);

        if (parent == null)
            throw new PlayerNotFoundException("That parent group doesn't exist");

        group.setParent(parent);
        addChatMessage(sender, "Set the parent of: " + args[0] + " to: " + args[1]);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {

        if (args.length == 1 || args.length == 2) {
            return getListOfStringsFromIterableMatchingLastWord(args, PermissionManager.getGroupNames());
        }

        return null;
    }
}
