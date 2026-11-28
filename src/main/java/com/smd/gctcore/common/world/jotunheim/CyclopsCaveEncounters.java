package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.Tags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Spawn cave occupants once their own terrain is loaded; never generate chunks to spawn them. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class CyclopsCaveEncounters {
    private static final ResourceLocation TEK_CYCLOPS = new ResourceLocation("cyclopstek", "cyclops");
    private static final ResourceLocation ICE_CYCLOPS = new ResourceLocation("iceandfire", "cyclops");

    private CyclopsCaveEncounters() {}

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)
                || !(event.world.provider instanceof WorldProviderJotunheim)
                || event.world.getTotalWorldTime() % 20L != 0L
                || event.world.getDifficulty() == EnumDifficulty.PEACEFUL) return;
        spawnPending((WorldServer) event.world, CyclopsCaveData.get(event.world));
    }

    static void spawnPending(WorldServer world, CyclopsCaveData data) {
        for (CyclopsCaveData.Encounter cave : data.pendingBatch(16)) {
            for (int slot = 0; slot < cave.positions.size(); slot++) {
                if (cave.hasSpawned(slot)) continue;
                BlockPos feet = cave.positions.get(slot);
                if (!world.isBlockLoaded(feet, false)) continue;
                UUID id = occupantId(world.getSeed(), cave.center, slot);
                // Recover a successfully saved entity if a crash interrupted the completion record.
                if (world.getEntityFromUuid(id) != null) {
                    data.spawned(cave, slot);
                    continue;
                }
                Entity created = EntityList.createEntityByIDFromName(slot == 0 ? TEK_CYCLOPS : ICE_CYCLOPS, world);
                if (!(created instanceof EntityLiving)) {
                    if (created != null) discard(created);
                    continue;
                }
                EntityLiving cyclops = (EntityLiving) created;
                cyclops.setUniqueId(id);
                cyclops.setLocationAndAngles(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D,
                        (float) Math.toDegrees(Math.atan2(cave.center.getZ() - feet.getZ(),
                                cave.center.getX() - feet.getX())) - 90.0F, 0.0F);
                AxisAlignedBB box = cyclops.getEntityBoundingBox();
                if (box.minY < 1 || box.maxY > world.getHeight()
                        || !world.isAreaLoaded(new BlockPos(box.minX - 1, box.minY - 1, box.minZ - 1),
                        new BlockPos(box.maxX + 1, box.maxY + 1, box.maxZ + 1), false)
                        || !world.getBlockState(feet.down()).isFullCube() || !cyclops.isNotColliding()) {
                    // Cyclops Tek registers itself on the event bus in its constructor.
                    // Discarded candidates must not leave the mod's event listener behind.
                    discard(cyclops);
                    continue;
                }
                cyclops.onInitialSpawn(world.getDifficultyForLocation(feet), null);
                cyclops.enablePersistence();
                cyclops.getEntityData().setLong("GCTCyclopsCave", cave.center.toLong());
                cyclops.getEntityData().setInteger("GCTCyclopsCaveSlot", slot);
                if (world.spawnEntity(cyclops)) data.spawned(cave, slot);
                else discard(cyclops);
            }
        }
    }

    private static void discard(Entity entity) {
        entity.setDead();
        MinecraftForge.EVENT_BUS.unregister(entity);
    }

    static UUID occupantId(long seed, BlockPos center, int slot) {
        return UUID.nameUUIDFromBytes(("gctcore:cyclops_cave:" + seed + ':' + center.toLong() + ':' + slot)
                .getBytes(StandardCharsets.UTF_8));
    }
}
