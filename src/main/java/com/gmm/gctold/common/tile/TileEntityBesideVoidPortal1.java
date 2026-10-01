package com.gmm.gctold.common.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityBesideVoidPortal1 extends TileEntityLockableLoot {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);

    @Override
    public int getSizeInventory() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    @Override
    public String getName() {
        return "container.beside_void_portal_1";
    }

    @Override
    public String getGuiID() {
        return "gctcore:beside_void_portal_1";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        stacks = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        if (!checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, stacks);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, stacks);
        }
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        fillWithLoot(playerIn);
        return new ContainerChest((IInventory) playerInventory, this, playerIn);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return stacks;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
