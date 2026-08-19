package com.smd.gctcore.common.tile.blood_altar;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.smd.gctcore.Tags;
import com.smd.gctcore.common.network.GctNetworkHandler;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.client.ControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.gui.GuiContainerBase;
import hellfirepvp.modularmachinery.common.container.ContainerFactoryController;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Blood Altar's factory GUI.  Its dimensions and queue layout mirror the
 * {@code mmce_gui_ext.factoryController} section of the original JSON while
 * using GCT-Core-owned textures and native MMCE GUI events.
 */
public class GuiBloodAltarController extends GuiContainerBase<ContainerFactoryController> {
    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 213;
    private static final int QUEUE_X = 8;
    private static final int QUEUE_Y = 8;
    private static final int SCROLLBAR_X = 94;
    private static final int SCROLLBAR_Y = 8;
    // The JSON requests seven rows, but its 213px-high background only has
    // room for six 32px rows (including the one-pixel separators). MMCEGE
    // clamps the requested value in exactly this way before it renders.
    private static final int REQUESTED_QUEUE_ROWS = 7;
    private static final int THREAD_ROW_WIDTH = 86;
    private static final int THREAD_ROW_HEIGHT = 32;
    private static final int CORE_THREAD_COLOR = 0xFFB2E5FF;
    private static final double FONT_SCALE = 0.72D;
    private static final int INFO_X = 113;
    private static final int INFO_Y = 12;
    private static final int INFO_WIDTH = 135;
    // MMCEGE's default factory information panel is 112px high and leaves a
    // two-pixel inset for its scissor rectangle and scrollbar.
    private static final int INFO_PANEL_HEIGHT = 112;
    private static final int MODE_BUTTON_ID = 0;

    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            Tags.MOD_ID, "textures/gui/mmce/bloodaltar/bloodaltargui.png");
    private static final ResourceLocation BUTTON = new ResourceLocation(
            Tags.MOD_ID, "textures/gui/mmce/bloodaltar/button.png");
    private static final ResourceLocation BUTTON_PRESSED = new ResourceLocation(
            Tags.MOD_ID, "textures/gui/mmce/bloodaltar/button_1.png");
    private static final ResourceLocation FACTORY_ELEMENTS = new ResourceLocation(
            ModularMachinery.MODID, "textures/gui/guifactoryelements.png");
    private static final ResourceLocation MMCE_SCROLLBAR_TEXTURE = new ResourceLocation(
            "minecraft", "textures/gui/container/creative_inventory/tabs.png");

    private static final int[] ORB_CAPACITY = {
            0, 5000, 25000, 150000, 1000000, 10000000, 30000000, 80000000, 200000000
    };
    private static final int[] LEVEL_SPEED_MULTIPLIER = {
            0, 1, 1, 2, 4, 20, 80, 400, 2000, 10000
    };
    private static final IEventHandler<ControllerGUIRenderEvent> INFO_EVENT_HANDLER =
            GuiBloodAltarController::appendBloodAltarInfo;

    /**
     * The native MMCE scrollbar only renders a 12x15 texture thumb.  The
     * factory GUI background already contains a grey strip at this position,
     * which makes the native widget effectively invisible with this texture.
     * MMCEGE's panel scrollbar draws its own track and thumb, so do the same
     * here and keep the scroll state in this GUI.
     */
    private final RecipeScrollbar scrollbar = new RecipeScrollbar();
    private final InfoScrollbar infoScrollbar = new InfoScrollbar();
    private final BloodAltarFactoryController factory;
    /** True while the left mouse button is dragging the recipe scrollbar. */
    private boolean draggingScrollbar;
    private boolean draggingInfoScrollbar;
    private ModeButton modeButton;

    public GuiBloodAltarController(final BloodAltarFactoryController factory, final EntityPlayer player) {
        super(new ContainerFactoryController(factory, player));
        this.factory = factory;
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();
        ensureInfoEventHandler(factory.getFoundMachine());
        updateScrollbar();
        draggingScrollbar = false;
        draggingInfoScrollbar = false;
        modeButton = new ModeButton(guiLeft + 255, guiTop + 111);
        buttonList.add(modeButton);
    }

    @Override
    protected void setWidthHeight() {
        // The original MMCE factory controller dimensions are fixed at 280x213.
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        Gui.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0,
                GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        updateScrollbar();
        scrollbar.draw(this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        drawRecipeQueue();
        drawInformationFromEvent();
    }

    private void drawRecipeQueue() {
        final Collection<FactoryRecipeThread> coreThreads = factory.getCoreRecipeThreads().values();
        final List<FactoryRecipeThread> regularThreads = factory.getFactoryRecipeThreadList();
        final List<FactoryRecipeThread> threads = new ArrayList<>((int) ((coreThreads.size() + regularThreads.size()) * 1.5D));
        threads.addAll(coreThreads);
        threads.addAll(regularThreads);

        final int first = scrollbar.getCurrentScroll();
        final int count = Math.min(getVisibleQueueRows(), Math.max(0, threads.size() - first));
        int y = QUEUE_Y;
        for (int index = 0; index < count; index++) {
            drawRecipeInfo(threads.get(first + index), first + index, QUEUE_X, y);
            y += THREAD_ROW_HEIGHT + 1;
        }
    }

    private void drawRecipeInfo(final FactoryRecipeThread thread, final int id, final int x, final int y) {
        final CraftingStatus status = thread.getStatus();
        final ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();

        mc.getTextureManager().bindTexture(FACTORY_ELEMENTS);
        if (thread.isCoreThread()) {
            applyColor(CORE_THREAD_COLOR);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        drawTexturedModalRect(x, y, 0, 0, THREAD_ROW_WIDTH, THREAD_ROW_HEIGHT);

        if (status.isCrafting()) {
            GlStateManager.color(0.6F, 1.0F, 0.75F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 0.6F, 0.6F, 1.0F);
        }
        if (activeRecipe != null && activeRecipe.getTotalTick() > 0) {
            final float progress = (float) activeRecipe.getTick() / (float) activeRecipe.getTotalTick();
            drawTexturedModalRect(x, y, 0, 0, (int) (THREAD_ROW_WIDTH * progress), THREAD_ROW_HEIGHT);
        }

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.scale(FONT_SCALE, FONT_SCALE, FONT_SCALE);
        final int textX = (int) (x / FONT_SCALE) + 2;
        int textY = (int) ((y + 2) / FONT_SCALE);
        final int parallelism = activeRecipe == null ? 1 : activeRecipe.getParallelism();
        final String threadName = thread.isCoreThread()
                ? localizeThreadName(thread.getThreadName())
                : I18n.format("gui.factory.thread", id);
        final String displayedName = parallelism > 1
                ? I18n.format("gui.gctcore.blood_altar.thread.parallel", threadName,
                        I18n.format("gui.controller.parallelism", parallelism))
                : threadName;
        fontRenderer.drawString(displayedName, textX, textY, 0x222222);
        textY += 12;

        for (String line : fontRenderer.listFormattedStringToWidth(
                I18n.format(status.getUnlocMessage()), (int) ((THREAD_ROW_WIDTH - 6) / FONT_SCALE))) {
            if (!fitsRecipeText(textY, y)) {
                break;
            }
            fontRenderer.drawString(line, textX, textY, 0x222222);
            textY += 10;
        }
        if (activeRecipe != null && activeRecipe.getTotalTick() > 0) {
            final int progress = activeRecipe.getTick() * 100 / activeRecipe.getTotalTick();
            if (fitsRecipeText(textY, y)) {
                fontRenderer.drawString(I18n.format("gui.controller.status.crafting.progress", progress + "%"),
                        textX, textY, 0x222222);
            }
        }
        GlStateManager.popMatrix();
    }

    /** Coordinates passed to FontRenderer are in the scaled text space. */
    private static boolean fitsRecipeText(final int scaledY, final int rowY) {
        return (scaledY + 9) * FONT_SCALE <= rowY + THREAD_ROW_HEIGHT - 1;
    }

    private void drawInformationFromEvent() {
        ensureInfoEventHandler(factory.getFoundMachine());
        final ControllerGUIRenderEvent event = new ControllerGUIRenderEvent(factory);
        event.postEvent();

        final int lineHeight = Math.max(1, MathHelper.ceil((fontRenderer.FONT_HEIGHT + 2) * FONT_SCALE));
        final int wrapWidth = MathHelper.floor((INFO_WIDTH - 8) / FONT_SCALE);
        final List<String> lines = new ArrayList<>();
        for (String entry : event.getExtraInfo()) {
            lines.addAll(fontRenderer.listFormattedStringToWidth(entry, wrapWidth));
        }
        final int viewportHeight = Math.max(1, INFO_PANEL_HEIGHT - 4);
        final int contentHeight = lines.size() * lineHeight;
        infoScrollbar.setBounds(INFO_X + INFO_WIDTH - 4, INFO_Y + 1, 3, INFO_PANEL_HEIGHT - 2);
        infoScrollbar.setRange(Math.max(0, contentHeight - viewportHeight));

        // MMCEGE clips panel text with an OpenGL scissor rectangle.  This is
        // important here because the inventory slots begin immediately below
        // the information panel and must never be covered by status text.
        enableScissor(guiLeft + INFO_X + 1, guiTop + INFO_Y + 1,
                INFO_WIDTH - 2, INFO_PANEL_HEIGHT - 2);
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.scale(FONT_SCALE, FONT_SCALE, FONT_SCALE);
        int y = (int) ((INFO_Y + 2 - infoScrollbar.getCurrentScroll()) / FONT_SCALE);
        final int x = (int) ((INFO_X + 3) / FONT_SCALE);
        for (String line : lines) {
            fontRenderer.drawStringWithShadow(line, x, y, 0xFFFFFF);
            y += fontRenderer.FONT_HEIGHT + 2;
        }
        GlStateManager.popMatrix();
        disableScissor();
        infoScrollbar.draw();
    }

    private void updateScrollbar() {
        final int total = factory.getCoreRecipeThreads().size() + factory.getFactoryRecipeThreadList().size();
        final int visibleRows = getVisibleQueueRows();
        scrollbar.setBounds(guiLeft + SCROLLBAR_X, guiTop + SCROLLBAR_Y, 12,
                Math.max(THREAD_ROW_HEIGHT, visibleRows * (THREAD_ROW_HEIGHT + 1) - 1));
        scrollbar.setRange(0, Math.max(0, total - visibleRows));
    }

    private void enableScissor(final int x, final int y, final int width, final int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        final ScaledResolution resolution = new ScaledResolution(mc);
        final int scale = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, mc.displayHeight - (y + height) * scale,
                width * scale, height * scale);
    }

    private static void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /**
     * Mirrors MMCEGE's effective-row calculation for the factory GUI. This
     * keeps every queue entry and the scroll bar inside bloodaltargui.png
     * while preserving the JSON's requested seven-row layout.
     */
    private static int getVisibleQueueRows() {
        final int availableHeight = Math.max(THREAD_ROW_HEIGHT, GUI_HEIGHT - QUEUE_Y - 8);
        final int rowsThatFit = Math.max(1, (availableHeight + 1) / (THREAD_ROW_HEIGHT + 1));
        return Math.min(REQUESTED_QUEUE_ROWS, rowsThatFit);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        final int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            final int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            final int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            if (infoScrollbar.isPanelHovered(mouseX, mouseY, guiLeft, guiTop)) {
                infoScrollbar.wheel(wheel);
            } else {
                scrollbar.wheel(wheel);
            }
        }
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        // Keep the drag state here so a drag remains active when the cursor
        // briefly leaves the 12px track, matching MMCEGE's panel behavior.
        if (mouseButton == 0 && infoScrollbar.mouseClicked(mouseX, mouseY, guiLeft, guiTop)) {
            draggingInfoScrollbar = true;
            draggingScrollbar = false;
        } else if (mouseButton == 0 && scrollbar.mouseClicked(mouseX, mouseY)) {
            draggingScrollbar = true;
            draggingInfoScrollbar = false;
        } else if (mouseButton == 0) {
            draggingScrollbar = false;
            draggingInfoScrollbar = false;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(final int mouseX, final int mouseY, final int clickedMouseButton,
                                  final long timeSinceLastClick) {
        if (clickedMouseButton == 0 && draggingInfoScrollbar) {
            infoScrollbar.dragTo(mouseY, guiLeft, guiTop);
        } else if (clickedMouseButton == 0 && draggingScrollbar) {
            // Clamp the pointer so dragging outside the track still reaches
            // the first/last queue entry and never loses the active drag.
            final int clampedY = MathHelper.clamp(mouseY, scrollbar.getTop(),
                    scrollbar.getTop() + scrollbar.getHeight());
            scrollbar.dragTo(clampedY);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        if (state == 0) {
            draggingScrollbar = false;
            scrollbar.release();
            draggingInfoScrollbar = false;
            infoScrollbar.release();
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button.id == MODE_BUTTON_ID) {
            GctNetworkHandler.CHANNEL.sendToServer(new BloodAltarFactoryController.ModePacket(factory.getPos()));
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (modeButton != null && modeButton.isMouseOver()) {
            drawHoveringText(I18n.format("gui.gctcore.blood_altar.mode_button"), mouseX, mouseY);
        }
    }

    private static void ensureInfoEventHandler(@Nullable final DynamicMachine machine) {
        if (!BloodAltarMachine.isBloodAltar(machine)) {
            return;
        }
        final List<?> handlers = machine.getMachineEventHandlers(ControllerGUIRenderEvent.class);
        if (handlers == null || !handlers.contains(INFO_EVENT_HANDLER)) {
            machine.addMachineEventHandler(ControllerGUIRenderEvent.class, INFO_EVENT_HANDLER);
        }
    }

    /** Uses MMCE's ControllerGUIRenderEvent instead of a CraftTweaker callback. */
    private static void appendBloodAltarInfo(final ControllerGUIRenderEvent event) {
        if (!(event.getController() instanceof BloodAltarFactoryController)) {
            return;
        }
        final BloodAltarFactoryController controller = (BloodAltarFactoryController) event.getController();
        final NBTTagCompound data = controller.getCustomDataTag();
        final int level = getInt(data, BloodAltarMachine.DATA_LEVEL);
        final int mode = getInt(data, BloodAltarMachine.DATA_MODE);
        final int speed = getInt(data, BloodAltarMachine.DATA_SPEED);
        final BigInteger capacity = getBigInteger(data, BloodAltarMachine.DATA_CAPACITY);
        final BigInteger lifeEssence = getBigInteger(data, BloodAltarMachine.DATA_LIFE_ESSENCE);

        final int speedRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_SPEED);
        final int efficiencyRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_EFFICIENCY);
        final int dislocationRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_DISLOCATION);
        final int augmentedCapacityRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_AUGMENTED_CAPACITY);
        final int capacityRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_CAPACITY);
        final int orbRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_ORB);
        final int accelerationRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_ACCELERATION);
        final int threadRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_THREAD);
        final int economyRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_ECONOMY);
        final int purificationRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_PURIFICATION);
        final int personalRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_PERSONAL);
        final int sacrificeRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_SACRIFICE);
        final int selfSacrificeRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_SELF_SACRIFICE);
        final int checkTime = Math.max(1, 20 - accelerationRunes);
        final int multiplier = level >= 0 && level < LEVEL_SPEED_MULTIPLIER.length
                ? LEVEL_SPEED_MULTIPLIER[level] : 0;
        final BigInteger workSpeed = BigInteger.valueOf(speed).multiply(BigInteger.valueOf(multiplier));
        final BigInteger transferSpeed = new BigDecimal("1.2").pow(Math.max(0, dislocationRunes))
                .multiply(BigDecimal.valueOf(20L)).setScale(0, RoundingMode.DOWN).toBigInteger();

        final List<String> info = new ArrayList<>();
        info.add(I18n.format("gui.gctcore.blood_altar.header"));
        info.add(I18n.format("gui.gctcore.blood_altar.machine", I18n.format("gui.gctcore.blood_altar.machine_name")));
        info.add(I18n.format("gui.gctcore.blood_altar.level", localizeIndexed("gui.gctcore.blood_altar.level", level)));
        info.add(I18n.format("gui.gctcore.blood_altar.capacity", comma(capacity)));
        info.add(I18n.format("gui.gctcore.blood_altar.stored", comma(lifeEssence)));
        info.add(I18n.format("gui.gctcore.blood_altar.mode", localizeIndexed("gui.gctcore.blood_altar.mode", mode)));
        info.add(I18n.format("gui.gctcore.blood_altar.work_speed", comma(workSpeed), checkTime));
        info.add(I18n.format("gui.gctcore.blood_altar.transfer_speed", comma(transferSpeed), checkTime));
        // Two rune types per line keep the information panel compact and make
        // each count readable without relying on FontRenderer line wrapping.
        final int baseRunes = controller.getBlocksInPattern(BloodAltarMachine.RUNE_BASE);
        info.add(formatRunePair("base", baseRunes, "augmented_capacity", augmentedCapacityRunes));
        info.add(formatRunePair("speed", speedRunes, "capacity", capacityRunes));
        info.add(formatRunePair("dislocation", dislocationRunes, "acceleration", accelerationRunes));
        info.add(formatRunePair("efficiency", efficiencyRunes, "thread", threadRunes));
        info.add(formatRunePair("economy", economyRunes, "purification", purificationRunes));
        info.add(formatRunePair("orb", orbRunes, "personal", personalRunes));
        info.add(formatRunePair("sacrifice", sacrificeRunes, "self_sacrifice", selfSacrificeRunes));
        appendPlayerNetworkInfo(info, controller, lifeEssence, speedRunes, dislocationRunes,
                orbRunes, accelerationRunes, personalRunes);
        event.setExtraInfo(info.toArray(new String[0]));
    }

    private static void appendPlayerNetworkInfo(final List<String> info,
                                                final BloodAltarFactoryController controller,
                                                final BigInteger storedLifeEssence,
                                                final int speedRunes, final int dislocationRunes,
                                                final int orbRunes, final int accelerationRunes,
                                                final int personalRunes) {
        // These status rows are deliberately permanent.  Querying by UUID is
        // also valid while the owner is offline, unlike getPlayerEntityByUUID.
        // A just-placed, unowned controller displays zero-capacity network
        // values until it receives an owner instead of dropping the rows.
        final UUID owner = controller.getOwner();
        final SoulNetwork network = owner == null ? null : NetworkHelper.getSoulNetwork(owner);

        final int tier = network == null ? 0
                : MathHelper.clamp(network.getOrbTier(), 0, ORB_CAPACITY.length - 1);
        final BigDecimal maxCapacityDecimal = BigDecimal.valueOf(ORB_CAPACITY[tier])
                .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.02D).multiply(BigDecimal.valueOf(orbRunes))));
        final BigDecimal maxTransferDecimal = BigDecimal.valueOf(20L)
                .multiply(BigDecimal.valueOf(1L + Math.min(19, Math.max(0, accelerationRunes))))
                .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(speedRunes).divide(BigDecimal.valueOf(5L))))
                .multiply(new BigDecimal("1.2").pow(Math.max(0, dislocationRunes)))
                .multiply(BigDecimal.valueOf(2L).pow(Math.max(0, personalRunes)));

        final boolean transferOverflow = maxTransferDecimal.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0;
        final boolean capacityOverflow = maxCapacityDecimal.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0;
        final BigInteger maxTransfer = transferOverflow ? BigInteger.valueOf(Integer.MAX_VALUE)
                : maxTransferDecimal.setScale(0, RoundingMode.DOWN).toBigInteger();
        final BigInteger maxCapacity = capacityOverflow ? BigInteger.valueOf(Integer.MAX_VALUE)
                : maxCapacityDecimal.setScale(0, RoundingMode.DOWN).toBigInteger();
        final BigInteger playerLifeEssence = network == null ? BigInteger.ZERO
                : BigInteger.valueOf(network.getCurrentEssence());
        final BigInteger actualTransfer = storedLifeEssence.max(BigInteger.ZERO)
                .min(maxTransfer).min(maxCapacity.subtract(playerLifeEssence).max(BigInteger.ZERO));

        if (transferOverflow) {
            info.add(I18n.format("gui.gctcore.blood_altar.warning.transfer_overflow"));
        }
        if (capacityOverflow) {
            info.add(I18n.format("gui.gctcore.blood_altar.warning.capacity_overflow"));
        }
        if (playerLifeEssence.compareTo(maxCapacity) > 0) {
            info.add(I18n.format("gui.gctcore.blood_altar.warning.player_network_full"));
        }
        info.add(I18n.format("gui.gctcore.blood_altar.player.max_transfer", comma(maxTransfer)));
        info.add(I18n.format("gui.gctcore.blood_altar.player.actual_transfer", comma(actualTransfer)));
        info.add(I18n.format("gui.gctcore.blood_altar.player.network", comma(maxCapacity), comma(playerLifeEssence)));
    }

    private static int getInt(@Nullable final NBTTagCompound data, final String key) {
        return data == null ? 0 : data.getInteger(key);
    }

    private static BigInteger getBigInteger(@Nullable final NBTTagCompound data, final String key) {
        if (data == null || !data.hasKey(key)) {
            return BigInteger.ZERO;
        }
        try {
            return new BigInteger(data.getString(key));
        } catch (NumberFormatException ignored) {
            return BigInteger.ZERO;
        }
    }

    private static String comma(final BigInteger value) {
        final String number = value.toString();
        final int start = number.startsWith("-") ? 1 : 0;
        final StringBuilder result = new StringBuilder(number.length() + number.length() / 3);
        if (start == 1) {
            result.append('-');
        }
        for (int index = start; index < number.length(); index++) {
            if (index > start && (number.length() - index) % 3 == 0) {
                result.append(',');
            }
            result.append(number.charAt(index));
        }
        return result.toString();
    }

    private static String localizeThreadName(final String key) {
        return I18n.hasKey(key) ? I18n.format(key) : key;
    }

    private static String formatRunePair(final String firstKey, final int firstCount,
                                         final String secondKey, final int secondCount) {
        return I18n.format("gui.gctcore.blood_altar.runes.pair",
                I18n.format("gui.gctcore.blood_altar.runes." + firstKey), firstCount,
                I18n.format("gui.gctcore.blood_altar.runes." + secondKey), secondCount);
    }

    private static String localizeIndexed(final String baseKey, final int index) {
        final String key = baseKey + '.' + index;
        return I18n.hasKey(key) ? I18n.format(key) : I18n.format(baseKey + ".unknown");
    }

    private static void applyColor(final int color) {
        GlStateManager.color(
                ((color >>> 16) & 0xFF) / 255.0F,
                ((color >>> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                ((color >>> 24) & 0xFF) / 255.0F);
    }

    /** MMCEGE-style information-panel scrollbar (coordinates are GUI-local). */
    private static final class InfoScrollbar {
        private int left;
        private int top;
        private int width;
        private int height;
        private int maxScroll;
        private int currentScroll;
        private int dragOffset;
        private boolean pressed;

        private void setBounds(final int left, final int top, final int width, final int height) {
            this.left = left;
            this.top = top;
            this.width = Math.max(1, width);
            this.height = Math.max(1, height);
        }

        private void setRange(final int maxScroll) {
            this.maxScroll = Math.max(0, maxScroll);
            this.currentScroll = MathHelper.clamp(this.currentScroll, 0, this.maxScroll);
        }

        private int getCurrentScroll() {
            return currentScroll;
        }

        private void wheel(final int delta) {
            // MMCE's wheel delta is normalized to one unit.  Move two pixels
            // per unit for the requested 2x information-panel sensitivity.
            final int step = MathHelper.clamp(-delta, -1, 1) * 2;
            currentScroll = MathHelper.clamp(currentScroll + step, 0, maxScroll);
        }

        private boolean isPanelHovered(final int mouseX, final int mouseY,
                                       final int guiLeft, final int guiTop) {
            final int localX = mouseX - guiLeft;
            final int localY = mouseY - guiTop;
            return localX >= INFO_X && localX < INFO_X + INFO_WIDTH
                    && localY >= INFO_Y && localY < INFO_Y + INFO_PANEL_HEIGHT;
        }

        private boolean mouseClicked(final int mouseX, final int mouseY,
                                     final int guiLeft, final int guiTop) {
            if (maxScroll <= 0) {
                return false;
            }
            final int localX = mouseX - guiLeft;
            final int localY = mouseY - guiTop;
            final int thumbHeight = getThumbHeight();
            final int thumbTop = getThumbTop(thumbHeight);
            if (localX < left || localX >= left + width
                    || localY < thumbTop || localY >= thumbTop + thumbHeight
                    || localY < top || localY >= top + height) {
                return false;
            }
            dragOffset = localY - thumbTop;
            pressed = true;
            return true;
        }

        private void dragTo(final int mouseY, final int guiLeft, final int guiTop) {
            if (!pressed || maxScroll <= 0) {
                return;
            }
            final int localY = mouseY - guiTop;
            final int thumbHeight = getThumbHeight();
            final int maxTravel = Math.max(1, height - thumbHeight);
            final int thumbTop = MathHelper.clamp(localY - dragOffset, top, top + maxTravel);
            currentScroll = MathHelper.clamp(
                    ((thumbTop - top) * maxScroll) / maxTravel, 0, maxScroll);
        }

        private void release() {
            pressed = false;
            dragOffset = 0;
        }

        private void draw() {
            if (maxScroll <= 0) {
                return;
            }
            final int thumbHeight = getThumbHeight();
            final int thumbTop = getThumbTop(thumbHeight);
            // Geometry follows GuiFactoryControllerResizable.drawPanelScrollBar().
            // The alpha is deliberately lower than MMCEGE's default so this
            // narrow control does not dominate the altar information panel.
            Gui.drawRect(left, top, left + width, top + height, 0x30000000);
            Gui.drawRect(left, thumbTop, left + width, thumbTop + thumbHeight,
                    pressed ? 0xB0FFFFFF : 0x90FFFFFF);
        }

        private int getThumbHeight() {
            return Math.max(12, (height * height) / (height + maxScroll + 1));
        }

        private int getThumbTop(final int thumbHeight) {
            final int maxTravel = Math.max(1, height - thumbHeight);
            return top + (currentScroll * maxTravel) / Math.max(1, maxScroll);
        }
    }

    /**
     * Small self-contained scrollbar following MMCEGE's panel scrollbar
     * model: a visible track, a fixed-size thumb, an integer row range and
     * explicit press/drag/release handling.  Coordinates are screen absolute,
     * exactly like GuiScreen mouse callbacks.
     */
    private static final class RecipeScrollbar {
        private int left;
        private int top;
        private int width = 12;
        private int height = 197;
        private int minScroll;
        private int maxScroll;
        private int currentScroll;
        private int dragOffset;
        private boolean pressed;

        private void setBounds(final int left, final int top, final int width, final int height) {
            this.left = left;
            this.top = top;
            this.width = Math.max(4, width);
            this.height = Math.max(15, height);
        }

        private void setRange(final int min, final int max) {
            this.minScroll = min;
            this.maxScroll = Math.max(min, max);
            this.currentScroll = MathHelper.clamp(this.currentScroll, this.minScroll, this.maxScroll);
        }

        private int getCurrentScroll() {
            return currentScroll;
        }

        private int getTop() {
            return top;
        }

        private int getHeight() {
            return height;
        }

        private void wheel(final int delta) {
            int step = MathHelper.clamp(-delta, -1, 1);
            currentScroll = MathHelper.clamp(currentScroll + step, minScroll, maxScroll);
        }

        private boolean mouseClicked(final int mouseX, final int mouseY) {
            if (!contains(mouseX, mouseY) || maxScroll <= minScroll) {
                return false;
            }
            final int thumbHeight = getThumbHeight();
            final int thumbTop = getThumbTop(thumbHeight);
            if (mouseY >= thumbTop && mouseY <= thumbTop + thumbHeight) {
                dragOffset = mouseY - thumbTop;
            } else {
                // Clicking the track moves the thumb toward the click, as in
                // MMCEGE's panel scrollbar, then starts a drag immediately.
                dragOffset = thumbHeight / 2;
                setCurrentFromThumbTop(mouseY - dragOffset, thumbHeight);
            }
            pressed = true;
            return true;
        }

        private void dragTo(final int mouseY) {
            if (!pressed || maxScroll <= minScroll) {
                return;
            }
            setCurrentFromThumbTop(mouseY - dragOffset, getThumbHeight());
        }

        private void release() {
            pressed = false;
            dragOffset = 0;
        }

        private void draw(final GuiBloodAltarController gui) {
            final int thumbHeight = getThumbHeight();
            final int thumbTop = getThumbTop(thumbHeight);
            // Explicit contrast is intentional: the blood altar texture's
            // built-in strip otherwise hides MMCE's 12x15 texture thumb.
            Gui.drawRect(left, top, left + width, top + height, 0x70000000);
            // Keep the same creative-inventory thumb texture used by MMCE's
            // GuiScrollbar: U=232 is enabled, U=244 is disabled.
            gui.mc.getTextureManager().bindTexture(MMCE_SCROLLBAR_TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, pressed ? 1.0F : 0.88F);
            gui.drawTexturedModalRect(left, thumbTop,
                    maxScroll > minScroll ? 232 : 244, 0, width, thumbHeight);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        private boolean contains(final int mouseX, final int mouseY) {
            return mouseX >= left && mouseX < left + width
                    && mouseY >= top && mouseY <= top + height;
        }

        private int getThumbHeight() {
            return Math.min(15, height);
        }

        private int getThumbTop(final int thumbHeight) {
            final int travel = Math.max(0, height - thumbHeight);
            if (maxScroll <= minScroll || travel == 0) {
                return top;
            }
            return top + (currentScroll - minScroll) * travel / (maxScroll - minScroll);
        }

        private void setCurrentFromThumbTop(final int requestedTop, final int thumbHeight) {
            final int travel = Math.max(0, height - thumbHeight);
            final int clampedTop = MathHelper.clamp(requestedTop, top, top + travel);
            if (travel == 0 || maxScroll <= minScroll) {
                currentScroll = minScroll;
                return;
            }
            currentScroll = minScroll + Math.round(
                    (clampedTop - top) * (maxScroll - minScroll) / (float) travel);
            currentScroll = MathHelper.clamp(currentScroll, minScroll, maxScroll);
        }
    }

    private final class ModeButton extends GuiButton {
        private ModeButton(final int x, final int y) {
            super(MODE_BUTTON_ID, x, y, 18, 18, "");
        }

        @Override
        public void drawButton(final net.minecraft.client.Minecraft minecraft, final int mouseX,
                               final int mouseY, final float partialTicks) {
            if (!visible) {
                return;
            }
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            minecraft.getTextureManager().bindTexture(hovered && Mouse.isButtonDown(0) ? BUTTON_PRESSED : BUTTON);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        }
    }
}
