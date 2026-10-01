package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

  public class BlockPrimordialStonebrickSlab extends BlockSlab {
  public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
  public static final Block block = new BlockPrimordialStonebrickSlab();
  public static final Block block_slab_double = new Double();

  public BlockPrimordialStonebrickSlab() {
    super(Material.ROCK);
    setTranslationKey("primordial_stonebrick_slab");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 4);
    setHardness(2.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
    IBlockState state = this.blockState.getBaseState().withProperty((IProperty)VARIANT, Variant.DEFAULT);
    if (!isDouble())
      state = state.withProperty((IProperty)BlockSlab.HALF, (Comparable)BlockSlab.EnumBlockHalf.BOTTOM);
    setDefaultState(state);
    this.useNeighborBrightness = !isDouble();
  }

  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Item.getItemFromBlock(block);
  }

  public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
    return new ItemStack(block);
  }

  protected BlockStateContainer createBlockState() {
    return isDouble() ? new BlockStateContainer((Block)this, new IProperty[] { (IProperty)VARIANT }) : new BlockStateContainer((Block)this, new IProperty[] { (IProperty)HALF, (IProperty)VARIANT });
  }

  public IBlockState getStateFromMeta(int meta) {
    if (isDouble())
      return getDefaultState();
    return getDefaultState().withProperty((IProperty)HALF, (Comparable)BlockSlab.EnumBlockHalf.values()[meta % 2]);
  }

  public int getMetaFromState(IBlockState state) {
    if (isDouble())
      return 0;
    return ((BlockSlab.EnumBlockHalf)state.getValue((IProperty)HALF)).ordinal();
  }

  public String getTranslationKey(int meta) {
    return getTranslationKey();
  }

  public IProperty<?> getVariantProperty() {
    return (IProperty<?>)VARIANT;
  }

  public Comparable<?> getTypeForItem(ItemStack stack) {
    return Variant.DEFAULT;
  }

  public boolean isDouble() {
    return false;
  }

  public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
    if (isDouble())
      return true;
    return super.doesSideBlockRendering(state, world, pos, face);
  }

  public enum Variant implements IStringSerializable {
    DEFAULT;

    public String getName() {
      return "default";
    }
  }

  public static class Double extends BlockPrimordialStonebrickSlab {
    public Double() {
      super();
    }

    @Override
    public boolean isDouble() {
      return true;
    }
  }
}
