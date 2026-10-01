package com.gmm.gctold.common.entity;

import com.gmm.gctold.common.entity.EntityAncientShoggoth;
import com.gmm.gctold.common.entity.EntityApocalypseCube;
import com.gmm.gctold.common.entity.EntityApocalypseHolder;
import com.gmm.gctold.common.entity.EntityApocalypseKnight;
import com.gmm.gctold.common.entity.EntityBligtz;
import com.gmm.gctold.common.entity.EntityBloodyShoggoth;
import com.gmm.gctold.common.entity.EntityBlueFlameBeholder;
import com.gmm.gctold.common.entity.EntityBnatuz;
import com.gmm.gctold.common.entity.EntityBninz;
import com.gmm.gctold.common.entity.EntityBthdz;
import com.gmm.gctold.common.entity.EntityDarkerLesserShoggoth;
import com.gmm.gctold.common.entity.EntityElf;
import com.gmm.gctold.common.entity.EntityMixtureShoggoth;
import com.gmm.gctold.common.entity.EntityRemnantWandering;
import com.gmm.gctold.common.entity.EntityReversedElf;
import com.gmm.gctold.common.entity.EntityRottened;
import com.gmm.gctold.common.entity.EntityShadowBase;
import com.gmm.gctold.common.entity.EntityWeatherEyevil;
import com.gmm.gctold.common.entity.EntityWeatherWaterRod;
import com.gmm.gctold.common.entity.EntityZethur;
import com.gmm.gctold.common.entity.EntityZjarugoth;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class GctAllEntities {
    private GctAllEntities() {
    }

    public static void init(FMLInitializationEvent event) {
        EntityBlueFlameBeholder.init(event);
        EntityDarkerLesserShoggoth.init(event);
        EntityReversedElf.init(event);
        EntityRottened.init(event);
        EntityShadowBase.init(event);
    }

    @SideOnly(Side.CLIENT)
    public static void registerRenderers(FMLPreInitializationEvent event) {
        EntityAncientShoggoth.registerRenderers(event);
        EntityApocalypseCube.registerRenderers(event);
        EntityApocalypseHolder.registerRenderers(event);
        EntityApocalypseKnight.registerRenderers(event);
        EntityBligtz.registerRenderers(event);
        EntityBloodyShoggoth.registerRenderers(event);
        EntityBlueFlameBeholder.registerRenderers(event);
        EntityBnatuz.registerRenderers(event);
        EntityBninz.registerRenderers(event);
        EntityBthdz.registerRenderers(event);
        EntityDarkerLesserShoggoth.registerRenderers(event);
        EntityElf.registerRenderers(event);
        EntityMixtureShoggoth.registerRenderers(event);
        EntityRemnantWandering.registerRenderers(event);
        EntityReversedElf.registerRenderers(event);
        EntityRottened.registerRenderers(event);
        EntityShadowBase.registerRenderers(event);
        EntityWeatherEyevil.registerRenderers(event);
        EntityWeatherWaterRod.registerRenderers(event);
        EntityZethur.registerRenderers(event);
        EntityZjarugoth.registerRenderers(event);
    }
}
