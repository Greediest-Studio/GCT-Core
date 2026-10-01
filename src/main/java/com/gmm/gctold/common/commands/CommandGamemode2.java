package com.gmm.gctold.common.commands;

import java.util.Collections;
import java.util.List;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;

public class CommandGamemode2 implements ICommand {
    @Override
    public int compareTo(ICommand command) {
        return getName().compareTo(command.getName());
    }

    @Override
    public String getName() {
        return "gm2";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/gm2";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        Entity entity = sender.getCommandSenderEntity();
        if (entity == null) {
            return;
        }

        if (entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).setGameType(GameType.ADVENTURE);
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
}
