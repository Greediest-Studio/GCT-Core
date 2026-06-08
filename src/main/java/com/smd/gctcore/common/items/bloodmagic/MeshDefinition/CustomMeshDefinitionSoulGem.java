package com.smd.gctcore.common.items.bloodmagic.MeshDefinition;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
import com.smd.gctcore.Tags;
import com.smd.gctcore.common.items.bloodmagic.soulgem.SoulGem;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class CustomMeshDefinitionSoulGem implements ItemMeshDefinition {

    //根据类型和意志类型返回指定type
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof SoulGem) {
            SoulGem gem = (SoulGem) stack.getItem();
            EnumDemonWillType type = gem.getCurrentType(stack);
            int metadata = stack.getItemDamage();
            String variantName = SoulGem.names[metadata].toLowerCase() + "_" + type.getName().toLowerCase();

            return new ModelResourceLocation(new ResourceLocation(Tags.MOD_ID, "bloodmagic/soul_gem"), "type=" + variantName);
        }
        return new ModelResourceLocation(new ResourceLocation(Tags.MOD_ID, "bloodmagic/soul_gem"), "type=improved_default");
    }
}
