package com.smd.gctcore.misc;

import net.minecraftforge.fml.common.Loader;

public enum Mods {
    TOP("theoneprobe"),
    AS("astralsorcery"),
    AE2("appliedenergistics2"),
    MORETCON("moretcon");

    public final String modid;

    Mods(String modid){
        this.modid = modid;
    }

    public boolean isLoading(){
        return Loader.isModLoaded(this.modid);
    }
}
