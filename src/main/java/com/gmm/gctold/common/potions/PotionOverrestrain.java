package com.gmm.gctold.common.potions;

import com.smd.gctcore.Tags;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class PotionOverrestrain extends Potion {
    public static final PotionOverrestrain potion = new PotionOverrestrain();

    private PotionOverrestrain() {
        super(false, 0xCC00CC);
        setBeneficial();
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "overrestrain"));
        setPotionName("effect.overrestrain");
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(PotionEffect effect) {
        return false;
    }
}
