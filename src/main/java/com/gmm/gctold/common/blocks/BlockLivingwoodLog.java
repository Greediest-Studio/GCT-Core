package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

  public class BlockLivingwoodLog extends Block {
  public static final PropertyDirection FACING = BlockDirectional.FACING;
  public static final Block block = new BlockLivingwoodLog();

  public BlockLivingwoodLog() {
    super(Material.ROCK);
    setTranslationKey("livingwood_log");
    setSoundType(SoundType.WOOD);
    setHarvestLevel("axe", 1);
    setHardness(2.0F);
    setResistance(2.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
    setDefaultState(this.blockState.getBaseState().withProperty((IProperty)FACING, (Comparable)EnumFacing.SOUTH));
  }

  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, new IProperty[] { (IProperty)FACING });
  }

  public IBlockState withRotation(IBlockState state, Rotation rot) {
    if (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90) {
      if ((EnumFacing)state.getValue((IProperty)FACING) == EnumFacing.WEST || (EnumFacing)state.getValue((IProperty)FACING) == EnumFacing.EAST)
        return state.withProperty((IProperty)FACING, (Comparable)EnumFacing.UP);
      if ((EnumFacing)state.getValue((IProperty)FACING) == EnumFacing.UP || (EnumFacing)state.getValue((IProperty)FACING) == EnumFacing.DOWN)
        return state.withProperty((IProperty)FACING, (Comparable)EnumFacing.WEST);
    }
    return state;
  }

  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)EnumFacing.byIndex(meta));
  }

  public int getMetaFromState(IBlockState state) {
    return ((EnumFacing)state.getValue((IProperty)FACING)).getIndex();
  }

  public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    if (facing == EnumFacing.WEST || facing == EnumFacing.EAST) {
      facing = EnumFacing.UP;
    } else if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
      facing = EnumFacing.EAST;
    } else {
      facing = EnumFacing.SOUTH;
    }
    return getDefaultState().withProperty((IProperty)FACING, (Comparable)facing);
  }
}
