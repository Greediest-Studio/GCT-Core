package com.gmm.gctold.common.entity;

import java.util.ArrayList;
import java.util.Iterator;
import com.gmm.gctold.common.events.SanityEvents;
import com.gmm.gctold.common.items.ItemShoggothTancale;
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
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityBloodyShoggoth {


  private Biome[] allbiomes(RegistryNamespaced<ResourceLocation, Biome> in) {
    Iterator<Biome> itr = in.iterator();
    ArrayList<Biome> ls = new ArrayList<>();
    while (itr.hasNext())
      ls.add(itr.next());
    return ls.<Biome>toArray(new Biome[ls.size()]);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(BloodyShoggothEntity.class, renderManager -> new RenderLiving(renderManager, new ModelShoggoth(), 1.0F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/shoggoth_bloody.png");
          }
        });
  }

  public static class BloodyShoggothEntity extends EntityMob {
    public BloodyShoggothEntity(World world) {
      super(world);
      setSize(2.5F, 2.5F);
      this.experienceValue = 20;
      this.isImmuneToFire = true;
      setNoAI(false);
      enablePersistence();
    }

    protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, (EntityAIBase)new EntityAIAttackMelee((EntityCreature)this, 1.2D, false));
      this.targetTasks.addTask(2, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this,
          EntityPlayer.class, 10, false, false, player -> SanityEvents.canSanityTargetPlayer((EntityPlayer) player)));
      this.targetTasks.addTask(3, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityCreature.class, false, false));
      this.tasks.addTask(4, (EntityAIBase)new EntityAIWander((EntityCreature)this, 1.0D));
      this.targetTasks.addTask(5, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
      this.tasks.addTask(6, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
      this.tasks.addTask(7, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
    }

    public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEFINED;
    }

    protected boolean canDespawn() {
      return false;
    }

    protected Item getDropItem() {
      return (new ItemStack(ItemShoggothTancale.block, 1)).getItem();
    }

    public SoundEvent getAmbientSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("gctcore:shoggoth_ambient"));
    }

    public SoundEvent getHurtSound(DamageSource ds) {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("gctcore:shoggoth_hurt"));
    }

    public SoundEvent getDeathSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("gctcore:shoggoth_death"));
    }

    protected float getSoundVolume() {
      return 1.0F;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
      if (source.getImmediateSource() instanceof net.minecraft.entity.projectile.EntityArrow)
        return false;
      if (source.getImmediateSource() instanceof net.minecraft.entity.projectile.EntityPotion)
        return false;
      return super.attackEntityFrom(source, amount);
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(30.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(600.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(22.0D);
    }
  }

  public static class ModelShoggoth extends ModelBase {
    private final ModelRenderer mouthBack;

    private final ModelRenderer mouthBackChild;

    private final ModelRenderer mouthBackChildChild;

    private final ModelRenderer mouthBackChildChildChild;

    private final ModelRenderer mouthBackChildChildChild_1;

    private final ModelRenderer mouthRight;

    private final ModelRenderer mouthBottom;

    private final ModelRenderer mouthBottomChild;

    private final ModelRenderer mouthBottomChild_1;

    private final ModelRenderer mouthUpper;

    private final ModelRenderer mouthUpperChild_1;

    private final ModelRenderer mouthUpperChild_2;

    private final ModelRenderer mouthUpperChild;

    private final ModelRenderer sideSmall;

    private final ModelRenderer sideSmall_r1;

    private final ModelRenderer sideLarge;

    private final ModelRenderer eyeTentacleLower;

    private final ModelRenderer eyeTentacleLowerChild;

    private final ModelRenderer eyeTentacleLowerChildChild;

    private final ModelRenderer eyeTentacleLowerChildChildChild;

    private final ModelRenderer mouthLeft;

    private final ModelRenderer rightTentacleLower;

    private final ModelRenderer rightTentacleLowerChild;

    private final ModelRenderer bottom;

    private final ModelRenderer bottom_r1;

    private final ModelRenderer bottomChild_2;

    private final ModelRenderer bottomChildChild_2;

    private final ModelRenderer bottomChildChildChild_2;

    private final ModelRenderer bottomChildChildChildChild_2;

    private final ModelRenderer bottomChild;

    private final ModelRenderer bottomChildChild;

    private final ModelRenderer bottomChildChildChild;

    private final ModelRenderer bottomChildChildChildChild;

    private final ModelRenderer bottomChild_1;

    private final ModelRenderer bottomChildChild_1;

    private final ModelRenderer bottomChildChildChild_1;

    private final ModelRenderer bottomChildChildChildChild_1;

    private final ModelRenderer leftTentacleLower;

    private final ModelRenderer leftTentacleLowerChild;

    private final ModelRenderer abyssCube;

    private final ModelRenderer cube_r1;

    private final ModelRenderer cube_r2;

    private final ModelRenderer cube_r3;

    private final ModelRenderer cube_r4;

    private final ModelRenderer cube_r5;

    private final ModelRenderer cube_r6;

    private final ModelRenderer cube_r7;

    private final ModelRenderer cube_r8;

    private final ModelRenderer cube_r9;

    private final ModelRenderer cube_r10;

    private final ModelRenderer cube_r11;

    private final ModelRenderer cube_r12;

    private final ModelRenderer cube_r13;

    private final ModelRenderer otherChunk;

    public ModelShoggoth() {
      this.textureWidth = 128;
      this.textureHeight = 128;
      this.mouthBack = new ModelRenderer(this);
      this.mouthBack.setRotationPoint(-9.0F, -15.0F, 8.0F);
      setRotationAngle(this.mouthBack, -0.3129F, 0.0F, 0.0F);
      this.mouthBack.cubeList.add(new ModelBox(this.mouthBack, 21, 17, -14.0F, -10.4658F, -3.3864F, 28, 25, 25, 0.0F, false));
      this.mouthBackChild = new ModelRenderer(this);
      this.mouthBackChild.setRotationPoint(0.0F, 1.0F, 20.0F);
      this.mouthBack.addChild(this.mouthBackChild);
      setRotationAngle(this.mouthBackChild, -0.5918F, 0.0F, 0.0F);
      this.mouthBackChild.cubeList.add(new ModelBox(this.mouthBackChild, 0, 0, -2.0F, -22.7965F, -8.6492F, 4, 16, 4, 0.0F, false));
      this.mouthBackChildChild = new ModelRenderer(this);
      this.mouthBackChildChild.setRotationPoint(0.0F, -15.3F, 1.0F);
      this.mouthBackChild.addChild(this.mouthBackChildChild);
      setRotationAngle(this.mouthBackChildChild, -0.6829F, 0.0F, 0.0F);
      this.mouthBackChildChild.cubeList.add(new ModelBox(this.mouthBackChildChild, 0, 0, -1.5F, -9.8138F, -11.8984F, 3, 10, 3, 0.0F, false));
      this.mouthBackChildChildChild = new ModelRenderer(this);
      this.mouthBackChildChildChild.setRotationPoint(0.0F, -10.0F, -1.0F);
      this.mouthBackChildChild.addChild(this.mouthBackChildChildChild);
      setRotationAngle(this.mouthBackChildChildChild, 1.3203F, 0.0F, 0.0F);
      this.mouthBackChildChildChild.cubeList.add(new ModelBox(this.mouthBackChildChildChild, -8, 77, -4.0F, -10.6091F, -2.9063F, 8, 1, 8, 0.0F, false));
      this.mouthBackChildChildChild_1 = new ModelRenderer(this);
      this.mouthBackChildChildChild_1.setRotationPoint(0.0F, -10.0F, 0.0F);
      this.mouthBackChildChild.addChild(this.mouthBackChildChildChild_1);
      setRotationAngle(this.mouthBackChildChildChild_1, 0.7285F, 0.0F, 0.0F);
      this.mouthBackChildChildChild_1.cubeList.add(new ModelBox(this.mouthBackChildChildChild_1, 6, 77, -4.0F, -7.1833F, -8.3307F, 8, 1, 8, 0.0F, false));
      this.mouthRight = new ModelRenderer(this);
      this.mouthRight.setRotationPoint(-24.0F, -15.0F, -15.0F);
      this.mouthRight.cubeList.add(new ModelBox(this.mouthRight, 51, 44, 0.0F, -11.0F, 0.0F, 4, 25, 23, 0.0F, false));
      this.mouthBottom = new ModelRenderer(this);
      this.mouthBottom.setRotationPoint(-9.0F, 10.0F, -15.0F);
      setRotationAngle(this.mouthBottom, 0.2182F, 0.0F, 0.0F);
      this.mouthBottom.cubeList.add(new ModelBox(this.mouthBottom, 28, 19, -15.0F, -10.7393F, 2.3808F, 30, 8, 47, 0.0F, false));
      this.mouthBottomChild = new ModelRenderer(this);
      this.mouthBottomChild.setRotationPoint(4.0F, 0.0F, 0.0F);
      this.mouthBottom.addChild(this.mouthBottomChild);
      this.mouthBottomChild.cubeList.add(new ModelBox(this.mouthBottomChild, 0, 22, -2.0F, -34.7393F, 2.3808F, 5, 24, 4, 0.0F, false));
      this.mouthBottomChild_1 = new ModelRenderer(this);
      this.mouthBottomChild_1.setRotationPoint(-5.0F, 0.0F, 0.0F);
      this.mouthBottom.addChild(this.mouthBottomChild_1);
      this.mouthBottomChild_1.cubeList.add(new ModelBox(this.mouthBottomChild_1, 13, 22, -2.0F, -34.7393F, 2.3808F, 5, 24, 4, 0.0F, false));
      this.mouthUpper = new ModelRenderer(this);
      this.mouthUpper.setRotationPoint(-9.0F, -15.0F, -15.0F);
      setRotationAngle(this.mouthUpper, -0.0436F, 0.0F, 0.0F);
      this.mouthUpper.cubeList.add(new ModelBox(this.mouthUpper, 0, 0, -11.0F, -10.9895F, -0.4798F, 22, 1, 23, 0.0F, false));
      this.mouthUpperChild_1 = new ModelRenderer(this);
      this.mouthUpperChild_1.setRotationPoint(-9.0F, 1.0F, 0.0F);
      this.mouthUpper.addChild(this.mouthUpperChild_1);
      this.mouthUpperChild_1.cubeList.add(new ModelBox(this.mouthUpperChild_1, -13, 0, -2.0F, -10.9895F, -0.4798F, 4, 24, 4, 0.0F, false));
      this.mouthUpperChild_2 = new ModelRenderer(this);
      this.mouthUpperChild_2.setRotationPoint(0.0F, 1.0F, 0.0F);
      this.mouthUpper.addChild(this.mouthUpperChild_2);
      this.mouthUpperChild_2.cubeList.add(new ModelBox(this.mouthUpperChild_2, 19, 0, -2.0F, -10.9895F, -0.4798F, 4, 24, 4, 0.0F, false));
      this.mouthUpperChild = new ModelRenderer(this);
      this.mouthUpperChild.setRotationPoint(9.0F, 1.0F, 0.0F);
      this.mouthUpper.addChild(this.mouthUpperChild);
      this.mouthUpperChild.cubeList.add(new ModelBox(this.mouthUpperChild, 19, 0, -2.0F, -10.9895F, -0.4798F, 4, 24, 4, 0.0F, false));
      this.sideSmall = new ModelRenderer(this);
      this.sideSmall.setRotationPoint(7.0F, 10.0F, -18.0F);
      setRotationAngle(this.sideSmall, 0.0911F, 0.0F, 0.182F);
      this.sideSmall_r1 = new ModelRenderer(this);
      this.sideSmall_r1.setRotationPoint(-7.0F, 14.0F, 18.0F);
      this.sideSmall.addChild(this.sideSmall_r1);
      setRotationAngle(this.sideSmall_r1, 0.0F, 0.0F, -0.0873F);
      this.sideSmall_r1.cubeList.add(new ModelBox(this.sideSmall_r1, 0, 0, 5.9552F, -24.9059F, -17.0158F, 22, 7, 18, 0.0F, false));
      this.sideLarge = new ModelRenderer(this);
      this.sideLarge.setRotationPoint(6.0F, -1.0F, 0.0F);
      setRotationAngle(this.sideLarge, 0.0F, 0.4363F, 0.2276F);
      this.sideLarge.cubeList.add(new ModelBox(this.sideLarge, 26, 1, -2.2494F, -10.7163F, -1.0489F, 22, 18, 27, 0.0F, false));
      this.eyeTentacleLower = new ModelRenderer(this);
      this.eyeTentacleLower.setRotationPoint(0.2F, -13.5F, -7.0F);
      this.eyeTentacleLower.cubeList.add(new ModelBox(this.eyeTentacleLower, 0, 0, -3.0F, -24.0F, 0.0F, 6, 13, 6, 0.0F, false));
      this.eyeTentacleLowerChild = new ModelRenderer(this);
      this.eyeTentacleLowerChild.setRotationPoint(0.0F, -11.0F, 1.5F);
      this.eyeTentacleLower.addChild(this.eyeTentacleLowerChild);
      setRotationAngle(this.eyeTentacleLowerChild, 0.2731F, 0.0F, 0.0F);
      this.eyeTentacleLowerChild.cubeList.add(new ModelBox(this.eyeTentacleLowerChild, 0, 0, -2.0F, -27.5922F, 2.9674F, 4, 17, 4, 0.0F, false));
      this.eyeTentacleLowerChildChild = new ModelRenderer(this);
      this.eyeTentacleLowerChildChild.setRotationPoint(0.0F, -15.0F, 0.0F);
      this.eyeTentacleLowerChild.addChild(this.eyeTentacleLowerChildChild);
      setRotationAngle(this.eyeTentacleLowerChildChild, 0.8652F, 0.0F, 0.0F);
      this.eyeTentacleLowerChildChild.cubeList.add(new ModelBox(this.eyeTentacleLowerChildChild, 0, 0, -1.5F, -18.6105F, 9.9872F, 3, 14, 3, 0.0F, false));
      this.eyeTentacleLowerChildChildChild = new ModelRenderer(this);
      this.eyeTentacleLowerChildChildChild.setRotationPoint(0.0F, -10.0F, -1.5F);
      this.eyeTentacleLowerChildChild.addChild(this.eyeTentacleLowerChildChildChild);
      setRotationAngle(this.eyeTentacleLowerChildChildChild, 0.1367F, 0.0F, 0.0F);
      this.eyeTentacleLowerChildChildChild.cubeList
        .add(new ModelBox(this.eyeTentacleLowerChildChildChild, 10, 54, -3.0F, -8.2069F, 10.5222F, 6, 6, 6, 0.0F, false));
      this.mouthLeft = new ModelRenderer(this);
      this.mouthLeft.setRotationPoint(4.0F, -15.0F, -15.0F);
      this.mouthLeft.cubeList.add(new ModelBox(this.mouthLeft, 0, 0, -2.0F, -11.0F, 0.0F, 4, 25, 23, 0.0F, false));
      this.rightTentacleLower = new ModelRenderer(this);
      this.rightTentacleLower.setRotationPoint(-22.0F, -4.0F, 15.3F);
      setRotationAngle(this.rightTentacleLower, 0.0F, 1.639F, -1.1839F);
      this.rightTentacleLower.cubeList.add(new ModelBox(this.rightTentacleLower, 0, 0, -2.6946F, -18.1509F, 10.163F, 4, 14, 4, 0.0F, false));
      this.rightTentacleLowerChild = new ModelRenderer(this);
      this.rightTentacleLowerChild.setRotationPoint(0.0F, -14.0F, 0.0F);
      this.rightTentacleLower.addChild(this.rightTentacleLowerChild);
      setRotationAngle(this.rightTentacleLowerChild, -0.5463F, 0.0F, 0.2731F);
      this.rightTentacleLowerChild.cubeList.add(new ModelBox(this.rightTentacleLowerChild, 0, 0, -2.7886F, -20.5351F, 6.7047F, 2, 12, 2, 0.0F, false));
      this.bottom = new ModelRenderer(this);
      this.bottom.setRotationPoint(0.0F, 17.0F, -21.0F);
      this.bottom_r1 = new ModelRenderer(this);
      this.bottom_r1.setRotationPoint(0.0F, -4.0F, 21.0F);
      this.bottom.addChild(this.bottom_r1);
      setRotationAngle(this.bottom_r1, 0.0F, 0.1309F, 0.0F);
      this.bottom_r1.cubeList.add(new ModelBox(this.bottom_r1, 50, 15, -27.0F, -7.0F, -21.0F, 52, 8, 52, 0.0F, false));
      this.bottomChild_2 = new ModelRenderer(this);
      this.bottomChild_2.setRotationPoint(21.0F, -0.2F, 19.0F);
      this.bottom.addChild(this.bottomChild_2);
      setRotationAngle(this.bottomChild_2, 0.0F, -1.5708F, 0.1309F);
      this.bottomChild_2.cubeList.add(new ModelBox(this.bottomChild_2, 0, 0, -8.0F, -10.9059F, -4.5642F, 16, 8, 7, 0.0F, false));
      this.bottomChildChild_2 = new ModelRenderer(this);
      this.bottomChildChild_2.setRotationPoint(0.0F, 0.0F, -6.0F);
      this.bottomChild_2.addChild(this.bottomChildChild_2);
      this.bottomChildChild_2.cubeList.add(new ModelBox(this.bottomChildChild_2, 0, 0, -6.0F, -10.9059F, -15.5642F, 12, 9, 17, 0.0F, false));
      this.bottomChildChildChild_2 = new ModelRenderer(this);
      this.bottomChildChildChild_2.setRotationPoint(0.0F, 1.9F, -15.7F);
      this.bottomChildChild_2.addChild(this.bottomChildChildChild_2);
      this.bottomChildChildChild_2.cubeList.add(new ModelBox(this.bottomChildChildChild_2, 0, 0, -4.0F, -10.9059F, -8.5642F, 8, 7, 10, 0.0F, false));
      this.bottomChildChildChildChild_2 = new ModelRenderer(this);
      this.bottomChildChildChildChild_2.setRotationPoint(0.0F, 2.0F, -8.0F);
      this.bottomChildChildChild_2.addChild(this.bottomChildChildChildChild_2);
      this.bottomChildChildChildChild_2.cubeList
        .add(new ModelBox(this.bottomChildChildChildChild_2, -21, -21, -3.0F, -10.9059F, -27.5642F, 6, 6, 29, 0.0F, false));
      this.bottomChild = new ModelRenderer(this);
      this.bottomChild.setRotationPoint(-18.0F, -2.0F, 8.0F);
      this.bottom.addChild(this.bottomChild);
      setRotationAngle(this.bottomChild, 0.1745F, 0.6545F, 0.0F);
      this.bottomChild.cubeList.add(new ModelBox(this.bottomChild, 0, 0, -8.0F, -11.0F, -6.0F, 16, 8, 7, 0.0F, false));
      this.bottomChildChild = new ModelRenderer(this);
      this.bottomChildChild.setRotationPoint(0.0F, 0.0F, -6.0F);
      this.bottomChild.addChild(this.bottomChildChild);
      this.bottomChildChild.cubeList.add(new ModelBox(this.bottomChildChild, 67, 33, -6.0F, -11.0F, -17.0F, 12, 9, 17, 0.0F, false));
      this.bottomChildChildChild = new ModelRenderer(this);
      this.bottomChildChildChild.setRotationPoint(0.0F, 1.9F, -15.0F);
      this.bottomChildChild.addChild(this.bottomChildChildChild);
      this.bottomChildChildChild.cubeList.add(new ModelBox(this.bottomChildChildChild, 0, 0, -4.0F, -11.0F, -10.0F, 8, 7, 10, 0.0F, false));
      this.bottomChildChildChildChild = new ModelRenderer(this);
      this.bottomChildChildChildChild.setRotationPoint(0.0F, 2.0F, -8.0F);
      this.bottomChildChildChild.addChild(this.bottomChildChildChildChild);
      this.bottomChildChildChildChild.cubeList.add(new ModelBox(this.bottomChildChildChildChild, -27, -27, -3.0F, -11.0F, -35.0F, 6, 6, 35, 0.0F, false));
      this.bottomChild_1 = new ModelRenderer(this);
      this.bottomChild_1.setRotationPoint(-6.0F, -0.1F, 51.0F);
      this.bottom.addChild(this.bottomChild_1);
      setRotationAngle(this.bottomChild_1, -3.0107F, 0.0873F, 3.1416F);
      this.bottomChild_1.cubeList.add(new ModelBox(this.bottomChild_1, 0, 0, -8.0F, -11.0F, -6.0F, 16, 8, 7, 0.0F, false));
      this.bottomChildChild_1 = new ModelRenderer(this);
      this.bottomChildChild_1.setRotationPoint(0.0F, 0.0F, -6.0F);
      this.bottomChild_1.addChild(this.bottomChildChild_1);
      this.bottomChildChild_1.cubeList.add(new ModelBox(this.bottomChildChild_1, 19, 100, -6.0F, -11.0F, -17.0F, 12, 9, 17, 0.0F, false));
      this.bottomChildChildChild_1 = new ModelRenderer(this);
      this.bottomChildChildChild_1.setRotationPoint(0.0F, 1.8F, -15.0F);
      this.bottomChildChild_1.addChild(this.bottomChildChildChild_1);
      this.bottomChildChildChild_1.cubeList.add(new ModelBox(this.bottomChildChildChild_1, 0, 0, -4.0F, -11.0F, -10.0F, 8, 7, 10, 0.0F, false));
      this.bottomChildChildChildChild_1 = new ModelRenderer(this);
      this.bottomChildChildChildChild_1.setRotationPoint(0.0F, 2.0F, -8.0F);
      this.bottomChildChildChild_1.addChild(this.bottomChildChildChildChild_1);
      this.bottomChildChildChildChild_1.cubeList
        .add(new ModelBox(this.bottomChildChildChildChild_1, -21, -21, -3.0F, -11.0F, -29.0F, 6, 6, 29, 0.0F, false));
      this.leftTentacleLower = new ModelRenderer(this);
      this.leftTentacleLower.setRotationPoint(6.0F, 0.0F, 8.0F);
      setRotationAngle(this.leftTentacleLower, 0.0F, 0.0F, 0.3643F);
      this.leftTentacleLower.cubeList.add(new ModelBox(this.leftTentacleLower, 0, 0, -5.9187F, -24.2783F, 0.0F, 4, 14, 4, 0.0F, false));
      this.leftTentacleLowerChild = new ModelRenderer(this);
      this.leftTentacleLowerChild.setRotationPoint(0.0F, -14.0F, 0.0F);
      this.leftTentacleLower.addChild(this.leftTentacleLowerChild);
      setRotationAngle(this.leftTentacleLowerChild, -0.5463F, 0.0F, 0.2731F);
      this.leftTentacleLowerChild.cubeList.add(new ModelBox(this.leftTentacleLowerChild, 0, 0, -7.5461F, -19.5535F, -4.5926F, 2, 12, 2, 0.0F, false));
      this.abyssCube = new ModelRenderer(this);
      this.abyssCube.setRotationPoint(0.0F, 24.0F, 0.0F);
      this.cube_r1 = new ModelRenderer(this);
      this.cube_r1.setRotationPoint(26.0F, -18.0F, -22.0F);
      this.abyssCube.addChild(this.cube_r1);
      setRotationAngle(this.cube_r1, 0.3169F, -0.6414F, -0.507F);
      this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 91, 108, -4.0F, -5.0F, -1.0F, 5, 5, 11, 0.0F, false));
      this.cube_r2 = new ModelRenderer(this);
      this.cube_r2.setRotationPoint(-6.0F, -52.0F, -19.0F);
      this.abyssCube.addChild(this.cube_r2);
      setRotationAngle(this.cube_r2, -0.294F, -0.6414F, -0.507F);
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 92, 109, -4.0F, -5.0F, -1.0F, 5, 5, 10, 0.0F, false));
      this.cube_r3 = new ModelRenderer(this);
      this.cube_r3.setRotationPoint(12.0F, -49.0F, -16.0F);
      this.abyssCube.addChild(this.cube_r3);
      setRotationAngle(this.cube_r3, -0.3587F, -0.8482F, -0.4121F);
      this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 88, 107, -6.0F, -7.0F, -1.0F, 7, 7, 12, 0.0F, false));
      this.cube_r4 = new ModelRenderer(this);
      this.cube_r4.setRotationPoint(20.0F, -30.0F, 21.0F);
      this.abyssCube.addChild(this.cube_r4);
      setRotationAngle(this.cube_r4, 0.4396F, 0.8278F, -1.0485F);
      this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 90, 107, -4.0F, -5.0F, -1.0F, 5, 5, 12, 0.0F, false));
      this.cube_r5 = new ModelRenderer(this);
      this.cube_r5.setRotationPoint(-4.0F, -46.0F, 16.0F);
      this.abyssCube.addChild(this.cube_r5);
      setRotationAngle(this.cube_r5, 1.2654F, 0.9713F, -0.6107F);
      this.cube_r5.cubeList.add(new ModelBox(this.cube_r5, 90, 107, -4.0F, -5.0F, -1.0F, 5, 5, 12, 0.0F, false));
      this.cube_r6 = new ModelRenderer(this);
      this.cube_r6.setRotationPoint(-26.0F, -16.0F, 6.0F);
      this.abyssCube.addChild(this.cube_r6);
      setRotationAngle(this.cube_r6, 1.8455F, 1.2906F, -1.4535F);
      this.cube_r6.cubeList.add(new ModelBox(this.cube_r6, 89, 107, -5.0F, -6.0F, -1.0F, 6, 6, 12, 0.0F, false));
      this.cube_r7 = new ModelRenderer(this);
      this.cube_r7.setRotationPoint(-20.0F, -48.0F, 6.0F);
      this.abyssCube.addChild(this.cube_r7);
      setRotationAngle(this.cube_r7, 1.0601F, 1.2906F, -1.4535F);
      this.cube_r7.cubeList.add(new ModelBox(this.cube_r7, 89, 107, -5.0F, -6.0F, -1.0F, 6, 6, 12, 0.0F, false));
      this.cube_r8 = new ModelRenderer(this);
      this.cube_r8.setRotationPoint(-20.0F, -40.0F, -8.0F);
      this.abyssCube.addChild(this.cube_r8);
      setRotationAngle(this.cube_r8, 2.3595F, 1.2214F, -0.1058F);
      this.cube_r8.cubeList.add(new ModelBox(this.cube_r8, 89, 107, -5.0F, -6.0F, -1.0F, 6, 6, 12, 0.0F, false));
      this.cube_r9 = new ModelRenderer(this);
      this.cube_r9.setRotationPoint(-9.0F, -15.0F, 44.0F);
      this.abyssCube.addChild(this.cube_r9);
      setRotationAngle(this.cube_r9, 2.3595F, 1.2214F, -0.1058F);
      this.cube_r9.cubeList.add(new ModelBox(this.cube_r9, 89, 107, -5.0F, -6.0F, -1.0F, 6, 6, 12, 0.0F, false));
      this.cube_r10 = new ModelRenderer(this);
      this.cube_r10.setRotationPoint(27.0F, -25.0F, 16.0F);
      this.abyssCube.addChild(this.cube_r10);
      setRotationAngle(this.cube_r10, 0.808F, 0.9271F, 0.3075F);
      this.cube_r10.cubeList.add(new ModelBox(this.cube_r10, 89, 107, -5.0F, -6.0F, -1.0F, 6, 6, 12, 0.0F, false));
      this.cube_r11 = new ModelRenderer(this);
      this.cube_r11.setRotationPoint(28.0F, -22.0F, 1.0F);
      this.abyssCube.addChild(this.cube_r11);
      setRotationAngle(this.cube_r11, 1.8387F, 1.1041F, 1.4796F);
      this.cube_r11.cubeList.add(new ModelBox(this.cube_r11, 89, 107, -5.0F, -6.0F, -1.0F, 6, 6, 12, 0.0F, false));
      this.cube_r12 = new ModelRenderer(this);
      this.cube_r12.setRotationPoint(28.0F, -8.0F, -27.0F);
      this.abyssCube.addChild(this.cube_r12);
      setRotationAngle(this.cube_r12, 0.4881F, -0.3891F, -0.5882F);
      this.cube_r12.cubeList.add(new ModelBox(this.cube_r12, 88, 107, -6.0F, -7.0F, -1.0F, 7, 7, 12, 0.0F, false));
      this.cube_r13 = new ModelRenderer(this);
      this.cube_r13.setRotationPoint(-4.0F, -5.0F, -25.0F);
      this.abyssCube.addChild(this.cube_r13);
      setRotationAngle(this.cube_r13, 0.48F, 0.0F, 0.0F);
      this.cube_r13.cubeList.add(new ModelBox(this.cube_r13, 86, 107, -8.0F, -9.0F, -1.0F, 9, 9, 12, 0.0F, false));
      this.otherChunk = new ModelRenderer(this);
      this.otherChunk.setRotationPoint(12.0F, 2.0F, -7.0F);
      this.otherChunk.cubeList.add(new ModelBox(this.otherChunk, 0, 0, -6.0F, -11.0F, 0.0F, 12, 12, 12, 0.0F, false));
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.mouthBack.render(f5);
      this.mouthRight.render(f5);
      this.mouthBottom.render(f5);
      this.mouthUpper.render(f5);
      this.sideSmall.render(f5);
      this.sideLarge.render(f5);
      this.eyeTentacleLower.render(f5);
      this.mouthLeft.render(f5);
      this.rightTentacleLower.render(f5);
      this.bottom.render(f5);
      this.leftTentacleLower.render(f5);
      this.abyssCube.render(f5);
      this.otherChunk.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
      this.bottomChild.rotateAngleY = MathHelper.cos(f * 1.0F) * -1.0F * f1;
      this.eyeTentacleLower.rotateAngleY = f3 / 57.295776F;
      this.eyeTentacleLower.rotateAngleX = f4 / 57.295776F;
      this.bottomChild_2.rotateAngleY = MathHelper.cos(f * 1.0F) * -1.0F * f1;
      this.bottomChild_1.rotateAngleY = MathHelper.cos(f * 1.0F) * -1.0F * f1;
    }
  }
}

