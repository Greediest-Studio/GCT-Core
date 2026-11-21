package com.smd.gctcore.common.mixin.lostcities;

import com.smd.gctcore.common.integration.lostcities.LostCitiesStatePalette;
import net.minecraft.util.ObjectIntIdentityMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "mcjty.lostcities.dimensions.world.terraingen.LostCitiesTerrainGenerator", remap = false)
public abstract class MixinLostCitiesTerrainGenerator {

    @Redirect(
            method = {
                    "setupChars(Lmcjty/lostcities/config/LostCityProfile;)V",
                    "getDoor(Lnet/minecraft/block/Block;ZZLnet/minecraft/util/EnumFacing;)C",
                    "generateCorridors(Lmcjty/lostcities/dimensions/world/lost/BuildingInfo;ZZ)V",
                    "generatePart(Lmcjty/lostcities/dimensions/world/lost/BuildingInfo;Lmcjty/lostcities/dimensions/world/lost/cityassets/IBuildingPart;Lmcjty/lostcities/dimensions/world/lost/Transform;IIIZZZZZLmcjty/lostcities/dimensions/world/lost/Direction;)I"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ObjectIntIdentityMap;get(Ljava/lang/Object;)I",
                    remap = true
            ),
            require = 1,
            remap = false
    )
    private int gctcore$encodeBlockState(ObjectIntIdentityMap<?> map, Object value) {
        return LostCitiesStatePalette.getStateId(map, value);
    }

    @Redirect(
            method = {
                    "getCharactersNeedingTodo()Ljava/util/Set;",
                    "addStates(Lnet/minecraft/block/Block;Ljava/util/Set;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ObjectIntIdentityMap;get(Ljava/lang/Object;)I",
                    remap = true
            ),
            require = 1,
            remap = false
    )
    private static int gctcore$encodeBlockStateStatic(ObjectIntIdentityMap<?> map, Object value) {
        return LostCitiesStatePalette.getStateId(map, value);
    }

    @Redirect(
            method = {
                    "generatePart(Lmcjty/lostcities/dimensions/world/lost/BuildingInfo;Lmcjty/lostcities/dimensions/world/lost/cityassets/IBuildingPart;Lmcjty/lostcities/dimensions/world/lost/Transform;IIIZZZZZLmcjty/lostcities/dimensions/world/lost/Direction;)I",
                    "fixTorches(Lmcjty/lostcities/dimensions/world/lost/BuildingInfo;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ObjectIntIdentityMap;getByValue(I)Ljava/lang/Object;",
                    remap = true
            ),
            require = 1,
            remap = false
    )
    private Object gctcore$decodeBlockState(ObjectIntIdentityMap<?> map, int id) {
        return LostCitiesStatePalette.getStateById(map, id);
    }
}
