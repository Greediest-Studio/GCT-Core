package com.smd.gctcore.common.blocks.jotunheim;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Shared physical properties for the dimension's static natural materials. */
public class BlockJotunheimNatural extends Block {
    private final BlockRenderLayer layer;
    private final boolean cluster;

    public BlockJotunheimNatural(String registryName, Material material, SoundType sound,
                                String tool, float hardness, BlockRenderLayer layer, boolean cluster) {
        super(material);
        this.layer = layer;
        this.cluster = cluster;
        // Block's constructor queries virtual shape methods before these fields are assigned.
        this.fullBlock = layer == BlockRenderLayer.SOLID && !cluster;
        this.translucent = layer == BlockRenderLayer.TRANSLUCENT;
        setLightOpacity(this.fullBlock ? 255 : 1);
        setRegistryName(registryName);
        setTranslationKey("gctcore." + registryName);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(hardness);
        setResistance(hardness * 4.0F);
        setSoundType(sound);
        if (tool != null) setHarvestLevel(tool, 0);
        if (material == Material.ICE) slipperiness = 0.98F;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return layer == BlockRenderLayer.SOLID && !cluster;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return !cluster;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return cluster ? new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D) : FULL_BLOCK_AABB;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (layer == BlockRenderLayer.TRANSLUCENT && !cluster
                && world.getBlockState(pos.offset(side)).getBlock() == this) return false;
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return layer;
    }
}
