package com.smd.gctcore.common.blocks.jotunheim;

import com.smd.gctcore.common.world.jotunheim.JotunheimFloraProfiles;
import com.smd.gctcore.common.world.jotunheim.JotunheimTreeShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/** A renewable tree sapling using the same complete shapes as natural trees. */
public class BlockJotunheimSapling extends BlockJotunheimPlant implements IGrowable {
    private final JotunheimFloraProfiles.Tree tree;

    public BlockJotunheimSapling(String name, JotunheimFloraProfiles.Tree tree) {
        super(name, false, false);
        this.tree = tree;
        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || !world.isAreaLoaded(pos, 1)) return;
        super.updateTick(world, pos, state, random);
        if (world.getBlockState(pos).getBlock() == this
                && world.getLightFromNeighbors(pos.up()) >= 9 && random.nextInt(7) == 0) {
            generateTree(world, random, pos);
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return world.isBlockLoaded(pos) && world.isBlockLoaded(pos.down())
                && world.getBlockState(pos).getBlock() == this && canBlockStay(world, pos, state);
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return random.nextFloat() < 0.45F;
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        generateTree(world, random, pos);
    }

    /** Validate the entire tree before removing the sapling or writing any part. */
    public boolean generateTree(World world, Random random, BlockPos pos) {
        if (world.isRemote || !world.isBlockLoaded(pos) || !world.isBlockLoaded(pos.down())
                || world.getBlockState(pos).getBlock() != this
                || !canBlockStay(world, pos, world.getBlockState(pos))
                || !TerrainGen.saplingGrowTree(world, random, pos)) return false;

        JotunheimTreeShapes.Plan plan = JotunheimTreeShapes.create(tree, random, pos, world.getHeight() - 1);
        if (plan == null) return false;
        for (BlockPos support : plan.supports) {
            BlockPos floor = support.down();
            if (!world.isBlockLoaded(floor) || !canGrowOn(world.getBlockState(floor))) return false;
        }

        Map<BlockPos, IBlockState> previous = new LinkedHashMap<>();
        for (BlockPos target : plan.blocks.keySet()) {
            if (target.getY() < 1 || target.getY() >= world.getHeight()
                    || !world.isBlockLoaded(target)) return false;
            IBlockState existing = world.getBlockState(target);
            if (!canReplace(world, target, existing, pos)) return false;
            previous.put(target, existing);
        }

        for (Map.Entry<BlockPos, IBlockState> entry : plan.blocks.entrySet()) {
            IBlockState grown = entry.getValue();
            if (grown.getPropertyKeys().contains(BlockLeaves.DECAYABLE)) {
                grown = grown.withProperty(BlockLeaves.DECAYABLE, true)
                        .withProperty(BlockLeaves.CHECK_DECAY, false);
            }
            if (grown.equals(previous.get(entry.getKey()))) continue;
            if (!world.setBlockState(entry.getKey(), grown, 2)) {
                // A rejected placement must not consume the seed or leave half a tree.
                for (Map.Entry<BlockPos, IBlockState> old : previous.entrySet()) {
                    world.setBlockState(old.getKey(), old.getValue(), 2);
                }
                return false;
            }
        }
        for (BlockPos support : plan.supports) {
            BlockPos floor = support.down();
            IBlockState ground = world.getBlockState(floor);
            ground.getBlock().onPlantGrow(ground, world, floor, support);
        }
        return true;
    }

    private boolean canReplace(World world, BlockPos target, IBlockState existing, BlockPos origin) {
        Block block = existing.getBlock();
        if (target.equals(origin)) return block == this;
        if (block.hasTileEntity(existing) || existing.getMaterial().isLiquid()) return false;
        Material material = existing.getMaterial();
        return block.isAir(existing, world, target) || block.isLeaves(existing, world, target)
                || (material == Material.PLANTS || material == Material.VINE)
                && !(block instanceof BlockSapling) && !(block instanceof BlockJotunheimSapling)
                && block.isReplaceable(world, target);
    }
}
