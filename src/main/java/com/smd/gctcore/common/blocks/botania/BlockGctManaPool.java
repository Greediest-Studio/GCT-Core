package com.smd.gctcore.common.blocks.botania;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.botania.GctManaPoolTier;
import com.smd.gctcore.common.tile.botania.TileGctManaPool;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.PoolVariant;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.api.wand.IWandable;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.lexicon.LexiconData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockGctManaPool extends Block implements IWandHUD, IWandable, ILexiconable {

    public static final PropertyEnum<GctManaPoolTier> TIER = PropertyEnum.create("tier", GctManaPoolTier.class);

    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    private static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D);
    private static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.9375D, 1.0D, 0.5D, 1.0D);
    private static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 0.0625D);
    private static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0625D, 0.5D, 1.0D);
    private static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.9375D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);

    public BlockGctManaPool() {
        super(Material.ROCK);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "gct_mana_pool"));
        setTranslationKey("gctcore.gct_mana_pool");
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(2.0F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
        BotaniaAPI.blacklistBlockFromMagnet(this, 32767);
        setDefaultState(blockState.getBaseState()
                .withProperty(TIER, GctManaPoolTier.JOETUNHEIM)
                .withProperty(BotaniaStateProps.POOL_VARIANT, PoolVariant.DEFAULT)
                .withProperty(BotaniaStateProps.COLOR, EnumDyeColor.WHITE));
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty<?>[]{
                TIER,
                BotaniaStateProps.POOL_VARIANT,
                BotaniaStateProps.COLOR
        });
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
    @Nonnull
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world instanceof ChunkCache
                ? ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                : world.getTileEntity(pos);
        return tile instanceof TileGctManaPool
                ? state.withProperty(BotaniaStateProps.COLOR, ((TileGctManaPool) tile).color)
                : state.withProperty(BotaniaStateProps.COLOR, EnumDyeColor.WHITE);
    }

    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return AABB;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileGctManaPool && !((TileGctManaPool) tile).fragile) {
            super.getDrops(drops, world, pos, state, fortune);
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity tile, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, tile, tool);
        world.setBlockToAir(pos);
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
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileGctManaPool();
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityItem) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileGctManaPool && ((TileGctManaPool) tile).collideEntityItem((EntityItem) entity)) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);
            }
        }
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
                                      @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> boxes,
                                      @Nullable Entity entity, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, boxes, BOTTOM_AABB);
        addCollisionBoxToList(pos, entityBox, boxes, NORTH_AABB);
        addCollisionBoxToList(pos, entityBox, boxes, SOUTH_AABB);
        addCollisionBoxToList(pos, entityBox, boxes, WEST_AABB);
        addCollisionBoxToList(pos, entityBox, boxes, EAST_AABB);
    }

    @Override
    public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
        return side == EnumFacing.DOWN;
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
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileGctManaPool)) {
            return 0;
        }
        TileGctManaPool pool = (TileGctManaPool) tile;
        return TilePool.calculateComparatorLevel(pool.getCurrentMana(), pool.manaCap);
    }

    @Override
    public void renderHUD(Minecraft mc, ScaledResolution resolution, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileGctManaPool) {
            ((TileGctManaPool) tile).renderHUD(mc, resolution);
        }
    }

    @Override
    public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileGctManaPool) {
            ((TileGctManaPool) tile).onWanded(player, stack);
            return true;
        }
        return false;
    }

    @Override
    public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
        return LexiconData.pool;
    }

    @Override
    @Nonnull
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
        super.eventReceived(state, world, pos, id, param);
        TileEntity tile = world.getTileEntity(pos);
        return tile != null && tile.receiveClientEvent(id, param);
    }
}
