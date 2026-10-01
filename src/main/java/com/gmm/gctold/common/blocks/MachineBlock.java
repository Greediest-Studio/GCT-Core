package com.gmm.gctold.common.blocks;

import com.smd.gctcore.Tags;

import net.minecraft.creativetab.CreativeTabs;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MachineBlock extends Block {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    @Nullable
    private final String tooltip;

    public MachineBlock(String name, SoundType soundType, float hardness, float resistance, CreativeTabs tab,
            @Nullable String tooltip) {
        super(Material.ROCK);
        this.tooltip = tooltip;

        setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        setTranslationKey(name);
        setSoundType(soundType);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(0.0F);
        setLightOpacity(15);
        setCreativeTab(tab);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (this.tooltip != null) {
            tooltip.add(this.tooltip);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);
        return getDefaultState().withProperty(FACING, facing.getAxis().isHorizontal() ? facing : EnumFacing.NORTH);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
}
