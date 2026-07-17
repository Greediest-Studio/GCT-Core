package com.smd.gctcore.common.items.mekanism;

import com.smd.gctcore.common.integration.mekanism.DigitalMinerHarvestAccess;
import com.smd.gctcore.common.integration.mekanism.GctMekanismUpgrades;
import com.smd.gctcore.common.integration.mekanism.MekanismHarvestHelper;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
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

/**
 * Mekanism upgrade that grants Digital Miner a harvest level from item NBT.
 * NBT key defaults to {@code gctMiningLevel} (configurable). Default level is 0.
 */
public class ItemMiningLevelUpgrade extends Item implements IUpgradeItem {

    public ItemMiningLevelUpgrade() {
        setRegistryName("mining_level_upgrade");
        setTranslationKey("gctcore.mining_level_upgrade");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }

    @Nonnull
    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        MekanismHarvestHelper.writeMiningLevelToStack(stack, 0);
        return stack;
    }

    @Override
    public Upgrade getUpgradeType(ItemStack stack) {
        return GctMekanismUpgrades.MINING_LEVEL;
    }

    @Override
    public boolean canInstallUpgrade(ItemStack stack, IUpgradeTile tile) {
        return tile instanceof TileEntityDigitalMiner;
    }

    @Override
    public void onInstalled(ItemStack stack, IUpgradeTile tile, int amount) {
        if (amount > 0 && tile instanceof DigitalMinerHarvestAccess) {
            ((DigitalMinerHarvestAccess) tile).gct$setMiningLevel(MekanismHarvestHelper.readMiningLevelFromStack(stack));
        }
    }

    @Override
    public ItemStack getUninstalledStack(IUpgradeTile tile, Upgrade upgrade, int amount, ItemStack defaultStack) {
        ItemStack stack = defaultStack.isEmpty()
                ? new ItemStack(this, Math.max(1, amount))
                : defaultStack.copy();
        stack.setCount(Math.max(1, amount));
        int level = 0;
        if (tile instanceof DigitalMinerHarvestAccess) {
            level = ((DigitalMinerHarvestAccess) tile).gct$getMiningLevel();
        }
        MekanismHarvestHelper.writeMiningLevelToStack(stack, level);
        return stack;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        int level = MekanismHarvestHelper.readMiningLevelFromStack(stack);
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.gctcore.mining_level_upgrade.desc"));
        tooltip.add(TextFormatting.AQUA + I18n.format("tooltip.gctcore.mining_level_upgrade.level", level));
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("tooltip.gctcore.mining_level_upgrade.install"));
    }
}
