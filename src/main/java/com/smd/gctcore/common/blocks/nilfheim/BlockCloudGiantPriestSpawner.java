package com.smd.gctcore.common.blocks.nilfheim;

import com.smd.gctcore.common.tile.CloudGiantPriestSpawnerTileEntity;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCloudGiantPriestSpawner extends BlockContainer {
    public BlockCloudGiantPriestSpawner() {
        super(Material.ROCK);
        setRegistryName("cloud_giant_priest_spawner");
        setTranslationKey("gctcore.cloud_giant_priest_spawner");
        setCreativeTab(CreativeTabs.MISC);
        setSoundType(SoundType.STONE);
        setResistance(6000000.0F);
        setBlockUnbreakable();
        setHardness(-1.0F);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new CloudGiantPriestSpawnerTileEntity();
    }
}
