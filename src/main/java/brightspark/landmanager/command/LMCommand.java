package brightspark.landmanager.command;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.util.ListView;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

abstract class LMCommand extends CommandBase
{
    CapabilityAreas getWorldCapWithArea(MinecraftServer server, String areaName) throws CommandException
    {
        if(areaName == null) throw new WrongUsageException("No area name provided!");
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap == null) throw new CommandException("Failed to get areas from the world with dimension id %s", world.provider.getDimension());
            Area area = cap.getArea(areaName);
            if(area != null) return cap;
        }
        return null;
    }

    List<Area> getAllAreas(MinecraftServer server)
    {
        List<Area> areas = new ArrayList<>();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap != null) areas.addAll(cap.getAllAreas());
        }
        areas.sort(Comparator.comparing(Area::getName));
        return areas;
    }

    List<String> getAllAreaNames(MinecraftServer server)
    {
        List<String> areaNames = new ArrayList<>();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap != null) areaNames.addAll(cap.getAllAreaNames());
        }
        areaNames.sort(Comparator.naturalOrder());
        return areaNames;
    }

    String getPlayerNameFromUuid(MinecraftServer server, UUID uuid)
    {
        String playerName = null;
        if(uuid != null)
        {
            GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(uuid);
            if(profile != null) playerName = profile.getName();
        }
        return playerName;
    }

    String posToString(BlockPos pos)
    {
        return String.format("%sX: %s%s, %sY: %s%s, %sZ: %s%s",
                TextFormatting.YELLOW, TextFormatting.RESET, pos.getX(),
                TextFormatting.YELLOW, TextFormatting.RESET, pos.getY(),
                TextFormatting.YELLOW, TextFormatting.RESET, pos.getZ());
    }

    String argsToString(String[] args, int startIndex)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = startIndex; i < args.length; i++)
            sb.append(args[i]).append(" ");
        return sb.toString().trim();
    }

    ITextComponent goldTextComponent(String text, Object... args)
    {
        return textComponentWithColour(TextFormatting.GOLD, text, args);
    }

    ITextComponent textComponentWithColour(TextFormatting colour, String text, Object... args)
    {
        ITextComponent textComponent = new TextComponentTranslation(text, args);
        textComponent.getStyle().setColor(colour);
        return textComponent;
    }

    <T> ListView<T> getListView(List<T> list, int page, int maxPerPage)
    {
        page = Math.max(0, page);
        int size = list.size();
        int pageMax = size / maxPerPage;
        //We reduce the given page number by 1, because we calculate starting from page 0, but is shown to start from page 1.
        if(page > 0) page--;
        if(page * maxPerPage > size) page = pageMax;
        //Work out the range to get from the list
        int min = page * maxPerPage;
        int max = min + maxPerPage;
        if(size < max) max = size;

        return new ListView<>(list.subList(min, max), page, pageMax);
    }
}
