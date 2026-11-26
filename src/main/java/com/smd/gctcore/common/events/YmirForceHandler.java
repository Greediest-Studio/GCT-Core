package com.smd.gctcore.common.events;

import com.smd.gctcore.common.network.GctNetworkHandler;
import com.smd.gctcore.common.network.PacketYmirForce;
import com.smd.gctcore.common.world.jotunheim.JotunheimYmirWells;
import com.smd.gctcore.common.world.jotunheim.WorldProviderJotunheim;
import com.smd.gctcore.common.world.jotunheim.YmirForceRules;
import com.smd.gctcore.misc.YmirBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;

/** Server authority for the force field, talisman conversion and well shelter. */
public final class YmirForceHandler {
    private static final String DATA = "gctcore:ymir_force";
    private static final int[][] MASONRY_PROBES = {{8, 3}, {-8, -3}, {3, -8}, {-3, 8}};
    public static final DamageSource FORCE_DAMAGE = new DamageSource("ymir_force")
            .setDamageBypassesArmor().setDamageIsAbsolute();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) return;
        EntityPlayer player = event.player;
        int carried = YmirForceRules.normalizeTalismans(player);
        boolean active = player.world.provider instanceof WorldProviderJotunheim;
        JotunheimYmirWells.Plan shelter = active ? shelteredWell(player) : null;
        tickExposure(player, data(player), carried, active, shelter);
        sync(player, active, shelter != null, false);
    }

    /** Kept separate from networking and terrain lookup so the actual server rules can be checked. */
    public static void tickExposure(EntityPlayer player, NBTTagCompound state, int carried,
                                    boolean active, @Nullable JotunheimYmirWells.Plan shelter) {
        if (!active || !player.isEntityAlive()) {
            state.removeTag("journey");
            state.setInteger("exposure", 0);
            return;
        }
        int destination = YmirForceRules.advanceTraversal(state, shelter, player.posY, carried >= 0);
        if (destination >= 0) {
            YmirForceRules.tuneTalismans(player, destination);
            carried = destination;
        }
        boolean protectedNow = shelter != null || carried == YmirForceRules.layerAtHeight(player.posY)
                || player.isCreative() || player.isSpectator();
        if (protectedNow) {
            state.setInteger("exposure", 0);
            return;
        }
        int ticks = state.getInteger("exposure") + 1;
        state.setInteger("exposure", ticks % YmirForceRules.DAMAGE_INTERVAL);
        if (ticks < YmirForceRules.DAMAGE_INTERVAL) return;
        // Combat invulnerability frames cannot postpone the once-per-second environmental hit.
        int previousResistance = player.hurtResistantTime;
        player.hurtResistantTime = 0;
        player.attackEntityFrom(FORCE_DAMAGE, player.getMaxHealth() * YmirForceRules.DAMAGE_FRACTION);
        player.hurtResistantTime = Math.max(previousResistance, player.hurtResistantTime);
    }

    @Nullable
    public static JotunheimYmirWells.Plan shelteredWell(EntityPlayer player) {
        World world = player.world;
        if (!(world.provider instanceof WorldProviderJotunheim) || !world.getWorldInfo().isMapFeaturesEnabled()) return null;
        JotunheimYmirWells wells = ((WorldProviderJotunheim) world.provider).ymirWells();
        if (wells == null) return null;
        JotunheimYmirWells.Plan plan = wells.wellAt(player.getPosition());
        if (plan == null || !plan.shelters(player.getPosition())) return null;
        // A predicted location in an old, already generated chunk is not an actual well.
        // Sample loaded masonry only; never load neighbours on a player tick.
        int y = MathHelper.clamp(MathHelper.floor(player.posY), plan.lowerFloorY, plan.upperFloorY);
        for (int[] probe : MASONRY_PROBES) {
            BlockPos pos = new BlockPos(plan.x + probe[0], y, plan.z + probe[1]);
            if (world.isBlockLoaded(pos, false)
                    && YmirBlocks.blocks().containsValue(world.getBlockState(pos).getBlock())) return plan;
        }
        return null;
    }

    private static NBTTagCompound data(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        if (!entityData.hasKey(DATA, 10)) entityData.setTag(DATA, new NBTTagCompound());
        return entityData.getCompoundTag(DATA);
    }

    private static void sync(EntityPlayer player, boolean active, boolean shielded, boolean force) {
        if (!(player instanceof EntityPlayerMP)) return;
        NBTTagCompound state = data(player);
        int tick = player.ticksExisted;
        if (!force && state.hasKey("syncDimension") && state.getInteger("syncDimension") == player.dimension
                && state.getBoolean("syncActive") == active && state.getBoolean("syncShield") == shielded
                && (!active || (tick - state.getInteger("lastSync") >= 0
                && tick - state.getInteger("lastSync") < 20))) return;
        state.setInteger("syncDimension", player.dimension);
        state.setBoolean("syncActive", active);
        state.setBoolean("syncShield", shielded);
        state.setInteger("lastSync", tick);
        GctNetworkHandler.CHANNEL.sendTo(new PacketYmirForce(active, shielded, player.dimension), (EntityPlayerMP) player);
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) { refresh(event.player, false); }

    @SubscribeEvent
    public void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) { refresh(event.player, true); }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) { refresh(event.player, true); }

    private static void refresh(EntityPlayer player, boolean resetJourney) {
        if (player.world.isRemote) return;
        NBTTagCompound state = data(player);
        if (resetJourney) state.removeTag("journey");
        state.setInteger("exposure", 0);
        YmirForceRules.normalizeTalismans(player);
        boolean active = player.world.provider instanceof WorldProviderJotunheim;
        sync(player, active, active && shelteredWell(player) != null, true);
    }
}
