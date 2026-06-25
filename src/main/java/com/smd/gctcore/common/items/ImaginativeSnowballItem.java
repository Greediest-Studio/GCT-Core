package com.smd.gctcore.common.items;

import com.smd.gctcore.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ImaginativeSnowballItem extends Item {

    public ImaginativeSnowballItem() {
        setRegistryName("imaginative_snowball");
        setTranslationKey(Tags.MOD_ID + ".imaginative_snowball");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(16);
    }
}
