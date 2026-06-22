package com.smd.gctcore.common.blocks.nilfheim;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
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
    public void onEntityCollision(World world, BlockPos pos, net.minecraft.block.state.IBlockState state, Entity entity) {
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
