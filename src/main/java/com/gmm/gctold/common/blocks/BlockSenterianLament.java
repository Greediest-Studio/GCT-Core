package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
public class BlockSenterianLament extends Block {
    public static final Block block = new BlockSenterianLament();

    public BlockSenterianLament() {
    super(Material.ROCK);
    this.setTranslationKey("senterian_lament");
    this.setSoundType(SoundType.STONE);
    this.setHardness(1.0f);
    this.setResistance(4000.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);
    this.setBlockUnbreakable();

    }
}