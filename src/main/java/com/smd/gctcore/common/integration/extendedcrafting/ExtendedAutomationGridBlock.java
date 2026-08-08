package com.smd.gctcore.common.integration.extendedcrafting;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.EnumSet;

final class ExtendedAutomationGridBlock implements IGridBlock {
    private final TileExtendedGridMachine tile;

    ExtendedAutomationGridBlock(TileExtendedGridMachine tile) {
        this.tile = tile;
    }

    @Override public double getIdlePowerUsage() { return tile.idlePowerUsage(); }
    @Override @Nonnull public EnumSet<GridFlags> getFlags() {
        return tile.requiresChannel() ? EnumSet.of(GridFlags.REQUIRE_CHANNEL) : EnumSet.noneOf(GridFlags.class);
    }
    @Override public boolean isWorldAccessible() { return true; }
    @Override @Nonnull public DimensionalCoord getLocation() { return new DimensionalCoord(tile); }
    @Override @Nonnull public AEColor getGridColor() { return AEColor.TRANSPARENT; }
    @Override public void onGridNotification(@Nonnull GridNotification notification) { }
    @Override public void setNetworkStatus(IGrid grid, int channelsInUse) { }
    @Override @Nonnull public EnumSet<EnumFacing> getConnectableSides() { return EnumSet.allOf(EnumFacing.class); }
    @Override @Nonnull public IGridHost getMachine() { return tile; }
    @Override public void gridChanged() { tile.onGridChanged(); }
    @Override @Nonnull public ItemStack getMachineRepresentation() {
        return new ItemStack(ExtendedCraftingAutomation.block(tile.machineKind(), tile.tier()));
    }
}
