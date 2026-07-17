package com.smd.gctcore.misc;

import net.minecraftforge.fml.common.Loader;

public enum Mods {
    TOP("theoneprobe"),
    AS("astralsorcery"),
    AE2("appliedenergistics2"),
    NATURES_AURA("naturesaura"),
    MORETCON("moretcon"),
    BOT("botania"),
    MEKANISM("mekanism");

    public final String modid;

    Mods(String modid){
        this.modid = modid;
    }

    public boolean isLoading(){
        return Loader.isModLoaded(this.modid);
    }
}
