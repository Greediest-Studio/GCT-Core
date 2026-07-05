package com.smd.gctcore.common.mixin.tconstruct;

import com.smd.gctcore.common.config.MaterialShaderFixConfig;
import com.smd.gctcore.gctcore;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tools.ToolPart;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mixin(value = ToolPart.class, remap = false)
public abstract class MixinToolPart implements IToolPart {
    
    /**
     * Standard TConstruct material stat types that are commonly present in materials.
     * If a material has any of these, it's considered a valid material for rendering purposes.
     */
    private static final Set<String> STANDARD_STAT_TYPES = new HashSet<>(Arrays.asList(
        "head",       // MaterialTypes.HEAD - Tool heads (pickaxe head, axe head, etc.)
        "handle",     // MaterialTypes.HANDLE - Tool handles
        "extra",      // MaterialTypes.EXTRA - Tool extras/binding
        "bow",        // MaterialTypes.BOW - Bow limbs
        "bowstring",  // MaterialTypes.BOWSTRING - Bow strings
        "projectile", // MaterialTypes.PROJECTILE - Arrow heads
        "shaft",      // MaterialTypes.SHAFT - Arrow shafts
        "fletching"   // MaterialTypes.FLETCHING - Arrow fletching
    ));
    
    /**
     * Gets the set of custom stat types from config.
     * Cached as a Set for efficient lookup.
     */
    private static Set<String> getCustomStatTypes() {
        return new HashSet<>(Arrays.asList(MaterialShaderFixConfig.customStatTypes));
    }
    
    /**
     * Override canUseMaterialForRendering from the IToolPart interface.
     * By implementing this method in the Mixin class, it provides a concrete implementation
     * that takes precedence over the interface's default implementation when ToolPart is loaded.
     * This method is called by CustomTextureCreator to determine which materials should have
     * colored textures generated for this tool part.
     * 
     * @param mat the material to check
     * @return true if the material can be used for rendering this part
     */
    @Override
    public boolean canUseMaterialForRendering(Material mat) {
        // If the fix is disabled in config, use original behavior (call canUseMaterial)
        if (!MaterialShaderFixConfig.enableShaderFix) {
            return this.canUseMaterial(mat);
        }
        
        // First, check if the material can be used normally (has the required custom stats)
        boolean canUse = this.canUseMaterial(mat);
        if (canUse) {
            return true;
        }
        
        // For parts that use custom stat types, allow rendering with any material that has
        // standard stats. This enables shader generation for CraftTweaker materials.
        boolean usesCustom = this.usesCustomStatType();
        boolean hasStandard = this.hasAnyStandardStat(mat);
        
        // DEBUG logging if enabled
        if (MaterialShaderFixConfig.enableDebugLogging && usesCustom && hasStandard) {
            gctcore.LOGGER.debug("Enabling {} rendering for custom part: {}", mat.identifier, this);
        }
        
        if (usesCustom) {
            return hasStandard;
        }
        
        // For standard parts, use the original behavior
        return false;
    }
    
    /**
     * Determines if this tool part requires any custom (non-vanilla) stat type.
     * Uses the custom stat types defined in the config.
     * IMPORTANT: This method checks if the part's unlocalized name contains any custom stat type identifier.
     * This is more reliable than checking registered tools because some tools may not be registered
     * depending on config settings (e.g., Moar-TCon's Bomb tool depends on enableBomb config).
     * 
     * @return true if this part is likely used for a custom stat type
     */
    private boolean usesCustomStatType() {
        Set<String> customTypes = getCustomStatTypes();
        IToolPart thisPart = this;
        
        // Get the part's unlocalized name (e.g., "item.moretcon.explosive_charge.name")
        String unlocalizedName = thisPart.toString();
        
        // Check if the unlocalized name contains any custom stat type identifier
        // This handles cases where the stat type is part of the item name
        for (String customType : customTypes) {
            // Remove namespace prefix (e.g., "moretcon.explosive_charge" -> "explosive_charge")
            String simpleType = customType.contains(".") ? 
                customType.substring(customType.lastIndexOf('.') + 1) : customType;
            
            if (unlocalizedName.contains(simpleType)) {
                if (MaterialShaderFixConfig.enableDebugLogging) {
                    gctcore.LOGGER.debug("Part {} identified as custom stat type: {}", unlocalizedName, customType);
                }
                return true;
            }
        }
        
        // Fallback: Check registered tools (for parts that don't have the stat type in their name)
        for (ToolCore tool : TinkerRegistry.getTools()) {
            for (PartMaterialType pmt : tool.getRequiredComponents()) {
                if (pmt.isValidItem(thisPart)) {
                    for (String customType : customTypes) {
                        if (pmt.usesStat(customType)) {
                            if (MaterialShaderFixConfig.enableDebugLogging) {
                                gctcore.LOGGER.debug("Part {} found in tool {} using custom stat: {}", unlocalizedName, tool.getIdentifier(), customType);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the material has any of the standard TConstruct stat types.
     * Materials with at least one standard stat are considered valid for rendering.
     * 
     * @param mat the material to check
     * @return true if the material has at least one standard stat type
     */
    private boolean hasAnyStandardStat(Material mat) {
        for (String statType : STANDARD_STAT_TYPES) {
            if (mat.hasStats(statType)) {
                return true;
            }
        }
        return false;
    }
}
