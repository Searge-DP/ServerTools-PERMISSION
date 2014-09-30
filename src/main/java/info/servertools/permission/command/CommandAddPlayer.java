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

import com.google.common.base.Strings;
import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.util.AccountUtils;
import info.servertools.permission.Group;
import info.servertools.permission.PermissionManager;
import info.servertools.permission.ServerToolsPermission;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CommandAddPlayer extends ServerToolsCommand {

    public CommandAddPlayer(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.OP;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {

        if (par2ArrayOfStr.length == 1) {
            return getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames());
        } else if (par2ArrayOfStr.length == 2) {
            Collection<String> groupKeys = PermissionManager.getGroupNames();
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

        Group group = PermissionManager.getGroup(args[1]);

        if (group == null)
            throw new PlayerNotFoundException("That group doesn't exist");

        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(args[0]);
        UUID targetUUID;
        if (player == null) {
            String uuid = AccountUtils.getUUID(args[0].trim());
            if (Strings.isNullOrEmpty(uuid)) {
                throw new PlayerNotFoundException("Could not retrieve UUID for that player");
            }
            targetUUID = UUID.fromString(uuid);
        } else {
            targetUUID = player.getPersistentID();
        }

        group.addPlayer(targetUUID);

        ServerToolsPermission.log.info("Added {} with UUID {} to the {} group", args[0], targetUUID, group.getName());
        notifyOperators(sender, this, String.format("Added %s to the %s group", args[0], args[1]));
    }
}
