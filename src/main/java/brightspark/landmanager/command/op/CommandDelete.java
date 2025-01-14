package brightspark.landmanager.command.op;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommandArea;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.logs.AreaLogType;
import brightspark.landmanager.data.requests.RequestsWorldSavedData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;

//lm op delete <areaName>
public class CommandDelete extends LMCommandArea
{
    @Override
    public String getName()
    {
        return "delete";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.delete.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, Area area, CapabilityAreas cap)
    {
        if(cap.removeArea(area.getName()))
        {
            LandManager.areaLog(AreaLogType.DELETE, area.getName(), sender);
            RequestsWorldSavedData requests = RequestsWorldSavedData.get(server.getEntityWorld());
            if(requests != null)
                requests.deleteAllForArea(area.getName());
            sender.sendMessage(new TextComponentTranslation("lm.command.delete.deleted", area.getName()));
            //Notify all members of the area that the area was deleted
            notifyPlayer(server, area.getOwner());
            area.getMembers().forEach(memberUuid -> notifyPlayer(server, memberUuid));
        }
        else
            sender.sendMessage(new TextComponentTranslation("lm.command.delete.failed", area.getName()));
    }

    private void notifyPlayer(MinecraftServer server, UUID uuid)
    {
        EntityPlayerMP player = getPlayerFromUuid(server, uuid);
        if(player != null)
            player.sendMessage(new TextComponentTranslation("lm.command.delete.notify"));
    }
}
