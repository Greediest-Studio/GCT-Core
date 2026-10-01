package com.gmm.gctold.common.blocks;

import com.gmm.gctold.client.GctAllFluidModels;
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

public class BlockNoxexeum extends BlockFluidClassic {
    private static final Fluid FLUID = registerFluid();

    public static final Block block = new BlockNoxexeum();
    public static final Item item = new ItemBlock(block);

    public BlockNoxexeum() {
        super(FLUID, Material.WATER);
        setTranslationKey("noxexeum");
    }

    public static void preInit(FMLPreInitializationEvent event) {
        FluidRegistry.addBucketForFluid(FLUID);
    }

    private static Fluid registerFluid() {
        Fluid fluid = new Fluid("noxexeum",
                new ResourceLocation("gctold:blocks/deepest_noxexeum_still"),
                new ResourceLocation("gctold:blocks/deepest_noxexeum_flow"))
                .setLuminosity(0)
                .setDensity(2560)
                .setViscosity(5000)
                .setGaseous(false);
        return FluidRegistry.registerFluid(fluid) ? fluid : FluidRegistry.getFluid(fluid.getName());
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        super.onEntityCollision(world, pos, state, entity);
        if (!world.isRemote && entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)entity;
            String name = entity.getDisplayName().getUnformattedText();
            if ("bninz".equals(name) || "黑暗人".equals(name)) {
                living.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20, 1, false, false));
            } else {
                living.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 1, false, false));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        GctAllFluidModels.register(block, item, "noxexeum");
    }
}
