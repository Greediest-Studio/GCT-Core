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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityRemnantWandering {


  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(RemnantWanderingEntity.class, renderManager -> new RenderLiving(renderManager, new ModelRemnant(), 0.5F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/modelremnant-texture.png");
          }
        });
  }

  public static class RemnantWanderingEntity extends EntityMob {
    public RemnantWanderingEntity(World world) {
      super(world);
      setSize(0.6F, 1.8F);
      this.experienceValue = 10;
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
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("gctcore:remnant_ambient"));
    }

    public SoundEvent getHurtSound(DamageSource ds) {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.hurt"));
    }

    public SoundEvent getDeathSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.death"));
    }

    protected float getSoundVolume() {
      return 1.0F;
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(80.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
    }
  }

  public static class ModelRemnant extends ModelBase {
    private final ModelRenderer body;

    private final ModelRenderer head;

    private final ModelRenderer headChild_1;

    private final ModelRenderer headChild;

    private final ModelRenderer leg1;

    private final ModelRenderer leg1Child;

    private final ModelRenderer leg1ChildChild;

    private final ModelRenderer leg1ChildChildChild;

    private final ModelRenderer leg1ChildChildChildChild;

    private final ModelRenderer leg4;

    private final ModelRenderer leg4Child;

    private final ModelRenderer leg4ChildChild;

    private final ModelRenderer leg4ChildChildChild;

    private final ModelRenderer leg4ChildChildChildChild;

    private final ModelRenderer leg2;

    private final ModelRenderer leg2Child;

    private final ModelRenderer leg2ChildChild;

    private final ModelRenderer leg2ChildChildChild;

    private final ModelRenderer leg2ChildChildChildChild;

    private final ModelRenderer leg3;

    private final ModelRenderer leg3Child;

    private final ModelRenderer leg3ChildChild;

    private final ModelRenderer leg3ChildChildChild;

    private final ModelRenderer leg3ChildChildChildChild;

    private final ModelRenderer rightarm;

    private final ModelRenderer leftarm;

    private final ModelRenderer cube_r1;

    private final ModelRenderer leftarmChild_2;

    private final ModelRenderer leftarmChild_1;

    private final ModelRenderer leftarmChild_3;

    private final ModelRenderer leftarmChild;

    public ModelRemnant() {
      this.textureWidth = 128;
      this.textureHeight = 64;
      this.body = new ModelRenderer(this);
      this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.body.cubeList.add(new ModelBox(this.body, 16, 16, -4.0F, 0.0F, -2.0F, 8, 22, 4, 0.0F, false));
      this.head = new ModelRenderer(this);
      this.head.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
      this.headChild_1 = new ModelRenderer(this);
      this.headChild_1.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.head.addChild(this.headChild_1);
      setRotationAngle(this.headChild_1, 0.0F, 0.5236F, 0.0F);
      this.headChild_1.cubeList.add(new ModelBox(this.headChild_1, 32, 0, -2.6F, -8.0F, -6.0F, 6, 8, 1, 0.0F, false));
      this.headChild = new ModelRenderer(this);
      this.headChild.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.head.addChild(this.headChild);
      setRotationAngle(this.headChild, 0.0F, -0.5236F, 0.0F);
      this.headChild.cubeList.add(new ModelBox(this.headChild, 32, 0, -3.4F, -8.0F, -6.0F, 6, 8, 1, 0.0F, false));
      this.leg1 = new ModelRenderer(this);
      this.leg1.setRotationPoint(1.0F, 23.0F, 0.0F);
      setRotationAngle(this.leg1, 0.0F, 0.6662F, 0.0F);
      this.leg1.cubeList.add(new ModelBox(this.leg1, 0, 42, 0.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg1Child = new ModelRenderer(this);
      this.leg1Child.setRotationPoint(2.0F, 0.0F, 0.0F);
      this.leg1.addChild(this.leg1Child);
      this.leg1Child.cubeList.add(new ModelBox(this.leg1Child, 0, 42, 0.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg1ChildChild = new ModelRenderer(this);
      this.leg1ChildChild.setRotationPoint(2.0F, 0.0F, 0.0F);
      this.leg1Child.addChild(this.leg1ChildChild);
      this.leg1ChildChild.cubeList.add(new ModelBox(this.leg1ChildChild, 0, 42, 0.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg1ChildChildChild = new ModelRenderer(this);
      this.leg1ChildChildChild.setRotationPoint(-1.0F, -23.0F, 0.0F);
      this.leg1ChildChild.addChild(this.leg1ChildChildChild);
      this.leg1ChildChildChild.cubeList.add(new ModelBox(this.leg1ChildChildChild, 0, 42, 0.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg1ChildChildChildChild = new ModelRenderer(this);
      this.leg1ChildChildChildChild.setRotationPoint(2.0F, 0.0F, 0.0F);
      this.leg1ChildChildChild.addChild(this.leg1ChildChildChildChild);
      this.leg1ChildChildChildChild.cubeList.add(new ModelBox(this.leg1ChildChildChildChild, 0, 42, 0.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg4 = new ModelRenderer(this);
      this.leg4.setRotationPoint(0.0F, 23.0F, -1.0F);
      setRotationAngle(this.leg4, 0.0F, 0.6662F, 0.0F);
      this.leg4.cubeList.add(new ModelBox(this.leg4, 2, 42, -1.0F, -1.0F, -2.0F, 2, 2, 2, 0.0F, false));
      this.leg4Child = new ModelRenderer(this);
      this.leg4Child.setRotationPoint(0.0F, 0.0F, -2.0F);
      this.leg4.addChild(this.leg4Child);
      this.leg4Child.cubeList.add(new ModelBox(this.leg4Child, 2, 42, -1.0F, -1.0F, -2.0F, 2, 2, 2, 0.0F, false));
      this.leg4ChildChild = new ModelRenderer(this);
      this.leg4ChildChild.setRotationPoint(0.0F, -23.0F, -1.0F);
      this.leg4Child.addChild(this.leg4ChildChild);
      this.leg4ChildChild.cubeList.add(new ModelBox(this.leg4ChildChild, 2, 42, -1.0F, -1.0F, -2.0F, 2, 2, 2, 0.0F, false));
      this.leg4ChildChildChild = new ModelRenderer(this);
      this.leg4ChildChildChild.setRotationPoint(0.0F, 0.0F, -2.0F);
      this.leg4ChildChild.addChild(this.leg4ChildChildChild);
      this.leg4ChildChildChild.cubeList.add(new ModelBox(this.leg4ChildChildChild, 2, 42, -1.0F, -1.0F, -2.0F, 2, 2, 2, 0.0F, false));
      this.leg4ChildChildChildChild = new ModelRenderer(this);
      this.leg4ChildChildChildChild.setRotationPoint(0.0F, 0.0F, -2.0F);
      this.leg4ChildChildChild.addChild(this.leg4ChildChildChildChild);
      this.leg4ChildChildChildChild.cubeList.add(new ModelBox(this.leg4ChildChildChildChild, 2, 42, -1.0F, -1.0F, -2.0F, 2, 2, 2, 0.0F, false));
      this.leg2 = new ModelRenderer(this);
      this.leg2.setRotationPoint(-1.0F, 23.0F, 0.0F);
      setRotationAngle(this.leg2, 0.0F, 0.6662F, 0.0F);
      this.leg2.cubeList.add(new ModelBox(this.leg2, 0, 42, -2.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg2Child = new ModelRenderer(this);
      this.leg2Child.setRotationPoint(-2.0F, 0.0F, 0.0F);
      this.leg2.addChild(this.leg2Child);
      this.leg2Child.cubeList.add(new ModelBox(this.leg2Child, 0, 42, -2.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg2ChildChild = new ModelRenderer(this);
      this.leg2ChildChild.setRotationPoint(-1.0F, -23.0F, 0.0F);
      this.leg2Child.addChild(this.leg2ChildChild);
      this.leg2ChildChild.cubeList.add(new ModelBox(this.leg2ChildChild, 0, 42, -2.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg2ChildChildChild = new ModelRenderer(this);
      this.leg2ChildChildChild.setRotationPoint(-2.0F, 0.0F, 0.0F);
      this.leg2ChildChild.addChild(this.leg2ChildChildChild);
      this.leg2ChildChildChild.cubeList.add(new ModelBox(this.leg2ChildChildChild, 0, 42, -2.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg2ChildChildChildChild = new ModelRenderer(this);
      this.leg2ChildChildChildChild.setRotationPoint(-2.0F, 0.0F, 0.0F);
      this.leg2ChildChildChild.addChild(this.leg2ChildChildChildChild);
      this.leg2ChildChildChildChild.cubeList.add(new ModelBox(this.leg2ChildChildChildChild, 0, 42, -2.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F, false));
      this.leg3 = new ModelRenderer(this);
      this.leg3.setRotationPoint(0.0F, 23.0F, 1.0F);
      setRotationAngle(this.leg3, 0.0F, 0.6662F, 0.0F);
      this.leg3.cubeList.add(new ModelBox(this.leg3, 2, 42, -1.0F, -1.0F, 0.0F, 2, 2, 2, 0.0F, false));
      this.leg3Child = new ModelRenderer(this);
      this.leg3Child.setRotationPoint(0.0F, 0.0F, 2.0F);
      this.leg3.addChild(this.leg3Child);
      this.leg3Child.cubeList.add(new ModelBox(this.leg3Child, 2, 42, -1.0F, -1.0F, 0.0F, 2, 2, 2, 0.0F, false));
      this.leg3ChildChild = new ModelRenderer(this);
      this.leg3ChildChild.setRotationPoint(0.0F, -23.0F, 1.0F);
      this.leg3Child.addChild(this.leg3ChildChild);
      this.leg3ChildChild.cubeList.add(new ModelBox(this.leg3ChildChild, 2, 42, -1.0F, -1.0F, 0.0F, 2, 2, 2, 0.0F, false));
      this.leg3ChildChildChild = new ModelRenderer(this);
      this.leg3ChildChildChild.setRotationPoint(0.0F, 0.0F, 2.0F);
      this.leg3ChildChild.addChild(this.leg3ChildChildChild);
      this.leg3ChildChildChild.cubeList.add(new ModelBox(this.leg3ChildChildChild, 2, 42, -1.0F, -1.0F, 0.0F, 2, 2, 2, 0.0F, false));
      this.leg3ChildChildChildChild = new ModelRenderer(this);
      this.leg3ChildChildChildChild.setRotationPoint(0.0F, 0.0F, -2.0F);
      this.leg3ChildChildChild.addChild(this.leg3ChildChildChildChild);
      this.leg3ChildChildChildChild.cubeList.add(new ModelBox(this.leg3ChildChildChildChild, 2, 42, -1.0F, -1.0F, 0.0F, 2, 2, 2, 0.0F, false));
      this.rightarm = new ModelRenderer(this);
      this.rightarm.setRotationPoint(-4.0F, 2.0F, 0.0F);
      this.rightarm.cubeList.add(new ModelBox(this.rightarm, 0, 20, -4.0F, -2.0F, -2.0F, 4, 16, 4, 0.0F, false));
      this.leftarm = new ModelRenderer(this);
      this.leftarm.setRotationPoint(4.0F, 2.0F, 0.0F);
      this.leftarm.cubeList.add(new ModelBox(this.leftarm, 0, 20, 0.0F, -2.0F, -2.0F, 4, 10, 4, 0.0F, false));
      this.cube_r1 = new ModelRenderer(this);
      this.cube_r1.setRotationPoint(-10.0F, 0.0F, 0.0F);
      this.leftarm.addChild(this.cube_r1);
      setRotationAngle(this.cube_r1, 0.0F, -1.5708F, 0.0F);
      this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 0, 28, -4.0F, 2.0F, 2.0F, 8, 12, 2, 0.0F, false));
      this.leftarmChild_2 = new ModelRenderer(this);
      this.leftarmChild_2.setRotationPoint(1.0F, 8.0F, -1.0F);
      this.leftarm.addChild(this.leftarmChild_2);
      setRotationAngle(this.leftarmChild_2, 0.0F, 0.0F, 0.1134F);
      this.leftarmChild_2.cubeList.add(new ModelBox(this.leftarmChild_2, 0, 46, -1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, false));
      this.leftarmChild_1 = new ModelRenderer(this);
      this.leftarmChild_1.setRotationPoint(3.0F, 8.0F, 1.0F);
      this.leftarm.addChild(this.leftarmChild_1);
      setRotationAngle(this.leftarmChild_1, 0.0F, 0.0F, 0.1134F);
      this.leftarmChild_1.cubeList.add(new ModelBox(this.leftarmChild_1, 0, 46, -1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, false));
      this.leftarmChild_3 = new ModelRenderer(this);
      this.leftarmChild_3.setRotationPoint(3.0F, 8.0F, -1.0F);
      this.leftarm.addChild(this.leftarmChild_3);
      setRotationAngle(this.leftarmChild_3, 0.1833F, 0.0F, 0.0F);
      this.leftarmChild_3.cubeList.add(new ModelBox(this.leftarmChild_3, 0, 46, -1.0F, 0.0F, -1.0F, 2, 9, 2, 0.0F, false));
      this.leftarmChild = new ModelRenderer(this);
      this.leftarmChild.setRotationPoint(1.0F, 8.0F, 1.0F);
      this.leftarm.addChild(this.leftarmChild);
      setRotationAngle(this.leftarmChild, 0.1833F, 0.0F, 0.0F);
      this.leftarmChild.cubeList.add(new ModelBox(this.leftarmChild, 0, 46, -1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, false));
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.body.render(f5);
      this.head.render(f5);
      this.leg1.render(f5);
      this.leg4.render(f5);
      this.leg2.render(f5);
      this.leg3.render(f5);
      this.rightarm.render(f5);
      this.leftarm.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
      this.leg2ChildChildChild.rotateAngleY = f2 / 20.0F;
      this.leg3ChildChildChildChild.rotateAngleY = f2 / 20.0F;
      this.leg3Child.rotateAngleY = f2 / 20.0F;
      this.leg2ChildChild.rotateAngleY = f2 / 20.0F;
      this.leg1ChildChildChildChild.rotateAngleY = f2 / 20.0F;
      this.leftarmChild.rotateAngleX = MathHelper.cos(f * 0.6662F) * f1;
      this.cube_r1.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.1415927F) * f1;
      this.head.rotateAngleY = f3 / 57.295776F;
      this.head.rotateAngleX = f4 / 57.295776F;
      this.leg1.rotateAngleY = f2 / 20.0F;
      this.leg2Child.rotateAngleY = f2 / 20.0F;
      this.leg4.rotateAngleY = f2 / 20.0F;
      this.leftarmChild_1.rotateAngleX = MathHelper.cos(f * 0.6662F) * f1;
      this.leg2.rotateAngleY = f2 / 20.0F;
      this.leftarmChild_2.rotateAngleX = MathHelper.cos(f * 0.6662F) * f1;
      this.leg3.rotateAngleY = f2 / 20.0F;
      this.leftarmChild_3.rotateAngleX = MathHelper.cos(f * 0.6662F) * f1;
      this.leg4ChildChildChildChild.rotateAngleY = f2 / 20.0F;
      this.leg3ChildChildChild.rotateAngleY = f2 / 20.0F;
      this.leg4ChildChild.rotateAngleY = f2 / 20.0F;
      this.leg4Child.rotateAngleY = f2 / 20.0F;
      this.rightarm.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.1415927F) * f1;
      this.leg1ChildChild.rotateAngleY = f2 / 20.0F;
      this.leg4ChildChildChild.rotateAngleY = f2 / 20.0F;
      this.leg1Child.rotateAngleY = f2 / 20.0F;
      this.leg2ChildChildChildChild.rotateAngleY = f2 / 20.0F;
      this.leg3ChildChild.rotateAngleY = f2 / 20.0F;
      this.headChild_1.rotateAngleY = f3 / 57.295776F;
      this.headChild_1.rotateAngleX = f4 / 57.295776F;
      this.headChild.rotateAngleY = f3 / 57.295776F;
      this.headChild.rotateAngleX = f4 / 57.295776F;
      this.leftarm.rotateAngleX = MathHelper.cos(f * 0.6662F) * f1;
      this.leg1ChildChildChild.rotateAngleY = f2 / 20.0F;
    }
  }
}

