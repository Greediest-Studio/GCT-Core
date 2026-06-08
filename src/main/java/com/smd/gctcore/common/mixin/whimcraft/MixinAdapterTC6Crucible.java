package com.smd.gctcore.common.mixin.whimcraft;

import com.xinyihl.whimcraft.common.integration.adapter.tc6.AdapterTC6Crucible;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IThaumcraftRecipe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Mixin(value = AdapterTC6Crucible.class, remap = false)
public class MixinAdapterTC6Crucible {

    @Unique
    private static final ResourceLocation GCTCORE$MMCE_ADDONS_ESSENTIA_TYPE =
            new ResourceLocation("modularmachineryaddons", "essentia");

    @Unique
    private static final String GCTCORE$MMCE_ADDONS_ESSENTIA_REQUIREMENT =
            "github.alecsio.mmceaddons.common.hatch.thaumcraft.ae2.essentia.RequirementEssentia";

    @Inject(method = "lambda$createRecipesFor$2", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gctcore$skipRecipeWithInvalidCatalyst(
            List<?> modifiers,
            ResourceLocation machineName,
            Map<?, ?> pcbMap,
            List<?> recipes,
            ResourceLocation recipeName,
            IThaumcraftRecipe tcRecipe,
            CallbackInfo ci
    ) {
        if (!(tcRecipe instanceof CrucibleRecipe)) {
            return;
        }

        Ingredient catalyst = ((CrucibleRecipe) tcRecipe).getCatalyst();
        if (catalyst == null) {
            return;
        }

        ItemStack[] stacks = catalyst.getMatchingStacks();
        if (gctcore$isInvalidCatalystStacks(stacks)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean gctcore$isInvalidCatalystStacks(ItemStack[] stacks) {
        if (stacks == null || stacks.length == 0) {
            return true;
        }

        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                return false;
            }
        }

        return true;
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
    private static RequirementType<?, ?> gctcore$getEssentiaRequirementType() {
        RequirementType<?, ?> requirementType = RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(GCTCORE$MMCE_ADDONS_ESSENTIA_TYPE);
        if (requirementType == null) {
            throw new IllegalStateException("Missing MMCE Addons essentia requirement type: " + GCTCORE$MMCE_ADDONS_ESSENTIA_TYPE);
        }
        return requirementType;
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
