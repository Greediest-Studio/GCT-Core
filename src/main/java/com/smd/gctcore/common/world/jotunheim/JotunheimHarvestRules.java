package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.Tags;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.ref.WeakReference;
import java.util.UUID;

/** Dimension-local harvest requirements for the two reused terrain rocks. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class JotunheimHarvestRules {
    private static final ResourceLocation BASALT = new ResourceLocation("netherized", "basalt");
    private static final ResourceLocation MAGMA = new ResourceLocation("minecraft", "magma");
    private static final ThreadLocal<HarvestAttempt> LAST_ATTEMPT = new ThreadLocal<>();

    private JotunheimHarvestRules() {}

    private static final class HarvestAttempt {
        private final WeakReference<World> world;
        private final UUID player;
        private final long tick;
        private final long pos;
        private final boolean allowed;

        private HarvestAttempt(BlockEvent.BreakEvent event, boolean allowed) {
            world = new WeakReference<>(event.getWorld());
            player = event.getPlayer().getUniqueID();
            tick = event.getWorld().getTotalWorldTime();
            pos = event.getPos().toLong();
            this.allowed = allowed;
        }

        private boolean matches(World candidateWorld, EntityPlayer candidatePlayer, BlockPos candidatePos) {
            return world.get() == candidateWorld && player.equals(candidatePlayer.getUniqueID())
                    && tick == candidateWorld.getTotalWorldTime() && pos == candidatePos.toLong();
        }
    }

    private static boolean applies(EntityPlayer player, IBlockState state) {
        if (player == null || player.isCreative()
                || !(player.world.provider instanceof WorldProviderJotunheim)) return false;
        ResourceLocation id = state.getBlock().getRegistryName();
        return BASALT.equals(id) || MAGMA.equals(id);
    }

    private static int pickaxeLevel(EntityPlayer player, IBlockState state) {
        ItemStack tool = player.getHeldItemMainhand();
        return tool.isEmpty() ? -1 : tool.getItem().getHarvestLevel(tool, "pickaxe", player, state);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (applies(event.getEntityPlayer(), event.getTargetBlock())) {
            event.setCanHarvest(pickaxeLevel(event.getEntityPlayer(), event.getTargetBlock()) >= 13);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!applies(event.getEntityPlayer(), event.getState())) return;
        int level = pickaxeLevel(event.getEntityPlayer(), event.getState());
        if (level >= 13) return;
        IBlockState state = event.getState();
        ItemStack tool = event.getEntityPlayer().getHeldItemMainhand();
        String harvestTool = state.getBlock().getHarvestTool(state);
        // ForgeHooks bypasses HarvestCheck only for tool-free material or a recognized
        // required tool. The fallback already uses our /100 divisor, including magma
        // whose vanilla block does not declare a harvest tool. Avoid applying it twice.
        boolean directHarvest = state.getMaterial().isToolNotRequired();
        if (!directHarvest && !tool.isEmpty() && harvestTool != null) {
            int originalLevel = tool.getItem().getHarvestLevel(tool, harvestTool, event.getEntityPlayer(), state);
            directHarvest = originalLevel >= 0 && originalLevel >= state.getBlock().getHarvestLevel(state);
        }
        if (directHarvest) {
            event.setNewSpeed(event.getNewSpeed() * 0.3F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isRemote) return;
        LAST_ATTEMPT.remove();
        if (event.isCanceled() || !applies(event.getPlayer(), event.getState())) return;
        boolean allowed = pickaxeLevel(event.getPlayer(), event.getState()) >= 13;
        // Item#onBlockDestroyed can consume a tool's final durability before drops are
        // fired. Remember eligibility only for this synchronous break, not the empty hand.
        LAST_ATTEMPT.set(new HarvestAttempt(event, allowed));
        if (!allowed) event.setExpToDrop(0);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (!applies(event.getHarvester(), event.getState())) return;
        HarvestAttempt attempt = LAST_ATTEMPT.get();
        LAST_ATTEMPT.remove();
        boolean allowed = attempt != null && attempt.matches(event.getWorld(), event.getHarvester(), event.getPos())
                ? attempt.allowed : pickaxeLevel(event.getHarvester(), event.getState()) >= 13;
        if (!allowed) {
            event.getDrops().clear();
            event.setDropChance(0.0F);
        }
    }
}
