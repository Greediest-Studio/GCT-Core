package com.gmm.gctold.client;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public final class GctAllFluidModels {
    private GctAllFluidModels() {
    }

    public static void register(Block block, Item item, String registryName) {
        ModelBakery.registerItemVariants(item, new ResourceLocation[0]);
        ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                return new ModelResourceLocation("gctold:" + registryName, registryName);
            }
        });
        ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation("gctold:" + registryName, registryName);
            }
        });
    }
}
