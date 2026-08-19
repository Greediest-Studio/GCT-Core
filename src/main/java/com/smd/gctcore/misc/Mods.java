package com.smd.gctcore.misc;

import net.minecraftforge.fml.common.Loader;

public enum Mods {
    TOP("theoneprobe"),
    AS("astralsorcery"),
    AE2("appliedenergistics2"),
    EXTENDED_CRAFTING("extendedcrafting"),
    NATURES_AURA("naturesaura"),
    MORETCON("moretcon"),
    BOT("botania"),
    EXTRABOTANY("extrabotany"),
    BOTANIVERSE("botaniverse"),
    BOTANIC_ADDITIONS("botanicadds"),
    MEKANISM("mekanism");

    public final String modid;

    Mods(String modid){
        this.modid = modid;
    }

    public boolean isLoading(){
        return Loader.isModLoaded(this.modid);
    }
}
