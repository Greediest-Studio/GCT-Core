package com.smd.gctcore.common.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class RawQuartzItem extends Item {

    public RawQuartzItem() {
        setRegistryName("raw_quartz");
        setTranslationKey("gctcore.raw_quartz");
        setCreativeTab(CreativeTabs.MATERIALS);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GOLD + I18n.format("tooltip.gctcore.raw_quartz"));
    }
}
