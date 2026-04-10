package com.smd.gctcore.common.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

public class CrimsonTempleData extends WorldSavedData {
    public static final String NAME = "gctcore_crimson_temple_data";
    private final List<BlockPos> temples = new ArrayList<>();

    public CrimsonTempleData() {
        super(NAME);
    }

    public CrimsonTempleData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        temples.clear();
        NBTTagList list = nbt.getTagList("temples", 4);
        for (int i = 0; i < list.tagCount(); i++) {
            if (list.get(i) instanceof NBTTagLong) {
                NBTTagLong l = (NBTTagLong) list.get(i);
                long val = l.getLong();
                temples.add(BlockPos.fromLong(val));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : temples) {
            list.appendTag(new NBTTagLong(pos.toLong()));
        }
        compound.setTag("temples", list);
        return compound;
    }

    public void addTemple(BlockPos pos) {
        if (!temples.contains(pos)) {
            temples.add(pos);
            this.markDirty();
        }
    }

    public List<BlockPos> getTemples() {
        return new ArrayList<>(temples);
    }

    public static CrimsonTempleData get(World world) {
        World worldServer = world.getMinecraftServer().getWorld(41);
        if (worldServer == null) return null;
        CrimsonTempleData data = (CrimsonTempleData) worldServer.getPerWorldStorage().getOrLoadData(CrimsonTempleData.class, NAME);
        if (data == null) {
            data = new CrimsonTempleData();
            worldServer.getPerWorldStorage().setData(NAME, data);
        }
        return data;
    }
}
