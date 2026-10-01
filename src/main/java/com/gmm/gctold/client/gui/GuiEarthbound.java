package com.gmm.gctold.client.gui;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import com.gmm.gctold.common.network.GctAllNetwork;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public final class GuiEarthbound {
  public static int GUIID = 2;

  private GuiEarthbound() {
  }

  public static class EarthboundContainer extends Container implements Supplier<Map<Integer, Slot>> {
    private IInventory internal;

    private World world;

    private EntityPlayer entity;

    private int x;

    private int y;

    private int z;

    private Map<Integer, Slot> customSlots = new HashMap<>();

    public EarthboundContainer(World world, int x, int y, int z, EntityPlayer player) {
      this.world = world;
      this.entity = player;
      this.x = x;
      this.y = y;
      this.z = z;
      this.internal = (IInventory)new InventoryBasic("", true, 1);
      TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
      if (ent instanceof IInventory)
        this.internal = (IInventory)ent;
      this.customSlots.put(Integer.valueOf(0), addSlotToContainer(new Slot(this.internal, 0, 76, 7) {

            }));
      int si;
      for (si = 0; si < 3; si++) {
        for (int sj = 0; sj < 9; sj++)
          addSlotToContainer(new Slot((IInventory)player.inventory, sj + (si + 1) * 9, 5 + sj * 18, 31 + si * 18));
      }
      for (si = 0; si < 9; si++)
        addSlotToContainer(new Slot((IInventory)player.inventory, si, 5 + si * 18, 89));
    }

    public Map<Integer, Slot> get() {
      return this.customSlots;
    }

    public boolean canInteractWith(EntityPlayer player) {
      return this.internal.isUsableByPlayer(player);
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
        ItemStack itemstack1 = slot.getStack();
        itemstack = itemstack1.copy();
        if (index < 1) {
          if (!mergeItemStack(itemstack1, 1, this.inventorySlots.size(), true))
            return ItemStack.EMPTY;
          slot.onSlotChange(itemstack1, itemstack);
        } else if (!mergeItemStack(itemstack1, 0, 1, false)) {
          if (index < 28) {
            if (!mergeItemStack(itemstack1, 28, this.inventorySlots.size(), true))
              return ItemStack.EMPTY;
          } else if (!mergeItemStack(itemstack1, 1, 28, false)) {
            return ItemStack.EMPTY;
          }
          return ItemStack.EMPTY;
        }
        if (itemstack1.getCount() == 0) {
          slot.putStack(ItemStack.EMPTY);
        } else {
          slot.onSlotChanged();
        }
        if (itemstack1.getCount() == itemstack.getCount())
          return ItemStack.EMPTY;
        slot.onTake(playerIn, itemstack1);
      }
      return itemstack;
    }

    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
      boolean flag = false;
      int i = startIndex;
      if (reverseDirection)
        i = endIndex - 1;
      if (stack.isStackable())
        while (!stack.isEmpty() && (
          reverseDirection ? (
          i < startIndex) : (

          i >= endIndex))) {
          Slot slot = this.inventorySlots.get(i);
          ItemStack itemstack = slot.getStack();
          if (slot.isItemValid(itemstack) && !itemstack.isEmpty() && itemstack.getItem() == stack.getItem() && (
            !stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) &&
            ItemStack.areItemStackTagsEqual(stack, itemstack)) {
            int j = itemstack.getCount() + stack.getCount();
            int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
            if (j <= maxSize) {
              stack.setCount(0);
              itemstack.setCount(j);
              slot.putStack(itemstack);
              flag = true;
            } else if (itemstack.getCount() < maxSize) {
              stack.shrink(maxSize - itemstack.getCount());
              itemstack.setCount(maxSize);
              slot.putStack(itemstack);
              flag = true;
            }
          }
          if (reverseDirection) {
            i--;
            continue;
          }
          i++;
        }
      if (!stack.isEmpty()) {
        if (reverseDirection) {
          i = endIndex - 1;
        } else {
          i = startIndex;
        }
        while (reverseDirection ? (
          i < startIndex) : (

          i >= endIndex)) {
          Slot slot1 = this.inventorySlots.get(i);
          ItemStack itemstack1 = slot1.getStack();
          if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
            if (stack.getCount() > slot1.getSlotStackLimit()) {
              slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
            } else {
              slot1.putStack(stack.splitStack(stack.getCount()));
            }
            slot1.onSlotChanged();
            flag = true;
            break;
          }
          if (reverseDirection) {
            i--;
            continue;
          }
          i++;
        }
      }
      return flag;
    }

    public void onContainerClosed(EntityPlayer playerIn) {
      super.onContainerClosed(playerIn);
      if (this.internal instanceof InventoryBasic && playerIn instanceof EntityPlayerMP)
        clearContainer(playerIn, playerIn.world, this.internal);
    }

    private void slotChanged(int slotid, int ctype, int meta) {
      if (this.world != null && this.world.isRemote) {
        GctAllNetwork.CHANNEL.sendToServer(new GuiEarthbound.GUISlotChangedMessage(slotid, this.x, this.y, this.z, ctype, meta));
        GuiEarthbound.handleSlotAction(this.entity, slotid, ctype, meta, this.x, this.y, this.z);
      }
    }
  }

  public static class EarthboundScreen extends GuiContainer {
    private World world;

    private int x;

    private int y;

    private int z;

    private EntityPlayer entity;

    public EarthboundScreen(World world, int x, int y, int z, EntityPlayer entity) {
      super(new GuiEarthbound.EarthboundContainer(world, x, y, z, entity));
      this.world = world;
      this.x = x;
      this.y = y;
      this.z = z;
      this.entity = entity;
      this.xSize = 170;
      this.ySize = 110;
    }

    private static final ResourceLocation texture = new ResourceLocation("gctold:textures/earthbound.png");

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      drawDefaultBackground();
      super.drawScreen(mouseX, mouseY, partialTicks);
      renderHoveredToolTip(mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.renderEngine.bindTexture(texture);
      int k = (this.width - this.xSize) / 2;
      int l = (this.height - this.ySize) / 2;
      drawModalRectWithCustomSizedTexture(k, l, 0.0F, 0.0F, this.xSize, this.ySize, this.xSize, this.ySize);
      this.zLevel = 100.0F;
    }

    public void updateScreen() {
      super.updateScreen();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
      super.keyTyped(typedChar, keyCode);
    }

    protected void drawGuiContainerForegroundLayer(int par1, int par2) {}

    public void onGuiClosed() {
      super.onGuiClosed();
      Keyboard.enableRepeatEvents(false);
    }

    public void initGui() {
      super.initGui();
      this.guiLeft = (this.width - 170) / 2;
      this.guiTop = (this.height - 110) / 2;
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();
    }

    protected void actionPerformed(GuiButton button) {
      GctAllNetwork.CHANNEL.sendToServer(new GuiEarthbound.GUIButtonPressedMessage(button.id, this.x, this.y, this.z));
      GuiEarthbound.handleButtonAction(this.entity, button.id, this.x, this.y, this.z);
    }

    public boolean doesGuiPauseGame() {
      return false;
    }
  }

  public static class GUIButtonPressedMessageHandler implements IMessageHandler<GUIButtonPressedMessage, IMessage> {
    public IMessage onMessage(GuiEarthbound.GUIButtonPressedMessage message, MessageContext context) {
      EntityPlayerMP entity = (context.getServerHandler()).player;
      entity.getServerWorld().addScheduledTask(() -> {
            int buttonID = message.buttonID;
            int x = message.x;
            int y = message.y;
            int z = message.z;
            GuiEarthbound.handleButtonAction((EntityPlayer)entity, buttonID, x, y, z);
          });
      return null;
    }
  }

  public static class GUISlotChangedMessageHandler implements IMessageHandler<GUISlotChangedMessage, IMessage> {
    public IMessage onMessage(GuiEarthbound.GUISlotChangedMessage message, MessageContext context) {
      EntityPlayerMP entity = (context.getServerHandler()).player;
      entity.getServerWorld().addScheduledTask(() -> {
            int slotID = message.slotID;
            int changeType = message.changeType;
            int meta = message.meta;
            int x = message.x;
            int y = message.y;
            int z = message.z;
            GuiEarthbound.handleSlotAction((EntityPlayer)entity, slotID, changeType, meta, x, y, z);
          });
      return null;
    }
  }

  public static class GUIButtonPressedMessage implements IMessage {
    int buttonID;

    int x;

    int y;

    int z;

    public GUIButtonPressedMessage() {}

    public GUIButtonPressedMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
    }

    public void toBytes(ByteBuf buf) {
      buf.writeInt(this.buttonID);
      buf.writeInt(this.x);
      buf.writeInt(this.y);
      buf.writeInt(this.z);
    }

    public void fromBytes(ByteBuf buf) {
      this.buttonID = buf.readInt();
      this.x = buf.readInt();
      this.y = buf.readInt();
      this.z = buf.readInt();
    }
  }

  public static class GUISlotChangedMessage implements IMessage {
    int slotID;

    int x;

    int y;

    int z;

    int changeType;

    int meta;

    public GUISlotChangedMessage() {}

    public GUISlotChangedMessage(int slotID, int x, int y, int z, int changeType, int meta) {
      this.slotID = slotID;
      this.x = x;
      this.y = y;
      this.z = z;
      this.changeType = changeType;
      this.meta = meta;
    }

    public void toBytes(ByteBuf buf) {
      buf.writeInt(this.slotID);
      buf.writeInt(this.x);
      buf.writeInt(this.y);
      buf.writeInt(this.z);
      buf.writeInt(this.changeType);
      buf.writeInt(this.meta);
    }

    public void fromBytes(ByteBuf buf) {
      this.slotID = buf.readInt();
      this.x = buf.readInt();
      this.y = buf.readInt();
      this.z = buf.readInt();
      this.changeType = buf.readInt();
      this.meta = buf.readInt();
    }
  }

  private static void handleButtonAction(EntityPlayer entity, int buttonID, int x, int y, int z) {
    World world = entity.world;
    if (!world.isBlockLoaded(new BlockPos(x, y, z)))
      return;
  }

  private static void handleSlotAction(EntityPlayer entity, int slotID, int changeType, int meta, int x, int y, int z) {
    World world = entity.world;
    if (!world.isBlockLoaded(new BlockPos(x, y, z)))
      return;
  }
}

