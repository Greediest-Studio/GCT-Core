package com.smd.gctcore.common.blocks.botania;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.botania.GctManaWoodVariant;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class BlockGctManaWood extends Block {

    public static final PropertyEnum<GctManaWoodVariant> VARIANT =
            PropertyEnum.create("variant", GctManaWoodVariant.class);

    public BlockGctManaWood() {
        super(Material.WOOD);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "gct_mana_wood"));
        setTranslationKey("gctcore.gct_mana_wood");
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(2.0F);
        setResistance(10.0F);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);
        setDefaultState(blockState.getBaseState()
                .withProperty(VARIANT, GctManaWoodVariant.JOETUNHEIM));
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, GctManaWoodVariant.fromMeta(meta));
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == getCreativeTab()) {
            for (GctManaWoodVariant variant : GctManaWoodVariant.values()) {
                items.add(new ItemStack(this, 1, variant.ordinal()));
            }
        }
    }
}
