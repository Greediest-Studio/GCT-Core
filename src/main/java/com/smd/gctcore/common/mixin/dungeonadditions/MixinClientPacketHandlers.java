package com.smd.gctcore.common.mixin.dungeonadditions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = {
        "com.dungeon_additions.da.packets.PacketFalterProgress$Handler",
        "com.dungeon_additions.da.packets.PacketFalterCapability$Handler",
        "com.dungeon_additions.da.packets.PacketParryAnimationItem$Handler",
        "com.dungeon_additions.da.packets.PacketPlayerSwingAll$Handler",
        "com.dungeon_additions.da.packets.PacketSwingItem$Handler"
}, remap = false)
public abstract class MixinClientPacketHandlers {

    @WrapOperation(method = "lambda$onMessage$0", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntityByID(I)Lnet/minecraft/entity/Entity;"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;func_73045_a(I)Lnet/minecraft/entity/Entity;")
    }, remap = false, require = 1)
    private static Entity gctcore$findEntity(WorldClient world, int entityId, Operation<Entity> original) {
        return world == null ? null : original.call(world, entityId);
    }

    @WrapOperation(method = "lambda$onMessage$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;hasCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z"),
            remap = false, require = 1)
    private static boolean gctcore$hasCapability(Entity entity, Capability<?> capability, EnumFacing facing,
            Operation<Boolean> original) {
        return entity != null && original.call(entity, capability, facing);
    }
}
