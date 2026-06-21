package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public abstract class BlockNilfheimSlab extends BlockSlab {
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    protected BlockNilfheimSlab(String name, Material material, SoundType soundType, String tool, int harvestLevel) {
        super(material);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(material == Material.WOOD ? 2.0F : 2.4F);
        setResistance(material == Material.WOOD ? 5.0F : 12.0F);
        setSoundType(soundType);
        setHarvestLevel(tool, harvestLevel);
        IBlockState state = blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT);
        if (!isDouble()) {
            state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
        }
        setDefaultState(state);
        this.useNeighborBrightness = true;
    }

    @Override
    public String getTranslationKey(int meta) {
        return getTranslationKey();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState().withProperty(VARIANT, Variant.DEFAULT);
        if (!isDouble()) {
            state = state.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
        }
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (!isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP) {
            return 8;
        }
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return isDouble()
                ? new BlockStateContainer(this, VARIANT)
                : new BlockStateContainer(this, HALF, VARIANT);
    }

    public static class Half extends BlockNilfheimSlab {
        public Half(String name, Material material, SoundType soundType, String tool, int harvestLevel) {
            super(name, material, soundType, tool, harvestLevel);
        }

        @Override
        public boolean isDouble() {
            return false;
        }
    }

    public static class Double extends BlockNilfheimSlab {
        public Double(String name, Material material, SoundType soundType, String tool, int harvestLevel) {
            super(name, material, soundType, tool, harvestLevel);
        }

        @Override
        public boolean isDouble() {
            return true;
        }
    }

    public enum Variant implements IStringSerializable {
        DEFAULT;

        @Override
        public String getName() {
            return "default";
        }
    }
}
