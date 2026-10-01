package com.gmm.gctold.common.potions;

import com.smd.gctcore.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionChanneling extends Potion {
    public static final PotionChanneling potion = new PotionChanneling();
    public static final PotionType potionType = new PotionType(new PotionEffect(potion, 3600))
            .setRegistryName(new ResourceLocation(Tags.MOD_ID, "channeling"));

    private final ResourceLocation icon = new ResourceLocation(Tags.MOD_ID, "textures/mob_effect/channeling.png");

    private PotionChanneling() {
        super(true, 0x3300FF);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "channeling"));
        setPotionName("effect.channeling");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (!entity.world.isRemote && entity.world.rand.nextDouble() < 0.05D) {
            entity.world.addWeatherEffect(new EntityLightningBolt(entity.world, entity.posX, entity.posY, entity.posZ, false));
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
