package com.smd.gctcore.common.blocks.botania;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.botania.GctManaPoolTier;
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

public class BlockGctManaRock extends Block {

    public static final PropertyEnum<GctManaPoolTier> TIER = PropertyEnum.create("tier", GctManaPoolTier.class);

    public BlockGctManaRock() {
        super(Material.ROCK);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "gct_mana_rock"));
        setTranslationKey("gctcore.gct_mana_rock");
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(2.0F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
        setDefaultState(blockState.getBaseState().withProperty(TIER, GctManaPoolTier.JOETUNHEIM));
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TIER);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TIER).ordinal();
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(TIER, GctManaPoolTier.fromMeta(meta));
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == getCreativeTab()) {
            for (GctManaPoolTier tier : GctManaPoolTier.values()) {
                items.add(new ItemStack(this, 1, tier.ordinal()));
            }
        }
    }
}
