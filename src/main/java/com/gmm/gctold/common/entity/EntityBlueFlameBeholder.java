package com.gmm.gctold.common.entity;

import com.gmm.gctold.common.events.SanityEvents;
import com.gmm.gctold.misc.registry.GctAllItems;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityBlueFlameBeholder {


  public static void init(FMLInitializationEvent event) {
    Biome[] spawnBiomes = { (Biome)Biome.REGISTRY.getObject(new ResourceLocation("gctcore:warped")) };
    EntityRegistry.addSpawn(BlueFlameBeholderEntity.class, 20, 4, 4, EnumCreatureType.MONSTER, spawnBiomes);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(BlueFlameBeholderEntity.class, renderManager -> new RenderLiving(renderManager, new ModelWeatherBeholder(), 0.5F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/beholder.png");
          }
        });
  }

  public static class BlueFlameBeholderEntity extends EntityMob {
    public BlueFlameBeholderEntity(World world) {
      super(world);
      setSize(0.6F, 1.8F);
      this.experienceValue = 5;
      this.isImmuneToFire = false;
      setNoAI(false);
    }

    protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, (EntityAIBase)new EntityAIAttackMelee((EntityCreature)this, 1.2D, false));
      this.targetTasks.addTask(2, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
      this.tasks.addTask(3, (EntityAIBase)new EntityAIWander((EntityCreature)this, 0.8D));
      this.tasks.addTask(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
      this.targetTasks.addTask(5, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this,
          EntityPlayer.class, 10, false, false, player -> SanityEvents.canSanityTargetPlayer((EntityPlayer) player)));
    }

    public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEFINED;
    }

    protected Item getDropItem() {
      return (new ItemStack(GctAllItems.WARPED_SOUL, 1)).getItem();
    }

    public SoundEvent getAmbientSound() {
      return null;
    }

    public SoundEvent getHurtSound(DamageSource ds) {
      return null;
    }

    public SoundEvent getDeathSound() {
      return null;
    }

    protected float getSoundVolume() {
      return 1.0F;
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
    }
  }

  public static class ModelWeatherBeholder extends ModelBase {
    private final ModelRenderer main;

    private final ModelRenderer mainChild10;

    private final ModelRenderer mainChild0;

    private final ModelRenderer mainChild4;

    private final ModelRenderer mainChild8;

    private final ModelRenderer mainChild2;

    private final ModelRenderer mainChild6;

    private final ModelRenderer mainChild9;

    private final ModelRenderer mainChild;

    private final ModelRenderer mainChildChild;

    private final ModelRenderer mainChildChild_r1;

    private final ModelRenderer mainChild1;

    private final ModelRenderer mainChild5;

    private final ModelRenderer mainChild7;

    private final ModelRenderer mainChild3;

    private final ModelRenderer mainChild3_r1;

    private final ModelRenderer bb_main;

    private final ModelRenderer cube_r1;

    private final ModelRenderer cube_r2;

    private final ModelRenderer cube_r3;

    private final ModelRenderer cube_r4;

    public ModelWeatherBeholder() {
      this.textureWidth = 16;
      this.textureHeight = 16;
      this.main = new ModelRenderer(this);
      this.main.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.main.cubeList.add(new ModelBox(this.main, 0, 0, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0.0F, false));
      this.mainChild10 = new ModelRenderer(this);
      this.mainChild10.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild10);
      setRotationAngle(this.mainChild10, -0.1745F, 0.0F, 0.0F);
      this.mainChild10.cubeList.add(new ModelBox(this.mainChild10, 0, 0, -2.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.mainChild0 = new ModelRenderer(this);
      this.mainChild0.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild0);
      setRotationAngle(this.mainChild0, -0.1745F, 0.0F, 0.0F);
      this.mainChild0.cubeList.add(new ModelBox(this.mainChild0, 0, 0, -8.0F, -27.8785F, -9.3892F, 16, 12, 16, 0.0F, false));
      this.mainChild4 = new ModelRenderer(this);
      this.mainChild4.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild4);
      setRotationAngle(this.mainChild4, -0.1745F, 0.0F, 0.0F);
      this.mainChild4.cubeList.add(new ModelBox(this.mainChild4, 0, 0, 5.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.mainChild8 = new ModelRenderer(this);
      this.mainChild8.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild8);
      setRotationAngle(this.mainChild8, -0.1745F, 0.0F, 0.0F);
      this.mainChild8.cubeList.add(new ModelBox(this.mainChild8, 0, 0, -4.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.mainChild2 = new ModelRenderer(this);
      this.mainChild2.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild2);
      setRotationAngle(this.mainChild2, -0.1745F, 0.0F, 0.0F);
      this.mainChild2.cubeList.add(new ModelBox(this.mainChild2, 0, 0, -2.0F, -23.8785F, -11.3892F, 4, 3, 1, 0.0F, false));
      this.mainChild6 = new ModelRenderer(this);
      this.mainChild6.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild6);
      setRotationAngle(this.mainChild6, -0.1745F, 0.0F, 0.0F);
      this.mainChild6.cubeList.add(new ModelBox(this.mainChild6, 0, 0, -8.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.mainChild9 = new ModelRenderer(this);
      this.mainChild9.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild9);
      setRotationAngle(this.mainChild9, -0.1745F, 0.0F, 0.0F);
      this.mainChild9.cubeList.add(new ModelBox(this.mainChild9, 0, 0, 1.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.mainChild = new ModelRenderer(this);
      this.mainChild.setRotationPoint(4.0F, -12.0F, -4.0F);
      this.main.addChild(this.mainChild);
      this.mainChild.cubeList.add(new ModelBox(this.mainChild, 0, 0, -20.0F, -26.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.mainChild.cubeList.add(new ModelBox(this.mainChild, 0, 0, 9.0F, -23.0F, 5.0F, 2, 9, 2, 0.0F, false));
      this.mainChildChild = new ModelRenderer(this);
      this.mainChildChild.setRotationPoint(-11.0F, -8.0F, 0.0F);
      this.mainChild.addChild(this.mainChildChild);
      setRotationAngle(this.mainChildChild, 0.0F, 0.0F, -0.7418F);
      this.mainChildChild.cubeList.add(new ModelBox(this.mainChildChild, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.mainChildChild_r1 = new ModelRenderer(this);
      this.mainChildChild_r1.setRotationPoint(7.0F, 44.0F, 4.0F);
      this.mainChildChild.addChild(this.mainChildChild_r1);
      setRotationAngle(this.mainChildChild_r1, 0.0F, 0.0F, 1.4399F);
      this.mainChildChild_r1.cubeList.add(new ModelBox(this.mainChildChild_r1, 0, 0, -34.0F, -18.0F, 1.0F, 2, 11, 2, 0.0F, false));
      this.mainChild1 = new ModelRenderer(this);
      this.mainChild1.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild1);
      setRotationAngle(this.mainChild1, -0.1745F, 0.0F, 0.0F);
      this.mainChild1.cubeList.add(new ModelBox(this.mainChild1, 0, 0, -6.0F, -25.8785F, -10.3892F, 12, 7, 1, 0.0F, false));
      this.mainChild5 = new ModelRenderer(this);
      this.mainChild5.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild5);
      setRotationAngle(this.mainChild5, -0.1745F, 0.0F, 0.0F);
      this.mainChild5.cubeList.add(new ModelBox(this.mainChild5, 0, 0, 3.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.mainChild7 = new ModelRenderer(this);
      this.mainChild7.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild7);
      setRotationAngle(this.mainChild7, -0.1745F, 0.0F, 0.0F);
      this.mainChild7.cubeList.add(new ModelBox(this.mainChild7, 0, 0, -6.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.mainChild3 = new ModelRenderer(this);
      this.mainChild3.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.mainChild3);
      setRotationAngle(this.mainChild3, -0.1745F, 0.0F, 0.0F);
      this.mainChild3.cubeList.add(new ModelBox(this.mainChild3, 0, 0, 7.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.mainChild3_r1 = new ModelRenderer(this);
      this.mainChild3_r1.setRotationPoint(0.0F, 25.0F, 1.0F);
      this.mainChild3.addChild(this.mainChild3_r1);
      setRotationAngle(this.mainChild3_r1, 0.4363F, 0.0F, 0.0F);
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, -7.0F, -39.8785F, 8.6108F, 1, 4, 1, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, -5.0F, -39.8785F, 8.6108F, 1, 4, 1, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, -3.0F, -39.8785F, 8.6108F, 1, 4, 1, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, -1.0F, -39.8785F, 8.6108F, 1, 4, 1, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, 0.0F, -39.8785F, 8.6108F, 1, 4, 1, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, 2.0F, -39.8785F, 8.6108F, 1, 4, 1, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, 4.0F, -39.8785F, 8.6108F, 1, 4, 1, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, 6.0F, -39.8785F, 8.6108F, 1, 4, 1, 0.0F, false));
      this.bb_main = new ModelRenderer(this);
      this.bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
      this.cube_r1 = new ModelRenderer(this);
      this.cube_r1.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bb_main.addChild(this.cube_r1);
      setRotationAngle(this.cube_r1, 0.0F, 0.0F, -0.2182F);
      this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 0, 0, 18.0F, -65.0F, 0.0F, 2, 10, 2, 0.0F, false));
      this.cube_r2 = new ModelRenderer(this);
      this.cube_r2.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bb_main.addChild(this.cube_r2);
      setRotationAngle(this.cube_r2, 4.0E-4F, 0.0057F, 0.3489F);
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, -15.0F, -57.0F, 0.0F, 2, 8, 2, 0.0F, false));
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, -27.0F, -60.0F, 0.0F, 2, 8, 2, 0.0F, false));
      this.cube_r3 = new ModelRenderer(this);
      this.cube_r3.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bb_main.addChild(this.cube_r3);
      setRotationAngle(this.cube_r3, 0.0F, 0.0F, -0.1745F);
      this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 0, 0, 3.0F, -59.0F, 0.0F, 2, 12, 2, 0.0F, false));
      this.cube_r4 = new ModelRenderer(this);
      this.cube_r4.setRotationPoint(0.0F, -25.0F, -1.0F);
      this.bb_main.addChild(this.cube_r4);
      setRotationAngle(this.cube_r4, 0.1309F, 0.0F, 0.0F);
      this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 0, 0, -8.0F, -14.0F, -5.0F, 16, 3, 16, 0.0F, false));
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.main.render(f5);
      this.bb_main.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
      this.mainChild3_r1.rotateAngleY = f3 / 57.295776F;
      this.mainChild3_r1.rotateAngleX = f4 / 57.295776F;
      this.mainChildChild.rotateAngleY = f3 / 57.295776F;
      this.mainChildChild.rotateAngleX = f4 / 57.295776F;
      this.mainChildChild_r1.rotateAngleY = f3 / 57.295776F;
      this.mainChildChild_r1.rotateAngleX = f4 / 57.295776F;
      this.mainChild6.rotateAngleY = f3 / 57.295776F;
      this.mainChild6.rotateAngleX = f4 / 57.295776F;
      this.cube_r4.rotateAngleY = f3 / 57.295776F;
      this.cube_r4.rotateAngleX = f4 / 57.295776F;
      this.mainChild5.rotateAngleY = f3 / 57.295776F;
      this.mainChild5.rotateAngleX = f4 / 57.295776F;
      this.main.rotateAngleY = f3 / 57.295776F;
      this.main.rotateAngleX = f4 / 57.295776F;
      this.mainChild4.rotateAngleY = f3 / 57.295776F;
      this.mainChild4.rotateAngleX = f4 / 57.295776F;
      this.mainChild3.rotateAngleY = f3 / 57.295776F;
      this.mainChild3.rotateAngleX = f4 / 57.295776F;
      this.mainChild9.rotateAngleY = f3 / 57.295776F;
      this.mainChild9.rotateAngleX = f4 / 57.295776F;
      this.cube_r1.rotateAngleY = f3 / 57.295776F;
      this.cube_r1.rotateAngleX = f4 / 57.295776F;
      this.mainChild8.rotateAngleY = f3 / 57.295776F;
      this.mainChild8.rotateAngleX = f4 / 57.295776F;
      this.cube_r2.rotateAngleY = f3 / 57.295776F;
      this.cube_r2.rotateAngleX = f4 / 57.295776F;
      this.mainChild7.rotateAngleY = f3 / 57.295776F;
      this.mainChild7.rotateAngleX = f4 / 57.295776F;
      this.cube_r3.rotateAngleY = f3 / 57.295776F;
      this.cube_r3.rotateAngleX = f4 / 57.295776F;
      this.bb_main.rotateAngleY = f3 / 57.295776F;
      this.bb_main.rotateAngleX = f4 / 57.295776F;
      this.mainChild2.rotateAngleY = f3 / 57.295776F;
      this.mainChild2.rotateAngleX = f4 / 57.295776F;
      this.mainChild1.rotateAngleY = f3 / 57.295776F;
      this.mainChild1.rotateAngleX = f4 / 57.295776F;
      this.mainChild0.rotateAngleY = f3 / 57.295776F;
      this.mainChild0.rotateAngleX = f4 / 57.295776F;
      this.mainChild10.rotateAngleY = f3 / 57.295776F;
      this.mainChild10.rotateAngleX = f4 / 57.295776F;
      this.mainChild.rotateAngleY = f3 / 57.295776F;
      this.mainChild.rotateAngleX = f4 / 57.295776F;
    }
  }
}

