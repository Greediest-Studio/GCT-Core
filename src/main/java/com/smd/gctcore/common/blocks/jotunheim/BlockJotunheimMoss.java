package com.smd.gctcore.common.blocks.jotunheim;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

/** Rotten moss has a fixed stone base selected by terrain generation. */
public class BlockJotunheimMoss extends BlockJotunheimSoil {
    public static final PropertyEnum<Base> BASE = PropertyEnum.create("base", Base.class);

    public BlockJotunheimMoss(String name) {
        super(name, true);
        setDefaultState(blockState.getBaseState().withProperty(BASE, Base.GIANTSTONE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BASE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BASE, meta == 1 ? Base.CLOUDROCK : Base.GIANTSTONE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BASE).ordinal();
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    public static IBlockState withBase(IBlockState mossState, boolean cloudrock) {
        if (!(mossState.getBlock() instanceof BlockJotunheimMoss)) return mossState;
        return mossState.withProperty(BASE, cloudrock ? Base.CLOUDROCK : Base.GIANTSTONE);
    }

    public enum Base implements IStringSerializable {
        GIANTSTONE("giantstone"),
        CLOUDROCK("cloudrock");

        private final String name;

        Base(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
