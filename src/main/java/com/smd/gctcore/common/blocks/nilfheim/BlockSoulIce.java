package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSoulIce extends Block {
    public BlockSoulIce() {
        super(Material.ICE);
        setRegistryName("soul_ice");
        setTranslationKey("gctcore.soul_ice");
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(0.5F);
        setResistance(2.0F);
        setLightOpacity(3);
        setLightLevel(0.25F);
        setSoundType(SoundType.GLASS);
        setHarvestLevel("pickaxe", 13);
        this.slipperiness = 0.98F;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState neighbor = blockAccess.getBlockState(pos.offset(side));
        return neighbor.getBlock() != this && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
}
