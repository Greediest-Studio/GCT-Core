package com.smd.gctcore.common.mixin.dynamicdynamos;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Pseudo
@Mixin(targets = "com.elytradev.dynamicdynamos.Proxy", remap = false)
public abstract class MixinProxy {

    @Shadow
    private Map<Object, Integer> lastTickRate;

    @Shadow
    private List<Runnable> doLater;

    @Unique
    private static Class<?> gctcore$dynamoClass;

    /**
     * Overwrite Dynamic Dynamos' server tick handler to avoid fail-fast iteration over mutable tick lists.
     *
     * @author gctcore
     * @reason MoreFluxStorage/Dynamic Dynamos can throw ConcurrentModificationException while iterating
     * deferred sync tasks or loaded tile entities during the server tick.
     */
    @Overwrite
    public void onTick(ServerTickEvent e) {
        if (e.phase != Phase.END) {
            return;
        }

        List<Runnable> pendingTasks = new ArrayList<>(this.doLater);
        this.doLater.clear();
        for (Runnable task : pendingTasks) {
            task.run();
        }

        for (WorldServer world : DimensionManager.getWorlds()) {
            List<TileEntity> loadedTiles = new ArrayList<>(world.loadedTileEntityList);
            for (TileEntity te : loadedTiles) {
                if (gctcore$isDynamo(te)) {
                    int energyPerTick = gctcore$getEnergyPerTick(te);
                    Integer previousRate = this.lastTickRate.get(te);

                    if (previousRate != null && previousRate != energyPerTick) {
                        Chunk chunk = world.getChunk(te.getPos());
                        List<EntityPlayer> players = new ArrayList<>(world.playerEntities);
                        for (EntityPlayer player : players) {
                            if (player instanceof EntityPlayerMP) {
                                EntityPlayerMP mp = (EntityPlayerMP) player;
                                if (world.getPlayerChunkMap().isPlayerWatchingChunk(mp, chunk.x, chunk.z)) {
                                    gctcore$sendEnergyRate(te, energyPerTick, mp);
                                }
                            }
                        }
                    }

                    this.lastTickRate.put(te, energyPerTick);
                }
            }
        }
    }

    @Unique
    private static boolean gctcore$isDynamo(TileEntity te) {
        try {
            if (gctcore$dynamoClass == null) {
                gctcore$dynamoClass = Class.forName("cofh.thermalexpansion.block.dynamo.TileDynamoBase");
            }
            return gctcore$dynamoClass.isInstance(te);
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    @Unique
    private static int gctcore$getEnergyPerTick(TileEntity dynamo) {
        try {
            return (Integer) dynamo.getClass().getMethod("getInfoEnergyPerTick").invoke(dynamo);
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    @Unique
    private static void gctcore$sendEnergyRate(TileEntity dynamo, int energyPerTick, EntityPlayerMP player) {
        try {
            Class<?> messageClass = Class.forName("com.elytradev.dynamicdynamos.UpdateDynamoEnergyRate$Message");
            Object message = messageClass.newInstance();
            messageClass.getField("pos").set(message, dynamo.getPos());
            messageClass.getField("energyPerTick").setInt(message, energyPerTick);

            Class<?> modClass = Class.forName("com.elytradev.dynamicdynamos.DynamicDynamos");
            Object mod = modClass.getField("inst").get(null);
            Object network = modClass.getField("network").get(mod);

            if (network instanceof SimpleNetworkWrapper && message instanceof IMessage) {
                ((SimpleNetworkWrapper) network).sendTo((IMessage) message, player);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
