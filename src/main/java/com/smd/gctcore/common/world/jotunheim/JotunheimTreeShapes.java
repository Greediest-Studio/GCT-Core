package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.misc.JotunheimBlocks;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Complete, deterministic tree blueprints. No world access or chunk writes occur here.
 * A generator validates the whole blueprint before clipping writes to its own chunk.
 */
public final class JotunheimTreeShapes {
    /** All shapes fit within seven horizontal blocks of their anchor. */
    public static final int MAX_RADIUS = 7;

    public static final class Plan {
        public final Map<BlockPos, IBlockState> blocks;
        /** Feet positions that require a solid block immediately below. */
        public final Set<BlockPos> supports;
        /** Logs, stems and caps: these must never overwrite existing solid terrain. */
        public final Set<BlockPos> structural;

        private Plan(Builder builder) {
            blocks = Collections.unmodifiableMap(new LinkedHashMap<>(builder.blocks));
            supports = Collections.unmodifiableSet(new LinkedHashSet<>(builder.supports));
            structural = Collections.unmodifiableSet(new LinkedHashSet<>(builder.structural));
        }
    }

    private JotunheimTreeShapes() {}

    /** maxY is inclusive. A cramped site produces a smaller complete tree, or null. */
    public static Plan create(JotunheimFloraProfiles.Tree type, Random random, BlockPos feet, int maxY) {
        int room = room(feet, maxY);
        if (room < 4 || type == null || type == JotunheimFloraProfiles.Tree.NONE) return null;
        Builder b = new Builder(feet);
        switch (type) {
            case OAK: oak(b, random, room); break;
            case ELDER_PINE: pine(b, random, room); break;
            case SUNKENWOOD: sunken(b, random, room); break;
            case WHITE_OAK: whiteOak(b, random, room); break;
            case CHARRED: charred(b, random, room); break;
            case LIGHTNING: lightning(b, random, room); break;
            case FUNGAL: fungus(b, random, room, false); break;
            case EMBER_FUNGAL: fungus(b, random, room, true); break;
            default: return null;
        }
        return b.blocks.isEmpty() ? null : new Plan(b);
    }

    /** A low ancient-root arch, including both grounded ends. */
    public static Plan createRoots(Random random, BlockPos feet, int maxY) {
        int room = room(feet, maxY);
        if (room < 3) return null;
        Builder b = new Builder(feet);
        int span = 3 + random.nextInt(3);
        int rise = Math.min(room - 1, 1 + random.nextInt(3));
        boolean alongX = random.nextBoolean();
        int sign = random.nextBoolean() ? 1 : -1;
        BlockPos end = feet.add(alongX ? span * sign : 0, 0, alongX ? 0 : span * sign);
        b.support(feet);
        b.support(end);
        b.line(feet, feet.up(rise), "ancient_root");
        b.line(feet.up(rise), end.up(rise - 1), "ancient_root");
        b.line(end.up(rise - 1), end, "ancient_root");
        return new Plan(b);
    }

    /** Compact bushes share the same placement and support contract as full trees. */
    public static Plan createShrub(Random random, BlockPos feet, int maxY, boolean white) {
        if (room(feet, maxY) < 4) return null;
        Builder b = new Builder(feet);
        b.support(feet);
        int stem = Math.min(1 + random.nextInt(2), room(feet, maxY) - 3);
        b.line(feet, feet.up(stem), "jotunn_oak_log");
        b.foliage(feet.up(stem + 1), 2, 1, white ? "white_crown_leaves" : "jotunn_oak_leaves");
        return new Plan(b);
    }

    private static int room(BlockPos feet, int maxY) {
        return feet.getY() < 1 ? 0 : Math.min(255, maxY) - feet.getY() + 1;
    }

    private static int height(Random random, int room, int min, int desired, int variation, int crown) {
        int h = Math.min(desired + random.nextInt(variation), room - crown);
        return h >= min ? h : -1;
    }

    private static void oak(Builder b, Random random, int room) {
        int h = height(random, room, 8, 11, 5, 3);
        if (h < 0) return;
        BlockPos p = b.origin;
        for (int x = 0; x <= 1; x++) for (int z = 0; z <= 1; z++) {
            BlockPos foot = p.add(x, 0, z);
            b.support(foot);
            b.line(foot, foot.up(h - 2 + random.nextInt(2)), "jotunn_oak_log");
        }
        int turn = random.nextInt(4);
        for (int side = 0; side < 4; side++) {
            int index = (side + turn) & 3;
            int dx = index == 0 ? 4 : index == 2 ? -4 : 0;
            int dz = index == 1 ? 4 : index == 3 ? -4 : 0;
            BlockPos start = p.add(dx > 0 ? 1 : 0, h - 5, dz > 0 ? 1 : 0);
            BlockPos end = p.add(dx, h - 2 + random.nextInt(2), dz);
            b.line(start, end, "jotunn_oak_log");
            b.foliage(end.up(1), 3, 2, "jotunn_oak_leaves");
        }
        b.foliage(p.add(1, h, 1), 3, 2, "jotunn_oak_leaves");
    }

