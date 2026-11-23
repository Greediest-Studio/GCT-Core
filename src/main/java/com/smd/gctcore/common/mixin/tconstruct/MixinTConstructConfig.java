package com.smd.gctcore.common.mixin.tconstruct;

import com.smd.gctcore.common.integration.tconstruct.TConPartWhitelistHelper;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.common.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Syncs part material whitelist properties directly inside Tinkers' Construct's tconstruct.cfg
 * under the clientside category, right alongside listAllPartMaterials.
 */
@Mixin(value = Config.class, remap = false)
public abstract class MixinTConstructConfig {

    @Unique
    private static final String GCTCORE$CAT_CLIENTSIDE = "clientside";
    @Unique
    private static final String GCTCORE$KEY_LIST_ALL_PART_MATERIALS = "listAllPartMaterials";
    @Unique
    private static final String GCTCORE$KEY_ENABLE_WHITELIST = "enablePartMaterialWhitelist";
    @Unique
    private static final String GCTCORE$KEY_PART_WHITELIST = "partMaterialWhitelist";

    @Shadow
    static Configuration configFile;

    @Shadow
    static ConfigCategory ClientSide;

    @Inject(
            method = "syncConfig()Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/config/Configuration;hasChanged()Z",
                    ordinal = 0
            ),
            require = 1
    )
    private static void gctcore$syncPartDisplayConfig(CallbackInfoReturnable<Boolean> cir) {
        if (configFile == null) {
            return;
        }

        Property enableProp = configFile.get(GCTCORE$CAT_CLIENTSIDE, GCTCORE$KEY_ENABLE_WHITELIST, true);
        enableProp.setComment(
                "Enable part material whitelist override when listAllPartMaterials is false. "
                        + "Whitelisted parts display all valid materials in creative and JEI/HEI; "
                        + "unlisted parts keep the original false behavior (first valid material only)."
        );
        boolean enabled = enableProp.getBoolean();

        Property listProp = configFile.get(GCTCORE$CAT_CLIENTSIDE, GCTCORE$KEY_PART_WHITELIST, new String[0]);
        listProp.setComment(
                "Registry names of Tinkers' part items that should list all valid materials even when "
                        + "listAllPartMaterials is false (format: modid:item_name, or modid:* for all parts of a mod; "
                        + "bare names without ':' default to tconstruct). Also applies to tconstruct:bolt_core if listed. "
                        + "Examples: tconstruct:pick_head, tconstruct:sharpening_kit, tconstruct:shard"
        );
        String[] whitelist = listProp.getStringList();

        TConPartWhitelistHelper.updateConfig(enabled, whitelist);

        if (ClientSide != null) {
            List<String> order = new ArrayList<>(ClientSide.getPropertyOrder());
            order.remove(GCTCORE$KEY_ENABLE_WHITELIST);
            order.remove(GCTCORE$KEY_PART_WHITELIST);
            int anchor = order.indexOf(GCTCORE$KEY_LIST_ALL_PART_MATERIALS);
            if (anchor >= 0) {
                order.add(anchor + 1, GCTCORE$KEY_ENABLE_WHITELIST);
                order.add(anchor + 2, GCTCORE$KEY_PART_WHITELIST);
            } else {
                order.add(GCTCORE$KEY_ENABLE_WHITELIST);
                order.add(GCTCORE$KEY_PART_WHITELIST);
            }
            ClientSide.setPropertyOrder(order);
        }
    }
}
