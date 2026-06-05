package com.smd.gctcore.common.items;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.smd.gctcore.Tags;
import com.smd.gctcore.common.integration.mmce.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MMCE_BuilderTool extends Item implements IGuiHolder<PlayerInventoryGuiData> {

    public MMCE_BuilderTool() {
        setTranslationKey(Tags.MOD_ID + ".mmce_builder_tool");
        setRegistryName("mmce_builder_tool");
        setMaxStackSize(1);
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(World world, @NotNull EntityPlayer player, @NotNull EnumHand hand) {
        if (world.isRemote && !player.isSneaking()) {
            GuiFactories.playerInventory().openFromHandClient(hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world, BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            return EnumActionResult.PASS;
        }
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            ItemStack stack = player.getHeldItem(hand);
            MMCE_BuilderService.start((EntityPlayerMP) player, pos, MMCE_BuilderConfig.useAeItems(stack),
                    MMCE_BuilderConfig.useAeFluids(stack), MMCE_BuilderConfig.craftMissing(stack), MMCE_BuilderConfig.dynamicLength(stack),
                    MMCE_BuilderConfig.TICK_INTERVAL, MMCE_BuilderConfig.OPERATIONS_PER_TICK);
        }
        return EnumActionResult.SUCCESS;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(Tags.MOD_ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ItemStack stack = data.getUsedItemStack();
        if (data.getInventoryType() == InventoryTypes.PLAYER) {
            syncManager.bindPlayerInventory(data.getPlayer(), (inv, index) -> index == data.getSlotIndex()
                    ? new com.cleanroommc.modularui.widgets.slot.ModularSlot(inv, index).accessibility(false, false)
                    : new com.cleanroommc.modularui.widgets.slot.ModularSlot(inv, index));
        }

        ModularPanel panel = ModularPanel.defaultPanel("gct_mmce_builder", 176, 118);
        panel.child(Flow.column().margin(7).widthRel(1f).heightRel(1f)
                .child(new TextWidget<>(IKey.lang("gui.gctcore.mmce_builder.title")).height(12).widthRel(1f))
                .child(row("gui.gctcore.mmce_builder.use_ae_items", new ToggleButton()
                        .value(SyncHandlers.bool(() -> MMCE_BuilderConfig.useAeItems(stack), val -> MMCE_BuilderConfig.setUseAeItems(stack, val)))
                        .size(18, 18)
                        .overlay(false, IKey.lang("gui.gctcore.mmce_builder.off"))
                        .overlay(true, IKey.lang("gui.gctcore.mmce_builder.on"))))
                .child(row("gui.gctcore.mmce_builder.use_ae_fluids", new ToggleButton()
                        .value(SyncHandlers.bool(() -> MMCE_BuilderConfig.useAeFluids(stack), val -> MMCE_BuilderConfig.setUseAeFluids(stack, val)))
                        .size(18, 18)
                        .overlay(false, IKey.lang("gui.gctcore.mmce_builder.off"))
                        .overlay(true, IKey.lang("gui.gctcore.mmce_builder.on"))))
                .child(row("gui.gctcore.mmce_builder.craft_missing", new ToggleButton()
                        .value(SyncHandlers.bool(() -> MMCE_BuilderConfig.craftMissing(stack), val -> MMCE_BuilderConfig.setCraftMissing(stack, val)))
                        .size(18, 18)
                        .overlay(false, IKey.lang("gui.gctcore.mmce_builder.off"))
                        .overlay(true, IKey.lang("gui.gctcore.mmce_builder.on"))))
                .child(row("gui.gctcore.mmce_builder.dynamic_length", new TextFieldWidget()
                        .value(SyncHandlers.string(() -> String.valueOf(MMCE_BuilderConfig.dynamicLength(stack)), val -> {
                            try {
                                MMCE_BuilderConfig.setDynamicLength(stack, Integer.parseInt(val));
                            } catch (NumberFormatException ignored) {
                                MMCE_BuilderConfig.setDynamicLength(stack, 1);
                            }
                        }))
                        .setNumbers(0, 4096)
                        .background(GuiTextures.DISPLAY_SMALL)
                        .width(50).height(18))));
        return panel;
    }

    private Widget<?> row(String labelKey, Widget<?> control) {
        return Flow.row().widthRel(1f).height(22)
                .child(new TextWidget<>(IKey.lang(labelKey)).width(112).height(18).alignment(Alignment.CenterLeft))
                .child(control);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        tooltip.add(I18n.translateToLocal("tooltip.gctcore.mmce_builder_tool.1"));
        tooltip.add(I18n.translateToLocalFormatted("tooltip.gctcore.mmce_builder_tool.2", MMCE_BuilderConfig.useAeItems(stack) ? "True" : "False"));
        tooltip.add(I18n.translateToLocalFormatted("tooltip.gctcore.mmce_builder_tool.3", MMCE_BuilderConfig.useAeFluids(stack) ? "True" : "False"));
        tooltip.add(I18n.translateToLocalFormatted("tooltip.gctcore.mmce_builder_tool.4", MMCE_BuilderConfig.dynamicLength(stack)));
        tooltip.add(I18n.translateToLocal("tooltip.gctcore.mmce_builder_tool.5"));
    }
}