    private static void pine(Builder b, Random random, int room) {
        int h = height(random, room, 8, 12, 8, 2);
        if (h < 0) return;
        BlockPos p = b.origin;
        b.support(p);
        b.line(p, p.up(h), "elder_pine_log");
        int lowest = Math.max(3, h / 3);
        for (int y = lowest; y < h; y += 3) {
            int radius = Math.max(1, Math.min(4, (h - y + 2) / 3));
            for (int d = -1; d <= 1; d++) {
                b.foliage(p.up(y + d), Math.max(1, radius - (d > 0 ? 1 : 0)), 0, "elder_pine_leaves");
            }
            if (radius > 1) {
                b.line(p.up(y), p.add(radius - 1, y, 0), "elder_pine_log");
                b.line(p.up(y), p.add(0, y, 1 - radius), "elder_pine_log");
            }
        }
        b.foliage(p.up(h), 1, 1, "elder_pine_leaves");
    }

    private static void sunken(Builder b, Random random, int room) {
        int h = height(random, room, 6, 8, 5, 3);
        if (h < 0) return;
        BlockPos p = b.origin;
        int dx = random.nextBoolean() ? 1 : -1;
        int dz = random.nextBoolean() ? 1 : -1;
        b.support(p);
        BlockPos bend = p.add(dx, h / 2, 0);
        BlockPos crown = p.add(dx * 2, h - 1, dz);
        b.line(p, p.up(h / 2), "sunkenwood_log");
        b.line(p.up(h / 2), bend, "sunkenwood_log");
        b.line(bend, crown, "sunkenwood_log");
        b.foliage(crown.up(1), 4, 2, "sunkenwood_leaves");
        BlockPos branch = p.add(-dx * 2, h - 2, dz * 2);
        b.line(bend.up(1), branch, "sunkenwood_log");
        b.foliage(branch.up(1), 3, 1, "sunkenwood_leaves");
        // Short knee roots remain connected and have their own ground contacts.
        for (int sign : new int[] {-1, 1}) {
            BlockPos root = p.add(0, 0, sign * 2);
            b.support(root);
            b.line(p.up(2), root.up(1), "ancient_root");
            b.line(root.up(1), root, "ancient_root");
        }
    }

    private static void whiteOak(Builder b, Random random, int room) {
        int h = height(random, room, 4, 5, 4, 3);
        if (h < 0) return;
        BlockPos p = b.origin;
        b.support(p);
        b.line(p, p.up(h), "jotunn_oak_log");
        int dx = random.nextBoolean() ? 1 : -1;
        b.line(p.up(h - 2), p.add(dx * 2, h - 1, 0), "jotunn_oak_log");
        b.foliage(p.up(h), 3, 2, "white_crown_leaves");
        b.foliage(p.add(dx * 2, h - 1, 0), 2, 1, "white_crown_leaves");
    }

    private static void charred(Builder b, Random random, int room) {
        int h = height(random, room, 4, 5, 6, 1);
        if (h < 0) return;
        BlockPos p = b.origin;
        b.support(p);
        int dx = random.nextBoolean() ? 1 : -1;
        b.line(p, p.up(h / 2), "charred_log");
        b.line(p.up(h / 2), p.add(dx, h - 1, 0), "charred_log");
        b.line(p.up(h / 2), p.add(-dx * (2 + random.nextInt(2)), h / 2 + 1, 0), "charred_log");
        if (h > 6) b.line(p.add(dx, h - 3, 0), p.add(dx, h - 2, 2), "charred_log");
    }

    private static void lightning(Builder b, Random random, int room) {
        int h = height(random, room, 6, 8, 6, 2);
        if (h < 0) return;
        BlockPos p = b.origin;
        b.support(p);
        b.line(p, p.up(h - 3), "lightning_struck_log");
        int dx = random.nextBoolean() ? 1 : -1;
        BlockPos tallFork = p.add(dx * 2, h, 0);
        BlockPos shortFork = p.add(-dx * 2, h - 2, 1);
        b.line(p.up(h - 4), tallFork, "lightning_struck_log");
        b.line(p.up(h - 4), shortFork, "lightning_struck_log");
        b.foliage(shortFork, 2, 1, "elder_pine_leaves");
        if (random.nextBoolean()) b.foliage(tallFork.down(1), 1, 1, "elder_pine_leaves");
    }

