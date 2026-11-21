package com.smd.gctcore.client;

import com.smd.gctcore.misc.JotunheimBlocks;
import com.smd.gctcore.misc.JotunheimWoodBlocks;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimSlab;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockVine;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Connects approved nested art model locations to the statically registered blocks. */
@SideOnly(Side.CLIENT)
public final class JotunheimBlockModels {
    private JotunheimBlockModels() { }

    public static void registerModels() {
        for (JotunheimBlocks.Entry entry : JotunheimBlocks.entries()) {
            if (entry.getBlock() instanceof BlockLeaves) {
                ModelLoader.setCustomStateMapper(entry.getBlock(), new StateMap.Builder()
                        .ignore(BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE).build());
            } else if (entry.getBlock() instanceof BlockVine) {
                ModelLoader.setCustomStateMapper(entry.getBlock(), new StateMap.Builder().ignore(BlockVine.UP).build());
            }
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(entry.getBlock()),
                    entry.getMetadata(), new ModelResourceLocation(
                            new ResourceLocation("gctcore", entry.getModelPath()), "inventory"));
        }
        for (JotunheimWoodBlocks.Family family : JotunheimWoodBlocks.families()) {
            // A single variant is used by the shared slab implementation.
            ModelLoader.setCustomStateMapper(family.doubleSlab,
                    new StateMap.Builder().ignore(BlockNilfheimSlab.VARIANT).build());
            woodItem(family.planks, family.key + "_planks");
            woodItem(family.slab, family.key + "_slab");
            woodItem(family.stairs, family.key + "_stairs");
            woodItem(family.fence, family.key + "_fence");
            woodItem(family.fenceGate, family.key + "_fence_gate");
        }
    }

    private static void woodItem(Block block, String key) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                new ModelResourceLocation(new ResourceLocation("gctcore", "jotunheim/" + key), "inventory"));
    }
}
