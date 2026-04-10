package com.smd.gctcore.common.world.OrderCore;

import com.smd.gctcore.common.world.chunks.ChunkGeneratorOrderCore;

import net.minecraft.init.Biomes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderOrderCore extends WorldProvider {

    public WorldProviderOrderCore() {
        super();
        this.setDimension(103);
        if (world != null && world.isRemote) {
            setupClientSide();
        }
    }

    @SideOnly(Side.CLIENT)
    private void setupClientSide() {
        // 设置自定义天空渲染器 - 这将处理天空、太阳和月亮的渲染
        this.setSkyRenderer(new OrderCoreSkyRenderer());
        
        // 在1.12.2中，太阳和月亮的渲染通常在天空渲染器中一起处理
        // 或者通过重写WorldProvider的方法来自定义颜色
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionTypeOrderCore.ordercore;
    }

    @Override
    public boolean canRespawnHere() {
        return true;
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorOrderCore(world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public net.minecraft.util.math.Vec3d getSkyColor(net.minecraft.entity.Entity cameraEntity, float partialTicks) {
        // 自定义天空颜色 - 深紫色
        return new net.minecraft.util.math.Vec3d(0.2D, 0.1D, 0.4D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public net.minecraft.util.math.Vec3d getFogColor(float celestialAngle, float partialTicks) {
        // 返回透明的雾颜色，实际上禁用雾效的视觉效果
        return new net.minecraft.util.math.Vec3d(0.0D, 0.0D, 0.0D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        // 自定义日出日落颜色 - 金橙色
        float[] colors = new float[4];
        colors[0] = 1.0F; // 红色分量
        colors[1] = 0.7F; // 绿色分量  
        colors[2] = 0.2F; // 蓝色分量
        colors[3] = 0.8F; // Alpha透明度
        return colors;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public net.minecraft.util.math.Vec3d getCloudColor(float partialTicks) {
        // 自定义云朵颜色 - 浅紫色
        return new net.minecraft.util.math.Vec3d(0.6D, 0.5D, 0.8D);
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        return true;
    }

    @Override
    public int getAverageGroundLevel() {
        return 64;
    }

    @Override
    public boolean doesXZShowFog(int x, int z) {
        // 完全禁用坐标相关的雾效
        return false;
    }

    @Override
    public float getCloudHeight() {
        // 将云层设置得很高，减少视觉干扰
        return 512.0F;
    }

    @Override
    public double getVoidFogYFactor() {
        // 禁用虚空雾效
        return 0.0D;
    }

    @Override
    public boolean doesWaterVaporize() {
        // 禁用水蒸发（可能产生雾效）
        return false;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        // 自定义天体角度计算，可以控制太阳/月亮的移动速度
        // 返回值在0.0到1.0之间，0.0是午夜，0.5是正午
        return super.calculateCelestialAngle(worldTime, partialTicks);
    }

    @Override
    public boolean canBlockFreeze(net.minecraft.util.math.BlockPos pos, boolean byWater) {
        // 防止维度内结冰
        return false;
    }

    @Override
    public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk) {
        // 禁用闪电
        return false;
    }

    @Override
    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk) {
        return false;
    }

    @Override
    public void init () {
        this.biomeProvider = new BiomeProviderSingle(Biomes.VOID);
        this.hasSkyLight = true;
    }
}
