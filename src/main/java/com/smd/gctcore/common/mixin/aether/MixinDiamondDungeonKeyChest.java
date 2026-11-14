package com.smd.gctcore.common.mixin.aether;

import com.gildedgames.the_aether.blocks.dungeon.BlockTreasureChest;
import com.gildedgames.the_aether.tile_entities.TileEntityTreasureChest;
import com.smd.gctcore.misc.ItemRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockTreasureChest.class, remap = false)
public abstract class MixinDiamondDungeonKeyChest {

    @Inject(method = "func_180639_a", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void gctcore$unlockWithDiamondKey(World world, BlockPos pos, IBlockState state,
                                             EntityPlayer player, EnumHand hand, EnumFacing side,
                                             float hitX, float hitY, float hitZ,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (world.isRemote) {
            return;
        }

        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.isEmpty() || heldStack.getItem() != ItemRegistry.DIAMOND_DUNGEON_KEY) {
            return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityTreasureChest)) {
            return;
        }

        TileEntityTreasureChest chest = (TileEntityTreasureChest) tile;
        if (chest.isLocked()) {
            chest.unlock(3);
            heldStack.shrink(1);
            cir.setReturnValue(true);
        }
    }
}
