package com.smd.gctcore.common.integration.extendedcrafting;

import com.smd.gctcore.client.extendedcrafting.GuiExtendedMolecularAssembler;
import com.smd.gctcore.client.extendedcrafting.GuiExtendedInterface;
import com.smd.gctcore.client.extendedcrafting.GuiExtendedPatternTerminal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public final class ExtendedCraftingGuiHandler implements IGuiHandler {
    private static final int TERMINAL_BASE = 120;
    private static final int INTERFACE_BASE = 130;
    private static final int ASSEMBLER_BASE = 140;

    public static int terminalId(ExtendedCraftingTier tier) { return TERMINAL_BASE + tier.level(); }
    public static int interfaceId(ExtendedCraftingTier tier) { return INTERFACE_BASE + tier.level(); }
    public static int assemblerId(ExtendedCraftingTier tier) { return ASSEMBLER_BASE + tier.level(); }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id > TERMINAL_BASE && id <= TERMINAL_BASE + 4 && tile instanceof TileExtendedPatternTerminal)
            return new ContainerExtendedPatternTerminal(player.inventory, (TileExtendedPatternTerminal) tile);
        if (id > INTERFACE_BASE && id <= INTERFACE_BASE + 4 && tile instanceof TileExtendedInterface)
            return new ContainerExtendedInterface(player.inventory, (TileExtendedInterface) tile);
        if (id > ASSEMBLER_BASE && id <= ASSEMBLER_BASE + 4
                && tile instanceof TileExtendedMolecularAssembler)
            return new ContainerExtendedMolecularAssembler(player.inventory,
                    (TileExtendedMolecularAssembler) tile);
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Object container = getServerGuiElement(id, player, world, x, y, z);
        if (container instanceof ContainerExtendedPatternTerminal)
            return new GuiExtendedPatternTerminal((ContainerExtendedPatternTerminal) container);
        if (container instanceof ContainerExtendedInterface)
            return new GuiExtendedInterface((ContainerExtendedInterface) container);
        if (container instanceof ContainerExtendedMolecularAssembler)
            return new GuiExtendedMolecularAssembler(
                    (ContainerExtendedMolecularAssembler) container);
        return null;
    }
}
