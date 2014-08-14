package com.matthewprenger.servertools.permission.command;

import com.matthewprenger.servertools.core.command.CommandLevel;
import com.matthewprenger.servertools.core.command.ServerToolsCommand;
import com.matthewprenger.servertools.permission.Group;
import com.matthewprenger.servertools.permission.perms.PermissionManager;
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
