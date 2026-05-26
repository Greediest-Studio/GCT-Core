package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.common.entity.botania.EntityAlfSpark;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renderer for EntityAlfSpark. Color: #C0CCE0
 */
@SideOnly(Side.CLIENT)
public class RenderAlfSpark extends RenderCustomSpark<EntityAlfSpark> {

    // #C0CCE0 → R=0xC0=192, G=0xCC=204, B=0xE0=224
    private static final float R = 192f / 255f;
    private static final float G = 204f / 255f;
    private static final float B = 224f / 255f;

    public RenderAlfSpark(RenderManager manager) {
        super(manager);
    }

    @Override
    protected float getR() { return R; }

    @Override
    protected float getG() { return G; }

    @Override
    protected float getB() { return B; }
}
