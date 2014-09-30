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
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.List;

public class CommandRemovePerm extends ServerToolsCommand {

    public CommandRemovePerm(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.OP;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] strings) {

        if (strings.length == 1) {
            CommandHandler ch = (CommandHandler) MinecraftServer.getServer().getCommandManager();
            return ch.getPossibleCommands(sender, strings[0]);
        } else if (strings.length == 2) {
            Collection<String> groupKeys = PermissionManager.getGroupNames();
            return getListOfStringsMatchingLastWord(strings, groupKeys.toArray(new String[groupKeys.size()]));
        } else
            return null;

    }

    @Override
    public String getCommandUsage(ICommandSender sender) {

        return "/" + name + " [commandname] [groupname]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] strings) {

        if (strings.length != 2)
            throw new WrongUsageException(getCommandUsage(sender));

        Group group = PermissionManager.getGroup(strings[1]);

        if (group == null)
            throw new PlayerNotFoundException("That group doesn't exist");

        if (group.removePerm(strings[0]))
            notifyOperators(sender, this, String.format("Removed command %s from %s", strings[0], strings[1]));
        else
            throw new PlayerNotFoundException("That group didn't have that perm");
    }
}