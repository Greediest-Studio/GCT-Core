package com.smd.gctcore.common.integration.witherstorm;

import com.shiver.gct_additions.common.world.dimension.WorldTheVoid;
import com.wdcftgg.witherstormmod.api.common.event.CanWitherStormTargetMobEvent;
import com.wdcftgg.witherstormmod.api.common.event.SickenedMobConversionEvent;
import com.wdcftgg.witherstormmod.api.common.event.WitherStormConsumeEvent;
import com.wdcftgg.witherstormmod.api.common.event.WitherStormConsumeBlockEvent;
import com.wdcftgg.witherstormmod.api.common.event.WitherStormVictoryEvent;
import com.wdcftgg.witherstormmod.common.config.WitherStormConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.ItemHandlerHelper;

public final class WitherStormCompatibility {
    public static void configureSummoning() {
        WitherStormConfig.summoningDimensionListMode = WitherStormConfig.ListMode.WHITELIST;
        WitherStormConfig.summoningDimensions = new String[]{String.valueOf(WorldTheVoid.DIMID)};
    }

    private static boolean protectedEntity(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        return id != null && id.getNamespace().equals("gct_additions");
    }

    @SubscribeEvent
    public void protectTarget(CanWitherStormTargetMobEvent event) {
        if (protectedEntity(event.getPotentialTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void protectConsumedEntity(WitherStormConsumeEvent event) {
        if (protectedEntity(event.getConsumedEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void protectConversion(SickenedMobConversionEvent.Pre event) {
        if (event.getDirection() == SickenedMobConversionEvent.Direction.INFECTION
                && protectedEntity(event.getOriginal())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void consumeBedrock(WitherStormConsumeBlockEvent event) {
        Entity storm = event.getEntity();
        if (storm.dimension == WorldTheVoid.DIMID && event.getState().getBlock() == Blocks.BEDROCK) {
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public void victory(WitherStormVictoryEvent event) {
        if (event.getParticipants().isEmpty()) return;
        EntityPlayerMP recipient = event.getKiller() == null ? event.getParticipants().get(0) : event.getKiller();
        giveReward(recipient, "witherstormmod:withered_nether_star", 1);
        giveReward(recipient, "gct_additions:stormy_shard", 5);
        Entity storm = event.getEntity();
        giveReward(recipient, "additions:withered_nether_star_shard", 8 + storm.world.rand.nextInt(25));
        if (Loader.isModLoaded("ftbquests")) WitherStormQuestBridge.credit(event.getParticipants());
        event.handleLoot();
    }

    private static void giveReward(EntityPlayerMP player, String id, int count) {
        ItemHandlerHelper.giveItemToPlayer(player,
                new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)), count));
    }

}
