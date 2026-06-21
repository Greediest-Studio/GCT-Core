package com.smd.gctcore.common.events;

import com.smd.gctcore.common.network.GctNetworkHandler;
import com.smd.gctcore.common.network.PacketNilfheimErosion;
import com.smd.gctcore.common.world.biome.nilfheim.NilfheimBiomes;
import com.smd.gctcore.misc.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NilfheimErosionHandler {
    private static final String KEY_EROSION = "gctcore:nilfheim_erosion";
    private static final String KEY_LAST_SYNC = "gctcore:nilfheim_erosion_last_sync";
    private static final String KEY_LAST_DAMAGE = "gctcore:nilfheim_erosion_last_damage";
    private static final float MAX_EROSION = 2400.0F;
    private static final float GROWTH_PER_TICK = 5.0F;
    private static final float FAST_GROWTH_PER_TICK = 8.5F;
    private static final float DECAY_PER_TICK = 9.0F;
    private static final DamageSource EROSION_DAMAGE = new DamageSource("nilfheim_erosion").setDamageBypassesArmor();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return;
        }

        EntityPlayer player = event.player;
        NBTTagCompound data = player.getEntityData();
        boolean inNilfheim = player.dimension == NilfheimBiomes.DIMENSION_ID;
        float erosion = data.getFloat(KEY_EROSION);

        if (!inNilfheim) {
            if (erosion > 0.0F) {
                erosion = Math.max(0.0F, erosion - DECAY_PER_TICK * 2.0F);
                data.setFloat(KEY_EROSION, erosion);
                syncIfNeeded((EntityPlayerMP) player, erosion, false, true);
            }
            return;
        }

        if (isNearMistRepellingLight(player.world, player.getPosition())) {
            erosion = Math.max(0.0F, erosion - DECAY_PER_TICK);
        } else {
            erosion = Math.min(MAX_EROSION, erosion + growthFor(player));
        }

        data.setFloat(KEY_EROSION, erosion);

        if (erosion >= MAX_EROSION) {
            player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 0, true, true));
            long ticks = player.world.getTotalWorldTime();
            long lastDamage = data.getLong(KEY_LAST_DAMAGE);
            if (ticks - lastDamage >= 40L) {
                player.attackEntityFrom(EROSION_DAMAGE, player.getMaxHealth() * 0.1F);
                data.setLong(KEY_LAST_DAMAGE, ticks);
            }
        }

        syncIfNeeded((EntityPlayerMP) player, erosion, true, false);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || event.world.provider.getDimension() != NilfheimBiomes.DIMENSION_ID) {
            return;
        }

        applyLocalSnow((WorldServer) event.world);
    }

    private float growthFor(EntityPlayer player) {
        if (player.world.getBiome(player.getPosition()) == NilfheimBiomes.MISTVEIL_DEPTHS
                || player.world.getBiome(player.getPosition()) == NilfheimBiomes.HOWLING_ICEFIELD) {
            return FAST_GROWTH_PER_TICK;
        }
        return GROWTH_PER_TICK;
    }

    private void applyLocalSnow(WorldServer world) {
        if (world.getTotalWorldTime() % 4L != 0L) {
            return;
        }

        for (EntityPlayer player : world.playerEntities) {
            for (int i = 0; i < 4; i++) {
                int x = (int) player.posX + world.rand.nextInt(33) - 16;
                int z = (int) player.posZ + world.rand.nextInt(33) - 16;
                BlockPos column = new BlockPos(x, 0, z);
                if (!world.isBlockLoaded(column, false)) {
                    continue;
                }

                BlockPos top = world.getPrecipitationHeight(column);
                BlockPos below = top.down();
                if (!world.isBlockLoaded(below, false) || !world.isAreaLoaded(below, 1)) {
                    continue;
                }

                Chunk chunk = world.getChunk(below);
                if (!world.provider.canDoRainSnowIce(chunk)) {
                    continue;
                }

                if (world.canBlockFreezeNoWater(below)) {
                    world.setBlockState(below, Blocks.ICE.getDefaultState(), 2);
                }
                if (world.canSnowAt(top, true)) {
                    world.setBlockState(top, Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }
    }

    private boolean isNearMistRepellingLight(World world, BlockPos center) {
        int radius = 6;
        for (BlockPos pos : BlockPos.getAllInBox(center.add(-radius, -3, -radius), center.add(radius, 3, radius))) {
            if (!world.isBlockLoaded(pos, false)) {
                continue;
            }
            if (isMistRepellingBlock(world.getBlockState(pos).getBlock())) {
                return true;
            }
            if (world.getLightFor(EnumSkyBlock.BLOCK, pos) >= 13 && pos.distanceSq(center) <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    private boolean isMistRepellingBlock(Block block) {
        return block == Blocks.TORCH
                || block == Blocks.LIT_PUMPKIN
                || block == Blocks.LIT_REDSTONE_LAMP
                || block == Blocks.GLOWSTONE
                || block == Blocks.FIRE
                || block == Blocks.LAVA
                || block == Blocks.FLOWING_LAVA
                || block == Blocks.MAGMA
                || block == BlockRegistry.MIST_CRYSTAL_STONE
                || block == BlockRegistry.SOULFIRE_GRASS
                || block == BlockRegistry.GLOWING_CREEPER
                || block == BlockRegistry.WEEPING_ICE_FLOWER
                || block == BlockRegistry.SHADOWBERRY_BUSH
                || block == BlockRegistry.ASHEN_MUSHROOM;
    }

    private void syncIfNeeded(EntityPlayerMP player, float erosion, boolean active, boolean force) {
        NBTTagCompound data = player.getEntityData();
        long now = player.world.getTotalWorldTime();
        long last = data.getLong(KEY_LAST_SYNC);
        if (!force && now - last < 10L) {
            return;
        }

        data.setLong(KEY_LAST_SYNC, now);
        GctNetworkHandler.CHANNEL.sendTo(new PacketNilfheimErosion(erosion / MAX_EROSION, active), player);
    }
}
