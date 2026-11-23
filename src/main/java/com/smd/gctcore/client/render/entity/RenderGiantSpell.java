package com.smd.gctcore.client.render.entity;

import com.smd.gctcore.common.entity.EntityGiantSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGiantSpell extends RenderSnowball<EntityGiantSpell> {
    public RenderGiantSpell(RenderManager manager) {
        super(manager, Items.FIRE_CHARGE, Minecraft.getMinecraft().getRenderItem());
    }

    @Override
    public ItemStack getStackToRender(EntityGiantSpell entity) {
        return new ItemStack(entity.getSpellType() == EntityGiantSpell.HEX ? Items.ENDER_PEARL : Items.FIRE_CHARGE);
    }

    @Override
    public void doRender(EntityGiantSpell entity, double x, double y, double z, float yaw, float partialTicks) {
        if (entity.getSpellType() != EntityGiantSpell.GLOOM) {
            super.doRender(entity, x, y, z, yaw, partialTicks);
        }
    }
}
