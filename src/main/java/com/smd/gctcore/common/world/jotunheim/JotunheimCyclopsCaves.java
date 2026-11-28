package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.misc.JotunheimBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.smd.gctcore.common.world.jotunheim.JotunheimTerrainKernel.HeightLayer;

/** Enclosed circular island dungeons with a small, walkable surface entrance. */
public final class JotunheimCyclopsCaves {
    public static final String STRUCTURE_NAME = "CyclopsCave";
    public static final int REGION_CHUNKS = 32;
    private static final int REGION_BLOCKS = REGION_CHUNKS * 16;
    private static final int CANDIDATES = 32;
    private static final int[] DX = {0, 1, 0, -1}, DZ = {-1, 0, 1, 0};
    private final long seed;
    private final JotunheimTerrainKernel kernel;
    private final JotunheimYmirWells wells;
    private final JotunheimLakes lakes;
    private final Map<Long, List<Plan>> plans = cache(128);
    private final Map<Long, JotunheimTerrainKernel.SampledChunk> terrain = cache(64);

    public JotunheimCyclopsCaves(long seed, JotunheimTerrainKernel kernel) {
        this(seed, kernel, null, null);
    }

    public JotunheimCyclopsCaves(long seed, JotunheimTerrainKernel kernel,
                                JotunheimYmirWells wells, JotunheimLakes lakes) {
        this.seed = seed; this.kernel = kernel; this.wells = wells; this.lakes = lakes;
    }

