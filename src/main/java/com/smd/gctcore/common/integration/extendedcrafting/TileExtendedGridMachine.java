package com.smd.gctcore.common.integration.extendedcrafting;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

abstract class TileExtendedGridMachine extends TileEntity implements IGridHost {
    private static final String NODE_TAG = "ae2Node";
    protected ExtendedCraftingTier tier = ExtendedCraftingTier.BASIC;
    private IGridNode node;
    private NBTTagCompound nodeData;

    TileExtendedGridMachine() {
    }

    TileExtendedGridMachine(ExtendedCraftingTier tier) {
        this.tier = tier;
    }

    public ExtendedCraftingTier tier() {
        return tier;
    }

    protected abstract BlockExtendedCraftingAutomation.Kind machineKind();

    protected abstract boolean requiresChannel();

    protected double idlePowerUsage() {
        return requiresChannel() ? 1.0D : 0.0D;
    }

    protected void onGridChanged() {
    }

    @Override
    public IGridNode getGridNode(AEPartLocation dir) {
        if (node == null && world != null && !world.isRemote && !isInvalid()) {
            node = AEApi.instance().grid().createGridNode(new ExtendedAutomationGridBlock(this));
            if (nodeData != null) {
                node.loadFromNBT(NODE_TAG, nodeData);
                nodeData = null;
            }
            node.updateState();
        }
        return node;
    }

    @Override
    public AECableType getCableConnectionType(AEPartLocation dir) {
        return requiresChannel() ? AECableType.SMART : AECableType.COVERED;
    }

    @Override
    public void securityBreak() {
        if (world != null) world.destroyBlock(pos, true);
    }

    @Override
    public void validate() {
        super.validate();
        if (world != null && !world.isRemote) {
            getGridNode(AEPartLocation.INTERNAL);
        }
    }

    @Override
    public void invalidate() {
        destroyNode();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        destroyNode();
        super.onChunkUnload();
    }

    private void destroyNode() {
        if (node != null) {
            node.destroy();
            node = null;
        }
    }

    protected void refreshNode() {
        IGridNode current = getGridNode(AEPartLocation.INTERNAL);
        if (current != null) current.updateState();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (node != null) node.saveToNBT(NODE_TAG, compound);
        compound.setInteger("Tier", tier.level());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        tier = ExtendedCraftingTier.byLevel(compound.getInteger("Tier"));
        nodeData = compound.copy();
    }
}
