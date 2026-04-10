package com.smd.gctcore.common.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

public class ShadowberryCaveData extends WorldSavedData {
    public static final String NAME = "gctcore_shadowberry_cave_data";
    private final List<BlockPos> caves = new ArrayList<>();

    public ShadowberryCaveData() {
        super(NAME);
    }

    public ShadowberryCaveData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        caves.clear();
        NBTTagList list = nbt.getTagList("caves", 4);
        for (int i = 0; i < list.tagCount(); i++) {
            if (list.get(i) instanceof NBTTagLong) {
                NBTTagLong entry = (NBTTagLong) list.get(i);
                caves.add(BlockPos.fromLong(entry.getLong()));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : caves) {
            list.appendTag(new NBTTagLong(pos.toLong()));
        }
        compound.setTag("caves", list);
        return compound;
    }

    public void addCave(BlockPos pos) {
        if (!caves.contains(pos)) {
            caves.add(pos);
            this.markDirty();
        }
    }

    public List<BlockPos> getCaves() {
        return new ArrayList<>(caves);
    }

    public static ShadowberryCaveData get(World world) {
        World worldServer = world.getMinecraftServer().getWorld(42);
        if (worldServer == null) {
            return null;
        }

        ShadowberryCaveData data = (ShadowberryCaveData) worldServer.getPerWorldStorage().getOrLoadData(ShadowberryCaveData.class, NAME);
        if (data == null) {
            data = new ShadowberryCaveData();
            worldServer.getPerWorldStorage().setData(NAME, data);
        }
        return data;
    }
}