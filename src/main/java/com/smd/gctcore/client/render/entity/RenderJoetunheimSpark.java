package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.common.entity.botania.EntityJoetunheimSpark;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Renderer tinted with the average colour of the Joetunheim mana-pool rock. */
@SideOnly(Side.CLIENT)
public class RenderJoetunheimSpark extends RenderCustomSpark<EntityJoetunheimSpark> {

    private static final float R = 205F / 255F;
    private static final float G = 86F / 255F;
    private static final float B = 0F / 255F;

    public RenderJoetunheimSpark(RenderManager manager) {
        super(manager);
    }

    @Override
    protected float getR() { return R; }

    @Override
    protected float getG() { return G; }

    @Override
    protected float getB() { return B; }
}
