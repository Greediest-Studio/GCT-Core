package com.gmm.gctold.common.world.structure;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GctAllStructureGenerator implements IWorldGenerator {
    private final StructureFeature[] structures = {
            new StructureDIM54Portal1(),
            new StructureDIM54Portal2(),
            new StructureSeekAltarStructure(),
            new StructureStrChurch(),
            new StructureStrBar(),
            new StructureStrBlacksmith(),
            new StructureStrFarm(),
            new StructureStrFarmhouse(),
            new StructureStrHouse(),
            new StructureStrLibarary(),
            new StructureStrStorage(),
            new StructureStrTower1(),
            new StructureStrTower2(),
            new StructureHeavenStonePostSmall(),
            new StructureHeavenStonePostSmall2(),
            new StructureHeavenStonePostLarge(),
            new StructureHeavenPortal(),
            new StructureReversedDungeon(),
            new StructureElvenPost(),
            new StructureLibraIsland1(),
            new StructureLibraIsland2(),
            new StructureEndWitheriumComet(),
            new StructureMeteoriteBall(),
            new StructureObsidioriteBig(),
            new StructureObsidioriteMiddle(),
            new StructureObsidioriteSmall(),
            new StructureObsidioriteTiny(),
            new StructureOrder1(),
            new StructureOrder2(),
            new StructureOrderB(),
            new StructureOrderStoneRod(),
            new StructureOrderStoneRod2(),
            new StructureReditriteMeteor1(),
            new StructureReditriteMeteor2(),
            new StructureVoidBedrockRod1(),
            new StructureVoidBedrockRod2(),
            new StructureVoidBedrockRod3(),
            new StructureVoidBedrockRod4(),
            new StructureVoidBedrockRodTreasure()
    };

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider) {
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        int dimensionId = world.provider.getDimension();

        for (StructureFeature structure : structures) {
            structure.generateWorld(random, blockX, blockZ, world, dimensionId, chunkGenerator, chunkProvider);
        }
        GctAllTreeGenerator.generate(random, blockX, blockZ, world, dimensionId);
        GctAllOreGenerator.generate(random, blockX, blockZ, world, dimensionId);
    }
}
