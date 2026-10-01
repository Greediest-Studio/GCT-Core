package com.gmm.gctold.common.entity;

import net.minecraft.client.model.ModelBase;
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
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityRottened {


  public static void init(FMLInitializationEvent event) {
    Biome[] spawnBiomes = { (Biome)Biome.REGISTRY.getObject(new ResourceLocation("gctcore:bloodyplain")), (Biome)Biome.REGISTRY.getObject(new ResourceLocation("gctcore:curruptplain")) };
    EntityRegistry.addSpawn(RottenedEntity.class, 30, 4, 4, EnumCreatureType.MONSTER, spawnBiomes);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(RottenedEntity.class, renderManager -> new RenderLiving(renderManager, new ModelZombie(), 0.5F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/rottened.png");
          }
        });
  }

  public static class RottenedEntity extends EntityZombie {
    public RottenedEntity(World world) {
      super(world);
      setSize(0.6F, 1.8F);
      this.experienceValue = 5;
      this.isImmuneToFire = true;
      setNoAI(false);
    }

    protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, (EntityAIBase)new EntityAIAttackMelee((EntityCreature)this, 1.2D, false));
      this.tasks.addTask(2, (EntityAIBase)new EntityAIWander((EntityCreature)this, 1.0D));
      this.targetTasks.addTask(3, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
      this.tasks.addTask(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
      this.tasks.addTask(5, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
    }

    public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEAD;
    }

    protected Item getDropItem() {
      return null;
    }

    public SoundEvent getAmbientSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.zombie.ambient"));
    }

    public SoundEvent getHurtSound(DamageSource ds) {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.zombie.hurt"));
    }

    public SoundEvent getDeathSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.zombie.death"));
    }

    protected float getSoundVolume() {
      return 1.0F;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
      if (source.getImmediateSource() instanceof net.minecraft.entity.projectile.EntityPotion)
        return false;
      return super.attackEntityFrom(source, amount);
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
    }
  }

  public static class ModelZombie extends ModelBase {
    public ModelRenderer bipedRightArm;

    public ModelRenderer bipedRightLeg;

    public ModelRenderer bipedHead;

    public ModelRenderer bipedBody;

    public ModelRenderer bipedLeftArm;

    public ModelRenderer bipedLeftLeg;

    public ModelRenderer bipedHeadwear;

    public ModelZombie() {
      this.textureWidth = 64;
      this.textureHeight = 64;
      this.bipedHead = new ModelRenderer(this, 0, 0);
      this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
      this.bipedRightArm = new ModelRenderer(this, 40, 16);
      this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
      this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
      setRotateAngle(this.bipedRightArm, -1.3962635F, -0.1F, 0.1F);
      this.bipedLeftArm = new ModelRenderer(this, 40, 16);
      this.bipedLeftArm.mirror = true;
      this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
      this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
      setRotateAngle(this.bipedLeftArm, -1.3962635F, 0.1F, -0.1F);
      this.bipedHeadwear = new ModelRenderer(this, 32, 0);
      this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
      this.bipedRightLeg = new ModelRenderer(this, 0, 16);
      this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.1F);
      this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
      this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
      this.bipedLeftLeg.mirror = true;
      this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.1F);
      this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
      this.bipedBody = new ModelRenderer(this, 16, 16);
      this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.bipedHead.render(f5);
      this.bipedRightArm.render(f5);
      this.bipedLeftArm.render(f5);
      this.bipedHeadwear.render(f5);
      this.bipedRightLeg.render(f5);
      this.bipedLeftLeg.render(f5);
      this.bipedBody.render(f5);
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
      this.bipedRightArm.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.1415927F) * f1;
      this.bipedRightLeg.rotateAngleX = MathHelper.cos(f * 1.0F) * 1.0F * f1;
      this.bipedHead.rotateAngleY = f3 / 57.295776F;
      this.bipedHead.rotateAngleX = f4 / 57.295776F;
      this.bipedLeftArm.rotateAngleX = MathHelper.cos(f * 0.6662F) * f1;
      this.bipedLeftLeg.rotateAngleX = MathHelper.cos(f * 1.0F) * -1.0F * f1;
      this.bipedHeadwear.rotateAngleY = f3 / 57.295776F;
      this.bipedHeadwear.rotateAngleX = f4 / 57.295776F;
    }
  }
}

