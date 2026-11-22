package com.smd.gctcore.common.potions;

import com.smd.gctcore.Tags;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

/** A synchronized movement lock; the matching event handlers also stop jumps and knockback. */
public final class PotionImmobilized extends Potion {

    public PotionImmobilized() {
        super(true, 0x8C7853);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "immobilized"));
        setPotionName("effect.gctcore.immobilized");
        registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
                "A371D1CC-4C92-4A22-B594-D2B2A84EC583", -1.0D, 2);
    }
}
