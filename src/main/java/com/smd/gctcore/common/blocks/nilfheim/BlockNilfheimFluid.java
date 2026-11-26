package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockNilfheimFluid extends BlockFluidClassic {
    private static final int EFFECT_DURATION = 100;
    private static final int LEVEL_10 = 9;
    private static final int LEVEL_2 = 1;

    public BlockNilfheimFluid(Fluid fluid, String name) {
        super(fluid, Material.WATER);
        setRegistryName(name);
        setTranslationKey("gctcore." + name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setQuantaPerBlock(4);
        setTickRate(6);
        setLightOpacity(3);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        // Lighting engines can sample a new chunk before it is published to the
        // world. Forge's fluid implementation re-reads this position from World,
        // recursively generating that same chunk. The supplied state is sufficient.
        if (maxScaledLight == 0) {
            return super.getLightValue(state);
        }
        int quanta = MathHelper.clamp(quantaPerBlock - state.getValue(LEVEL), 0, quantaPerBlock);
        return MathHelper.clamp((int) (quanta / quantaPerBlockFloat * maxScaledLight), 0, 15);
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        super.onEntityCollision(world, pos, state, entity);
        if (world.isRemote || !(entity instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase living = (EntityLivingBase) entity;
        living.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, EFFECT_DURATION, LEVEL_10, true, true));
        living.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, EFFECT_DURATION, LEVEL_10, true, true));
        living.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, EFFECT_DURATION, 0, true, true));
        living.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, EFFECT_DURATION, LEVEL_2, true, true));
    }
}
