package com.smd.gctcore.client;

import com.smd.gctcore.misc.YmirBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class YmirBlockModels {
    private YmirBlockModels() { }

    public static void registerModels() {
        for (YmirBlocks.Family family : YmirBlocks.families()) {
            item(family.stone);
            item(family.bricks);
            item(family.crackedBricks);
            item(family.chiseled);
            item(family.pillar);
            item(family.stairs);
            item(family.slab);
            item(family.wall);
        }
    }

    private static void item(Block block) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }
}
