package info.servertools.permission.command;

import com.google.common.base.Joiner;
import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.permission.Group;
import info.servertools.permission.PermissionManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandListPerms extends ServerToolsCommand {

    public CommandListPerms(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.ANYONE;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + " {GROUP}";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        final Set<String> myPerms = new HashSet<>();
        switch (args.length) {
            case 0:
                if (!(sender instanceof EntityPlayerMP)) {
                    throw new WrongUsageException("Using this command with 0 args is only supported by players");
                }
                for (final Group group : PermissionManager.getGroups(((EntityPlayerMP) sender).getPersistentID())) {
                    myPerms.addAll(group.getPerms());
                }
                break;
            case 1:
                final Group group = PermissionManager.getGroup(args[0]);
                if (group == null) throw new PlayerNotFoundException("That group doesn't exist");

                myPerms.addAll(group.getPerms());
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }

        addChatMessage(sender, args.length == 0 ? "Your perms are" : args[0] + "'s perms are:");
        addChatMessage(sender, Joiner.on(", ").join(myPerms));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return args.length == 1 ? getListOfStringsFromIterableMatchingLastWord(args, PermissionManager.getGroupNames()) : null;
    }
}
