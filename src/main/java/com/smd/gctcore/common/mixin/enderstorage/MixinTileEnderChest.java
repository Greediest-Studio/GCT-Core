package com.smd.gctcore.common.mixin.enderstorage;

import codechicken.enderstorage.storage.EnderItemStorage;
import codechicken.enderstorage.tile.TileEnderChest;
import codechicken.enderstorage.tile.TileFrequencyOwner;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import java.util.List;

@Mixin(TileEnderChest.class)
public abstract class MixinTileEnderChest extends TileFrequencyOwner {


    @Shadow(remap = false)
    public EnderItemStorage getStorage() {return null;}
    @Shadow(remap = false)
    private List<EnumFacing> emptySides;
    /**
     * @author Gct-Core
     * @reason 修复自动输出功能
     */
    @Overwrite(remap = false)
    private void pushItems() {
        emptySides.clear();
        for(ItemStack stack: getStorage().getInventory()) {
            if(stack.isEmpty()) continue;
            for (EnumFacing side: EnumFacing.VALUES) {
                if(emptySides.contains(side)) continue;
                TileEntity te = world.getTileEntity(getPos().offset(side));
                if(te == null || te instanceof TileEnderChest) {
                    emptySides.add(side);
                    continue;
                }
                IItemHandler inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
                if(inventory == null) {
                    emptySides.add(side);
                    continue;
                }

                for(int i = 0; i < inventory.getSlots();i++) {
                    ItemStack left = inventory.insertItem(i, stack, true);
                    if(left.getCount() > 0) {
                        int toInsert = stack.getCount() - left.getCount();
                        stack.shrink(toInsert);
                        ItemStack insertStack = stack.copy();
                        insertStack.setCount(toInsert);
                        inventory.insertItem(i, insertStack, false);
                        getStorage().setDirty();
                    } else {
                        inventory.insertItem(i, stack.copy(), false);
                        stack.setCount(0);
                        getStorage().setDirty();
                    }
                }
            }
        }
    }
}
