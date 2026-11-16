package com.smd.gctcore.common.mixin.aether;

import com.gildedgames.the_aether.entities.hostile.EntityZephyr;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.translation.I18n;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityZephyr.class)
public abstract class MixinAetherZephyrName {

    public String getName() {
        Entity entity = (Entity) (Object) this;
        return entity.hasCustomName()
                ? entity.getCustomNameTag()
                : I18n.translateToLocal("entity.gctcore.aether_zephyr.name");
    }
}
