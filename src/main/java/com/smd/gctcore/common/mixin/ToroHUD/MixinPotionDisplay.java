package com.smd.gctcore.common.mixin.ToroHUD;

import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.torocraft.torohud.display.PotionDisplay;
import net.torocraft.torohud.network.MessageEntityStatsResponse;
import org.spongepowered.asm.mixin.*;

import java.util.Collection;

import static net.torocraft.torohud.display.PotionDisplay.INVENTORY_BACKGROUND;

@Mixin(PotionDisplay.class)
public class MixinPotionDisplay {

    @Mutable
    @Final
    @Shadow(remap = false)
    private final Minecraft mc;
    @Mutable
    @Final
    @Shadow(remap = false)
    private final Gui gui;
    @Shadow(remap = false)
    private int x, y;

    public MixinPotionDisplay(Minecraft mc, Gui gui) {
        this.mc = mc;
        this.gui = gui;
    }

    @Shadow(remap = false)
    private String getAmplifierText(PotionEffect potioneffect) {return null;}

    /**
     * @author Gct-Core
     * @reason 报空
     */
    @Overwrite(remap = false)
    private void drawEffects() {
        Collection<PotionEffect> potions = MessageEntityStatsResponse.POTIONS;

        if (potions == null || potions.isEmpty()) {
            return;
        }

        int x = this.x + 3;
        int y = this.y + 18;

        for (PotionEffect potion : Ordering.natural().sortedCopy(potions)) {

            if (potion == null) {continue;}
            int textureIndex = potion.getPotion().getStatusIconIndex();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            mc.renderEngine.bindTexture(INVENTORY_BACKGROUND);
            gui.drawTexturedModalRect(x, y, textureIndex % 8 * 18, 198 + textureIndex / 8 * 18, 18, 18);

            String duration = Potion.getPotionDurationString(potion, 1.0F);
            mc.fontRenderer.drawStringWithShadow(duration, x, y + 18, 0xe0e0e0);

            mc.fontRenderer.drawStringWithShadow(getAmplifierText(potion), x, y, 0xc0c0c0);

            x += 24;
        }
    }
}
