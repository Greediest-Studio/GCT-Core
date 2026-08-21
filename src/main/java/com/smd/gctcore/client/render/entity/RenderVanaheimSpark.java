package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.common.botania.GctManaPoolTier;
import com.smd.gctcore.common.entity.botania.EntityVanaheimSpark;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Renderer tinted with the average colour of the Vanaheim mana-pool rock. */
@SideOnly(Side.CLIENT)
public class RenderVanaheimSpark extends RenderCustomSpark<EntityVanaheimSpark> {

    private static final int COLOR = GctManaPoolTier.VANAHEIM.getColor();
    private static final float R = ((COLOR >> 16) & 0xFF) / 255F;
    private static final float G = ((COLOR >> 8) & 0xFF) / 255F;
    private static final float B = (COLOR & 0xFF) / 255F;

    public RenderVanaheimSpark(RenderManager manager) {
        super(manager);
    }

    @Override
    protected float getR() { return R; }

    @Override
    protected float getG() { return G; }

    @Override
    protected float getB() { return B; }
}
