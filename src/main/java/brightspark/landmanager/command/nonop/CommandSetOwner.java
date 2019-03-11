package brightspark.landmanager.command.nonop;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.UUID;

//lm setowner <areaName> [playerName]
public class CommandSetOwner extends LMCommand
{
	@Override
	public String getName()
	{
		return "setowner";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "lm.command.setowner.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length < 1)
			throwWrongUsage(sender);
		//Only OPs can set the owner to null
		if(args.length == 1 && !isOP(server, sender))
			throw new CommandException("lm.command.setowner.op");

		String areaName = args[0];
		Pair<CapabilityAreas, Area> pair = getAreaAndCap(server, areaName);
		Area area = pair.getRight();
		UUID player = null;
		if(args.length > 1)
		{
			checkCanEditArea(server, sender, area);
			player = getUuidFromPlayerName(server, argsToString(args, 1));
		}

		//Set the owner and update members
		UUID prevOwner = area.getOwner();
		area.setOwner(player);
		if(player != null)
			area.removeMember(player);
		if(prevOwner != null)
			area.addMember(prevOwner);
		pair.getLeft().dataChanged();
		sender.sendMessage(new TextComponentTranslation("lm.command.setowner.success", areaName, player));
		LandManager.areaLog(AreaLogType.SET_OWNER, areaName, sender);
	}
}
