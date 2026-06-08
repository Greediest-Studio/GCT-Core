package com.smd.gctcore.common.mixin.whimcraft;

import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6Arcane;
import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6Crucible;
import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6InfusionMatrix;
import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6InfusionMatrixResearch;
import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6Smelter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.aspects.Aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mixin(
        value = {
                AdapterTC6Arcane.class,
                AdapterTC6Crucible.class,
                AdapterTC6InfusionMatrix.class,
                AdapterTC6InfusionMatrixResearch.class,
                AdapterTC6Smelter.class
        },
        remap = false
)
public abstract class MixinAdapterTC6EssentiaBridge {

    @Unique
    private static final ResourceLocation GCTCORE$MMCE_ADDONS_ESSENTIA_TYPE =
            new ResourceLocation("modularmachineryaddons", "essentia");

    @Unique
    private static final String GCTCORE$MMCE_ADDONS_ESSENTIA_REQUIREMENT =
            "github.alecsio.mmceaddons.common.hatch.thaumcraft.ae2.essentia.RequirementEssentia";

    @Redirect(
            method = "lambda$null$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/xinyihl/whimcraft/common/integration/adapter/tc6/AspectRequirementUtil;getRequirementType()Lhellfirepvp/modularmachinery/common/crafting/requirement/type/RequirementType;"
            ),
            remap = false,
            require = 0
    )
    private static RequirementType<?, ?> gctcore$getEssentiaRequirementType0() {
        return gctcore$getEssentiaRequirementType();
    }

    @Redirect(
            method = "lambda$null$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/xinyihl/whimcraft/common/integration/adapter/tc6/AspectRequirementUtil;getRequirementType()Lhellfirepvp/modularmachinery/common/crafting/requirement/type/RequirementType;"
            ),
            remap = false,
            require = 0
    )
    private static RequirementType<?, ?> gctcore$getEssentiaRequirementType1() {
        return gctcore$getEssentiaRequirementType();
    }

    @Redirect(
            method = "lambda$null$3",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/xinyihl/whimcraft/common/integration/adapter/tc6/AspectRequirementUtil;getRequirementType()Lhellfirepvp/modularmachinery/common/crafting/requirement/type/RequirementType;"
            ),
            remap = false,
            require = 0
    )
    private static RequirementType<?, ?> gctcore$getEssentiaRequirementType3() {
        return gctcore$getEssentiaRequirementType();
    }

    @Redirect(
            method = "lambda$null$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/xinyihl/whimcraft/common/integration/adapter/tc6/AspectRequirementUtil;getRequirement(Lhellfirepvp/modularmachinery/common/machine/IOType;ILthaumcraft/api/aspects/Aspect;)Lhellfirepvp/modularmachinery/common/crafting/helper/ComponentRequirement;"
            ),
            remap = false,
            require = 0
    )
    private static ComponentRequirement<?, ?> gctcore$getEssentiaRequirement0(IOType actionType, int amount, Aspect aspect) {
        return gctcore$getEssentiaRequirement(actionType, amount, aspect);
    }

    @Redirect(
            method = "lambda$null$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/xinyihl/whimcraft/common/integration/adapter/tc6/AspectRequirementUtil;getRequirement(Lhellfirepvp/modularmachinery/common/machine/IOType;ILthaumcraft/api/aspects/Aspect;)Lhellfirepvp/modularmachinery/common/crafting/helper/ComponentRequirement;"
            ),
            remap = false,
            require = 0
    )
    private static ComponentRequirement<?, ?> gctcore$getEssentiaRequirement1(IOType actionType, int amount, Aspect aspect) {
        return gctcore$getEssentiaRequirement(actionType, amount, aspect);
    }

    @Redirect(
            method = "lambda$null$3",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/xinyihl/whimcraft/common/integration/adapter/tc6/AspectRequirementUtil;getRequirement(Lhellfirepvp/modularmachinery/common/machine/IOType;ILthaumcraft/api/aspects/Aspect;)Lhellfirepvp/modularmachinery/common/crafting/helper/ComponentRequirement;"
            ),
            remap = false,
            require = 0
    )
    private static ComponentRequirement<?, ?> gctcore$getEssentiaRequirement3(IOType actionType, int amount, Aspect aspect) {
        return gctcore$getEssentiaRequirement(actionType, amount, aspect);
    }

    @Unique
    private static RequirementType<?, ?> gctcore$getEssentiaRequirementType() {
        RequirementType<?, ?> requirementType = RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(GCTCORE$MMCE_ADDONS_ESSENTIA_TYPE);
        if (requirementType == null) {
            throw new IllegalStateException("Missing MMCE Addons essentia requirement type: " + GCTCORE$MMCE_ADDONS_ESSENTIA_TYPE);
        }
        return requirementType;
    }

    @Unique
    private static ComponentRequirement<?, ?> gctcore$getEssentiaRequirement(IOType actionType, int amount, Aspect aspect) {
        try {
            Class<?> requirementClass = Class.forName(GCTCORE$MMCE_ADDONS_ESSENTIA_REQUIREMENT);
            Method from = requirementClass.getMethod("from", IOType.class, String.class, int.class);
            return (ComponentRequirement<?, ?>) from.invoke(null, actionType, aspect.getTag(), amount);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Missing MMCE Addons essentia requirement class: " + GCTCORE$MMCE_ADDONS_ESSENTIA_REQUIREMENT, e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Incompatible MMCE Addons essentia requirement API.", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Failed to create MMCE Addons essentia requirement.", e.getCause());
        }
    }
}
