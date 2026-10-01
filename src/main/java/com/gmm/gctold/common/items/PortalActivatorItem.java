package com.gmm.gctold.common.items;

import java.util.function.IntSupplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.common.DimensionManager;
import com.gmm.gctold.common.world.structure.StructureGenerationHelper;
import com.gmm.gctold.common.world.structure.StructureTemplateId;

public class PortalActivatorItem extends Item {
    @FunctionalInterface
    public interface PortalPlacer {
        void place(World world, BlockPos origin, EnumFacing widthDir);
    }

    private static final int FALLBACK_PORTAL_WIDTH = 2;
    private static final int FALLBACK_PORTAL_HEIGHT = 3;
    private static final int TELEPORT_SEARCH_RADIUS = 32;
    private static final int TELEPORT_SEARCH_VERTICAL = 32;

    private final Block portalBlock;
    private final IntSupplier targetDimension;
    private final StructureTemplateId eastWestTemplate;
    private final StructureTemplateId northSouthTemplate;
    private final int portalHeight;
    private final PortalPlacer portalPlacer;

    public PortalActivatorItem(String name, CreativeTabs tab, Block portalBlock,
            IntSupplier targetDimension) {
        this(name, tab, portalBlock, targetDimension, null, null);
    }

    public PortalActivatorItem(String name, CreativeTabs tab, Block portalBlock,
            IntSupplier targetDimension, int portalHeight, PortalPlacer portalPlacer) {
        this(name, tab, portalBlock, targetDimension, null, null, portalHeight, portalPlacer);
    }

    public PortalActivatorItem(String name, CreativeTabs tab, Block portalBlock,
            IntSupplier targetDimension, StructureTemplateId eastWestTemplate, StructureTemplateId northSouthTemplate) {
        this(name, tab, portalBlock, targetDimension, eastWestTemplate, northSouthTemplate, FALLBACK_PORTAL_HEIGHT, null);
    }

    private PortalActivatorItem(String name, CreativeTabs tab, Block portalBlock,
            IntSupplier targetDimension, StructureTemplateId eastWestTemplate, StructureTemplateId northSouthTemplate,
            int portalHeight, PortalPlacer portalPlacer) {
        this.portalBlock = portalBlock;
        this.targetDimension = targetDimension;
        this.eastWestTemplate = eastWestTemplate;
        this.northSouthTemplate = northSouthTemplate;
        this.portalHeight = portalHeight;
        this.portalPlacer = portalPlacer;
        setMaxStackSize(1);
        setMaxDamage(64);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(tab);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, EnumHand hand) {
        if (player.isSneaking()) {
            return EnumActionResult.PASS;
        }
        return forcePortal(player, world, pos, hand, facing);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return EnumActionResult.PASS;
        }
        return forcePortal(player, world, pos, hand, facing);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(teleportToTargetDimension(player) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
    }

