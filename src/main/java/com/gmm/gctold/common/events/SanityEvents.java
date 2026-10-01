package com.gmm.gctold.common.events;

import com.gmm.gctold.common.entity.EntityAncientShoggoth;
import com.gmm.gctold.common.entity.EntityBloodyShoggoth;
import com.gmm.gctold.common.entity.EntityBlueFlameBeholder;
import com.gmm.gctold.common.entity.EntityDarkerLesserShoggoth;
import com.gmm.gctold.common.entity.EntityMixtureShoggoth;
import com.gmm.gctold.common.entity.EntityRemnantWandering;
import com.gmm.gctold.common.entity.EntityShadowBase;
import com.shinoow.abyssalcraft.api.entity.IAntiEntity;
import com.shinoow.abyssalcraft.api.entity.ICoraliumEntity;
import com.shinoow.abyssalcraft.api.entity.IDreadEntity;
import com.shinoow.abyssalcraft.api.entity.IOmotholEntity;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.UUID;

public final class SanityEvents {
    private static final String FINAL_STAGE = "final";
    private static final String SANITY_KEY = "sanityAbyss";
    private static final float ATTACK_SANITY_COST = 0.5F;
    private static final float FRIENDLY_SANITY_THRESHOLD = -30.0F;
    private static final int FRIENDLY_TARGET_CLEAR_INTERVAL = 10;
    private static final double FRIENDLY_TARGET_CLEAR_RANGE = 32.0D;
    private static final UUID SANITY_MOVEMENT_SPEED_UUID = UUID.fromString("ab781730-70d4-4b2e-8b19-19eac979a8f6");
    private static final String SANITY_MOVEMENT_SPEED_NAME = "gctcore.sanity_movement_speed";
    private static final double ATTRIBUTE_AMOUNT_EPSILON = 1.0E-6D;
    private static final Class<?>[] GCT_SANITY_TARGETS = {
            EntityBloodyShoggoth.BloodyShoggothEntity.class,
            EntityShadowBase.ShadowBaseEntity.class,
            EntityRemnantWandering.RemnantWanderingEntity.class,
            EntityMixtureShoggoth.MixtureShoggothEntity.class,
            EntityAncientShoggoth.AncientShoggothEntity.class,
            EntityDarkerLesserShoggoth.DarkerLesserShoggothEntity.class,
            EntityBlueFlameBeholder.BlueFlameBeholderEntity.class
    };
    private static final String[] ABYSSALCRAFT_BOSS_NAMES = {
            "chagaroth",
            "dragonboss",
            "jzahar",
            "j'zahar",
            "sacthoth"
    };

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (event.isCanceled() || player.world.isRemote || !GameStageHelper.hasStage(player, FINAL_STAGE)) {
            return;
        }
        if (!(event.getTarget() instanceof EntityLivingBase) || !isSanityTarget(event.getTarget())) {
            return;
        }

