package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.BlockVine;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Collections;

public class BlockNilfheimVine extends BlockVine {
    public BlockNilfheimVine(String name, float lightLevel) {
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(0.2F);
        setLightLevel(lightLevel);
        setSoundType(SoundType.PLANT);
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }

    @Override
    public boolean canAttachTo(World world, BlockPos pos, EnumFacing side) {
        IBlockState above = world.getBlockState(pos.up());
        return isAcceptableNeighbor(world, pos.offset(side.getOpposite()), side)
                && (above.getBlock().isAir(above, world, pos.up())
                || above.getBlock() == this
                || isAcceptableNeighbor(world, pos.up(), EnumFacing.UP));
    }

    @Override
    public java.util.List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this));
    }

    private boolean isAcceptableNeighbor(World world, BlockPos pos, EnumFacing side) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlockFaceShape(world, pos, side) == BlockFaceShape.SOLID && !isExceptBlockForAttaching(state.getBlock());
    }
}
