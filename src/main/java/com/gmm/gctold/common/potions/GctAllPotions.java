package com.gmm.gctold.common.potions;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraftforge.event.RegistryEvent;

public final class GctAllPotions {
    private GctAllPotions() {
    }

    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().registerAll(
                PotionAbyssPlague.potion,
                PotionAntiAnyposion.potion,
                PotionBluefire.potion,
                PotionBlueScreen.potion,
                PotionChanneling.potion,
                PotionCorrecting.potion,
                PotionCurseOfTwilight.potion,
                PotionOverrestrain.potion,
                PotionStop.potion,
                PotionWorldEnd.potion,
                PotionZjarugothDamage.potion
        );
    }

    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        event.getRegistry().registerAll(
                PotionAbyssPlague.potionType,
                PotionAntiAnyposion.potionType,
                PotionBluefire.potionType,
                PotionBlueScreen.potionType,
                PotionChanneling.potionType,
                PotionCurseOfTwilight.potionType,
                PotionStop.potionType,
                PotionWorldEnd.potionType
        );
    }
}
