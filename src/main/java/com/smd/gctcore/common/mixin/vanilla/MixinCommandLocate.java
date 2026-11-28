package com.smd.gctcore.common.mixin.vanilla;

import com.smd.gctcore.common.world.jotunheim.WorldProviderJotunheim;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandLocate;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/** Expose the dimension's structures in the existing /locate command. */
@Mixin(CommandLocate.class)
public abstract class MixinCommandLocate {
    @Inject(method = "getTabCompletions", at = @At("RETURN"), cancellable = true)
    private void gctcore$jotunheimStructures(MinecraftServer server, ICommandSender sender,
                                            String[] args, BlockPos targetPos,
                                            CallbackInfoReturnable<List<String>> cir) {
        if (args.length != 1 || !(sender.getEntityWorld().provider instanceof WorldProviderJotunheim)) return;
        List<String> completions = new ArrayList<>(cir.getReturnValue());
        for (String name : CommandBase.getListOfStringsMatchingLastWord(args, "CyclopsCave", "YmirHell")) {
            if (!completions.contains(name)) completions.add(name);
        }
        cir.setReturnValue(completions);
    }
}