    private static <K,V> Map<K,V> cache(final int limit) {
        return new LinkedHashMap<K,V>(limit, .75F, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<K,V> entry) { return size() > limit; }
        };
    }

    public static boolean acceptsName(String name) {
        return STRUCTURE_NAME.equalsIgnoreCase(name) || "cyclops_cave".equalsIgnoreCase(name)
                || "gctcore:cyclops_cave".equalsIgnoreCase(name);
    }

    public static final class Plan {
        public final int x, z, floorY, surfaceY, radius, chamberHeight, direction, entranceRadius;
        public final int minX, maxX, minY, maxY, minZ, maxZ;
        public final HeightLayer layer;
        private final List<BlockPos> guardians;
        private final IBlockState rock, surface;
        private final Map<Long, int[]> mound;

        private Plan(int x, int z, int floorY, int surfaceY, int radius, int height,
                     int direction, int entranceRadius, HeightLayer layer, List<BlockPos> guardians,
                     IBlockState rock, IBlockState surface, Map<Long, int[]> mound) {
            this.x=x; this.z=z; this.floorY=floorY; this.surfaceY=surfaceY; this.radius=radius;
            this.chamberHeight=height; this.direction=direction; this.entranceRadius=entranceRadius;
            this.layer=layer; this.guardians=Collections.unmodifiableList(new ArrayList<>(guardians));
            this.rock=rock; this.surface=surface; this.mound=Collections.unmodifiableMap(mound);
            int ex=x+DX[direction]*entranceRadius, ez=z+DZ[direction]*entranceRadius;
            int loX=Math.min(x-radius-2,ex-9), hiX=Math.max(x+radius+2,ex+9);
            int loZ=Math.min(z-radius-2,ez-9), hiZ=Math.max(z+radius+2,ez+9);
            int high=Math.max(floorY+height+3,surfaceY+4);
            for(BlockPos guardian:guardians) {
                loX=Math.min(loX,guardian.getX()-1); hiX=Math.max(hiX,guardian.getX()+1);
                loZ=Math.min(loZ,guardian.getZ()-1); hiZ=Math.max(hiZ,guardian.getZ()+1);
                high=Math.max(high,guardian.getY()+8);
            }
            minX=loX; maxX=hiX; minZ=loZ; maxZ=hiZ; minY=floorY-2; maxY=high;
        }

        public BlockPos centerFeet() { return new BlockPos(x,floorY+1,z); }
        public BlockPos center() { return centerFeet(); }
        public BlockPos entrance() { return new BlockPos(x+DX[direction]*entranceRadius,surfaceY+1,z+DZ[direction]*entranceRadius); }
        public List<BlockPos> guardianFeet() { return guardians; }
        /** Last air block in the domed chamber; the broad middle has room for its resident. */
        public int ceilingAt(int worldX,int worldZ) {
            double distance=Math.hypot(worldX-x,worldZ-z)/(double)radius;
            return floorY+Math.max(4,(int)Math.floor(chamberHeight*Math.sqrt(Math.max(0,1-distance*distance))));
        }
        public boolean contains(int wx,int y,int wz) {
            return wx>=minX&&wx<=maxX&&wz>=minZ&&wz<=maxZ&&y>=minY&&y<=maxY;
        }
        public boolean intersectsChunk(int cx,int cz) {
            return maxX>=cx*16&&minX<=cx*16+15&&maxZ>=cz*16&&minZ<=cz*16+15;
        }
        int pathFloor(int r) {
            int inner=radius-1;
            return floorY+Math.floorDiv((r-inner)*(surfaceY-floorY),entranceRadius-inner);
        }
    }

    @Nullable
    public synchronized Plan planForRegion(int regionX,int regionZ) {
        long id=key(regionX,regionZ); List<Plan> cached=plans.get(id);
        if(cached!=null)return cached.isEmpty()?null:cached.get(0);
        Random random=new Random(mix(seed^id^0x4359434C4F50534CL));
        Plan selected=null;
        int target=random.nextInt(4);
        for(int attempt=0;attempt<CANDIDATES&&selected==null;attempt++) {
            int x=regionX*REGION_BLOCKS+64+random.nextInt(REGION_BLOCKS-128);
            int z=regionZ*REGION_BLOCKS+64+random.nextInt(REGION_BLOCKS-128);
            // Give the selected stratum several sites before trying another stratum.
            for(int offset=0;offset<(attempt<12?1:4)&&selected==null;offset++) {
                HeightLayer layer=HeightLayer.values()[(target+offset)%4];
                selected=planAt(x,z,layer,random);
            }
        }
        plans.put(id,selected==null?Collections.emptyList():Collections.singletonList(selected));
        return selected;
    }

    public synchronized List<Plan> plansForChunk(int chunkX,int chunkZ) {
        List<Plan> result=new ArrayList<>();
        // Complete footprints fit inside their owning region, including guardian clearings.
        Plan plan=planForRegion(Math.floorDiv(chunkX,REGION_CHUNKS),Math.floorDiv(chunkZ,REGION_CHUNKS));
        if(plan!=null&&plan.intersectsChunk(chunkX,chunkZ))result.add(plan);
        return result;
    }

    /** Reserves the carved chamber, stepped passage and surface mound for decoration planning. */
    public synchronized boolean blocksVegetation(int x,int y,int z) {
        Plan plan=planForRegion(Math.floorDiv(x,REGION_BLOCKS),Math.floorDiv(z,REGION_BLOCKS));
        return plan!=null&&blockAt(plan,x,y,z)!=null;
    }

    @Nullable
    Plan planAt(int x,int z,HeightLayer layer,Random random) {
        int radius=18+random.nextInt(4), height=layer==HeightLayer.LAVA?6:10;
        int centerTop=top(x,z,layer);
        if(centerTop<layer.minY+height+6)return null;
        int minTop=centerTop,maxTop=centerTop;
        // Cheap outer-circle probes reject exposed, steep, or overly thin island sites.
        for(int i=0;i<12;i++) {
            double angle=i*Math.PI/6;
            int top=top(x+(int)Math.round(Math.cos(angle)*(radius+2)),z+(int)Math.round(Math.sin(angle)*(radius+2)),layer);
            if(top<layer.minY+height+6)return null;
            minTop=Math.min(minTop,top);maxTop=Math.max(maxTop,top);
        }
        if(maxTop-minTop>9)return null;
        int outer=radius+2;
        for(int dz=-outer;dz<=outer;dz++)for(int dx=-outer;dx<=outer;dx++) {
            if(dx*dx+dz*dz>outer*outer)continue;
            int top=top(x+dx,z+dz,layer);
            if(top<layer.minY+height+6)return null;
            minTop=Math.min(minTop,top);maxTop=Math.max(maxTop,top);
            if(maxTop-minTop>9)return null;
        }
        int floor=minTop-height-3;
        if(floor-2<layer.minY)return null;
        // Exactly two intact layers beneath the floor and over the roof; complete ring is also intact.
        for(int dz=-outer;dz<=outer;dz++)for(int dx=-outer;dx<=outer;dx++) {
            int d2=dx*dx+dz*dz;if(d2>outer*outer)continue;
            int ceiling=floor+Math.max(4,(int)Math.floor(height*Math.sqrt(Math.max(0,1-d2/(double)(radius*radius)))));
            if(!solid(x+dx,floor-2,z+dz)||!solid(x+dx,floor-1,z+dz))return null;
            if(!solid(x+dx,ceiling+1,z+dz)||!solid(x+dx,ceiling+2,z+dz))return null;
            if(d2>=(radius-1)*(radius-1))for(int y=floor;y<=ceiling+2;y++)if(!solid(x+dx,y,z+dz))return null;
            if(lakes!=null&&lakes.blocksVegetation(x+dx,top(x+dx,z+dz,layer)+1,z+dz))return null;
        }
        int first=random.nextInt(4);
        for(int turn=0;turn<4;turn++) {
            int direction=(first+turn)%4;
            // Full-block steps need a run at least as long as the maximum allowed rise.
            int entranceRadius=radius+22;
            int ex=x+DX[direction]*entranceRadius,ez=z+DZ[direction]*entranceRadius;
            int surface=top(ex,ez,layer);
            if(surface<floor+7||surface-floor>20||surface+10>layer.maxY+10)continue;
            boolean good=true;
            int inner=radius-1;
            for(int r=inner;r<=entranceRadius&&good;r++)for(int w=-2;w<=2;w++) {
                int wx=x+DX[direction]*r-DZ[direction]*w,wz=z+DZ[direction]*r+DX[direction]*w;
                int pathFloor=floor+Math.floorDiv((r-inner)*(surface-floor),entranceRadius-inner);
                int ground=top(wx,wz,layer);
                if(ground<pathFloor||!solid(wx,pathFloor-1,wz)||!solid(wx,pathFloor-2,wz)
                        ||r<entranceRadius-5&&ground<pathFloor+4
                        ||lakes!=null&&lakes.blocksVegetation(wx,ground+1,wz)) {good=false;break;}
            }
            if(!good)continue;
            // The tunnel mouth joins actual open land, rather than ending against a cliff.
            for(int r=entranceRadius+1;r<=entranceRadius+3&&good;r++)for(int w=-1;w<=1;w++) {
                int wx=x+DX[direction]*r-DZ[direction]*w,wz=z+DZ[direction]*r+DX[direction]*w;
                int ground=top(wx,wz,layer);
                if(Math.abs(ground-surface)>1) {good=false;break;}
                for(int y=ground+1;y<=ground+3;y++)if(solid(wx,y,wz)){good=false;break;}
            }
            if(!good)continue;
            List<BlockPos> guardians=new ArrayList<>();
            for(int side:new int[]{-1,1}) {
                int gx=ex-DZ[direction]*side*6,gz=ez+DX[direction]*side*6;
                int ground=top(gx,gz,layer);
                if(ground<layer.minY+2||ground+9>256)continue;
                boolean valid=true;
                for(int dz=-1;dz<=1&&valid;dz++)for(int dx=-1;dx<=1;dx++) {
                    int nearby=top(gx+dx,gz+dz,layer);
                    if(Math.abs(nearby-ground)>1||!solid(gx+dx,ground-1,gz+dz)
                            ||lakes!=null&&lakes.blocksVegetation(gx+dx,nearby+1,gz+dz)) {valid=false;break;}
                    for(int y=ground+1;y<=ground+8;y++)if(solid(gx+dx,y,gz+dz)){valid=false;break;}
                }
                if(valid)guardians.add(new BlockPos(gx,ground+1,gz));
            }
            if(guardians.isEmpty())continue;
            if(guardians.size()==2&&random.nextBoolean())guardians.remove(1);
            JotunheimEcology.Profile profile=JotunheimEcology.resolve(kernel.biomeIndex(x,z),layer);
            String rock="basalt".equals(profile.rock)?"volcanic_rock":profile.rock;
            Map<Long,int[]> mound=new LinkedHashMap<>();
            for(int along=-6;along<=1;along++)for(int across=-5;across<=5;across++) {
                if(along>0&&Math.abs(across)<=1)continue;
                double distance=along*along/49.0D+across*across/36.0D;
                if(distance>=1.0D)continue;
                int wx=ex+DX[direction]*along-DZ[direction]*across;
                int wz=ez+DZ[direction]*along+DX[direction]*across;
                int ground=top(wx,wz,layer),rise=(int)Math.ceil(3.0D*(1.0D-distance));
                if(ground<surface-2||ground>surface+2||ground+rise>layer.maxY+3
                        ||lakes!=null&&lakes.blocksVegetation(wx,ground+1,wz)){good=false;break;}
                mound.put(key(wx,wz),new int[]{ground,Math.min(surface+3,ground+rise)});
            }
            if(!good)continue;
            Plan plan=new Plan(x,z,floor,surface,radius,height,direction,entranceRadius,layer,guardians,
                    JotunheimBlocks.state(rock),JotunheimBlocks.state(profile.top),mound);
            if(!overlapsWell(plan))return plan;
        }
        return null;
    }

    private boolean overlapsWell(Plan cave) {
        if(wells==null)return false;
        int region=JotunheimYmirWells.REGION_CHUNKS*16;
        for(int rx=Math.floorDiv(cave.minX-16,region);rx<=Math.floorDiv(cave.maxX+16,region);rx++)
            for(int rz=Math.floorDiv(cave.minZ-16,region);rz<=Math.floorDiv(cave.maxZ+16,region);rz++) {
                JotunheimYmirWells.Plan well=wells.planForRegion(rx,rz);
                if(well!=null&&well.x+16>=cave.minX&&well.x-16<=cave.maxX
                        &&well.z+16>=cave.minZ&&well.z-16<=cave.maxZ
                        &&well.upperFloorY+8>=cave.minY&&well.lowerFloorY-8<=cave.maxY)return true;
            }
        return false;
    }

    public synchronized void decorate(ChunkPrimer primer,int chunkX,int chunkZ) {
        int x0=chunkX*16,z0=chunkZ*16;
        for(Plan plan:plansForChunk(chunkX,chunkZ)) {
            for(int x=Math.max(x0,plan.minX);x<=Math.min(x0+15,plan.maxX);x++)
                for(int z=Math.max(z0,plan.minZ);z<=Math.min(z0+15,plan.maxZ);z++)
                    for(int y=plan.minY;y<=Math.min(255,plan.maxY);y++) {
                        IBlockState block=blockAt(plan,x,y,z);
                        if(block!=null)primer.setBlockState(x-x0,y,z-z0,block);
                    }
        }
    }

    /** Null leaves the existing terrain/decoration untouched. Absolute world coordinates. */
    @Nullable
    public IBlockState blockAt(Plan p,int x,int y,int z) {
        if(!p.contains(x,y,z))return null;
        for(BlockPos guardian:p.guardians) {
            if(Math.abs(x-guardian.getX())<=1&&Math.abs(z-guardian.getZ())<=1) {
                if(y==guardian.getY()-1)return p.surface;
                if(y>=guardian.getY()&&y<=guardian.getY()+8)return Blocks.AIR.getDefaultState();
            }
        }
        int dx=x-p.x,dz=z-p.z;
        int r=dx*DX[p.direction]+dz*DZ[p.direction];
        int w=-dx*DZ[p.direction]+dz*DX[p.direction];
        int d2=dx*dx+dz*dz;
        // The circular room owns every interior column. The passage must never stamp
        // its straight walls or raised floor across a sector of the chamber.
        if(d2<=p.radius*p.radius) {
            int ceiling=p.ceilingAt(x,z);
            if(y>=p.floorY-2&&y<=p.floorY)return p.rock;
            if(y>p.floorY&&y<=ceiling)return Blocks.AIR.getDefaultState();
            if(y>ceiling&&y<=ceiling+2)return p.rock;
        }
        if(r>=p.radius-1&&r<=p.entranceRadius&&Math.abs(w)<=2) {
            int pathFloor=p.pathFloor(r);
            if(y>=pathFloor-1&&y<=pathFloor+4) {
                if(y<=pathFloor)return p.rock;
                int roof=pathFloor+(r==p.entranceRadius?3:4);
                if(y>roof)return null;
                if(Math.abs(w)==2||y==roof) {
                    // A three-block turf lintel keeps the visible mouth low and inconspicuous.
                    if(r>=p.entranceRadius-4&&y==roof)return p.surface;
                    return p.rock;
                }
                return Blocks.AIR.getDefaultState();
            }
        }
        int[] mound=p.mound.get(key(x,z));
        if(mound!=null) {
            if(y>=mound[0]&&y<=mound[1])return y==mound[1]?p.surface:p.rock;
            if(y>mound[1]&&y<=mound[1]+5)return Blocks.AIR.getDefaultState();
        }
        if(d2<=(p.radius+2)*(p.radius+2)) {
            int ceiling=p.ceilingAt(x,z);
            if(y>=p.floorY-2&&y<=p.floorY)return p.rock;
            if(y>p.floorY&&y<=ceiling)return d2<=p.radius*p.radius?Blocks.AIR.getDefaultState():p.rock;
            if(y>ceiling&&y<=ceiling+2)return p.rock;
        }
        return null;
    }

    private JotunheimTerrainKernel.SampledChunk terrain(int x,int z) {
        int cx=Math.floorDiv(x,16),cz=Math.floorDiv(z,16);long id=key(cx,cz);
        JotunheimTerrainKernel.SampledChunk chunk=terrain.get(id);
        if(chunk==null){chunk=kernel.sampleChunk(cx,cz);terrain.put(id,chunk);}return chunk;
    }
    private int top(int x,int z,HeightLayer layer) {
        JotunheimTerrainKernel.SampledChunk chunk=terrain(x,z);
        for(int y=layer.maxY;y>=layer.minY;y--)if(chunk.isSolid(Math.floorMod(x,16),y,Math.floorMod(z,16)))return y;
        return -1;
    }
    private boolean solid(int x,int y,int z) {return terrain(x,z).isSolid(Math.floorMod(x,16),y,Math.floorMod(z,16));}
    private static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
    private static long mix(long value){value=(value^(value>>>30))*0xBF58476D1CE4E5B9L;value=(value^(value>>>27))*0x94D049BB133111EBL;return value^(value>>>31);}
}
