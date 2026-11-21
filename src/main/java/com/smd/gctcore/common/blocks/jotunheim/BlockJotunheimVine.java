package com.smd.gctcore.common.blocks.jotunheim;

import net.minecraft.block.BlockVine;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockJotunheimVine extends BlockVine {
    public BlockJotunheimVine(String name) {
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(0.2F);
        setSoundType(SoundType.PLANT);
        setTickRandomly(false);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        // Spreading and ecological generation are reserved for the vegetation stage.
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }

    @Override
    public boolean canAttachTo(World world, BlockPos pos, EnumFacing side) {
        IBlockState above = world.getBlockState(pos.up());
        return acceptableNeighbor(world, pos.offset(side.getOpposite()), side)
                && (above.getBlock().isAir(above, world, pos.up()) || above.getBlock() == this
                || acceptableNeighbor(world, pos.up(), EnumFacing.UP));
    }

    private boolean acceptableNeighbor(World world, BlockPos pos, EnumFacing side) {
        IBlockState neighbor = world.getBlockState(pos);
        return neighbor.getBlockFaceShape(world, pos, side) == BlockFaceShape.SOLID
                && !isExceptBlockForAttaching(neighbor.getBlock());
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this));
    }
}
