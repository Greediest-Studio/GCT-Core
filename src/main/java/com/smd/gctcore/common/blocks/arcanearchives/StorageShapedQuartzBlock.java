package com.smd.gctcore.common.blocks.arcanearchives;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class StorageShapedQuartzBlock extends Block {

    public StorageShapedQuartzBlock() {
        super(Material.ROCK);
        setRegistryName("storage_shaped_quartz");
        setTranslationKey("gctcore.storage_shaped_quartz");
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setLightLevel(1.0F);
        setHardness(1.7F);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GOLD + I18n.format("tooltip.gctcore.storage_shaped_quartz"));
    }
}
