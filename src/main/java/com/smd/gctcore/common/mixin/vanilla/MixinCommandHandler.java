package com.smd.gctcore.common.mixin.vanilla;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Wraps ToolProgressionCommand on registration because tyra314.toolprogression
 * is in FML TransformerExclusions and cannot be transformed directly by Mixin.
 * Removes the sendMessage side effect from checkPermission on dedicated servers.
 */
@Mixin(CommandHandler.class)
public abstract class MixinCommandHandler {

    @ModifyVariable(method = "registerCommand", at = @At("HEAD"), argsOnly = true)
    private ICommand gctcore$wrapToolProgressionCommand(ICommand command) {
        if (command != null && "tyra314.toolprogression.command.ToolProgressionCommand".equals(command.getClass().getName())) {
            return new CommandBase() {
                @Override
                @Nonnull
                public String getName() {
                    return command.getName();
                }

                @Override
                @Nonnull
                public String getUsage(@Nonnull ICommandSender sender) {
                    return command.getUsage(sender);
                }

                @Override
                @Nonnull
                public List<String> getAliases() {
                    return command.getAliases();
                }

                @Override
                public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
                    command.execute(server, sender, args);
                }

                @Override
                public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
                    return server.isSinglePlayer();
                }

                @Override
                @Nonnull
                public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
                    return command.getTabCompletions(server, sender, args, targetPos);
                }

                @Override
                public boolean isUsernameIndex(@Nonnull String[] args, int index) {
                    return command.isUsernameIndex(args, index);
                }

                @Override
                public int getRequiredPermissionLevel() {
                    return command instanceof CommandBase ? ((CommandBase) command).getRequiredPermissionLevel() : super.getRequiredPermissionLevel();
                }

                @Override
                public int compareTo(@Nonnull ICommand other) {
                    return command.compareTo(other);
                }
            };
        }
        return command;
    }
}
