package com.smd.gctcore.common.world.jotunheim;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Remembers each generated cave's original occupants, including after they are killed. */
public final class CyclopsCaveData extends WorldSavedData {
    private static final String NAME = "gctcore_cyclops_caves";
    private final Map<Long, Encounter> encounters = new LinkedHashMap<>();
    private final ArrayDeque<Long> pending = new ArrayDeque<>();

    public CyclopsCaveData() { super(NAME); }
    public CyclopsCaveData(String name) { super(name); }

    public static CyclopsCaveData get(World world) {
        CyclopsCaveData data = (CyclopsCaveData) world.getPerWorldStorage()
                .getOrLoadData(CyclopsCaveData.class, NAME);
        if (data == null) {
            data = new CyclopsCaveData();
            world.getPerWorldStorage().setData(NAME, data);
        }
        return data;
    }

    /** Slot zero is the underground Cyclops Tek giant; the remaining slots guard the entrance. */
    public static final class Encounter {
        final BlockPos center;
        final List<BlockPos> positions;
        int spawned;

        Encounter(BlockPos center, List<BlockPos> positions, int spawned) {
            this.center = center.toImmutable();
            this.positions = new ArrayList<>(positions);
            this.spawned = spawned;
        }

        boolean complete() { return spawned == (1 << positions.size()) - 1; }
        boolean hasSpawned(int slot) { return (spawned & (1 << slot)) != 0; }
    }

    public synchronized void register(BlockPos center, List<BlockPos> guardians) {
        long id = center.toLong();
        if (encounters.containsKey(id)) return;
        List<BlockPos> positions = new ArrayList<>();
        positions.add(center.toImmutable());
        for (BlockPos guardian : guardians) {
            if (positions.size() >= 3) break;
            positions.add(guardian.toImmutable());
        }
        if (positions.size() < 2) throw new IllegalArgumentException("A cyclops cave needs an exterior guardian");
        encounters.put(id, new Encounter(center, positions, 0));
        pending.addLast(id);
        markDirty();
    }

    /** Rotate a bounded batch so unloaded or obstructed caves cannot starve later encounters. */
    synchronized List<Encounter> pendingBatch(int limit) {
        List<Encounter> batch = new ArrayList<>();
        int attempts = Math.min(limit, pending.size());
        for (int i = 0; i < attempts; i++) {
            Long id = pending.removeFirst();
            Encounter encounter = encounters.get(id);
            if (encounter == null || encounter.complete()) continue;
            pending.addLast(id);
            batch.add(encounter);
        }
        return batch;
    }

    synchronized void spawned(Encounter encounter, int slot) {
        if (encounter.hasSpawned(slot)) return;
        encounter.spawned |= 1 << slot;
        if (encounter.complete()) pending.remove(encounter.center.toLong());
        markDirty();
    }

    @Override
    public synchronized void readFromNBT(NBTTagCompound nbt) {
        encounters.clear();
        pending.clear();
        NBTTagList caves = nbt.getTagList("Caves", 10);
        for (int i = 0; i < caves.tagCount(); i++) {
            NBTTagCompound cave = caves.getCompoundTagAt(i);
            BlockPos center = BlockPos.fromLong(cave.getLong("Center"));
            NBTTagList saved = cave.getTagList("Positions", 4);
            if (saved.tagCount() < 2 || saved.tagCount() > 3) continue;
            List<BlockPos> positions = new ArrayList<>();
            for (int j = 0; j < saved.tagCount(); j++) {
                positions.add(BlockPos.fromLong(((NBTTagLong) saved.get(j)).getLong()));
            }
            int mask = (1 << positions.size()) - 1;
            Encounter encounter = new Encounter(center, positions, cave.getInteger("Spawned") & mask);
            encounters.put(center.toLong(), encounter);
            if (!encounter.complete()) pending.addLast(center.toLong());
        }
    }

    @Override
    public synchronized NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList caves = new NBTTagList();
        for (Encounter encounter : encounters.values()) {
            NBTTagCompound cave = new NBTTagCompound();
            cave.setLong("Center", encounter.center.toLong());
            cave.setInteger("Spawned", encounter.spawned);
            NBTTagList positions = new NBTTagList();
            for (BlockPos pos : encounter.positions) positions.appendTag(new NBTTagLong(pos.toLong()));
            cave.setTag("Positions", positions);
            caves.appendTag(cave);
        }
        nbt.setTag("Caves", caves);
        return nbt;
    }
}
