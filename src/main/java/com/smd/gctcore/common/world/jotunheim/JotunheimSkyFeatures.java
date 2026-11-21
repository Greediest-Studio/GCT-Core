package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.misc.JotunheimBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Seeded, connected cloud banks confined to a new chunk primer. */
public final class JotunheimSkyFeatures {
    private static final int CLOUD_CELL = 48;
    /** Hard footprint bound, including rotated lobes and domain warping. */
    private static final int CLOUD_RADIUS = 30;
    private static final int CLOUD_WIDTH = CLOUD_RADIUS * 2 + 1;
    private final long seed;
    private final JotunheimTerrainKernel kernel;
    private final IBlockState[] clouds = new IBlockState[7];
    private final Map<Long, List<CloudBank>> banks = new LinkedHashMap<Long, List<CloudBank>>(128, 0.75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, List<CloudBank>> entry) { return size() > 128; }
    };

    public JotunheimSkyFeatures(long seed, JotunheimTerrainKernel kernel) {
        this.seed = seed;
        this.kernel = kernel;
        // Resolve only after block registration, without referring to Aether classes or enums.
        Block aercloud = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("aether_legacy", "aercloud"));
        if (aercloud != null && aercloud != Blocks.AIR) {
            for (int meta = 0; meta < 4; meta++) clouds[meta] = aercloud.getStateFromMeta(meta);
        }
        clouds[4] = JotunheimBlocks.state("frostbound_aercloud");
        clouds[5] = JotunheimBlocks.state("ashen_aercloud");
        clouds[6] = JotunheimBlocks.state("thundering_aercloud");
    }

    /** Bounded caches are guarded when a pack invokes generation from multiple threads. */
    public synchronized void decorate(ChunkPrimer primer, int chunkX, int chunkZ) {
        int x0 = chunkX * 16, z0 = chunkZ * 16;
        int minX = Math.floorDiv(x0-CLOUD_RADIUS, CLOUD_CELL);
        int maxX = Math.floorDiv(x0+15+CLOUD_RADIUS, CLOUD_CELL);
        int minZ = Math.floorDiv(z0-CLOUD_RADIUS, CLOUD_CELL);
        int maxZ = Math.floorDiv(z0+15+CLOUD_RADIUS, CLOUD_CELL);
        for (int cx = minX; cx <= maxX; cx++) for (int cz = minZ; cz <= maxZ; cz++) {
            for (CloudBank bank : banks(cx, cz)) placeBank(primer, x0, z0, bank);
        }
    }

    /**
     * The vanilla initial skylight map does not initialize BLOCK light. Seed and
     * spread emitters locally before the new chunk is sent to clients; later vanilla
     * neighbour-light updates join chunk edges. No World or neighbour chunk is read.
     * Propagation includes empty sections, but full chunk packets omit their light;
     * only write existing nonempty sections to avoid allocating them and rebuilding skylight.
     */
    public static void initializeBlockLight(Chunk chunk) {
        byte[] opacity = new byte[16*16*256];
        byte[] levels = new byte[opacity.length];
        @SuppressWarnings("unchecked")
        ArrayDeque<Integer>[] buckets = (ArrayDeque<Integer>[]) new ArrayDeque<?>[16];
        for (int level=0; level<16; level++) buckets[level] = new ArrayDeque<>();
        for (int x=0; x<16; x++) for (int z=0; z<16; z++) for (int y=0; y<256; y++) {
            int index=(x*16+z)*256+y;
            IBlockState state=chunk.getBlockState(x,y,z);
            opacity[index]=(byte)Math.max(1,Math.min(15,state.getLightOpacity()));
            int emitted=Math.max(0,Math.min(15,state.getLightValue()));
            if (emitted>0) { levels[index]=(byte)emitted; buckets[emitted].add(index); }
        }
        for (int level=15; level>1; level--) {
            while (!buckets[level].isEmpty()) {
                int index=buckets[level].removeFirst();
                if (levels[index]!=level) continue;
                int y=index&255, z=(index>>>8)&15, x=index>>>12;
                if (x>0) spreadLight(index-4096,level,opacity,levels,buckets);
                if (x<15) spreadLight(index+4096,level,opacity,levels,buckets);
                if (z>0) spreadLight(index-256,level,opacity,levels,buckets);
                if (z<15) spreadLight(index+256,level,opacity,levels,buckets);
                if (y>0) spreadLight(index-1,level,opacity,levels,buckets);
                if (y<255) spreadLight(index+1,level,opacity,levels,buckets);
            }
        }
        ExtendedBlockStorage[] sections=chunk.getBlockStorageArray();
        for (int sectionY=0; sectionY<sections.length; sectionY++) {
            ExtendedBlockStorage section=sections[sectionY];
            if (section==Chunk.NULL_BLOCK_STORAGE || section.isEmpty()) continue;
            int y0=sectionY*16;
            for (int x=0; x<16; x++) for (int z=0; z<16; z++) for (int y=y0; y<y0+16; y++) {
                int index=(x*16+z)*256+y;
                if (levels[index]>0) chunk.setLightFor(EnumSkyBlock.BLOCK,
                        new BlockPos(chunk.x*16+x,y,chunk.z*16+z),levels[index]);
            }
        }
    }

    private static void spreadLight(int index, int source, byte[] opacity, byte[] levels,
                                     ArrayDeque<Integer>[] buckets) {
        int light=source-opacity[index];
        if (light>levels[index]) { levels[index]=(byte)light; buckets[light].add(index); }
    }

    private static final class CloudBank {
        final int x, y, z, shape;
        final long shapeSeed;
        final IBlockState state;
        /** Complete bank plan; zero thickness marks air. All columns share y for connectivity. */
        final byte[] bottom = new byte[CLOUD_WIDTH*CLOUD_WIDTH];
        final byte[] thickness = new byte[bottom.length];
        CloudBank(int x, int y, int z, long shapeSeed, IBlockState state) {
            this.x=x; this.y=y; this.z=z;
            this.shapeSeed=shapeSeed; this.state=state;
            Random random=new Random(shapeSeed);
            shape=random.nextInt(4);
            double angle=random.nextDouble()*Math.PI*2.0D;
            double cosine=Math.cos(angle), sine=Math.sin(angle);
            double radiusX=shape==1 ? 16+random.nextInt(5) : 10+random.nextInt(9);
            double radiusZ=shape==1 ? 7+random.nextInt(4) : 8+random.nextInt(7);
            List<double[]> lobes=new ArrayList<>();
            lobes.add(new double[]{0,0,shape==3?0.74D:0.61D,shape==1?0.46D:0.58D});
            int count=shape==1 ? 5 : 4+random.nextInt(4);
            double phase=random.nextDouble()*Math.PI*2.0D;
            for(int n=0;n<count;n++) {
                double lx,lz,rx,rz;
                if(shape==1) {
                    // Uneven overlapping puffs stretched along the prevailing wind.
                    lx=-0.60D+n*0.28D;
                    lz=(random.nextDouble()-0.5D)*0.26D;
                    rx=0.35D+random.nextDouble()*0.14D;
                    rz=0.43D+random.nextDouble()*0.17D;
                } else if(shape==2) {
                    // An offset arc gives some banks a hooked, asymmetric outline.
                    double theta=phase+n*2.8D/(count-1);
                    lx=Math.cos(theta)*0.54D;
                    lz=Math.sin(theta)*0.48D;
                    rx=0.35D+random.nextDouble()*0.13D;
                    rz=0.32D+random.nextDouble()*0.16D;
                } else {
                    double theta=phase+n*Math.PI*2.0D/count+(random.nextDouble()-0.5D)*0.5D;
                    double distance=0.32D+random.nextDouble()*0.22D;
                    lx=Math.cos(theta)*distance;
                    lz=Math.sin(theta)*distance*(shape==3?0.68D:1.0D);
                    rx=0.35D+random.nextDouble()*0.20D;
                    rz=0.31D+random.nextDouble()*0.19D;
                }
                lobes.add(new double[]{lx,lz,rx,rz});
            }
            int topDepth=2+random.nextInt(3), bottomDepth=2+random.nextInt(2);
            for(int dx=-CLOUD_RADIUS;dx<=CLOUD_RADIUS;dx++) for(int dz=-CLOUD_RADIUS;dz<=CLOUD_RADIUS;dz++) {
                int index=(dx+CLOUD_RADIUS)*CLOUD_WIDTH+dz+CLOUD_RADIUS;
                // World-space noise is evaluated before chunk clipping, including negative coordinates.
                double wx=x+dx, wz=z+dz;
                double warpedX=dx+noise(shapeSeed+11,wx*0.085D,wz*0.085D)*1.8D;
                double warpedZ=dz+noise(shapeSeed+29,wx*0.085D,wz*0.085D)*1.8D;
                double u=(warpedX*cosine+warpedZ*sine)/radiusX;
                double v=(-warpedX*sine+warpedZ*cosine)/radiusZ;
                double q=-Double.MAX_VALUE;
                for(double[] lobe:lobes) {
                    double lu=(u-lobe[0])/lobe[2],lv=(v-lobe[1])/lobe[3];
                    q=Math.max(q,1.0D-lu*lu-lv*lv);
                }
                q+=noise(shapeSeed+47,wx*0.11D,wz*0.11D)*0.17D
                        +noise(shapeSeed+71,wx*0.24D,wz*0.24D)*0.055D;
                if(q<=0.055D) continue;
                double lens=Math.sqrt(Math.max(0.0D,Math.min(1.0D,q)));
                int top=Math.max(0,(int)Math.floor(lens*topDepth
                        +noise(shapeSeed+97,wx*0.13D,wz*0.13D)*0.7D-0.25D));
                int below=Math.max(0,(int)Math.floor(lens*bottomDepth
                        +noise(shapeSeed+113,wx*0.16D,wz*0.16D)*0.65D-0.35D));
                bottom[index]=(byte)-below;
                thickness[index]=(byte)(1+top+below);
            }
            // Retain the central connected body only. Noise cannot scatter isolated one-block clouds.
            boolean[] connected=new boolean[bottom.length];
            ArrayDeque<Integer> pending=new ArrayDeque<>();
            int centre=CLOUD_RADIUS*CLOUD_WIDTH+CLOUD_RADIUS;
            if(thickness[centre]>0) { connected[centre]=true;pending.add(centre); }
            while(!pending.isEmpty()) {
                int index=pending.removeFirst(),px=index/CLOUD_WIDTH,pz=index%CLOUD_WIDTH;
                if(px>0) connect(index-CLOUD_WIDTH,thickness,connected,pending);
                if(px<CLOUD_WIDTH-1) connect(index+CLOUD_WIDTH,thickness,connected,pending);
                if(pz>0) connect(index-1,thickness,connected,pending);
                if(pz<CLOUD_WIDTH-1) connect(index+1,thickness,connected,pending);
            }
            for(int index=0;index<thickness.length;index++) if(!connected[index]) thickness[index]=0;
        }
    }

    private static void connect(int index,byte[] thickness,boolean[] connected,ArrayDeque<Integer> pending) {
        if(thickness[index]>0&&!connected[index]) { connected[index]=true;pending.add(index); }
    }

    /** Smooth lattice noise with a per-bank seed, rather than repeated trigonometric ripples. */
    private static double noise(long seed,double x,double z) {
        int ix=(int)Math.floor(x),iz=(int)Math.floor(z);
        double tx=x-ix,tz=z-iz;
        tx=tx*tx*tx*(tx*(tx*6.0D-15.0D)+10.0D);
        tz=tz*tz*tz*(tz*(tz*6.0D-15.0D)+10.0D);
        double a=lattice(seed,ix,iz),b=lattice(seed,ix+1,iz);
        double c=lattice(seed,ix,iz+1),d=lattice(seed,ix+1,iz+1);
        return (a+(b-a)*tx)*(1.0D-tz)+(c+(d-c)*tx)*tz;
    }

    private static double lattice(long seed,int x,int z) {
        long value=seed^((long)x*0x632BE59BD9B4E019L)^((long)z*0x9E3779B97F4A7C15L);
        value=(value^(value>>>30))*0xBF58476D1CE4E5B9L;
        value=(value^(value>>>27))*0x94D049BB133111EBL;
        value^=value>>>31;
        return (value>>>11)*0x1.0p-52-1.0D;
    }

    private List<CloudBank> banks(int cx, int cz) {
        long id = key(cx, cz);
        List<CloudBank> result = banks.get(id);
        if (result != null) return result;
        result = new ArrayList<>();
        // Loose banks occupy the space beneath each of the upper three island strata.
        int[] heights = {39, 111, 198};
        double[] chance = {0.34D, 0.55D, 0.86D};
        for (int band = 0; band < heights.length; band++) {
            Random random = random(cx, cz, 7101L+band*131L);
            if (random.nextDouble() > chance[band]) continue;
            int x = cx*CLOUD_CELL+7+random.nextInt(CLOUD_CELL-14);
            int z = cz*CLOUD_CELL+7+random.nextInt(CLOUD_CELL-14);
            int biome = kernel.biomeIndex(x, z);
            int variant = cloudVariant(biome, random);
            IBlockState cloud = clouds[variant];
            // Aether is optional in a development environment; retain special biome banks.
            if (cloud == null) continue;
            int y = heights[band]+random.nextInt(band == 2 ? 13 : 7)-(band == 2 ? 6 : 3);
            result.add(new CloudBank(x, y, z, random.nextLong(), cloud));
        }
        banks.put(id, result);
        return result;
    }

    private static int cloudVariant(int biome, Random random) {
        double roll = random.nextDouble();
        if (biome == 1 && roll < 0.70D) return 4;
        if (biome == 5 && roll < 0.70D) return 6;
        if (biome == 6 && roll < 0.70D) return 5;
        if (biome == 4) return roll < 0.38D ? 2 : roll < 0.76D ? 3 : 1;
        if (biome == 3) return roll < 0.45D ? 1 : roll < 0.70D ? 3 : 0;
        if (biome == 7) return roll < 0.36D ? 2 : roll < 0.53D ? 3 : 0;
        return roll < 0.58D ? 0 : roll < 0.78D ? 1 : roll < 0.89D ? 2 : 3;
    }

    private void placeBank(ChunkPrimer primer, int x0, int z0, CloudBank bank) {
        int lowX=Math.max(x0,bank.x-CLOUD_RADIUS), highX=Math.min(x0+15,bank.x+CLOUD_RADIUS);
        int lowZ=Math.max(z0,bank.z-CLOUD_RADIUS), highZ=Math.min(z0+15,bank.z+CLOUD_RADIUS);
        for (int wx=lowX; wx<=highX; wx++) for (int wz=lowZ; wz<=highZ; wz++) {
            int index=(wx-bank.x+CLOUD_RADIUS)*CLOUD_WIDTH+wz-bank.z+CLOUD_RADIUS;
            int count=bank.thickness[index],bottom=bank.y+bank.bottom[index];
            for (int y=bottom; y<bottom+count; y++) {
                if (y<1 || y>254) continue;
                int x=wx-x0, z=wz-z0;
                // Trees, roots, ground, pools, and earlier banks all keep their existing blocks.
                if (primer.getBlockState(x,y,z).getMaterial()==Material.AIR) primer.setBlockState(x,y,z,bank.state);
            }
        }
    }

    private Random random(int x, int z, long salt) {
        long value=seed^((long)x*0x632BE59BD9B4E019L)^((long)z*0x9E3779B97F4A7C15L)^(salt*0x94D049BB133111EBL);
        value=(value^(value>>>30))*0xBF58476D1CE4E5B9L;
        value=(value^(value>>>27))*0x94D049BB133111EBL;
        return new Random(value^(value>>>31));
    }

    private static long key(int x,int z) { return ((long)x<<32)^(z&0xFFFFFFFFL); }
}
