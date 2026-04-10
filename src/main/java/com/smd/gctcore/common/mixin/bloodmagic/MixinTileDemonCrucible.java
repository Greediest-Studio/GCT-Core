package com.smd.gctcore.common.mixin.bloodmagic;

import WayofTime.bloodmagic.demonAura.WorldDemonWillHandler;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.soul.IDemonWillConduit;
import WayofTime.bloodmagic.soul.IDemonWillGem;
import WayofTime.bloodmagic.soul.IDiscreteDemonWill;
import WayofTime.bloodmagic.tile.TileDemonCrucible;
import WayofTime.bloodmagic.tile.TileInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TileDemonCrucible.class)
public abstract class MixinTileDemonCrucible extends TileInventory implements ITickable, IDemonWillConduit, ISidedInventory {

    public MixinTileDemonCrucible() {
        super(1, "demonCrucible");
    }

    @Unique
    public final int newMaxWill = 2147483647;//TODO: 可视作单种区块意志上限，暂定为int上限，实际区块内恶魔意志上限和物品形式存储上限一样是double型
    @Unique
    public final double newGemDrainRate = 1024.0;//TODO: 交互速率
    @Shadow(remap = false)
    public int internalCounter = 0;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 100), remap = false)
    private int modifyMaxWill(int original) {
        return newMaxWill;
    }
    @ModifyConstant(method = "<init>", constant = @Constant(doubleValue = 10.0), remap = false)
    private double modifyGemDrainRate(double original) {
        return newGemDrainRate;
    }

    @Override
    public void update() {
        if (getWorld().isRemote) {
            return;
        }

        internalCounter++;
        ItemStack stack = this.getStackInSlot(0);

        if (getWorld().isBlockPowered(getPos())) {
            fillGemFromWillMap(stack);
        } else {
            drainGemToChunk(stack);
        }
    }

    @Unique
    private void fillGemFromWillMap(ItemStack stack) {
        if (!(stack.getItem() instanceof IDemonWillGem)) {
            return;
        }

        IDemonWillGem gemItem = (IDemonWillGem) stack.getItem();
        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            double currentAmount = WorldDemonWillHandler.getCurrentWill(getWorld(), pos, type);
            if (currentAmount > 0) {
                double drainAmount = Math.min(newGemDrainRate, currentAmount);
                double filled = WorldDemonWillHandler.drainWill(getWorld(), pos, type, drainAmount, false);
                filled = gemItem.fillWill(type, stack, filled, false);

                if (filled > 0) {
                    filled = WorldDemonWillHandler.drainWill(getWorld(), pos, type, filled, true);
                    gemItem.fillWill(type, stack, filled, true);
                }
            }
        }
    }

    @Unique
    private void drainGemToChunk(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (stack.getItem() instanceof IDemonWillGem) {
            drainGemWillToChunk(stack);
        } else if (stack.getItem() instanceof IDiscreteDemonWill) {
            drainDiscreteWillToChunk(stack);
        }
    }

    @Unique
    private void drainGemWillToChunk(ItemStack stack) {
        IDemonWillGem gemItem = (IDemonWillGem) stack.getItem();
        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            double currentAmount = WorldDemonWillHandler.getCurrentWill(getWorld(), pos, type);
            double drainAmount = Math.min(newMaxWill - currentAmount, newGemDrainRate);
            double filled = WorldDemonWillHandler.fillWillToMaximum(getWorld(), pos, type, drainAmount, newMaxWill, false);
            filled = gemItem.drainWill(type, stack, filled, false);

            if (filled > 0) {
                filled = gemItem.drainWill(type, stack, filled, true);
                WorldDemonWillHandler.fillWillToMaximum(getWorld(), pos, type, filled, newMaxWill, true);
            }
        }
    }

    @Unique
    private void drainDiscreteWillToChunk(ItemStack stack) {
        IDiscreteDemonWill willItem = (IDiscreteDemonWill) stack.getItem();
        EnumDemonWillType type = willItem.getType(stack);
        double currentAmount = WorldDemonWillHandler.getCurrentWill(getWorld(), pos, type);
        double needed = newMaxWill - currentAmount;
        double discreteAmount = willItem.getDiscretization(stack);

        if (needed >= discreteAmount) {
            double filled = willItem.drainWill(stack, discreteAmount);
            if (filled > 0) {
                WorldDemonWillHandler.fillWillToMaximum(getWorld(), pos, type, filled, newMaxWill, true);
                if (stack.getCount() <= 0) {
                    this.setInventorySlotContents(0, ItemStack.EMPTY);
                }
            }
        }
    }

}
