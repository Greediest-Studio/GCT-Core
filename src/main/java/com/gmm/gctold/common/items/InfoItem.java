package com.gmm.gctold.common.items;

import java.util.List;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InfoItem extends Item {
    private final String tooltip;
    private final boolean glowing;

    public InfoItem(String name) {
        this(name, 64, 0, null, false);
    }

    public InfoItem(String name, int maxStackSize, int maxDamage, String tooltip, boolean glowing) {
        this.tooltip = tooltip;
        this.glowing = glowing;
        setMaxDamage(maxDamage);
        setMaxStackSize(maxStackSize);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(GctAllCreativeTab.TAB);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (this.tooltip != null && !this.tooltip.isEmpty()) {
            tooltip.add(this.tooltip);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return glowing;
    }
}
