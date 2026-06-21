package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockNilfheimLeaves extends BlockLeaves {
    public BlockNilfheimLeaves(String name) {
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(0.2F);
        setSoundType(SoundType.PLANT);
        setLightOpacity(1);
        setDefaultState(blockState.getBaseState()
                .withProperty(CHECK_DECAY, Boolean.FALSE)
                .withProperty(DECAYABLE, Boolean.TRUE));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(DECAYABLE, (meta & 4) == 0)
                .withProperty(CHECK_DECAY, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (!state.getValue(DECAYABLE)) {
            meta |= 4;
        }
        if (state.getValue(CHECK_DECAY)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
    }

    @Override
    public void getSubBlocks(net.minecraft.creativetab.CreativeTabs itemIn, NonNullList<net.minecraft.item.ItemStack> items) {
        items.add(new net.minecraft.item.ItemStack(this));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return Blocks.LEAVES.getRenderLayer();
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side) {
        return true;
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return BlockPlanks.EnumType.SPRUCE;
    }

    @Override
    public java.util.List<ItemStack> onSheared(ItemStack item, IBlockAccess world, net.minecraft.util.math.BlockPos pos, int fortune) {
        return java.util.Collections.singletonList(new ItemStack(this));
    }
}
