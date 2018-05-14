package brightspark.landmanager.command;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import brightspark.landmanager.item.LMItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.*;

public class CommandLandManagerOp extends LMCommand
{
    @Override
    public String getName()
    {
        return "lmop";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "\nlmop delete <areaName>\n" +
                "lmop allocate <playerName> <areaName>\n" +
                "lmop clearAllocation <areaName>\n" +
                "lmop spawning <areaName> [true/false]\n" +
                "lmop tool";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length == 0)
            throw new WrongUsageException(getUsage(sender));

        String command = args[0].toLowerCase();

        String areaName = null;
        CapabilityAreas cap = null;
        //Only get the area name and capability from the arguments if necessary for the command
        if(command.equals("delete") || command.equals("allocate") || command.equals("clearallocation") || command.equals("spawning"))
        {
            areaName = argsToString(args, command.equals("allocate") ? 2 : 1);
            if(areaName.isEmpty()) areaName = null;
            if(areaName != null) cap = getWorldCapWithArea(server, areaName);
            if(cap == null)
            {
                sender.sendMessage(new TextComponentTranslation("message.command.none", areaName));
                return;
            }
        }

        switch(command)
        {
            case "delete": //lmop delete <areaName>
                if(cap.removeArea(areaName))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.delete.deleted", areaName));
                    LandManager.areaLog(AreaLogType.DELETE, areaName, (EntityPlayerMP) sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.delete.failed", areaName));
                break;
            case "allocate": //lmop allocate <playerName> <areaName>
                UUID uuid = null;
                GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
                if(profile != null) uuid = profile.getId();
                if(uuid == null)
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.allocate.noplayer", args[1]));
                    return;
                }
                if(cap.setAllocation(areaName, uuid))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.allocate.success", areaName, profile.getName()));
                    LandManager.areaLog(AreaLogType.ALLOCATE, areaName, (EntityPlayerMP) sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.allocate.failed", areaName, profile.getName()));
                break;
            case "clearallocation": //lmop clearAllocation <areaName>
                if(cap.clearAllocation(areaName))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.clear.cleared", areaName));
                    LandManager.areaLog(AreaLogType.CLEAR_ALLOCATION, areaName, (EntityPlayerMP) sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.clear.failed", areaName));
                break;
            case "spawning": //lmop spawning <areaName> [true/false]
                Boolean spawning = null;
                if(args.length >= 2)
                {
                    String arg2 = args[1];
                    if(arg2.equalsIgnoreCase("true") || arg2.equalsIgnoreCase("t"))
                        spawning = true;
                    else if(arg2.equalsIgnoreCase("false") || arg2.equalsIgnoreCase("f"))
                        spawning = false;
                }
                if(cap.setSpawning(areaName, spawning))
                {
                    sender.sendMessage(new TextComponentTranslation("message.command.spawning.success", cap.getArea(areaName).getStopsEntitySpawning(), areaName));
                    LandManager.areaLog(AreaLogType.SET_SPAWNING, areaName, (EntityPlayerMP) sender);
                }
                else
                    sender.sendMessage(new TextComponentTranslation("message.command.spawning.failed", areaName));
                break;
            case "tool": //lmop tool
                if(!(sender instanceof EntityPlayer))
                    sender.sendMessage(new TextComponentTranslation("message.command.tool.player"));
                else if(!((EntityPlayer) sender).addItemStackToInventory(new ItemStack(LMItems.adminItem)))
                    sender.sendMessage(new TextComponentTranslation("message.command.tool.inventory"));
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        switch(args.length)
        {
            case 1:
                return getListOfStringsMatchingLastWord(args, "allocate", "clearAllocation", "delete", "spawning", "tool");
            case 2:
                switch(args[0])
                {
                    case "allocate":
                        return getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames());
                    case "clearAllocation":
                    case "delete":
                    case "spawning":
                        return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
                    default:
                        return Collections.emptyList();
                }
            case 3:
                switch(args[0])
                {
                    case "allocate":
                        return getListOfStringsMatchingLastWord(args, getAllAreaNames(server));
                    case "spawning":
                        return getListOfStringsMatchingLastWord(args, "t", "true", "f", "false");
                    default:
                        return Collections.emptyList();
                }
            default:
                return Collections.emptyList();
        }
    }
}
