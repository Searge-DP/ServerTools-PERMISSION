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
import info.servertools.permission.ServerToolsPermission;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;

import java.util.UUID;

public class CommandRemoveUUID extends ServerToolsCommand {

    public CommandRemoveUUID(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.OP;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + " [UUID] [Group]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length != 2)
            throw new WrongUsageException(getCommandUsage(sender));

        try {
            UUID uuid = UUID.fromString(args[0]);

            Group group = PermissionManager.getGroup(args[1]);

            if (group == null)
                throw new PlayerNotFoundException("That group doesn't exist");

            if (group.removePlayer(uuid)) {
                ServerToolsPermission.log.info("Removed UUID {} from the {} group", args[0], group.getName());
                notifyOperators(sender, this, String.format("Removed %s from the %s group", args[0], group.getName()));
            } else {
                throw new PlayerNotFoundException("That player wasn't a member of that group");
            }

        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }
}
