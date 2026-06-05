package com.smd.gctcore.common.world;

import com.smd.gctcore.common.blocks.arcanearchives.RadiantResonatorBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RadiantResonatorData extends WorldSavedData {
    private static final String NAME = "gctcore_radiant_resonators";
    private final Map<UUID, Set<DimPos>> resonators = new HashMap<>();

    public RadiantResonatorData() {
        super(NAME);
    }

    public RadiantResonatorData(String name) {
        super(name);
    }

    public static RadiantResonatorData get(World world) {
        World saveWorld = DimensionManager.getWorld(0);
        if (saveWorld == null) {
            saveWorld = world;
        }

        MapStorage storage = saveWorld.getMapStorage();
        RadiantResonatorData data = (RadiantResonatorData) storage.getOrLoadData(RadiantResonatorData.class, NAME);
        if (data == null) {
            data = new RadiantResonatorData();
            storage.setData(NAME, data);
        }
        return data;
    }

    public int count(UUID owner) {
        Set<DimPos> positions = resonators.get(owner);
        return positions == null ? 0 : positions.size();
    }

    public void add(UUID owner, int dimension, BlockPos pos) {
        resonators.computeIfAbsent(owner, key -> new HashSet<>()).add(new DimPos(dimension, pos));
        markDirty();
    }

    public void remove(UUID owner, int dimension, BlockPos pos) {
        Set<DimPos> positions = resonators.get(owner);
        if (positions != null) {
            positions.remove(new DimPos(dimension, pos));
            if (positions.isEmpty()) {
                resonators.remove(owner);
            }
            markDirty();
        }
    }

    public void removeAt(int dimension, BlockPos pos) {
        DimPos target = new DimPos(dimension, pos);
        Iterator<Map.Entry<UUID, Set<DimPos>>> ownerIterator = resonators.entrySet().iterator();
        while (ownerIterator.hasNext()) {
            Map.Entry<UUID, Set<DimPos>> entry = ownerIterator.next();
            if (entry.getValue().remove(target)) {
                if (entry.getValue().isEmpty()) {
                    ownerIterator.remove();
                }
                markDirty();
                return;
            }
        }
    }

    public void prune(UUID owner) {
        Set<DimPos> positions = resonators.get(owner);
        if (positions == null) {
            return;
        }

        boolean changed = false;
        Iterator<DimPos> iterator = positions.iterator();
        while (iterator.hasNext()) {
            DimPos dimPos = iterator.next();
            World world = DimensionManager.getWorld(dimPos.dimension);
            if (world != null && !(world.getBlockState(dimPos.pos).getBlock() instanceof RadiantResonatorBlock)) {
                iterator.remove();
                changed = true;
            }
        }

        if (positions.isEmpty()) {
            resonators.remove(owner);
            changed = true;
        }
        if (changed) {
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        resonators.clear();
        NBTTagList list = nbt.getTagList("resonators", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            UUID owner = UUID.fromString(tag.getString("owner"));
            BlockPos pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
            add(owner, tag.getInteger("dim"), pos);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, Set<DimPos>> entry : resonators.entrySet()) {
            for (DimPos dimPos : entry.getValue()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("owner", entry.getKey().toString());
                tag.setInteger("dim", dimPos.dimension);
                tag.setInteger("x", dimPos.pos.getX());
                tag.setInteger("y", dimPos.pos.getY());
                tag.setInteger("z", dimPos.pos.getZ());
                list.appendTag(tag);
            }
        }
        compound.setTag("resonators", list);
        return compound;
    }

    private static class DimPos {
        private final int dimension;
        private final BlockPos pos;

        private DimPos(int dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos.toImmutable();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DimPos)) {
                return false;
            }
            DimPos other = (DimPos) obj;
            return dimension == other.dimension && pos.equals(other.pos);
        }

        @Override
        public int hashCode() {
            return 31 * dimension + pos.hashCode();
        }
    }
}
