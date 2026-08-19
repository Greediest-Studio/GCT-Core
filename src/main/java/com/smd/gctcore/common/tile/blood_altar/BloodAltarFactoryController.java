package com.smd.gctcore.common.tile.blood_altar;

import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import hellfirepvp.modularmachinery.common.container.ContainerFactoryController;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Keeps the Blood Altar's factory-controller identity stable for world saves
 * and provides the extension point for its custom GUI in the next migration.
 */
public class BloodAltarFactoryController extends TileFactoryController {

    public BloodAltarFactoryController() {
    }

    public BloodAltarFactoryController(final IBlockState state) {
        super(state);
    }

    /** Server-authoritative action behind the GUI's mode button. */
    public static final class ModePacket implements IMessage, IMessageHandler<ModePacket, IMessage> {
        private BlockPos controllerPos = BlockPos.ORIGIN;

        public ModePacket() {
        }

        public ModePacket(final BlockPos controllerPos) {
            this.controllerPos = controllerPos == null ? BlockPos.ORIGIN : controllerPos;
        }

        @Override
        public void fromBytes(final ByteBuf buffer) {
            this.controllerPos = BlockPos.fromLong(buffer.readLong());
        }

        @Override
        public void toBytes(final ByteBuf buffer) {
            buffer.writeLong(controllerPos.toLong());
        }

        @Override
        public IMessage onMessage(final ModePacket message, final MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> toggleMode(player, message.controllerPos));
            return null;
        }

        private static void toggleMode(final EntityPlayerMP player, final BlockPos position) {
            if (player == null || player.world == null || !player.world.isBlockLoaded(position)
                    || player.getDistanceSqToCenter(position) > 64.0D
                    || !(player.openContainer instanceof ContainerFactoryController)) {
                return;
            }
            final ContainerFactoryController container = (ContainerFactoryController) player.openContainer;
            if (!(container.getOwner() instanceof BloodAltarFactoryController)
                    || !container.getOwner().getPos().equals(position)) {
                return;
            }

            final BloodAltarFactoryController controller =
                    (BloodAltarFactoryController) container.getOwner();
            final NBTTagCompound existing = controller.getCustomDataTag();
            final NBTTagCompound data = existing == null ? new NBTTagCompound() : existing.copy();
            data.setInteger(BloodAltarMachine.DATA_MODE,
                    data.getInteger(BloodAltarMachine.DATA_MODE) == 0 ? 1 : 0);
            controller.setCustomDataTag(data);
            controller.markForUpdateSync();
        }
    }

}
