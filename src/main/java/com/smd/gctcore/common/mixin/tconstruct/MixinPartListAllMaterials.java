package com.smd.gctcore.common.mixin.tconstruct;

import com.smd.gctcore.common.integration.tconstruct.TConPartWhitelistHelper;
import net.minecraft.item.Item;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.tinkering.MaterialItem;
import slimeknights.tconstruct.library.tools.Shard;
import slimeknights.tconstruct.library.tools.ToolPart;
import slimeknights.tconstruct.tools.common.item.SharpeningKit;

/**
 * Allows whitelisted Tinkers' part items to list all valid material variants even when
 * Config.listAllPartMaterials is disabled, while leaving unlisted parts on the first valid material.
 */
@Mixin(
        value = {
                MaterialItem.class,
                ToolPart.class,
                Shard.class,
                SharpeningKit.class
        },
        remap = false
)
public abstract class MixinPartListAllMaterials {

    @Redirect(
            method = "getSubItems(Lnet/minecraft/creativetab/CreativeTabs;Lnet/minecraft/util/NonNullList;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lslimeknights/tconstruct/common/config/Config;listAllPartMaterials:Z",
                    opcode = Opcodes.GETSTATIC,
                    remap = false
            ),
            remap = true,
            require = 1
    )
    private boolean gctcore$shouldListAllPartMaterials() {
        return Config.listAllPartMaterials
                || TConPartWhitelistHelper.shouldListAllMaterials((Item) (Object) this);
    }
}
