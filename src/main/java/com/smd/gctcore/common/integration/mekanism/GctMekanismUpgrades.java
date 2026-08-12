package com.smd.gctcore.common.integration.mekanism;

import com.smd.gctcore.Tags;
import com.smd.gctcore.misc.ItemRegistry;
import mekanism.api.EnumColor;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Mekanism upgrades registered by Gct-Core.
 * <p>
 * Item: {@code gctcore:mining_level_upgrade}<br>
 * NBT key (default): {@code gctMiningLevel} — integer harvest level.
 */
public final class GctMekanismUpgrades {

    public static final int MAX_ENERGY_MK2_LEVEL = 8;
    public static final int MAX_ENERGY_MK2_STACK_SIZE = 64;
    public static Upgrade MINING_LEVEL;
    public static Upgrade ENERGY_MK2;

    private GctMekanismUpgrades() {
    }

    public static void init() {
        if (!Loader.isModLoaded("mekanism") || MINING_LEVEL != null) {
            return;
        }
        MINING_LEVEL = Upgrade.builder(Tags.MOD_ID, "mining_level")
                .maxInstalled(1)
                .maxItemStackSize(1)
                .color(EnumColor.ORANGE)
                .stack(GctMekanismUpgrades::createStack)
                .info(GctMekanismUpgrades::miningLevelInfo)
                .register();
        ENERGY_MK2 = Upgrade.builder(Tags.MOD_ID, "energy_mk2")
                .maxInstalled(MAX_ENERGY_MK2_LEVEL)
                .maxItemStackSize(MAX_ENERGY_MK2_STACK_SIZE)
                .color(EnumColor.AQUA)
                .stack(GctMekanismUpgrades::createEnergyMk2Stack)
                .info(GctMekanismUpgrades::energyMk2Info)
                .conflictsWith(Upgrade.ENERGY)
                .register();
    }

    private static ItemStack createStack(int count) {
        if (ItemRegistry.MINING_LEVEL_UPGRADE == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(ItemRegistry.MINING_LEVEL_UPGRADE, Math.max(1, count));
        MekanismHarvestHelper.writeMiningLevelToStack(stack, 0);
        return stack;
    }

    /**
     * Shown in the upgrade window when this upgrade is selected.
     */
    private static List<String> miningLevelInfo(Upgrade upgrade, IUpgradeTile tile) {
        List<String> info = new ArrayList<>(1);
        int level = 0;
        if (tile instanceof DigitalMinerHarvestAccess) {
            level = ((DigitalMinerHarvestAccess) tile).gct$getMiningLevel();
        }
        info.add(LangUtils.localize("upgrade.mining_level.info") + ": " + level);
        return info;
    }

    public static boolean isMiningLevelUpgrade(Upgrade upgrade) {
        return MINING_LEVEL != null && upgrade == MINING_LEVEL;
    }

    private static ItemStack createEnergyMk2Stack(int count) {
        if (ItemRegistry.ENERGY_MK2_UPGRADE == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(ItemRegistry.ENERGY_MK2_UPGRADE, Math.max(1, count));
    }

    public static List<String> energyMk2Info(Upgrade upgrade, IUpgradeTile tile) {
        List<String> info = new ArrayList<>(1);
        int installed = tile == null ? 0 : tile.getInstalledUpgrades(upgrade);
        double multiplier = Math.pow(2, installed);
        info.add(LangUtils.localize("gui.upgrades.effect") + ": " + MekanismUtils.exponential(multiplier) + "x");
        return info;
    }

    public static boolean isEnergyMk2Upgrade(Upgrade upgrade) {
        return ENERGY_MK2 != null && upgrade == ENERGY_MK2;
    }
}
