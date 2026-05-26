package com.smd.gctcore.common.mixin.ageofminecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.helpful.EntityFriendlyCreature;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = EntityFriendlyCreature.class)
public abstract class MixinEntityFriendlyCreature {

    /**
     * 完全关闭召唤师生物的队伍/友伤判定，让任何来源的攻击都能结算伤害。
     *
     * @author Gct-Core
     * @reason Summoner mobs should never ignore player damage.
     */
    @Overwrite
    public boolean isOnSameTeam(Entity entity) {
        return false;
    }

    /**
     * 召唤师生物不再加入任何计分板队伍，避免被友军保护逻辑拦截。
     *
     * @author Gct-Core
     * @reason Summoner mobs must be damageable by all attackers.
     */
    @Overwrite(remap = false)
    public Team getTeam() {
        return null;
    }
}
