package brightspark.landmanager.command.optional;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm passives <areaName>
//OR
//lm op passives <areaName>
public class CommandPassives extends LMCommandArea
{
    @Override
    public String getName()
    {
        return "passives";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return LMConfig.permissions.passiveSpawning ?  "lm.command.passives.usage" : "lm.command.passives.usage.op";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap) throws CommandException
    {
        checkCanEditArea(server, sender, area);

        area.togglePassiveSpawning();
        cap.dataChanged();
        sender.sendMessage(new TextComponentTranslation("lm.command.passives.success", area.canPassiveSpawn(), area.getName()));
        LandManager.areaLog(AreaLogType.SET_PASSIVES, area.getName(), sender);
    }
}
