package brightspark.landmanager.data.areas;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class Area implements INBTSerializable<NBTTagCompound>
{
    private static final Pattern AREA_NAME = Pattern.compile("^\\w+$");

    private String name;
    private int dimensionId;
    private BlockPos pos1, pos2;
    private UUID owner;
    private Set<UUID> members = new HashSet<>();
    //TODO: Have defaults configurable
    private boolean
            canPassiveSpawn = true,
            canHostileSpawn = true,
            explosions = true,
            interactions = false;

    public Area(String name, int dimensionId, BlockPos position1, BlockPos position2)
    {
        this.name = name;
        this.dimensionId = dimensionId;
        pos1 = new BlockPos(
                Math.min(position1.getX(), position2.getX()),
                Math.min(position1.getY(), position2.getY()),
                Math.min(position1.getZ(), position2.getZ()));
        pos2 = new BlockPos(
                Math.max(position1.getX(), position2.getX()),
                Math.max(position1.getY(), position2.getY()),
                Math.max(position1.getZ(), position2.getZ()));
    }

    public Area(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    public static boolean validateName(String areaName)
    {
        return AREA_NAME.matcher(areaName).matches();
    }

    public String getName()
    {
        return name;
    }

    public boolean setName(String name)
    {
        if(!validateName(name))
            return false;
        this.name = name;
        return true;
    }

    public int getDimensionId()
    {
        return dimensionId;
    }

    public BlockPos getMinPos()
    {
        return pos1;
    }

    public BlockPos getMaxPos()
    {
        return pos2;
    }

    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(UUID playerUuid)
    {
        owner = playerUuid;
    }

    public boolean isOwner(UUID playerUuid)
    {
        return Objects.equals(owner, playerUuid);
    }

    public Set<UUID> getMembers()
    {
        return members;
    }

    public boolean addMember(UUID playerUuid)
    {
        return members.add(playerUuid);
    }

    public boolean removeMember(UUID playerUuid)
    {
        return members.remove(playerUuid);
    }

    public boolean isMember(UUID playerUuid)
    {
        return isOwner(playerUuid) || members.contains(playerUuid);
    }

    public boolean canPassiveSpawn()
    {
        return canPassiveSpawn;
    }

    public void togglePassiveSpawning()
    {
        canPassiveSpawn = !canPassiveSpawn;
    }

    public void setPassiveSpawning(boolean value)
    {
        canPassiveSpawn = value;
    }

    public boolean canHostileSpawn()
    {
        return canHostileSpawn;
    }

    public void toggleHostileSpawning()
    {
        canHostileSpawn = !canHostileSpawn;
    }

    public void setHostileSpawning(boolean value)
    {
        canHostileSpawn = value;
    }

    public boolean canExplosionsCauseDamage()
    {
        return explosions;
    }

    public void toggleExplosions()
    {
        explosions = !explosions;
    }

    public void setExplosions(boolean value)
    {
        explosions = value;
    }

    public boolean canInteract()
    {
        return interactions;
    }

    public void toggleInteractions()
    {
        interactions = !interactions;
    }

    public void setInteractions(boolean value)
    {
        interactions = value;
    }

    public AxisAlignedBB asAABB()
    {
        Vec3d p1 = new Vec3d(pos1).add(new Vec3d(0.4d, 0.4d, 0.4d));
        Vec3d p2 = new Vec3d(pos2).add(new Vec3d(0.6d, 0.6d, 0.6d));
        return new AxisAlignedBB(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }

    public boolean intersects(Area area)
    {
        return asAABB().intersects(area.asAABB());
    }

    public boolean intersects(BlockPos pos)
    {
        return asAABB().contains(new Vec3d(pos).add(new Vec3d(0.5d, 0.5d, 0.5d)));
    }

    public void extendToMinMaxY(World world)
    {
        pos1 = new BlockPos(pos1.getX(), 0, pos1.getZ());
        pos2 = new BlockPos(pos2.getX(), world.getHeight(), pos2.getZ());
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("name", name);
        nbt.setInteger("dimension", dimensionId);
        nbt.setLong("position1", pos1.toLong());
        nbt.setLong("position2", pos2.toLong());
        if(owner != null)
            nbt.setUniqueId("player", owner);
        if(!members.isEmpty())
        {
            NBTTagList memberList = new NBTTagList();
            members.forEach(member ->
            {
                NBTTagCompound memberNbt = new NBTTagCompound();
                memberNbt.setUniqueId("uuid", member);
                memberList.appendTag(memberNbt);
            });
            nbt.setTag("members", memberList);
        }
        nbt.setBoolean("passive", canPassiveSpawn);
        nbt.setBoolean("hostile", canHostileSpawn);
        nbt.setBoolean("explosions", explosions);
        nbt.setBoolean("interact", interactions);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        name = nbt.getString("name");
        dimensionId = nbt.getInteger("dimension");
        pos1 = BlockPos.fromLong(nbt.getLong("position1"));
        pos2 = BlockPos.fromLong(nbt.getLong("position2"));
        if(nbt.hasUniqueId("player"))
            owner = nbt.getUniqueId("player");
        members.clear();
        if(nbt.hasKey("members"))
        {
            NBTTagList memberList = nbt.getTagList("members", Constants.NBT.TAG_COMPOUND);
            memberList.forEach(memberNbt -> members.add(((NBTTagCompound) memberNbt).getUniqueId("uuid")));
        }
        canPassiveSpawn = nbt.getBoolean("passive");
        canHostileSpawn = nbt.getBoolean("hostile");
        explosions = nbt.getBoolean("explosions");
        interactions = nbt.getBoolean("interact");
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null) return false;
        if(obj == this) return true;
        if(obj.getClass() != getClass()) return false;
        Area other = (Area) obj;

        return new EqualsBuilder()
            .append(name, other.name)
            .append(dimensionId, other.dimensionId)
            .append(pos1, other.pos1)
            .append(pos2, other.pos2)
            .isEquals();
    }
}
