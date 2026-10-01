package com.gmm.gctold.common.potions;

import com.smd.gctcore.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionCurseOfTwilight extends Potion {
    public static final PotionCurseOfTwilight potion = new PotionCurseOfTwilight();
    public static final PotionType potionType = new PotionType(new PotionEffect(potion, 3600))
            .setRegistryName(new ResourceLocation(Tags.MOD_ID, "curse_of_twilight"));

    private final ResourceLocation icon = new ResourceLocation(Tags.MOD_ID, "textures/mob_effect/curse_of_twilight.png");

    private PotionCurseOfTwilight() {
        super(true, 0x9900C0);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "curse_of_twilight"));
        setPotionName("effect.curse_of_twilight");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.world.rand.nextDouble() < 0.5D) {
            entity.attackEntityFrom(DamageSource.GENERIC, entity.getMaxHealth() / (20 + entity.world.rand.nextInt(11)));
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
