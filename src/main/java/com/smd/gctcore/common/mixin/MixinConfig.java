package com.smd.gctcore.common.mixin;

import com.smd.gctcore.common.config.GCTMixinConfig;
import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.*;
import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class MixinConfig implements ILateMixinLoader {

    private static final Map<String, BooleanSupplier> MIXIN_CONFIGS = new LinkedHashMap<>();

    static {
        addModdedMixinCFG("mixins.gctcore.aether.json", "aether_legacy");
        addModdedMixinCFG("mixins.gctcore.avaritia.json", "avaritia");
        addModdedMixinCFG("mixins.gctcore.bountifulbaubles.json", "bountifulbaubles");
        addModdedMixinCFG("mixins.gctcore.astralsorcery.json", "astralsorcery");
        addModdedMixinCFG("mixins.gctcore.tconstruct.json", "tconstruct");
        addModdedMixinCFG("mixins.gctcore.biomesoplenty.json", "biomesoplenty");
        addModdedMixinCFG("mixins.gctcore.enderstorage.json", "enderstorage");
        addModdedMixinCFG("mixins.gctcore.extrabotany.json", "extrabotany");
        addModdedMixinCFG("mixins.gctcore.abyssalcraft.json", "abyssalcraft");
        addModdedMixinCFG("mixins.gctcore.moretcon.json", "moretcon");
        addModdedMixinCFG("mixins.gctcore.gctmobs.json", "gct_mobs");
        addModdedMixinCFG("mixins.gctcore.gctores.json", "gct_ores");
        addModdedMixinCFG("mixins.gctcore.gctaby.json", "gct_aby");
        addModdedMixinCFG("mixins.gctcore.industrialforegoing.json", "industrialforegoing");
        addModdedMixinCFG("mixins.gctcore.bloodmagic.json", "bloodmagic");
        addModdedMixinCFG("mixins.gctcore.pewter.json", "pewter");
        addModdedMixinCFG("mixins.gctcore.ToroHUD.json", "torohud");
        addModdedMixinCFG("mixins.gctcore.thaumicrestoration.json", "thaumicrestoration");
        addModdedMixinCFG("mixins.gctcore.enderio.json","enderio");
        addModdedMixinCFG("mixins.gctcore.endertweaker.json","endertweaker");
        addModdedMixinCFG("mixins.gctcore.thaumicadditions.json","thaumicadditions");
        addModdedMixinCFG("mixins.gctcore.simplesmelteryaccelerator.json","simplesmelteryaccelerator");
        addModdedMixinCFG("mixins.gctcore.whimcraft.json", "whimcraft");
        addModdedMixinCFG("mixins.gctcore.konkrete.json","konkrete");
        addModdedMixinCFG("mixins.gctcore.hei.json", "jei");
        addMixinCFG("mixins.gctcore.embers.json", () -> modLoaded("embers") && modLoaded("jei"));
        addModdedMixinCFG("mixins.gctcore.ae2.json", "appliedenergistics2");
        addMixinCFG("mixins.gctcore.ae2reid.json",
                () -> modLoaded("appliedenergistics2") && modLoaded("jeid"));
        addMixinCFG("mixins.gctcore.fluxapplied.json",
                () -> modLoaded("appliedenergistics2") && modLoaded("flux_applied"));
        addModdedMixinCFG("mixins.gctcore.botaniaapplie.json", "botania_applie");
        addModdedMixinCFG("mixins.gctcore.botania.json", "botania");
        addModdedMixinCFG("mixins.gctcore.botaniverse.json", "botaniverse");
        addMixinCFG("mixins.gctcore.mmceparallelequalizer.json",
                () -> modLoaded("modularmachinery") && modLoaded("mmceparallelequalizer"));
        addModdedMixinCFG("mixins.gctcore.mekanism.json", "mekanism");
        addModdedMixinCFG("mixins.gctcore.mekceumoremachine.json", "mekceumoremachine");
        addModdedMixinCFG("mixins.gctcore.quantumthings.json", "randomthings");
        addModdedMixinCFG("mixins.gctcore.projecte.json", "projecte");
        addModdedMixinCFG("mixins.gctcore.cyclic.json", "cyclic");
        addModdedMixinCFG("mixins.gctcore.dungeonsmod.json", "dungeonsmod");
        addModdedMixinCFG("mixins.gctcore.scalingguis.json", "scalingguis");
        if(GCTMixinConfig.enableMixinItemToolSceptre) {
            addModdedMixinCFG("mixins.gctcore.tconevo.json", "tconevo");
        }
        addMixinCFG("mixins.gctcore.twilightforest.json", () -> modLoaded("twilightforest") && modLoaded("gct_mobs"));
        addMixinCFG("mixins.gctcore.ftbquests.json",
                () -> modLoaded("ftbquests") && modLoaded("retro_sophisticated_backpacks"));
    }

    @Override
    public List<String> getMixinConfigs() {
        return new ArrayList<>(MIXIN_CONFIGS.keySet());
    }

    @Override
    public boolean shouldMixinConfigQueue(final String mixinConfig) {
        BooleanSupplier supplier = MIXIN_CONFIGS.get(mixinConfig);
        if (supplier == null) {
            return false;
        }
        return supplier.getAsBoolean();
    }

    private static boolean modLoaded(final String modID) {
        return Loader.isModLoaded(modID);
    }

    private static void addModdedMixinCFG(final String mixinConfig, final String modID) {
        MIXIN_CONFIGS.put(mixinConfig, () -> modLoaded(modID));
    }

    private static void addMixinCFG(final String mixinConfig, final BooleanSupplier conditions) {
        MIXIN_CONFIGS.put(mixinConfig, conditions);
    }

}
