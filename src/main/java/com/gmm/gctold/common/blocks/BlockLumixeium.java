package com.gmm.gctold.common.blocks;

import com.gmm.gctold.client.GctAllFluidModels;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLumixeium extends BlockFluidClassic {
    private static final Fluid FLUID = registerFluid();

    public static final Block block = new BlockLumixeium();
    public static final Item item = new ItemBlock(block);

    public BlockLumixeium() {
        super(FLUID, Material.WATER);
        setTranslationKey("lumixeium");
    }

    public static void preInit(FMLPreInitializationEvent event) {
        FluidRegistry.addBucketForFluid(FLUID);
    }

    private static Fluid registerFluid() {
        Fluid fluid = new Fluid("lumixeium",
                new ResourceLocation("gctold:blocks/scared_lumixeium_still"),
                new ResourceLocation("gctold:blocks/scared_lumixeium_flow"))
                .setLuminosity(15)
                .setDensity(950)
                .setViscosity(500)
                .setGaseous(true);
        return FluidRegistry.registerFluid(fluid) ? fluid : FluidRegistry.getFluid(fluid.getName());
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.updateTick(world, pos, state, random);
        if (!world.isRemote) {
            destroyBrightAdjacentBlocks(world, pos);
        }
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        super.onEntityCollision(world, pos, state, entity);
        if (!world.isRemote && entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)entity;
            String name = entity.getDisplayName().getUnformattedText();
            if ("bligtz".equals(name) || "光明人".equals(name)) {
                living.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20, 1, false, false));
            } else {
                living.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 100, 1, false, false));
            }
        }
    }

    private void destroyBrightAdjacentBlocks(World world, BlockPos pos) {
        destroyIfBright(world, pos.east());
        destroyIfBright(world, pos.west());
        destroyIfBright(world, pos.south());
        destroyIfBright(world, pos.north());
        destroyIfBright(world, pos.down());
    }

    private void destroyIfBright(World world, BlockPos pos) {
        if (world.getBlockState(pos).getLightValue() >= 12) {
            world.destroyBlock(pos, false);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        GctAllFluidModels.register(block, item, "lumixeium");
    }
}
