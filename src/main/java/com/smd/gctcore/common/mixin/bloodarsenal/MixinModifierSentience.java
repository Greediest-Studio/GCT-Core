package com.smd.gctcore.common.mixin.bloodarsenal;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.soul.PlayerDemonWillHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.Tags;

/**
 * Fixes BloodArsenal's Sentience II modifier overwriting {@code Stats.Attack} from
 * {@code StatsOriginal.Attack} in {@code onUpdate}, which wiped Sharpness and any other
 * attack-modifying modifiers or traits.
 */
@Pseudo
@Mixin(targets = "arcaratus.bloodarsenal.compat.tconstruct.ModifierSentience", remap = false)
public abstract class MixinModifierSentience {

    @Unique
    private static final String GCT_SENTIENCE_ATTACK_BONUS_KEY = "GctSentienceAttackBonus";

    @Shadow(remap = false)
    public static double[] soulDrainPerSwing;

    @Shadow(remap = false)
    public static double[] soulDrop;

    @Shadow(remap = false)
    public static double[] staticDrop;

    @Shadow(remap = false)
    private int getLevel(ItemStack tool) {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    private int getWillLevel(double soulsRemaining) {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    public abstract double getExtraDamage(EnumDemonWillType type, int willBracket);

    @Shadow(remap = false)
    public abstract void setCurrentType(ItemStack stack, EnumDemonWillType type);

    @Shadow(remap = false)
    public abstract void setDrainOfActivatedSword(ItemStack stack, double drain);

    @Shadow(remap = false)
    public abstract void setStaticDropOfActivatedSword(ItemStack stack, double drop);

    @Shadow(remap = false)
    public abstract void setDropOfActivatedSword(ItemStack stack, double drop);

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void gctcore$preserveToolAttackModifiersOnUpdate(
            ItemStack tool,
            World world,
            Entity entity,
            int itemSlot,
            boolean isSelected,
            CallbackInfo ci
    ) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            EnumDemonWillType largestType = PlayerDemonWillHandler.getLargestWillType(player);
            double will = PlayerDemonWillHandler.getTotalDemonWill(largestType, player);
            EnumDemonWillType activeType = will > 0 ? largestType : EnumDemonWillType.DEFAULT;

            setCurrentType(tool, activeType);
            int willBracket = getWillLevel(will);
            double drain = willBracket >= 0 ? soulDrainPerSwing[willBracket] : 0.0D;

            gctcore$updateSentienceAttackBonus(tool, activeType, willBracket);

            setDrainOfActivatedSword(tool, drain);
            setStaticDropOfActivatedSword(tool, willBracket >= 0 ? staticDrop[willBracket] : 1.0D);
            setDropOfActivatedSword(tool, willBracket >= 0 ? soulDrop[willBracket] : 0.0D);
        }
        ci.cancel();
    }

    @Unique
    private void gctcore$updateSentienceAttackBonus(ItemStack tool, EnumDemonWillType type, int willBracket) {
        NBTTagCompound root = tool.getTagCompound();
        if (root == null || !root.hasKey(Tags.TOOL_DATA, Constants.NBT.TAG_COMPOUND)) {
            return;
        }
        NBTTagCompound statsTag = TagUtil.getToolTag(root);
        float previousBonus = statsTag.getFloat(GCT_SENTIENCE_ATTACK_BONUS_KEY);
        if (!Float.isFinite(previousBonus)) {
            previousBonus = 0.0F;
        }
        float newBonus = getLevel(tool) > 1 ? (float) getExtraDamage(type, willBracket) : 0.0F;
        if (!Float.isFinite(newBonus)) {
            newBonus = 0.0F;
        }
        if (Float.compare(previousBonus, newBonus) != 0) {
            float currentAttack = statsTag.getFloat(Tags.ATTACK);
            statsTag.setFloat(Tags.ATTACK, currentAttack - previousBonus + newBonus);
            if (newBonus == 0.0F) {
                statsTag.removeTag(GCT_SENTIENCE_ATTACK_BONUS_KEY);
            } else {
                statsTag.setFloat(GCT_SENTIENCE_ATTACK_BONUS_KEY, newBonus);
            }
        }
    }
}
