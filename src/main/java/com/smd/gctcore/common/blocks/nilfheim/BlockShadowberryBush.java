package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.inventory.InventoryHelper;

import java.util.Random;

public class BlockShadowberryBush extends BlockNilfheimPlant implements IGrowable {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 4);
    private static final int MAX_AGE = 4;
    private static final ResourceLocation SHADOWBERRY_ID = new ResourceLocation("additions", "shadowberry");

    public BlockShadowberryBush() {
        super("shadowberry_bush", 0.15F);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(AGE, 0));
    }

    public IBlockState getRandomGrowthState(Random rand) {
        return getDefaultState().withProperty(AGE, rand.nextInt(MAX_AGE + 1));
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        if (!world.isRemote && rand.nextInt(3) == 0) {
            growOneStage(world, pos, state);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (state.getValue(AGE) < MAX_AGE) {
            return false;
        }

        if (!world.isRemote) {
            Item berry = Item.REGISTRY.getObject(SHADOWBERRY_ID);
            if (berry != null && berry != Items.AIR) {
                ItemStack harvest = new ItemStack(berry);
                if (!player.addItemStackToInventory(harvest)) {
                    InventoryHelper.spawnItemStack(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, harvest);
                }
            }
            world.setBlockState(pos, state.withProperty(AGE, 0), 2);
        }
        return true;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        growOneStage(world, pos, state);
    }

    private void growOneStage(World world, BlockPos pos, IBlockState state) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            world.setBlockState(pos, state.withProperty(AGE, age + 1), 2);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AGE, Math.max(0, Math.min(MAX_AGE, meta)));
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(AGE) >= MAX_AGE ? super.getLightValue(state, world, pos) : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }
}
