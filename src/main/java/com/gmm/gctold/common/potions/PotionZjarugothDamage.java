package com.gmm.gctold.common.potions;

import com.smd.gctcore.Tags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class PotionZjarugothDamage extends Potion {
    public static final PotionZjarugothDamage potion = new PotionZjarugothDamage();

    private PotionZjarugothDamage() {
        super(true, 0xFFFFFF);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "zjarugoth_damage"));
        setPotionName("effect.zjarugoth_damage");
    }

    @Override
    public boolean isInstant() {
        return true;
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(PotionEffect effect) {
        return false;
    }

    @Override
    public void affectEntity(Entity source, Entity indirectSource, EntityLivingBase entity, int amplifier, double health) {
        entity.setHealth(entity.getHealth() - 10.0F);
    }
}
