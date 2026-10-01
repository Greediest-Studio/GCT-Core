package com.gmm.gctold.common.world.dimension;

import com.gmm.gctold.common.util.PlayerDimensionTransferHelper;
import com.gmm.gctold.common.world.biome.BiomeVoidHill;
import com.gmm.gctold.common.world.biome.BiomeVoidPlain;

import javax.annotation.Nullable;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Biomes;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import java.util.List;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraft.block.BlockFalling;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.material.Material;
import com.google.common.cache.LoadingCache;
import java.util.Random;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockPortal;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Teleporter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.block.Block;
import net.minecraft.world.DimensionType;
public class WorldTheVoid {
    public static int DIMID = 100;
    public static final boolean NETHER_TYPE = false;
    public static final PortalBlock portal = new PortalBlock();
    public static DimensionType dtype;

    public static void registerDimension() {
        if (DimensionManager.isDimensionRegistered(WorldTheVoid.DIMID)) {
            WorldTheVoid.DIMID = DimensionManager.getNextFreeDimId();
            System.err.println("Dimension ID for dimension the_void is already registered. Falling back to ID: " + WorldTheVoid.DIMID);
        }
        WorldTheVoid.dtype = DimensionType.register("the_void", "_the_void", WorldTheVoid.DIMID, WorldProviderMod.class, false);
        DimensionManager.registerDimension(WorldTheVoid.DIMID, WorldTheVoid.dtype);
    }

  public static class WorldProviderMod extends WorldProvider
    {
        public void init() {
            this.biomeProvider = new BiomeProviderCustom(this.world.getSeed());
            this.nether = false;
        }
        
        public void calculateInitialWeather() {
        }
        
        public void updateWeather() {
        }
        
        public boolean canDoLightning(final Chunk chunk) {
            return false;
        }
        
        public boolean canDoRainSnowIce(final Chunk chunk) {
            return false;
        }
        
        public DimensionType getDimensionType() {
            return WorldTheVoid.dtype;
        }
        
        @SideOnly(Side.CLIENT)
        public Vec3d getFogColor(final float par1, final float par2) {
            return new Vec3d(0.6, 0.6, 0.6);
        }
        
        public IChunkGenerator createChunkGenerator() {
            return (IChunkGenerator)new ChunkProviderModded(this.world, this.world.getSeed() - WorldTheVoid.DIMID);
        }
        
        public boolean isSurfaceWorld() {
            return false;
        }
        
        public boolean canRespawnHere() {
            return false;
        }
        
        @SideOnly(Side.CLIENT)
        public boolean doesXZShowFog(final int par1, final int par2) {
            return true;
        }
        
        public WorldProvider.WorldSleepResult canSleepAt(final EntityPlayer player, final BlockPos pos) {
            return WorldProvider.WorldSleepResult.BED_EXPLODES;
        }
        
        public boolean doesWaterVaporize() {
            return false;
        }
    }

  public static class TeleporterDimensionMod extends Teleporter
    {
        private Vec3d lastPortalVec;
        private EnumFacing teleportDirection;
        
        public TeleporterDimensionMod(final WorldServer worldServer, final Vec3d lastPortalVec, final EnumFacing teleportDirection) {
            super(worldServer);
            this.lastPortalVec = lastPortalVec;
            this.teleportDirection = teleportDirection;
        }
        