        float sanity = player.getEntityData().getFloat(SANITY_KEY);
        if (sanity >= ATTACK_SANITY_COST) {
            player.getEntityData().setFloat(SANITY_KEY, MathHelper.clamp(sanity - ATTACK_SANITY_COST, -100.0F, 100.0F));
        }
    }

    @SubscribeEvent
    public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (!(event.getTarget() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getTarget();
        if (isFriendlyToSanityPlayer(event.getEntityLiving(), player)) {
            clearCombatTarget(event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        Entity attacker = event.getSource().getTrueSource();
        if (attacker == null) {
            attacker = event.getSource().getImmediateSource();
        }
        if (attacker instanceof EntityLivingBase && isFriendlyToSanityPlayer(attacker, player)) {
            clearCombatTarget((EntityLivingBase) attacker);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFriendlyTargetHurt(LivingHurtEvent event) {
        Entity attacker = event.getSource().getTrueSource();
        if (!(attacker instanceof EntityPlayer) || !isFriendlyToSanityPlayer(event.getEntityLiving(), (EntityPlayer) attacker)) {
            return;
        }
        clearCombatTarget(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote) {
            return;
        }

        updateMovementSpeedModifier(player);

        if (player.ticksExisted % FRIENDLY_TARGET_CLEAR_INTERVAL != 0 || !isSanityFriendlyPlayer(player)) {
            return;
        }

        AxisAlignedBB area = player.getEntityBoundingBox().grow(FRIENDLY_TARGET_CLEAR_RANGE);
        for (EntityLivingBase entity : player.world.getEntitiesWithinAABB(EntityLivingBase.class, area,
                SanityEvents::isSanityTarget)) {
            if (entity instanceof EntityLiving && ((EntityLiving) entity).getAttackTarget() == player) {
                clearCombatTarget(entity);
            } else if (entity.getRevengeTarget() == player) {
                clearCombatTarget(entity);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (event.isCanceled() || player.world.isRemote || !GameStageHelper.hasStage(player, FINAL_STAGE)) {
            return;
        }
        if (player.getEntityData().getFloat(SANITY_KEY) <= -50.0F) {
            player.sendStatusMessage(new TextComponentString("\u00a7e\u4f60\u611f\u5230\u4e00\u9635\u7cbe\u795e\u604d\u60da\uff0c\u8fd9\u8ba9\u4f60\u65e0\u6cd5\u601d\u8003\u63a5\u4e0b\u6765\u7684\u884c\u52a8\u3002"), true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityLivingDamage(LivingDamageEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (GameStageHelper.hasStage(player, FINAL_STAGE)) {
                float sanity = player.getEntityData().getFloat(SANITY_KEY);
                float mtp = (100.0F - sanity) / 200.0F;
                event.setAmount(event.getAmount() * (1.0F + mtp * 4.0F));
            }
        }

        Entity source = event.getSource().getTrueSource();
        if (source instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) source;
            if (GameStageHelper.hasStage(player, FINAL_STAGE)) {
                float sanity = player.getEntityData().getFloat(SANITY_KEY);
                float mtp = (100.0F - sanity) / 200.0F;
                event.setAmount(event.getAmount() * (1.0F + mtp * 2.0F));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (GameStageHelper.hasStage(player, FINAL_STAGE)) {
            float sanity = player.getEntityData().getFloat(SANITY_KEY);
            float mtp = (100.0F - sanity) / 200.0F;
            event.setNewSpeed(event.getNewSpeed() * (1.0F - mtp));
        }
    }

    private static boolean isFriendlyToSanityPlayer(Entity target, EntityPlayer player) {
        return isSanityFriendlyPlayer(player) && isSanityTarget(target);
    }

    private static void updateMovementSpeedModifier(EntityPlayer player) {
        IAttributeInstance movementSpeed = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        AttributeModifier current = movementSpeed.getModifier(SANITY_MOVEMENT_SPEED_UUID);
        if (!GameStageHelper.hasStage(player, FINAL_STAGE)) {
            if (current != null) {
                movementSpeed.removeModifier(current);
            }
            return;
        }

        float sanity = player.getEntityData().getFloat(SANITY_KEY);
        double amount = (100.0F - sanity) / 200.0F;
        if (current != null && Math.abs(current.getAmount() - amount) <= ATTRIBUTE_AMOUNT_EPSILON) {
            return;
        }

        if (current != null) {
            movementSpeed.removeModifier(current);
        }
        movementSpeed.applyModifier(new AttributeModifier(
                SANITY_MOVEMENT_SPEED_UUID,
                SANITY_MOVEMENT_SPEED_NAME,
                amount,
                2).setSaved(false));
    }

    private static boolean isSanityFriendlyPlayer(EntityPlayer player) {
        return !player.world.isRemote
                && GameStageHelper.hasStage(player, FINAL_STAGE)
                && player.getEntityData().getFloat(SANITY_KEY) <= FRIENDLY_SANITY_THRESHOLD;
    }

    public static boolean canSanityTargetPlayer(EntityPlayer player) {
        return !isSanityFriendlyPlayer(player);
    }

    private static boolean isSanityTarget(Entity target) {
        return isGctSanityTarget(target) || isNonBossAbyssalCraftEntity(target);
    }

    private static boolean isGctSanityTarget(Entity target) {
        for (Class<?> targetClass : GCT_SANITY_TARGETS) {
            if (targetClass.isInstance(target)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNonBossAbyssalCraftEntity(Entity target) {
        return isAbyssalCraftEntity(target) && !isBossEntity(target);
    }

    private static boolean isAbyssalCraftEntity(Entity target) {
        ResourceLocation id = EntityList.getKey(target);
        return id != null && "abyssalcraft".equals(id.getNamespace())
                || target instanceof IAntiEntity
                || target instanceof ICoraliumEntity
                || target instanceof IDreadEntity
                || target instanceof IOmotholEntity;
    }

    private static boolean isBossEntity(Entity target) {
        ResourceLocation id = EntityList.getKey(target);
        String path = id == null ? "" : id.getPath().toLowerCase();
        String className = target.getClass().getName().toLowerCase();
        for (String bossName : ABYSSALCRAFT_BOSS_NAMES) {
            if (path.contains(bossName) || className.contains(bossName)) {
                return true;
            }
        }
        return false;
    }

    private static void clearCombatTarget(EntityLivingBase entity) {
        entity.setRevengeTarget(null);
        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).setAttackTarget(null);
        }
    }
}
