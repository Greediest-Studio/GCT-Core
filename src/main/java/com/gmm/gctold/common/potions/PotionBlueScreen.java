package com.gmm.gctold.common.potions;

import com.smd.gctcore.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionBlueScreen extends Potion {
    public static final PotionBlueScreen potion = new PotionBlueScreen();
    public static final PotionType potionType = new PotionType(new PotionEffect(potion, 3600))
            .setRegistryName(new ResourceLocation(Tags.MOD_ID, "blue_screen"));

    private final ResourceLocation icon = new ResourceLocation(Tags.MOD_ID, "textures/mob_effect/blue_screen.png");

    private PotionBlueScreen() {
        super(false, 0x00FFFF);
        setBeneficial();
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "blue_screen"));
        setPotionName("effect.blue_screen");
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