        public boolean makePortal(final Entity entityIn) {
            final int i = 16;
            double d0 = -1.0;
            final int j = MathHelper.floor(entityIn.posX);
            final int k = MathHelper.floor(entityIn.posY);
            final int l = MathHelper.floor(entityIn.posZ);
            int i2 = j;
            int j2 = k;
            int k2 = l;
            int l2 = 0;
            final int i3 = this.random.nextInt(4);
            final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            for (int j3 = j - 16; j3 <= j + 16; ++j3) {
                final double d2 = j3 + 0.5 - entityIn.posX;
                for (int l3 = l - 16; l3 <= l + 16; ++l3) {
                    final double d3 = l3 + 0.5 - entityIn.posZ;
                Label_0462:
                    for (int j4 = this.world.getActualHeight() - 1; j4 >= 0; --j4) {
                        if (this.world.isAirBlock((BlockPos)blockpos$mutableblockpos.setPos(j3, j4, l3))) {
                            while (j4 > 0 && this.world.isAirBlock((BlockPos)blockpos$mutableblockpos.setPos(j3, j4 - 1, l3))) {
                                --j4;
                            }
                            for (int k3 = i3; k3 < i3 + 4; ++k3) {
                                int l4 = k3 % 2;
                                int i4 = 1 - l4;
                                if (k3 % 4 >= 2) {
                                    l4 = -l4;
                                    i4 = -i4;
                                }
                                for (int j5 = 0; j5 < 3; ++j5) {
                                    for (int k4 = 0; k4 < 4; ++k4) {
                                        for (int l5 = -1; l5 < 4; ++l5) {
                                            final int i5 = j3 + (k4 - 1) * l4 + j5 * i4;
                                            final int j6 = j4 + l5;
                                            final int k5 = l3 + (k4 - 1) * i4 - j5 * l4;
                                            blockpos$mutableblockpos.setPos(i5, j6, k5);
                                            if (l5 < 0 && !this.world.getBlockState((BlockPos)blockpos$mutableblockpos).getMaterial().isSolid()) {
                                                continue Label_0462;
                                            }
                                            if (l5 >= 0 && !this.world.isAirBlock((BlockPos)blockpos$mutableblockpos)) {
                                                continue Label_0462;
                                            }
                                        }
                                    }
                                }
                                final double d4 = j4 + 0.5 - entityIn.posY;
                                final double d5 = d2 * d2 + d4 * d4 + d3 * d3;
                                if (d0 < 0.0 || d5 < d0) {
                                    d0 = d5;
                                    i2 = j3;
                                    j2 = j4;
                                    k2 = l3;
                                    l2 = k3 % 4;
                                }
                            }
                        }
                    }
                }
            }
            if (d0 < 0.0) {
                for (int l6 = j - 16; l6 <= j + 16; ++l6) {
                    final double d6 = l6 + 0.5 - entityIn.posX;
                    for (int j7 = l - 16; j7 <= l + 16; ++j7) {
                        final double d7 = j7 + 0.5 - entityIn.posZ;
                    Label_0835:
                        for (int i6 = this.world.getActualHeight() - 1; i6 >= 0; --i6) {
                            if (this.world.isAirBlock((BlockPos)blockpos$mutableblockpos.setPos(l6, i6, j7))) {
                                while (i6 > 0 && this.world.isAirBlock((BlockPos)blockpos$mutableblockpos.setPos(l6, i6 - 1, j7))) {
                                    --i6;
                                }
                                for (int k6 = i3; k6 < i3 + 2; ++k6) {
                                    final int j8 = k6 % 2;
                                    final int j9 = 1 - j8;
                                    for (int j10 = 0; j10 < 4; ++j10) {
                                        for (int j11 = -1; j11 < 4; ++j11) {
                                            final int j12 = l6 + (j10 - 1) * j8;
                                            final int i7 = i6 + j11;
                                            final int j13 = j7 + (j10 - 1) * j9;
                                            blockpos$mutableblockpos.setPos(j12, i7, j13);
                                            if (j11 < 0 && !this.world.getBlockState((BlockPos)blockpos$mutableblockpos).getMaterial().isSolid()) {
                                                continue Label_0835;
                                            }
                                            if (j11 >= 0 && !this.world.isAirBlock((BlockPos)blockpos$mutableblockpos)) {
                                                continue Label_0835;
                                            }
                                        }
                                    }
                                    final double d8 = i6 + 0.5 - entityIn.posY;
                                    final double d9 = d6 * d6 + d8 * d8 + d7 * d7;
                                    if (d0 < 0.0 || d9 < d0) {
                                        d0 = d9;
                                        i2 = l6;
                                        j2 = i6;
                                        k2 = j7;
                                        l2 = k6 % 2;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            final int i8 = i2;
            int k7 = j2;
            final int k8 = k2;
            int l7 = l2 % 2;
            int i9 = 1 - l7;
            if (l2 % 4 >= 2) {
                l7 = -l7;
                i9 = -i9;
            }
            if (d0 < 0.0) {
                j2 = (k7 = MathHelper.clamp(j2, 70, this.world.getActualHeight() - 10));
                for (int j14 = -1; j14 <= 1; ++j14) {
                    for (int l8 = 1; l8 < 3; ++l8) {
                        for (int k9 = -1; k9 < 3; ++k9) {
                            final int k10 = i8 + (l8 - 1) * l7 + j14 * i9;
                            final int k11 = k7 + k9;
                            final int k12 = k8 + (l8 - 1) * i9 - j14 * l7;
                            final boolean flag = k9 < 0;
                            this.world.setBlockState(new BlockPos(k10, k11, k12), flag ? Blocks.BEDROCK.getDefaultState().getBlock().getDefaultState() : Blocks.AIR.getDefaultState());
                        }
                    }
                }
            }
            final IBlockState iblockstate = WorldTheVoid.portal.getDefaultState().withProperty((IProperty)BlockPortal.AXIS, (Comparable)((l7 == 0) ? EnumFacing.Axis.Z : EnumFacing.Axis.X));
            for (int i10 = 0; i10 < 4; ++i10) {
                for (int l9 = 0; l9 < 4; ++l9) {
                    for (int l10 = -1; l10 < 4; ++l10) {
                        final int l11 = i8 + (l9 - 1) * l7;
                        final int l12 = k7 + l10;
                        final int k13 = k8 + (l9 - 1) * i9;
                        final boolean flag2 = l9 == 0 || l9 == 3 || l10 == -1 || l10 == 3;
                        this.world.setBlockState(new BlockPos(l11, l12, k13), flag2 ? Blocks.BEDROCK.getDefaultState().getBlock().getDefaultState() : iblockstate, 2);
                    }
                }
                for (int i11 = 0; i11 < 4; ++i11) {
                    for (int i12 = -1; i12 < 4; ++i12) {
                        final int i13 = i8 + (i11 - 1) * l7;
                        final int i14 = k7 + i12;
                        final int l13 = k8 + (i11 - 1) * i9;
                        final BlockPos blockpos = new BlockPos(i13, i14, l13);
                        this.world.notifyNeighborsOfStateChange(blockpos, this.world.getBlockState(blockpos).getBlock(), false);
                    }
                }
            }
            return true;
        }
        
        public void placeInPortal(final Entity entityIn, final float rotationYaw) {
            if (this.world.provider.getDimensionType().getId() != 1) {
                if (!this.placeInExistingPortal(entityIn, rotationYaw)) {
                    this.makePortal(entityIn);
                    this.placeInExistingPortal(entityIn, rotationYaw);
                }
            }
            else {
                final int i = MathHelper.floor(entityIn.posX);
                final int j = MathHelper.floor(entityIn.posY) - 1;
                final int k = MathHelper.floor(entityIn.posZ);
                final int l = 1;
                final int i2 = 0;
                for (int j2 = -2; j2 <= 2; ++j2) {
                    for (int k2 = -2; k2 <= 2; ++k2) {
                        for (int l2 = -1; l2 < 3; ++l2) {
                            final int i3 = i + k2 * 1 + j2 * 0;
                            final int j3 = j + l2;
                            final int k3 = k + k2 * 0 - j2 * 1;
                            final boolean flag = l2 < 0;
                            this.world.setBlockState(new BlockPos(i3, j3, k3), flag ? Blocks.BEDROCK.getDefaultState().getBlock().getDefaultState() : Blocks.AIR.getDefaultState());
                        }
                    }
                }
                entityIn.setLocationAndAngles((double)i, (double)j, (double)k, entityIn.rotationYaw, 0.0f);
                entityIn.motionX = 0.0;
                entityIn.motionY = 0.0;
                entityIn.motionZ = 0.0;
            }
        }
        
        public boolean placeInExistingPortal(final Entity entityIn, final float rotationYaw) {
            final int i = 128;
            double d0 = -1.0;
            final int j = MathHelper.floor(entityIn.posX);
            final int k = MathHelper.floor(entityIn.posZ);
            boolean flag = true;
            BlockPos blockpos = BlockPos.ORIGIN;
            final long l = ChunkPos.asLong(j, k);
            if (this.destinationCoordinateCache.containsKey(l)) {
                final Teleporter.PortalPosition teleporter$portalposition = (Teleporter.PortalPosition)this.destinationCoordinateCache.get(l);
                d0 = 0.0;
                blockpos = (BlockPos)teleporter$portalposition;
                teleporter$portalposition.lastUpdateTime = this.world.getTotalWorldTime();
                flag = false;
            }
            else {
                final BlockPos blockpos2 = new BlockPos(entityIn);
                for (int i2 = -128; i2 <= 128; ++i2) {
                    for (int j2 = -128; j2 <= 128; ++j2) {
                        BlockPos blockpos4;
                        for (BlockPos blockpos3 = blockpos2.add(i2, this.world.getActualHeight() - 1 - blockpos2.getY(), j2); blockpos3.getY() >= 0; blockpos3 = blockpos4) {
                            blockpos4 = blockpos3.down();
                            if (this.world.getBlockState(blockpos3).getBlock() == WorldTheVoid.portal) {
                                for (blockpos4 = blockpos3.down(); this.world.getBlockState(blockpos4).getBlock() == WorldTheVoid.portal; blockpos4 = blockpos4.down()) {
                                    blockpos3 = blockpos4;
                                }
                                final double d2 = blockpos3.distanceSq((Vec3i)blockpos2);
                                if (d0 < 0.0 || d2 < d0) {
                                    d0 = d2;
                                    blockpos = blockpos3;
                                }
                            }
                        }
                    }
                }
            }
            if (d0 >= 0.0) {
                if (flag) {
                    this.destinationCoordinateCache.put(l, new Teleporter.PortalPosition(blockpos, this.world.getTotalWorldTime()));
                }
                double d3 = blockpos.getX() + 0.5;
                double d4 = blockpos.getZ() + 0.5;
                final BlockPattern.PatternHelper blockpattern$patternhelper = WorldTheVoid.portal.createPatternHelper((World)this.world, blockpos);
                final boolean flag2 = blockpattern$patternhelper.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
                double d5 = (blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X) ? blockpattern$patternhelper.getFrontTopLeft().getZ() : ((double)blockpattern$patternhelper.getFrontTopLeft().getX());
                final double d6 = blockpattern$patternhelper.getFrontTopLeft().getY() + 1 - this.lastPortalVec.y * blockpattern$patternhelper.getHeight();
                if (flag2) {
                    ++d5;
                }
                if (blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X) {
                    d4 = d5 + (1.0 - this.lastPortalVec.x) * blockpattern$patternhelper.getWidth() * blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
                }
                else {
                    d3 = d5 + (1.0 - this.lastPortalVec.x) * blockpattern$patternhelper.getWidth() * blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
                }
                float f = 0.0f;
                float f2 = 0.0f;
                float f3 = 0.0f;
                float f4 = 0.0f;
                if (blockpattern$patternhelper.getForwards().getOpposite() == this.teleportDirection) {
                    f = 1.0f;
                    f2 = 1.0f;
                }
                else if (blockpattern$patternhelper.getForwards().getOpposite() == this.teleportDirection.getOpposite()) {
                    f = -1.0f;
                    f2 = -1.0f;
                }
                else if (blockpattern$patternhelper.getForwards().getOpposite() == this.teleportDirection.rotateY()) {
                    f3 = 1.0f;
                    f4 = -1.0f;
                }
                else {
                    f3 = -1.0f;
                    f4 = 1.0f;
                }
                final double d7 = entityIn.motionX;
                final double d8 = entityIn.motionZ;
                entityIn.motionX = d7 * f + d8 * f4;
                entityIn.motionZ = d7 * f3 + d8 * f2;
                entityIn.rotationYaw = rotationYaw - this.teleportDirection.getOpposite().getHorizontalIndex() * 90 + blockpattern$patternhelper.getForwards().getHorizontalIndex() * 90;
                if (entityIn instanceof EntityPlayerMP) {
                    ((EntityPlayerMP)entityIn).connection.setPlayerLocation(d3, d6, d4, entityIn.rotationYaw, entityIn.rotationPitch);
                }
                else {
                    entityIn.setLocationAndAngles(d3, d6, d4, entityIn.rotationYaw, entityIn.rotationPitch);
                }
                return true;
            }
            return false;
        }
    }

  public static class PortalBlock extends BlockPortal
    {
        public PortalBlock() {
            this.setHardness(-1.0f);
            this.setTranslationKey("the_void_portal");
            this.setRegistryName("the_void_portal");
            this.setLightLevel(0.0f);
        }
        
        public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random random) {
        }
        
        public void portalSpawn(final World worldIn, final BlockPos pos) {
            Size portalsize = new Size(worldIn, pos, EnumFacing.Axis.X);
            if (portalsize.isValid() && portalsize.portalBlockCount == 0) {
                portalsize.placePortalBlocks();
            }
            else {
                portalsize = new Size(worldIn, pos, EnumFacing.Axis.Z);
                if (portalsize.isValid() && portalsize.portalBlockCount == 0) {
                    portalsize.placePortalBlocks();
                }
            }
        }
        
        public BlockPattern.PatternHelper createPatternHelper(final World worldIn, final BlockPos pos) {
            EnumFacing.Axis enumfacing$axis = EnumFacing.Axis.Z;
            Size blockportal$size = new Size(worldIn, pos, EnumFacing.Axis.X);
            final LoadingCache<BlockPos, BlockWorldState> loadingcache = (LoadingCache<BlockPos, BlockWorldState>)BlockPattern.createLoadingCache(worldIn, true);
            if (!blockportal$size.isValid()) {
                enumfacing$axis = EnumFacing.Axis.X;
                blockportal$size = new Size(worldIn, pos, EnumFacing.Axis.Z);
            }
            if (!blockportal$size.isValid()) {
                return new BlockPattern.PatternHelper(pos, EnumFacing.NORTH, EnumFacing.UP, (LoadingCache)loadingcache, 1, 1, 1);
            }
            final int[] aint = new int[EnumFacing.AxisDirection.values().length];
            final EnumFacing enumfacing = blockportal$size.rightDir.rotateYCCW();
            final BlockPos blockpos = blockportal$size.bottomLeft.up(blockportal$size.getHeight() - 1);
            for (final EnumFacing.AxisDirection enumfacing$axisdirection : EnumFacing.AxisDirection.values()) {
                final BlockPattern.PatternHelper blockpattern$patternhelper = new BlockPattern.PatternHelper((enumfacing.getAxisDirection() == enumfacing$axisdirection) ? blockpos : blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1), EnumFacing.getFacingFromAxis(enumfacing$axisdirection, enumfacing$axis), EnumFacing.UP, (LoadingCache)loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);
                for (int i = 0; i < blockportal$size.getWidth(); ++i) {
                    for (int j = 0; j < blockportal$size.getHeight(); ++j) {
                        final BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(i, j, 1);
                        if (blockworldstate.getBlockState() != null && blockworldstate.getBlockState().getMaterial() != Material.AIR) {
                            final int[] array = aint;
                            final int ordinal = enumfacing$axisdirection.ordinal();
                            ++array[ordinal];
                        }
                    }
                }
            }
            EnumFacing.AxisDirection enumfacing$axisdirection2 = EnumFacing.AxisDirection.POSITIVE;
            for (final EnumFacing.AxisDirection enumfacing$axisdirection3 : EnumFacing.AxisDirection.values()) {
                if (aint[enumfacing$axisdirection3.ordinal()] < aint[enumfacing$axisdirection2.ordinal()]) {
                    enumfacing$axisdirection2 = enumfacing$axisdirection3;
                }
            }
            return new BlockPattern.PatternHelper((enumfacing.getAxisDirection() == enumfacing$axisdirection2) ? blockpos : blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1), EnumFacing.getFacingFromAxis(enumfacing$axisdirection2, enumfacing$axis), EnumFacing.UP, (LoadingCache)loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);
        }
        
        public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
            final EnumFacing.Axis enumfacing$axis = (EnumFacing.Axis)state.getValue((IProperty)PortalBlock.AXIS);
            if (enumfacing$axis == EnumFacing.Axis.X) {
                final Size blockportal$size = new Size(worldIn, pos, EnumFacing.Axis.X);
                if (!blockportal$size.isValid() || blockportal$size.portalBlockCount < blockportal$size.width * blockportal$size.height) {
                    worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }
            else if (enumfacing$axis == EnumFacing.Axis.Z) {
                final Size blockportal$size2 = new Size(worldIn, pos, EnumFacing.Axis.Z);
                if (!blockportal$size2.isValid() || blockportal$size2.portalBlockCount < blockportal$size2.width * blockportal$size2.height) {
                    worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }
        }
        
        @SideOnly(Side.CLIENT)
        public void randomDisplayTick(final IBlockState state, final World world, final BlockPos pos, final Random random) {
            for (int i = 0; i < 4; ++i) {
                double px = pos.getX() + random.nextFloat();
                final double py = pos.getY() + random.nextFloat();
                double pz = pos.getZ() + random.nextFloat();
                double vx = (random.nextFloat() - 0.5) / 2.0;
                final double vy = (random.nextFloat() - 0.5) / 2.0;
                double vz = (random.nextFloat() - 0.5) / 2.0;
                final int j = random.nextInt(4) - 1;
                if (world.getBlockState(pos.west()).getBlock() != this && world.getBlockState(pos.east()).getBlock() != this) {
                    px = pos.getX() + 0.5 + 0.25 * j;
                    vx = random.nextFloat() * 2.0f * j;
                }
                else {
                    pz = pos.getZ() + 0.5 + 0.25 * j;
                    vz = random.nextFloat() * 2.0f * j;
                }
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, py, pz, vx, vy, vz, new int[0]);
            }
        }
        
        public void onEntityCollision(final World worldIn, final BlockPos pos, final IBlockState state, final Entity entityIn) {
            if (!worldIn.isRemote && !entityIn.isRiding() && !entityIn.isBeingRidden() && entityIn instanceof EntityPlayerMP) {
                final EntityPlayerMP thePlayer = (EntityPlayerMP)entityIn;
                if (thePlayer.timeUntilPortal > 0) {
                    thePlayer.timeUntilPortal = 10;
                }
                else if (thePlayer.dimension != WorldTheVoid.DIMID) {
                    thePlayer.timeUntilPortal = 10;
          PlayerDimensionTransferHelper.transfer(thePlayer, WorldTheVoid.DIMID, this.getTeleporterForDimension((Entity)thePlayer, pos, WorldTheVoid.DIMID));
                }
                else {
                    thePlayer.timeUntilPortal = 10;
          PlayerDimensionTransferHelper.transfer(thePlayer, 0, this.getTeleporterForDimension((Entity)thePlayer, pos, 0));
                }
            }
        }
        
        private TeleporterDimensionMod getTeleporterForDimension(final Entity entity, final BlockPos pos, final int dimid) {
            final BlockPattern.PatternHelper bph = WorldTheVoid.portal.createPatternHelper(entity.world, new BlockPos((Vec3i)pos));
            final double d0 = (bph.getForwards().getAxis() == EnumFacing.Axis.X) ? bph.getFrontTopLeft().getZ() : ((double)bph.getFrontTopLeft().getX());
            double d2 = (bph.getForwards().getAxis() == EnumFacing.Axis.X) ? entity.posZ : entity.posX;
            d2 = Math.abs(MathHelper.pct(d2 - (double)((bph.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) ? 1 : 0), d0, d0 - bph.getWidth()));
            final double d3 = MathHelper.pct(entity.posY - 1.0, (double)bph.getFrontTopLeft().getY(), (double)(bph.getFrontTopLeft().getY() - bph.getHeight()));
            return new TeleporterDimensionMod(entity.getServer().getWorld(dimid), new Vec3d(d2, d3, 0.0), bph.getForwards());
        }
        
        public static class Size
        {
            private final World world;
            private final EnumFacing.Axis axis;
            private final EnumFacing rightDir;
            private final EnumFacing leftDir;
            private int portalBlockCount;
            private BlockPos bottomLeft;
            private int height;
            private int width;
            
            public Size(final World worldIn, BlockPos pos, final EnumFacing.Axis axis) {
                this.world = worldIn;
                this.axis = axis;
                if (axis == EnumFacing.Axis.X) {
                    this.leftDir = EnumFacing.EAST;
                    this.rightDir = EnumFacing.WEST;
                }
                else {
                    this.leftDir = EnumFacing.NORTH;
                    this.rightDir = EnumFacing.SOUTH;
                }
                for (BlockPos blockpos = pos; pos.getY() > blockpos.getY() - 21 && pos.getY() > 0 && this.isEmptyBlock(worldIn.getBlockState(pos.down()).getBlock()); pos = pos.down()) {}
                final int i = this.getDistanceUntilEdge(pos, this.leftDir) - 1;
                if (i >= 0) {
                    this.bottomLeft = pos.offset(this.leftDir, i);
                    this.width = this.getDistanceUntilEdge(this.bottomLeft, this.rightDir);
                    if (this.width < 2 || this.width > 21) {
                        this.bottomLeft = null;
                        this.width = 0;
                    }
                }
                if (this.bottomLeft != null) {
                    this.height = this.calculatePortalHeight();
                }
            }
            
            protected int getDistanceUntilEdge(final BlockPos pos, final EnumFacing direction) {
                int i;
                for (i = 0; i < 22; ++i) {
                    final BlockPos blockpos = pos.offset(direction, i);
                    if (!this.isEmptyBlock(this.world.getBlockState(blockpos).getBlock())) {
                        break;
                    }
                    if (this.world.getBlockState(blockpos.down()).getBlock() != Blocks.BEDROCK.getDefaultState().getBlock()) {
                        break;
                    }
                }
                final Block block = this.world.getBlockState(pos.offset(direction, i)).getBlock();
                return (block == Blocks.BEDROCK.getDefaultState().getBlock()) ? i : 0;
            }
            
            public int getHeight() {
                return this.height;
            }
            
            public int getWidth() {
                return this.width;
            }
            
            protected int calculatePortalHeight() {
                this.height = 0;
            Label_0197:
                while (this.height < 21) {
                    for (int i = 0; i < this.width; ++i) {
                        final BlockPos blockpos = this.bottomLeft.offset(this.rightDir, i).up(this.height);
                        Block block = this.world.getBlockState(blockpos).getBlock();
                        if (!this.isEmptyBlock(block)) {
                            break Label_0197;
                        }
                        if (block == WorldTheVoid.portal) {
                            ++this.portalBlockCount;
                        }
                        if (i == 0) {
                            block = this.world.getBlockState(blockpos.offset(this.leftDir)).getBlock();
                            if (block != Blocks.BEDROCK.getDefaultState().getBlock()) {
                                break Label_0197;
                            }
                        }
                        else if (i == this.width - 1) {
                            block = this.world.getBlockState(blockpos.offset(this.rightDir)).getBlock();
                            if (block != Blocks.BEDROCK.getDefaultState().getBlock()) {
                                break Label_0197;
                            }
                        }
                    }
                    ++this.height;
                }
                for (int j = 0; j < this.width; ++j) {
                    if (this.world.getBlockState(this.bottomLeft.offset(this.rightDir, j).up(this.height)).getBlock() != Blocks.BEDROCK.getDefaultState().getBlock()) {
                        this.height = 0;
                        break;
                    }
                }
                if (this.height <= 21 && this.height >= 3) {
                    return this.height;
                }
                this.bottomLeft = null;
                this.width = 0;
                return this.height = 0;
            }
            
            protected boolean isEmptyBlock(final Block blockIn) {
                return blockIn.getDefaultState().getMaterial() == Material.AIR || blockIn == Blocks.FIRE || blockIn == WorldTheVoid.portal;
            }
            
            public boolean isValid() {
                return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
            }
            
            public void placePortalBlocks() {
                for (int i = 0; i < this.width; ++i) {
                    final BlockPos blockpos = this.bottomLeft.offset(this.rightDir, i);
                    for (int j = 0; j < this.height; ++j) {
                        this.world.setBlockState(blockpos.up(j), WorldTheVoid.portal.getDefaultState().withProperty((IProperty)BlockPortal.AXIS, (Comparable)this.axis), 2);
                    }
                }
            }
        }
    }

  public static class ChunkProviderModded implements IChunkGenerator
    {
        private static final IBlockState STONE;
        private static final IBlockState AIR;
        private static final int SEALEVEL = 63;
        private final World world;
        private Random random;
        private final NoiseGeneratorSimplex islandNoise;
        private final NoiseGeneratorOctaves perlinnoise1;
        private final NoiseGeneratorOctaves perlinnoise2;
        private final NoiseGeneratorOctaves perlinnoise3;
        private final NoiseGeneratorPerlin height;
        private Biome[] biomesForGeneration;
        private double[] buffer;
        private double[] pnr;
        private double[] ar;
        private double[] br;
        private double[] depthbuff;
        private WorldGenerator islandGen;
        
        public ChunkProviderModded(final World worldIn, final long seed) {
            this.depthbuff = new double[256];
            worldIn.setSeaLevel(63);
            this.world = worldIn;
            this.random = new Random(seed);
            this.perlinnoise1 = new NoiseGeneratorOctaves(this.random, 16);
            this.perlinnoise2 = new NoiseGeneratorOctaves(this.random, 16);
            this.perlinnoise3 = new NoiseGeneratorOctaves(this.random, 8);
            this.height = new NoiseGeneratorPerlin(this.random, 4);
            this.islandNoise = new NoiseGeneratorSimplex(this.random);
            this.islandGen = new WorldGenerator() {
                public boolean generate(final World worldIn, final Random rand, final BlockPos position) {
                    float f = (float)(rand.nextInt(4) + 5);
                    for (int i = 0; f > 1.5f; f -= (float)(rand.nextInt(2) + 0.5), --i) {
                        for (int j = MathHelper.floor(-f); j <= MathHelper.ceil(f); ++j) {
                            for (int k = MathHelper.floor(-f); k <= MathHelper.ceil(f); ++k) {
                                if (j * j + k * k <= (f + 1.0f) * (f + 1.0f)) {
                                    this.setBlockAndNotifyAdequately(worldIn, position.add(j, i, k), ChunkProviderModded.STONE);
                                }
                            }
                        }
                    }
                    return true;
                }
            };
        }
        
        public Chunk generateChunk(final int x, final int z) {
            this.random.setSeed(x * 535358712L + z * 347539041L);
            final ChunkPrimer chunkprimer = new ChunkPrimer();
            this.setBlocksInChunk(x, z, chunkprimer);
            this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, x * 16, z * 16, 16, 16));
            final Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
            final byte[] abyte = chunk.getBiomeArray();
            for (int i = 0; i < abyte.length; ++i) {
                abyte[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
            }
            chunk.generateSkylightMap();
            return chunk;
        }
        
        public void populate(final int x, final int z) {
            ForgeEventFactory.onChunkPopulate(BlockFalling.fallInstantly = true, (IChunkGenerator)this, this.world, this.random, x, z, false);
            final int i = x * 16;
            final int j = z * 16;
            final BlockPos blockpos = new BlockPos(i, 0, j);
            final float f = this.getIslandHeightValue(x, z, 1, 1);
            if (f < -10.0f && this.random.nextInt(6) == 0) {
                this.islandGen.generate(this.world, this.random, blockpos.add(this.random.nextInt(16) + 8, 55 + this.random.nextInt(16), this.random.nextInt(16) + 8));
                if (this.random.nextInt(4) == 0) {
                    this.islandGen.generate(this.world, this.random, blockpos.add(this.random.nextInt(16) + 8, 55 + this.random.nextInt(16), this.random.nextInt(16) + 8));
                }
            }
            final Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
            MinecraftForge.EVENT_BUS.post((Event)new DecorateBiomeEvent.Pre(this.world, this.random, blockpos));
            biome.decorate(this.world, this.random, new BlockPos(i, 0, j));
            MinecraftForge.EVENT_BUS.post((Event)new DecorateBiomeEvent.Post(this.world, this.random, blockpos));
            if (TerrainGen.populate((IChunkGenerator)this, this.world, this.random, x, z, false, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
                WorldEntitySpawner.performWorldGenSpawning(this.world, biome, i + 8, j + 8, 16, 16, this.random);
            }
            ForgeEventFactory.onChunkPopulate(false, (IChunkGenerator)this, this.world, this.random, x, z, false);
            BlockFalling.fallInstantly = false;
        }
        
        public List<Biome.SpawnListEntry> getPossibleCreatures(final EnumCreatureType creatureType, final BlockPos pos) {
            return this.world.getBiome(pos).getSpawnableList(creatureType);
        }
        
        public void recreateStructures(final Chunk chunkIn, final int x, final int z) {
        }
        
        public boolean isInsideStructure(final World worldIn, final String structureName, final BlockPos pos) {
            return false;
        }
        
        public BlockPos getNearestStructurePos(final World worldIn, final String structureName, final BlockPos position, final boolean findUnexplored) {
            return null;
        }
        
        public boolean generateStructures(final Chunk chunkIn, final int x, final int z) {
            return false;
        }
        
        private double[] getHeights(double[] noiseField, final int x, final int y, final int z, final int sizeX, final int sizeY, final int sizeZ) {
            final ChunkGeneratorEvent.InitNoiseField event = new ChunkGeneratorEvent.InitNoiseField((IChunkGenerator)this, noiseField, x, y, z, sizeX, sizeY, sizeZ);
            MinecraftForge.EVENT_BUS.post((Event)event);
            if (event.getResult() == Event.Result.DENY) {
                return event.getNoisefield();
            }
            if (noiseField == null) {
                noiseField = new double[sizeX * sizeY * sizeZ];
            }
            double d0 = 684.412;
            final double d2 = 684.412;
            d0 *= 2.0;
            this.pnr = this.perlinnoise3.generateNoiseOctaves(this.pnr, x, y, z, sizeX, sizeY, sizeZ, d0 / 80.0, 4.277575000000001, d0 / 80.0);
            this.ar = this.perlinnoise1.generateNoiseOctaves(this.ar, x, y, z, sizeX, sizeY, sizeZ, d0, 684.412, d0);
            this.br = this.perlinnoise2.generateNoiseOctaves(this.br, x, y, z, sizeX, sizeY, sizeZ, d0, 684.412, d0);
            final int i = x / 2;
            final int j = z / 2;
            int k = 0;
            for (int l = 0; l < sizeX; ++l) {
                for (int i2 = 0; i2 < sizeZ; ++i2) {
                    final float f = this.getIslandHeightValue(i, j, l, i2);
                    for (int j2 = 0; j2 < sizeY; ++j2) {
                        final double d3 = this.ar[k] / 512.0;
                        final double d4 = this.br[k] / 512.0;
                        final double d5 = (this.pnr[k] / 10.0 + 1.0) / 2.0;
                        double d6;
                        if (d5 < 0.0) {
                            d6 = d3;
                        }
                        else if (d5 > 1.0) {
                            d6 = d4;
                        }
                        else {
                            d6 = d3 + (d4 - d3) * d5;
                        }
                        d6 -= 8.0;
                        d6 += f;
                        int k2 = 2;
                        if (j2 > sizeY / 2 - k2) {
                            double d7 = (j2 - (sizeY / 2 - k2)) / 64.0f;
                            d7 = MathHelper.clamp(d7, 0.0, 1.0);
                            d6 = d6 * (1.0 - d7) + -3000.0 * d7;
                        }
                        k2 = 8;
                        if (j2 < k2) {
                            final double d8 = (k2 - j2) / (k2 - 1.0f);
                            d6 = d6 * (1.0 - d8) + -30.0 * d8;
                        }
                        noiseField[k] = d6;
                        ++k;
                    }
                }
            }
            return noiseField;
        }
        
        private float getIslandHeightValue(final int chunkX, final int chunkZ, final int xOffset, final int zOffset) {
            float f = (float)(chunkX * 2 + xOffset);
            float f2 = (float)(chunkZ * 2 + zOffset);
            float f3 = 100.0f - MathHelper.sqrt(f * f + f2 * f2) * 8.0f;
            if (f3 > 80.0f) {
                f3 = 80.0f;
            }
            if (f3 < -100.0f) {
                f3 = -100.0f;
            }
            for (int i = -12; i <= 12; ++i) {
                for (int j = -12; j <= 12; ++j) {
                    final long k = chunkX + i;
                    final long l = chunkZ + j;
                    if (k * k + l * l > 4096L && this.islandNoise.getValue((double)k, (double)l) < -0.8999999761581421) {
                        final float f4 = (MathHelper.abs((float)k) * 3439.0f + MathHelper.abs((float)l) * 147.0f) % 13.0f + 9.0f;
                        f = (float)(xOffset - i * 2);
                        f2 = (float)(zOffset - j * 2);
                        float f5 = 100.0f - MathHelper.sqrt(f * f + f2 * f2) * f4;
                        if (f5 > 80.0f) {
                            f5 = 80.0f;
                        }
                        if (f5 < -100.0f) {
                            f5 = -100.0f;
                        }
                        if (f5 > f3) {
                            f3 = f5;
                        }
                    }
                }
            }
            return f3;
        }
        
        public void setBlocksInChunk(final int x, final int z, final ChunkPrimer primer) {
            final int i = 2;
            final int j = 3;
            final int k = 33;
            final int l = 3;
            this.buffer = this.getHeights(this.buffer, x * 2, 0, z * 2, 3, 33, 3);
            for (int i2 = 0; i2 < 2; ++i2) {
                for (int j2 = 0; j2 < 2; ++j2) {
                    for (int k2 = 0; k2 < 32; ++k2) {
                        final double d0 = 0.25;
                        double d2 = this.buffer[((i2 + 0) * 3 + j2 + 0) * 33 + k2 + 0];
                        double d3 = this.buffer[((i2 + 0) * 3 + j2 + 1) * 33 + k2 + 0];
                        double d4 = this.buffer[((i2 + 1) * 3 + j2 + 0) * 33 + k2 + 0];
                        double d5 = this.buffer[((i2 + 1) * 3 + j2 + 1) * 33 + k2 + 0];
                        final double d6 = (this.buffer[((i2 + 0) * 3 + j2 + 0) * 33 + k2 + 1] - d2) * 0.25;
                        final double d7 = (this.buffer[((i2 + 0) * 3 + j2 + 1) * 33 + k2 + 1] - d3) * 0.25;
                        final double d8 = (this.buffer[((i2 + 1) * 3 + j2 + 0) * 33 + k2 + 1] - d4) * 0.25;
                        final double d9 = (this.buffer[((i2 + 1) * 3 + j2 + 1) * 33 + k2 + 1] - d5) * 0.25;
                        for (int l2 = 0; l2 < 4; ++l2) {
                            final double d10 = 0.125;
                            double d11 = d2;
                            double d12 = d3;
                            final double d13 = (d4 - d2) * 0.125;
                            final double d14 = (d5 - d3) * 0.125;
                            for (int i3 = 0; i3 < 8; ++i3) {
                                final double d15 = 0.125;
                                double d16 = d11;
                                final double d17 = (d12 - d11) * 0.125;
                                for (int j3 = 0; j3 < 8; ++j3) {
                                    IBlockState iblockstate = ChunkProviderModded.AIR;
                                    if (d16 > 0.0) {
                                        iblockstate = ChunkProviderModded.STONE;
                                    }
                                    final int k3 = i3 + i2 * 8;
                                    final int l3 = l2 + k2 * 4;
                                    final int i4 = j3 + j2 * 8;
                                    primer.setBlockState(k3, l3, i4, iblockstate);
                                    d16 += d17;
                                }
                                d11 += d13;
                                d12 += d14;
                            }
                            d2 += d6;
                            d3 += d7;
                            d4 += d8;
                            d5 += d9;
                        }
                    }
                }
            }
        }
        
        private void replaceBiomeBlocks(final int x, final int z, final ChunkPrimer primer, final Biome[] biomesIn) {
            this.depthbuff = this.height.getRegion(this.depthbuff, (double)(x * 16), (double)(z * 16), 16, 16, 0.0625, 0.0625, 1.0);
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    this.generateBiomeTerrain(this.world, this.random, primer, x * 16 + i, z * 16 + j, this.depthbuff[j + i * 16], biomesIn[j + i * 16]);
                }
            }
        }
        
        private void generateBiomeTerrain(final World worldIn, final Random rand, final ChunkPrimer chunkPrimerIn, final int x, final int z, final double noiseVal, final Biome biome) {
            final int i = 63;
            IBlockState iblockstate = biome.topBlock;
            IBlockState iblockstate2 = biome.fillerBlock;
            int j = -1;
            final int k = (int)(noiseVal / 3.0 + 3.0 + rand.nextDouble() / 4.0);
            final int l = x & 0xF;
            final int i2 = z & 0xF;
            for (int j2 = 255; j2 >= 0; --j2) {
                final IBlockState iblockstate3 = chunkPrimerIn.getBlockState(i2, j2, l);
                if (iblockstate3.getMaterial() == Material.AIR) {
                    j = -1;
                }
                else if (iblockstate3.getBlock() == ChunkProviderModded.STONE.getBlock()) {
                    if (j == -1) {
                        if (k <= 0) {
                            iblockstate = ChunkProviderModded.AIR;
                            iblockstate2 = ChunkProviderModded.STONE;
                        }
                        j = k;
                        if (j2 >= i - 1) {
                            chunkPrimerIn.setBlockState(i2, j2, l, iblockstate);
                        }
                        else if (j2 < i - 7 - k) {
                            iblockstate2 = ChunkProviderModded.STONE;
                        }
                        else {
                            chunkPrimerIn.setBlockState(i2, j2, l, iblockstate2);
                        }
                    }
                    else if (j > 0) {
                        --j;
                        chunkPrimerIn.setBlockState(i2, j2, l, iblockstate2);
                    }
                }
            }
        }
        
        static {
            STONE = Blocks.BEDROCK.getDefaultState();
            AIR = Blocks.AIR.getDefaultState();
        }
    }

  public static class GenLayerBiomesCustom extends GenLayer
    {
        private Biome[] allowedBiomes;
        
        public GenLayerBiomesCustom(final long seed) {
            super(seed);
            this.allowedBiomes = new Biome[] { BiomeVoidPlain.biome, BiomeVoidHill.biome };
        }
        
        public int[] getInts(final int x, final int z, final int width, final int depth) {
            final int[] dest = IntCache.getIntCache(width * depth);
            for (int dz = 0; dz < depth; ++dz) {
                for (int dx = 0; dx < width; ++dx) {
                    this.initChunkSeed((long)(dx + x), (long)(dz + z));
                    dest[dx + dz * width] = Biome.getIdForBiome(this.allowedBiomes[this.nextInt(this.allowedBiomes.length)]);
                }
            }
            return dest;
        }
    }

  public static class BiomeProviderCustom extends BiomeProvider
    {
        private GenLayer genBiomes;
        private GenLayer biomeIndexLayer;
        private BiomeCache biomeCache;
        
        public BiomeProviderCustom() {
            this.biomeCache = new BiomeCache((BiomeProvider)this);
        }
        
        public BiomeProviderCustom(final long seed) {
            this.biomeCache = new BiomeCache((BiomeProvider)this);
            final GenLayer[] agenlayer = this.makeTheWorld(seed);
            this.genBiomes = agenlayer[0];
            this.biomeIndexLayer = agenlayer[1];
        }
        
        private GenLayer[] makeTheWorld(final long seed) {
            GenLayer biomes = new GenLayerBiomesCustom(1L);
            biomes = (GenLayer)new GenLayerZoom(1000L, biomes);
            biomes = (GenLayer)new GenLayerZoom(1001L, biomes);
            biomes = (GenLayer)new GenLayerZoom(1002L, biomes);
            biomes = (GenLayer)new GenLayerZoom(1003L, biomes);
            biomes = (GenLayer)new GenLayerZoom(1004L, biomes);
            biomes = (GenLayer)new GenLayerZoom(1005L, biomes);
            final GenLayer genlayervoronoizoom = (GenLayer)new GenLayerVoronoiZoom(10L, biomes);
            biomes.initWorldGenSeed(seed);
            genlayervoronoizoom.initWorldGenSeed(seed);
            return new GenLayer[] { biomes, genlayervoronoizoom };
        }
        
        public BiomeProviderCustom(final World world) {
            this(world.getSeed());
        }
        
        public void cleanupCache() {
            this.biomeCache.cleanupCache();
        }
        
        public Biome getBiome(final BlockPos pos) {
            return this.getBiome(pos, null);
        }
        
        public Biome getBiome(final BlockPos pos, final Biome defaultBiome) {
            return this.biomeCache.getBiome(pos.getX(), pos.getZ(), defaultBiome);
        }
        
        public Biome[] getBiomes(final Biome[] oldBiomeList, final int x, final int z, final int width, final int depth) {
            return this.getBiomes(oldBiomeList, x, z, width, depth, true);
        }
        
        public Biome[] getBiomesForGeneration(Biome[] biomes, final int x, final int z, final int width, final int height) {
            IntCache.resetIntCache();
            if (biomes == null || biomes.length < width * height) {
                biomes = new Biome[width * height];
            }
            final int[] aint = this.genBiomes.getInts(x, z, width, height);
            try {
                for (int i = 0; i < width * height; ++i) {
                    biomes[i] = Biome.getBiome(aint[i], Biomes.DEFAULT);
                }
                return biomes;
            }
            catch (final Throwable throwable) {
                final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
                final CrashReportCategory crashreportcategory = crashreport.makeCategory("RawBiomeBlock");
                crashreportcategory.addCrashSection("biomes[] size", biomes.length);
                crashreportcategory.addCrashSection("x", (Object)x);
                crashreportcategory.addCrashSection("z", (Object)z);
                crashreportcategory.addCrashSection("w", (Object)width);
                crashreportcategory.addCrashSection("h", (Object)height);
                throw new ReportedException(crashreport);
            }
        }
        
        public Biome[] getBiomes(@Nullable Biome[] listToReuse, final int x, final int z, final int width, final int length, final boolean cacheFlag) {
            IntCache.resetIntCache();
            if (listToReuse == null || listToReuse.length < width * length) {
                listToReuse = new Biome[width * length];
            }
            if (cacheFlag && width == 16 && length == 16 && (x & 0xF) == 0x0 && (z & 0xF) == 0x0) {
                final Biome[] abiome = this.biomeCache.getCachedBiomes(x, z);
                System.arraycopy(abiome, 0, listToReuse, 0, width * length);
                return listToReuse;
            }
            final int[] aint = this.biomeIndexLayer.getInts(x, z, width, length);
            for (int i = 0; i < width * length; ++i) {
                listToReuse[i] = Biome.getBiome(aint[i], Biomes.DEFAULT);
            }
            return listToReuse;
        }
        
        public boolean areBiomesViable(final int x, final int z, final int radius, final List<Biome> allowed) {
            IntCache.resetIntCache();
            final int i = x - radius >> 2;
            final int j = z - radius >> 2;
            final int k = x + radius >> 2;
            final int l = z + radius >> 2;
            final int i2 = k - i + 1;
            final int j2 = l - j + 1;
            final int[] aint = this.genBiomes.getInts(i, j, i2, j2);
            try {
                for (int k2 = 0; k2 < i2 * j2; ++k2) {
                    final Biome biome = Biome.getBiome(aint[k2]);
                    if (!allowed.contains(biome)) {
                        return false;
                    }
                }
                return true;
            }
            catch (final Throwable throwable) {
                final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
                final CrashReportCategory crashreportcategory = crashreport.makeCategory("Layer");
                crashreportcategory.addCrashSection("Layer", (Object)this.genBiomes.toString());
                crashreportcategory.addCrashSection("x", (Object)x);
                crashreportcategory.addCrashSection("z", (Object)z);
                crashreportcategory.addCrashSection("radius", (Object)radius);
                crashreportcategory.addCrashSection("allowed", (Object)allowed);
                throw new ReportedException(crashreport);
            }
        }
        
        @Nullable
        public BlockPos findBiomePosition(final int x, final int z, final int range, final List<Biome> biomes, final Random random) {
            IntCache.resetIntCache();
            final int i = x - range >> 2;
            final int j = z - range >> 2;
            final int k = x + range >> 2;
            final int l = z + range >> 2;
            final int i2 = k - i + 1;
            final int j2 = l - j + 1;
            final int[] aint = this.genBiomes.getInts(i, j, i2, j2);
            BlockPos blockpos = null;
            int k2 = 0;
            for (int l2 = 0; l2 < i2 * j2; ++l2) {
                final int i3 = i + l2 % i2 << 2;
                final int j3 = j + l2 / i2 << 2;
                final Biome biome = Biome.getBiome(aint[l2]);
                if (biomes.contains(biome) && (blockpos == null || random.nextInt(k2 + 1) == 0)) {
                    blockpos = new BlockPos(i3, 0, j3);
                    ++k2;
                }
            }
            return blockpos;
        }
    }
}