    private EnumActionResult forcePortal(EntityPlayer player, World world, BlockPos clickedPos, EnumHand hand,
            EnumFacing facing) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(clickedPos.offset(facing), facing, stack)) {
            return EnumActionResult.FAIL;
        }
        PortalPlacement placement = getPortalPlacement(player, clickedPos, facing);
        if (!isValidPortalY(world, placement)) {
            if (!world.isRemote) {
                player.sendMessage(new TextComponentString("该位置不可强制生成传送门结构"));
            }
            return EnumActionResult.FAIL;
        }
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        placePortalStructure(world, placement);
        if (hasPortalNear(world, placement.origin) && !player.capabilities.isCreativeMode) {
            stack.damageItem(1, (EntityLivingBase)player);
        }
        return EnumActionResult.SUCCESS;
    }

    private boolean teleportToTargetDimension(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return false;
        }
        EntityPlayerMP playerMP = (EntityPlayerMP)player;
        WorldServer targetWorld = getTargetWorld(playerMP.server);
        if (targetWorld == null) {
            return false;
        }

        BlockPos preferred = new BlockPos(playerMP.posX, playerMP.posY, playerMP.posZ);
        BlockPos destination = findOrCreateTeleportPos(targetWorld, preferred, playerMP.posY);
        transferPlayer(playerMP, targetWorld, destination);
        return true;
    }

    private WorldServer getTargetWorld(MinecraftServer server) {
        int dimension = targetDimension.getAsInt();
        WorldServer targetWorld = server.getWorld(dimension);
        if (targetWorld == null) {
            DimensionManager.initDimension(dimension);
            targetWorld = DimensionManager.getWorld(dimension);
        }
        return targetWorld;
    }

    private void transferPlayer(EntityPlayerMP player, WorldServer targetWorld, BlockPos destination) {
        int dimension = targetWorld.provider.getDimension();
        if (player.dimension != dimension) {
            player.changeDimension(dimension, new FixedPositionTeleporter(targetWorld, destination));
        }
        player.connection.setPlayerLocation(destination.getX() + 0.5D, destination.getY(), destination.getZ() + 0.5D,
                player.rotationYaw, player.rotationPitch);
        player.motionX = 0.0D;
        player.motionY = 0.0D;
        player.motionZ = 0.0D;
        player.fallDistance = 0.0F;
        player.timeUntilPortal = player.getPortalCooldown();
    }

    private BlockPos findOrCreateTeleportPos(WorldServer world, BlockPos preferred, double originalY) {
        BlockPos target = clampTeleportPos(preferred);
        boolean noFloorBelow = !hasNonAirBelow(world, target);
        boolean blockedAtTarget = isFullBlock(world, target);
        if (!noFloorBelow && !blockedAtTarget && isSafeTeleportPos(world, target)) {
            return target;
        }

        BlockPos nearby = findSafeTeleportPos(world, target);
        if (nearby != null) {
            return nearby;
        }

        if (noFloorBelow) {
            int glassY = originalY > 0.0D && originalY < 255.0D ? target.getY() : 128;
            BlockPos glassPos = new BlockPos(target.getX(), glassY, target.getZ());
            world.setBlockState(glassPos, Blocks.GLASS.getDefaultState(), 3);
            BlockPos feet = clampTeleportPos(glassPos.up());
            world.setBlockToAir(feet);
            world.setBlockToAir(feet.up());
            return feet;
        }

        BlockPos feet = clampTeleportPos(target);
        world.setBlockToAir(feet);
        world.setBlockToAir(feet.up());
        return feet;
    }

    private BlockPos findSafeTeleportPos(WorldServer world, BlockPos center) {
        for (int radius = 0; radius <= TELEPORT_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    for (int dy = -TELEPORT_SEARCH_VERTICAL; dy <= TELEPORT_SEARCH_VERTICAL; dy++) {
                        int y = center.getY() + dy;
                        if (y <= 0 || y >= 255) {
                            continue;
                        }
                        BlockPos candidate = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                        if (isSafeTeleportPos(world, candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isSafeTeleportPos(World world, BlockPos feet) {
        return isFullBlock(world, feet.down()) && isEmptyForPlayer(world, feet) && isEmptyForPlayer(world, feet.up());
    }

    private boolean hasNonAirBelow(World world, BlockPos feet) {
        for (int y = feet.getY() - 1; y >= 0; y--) {
            BlockPos pos = new BlockPos(feet.getX(), y, feet.getZ());
            IBlockState state = world.getBlockState(pos);
            if (!state.getBlock().isAir(state, world, pos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFullBlock(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.isFullCube() && state.getMaterial().blocksMovement() && !state.getMaterial().isLiquid();
    }

    private boolean isEmptyForPlayer(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isAir(state, world, pos) || !state.getMaterial().blocksMovement();
    }

    private BlockPos clampTeleportPos(BlockPos pos) {
        int y = Math.max(1, Math.min(254, pos.getY()));
        return new BlockPos(pos.getX(), y, pos.getZ());
    }

    private PortalPlacement getPortalPlacement(EntityPlayer player, BlockPos clickedPos, EnumFacing facing) {
        EnumFacing horizontalFacing = player.getHorizontalFacing();
        StructureTemplateId template = null;
        BlockPos origin = clickedPos.offset(facing);
        if (eastWestTemplate != null && northSouthTemplate != null) {
            switch (horizontalFacing) {
                case EAST:
                    template = eastWestTemplate;
                    origin = clickedPos.add(0, 1, -1);
                    break;
                case WEST:
                    template = eastWestTemplate;
                    origin = clickedPos.add(0, 1, -2);
                    break;
                case NORTH:
                    template = northSouthTemplate;
                    origin = clickedPos.add(-1, 1, 0);
                    break;
                case SOUTH:
                    template = northSouthTemplate;
                    origin = clickedPos.add(-2, 1, 0);
                    break;
                default:
                    break;
            }
        }
        return new PortalPlacement(origin, getPortalWidthDirection(player, facing), template);
    }

    private boolean isValidPortalY(World world, PortalPlacement placement) {
        if (placement.template != null && world instanceof WorldServer) {
            Template template = ((WorldServer)world).getStructureTemplateManager()
                    .getTemplate(world.getMinecraftServer(), placement.template.getResourceLocation());
            if (template == null) {
                return false;
            }
            int height = Math.max(template.getSize().getY(), 1);
            return placement.origin.getY() >= 0 && placement.origin.getY() + height - 1 <= 255;
        }
        return placement.origin.getY() >= 0 && placement.origin.getY() + portalHeight - 1 <= 255;
    }

    private EnumFacing getPortalWidthDirection(EntityPlayer player, EnumFacing facing) {
        if (facing.getAxis().isHorizontal()) {
            return facing.rotateY();
        }
        return player.getHorizontalFacing().rotateY();
    }

    private void placePortalStructure(World world, PortalPlacement placement) {
        if (placement.template != null) {
            StructureGenerationHelper.placeTemplate(world, placement.template, placement.origin, Rotation.NONE, Mirror.NONE);
            return;
        }
        if (portalPlacer != null) {
            portalPlacer.place(world, placement.origin, placement.widthDir);
            return;
        }
        placePortalBlocks(world, placement.origin, placement.widthDir);
    }

    private void placePortalBlocks(World world, BlockPos base, EnumFacing widthDir) {
        IBlockState portalState = portalBlock instanceof BlockPortal
                ? portalBlock.getDefaultState().withProperty(BlockPortal.AXIS, widthDir.getAxis())
                : portalBlock.getDefaultState();
        for (int width = 0; width < FALLBACK_PORTAL_WIDTH; width++) {
            for (int height = 0; height < FALLBACK_PORTAL_HEIGHT; height++) {
                BlockPos placePos = base.offset(widthDir, width).up(height);
                world.setBlockState(placePos, portalState, 3);
            }
        }
    }

    private boolean hasPortalNear(World world, BlockPos center) {
        for (BlockPos checkPos : BlockPos.getAllInBox(center.add(-3, 0, -3), center.add(6, 6, 6))) {
            if (world.getBlockState(checkPos).getBlock() == portalBlock) {
                return true;
            }
        }
        return false;
    }

    private static final class FixedPositionTeleporter extends Teleporter {
        private final BlockPos destination;

        private FixedPositionTeleporter(WorldServer world, BlockPos destination) {
            super(world);
            this.destination = destination;
        }

        @Override
        public void placeInPortal(Entity entity, float rotationYaw) {
            entity.setLocationAndAngles(destination.getX() + 0.5D, destination.getY(), destination.getZ() + 0.5D,
                    entity.rotationYaw, entity.rotationPitch);
            entity.motionX = 0.0D;
            entity.motionY = 0.0D;
            entity.motionZ = 0.0D;
        }

        @Override
        public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
            placeInPortal(entity, rotationYaw);
            return true;
        }

        @Override
        public boolean makePortal(Entity entity) {
            return true;
        }
    }

    private static final class PortalPlacement {
        private final BlockPos origin;
        private final EnumFacing widthDir;
        private final StructureTemplateId template;

        private PortalPlacement(BlockPos origin, EnumFacing widthDir, StructureTemplateId template) {
            this.origin = origin;
            this.widthDir = widthDir;
            this.template = template;
        }
    }
}
