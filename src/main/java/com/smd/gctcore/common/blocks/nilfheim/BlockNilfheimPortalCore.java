package com.smd.gctcore.common.blocks.nilfheim;

import com.smd.gctcore.common.tile.NilfheimPortalTileEntity;
import com.smd.gctcore.misc.ItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockNilfheimPortalCore extends Block {
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockNilfheimPortalCore() {
        super(Material.WOOD);
        setRegistryName("nilfheim_portal_core");
        setTranslationKey("gctcore.nilfheim_portal_core");
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(10.0F);
        setResistance(60.0F);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(ACTIVE, Boolean.FALSE));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() != ItemRegistry.IMAGINATIVE_SNOWBALL) {
            return false;
        }

        if (world.isRemote) {
            return true;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof NilfheimPortalTileEntity) || !((NilfheimPortalTileEntity) tile).tryActivate(player)) {
            return true;
        }

        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new NilfheimPortalTileEntity();
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state, IBlockAccess world, @Nonnull BlockPos pos) {
        return state.getValue(ACTIVE) ? 15 : 0;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTIVE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ACTIVE, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ACTIVE) ? 1 : 0;
    }
}
