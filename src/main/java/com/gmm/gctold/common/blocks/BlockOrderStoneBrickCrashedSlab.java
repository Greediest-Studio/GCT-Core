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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockOrderStoneBrickCrashedSlab extends BlockSlab {
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
    public static final Block block = new BlockOrderStoneBrickCrashedSlab();
    public static final Block block_slab_double = new Double();

    public BlockOrderStoneBrickCrashedSlab() {
    super(Material.ROCK);
    this.setTranslationKey("order_stone_brick_crashed_slab");
    this.setSoundType(SoundType.STONE);
    this.setHarvestLevel("pickaxe", 12);
    this.setHardness(50.0f);
    this.setResistance(4000.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);
    IBlockState state = this.blockState.getBaseState().withProperty((IProperty)VARIANT, (Comparable)Variant.DEFAULT);
    if (!this.isDouble()) {
    state = state.withProperty((IProperty)BlockSlab.HALF, (Comparable)BlockSlab.EnumBlockHalf.BOTTOM);
    }
    this.setDefaultState(state);
    this.useNeighborBrightness = !this.isDouble();

    }

    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
    return Item.getItemFromBlock(BlockOrderStoneBrickCrashedSlab.block);
    }

    public ItemStack getItem(final World worldIn, final BlockPos pos, final IBlockState state) {
    return new ItemStack(BlockOrderStoneBrickCrashedSlab.block);
    }

    protected BlockStateContainer createBlockState() {
    return this.isDouble() ? new BlockStateContainer((Block)this, new IProperty[] { (IProperty)VARIANT }) : new BlockStateContainer((Block)this, new IProperty[] { (IProperty)HALF, (IProperty)VARIANT });
    }

    public IBlockState getStateFromMeta(final int meta) {
    if (this.isDouble()) {
    return this.getDefaultState();
    }
    return this.getDefaultState().withProperty((IProperty)HALF, (Comparable)BlockSlab.EnumBlockHalf.values()[meta % 2]);
    }

    public int getMetaFromState(final IBlockState state) {
    if (this.isDouble()) {
    return 0;
    }
    return ((BlockSlab.EnumBlockHalf)state.getValue((IProperty)HALF)).ordinal();
    }

    public String getTranslationKey(final int meta) {
    return super.getTranslationKey();
    }

    public IProperty<?> getVariantProperty() {
    return (IProperty<?>)VARIANT;
    }

    public Comparable<?> getTypeForItem(final ItemStack stack) {
    return Variant.DEFAULT;
    }

    public boolean isDouble() {
    return false;
    }

    public boolean doesSideBlockRendering(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
    return this.isDouble() || super.doesSideBlockRendering(state, world, pos, face);
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
    }

    public boolean isOpaqueCube(final IBlockState state) {
    return false;
    }

    public enum Variant implements IStringSerializable
    {
    DEFAULT;

    public String getName() {
    return "default";
    }
    }

    public static class Double extends BlockOrderStoneBrickCrashedSlab
    {
    public Double() {
    super();
    }

    @Override
    public boolean isDouble() {
    return true;
    }
    }

}
