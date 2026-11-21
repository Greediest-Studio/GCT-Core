package com.smd.gctcore.common.blocks.jotunheim;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockJotunheimAercloud extends Block {
    public enum Variant implements IStringSerializable {
        FROSTBOUND, ASHEN, THUNDERING;
        @Override
        public String getName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    public BlockJotunheimAercloud() {
        super(Material.ICE);
        setRegistryName("jotunheim_aercloud");
        setTranslationKey("gctcore.jotunheim_aercloud");
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(0.2F);
        setHarvestLevel("shovel", 0);
        setSoundType(SoundType.CLOTH);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(VARIANT, Variant.FROSTBOUND));
    }

    @Override
    protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, VARIANT); }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, Variant.values()[meta >= 0 && meta < 3 ? meta : 0]);
    }

    @Override
    public int getMetaFromState(IBlockState state) { return state.getValue(VARIANT).ordinal(); }

    @Override
    public int damageDropped(IBlockState state) { return getMetaFromState(state); }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == getCreativeTab() || tab == CreativeTabs.SEARCH) {
            for (Variant variant : Variant.values()) items.add(new ItemStack(this, 1, variant.ordinal()));
        }
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        entity.fallDistance = 0.0F;
        if (entity.motionY < 0.0D) entity.motionY *= 0.005D;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.01D, 1.0D);
    }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return world.getBlockState(pos.offset(side)) != state
                && super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.TRANSLUCENT; }
}
