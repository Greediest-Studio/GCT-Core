package com.gmm.gctold.common.world.biome;

import java.util.Random;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class WorldGenAlfheimTree extends WorldGenAbstractTree {
    private final int minTreeHeight;
    private final int extraTreeHeight;
    private final boolean positiveExtraHeight;
    private final IBlockState log;
    private final IBlockState leaves;
    private final Block groundReplacement;
    private final Block[] baseBlocks;

    public WorldGenAlfheimTree(int minTreeHeight, IBlockState log, IBlockState leaves, Block grass, Block dirt) {
        this(minTreeHeight, 2, false, log, leaves, dirt, grass, dirt);
    }

    public WorldGenAlfheimTree(int minTreeHeight, int extraTreeHeight, IBlockState log, IBlockState leaves,
            Block... baseBlocks) {
        this(minTreeHeight, extraTreeHeight, true, log, leaves, baseBlocks[0], baseBlocks);
    }

    private WorldGenAlfheimTree(int minTreeHeight, int extraTreeHeight, boolean positiveExtraHeight, IBlockState log,
            IBlockState leaves, Block groundReplacement, Block... baseBlocks) {
        super(false);
        this.minTreeHeight = minTreeHeight;
        this.extraTreeHeight = Math.max(0, extraTreeHeight);
        this.positiveExtraHeight = positiveExtraHeight;
        this.log = log;
        this.leaves = fixedLeaves(leaves);
        this.groundReplacement = groundReplacement;
        this.baseBlocks = Arrays.copyOf(baseBlocks, baseBlocks.length);
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        int height = this.minTreeHeight + randomExtraHeight(rand);

        if (position.getY() < 1 || position.getY() + height + 1 > world.getHeight()) {
            return false;
        }
        if (!hasRoom(world, position, height) || !canGrowOn(world, position.down())) {
            return false;
        }

        world.setBlockState(position.down(), this.groundReplacement.getDefaultState(), 2);
        generateLeaves(world, rand, position, height);
        generateTrunk(world, position, height);
        return true;
    }

    private boolean hasRoom(World world, BlockPos position, int height) {
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        for (int y = position.getY(); y <= position.getY() + height + 1; y++) {
            int radius = 1;
            if (y == position.getY()) {
                radius = 0;
            } else if (y >= position.getY() + height - 2) {
                radius = 2;
            }

            for (int x = position.getX() - radius; x <= position.getX() + radius; x++) {
                for (int z = position.getZ() - radius; z <= position.getZ() + radius; z++) {
                    if (y < 0 || y >= world.getHeight()) {
                        return false;
                    }
                    checkPos.setPos(x, y, z);
                    if (!isReplaceable(world, checkPos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean canGrowOn(World world, BlockPos groundPos) {
        Block ground = world.getBlockState(groundPos).getBlock();
        for (Block baseBlock : this.baseBlocks) {
            if (ground == baseBlock) {
                return true;
            }
        }
        return false;
    }

    private void generateLeaves(World world, Random rand, BlockPos position, int height) {
        for (int y = position.getY() - 3 + height; y <= position.getY() + height; y++) {
            int canopyOffset = y - (position.getY() + height);
            int radius = 1 - canopyOffset / 2;

            for (int x = position.getX() - radius; x <= position.getX() + radius; x++) {
                int xOffset = x - position.getX();
                for (int z = position.getZ() - radius; z <= position.getZ() + radius; z++) {
                    int zOffset = z - position.getZ();
                    if (Math.abs(xOffset) == radius && Math.abs(zOffset) == radius
                            && (rand.nextInt(2) == 0 || canopyOffset == 0)) {
                        continue;
                    }

                    BlockPos leafPos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(leafPos);
                    if (state.getBlock().isAir(state, world, leafPos)
                            || state.getBlock().isLeaves(state, world, leafPos)
                            || state.getMaterial() == Material.VINE) {
                        setBlockAndNotifyAdequately(world, leafPos, this.leaves);
                    }
                }
            }
        }
    }

    private void generateTrunk(World world, BlockPos position, int height) {
        for (int y = 0; y < height; y++) {
            BlockPos logPos = position.up(y);
            IBlockState state = world.getBlockState(logPos);
            if (state.getBlock().isAir(state, world, logPos)
                    || state.getBlock().isLeaves(state, world, logPos)
                    || state.getMaterial() == Material.VINE) {
                setBlockAndNotifyAdequately(world, logPos, this.log);
            }
        }
    }

    @Override
    protected boolean canGrowInto(Block block) {
        Material material = block.getDefaultState().getMaterial();
        return material == Material.AIR || material == Material.LEAVES || block == Blocks.VINE
                || block == this.log.getBlock() || block == this.leaves.getBlock() || isBaseBlock(block);
    }

    @Override
    public boolean isReplaceable(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isAir(state, world, pos)
                || state.getBlock().isLeaves(state, world, pos)
                || state.getMaterial() == Material.VINE
                || canGrowInto(state.getBlock())
                || state.getBlock().isReplaceable(world, pos);
    }

    private int randomExtraHeight(Random rand) {
        if (extraTreeHeight <= 0) {
            return 0;
        }
        if (positiveExtraHeight) {
            return rand.nextInt(extraTreeHeight) + 1;
        }
        return rand.nextInt(extraTreeHeight + 1);
    }

    private boolean isBaseBlock(Block block) {
        for (Block baseBlock : this.baseBlocks) {
            if (block == baseBlock) {
                return true;
            }
        }
        return false;
    }

    private static IBlockState fixedLeaves(IBlockState state) {
        IBlockState fixed = state;
        if (fixed.getPropertyKeys().contains(BlockLeaves.CHECK_DECAY)) {
            fixed = fixed.withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);
        }
        if (fixed.getPropertyKeys().contains(BlockLeaves.DECAYABLE)) {
            fixed = fixed.withProperty(BlockLeaves.DECAYABLE, Boolean.FALSE);
        }
        return fixed;
    }
}
