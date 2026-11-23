package com.smd.gctcore.common.mixin.tconstruct;

import com.smd.gctcore.common.integration.tconstruct.TConPartWhitelistHelper;
import net.minecraft.item.Item;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.tools.ranged.item.BoltCore;

/**
 * Upstream BoltCore checks Config.listAllToolMaterials instead of listAllPartMaterials.
 * Allows tconstruct:bolt_core to respect the part material whitelist when explicitly listed.
 */
@Mixin(value = BoltCore.class, remap = false)
public abstract class MixinBoltCore {

    @Redirect(
            method = "getSubItems(Lnet/minecraft/creativetab/CreativeTabs;Lnet/minecraft/util/NonNullList;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lslimeknights/tconstruct/common/config/Config;listAllToolMaterials:Z",
                    opcode = Opcodes.GETSTATIC,
                    remap = false
            ),
            remap = true,
            require = 1
    )
    private boolean gctcore$shouldListAllBoltCoreMaterials() {
        return Config.listAllToolMaterials
                || TConPartWhitelistHelper.shouldListAllMaterials((Item) (Object) this);
    }
}
