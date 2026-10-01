package com.gmm.gctold.common.data;

import com.gmm.gctold.common.network.ClientTasks;
import com.gmm.gctold.common.network.GctAllNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public final class GctAllVariables {
    private GctAllVariables() {
    }

    public static class MapVariables extends WorldSavedData {
        public static final String DATA_NAME = "gctcore_mapvars";

        public double time;
        public double deathtime;

        public MapVariables() {
            super(DATA_NAME);
        }

        public MapVariables(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            time = nbt.getDouble("time");
            deathtime = nbt.getDouble("deathtime");
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            nbt.setDouble("time", time);
            nbt.setDouble("deathtime", deathtime);
            return nbt;
        }

        public void syncData(World world) {
            markDirty();
            if (world.isRemote) {
                GctAllNetwork.CHANNEL.sendToServer(new WorldSavedDataSyncMessage(0, this));
            } else {
                GctAllNetwork.CHANNEL.sendToAll(new WorldSavedDataSyncMessage(0, this));
            }
        }

        public static MapVariables get(World world) {
            MapVariables instance = (MapVariables) world.getMapStorage().getOrLoadData(MapVariables.class, DATA_NAME);
            if (instance == null) {
                instance = new MapVariables();
                world.getMapStorage().setData(DATA_NAME, instance);
            }
            return instance;
        }
    }

    public static class WorldVariables extends WorldSavedData {
        public static final String DATA_NAME = "gctcore_worldvars";

        public WorldVariables() {
            super(DATA_NAME);
        }

        public WorldVariables(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            return compound;
        }

        public void syncData(World world) {
            markDirty();
            if (world.isRemote) {
                GctAllNetwork.CHANNEL.sendToServer(new WorldSavedDataSyncMessage(1, this));
            } else {
                GctAllNetwork.CHANNEL.sendToDimension(new WorldSavedDataSyncMessage(1, this),
                        world.provider.getDimension());
            }
        }

        public static WorldVariables get(World world) {
            WorldVariables instance = (WorldVariables) world.getPerWorldStorage()
                    .getOrLoadData(WorldVariables.class, DATA_NAME);
            if (instance == null) {
                instance = new WorldVariables();
                world.getPerWorldStorage().setData(DATA_NAME, instance);
            }
            return instance;
        }
    }

    public static class WorldSavedDataSyncMessage implements IMessage {
        public int type;
        public WorldSavedData data;

        public WorldSavedDataSyncMessage() {
        }

        public WorldSavedDataSyncMessage(int type, WorldSavedData data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(type);
            ByteBufUtils.writeTag(buf, data.writeToNBT(new NBTTagCompound()));
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            type = buf.readInt();
            data = type == 0 ? new MapVariables() : new WorldVariables();
            data.readFromNBT(ByteBufUtils.readTag(buf));
        }
    }

    public static class WorldSavedDataSyncMessageHandler
            implements IMessageHandler<WorldSavedDataSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(WorldSavedDataSyncMessage message, MessageContext context) {
            if (context.side == Side.SERVER) {
                context.getServerHandler().player.getServerWorld()
                        .addScheduledTask(() -> syncData(message, context.side, context.getServerHandler().player.world));
            } else {
                ClientTasks.schedule(() -> syncData(message, context.side, ClientTasks.getClientWorld()));
            }
            return null;
        }

        private void syncData(WorldSavedDataSyncMessage message, Side side, World world) {
            if (world == null) {
                return;
            }
            if (side == Side.SERVER) {
                message.data.markDirty();
                if (message.type == 0) {
                    GctAllNetwork.CHANNEL.sendToAll(message);
                } else {
                    GctAllNetwork.CHANNEL.sendToDimension(message, world.provider.getDimension());
                }
            }
            if (message.type == 0) {
                world.getMapStorage().setData(MapVariables.DATA_NAME, message.data);
            } else {
                world.getPerWorldStorage().setData(WorldVariables.DATA_NAME, message.data);
            }
        }
    }
}
