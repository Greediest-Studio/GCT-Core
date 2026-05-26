package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.common.entity.botania.EntityGaiaSpark;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renderer for EntityGaiaSpark. Color: #97808B
 */
@SideOnly(Side.CLIENT)
public class RenderGaiaSpark extends RenderCustomSpark<EntityGaiaSpark> {

    // #97808B → R=0x97=151, G=0x80=128, B=0x8B=139
    private static final float R = 151f / 255f;
    private static final float G = 128f / 255f;
    private static final float B = 139f / 255f;

    public RenderGaiaSpark(RenderManager manager) {
        super(manager);
    }

    @Override
    protected float getR() { return R; }

    @Override
    protected float getG() { return G; }

    @Override
    protected float getB() { return B; }
}
