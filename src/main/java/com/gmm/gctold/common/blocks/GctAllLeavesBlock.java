package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;
import java.util.Random;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Collections;
import java.util.List;

public class GctAllLeavesBlock extends BlockLeaves {
    public GctAllLeavesBlock(String name) {
        setTranslationKey(name);
        setSoundType(SoundType.PLANT);
        setHardness(0.2F);
        setResistance(0.2F);
        setLightOpacity(255);
        setCreativeTab(GctAllCreativeTab.TAB);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(CHECK_DECAY, Boolean.FALSE)
                .withProperty(DECAYABLE, Boolean.FALSE));
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return BlockPlanks.EnumType.OAK;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(DECAYABLE, (meta & 4) != 0)
                .withProperty(CHECK_DECAY, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (state.getValue(DECAYABLE)) {
            meta |= 4;
        }
        if (state.getValue(CHECK_DECAY)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemById(0);
    }

    @Override
    public void dropApple(World worldIn, BlockPos pos, IBlockState state, int chance) {
    }

    @Override
    protected int getSaplingDropChance(IBlockState state) {
        return 0;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}
