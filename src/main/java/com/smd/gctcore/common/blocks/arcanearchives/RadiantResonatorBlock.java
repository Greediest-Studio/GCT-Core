package com.smd.gctcore.common.blocks.arcanearchives;

import com.smd.gctcore.common.config.GctCoreConfig;
import com.smd.gctcore.common.tile.RadiantResonatorTileEntity;
import com.smd.gctcore.common.world.RadiantResonatorData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class RadiantResonatorBlock extends Block {

    public RadiantResonatorBlock() {
        super(Material.IRON);
        setRegistryName("radiant_resonator");
        setTranslationKey("gctcore.radiant_resonator");
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(3.0F);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GOLD + I18n.format("tooltip.gctcore.radiant_resonator"));
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (world.isRemote) {
            return;
        }

        if (placer instanceof FakePlayer) {
            removeAndDrop(world, pos);
            return;
        }

        UUID owner = placer.getUniqueID();
        RadiantResonatorData data = RadiantResonatorData.get(world);
        data.prune(owner);
        if (data.count(owner) >= GctCoreConfig.radiantResonator.resonatorLimit) {
            removeAndDrop(world, pos);
            return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof RadiantResonatorTileEntity) {
            ((RadiantResonatorTileEntity) tile).setOwner(owner);
            data.add(owner, world.provider.getDimension(), pos);
        }
    }

    private void removeAndDrop(World world, BlockPos pos) {
        world.removeTileEntity(pos);
        world.setBlockToAir(pos);
        spawnAsEntity(world, pos, new ItemStack(this));
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof RadiantResonatorTileEntity)) {
            return 0;
        }

        RadiantResonatorTileEntity resonator = (RadiantResonatorTileEntity) tile;
        RadiantResonatorTileEntity.TickResult result = resonator.canTick();
        if (result == RadiantResonatorTileEntity.TickResult.OFFLINE || result == RadiantResonatorTileEntity.TickResult.OBSTRUCTION) {
            return 0;
        }
        if (result == RadiantResonatorTileEntity.TickResult.HARVEST_WAITING) {
            return 15;
        }
        return Math.max(1, Math.min((int) Math.floor(resonator.getPercentageComplete() / 7.14D) + 1, 14));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new RadiantResonatorTileEntity();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof RadiantResonatorTileEntity) {
                UUID owner = ((RadiantResonatorTileEntity) tile).getOwner();
                if (owner != null) {
                    RadiantResonatorData.get(world).remove(owner, world.provider.getDimension(), pos);
                } else {
                    RadiantResonatorData.get(world).removeAt(world.provider.getDimension(), pos);
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
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
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
