package com.smd.gctcore.common.events;

import com.smd.gctcore.Tags;
import com.smd.gctcore.misc.PotionsItemRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.EnumSet;

/**
 * Keeps a rooted player at the impact's X/Z, while allowing gravity and camera/item use.
 * Per-player anchors live on the entity itself, never in a static player/world cache.
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ImmobilizedHandler {

    private static final String ANCHOR = "gctcore:immobilized_anchor";

    private ImmobilizedHandler() {
    }

    public static boolean isImmobilized(EntityPlayer player) {
        return PotionsItemRegistry.IMMOBILIZED != null
                && player.isEntityAlive() && !player.isCreative() && !player.isSpectator()
                && player.isPotionActive(PotionsItemRegistry.IMMOBILIZED);
    }

    private static NBTTagCompound anchor(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(ANCHOR, 10)
                || data.getCompoundTag(ANCHOR).getInteger("dimension") != player.dimension) {
            NBTTagCompound anchor = new NBTTagCompound();
            anchor.setInteger("dimension", player.dimension);
            anchor.setDouble("x", player.posX);
            anchor.setDouble("y", player.posY);
            anchor.setDouble("z", player.posZ);
            data.setTag(ANCHOR, anchor);
        }
        return data.getCompoundTag(ANCHOR);
    }

    @SubscribeEvent
    public static void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer
                && event.getPotionEffect().getPotion() == PotionsItemRegistry.IMMOBILIZED) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (!player.world.isRemote && !player.getEntityData().hasKey(ANCHOR, 10)) {
                anchor(player);
            }
            stopMotion(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (!isImmobilized(player)) {
            player.getEntityData().removeTag(ANCHOR);
            // Switching to an invulnerable game mode releases the movement attribute too.
            if (!player.world.isRemote && (player.isCreative() || player.isSpectator())
                    && PotionsItemRegistry.IMMOBILIZED != null) {
                player.removePotionEffect(PotionsItemRegistry.IMMOBILIZED);
            }
            return;
        }

        stopMotion(player);
        if (player.world.isRemote) {
            return;
        }

        NBTTagCompound anchor = anchor(player);
        double x = anchor.getDouble("x");
        double z = anchor.getDouble("z");
        // A rooted player can fall; jumps, stepping and upward knockback cannot escape the lock.
        double y = Math.min(player.posY, anchor.getDouble("y"));
        anchor.setDouble("y", y);
        if (Math.abs(player.posX - x) > 1.0E-6D || Math.abs(player.posZ - z) > 1.0E-6D
                || player.posY > y + 1.0E-6D) {
            if (player instanceof EntityPlayerMP && ((EntityPlayerMP) player).connection != null) {
                // Resynchronize prediction as well as the server-side position.
                ((EntityPlayerMP) player).connection.setPlayerLocation(
                        x, y, z, 0.0F, 0.0F, EnumSet.of(
                                SPacketPlayerPosLook.EnumFlags.X_ROT,
                                SPacketPlayerPosLook.EnumFlags.Y_ROT));
            } else {
                player.setPosition(x, y, z);
            }
        }
    }

    private static void stopMotion(EntityPlayer player) {
        player.motionX = 0.0D;
        player.motionZ = 0.0D;
        if (player.motionY > 0.0D) {
            player.motionY = 0.0D;
        }
        player.setSprinting(false);
        player.moveForward = 0.0F;
        player.moveStrafing = 0.0F;
        player.setJumping(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJump(LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer
                && isImmobilized((EntityPlayer) event.getEntityLiving())) {
            stopMotion((EntityPlayer) event.getEntityLiving());
        }
    }

    private static void clear(EntityPlayer player) {
        player.getEntityData().removeTag(ANCHOR);
        if (!player.world.isRemote && PotionsItemRegistry.IMMOBILIZED != null) {
            player.removePotionEffect(PotionsItemRegistry.IMMOBILIZED);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            clear((EntityPlayer) event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        clear(event.player);
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        clear(event.player);
    }

    @SubscribeEvent
    public static void onClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        clear(event.getOriginal());
        clear(event.getEntityPlayer());
    }
}
