package com.smd.gctcore.common.blocks.botania;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.botania.GctManaPoolTier;
import com.smd.gctcore.common.tile.botania.TileGctManaPylon;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/** Decorative tiered pylon; intentionally has no portal or ritual behaviour. */
public class BlockGctManaPylon extends Block {
    public static final PropertyEnum<GctManaPoolTier> TIER =
            PropertyEnum.create("tier", GctManaPoolTier.class);

    // Same selection/collision shape as Botaniverse's MorePylon.
    private static final AxisAlignedBB AABB =
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.3125D, 0.875D);

    public BlockGctManaPylon() {
        super(Material.IRON);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "gct_mana_pylon"));
        setTranslationKey("gctcore.gct_mana_pylon");
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(5.5F);
        setSoundType(SoundType.METAL);
        setLightLevel(0.5F);
        setLightOpacity(0);
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

    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return AABB;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileGctManaPylon();
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Nonnull
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @Nonnull
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }
}
