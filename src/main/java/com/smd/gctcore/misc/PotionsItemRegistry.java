package com.smd.gctcore.misc;

import com.smd.gctcore.common.potions.PotionSukhavati;
import com.smd.gctcore.common.potions.PotionImmobilized;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionsItemRegistry {
    public static Potion SUKHAVATI;
    public static Potion IMMOBILIZED;
    
    public static void init(){
        SUKHAVATI = new PotionSukhavati();
        IMMOBILIZED = new PotionImmobilized();
    }

    @SubscribeEvent
    public void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().registerAll(
                SUKHAVATI,
                IMMOBILIZED
        );
    }
}
