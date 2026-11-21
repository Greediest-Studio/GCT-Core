package com.smd.gctcore.common.blocks.jotunheim;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Placeable one-block flora. Growth and world decoration are intentionally separate. */
public class BlockJotunheimPlant extends BlockBush {
    private final boolean rockPlant;
    private final boolean lichen;

    public BlockJotunheimPlant(String name, boolean rockPlant, boolean lichen) {
        this.rockPlant = rockPlant;
        this.lichen = lichen;
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(0.0F);
        setSoundType(SoundType.PLANT);
        setTickRandomly(false);
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.GROUND || material == Material.GRASS || material == Material.SAND
                || rockPlant && (material == Material.ROCK || material == Material.ICE || material == Material.PACKED_ICE)
                || super.canSustainBush(state);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                && canBlockStay(world, pos, getDefaultState());
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canSustainBush(world.getBlockState(pos.down()));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return lichen ? new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.07D, 1.0D)
                : new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.875D, 0.875D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
