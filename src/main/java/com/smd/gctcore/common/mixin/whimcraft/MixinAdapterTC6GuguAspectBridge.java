package com.smd.gctcore.common.mixin.whimcraft;

import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6Arcane;
import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6Crucible;
import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6InfusionMatrix;
import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6InfusionMatrixResearch;
import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6Smelter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.aspects.Aspect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
public abstract class MixinAdapterTC6GuguAspectBridge {

    @Unique
    private static final String GCTCORE$GUGU_REQUIREMENTS =
            "com.warmthdawn.mod.gugu_utils.modularmachenary.MMRequirements";

    @Unique
    private static final String GCTCORE$GUGU_ASPECT_INPUT =
            "com.warmthdawn.mod.gugu_utils.modularmachenary.requirements.RequirementAspect";

    @Unique
    private static final String GCTCORE$GUGU_ASPECT_OUTPUT =
            "com.warmthdawn.mod.gugu_utils.modularmachenary.requirements.RequirementAspectOutput";

    @Redirect(
            method = "lambda$null$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/xinyihl/whimcraft/common/integration/adapter/tc6/AspectRequirementUtil;getRequirementType()Lhellfirepvp/modularmachinery/common/crafting/requirement/type/RequirementType;"
            ),
            remap = false,
            require = 0
    )
    private static RequirementType<?, ?> gctcore$getAspectRequirementType0() {
        return gctcore$getAspectRequirementType();
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
    private static RequirementType<?, ?> gctcore$getAspectRequirementType1() {
        return gctcore$getAspectRequirementType();
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
    private static RequirementType<?, ?> gctcore$getAspectRequirementType3() {
        return gctcore$getAspectRequirementType();
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
    private static ComponentRequirement<?, ?> gctcore$getAspectRequirement0(IOType actionType, int amount, Aspect aspect) {
        return gctcore$getAspectRequirement(actionType, amount, aspect);
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
    private static ComponentRequirement<?, ?> gctcore$getAspectRequirement1(IOType actionType, int amount, Aspect aspect) {
        return gctcore$getAspectRequirement(actionType, amount, aspect);
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
    private static ComponentRequirement<?, ?> gctcore$getAspectRequirement3(IOType actionType, int amount, Aspect aspect) {
        return gctcore$getAspectRequirement(actionType, amount, aspect);
    }

    @Unique
    private static RequirementType<?, ?> gctcore$getAspectRequirementType() {
        try {
            Class<?> requirements = Class.forName(GCTCORE$GUGU_REQUIREMENTS);
            Field type = requirements.getField("REQUIREMENT_TYPE_ASPECT");
            return (RequirementType<?, ?>) type.get(null);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Missing gugu-utils requirement registry: " + GCTCORE$GUGU_REQUIREMENTS, e);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Incompatible gugu-utils aspect requirement registry.", e);
        }
    }

    @Unique
    private static ComponentRequirement<?, ?> gctcore$getAspectRequirement(IOType actionType, int amount, Aspect aspect) {
        try {
            if (actionType == IOType.INPUT) {
                Class<?> requirementClass = Class.forName(GCTCORE$GUGU_ASPECT_INPUT);
                Method createInput = requirementClass.getMethod("createInput", int.class, Aspect.class);
                return (ComponentRequirement<?, ?>) createInput.invoke(null, amount, aspect);
            }
            if (actionType == IOType.OUTPUT) {
                Class<?> requirementClass = Class.forName(GCTCORE$GUGU_ASPECT_OUTPUT);
                Constructor<?> constructor = requirementClass.getConstructor(int.class, Aspect.class);
                return (ComponentRequirement<?, ?>) constructor.newInstance(amount, aspect);
            }
            throw new IllegalStateException("Unknown IOType: " + actionType);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Missing gugu-utils aspect requirement class.", e);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException("Incompatible gugu-utils aspect requirement API.", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Failed to create gugu-utils aspect requirement.", e.getCause());
        }
    }
}
