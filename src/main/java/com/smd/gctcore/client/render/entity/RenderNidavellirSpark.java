package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.common.botania.GctManaPoolTier;
import com.smd.gctcore.common.entity.botania.EntityNidavellirSpark;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Renderer tinted with the average colour of the Nidavellir mana-pool rock. */
@SideOnly(Side.CLIENT)
public class RenderNidavellirSpark extends RenderCustomSpark<EntityNidavellirSpark> {

    private static final int COLOR = GctManaPoolTier.NIDAVELLIR.getColor();
    private static final float R = ((COLOR >> 16) & 0xFF) / 255F;
    private static final float G = ((COLOR >> 8) & 0xFF) / 255F;
    private static final float B = (COLOR & 0xFF) / 255F;

    public RenderNidavellirSpark(RenderManager manager) {
        super(manager);
    }

    @Override
    protected float getR() { return R; }

    @Override
    protected float getG() { return G; }

    @Override
    protected float getB() { return B; }
}
