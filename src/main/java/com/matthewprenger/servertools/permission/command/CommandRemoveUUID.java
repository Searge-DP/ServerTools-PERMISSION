package com.matthewprenger.servertools.permission.command;

import com.matthewprenger.servertools.core.command.CommandLevel;
import com.matthewprenger.servertools.core.command.ServerToolsCommand;
import com.matthewprenger.servertools.permission.Group;
import com.matthewprenger.servertools.permission.ServerToolsPermission;
import com.matthewprenger.servertools.permission.perms.PermissionManager;
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
                func_152373_a(sender, this, String.format("Removed %s from the %s group", args[0], group.getName()));
            } else {
                throw new PlayerNotFoundException("That player wasn't a member of that group");
            }

        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }
}
