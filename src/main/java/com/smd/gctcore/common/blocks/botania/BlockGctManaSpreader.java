package com.smd.gctcore.common.blocks.botania;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.botania.GctManaPoolTier;
import com.smd.gctcore.common.tile.botania.TileGctManaSpreader;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.api.wand.IWandable;
import vazkii.botania.api.wand.IWireframeAABBProvider;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lexicon.LexiconData;

import javax.annotation.Nonnull;

public class BlockGctManaSpreader extends Block
        implements IWandable, IWandHUD, ILexiconable, IWireframeAABBProvider {

    public static final PropertyEnum<GctManaPoolTier> TIER =
            PropertyEnum.create("tier", GctManaPoolTier.class);

    public BlockGctManaSpreader() {
        super(Material.WOOD);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "gct_mana_spreader"));
        setTranslationKey("gctcore.gct_mana_spreader");
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(2.0F);
        setSoundType(SoundType.WOOD);
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
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, getStateFromMeta(stack.getMetadata()), 3);
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileGctManaSpreader)) {
            return;
        }
        TileGctManaSpreader spreader = (TileGctManaSpreader) tile;
        EnumFacing orientation = EnumFacing.getDirectionFromEntityLiving(pos, placer);
        switch (orientation) {
            case DOWN:
                spreader.rotationY = -90.0F;
                break;
            case UP:
                spreader.rotationY = 90.0F;
                break;
            case NORTH:
                spreader.rotationX = 270.0F;
                break;
            case SOUTH:
                spreader.rotationX = 90.0F;
                break;
            case EAST:
                spreader.rotationX = 180.0F;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileGctManaSpreader)) {
            return false;
        }

        TileGctManaSpreader spreader = (TileGctManaSpreader) tile;
        ItemStack lens = spreader.getItemHandler().getStackInSlot(0);
        ItemStack held = player.getHeldItem(hand);
        boolean heldLens = !held.isEmpty() && held.getItem() instanceof ILens;
        boolean wool = !held.isEmpty() && held.getItem() == Item.getItemFromBlock(Blocks.WOOL);
        if (!held.isEmpty() && held.getItem() == ModItems.twigWand) {
            return false;
        }

        if (lens.isEmpty() && heldLens) {
            if (!player.capabilities.isCreativeMode) {
                player.setHeldItem(hand, ItemStack.EMPTY);
            }
            spreader.getItemHandler().setStackInSlot(0, held.copy());
            spreader.markDirty();
        } else if (!lens.isEmpty() && !wool) {
            ItemHandlerHelper.giveItemToPlayer(player, lens);
            spreader.getItemHandler().setStackInSlot(0, ItemStack.EMPTY);
            spreader.markDirty();
        }

        if (wool && spreader.paddingColor == -1) {
            spreader.paddingColor = held.getMetadata();
            held.shrink(1);
            if (held.isEmpty()) {
                player.setHeldItem(hand, ItemStack.EMPTY);
            }
            spreader.markDirty();
        } else if (held.isEmpty() && spreader.paddingColor != -1 && lens.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Blocks.WOOL, 1, spreader.paddingColor));
            spreader.paddingColor = -1;
            spreader.markDirty();
        }
        return true;
    }

    @Override
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileGctManaSpreader) {
            TileGctManaSpreader spreader = (TileGctManaSpreader) tile;
            if (spreader.paddingColor != -1) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(),
                        new ItemStack(Blocks.WOOL, 1, spreader.paddingColor));
            }
            vazkii.botania.common.core.helper.InventoryHelper.dropInventory(spreader, world, state, pos);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileGctManaSpreader) {
            ((TileGctManaSpreader) tile).onWanded(player, stack);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileGctManaSpreader();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUD(Minecraft mc, ScaledResolution resolution, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileGctManaSpreader) {
            ((TileGctManaSpreader) tile).renderHUD(mc, resolution);
        }
    }

    @Override
    public LexiconEntry getEntry(World world, BlockPos pos, EntityPlayer player, ItemStack lexicon) {
        return LexiconData.spreader;
    }

    @Override
    public AxisAlignedBB getWireframeAABB(World world, BlockPos pos) {
        return FULL_BLOCK_AABB.offset(pos).grow(0.0625D);
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

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
        super.eventReceived(state, world, pos, id, param);
        TileEntity tile = world.getTileEntity(pos);
        return tile != null && tile.receiveClientEvent(id, param);
    }
}
