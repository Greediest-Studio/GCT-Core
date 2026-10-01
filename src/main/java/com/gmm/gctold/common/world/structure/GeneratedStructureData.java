package com.gmm.gctold.common.world.structure;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.WorldSavedData;

public class GeneratedStructureData extends WorldSavedData {
    private static final String DATA_PREFIX = "gctcore_generated_structures_";

    private final List<StructureBoundingBox> bounds = new ArrayList<>();

    public GeneratedStructureData(String name) {
        super(name);
    }

    public static GeneratedStructureData get(World world) {
        String name = DATA_PREFIX + world.provider.getDimension();
        GeneratedStructureData data = (GeneratedStructureData) world.getPerWorldStorage()
                .getOrLoadData(GeneratedStructureData.class, name);
        if (data == null) {
            data = new GeneratedStructureData(name);
            world.getPerWorldStorage().setData(name, data);
        }
        return data;
    }

    public boolean intersects(StructureBoundingBox candidate) {
        for (StructureBoundingBox existing : bounds) {
            if (existing.intersectsWith(candidate)) {
                return true;
            }
        }
        return false;
    }

    public void add(StructureBoundingBox bounds) {
        this.bounds.add(new StructureBoundingBox(bounds));
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.bounds.clear();
        NBTTagList list = compound.getTagList("bounds", 11);
        for (int i = 0; i < list.tagCount(); i++) {
            int[] values = list.getIntArrayAt(i);
            if (values.length == 6) {
                this.bounds.add(new StructureBoundingBox(values));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (StructureBoundingBox box : bounds) {
            list.appendTag(new NBTTagIntArray(new int[] {
                    box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ
            }));
        }
        compound.setTag("bounds", list);
        return compound;
    }
}
