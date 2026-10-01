package com.gmm.gctold.common.potions;

import com.smd.gctcore.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionWorldEnd extends Potion {
    public static final PotionWorldEnd potion = new PotionWorldEnd();
    public static final PotionType potionType = new PotionType(new PotionEffect(potion, 3600))
            .setRegistryName(new ResourceLocation(Tags.MOD_ID, "world_end"));

    private static final Potion[] BUFFS = {
            MobEffects.SPEED,
            MobEffects.HASTE,
            MobEffects.STRENGTH,
            MobEffects.JUMP_BOOST,
            MobEffects.REGENERATION,
            MobEffects.RESISTANCE,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.HEALTH_BOOST
    };

    private final ResourceLocation icon = new ResourceLocation(Tags.MOD_ID, "textures/mob_effect/world_end.png");

    private PotionWorldEnd() {
        super(false, 0x000000);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "world_end"));
        setPotionName("effect.world_end");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        for (Potion potion : BUFFS) {
            PotionEffect current = entity.getActivePotionEffect(potion);
            if (current == null || current.getDuration() <= 2) {
                entity.addPotionEffect(new PotionEffect(potion, 5, 255, false, false));
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        if (mc.currentScreen != null) {
            mc.getTextureManager().bindTexture(icon);
            Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0.0F, 0.0F, 18, 18, 18.0F, 18.0F);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        mc.getTextureManager().bindTexture(icon);
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0.0F, 0.0F, 18, 18, 18.0F, 18.0F);
    }
}