    private static void fungus(Builder b, Random random, int room, boolean ember) {
        int h = height(random, room, 3, ember ? 3 : 5, ember ? 4 : 5, 3);
        if (h < 0) return;
        BlockPos p = b.origin;
        b.support(p);
        b.line(p, p.up(h - 1), "fungal_stem");
        int radius = ember ? 2 + random.nextInt(3) : 3 + random.nextInt(3);
        String cap = ember ? "ember_fungus_block" : "luminous_fungal_block";
        for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
            double d = x * x + z * z;
            if (d > radius * radius + 1) continue;
            if (ember) {
                // A thick, stepped fire-fungus bonnet rather than the luminous umbrella.
                b.solid(p.add(x, h, z), cap);
                if (d <= (radius - 1) * (radius - 1) + 1) b.solid(p.add(x, h + 1, z), cap);
                if (d <= Math.max(1, radius - 2)) b.solid(p.add(x, h + 2, z), cap);
            } else {
                // Broad gill plane, rolled rim and a small domed crown.
                b.solid(p.add(x, h, z), cap);
                if (d >= (radius - 1) * (radius - 1)) b.solid(p.add(x, h - 1, z), cap);
                if (d <= (radius - 2) * (radius - 2) + 1) b.solid(p.add(x, h + 1, z), cap);
                if (d <= 1) b.solid(p.add(x, h + 2, z), cap);
            }
        }
    }

    private static final class Builder {
        final BlockPos origin;
        final Map<BlockPos, IBlockState> blocks = new LinkedHashMap<>();
        final Set<BlockPos> supports = new LinkedHashSet<>();
        final Set<BlockPos> structural = new LinkedHashSet<>();

        Builder(BlockPos origin) { this.origin = origin.toImmutable(); }

        void support(BlockPos feet) { supports.add(feet.toImmutable()); }

        void solid(BlockPos pos, String key) { put(pos, JotunheimBlocks.state(key), true); }

        void log(BlockPos pos, String key, BlockLog.EnumAxis axis) {
            put(pos, JotunheimBlocks.state(key).withProperty(BlockLog.LOG_AXIS, axis), true);
        }

        void put(BlockPos pos, IBlockState state, boolean solid) {
            BlockPos immutable = pos.toImmutable();
            if (solid) structural.add(immutable);
            if (solid || !structural.contains(immutable)) blocks.put(immutable, state);
        }

        void line(BlockPos from, BlockPos to, String key) {
            int dx = to.getX() - from.getX(), dy = to.getY() - from.getY(), dz = to.getZ() - from.getZ();
            int length = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
            BlockPos previous = from;
            log(previous, key, BlockLog.EnumAxis.Y);
            for (int n = 1; n <= length; n++) {
                BlockPos next = from.add(Math.round(dx * n / (float) length),
                        Math.round(dy * n / (float) length), Math.round(dz * n / (float) length));
                // Explicit one-axis steps keep ascending branches face-connected.
                while (previous.getX() != next.getX()) {
                    previous = previous.add(Integer.signum(next.getX() - previous.getX()), 0, 0);
                    log(previous, key, BlockLog.EnumAxis.X);
                }
                while (previous.getZ() != next.getZ()) {
                    previous = previous.add(0, 0, Integer.signum(next.getZ() - previous.getZ()));
                    log(previous, key, BlockLog.EnumAxis.Z);
                }
                while (previous.getY() != next.getY()) {
                    previous = previous.add(0, Integer.signum(next.getY() - previous.getY()), 0);
                    log(previous, key, BlockLog.EnumAxis.Y);
                }
            }
        }

        void foliage(BlockPos center, int radius, int halfHeight, String key) {
            IBlockState leaves = JotunheimBlocks.state(key)
                    .withProperty(BlockLeaves.CHECK_DECAY, false).withProperty(BlockLeaves.DECAYABLE, false);
            for (int dy = -halfHeight; dy <= halfHeight; dy++) {
                double slice = halfHeight == 0 ? 1.0D : 1.0D - 0.65D * dy * dy / (halfHeight * halfHeight);
                for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > (radius * radius + 0.5D) * slice) continue;
                    put(center.add(dx, dy, dz), leaves, false);
                }
            }
        }
    }
}
