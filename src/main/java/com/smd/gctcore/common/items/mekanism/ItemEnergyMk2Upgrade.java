package com.smd.gctcore.common.items.mekanism;

import com.smd.gctcore.common.integration.mekanism.GctMekanismUpgrades;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.base.IUpgradeTile;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemEnergyMk2Upgrade extends Item implements IUpgradeItem {

    public ItemEnergyMk2Upgrade() {
        setRegistryName("energy_mk2_upgrade");
        setTranslationKey("gctcore.energy_mk2_upgrade");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(GctMekanismUpgrades.MAX_ENERGY_MK2_STACK_SIZE);
    }

    @Nonnull
    @Override
    public ItemStack getDefaultInstance() {
        return super.getDefaultInstance();
    }

    @Override
    public Upgrade getUpgradeType(ItemStack stack) {
        return GctMekanismUpgrades.ENERGY_MK2;
    }

    @Override
    public boolean canInstallUpgrade(ItemStack stack, IUpgradeTile tile) {
        return tile != null && tile.supportsUpgrade(Upgrade.ENERGY);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.gctcore.energy_mk2_upgrade.desc"));
        tooltip.add(TextFormatting.AQUA + I18n.format("tooltip.gctcore.energy_mk2_upgrade.multiplier", 2));
    }
}
