package com.smd.gctcore.common.integration.astralsorcery;

import hellfirepvp.astralsorcery.common.block.network.BlockCelestialCollectorCrystal;
import hellfirepvp.astralsorcery.common.block.network.BlockCollectorCrystalBase;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.item.block.ItemCollectorCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.tile.network.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.util.ItemUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/** Eternal collector crystal block; all celestial network and constellation behavior is inherited. */
public class EternalCollectorCrystalBlock extends BlockCelestialCollectorCrystal {

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (IWeakConstellation constellation : ConstellationRegistry.getWeakConstellations()) {
            ItemStack stack = new ItemStack(this);
            ItemCollectorCrystal.setConstellation(stack, constellation);
            ItemCollectorCrystal.setType(stack, BlockCollectorCrystalBase.CollectorCrystalType.CELESTIAL_CRYSTAL);
            CrystalProperties.applyCrystalProperties(stack, EternalCrystalProperties.max());
            list.add(stack);
        }
    }

    @Override
    public int getMaxSize(ItemStack stack) {
        return EternalCrystalItem.MAX_SIZE;
    }

    @Nonnull
    @Override
    public ItemStack getDecriptor(IBlockState state) {
        ItemStack stack = new ItemStack(this);
        ItemCollectorCrystal.setType(stack, BlockCollectorCrystalBase.CollectorCrystalType.CELESTIAL_CRYSTAL);
        return stack;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new EternalCollectorCrystalTile();
    }

    @Override
    public void onBlockHarvested(World world, net.minecraft.util.math.BlockPos pos,
                                 IBlockState state, EntityPlayer player) {
        TileCollectorCrystal tile = MiscUtils.getTileAt(world, pos, TileCollectorCrystal.class, true);
        if (tile != null && !world.isRemote) {
            if (tile.isPlayerMade() && !player.isCreative()
                    && tile.getCrystalProperties() != null && tile.getConstellation() != null) {
                ItemStack drop = new ItemStack(this);
                CrystalProperties.applyCrystalProperties(drop, tile.getCrystalProperties());
                ItemCollectorCrystal.setType(drop, tile.getType() != null
                        ? tile.getType() : BlockCollectorCrystalBase.CollectorCrystalType.CELESTIAL_CRYSTAL);
                ItemCollectorCrystal.setConstellation(drop, tile.getConstellation());
                ItemCollectorCrystal.setTraitConstellation(drop, tile.getTrait());
                ItemUtils.dropItemNaturally(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                // The inherited handler would otherwise emit the ordinary celestial block as a second drop.
                tile.onPlace(tile.getConstellation(), tile.getTrait(), tile.getCrystalProperties(), null, tile.getType());
            }
        }
        super.onBlockHarvested(world, pos, state, player);
    }
}
