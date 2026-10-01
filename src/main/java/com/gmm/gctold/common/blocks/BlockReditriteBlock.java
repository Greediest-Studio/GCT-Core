package com.gmm.gctold.common.blocks;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import java.util.Random;
import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.Block;
public class BlockReditriteBlock extends Block {
    public static final Block block = new BlockReditriteBlock();

    public BlockReditriteBlock() {
    super(Material.ROCK);
    this.setTranslationKey("reditrite_block");
    this.setSoundType(SoundType.STONE);
    this.setHarvestLevel("pickaxe", 6);
    this.setHardness(50.0f);
    this.setResistance(4000.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);

    }



    public int quantityDropped(final Random random) {
    return 0;
    }

    public boolean removedByPlayer(final IBlockState state, final World world, final BlockPos pos, final EntityPlayer entity, final boolean willHarvest) {
    final boolean retval = super.removedByPlayer(state, world, pos, entity, willHarvest);
    if (world.rand.nextDouble() < 0.25D) {
        if (!world.isRemote && !entity.capabilities.isCreativeMode) {
            EntityItem item = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    new ItemStack(BlockReditriteBlock.block));
            item.setPickupDelay(10);
            world.spawnEntity(item);
        }
    } else {
        world.setBlockState(pos, BlockReditriteCobblestone.block.getDefaultState(), 3);
    }
    return retval;
    }

}
