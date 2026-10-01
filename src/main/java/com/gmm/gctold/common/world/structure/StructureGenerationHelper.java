package com.gmm.gctold.common.world.structure;

import com.smd.gctcore.gctcore;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import com.gmm.gctold.GctAllMod;
import com.smd.gctcore.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.BlockRotationProcessor;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.Loader;

public final class StructureGenerationHelper {
  private static final Rotation[] ROTATIONS = { Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180 };
  private static final Mirror[] MIRRORS = { Mirror.NONE, Mirror.LEFT_RIGHT };
  private static final Set<ResourceLocation> CHECKED_TEMPLATE_PALETTES = new HashSet<>();
  private static final Set<String> LOGGED_MISSING_EXTERNAL_BLOCKS = new HashSet<>();
  private static final Map<ResourceLocation, StructureBoundingBox> TEMPLATE_OCCUPANCY_BOUNDS = new HashMap<>();

  private StructureGenerationHelper() {}

  static int findSurfaceY(World world, int x, int z, boolean netherStyle) {
    if (netherStyle) {
      return findNetherSurfaceY(world, x, z);
    }

    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 255, z);
    while (pos.getY() > 0 && !isSolid(world, pos)) {
      pos.setY(pos.getY() - 1);
    }
    return pos.getY() - 1;
  }

  public static boolean placeTemplate(World world, Random random, ResourceLocation templateId, BlockPos origin) {
    return placeTemplate(world, templateId, origin, randomRotation(random), randomMirror(random));
  }

  public static boolean placeTemplate(World world, Random random, StructureTemplateId templateId, BlockPos origin) {
    return placeTemplate(world, templateId, origin, randomRotation(random), randomMirror(random));
  }

  public static boolean placeTemplate(World world, Random random, String templateName, BlockPos origin) {
    return placeTemplate(world, random, new StructureTemplateId(templateName), origin);
  }

  public static boolean placeTemplate(World world, String templateName, BlockPos origin, Rotation rotation,
      Mirror mirror) {
    return placeTemplate(world, new StructureTemplateId(templateName), origin, rotation, mirror);
  }

  public static boolean placeTemplate(World world, StructureTemplateId templateId, BlockPos origin, Rotation rotation,
      Mirror mirror) {
    return placeTemplate(world, templateId.getResourceLocation(), origin, rotation, mirror);
  }

  public static boolean placeTemplate(World world, ResourceLocation templateId, BlockPos origin, Rotation rotation,
      Mirror mirror) {
    return placeTemplate(world, templateId, origin, rotation, mirror, false);
  }

  static boolean placeTemplateDuringWorldgen(World world, StructureTemplateId templateId, BlockPos origin,
      Rotation rotation, Mirror mirror) {
    return placeTemplate(world, templateId.getResourceLocation(), origin, rotation, mirror, true);
  }

  private static boolean placeTemplate(World world, ResourceLocation templateId, BlockPos origin, Rotation rotation,
      Mirror mirror, boolean requireLoadedArea) {
    if (world.isRemote || !(world instanceof WorldServer) || world.getMinecraftServer() == null) {
      return false;
    }

    Template template = ((WorldServer)world).getStructureTemplateManager()
        .getTemplate(world.getMinecraftServer(), templateId);
    if (template == null) {
      return false;
    }

    PlacementSettings settings = new PlacementSettings()
        .setRotation(rotation)
        .setMirror(mirror)
        .setChunk((ChunkPos)null)
        .setReplacedBlock((Block)null)
        .setIgnoreStructureBlock(false)
        .setIgnoreEntities(false);
    StructureBoundingBox bounds = getTemplateBounds(templateId, template, origin, settings);
    if (requireLoadedArea) {
      if (!world.isAreaLoaded(bounds, false) || hasGeneratedStructure(world, bounds)) {
        return false;
      }
    }
    logMissingExternalBlocks(templateId);
    addTemplateBlocks(world, templateId, template, origin, settings);
    if (requireLoadedArea) {
      GeneratedStructureData.get(world).add(bounds);
    }
    return true;
  }

  private static void addTemplateBlocks(World world, ResourceLocation templateId, Template template, BlockPos origin,
      PlacementSettings settings) {
    IBlockState oldState = world.getBlockState(origin);
    world.notifyBlockUpdate(origin, oldState, oldState, 3);
    template.addBlocksToWorld(world, origin, new OptionalExternalBlockProcessor(templateId, origin, settings), settings,
        2);
  }

  private static int findNetherSurfaceY(World world, int x, int z) {
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 255, z);
    boolean foundAirPocket = false;
    while (pos.getY() > 0) {
      if (!foundAirPocket && !isSolid(world, pos)) {
        foundAirPocket = true;
      } else if (foundAirPocket && isSolid(world, pos)) {
        break;
      }
      pos.setY(pos.getY() - 1);
    }
    return pos.getY() - 1;
  }

  private static boolean isSolid(World world, BlockPos pos) {
    IBlockState state = world.getBlockState(pos);
    return !world.isAirBlock(pos) && state.getBlock().getMaterial(state).blocksMovement();
  }

  private static StructureBoundingBox getTemplateBounds(ResourceLocation templateId, Template template, BlockPos origin,
      PlacementSettings settings) {
    StructureBoundingBox localBounds = getTemplateOccupancyBounds(templateId);
    if (localBounds == null) {
      localBounds = getFullTemplateBounds(template);
    }

    StructureBoundingBox bounds = null;
    int[] xs = { localBounds.minX, localBounds.maxX };
    int[] ys = { localBounds.minY, localBounds.maxY };
    int[] zs = { localBounds.minZ, localBounds.maxZ };
    for (int x : xs) {
      for (int y : ys) {
        for (int z : zs) {
          BlockPos corner = Template.transformedBlockPos(settings, new BlockPos(x, y, z)).add(origin);
          if (bounds == null) {
            bounds = new StructureBoundingBox(corner, corner);
          } else {
            bounds.expandTo(new StructureBoundingBox(corner, corner));
          }
        }
      }
    }
    return bounds == null ? new StructureBoundingBox(origin, origin) : bounds;
  }

  private static StructureBoundingBox getFullTemplateBounds(Template template) {
    BlockPos size = template.getSize();
    return new StructureBoundingBox(0, 0, 0, Math.max(size.getX() - 1, 0), Math.max(size.getY() - 1, 0),
        Math.max(size.getZ() - 1, 0));
  }

  @Nullable
  private static StructureBoundingBox getTemplateOccupancyBounds(ResourceLocation templateId) {
    if (TEMPLATE_OCCUPANCY_BOUNDS.containsKey(templateId)) {
      StructureBoundingBox cached = TEMPLATE_OCCUPANCY_BOUNDS.get(templateId);
      return cached == null ? null : new StructureBoundingBox(cached);
    }

    StructureBoundingBox bounds = readTemplateOccupancyBounds(templateId);
    TEMPLATE_OCCUPANCY_BOUNDS.put(templateId, bounds == null ? null : new StructureBoundingBox(bounds));
    return bounds;
  }

  @Nullable
  private static StructureBoundingBox readTemplateOccupancyBounds(ResourceLocation templateId) {
    NBTTagCompound templateNbt = readTemplateNbt(templateId);
    if (templateNbt == null || !templateNbt.hasKey("palette", 9) || !templateNbt.hasKey("blocks", 9)) {
      return null;
    }

    NBTTagList palette = templateNbt.getTagList("palette", 10);
    boolean[] airStates = new boolean[palette.tagCount()];
    for (int i = 0; i < palette.tagCount(); i++) {
      IBlockState state = NBTUtil.readBlockState(palette.getCompoundTagAt(i));
      airStates[i] = state.getBlock() == Blocks.AIR;
    }

    StructureBoundingBox bounds = null;
    NBTTagList blocks = templateNbt.getTagList("blocks", 10);
    for (int i = 0; i < blocks.tagCount(); i++) {
      NBTTagCompound blockTag = blocks.getCompoundTagAt(i);
      int stateId = blockTag.getInteger("state");
      if (stateId >= 0 && stateId < airStates.length && airStates[stateId]) {
        continue;
      }

      NBTTagList pos = blockTag.getTagList("pos", 3);
      if (pos.tagCount() < 3) {
        continue;
      }
      BlockPos corner = new BlockPos(pos.getIntAt(0), pos.getIntAt(1), pos.getIntAt(2));
      if (bounds == null) {
        bounds = new StructureBoundingBox(corner, corner);
      } else {
        bounds.expandTo(new StructureBoundingBox(corner, corner));
      }
    }
    return bounds;
  }

  private static boolean hasGeneratedStructure(World world, StructureBoundingBox bounds) {
    return GeneratedStructureData.get(world).intersects(expand(bounds, 8));
  }

  private static StructureBoundingBox expand(StructureBoundingBox bounds, int distance) {
    return new StructureBoundingBox(
        bounds.minX - distance, bounds.minY - distance, bounds.minZ - distance,
        bounds.maxX + distance, bounds.maxY + distance, bounds.maxZ + distance);
  }

  static int randomChunkX(Random random, int chunkX) {
    return chunkX + random.nextInt(16);
  }

  static int randomChunkZ(Random random, int chunkZ) {
    return chunkZ + random.nextInt(16);
  }

  static Rotation randomRotation(Random random) {
    return ROTATIONS[random.nextInt(ROTATIONS.length)];
  }

  static Mirror randomMirror(Random random) {
    return MIRRORS[random.nextInt(MIRRORS.length)];
  }

  private static void logMissingExternalBlocks(ResourceLocation templateId) {
    if (!CHECKED_TEMPLATE_PALETTES.add(templateId)) {
      return;
    }

    NBTTagCompound templateNbt = readTemplateNbt(templateId);
    if (templateNbt == null || !templateNbt.hasKey("palette", 9)) {
      return;
    }

    NBTTagList palette = templateNbt.getTagList("palette", 10);
    for (int i = 0; i < palette.tagCount(); i++) {
      NBTTagCompound stateTag = palette.getCompoundTagAt(i);
      if (!stateTag.hasKey("Name", 8)) {
        continue;
      }

      ResourceLocation blockId = new ResourceLocation(stateTag.getString("Name"));
      if (isExternalBlockMissing(blockId)) {
        logMissingExternalBlock(templateId, blockId);
      }
    }
  }

  @Nullable
  private static NBTTagCompound readTemplateNbt(ResourceLocation templateId) {
    String path = "/assets/" + templateId.getNamespace() + "/structures/" + templateId.getPath() + ".nbt";
    try (InputStream inputStream = MinecraftServer.class.getResourceAsStream(path)) {
      return inputStream == null ? null : CompressedStreamTools.readCompressed(inputStream);
    } catch (IOException | RuntimeException e) {
      gctcore.LOGGER.warn("Unable to read structure template palette: {}", templateId, e);
      return null;
    }
  }

  private static boolean isExternalBlockMissing(ResourceLocation blockId) {
    return getExternalBlock(blockId) == null;
  }

  @Nullable
  private static Block getExternalBlock(ResourceLocation blockId) {
    String namespace = blockId.getNamespace();
    if ("minecraft".equals(namespace) || Tags.MOD_ID.equals(namespace)) {
      return Block.REGISTRY.getObject(blockId);
    }
    if (!Loader.isModLoaded(namespace) || !Block.REGISTRY.containsKey(blockId)) {
      return null;
    }
    return Block.REGISTRY.getObject(blockId);
  }

  private static void logMissingExternalBlock(ResourceLocation templateId, ResourceLocation blockId) {
    String key = templateId + "|" + blockId;
    if (LOGGED_MISSING_EXTERNAL_BLOCKS.add(key)) {
      String modId = blockId.getNamespace();
      gctcore.LOGGER.warn(
          "\u7f3a\u5c11\u6a21\u7ec4\uff1a{}\uff0c\u65e0\u6cd5\u83b7\u53d6{}\uff1a{}\uff0c\u7ed3\u6784\u540d\u79f0\u4e3a{}",
          modId, modId, blockId.getPath(), templateId.getPath());
    }
  }

  private static final class OptionalExternalBlockProcessor implements ITemplateProcessor {
    private final ResourceLocation templateId;
    private final BlockRotationProcessor rotationProcessor;

    private OptionalExternalBlockProcessor(ResourceLocation templateId, BlockPos origin, PlacementSettings settings) {
      this.templateId = templateId;
      this.rotationProcessor = new BlockRotationProcessor(origin, settings);
    }

    @Nullable
    @Override
    public Template.BlockInfo processBlock(World world, BlockPos pos, Template.BlockInfo blockInfo) {
      Template.BlockInfo rotated = this.rotationProcessor.processBlock(world, pos, blockInfo);
      if (rotated == null || canPlaceTemplateBlock(rotated.blockState.getBlock())) {
        return rotated == null || rotated.tileentityData != null ? rotated
            : new Template.BlockInfo(rotated.pos, rotated.blockState, blockInfo.tileentityData);
      }
      return new Template.BlockInfo(rotated.pos, Blocks.AIR.getDefaultState(), null);
    }

    private boolean canPlaceTemplateBlock(Block block) {
      ResourceLocation id = Block.REGISTRY.getNameForObject(block);
      if (id == null || !isExternalBlockMissing(id)) {
        return true;
      }
      logMissingExternalBlock(templateId, id);
      return false;
    }
  }
}
