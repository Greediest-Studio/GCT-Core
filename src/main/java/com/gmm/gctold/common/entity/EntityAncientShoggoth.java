package com.gmm.gctold.common.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import com.gmm.gctold.common.blocks.BlockSaniteOre;
import com.gmm.gctold.common.events.SanityEvents;
import com.gmm.gctold.common.items.ItemShoggothTancale;
import com.gmm.gctold.common.potions.PotionAbyssPlague;
import com.gmm.gctold.misc.registry.GctAllItems;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityAncientShoggoth {


  private Biome[] allbiomes(RegistryNamespaced<ResourceLocation, Biome> in) {
    Iterator<Biome> itr = in.iterator();
    ArrayList<Biome> ls = new ArrayList<>();
    while (itr.hasNext())
      ls.add(itr.next());
    return ls.<Biome>toArray(new Biome[ls.size()]);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(AncientShoggothEntity.class, renderManager -> new RenderLiving(renderManager, new ModelAncientShoggoth(), 8.0F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/as.png");
          }
        });
  }

  public static class AncientShoggothEntity extends EntityMob {
    private final BossInfoServer bossInfo;

    public AncientShoggothEntity(World world) {
      super(world);
      this.bossInfo = new BossInfoServer(getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS);
      setSize(8.0F, 8.0F);
      this.experienceValue = 30;
      this.isImmuneToFire = true;
      setNoAI(false);
      enablePersistence();
    }

    protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, (EntityAIBase)new EntityAIWander((EntityCreature)this, 1.0D));
      this.tasks.addTask(2, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
      this.tasks.addTask(3, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
      this.tasks.addTask(4, (EntityAIBase)new EntityAILeapAtTarget((EntityLiving)this, 0.8F));
      this.tasks.addTask(5, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.2D));
      this.targetTasks.addTask(6, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, true, new Class[0]));
      this.targetTasks.addTask(7, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this,
          EntityPlayer.class, 10, false, false, player -> SanityEvents.canSanityTargetPlayer((EntityPlayer) player)));
    }

    public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEFINED;
    }

    protected boolean canDespawn() {
      return false;
    }

    protected Item getDropItem() {
      return null;
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
      if (source.getImmediateSource() instanceof net.minecraft.entity.projectile.EntityPotion)
        return false;
      if (source == DamageSource.FALL)
        return false;
      if (source == DamageSource.CACTUS)
        return false;
      if (source == DamageSource.DROWN)
        return false;
      if (source == DamageSource.LIGHTNING_BOLT)
        return false;
      return super.attackEntityFrom(source, amount);
    }

    public void onDeath(DamageSource source) {
      super.onDeath(source);
      if (!this.world.isRemote) {
        spawnDrop(new ItemStack(BlockSaniteOre.block, 5 + this.rand.nextInt(8)));
        spawnDrop(new ItemStack(GctAllItems.ESSENCE_OF_DARKERREALM));
        spawnDrop(new ItemStack(GctAllItems.SHOGGOTH_TOOTH));
        spawnDrop(new ItemStack(ItemShoggothTancale.block, 2 + this.rand.nextInt(4)));
      }
    }

    public void onEntityUpdate() {
      super.onEntityUpdate();
      if (!this.world.isRemote) {
        runAbyssPlagueAura();
      }
    }

    public void onCollideWithPlayer(EntityPlayer entity) {
      super.onCollideWithPlayer(entity);
      entity.addPotionEffect(new PotionEffect(PotionAbyssPlague.potion, 100, 1));
      entity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 1));
    }

    private void runAbyssPlagueAura() {
      if (this.rand.nextDouble() < 0.004D) {
        EntityPlayer player = this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 32.0D, false);
        if (player != null) {
          player.addPotionEffect(new PotionEffect(PotionAbyssPlague.potion, 200, 0, false, true));
        }
      }
      if (this.rand.nextDouble() < 0.005D) {
        EntityPlayer player = this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 10.0D, false);
        if (player != null) {
          player.sendMessage(new TextComponentString("呜啊啊！"));
          player.attackEntityFrom(DamageSource.MAGIC, 12.0F);
        }
      }
    }

    private void spawnDrop(ItemStack stack) {
      if (stack.isEmpty()) {
        return;
      }
      EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, stack);
      item.setPickupDelay(10);
      this.world.spawnEntity(item);
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(120000.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(25.0D);
    }

    public boolean isNonBoss() {
      return false;
    }

    public void addTrackingPlayer(EntityPlayerMP player) {
      super.addTrackingPlayer(player);
      this.bossInfo.addPlayer(player);
    }

    public void removeTrackingPlayer(EntityPlayerMP player) {
      super.removeTrackingPlayer(player);
      this.bossInfo.removePlayer(player);
    }

    public void onUpdate() {
      super.onUpdate();
      this.bossInfo.setPercent(getHealth() / getMaxHealth());
    }

    public void onLivingUpdate() {
      super.onLivingUpdate();
      int i = (int)this.posX;
      int j = (int)this.posY;
      int k = (int)this.posZ;
      Random random = this.rand;
      for (int l = 0; l < 4; l++) {
        double d0 = (i + random.nextFloat());
        double d1 = (j + random.nextFloat());
        double d2 = (k + random.nextFloat());
        int i1 = random.nextInt(2) * 2 - 1;
        double d3 = (random.nextFloat() - 0.5D) * 0.5D;
        double d4 = (random.nextFloat() - 0.5D) * 0.5D;
        double d5 = (random.nextFloat() - 0.5D) * 0.5D;
        this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
      }
    }
  }

  public static class ModelAncientShoggoth extends ModelBase {
    public ModelRenderer headJoint;

    public ModelRenderer bodyBase;

    public ModelRenderer headJointChild;

    public ModelRenderer headJointChildChild;

    public ModelRenderer headJointChildChild_1;

    public ModelRenderer headJointChildChild_2;

    public ModelRenderer headJointChildChild_3;

    public ModelRenderer headJointChildChild_4;

    public ModelRenderer headJointChildChild_5;

    public ModelRenderer headJointChildChild_6;

    public ModelRenderer headJointChildChild_7;

    public ModelRenderer headJointChildChild_8;

    public ModelRenderer headJointChildChild_9;

    public ModelRenderer headJointChildChild_10;

    public ModelRenderer headJointChildChildChild;

    public ModelRenderer headJointChildChildChild_1;

    public ModelRenderer headJointChildChildChild_2;

    public ModelRenderer headJointChildChildChild_3;

    public ModelRenderer headJointChildChildChild_4;

    public ModelRenderer headJointChildChildChild_5;

    public ModelRenderer headJointChildChildChild_6;

    public ModelRenderer headJointChildChildChild_7;

    public ModelRenderer headJointChildChildChild_8;

    public ModelRenderer headJointChildChildChild_9;

    public ModelRenderer headJointChildChildChild_10;

    public ModelRenderer headJointChildChildChildChild;

    public ModelRenderer headJointChildChildChildChild_1;

    public ModelRenderer headJointChildChildChild_11;

    public ModelRenderer headJointChildChildChild_12;

    public ModelRenderer headJointChildChildChild_13;

    public ModelRenderer headJointChildChildChild_14;

    public ModelRenderer headJointChildChildChild_15;

    public ModelRenderer headJointChildChildChild_16;

    public ModelRenderer headJointChildChildChild_17;

    public ModelRenderer headJointChildChildChild_18;

    public ModelRenderer headJointChildChildChild_19;

    public ModelRenderer headJointChildChildChildChild_2;

    public ModelRenderer headJointChildChildChildChild_3;

    public ModelRenderer headJointChildChildChild_20;

    public ModelRenderer headJointChildChildChild_21;

    public ModelRenderer headJointChildChildChild_22;

    public ModelRenderer headJointChildChildChild_23;

    public ModelRenderer headJointChildChildChild_24;

    public ModelRenderer headJointChildChildChild_25;

    public ModelRenderer headJointChildChildChild_26;

    public ModelRenderer headJointChildChildChild_27;

    public ModelRenderer headJointChildChildChild_28;

    public ModelRenderer headJointChildChildChild_29;

    public ModelRenderer headJointChildChildChildChild_4;

    public ModelRenderer headJointChildChildChildChild_5;

    public ModelRenderer bodyBaseChild;

    public ModelRenderer bodyBaseChild_1;

    public ModelRenderer bodyBaseChild_2;

    public ModelRenderer bodyBaseChild_3;

    public ModelRenderer bodyBaseChild_4;

    public ModelRenderer bodyBaseChild_5;

    public ModelRenderer bodyBaseChild_6;

    public ModelRenderer bodyBaseChild_7;

    public ModelRenderer bodyBaseChild_8;

    public ModelRenderer bodyBaseChild_9;

    public ModelRenderer bodyBaseChildChild;

    public ModelRenderer bodyBaseChildChildChild;

    public ModelRenderer bodyBaseChildChildChild_1;

    public ModelRenderer bodyBaseChildChildChildChild;

    public ModelRenderer bodyBaseChildChildChildChildChild;

    public ModelRenderer bodyBaseChildChild_1;

    public ModelRenderer bodyBaseChildChild_2;

    public ModelRenderer bodyBaseChildChildChild_2;

    public ModelRenderer bodyBaseChildChild_3;

    public ModelRenderer bodyBaseChildChild_4;

    public ModelRenderer bodyBaseChildChild_5;

    public ModelRenderer bodyBaseChildChild_6;

    public ModelRenderer bodyBaseChildChild_7;

    public ModelRenderer bodyBaseChildChild_8;

    public ModelRenderer bodyBaseChildChild_9;

    public ModelRenderer bodyBaseChildChild_10;

    public ModelRenderer bodyBaseChildChild_11;

    public ModelRenderer bodyBaseChildChild_12;

    public ModelRenderer bodyBaseChildChild_13;

    public ModelRenderer bodyBaseChildChild_14;

    public ModelRenderer bodyBaseChildChild_15;

    public ModelRenderer bodyBaseChildChild_16;

    public ModelRenderer bodyBaseChildChild_17;

    public ModelRenderer bodyBaseChildChild_18;

    public ModelRenderer bodyBaseChildChild_19;

    public ModelRenderer bodyBaseChildChildChild_3;

    public ModelRenderer bodyBaseChildChildChild_4;

    public ModelRenderer bodyBaseChildChildChildChild_1;

    public ModelRenderer bodyBaseChildChildChild_5;

    public ModelRenderer bodyBaseChildChildChild_6;

    public ModelRenderer bodyBaseChildChildChild_7;

    public ModelRenderer bodyBaseChildChildChild_8;

    public ModelRenderer bodyBaseChildChildChildChild_2;

    public ModelRenderer bodyBaseChildChildChildChildChild_1;

    public ModelRenderer bodyBaseChildChildChildChildChild_2;

    public ModelRenderer bodyBaseChildChildChild_9;

    public ModelRenderer bodyBaseChildChildChildChild_3;

    public ModelRenderer bodyBaseChildChildChild_10;

    public ModelRenderer bodyBaseChildChildChild_11;

    public ModelRenderer bodyBaseChildChildChildChild_4;

    public ModelRenderer bodyBaseChildChildChildChild_5;

    public ModelRenderer bodyBaseChildChildChildChild_6;

    public ModelRenderer bodyBaseChildChildChildChildChild_3;

    public ModelRenderer bodyBaseChildChildChild_12;

    public ModelRenderer bodyBaseChildChildChildChild_7;

    public ModelRenderer bodyBaseChildChildChild_13;

    public ModelRenderer bodyBaseChildChildChild_14;

    public ModelRenderer bodyBaseChildChildChildChild_8;

    public ModelRenderer bodyBaseChildChildChild_15;

    public ModelRenderer bodyBaseChildChildChildChild_9;

    public ModelRenderer bodyBaseChildChildChild_16;

    public ModelRenderer bodyBaseChildChildChild_17;

    public ModelRenderer bodyBaseChildChildChildChild_10;

    public ModelRenderer bodyBaseChildChildChild_18;

    public ModelRenderer bodyBaseChildChildChildChild_11;

    public ModelRenderer bodyBaseChildChildChild_19;

    public ModelRenderer bodyBaseChildChildChildChild_12;

    public ModelRenderer bodyBaseChildChild_20;

    public ModelRenderer bodyBaseChildChildChild_20;

    public ModelRenderer bodyBaseChildChild_21;

    public ModelRenderer bodyBaseChildChild_22;

    public ModelRenderer bodyBaseChildChildChild_21;

    public ModelRenderer bodyBaseChildChildChild_22;

    public ModelRenderer bodyBaseChildChild_23;

    public ModelRenderer bodyBaseChildChildChild_23;

    public ModelRenderer bodyBaseChildChildChildChild_13;

    public ModelRenderer bodyBaseChildChildChildChild_14;

    public ModelRenderer bodyBaseChildChildChildChildChild_4;

    public ModelRenderer bodyBaseChildChildChildChildChild_5;

    public ModelRenderer bodyBaseChildChild_24;

    public ModelRenderer bodyBaseChildChild_25;

    public ModelRenderer bodyBaseChildChildChild_24;

    public ModelRenderer bodyBaseChildChild_26;

    public ModelRenderer bodyBaseChildChildChild_25;

    public ModelRenderer bodyBaseChildChildChild_26;

    public ModelRenderer bodyBaseChildChildChildChild_15;

    public ModelRenderer bodyBaseChildChildChildChildChild_6;

    public ModelRenderer bodyBaseChildChildChildChildChild_7;

    public ModelRenderer bodyBaseChildChild_27;

    public ModelRenderer bodyBaseChildChildChild_27;

    public ModelRenderer bodyBaseChildChildChildChild_16;

    public ModelRenderer bodyBaseChildChildChildChild_17;

    public ModelRenderer bodyBaseChildChildChildChildChild_8;

    public ModelRenderer bodyBaseChildChild_28;

    public ModelRenderer bodyBaseChildChild_29;

    public ModelRenderer bodyBaseChildChildChild_28;

    public ModelRenderer bodyBaseChildChildChild_29;

    public ModelRenderer bodyBaseChildChildChildChild_18;

    public ModelRenderer bodyBaseChildChildChildChild_19;

    public ModelRenderer bodyBaseChildChildChildChildChild_9;

    public ModelAncientShoggoth() {
      this.textureWidth = 128;
      this.textureHeight = 128;
      this.headJointChildChildChild_19 = new ModelRenderer(this, 105, 10);
      this.headJointChildChildChild_19.setRotationPoint(-3.4F, -3.4F, -0.4F);
      this.headJointChildChildChild_19.addBox(-0.5F, -0.5F, -2.9F, 1, 1, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_19, 0.19198622F, -0.19198622F, 0.87266463F);
      this.bodyBaseChildChildChild_23 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChildChildChild_23.mirror = true;
      this.bodyBaseChildChildChild_23.setRotationPoint(0.0F, 0.4F, 7.5F);
      this.bodyBaseChildChildChild_23.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_23, 0.13962634F, -0.02468361F, 0.0F);
      this.headJointChildChildChildChild_3 = new ModelRenderer(this, 96, 19);
      this.headJointChildChildChildChild_3.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.headJointChildChildChildChild_3.addBox(2.4F, -3.9F, 0.3F, 1, 4, 0, 0.0F);
      this.bodyBaseChildChildChild_12 = new ModelRenderer(this, 4, 117);
      this.bodyBaseChildChildChild_12.mirror = true;
      this.bodyBaseChildChildChild_12.setRotationPoint(0.0F, 0.0F, 6.3F);
      this.bodyBaseChildChildChild_12.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 6, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_12, -0.51910603F, 0.08477796F, 0.0F);
      this.bodyBaseChildChild_19 = new ModelRenderer(this, 95, 99);
      this.bodyBaseChildChild_19.mirror = true;
      this.bodyBaseChildChild_19.setRotationPoint(-3.5F, -14.5F, 5.9F);
      this.bodyBaseChildChild_19.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 6, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_19, -0.5904168F, 0.05651864F, 0.0F);
      this.bodyBaseChild_1 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChild_1.setRotationPoint(4.8F, 4.1F, 0.7F);
      this.bodyBaseChild_1.addBox(-2.0F, -2.0F, -8.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChild_1, 0.36593038F, -0.80827624F, 0.0F);
      this.headJointChildChild_9 = new ModelRenderer(this, 75, 30);
      this.headJointChildChild_9.setRotationPoint(4.8F, -16.8F, 2.7F);
      this.headJointChildChild_9.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChildChildChild_3 = new ModelRenderer(this, 16, 114);
      this.bodyBaseChildChildChildChild_3.mirror = true;
      this.bodyBaseChildChildChildChild_3.setRotationPoint(0.0F, 0.0F, 6.6F);
      this.bodyBaseChildChildChildChild_3.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_3, -0.27326235F, 0.14836143F, 0.0F);
      this.bodyBaseChildChildChildChild_6 = new ModelRenderer(this, 61, 30);
      this.bodyBaseChildChildChildChild_6.setRotationPoint(-1.6F, -1.9F, -5.1F);
      this.bodyBaseChildChildChildChild_6.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChildChildChildChild_3 = new ModelRenderer(this, 1, 114);
      this.bodyBaseChildChildChildChildChild_3.mirror = true;
      this.bodyBaseChildChildChildChildChild_3.setRotationPoint(0.0F, 0.0F, -7.2F);
      this.bodyBaseChildChildChildChildChild_3.addBox(-1.0F, -1.0F, -9.0F, 2, 2, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChildChild_3, 0.13962634F, -0.036549814F, 0.0F);
      this.headJointChildChild_3 = new ModelRenderer(this, 75, 30);
      this.headJointChildChild_3.setRotationPoint(-2.1F, -3.9F, 6.3F);
      this.headJointChildChild_3.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.headJointChildChild = new ModelRenderer(this, 59, 0);
      this.headJointChildChild.setRotationPoint(0.0F, -15.3F, 0.3F);
      this.headJointChildChild.addBox(-4.0F, -6.0F, -1.0F, 8, 6, 2, 0.0F);
      setRotateAngle(this.headJointChildChild, 0.29524094F, 0.0F, 0.0F);
      this.bodyBaseChildChildChild_27 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChildChildChild_27.mirror = true;
      this.bodyBaseChildChildChild_27.setRotationPoint(0.0F, 0.4F, 7.5F);
      this.bodyBaseChildChildChild_27.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_27, 0.0F, -5.8859587E-5F, 0.0F);
      this.headJointChildChild_5 = new ModelRenderer(this, 61, 30);
      this.headJointChildChild_5.setRotationPoint(-5.5F, -14.0F, 5.0F);
      this.headJointChildChild_5.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.headJointChildChildChild_23 = new ModelRenderer(this, 105, 10);
      this.headJointChildChildChild_23.setRotationPoint(0.2F, -5.5F, -0.4F);
      this.headJointChildChildChild_23.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_23, -0.31415927F, 0.27925268F, 0.7853982F);
      this.headJointChildChildChild_26 = new ModelRenderer(this, 105, 17);
      this.headJointChildChildChild_26.mirror = true;
      this.headJointChildChildChild_26.setRotationPoint(-1.7F, -5.5F, -0.7F);
      this.headJointChildChildChild_26.addBox(-0.5F, -0.5F, -4.8F, 1, 1, 5, 0.0F);
      setRotateAngle(this.headJointChildChildChild_26, -0.31415927F, -0.27925268F, -0.7853982F);
      this.headJointChildChildChild_6 = new ModelRenderer(this, 105, 10);
      this.headJointChildChildChild_6.setRotationPoint(-3.1F, -5.3F, -0.5F);
      this.headJointChildChildChild_6.addBox(-0.5F, -0.5F, -2.2F, 1, 1, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_6, 0.0F, 0.0F, 0.2617994F);
      this.bodyBaseChildChildChild_16 = new ModelRenderer(this, 17, 114);
      this.bodyBaseChildChildChild_16.setRotationPoint(0.0F, 0.0F, -3.8F);
      this.bodyBaseChildChildChild_16.addBox(-0.5F, -0.5F, -5.0F, 1, 1, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_16, 0.19198622F, 0.40214157F, 0.0F);
      this.bodyBaseChildChildChild_14 = new ModelRenderer(this, 5, 118);
      this.bodyBaseChildChildChild_14.setRotationPoint(0.0F, 0.3F, -4.7F);
      this.bodyBaseChildChildChild_14.addBox(-1.0F, -1.0F, -5.0F, 2, 2, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_14, 0.19198622F, -0.084930226F, 0.0F);
      this.headJointChildChildChild_17 = new ModelRenderer(this, 79, 10);
      this.headJointChildChildChild_17.setRotationPoint(0.0F, -5.0F, 0.0F);
      this.headJointChildChildChild_17.addBox(-3.5F, -0.7F, -2.9F, 7, 5, 2, 0.0F);
      this.bodyBaseChild_6 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChild_6.setRotationPoint(4.2F, 10.0F, -3.5F);
      this.bodyBaseChild_6.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChild_6, 0.0F, 0.8858348F, 0.0F);
      this.bodyBaseChildChildChildChildChild_4 = new ModelRenderer(this, 0, 113);
      this.bodyBaseChildChildChildChildChild_4.mirror = true;
      this.bodyBaseChildChildChildChildChild_4.setRotationPoint(0.0F, 0.4F, 8.9F);
      this.bodyBaseChildChildChildChildChild_4.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChildChild_4, 0.0F, -0.11107624F, 0.0F);
      this.bodyBaseChildChildChild_18 = new ModelRenderer(this, 5, 118);
      this.bodyBaseChildChildChild_18.mirror = true;
      this.bodyBaseChildChildChild_18.setRotationPoint(0.0F, 0.3F, -4.7F);
      this.bodyBaseChildChildChild_18.addBox(-1.0F, -1.0F, -5.0F, 2, 2, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_18, 0.19198622F, 0.084727205F, 0.0F);
      this.bodyBaseChild_4 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChild_4.mirror = true;
      this.bodyBaseChild_4.setRotationPoint(-4.2F, 10.0F, -3.5F);
      this.bodyBaseChild_4.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChild_4, 0.0F, -0.91416514F, 0.0F);
      this.headJointChildChildChild_2 = new ModelRenderer(this, 105, 10);
      this.headJointChildChildChild_2.mirror = true;
      this.headJointChildChildChild_2.setRotationPoint(3.4F, -3.4F, -0.4F);
      this.headJointChildChildChild_2.addBox(-0.5F, -0.5F, -2.9F, 1, 1, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_2, 0.0F, -0.19198622F, 0.5235988F);
      this.headJointChildChild_2 = new ModelRenderer(this, 61, 30);
      this.headJointChildChild_2.setRotationPoint(5.5F, -14.0F, 5.0F);
      this.headJointChildChild_2.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChildChildChildChild_9 = new ModelRenderer(this, 0, 113);
      this.bodyBaseChildChildChildChildChild_9.setRotationPoint(0.0F, 0.4F, 8.9F);
      this.bodyBaseChildChildChildChildChild_9.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChildChild_9, 0.0F, -0.33433282F, 0.0F);
      this.headJointChildChild_1 = new ModelRenderer(this, 61, 30);
      this.headJointChildChild_1.setRotationPoint(-1.2F, -11.9F, 6.1F);
      this.headJointChildChild_1.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChildChildChildChild_2 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChildChildChild_2.setRotationPoint(-1.0F, -1.2F, -3.8F);
      this.bodyBaseChildChildChildChildChild_2.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChildChild_28 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChildChildChild_28.setRotationPoint(0.0F, 0.4F, 7.5F);
      this.bodyBaseChildChildChild_28.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_28, 0.27925268F, -0.074296184F, 0.0F);
      this.bodyBaseChildChildChild_3 = new ModelRenderer(this, 17, 115);
      this.bodyBaseChildChildChild_3.setRotationPoint(0.0F, 0.0F, -3.8F);
      this.bodyBaseChildChildChild_3.addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_3, 0.31415927F, 0.049555924F, 0.0F);
      this.bodyBaseChildChildChildChildChild_5 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChildChildChild_5.setRotationPoint(-0.1F, -1.8F, 3.8F);
      this.bodyBaseChildChildChildChildChild_5.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChild_6 = new ModelRenderer(this, 6, 119);
      this.bodyBaseChildChild_6.mirror = true;
      this.bodyBaseChildChild_6.setRotationPoint(-4.8F, -7.8F, -6.6F);
      this.bodyBaseChildChild_6.addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_6, 0.13962634F, 0.24510363F, 0.0F);
      this.bodyBaseChildChildChildChild = new ModelRenderer(this, 92, 96);
      this.bodyBaseChildChildChildChild.setRotationPoint(0.0F, 0.4F, 7.6F);
      this.bodyBaseChildChildChildChild.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild, 0.0F, -0.0993672F, 0.0F);
      this.bodyBaseChild_2 = new ModelRenderer(this, 0, 31);
      this.bodyBaseChild_2.setRotationPoint(0.0F, 3.6F, 4.4F);
      this.bodyBaseChild_2.addBox(-6.5F, -16.0F, -7.0F, 13, 18, 14, 0.0F);
      setRotateAngle(this.bodyBaseChild_2, 0.7853982F, 0.0F, 0.0F);
      this.bodyBaseChildChildChild_6 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChildChildChild_6.setRotationPoint(0.0F, 0.0F, -7.8F);
      this.bodyBaseChildChildChild_6.addBox(-2.0F, -2.0F, -8.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_6, 0.06981317F, -0.13241434F, 0.0F);
      this.bodyBaseChildChildChildChild_16 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChildChild_16.setRotationPoint(-1.5F, -1.8F, 3.8F);
      this.bodyBaseChildChildChildChild_16.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChildChildChild_14 = new ModelRenderer(this, 92, 96);
      this.bodyBaseChildChildChildChild_14.mirror = true;
      this.bodyBaseChildChildChildChild_14.setRotationPoint(0.0F, 0.4F, 7.6F);
      this.bodyBaseChildChildChildChild_14.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_14, 0.0F, -0.04936722F, 0.0F);
      this.bodyBaseChildChild_17 = new ModelRenderer(this, 94, 98);
      this.bodyBaseChildChild_17.setRotationPoint(5.4F, -0.7F, 5.8F);
      this.bodyBaseChildChild_17.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 7, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_17, -0.7998563F, -0.056586314F, 0.0F);
      this.headJointChildChild_6 = new ModelRenderer(this, 59, 0);
      this.headJointChildChild_6.setRotationPoint(3.8F, -12.2F, -1.8F);
      this.headJointChildChild_6.addBox(-4.0F, -6.0F, -1.0F, 8, 6, 2, 0.0F);
      setRotateAngle(this.headJointChildChild_6, 0.09075138F, 0.0F, 0.9599311F);
      this.headJointChildChildChild_11 = new ModelRenderer(this, 81, 0);
      this.headJointChildChildChild_11.setRotationPoint(0.0F, -1.3F, 2.4F);
      this.headJointChildChildChild_11.addBox(-3.5F, -4.5F, -1.5F, 7, 6, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_11, 0.5235988F, 0.0F, 0.0F);
      this.headJointChildChildChild_5 = new ModelRenderer(this, 105, 10);
      this.headJointChildChildChild_5.setRotationPoint(0.4F, -5.5F, -0.5F);
      this.headJointChildChildChild_5.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_5, -0.31415927F, 0.27925268F, 0.7853982F);
      this.headJointChildChildChild_24 = new ModelRenderer(this, 59, 11);
      this.headJointChildChildChild_24.mirror = true;
      this.headJointChildChildChild_24.setRotationPoint(0.0F, -1.3F, -3.5F);
      this.headJointChildChildChild_24.addBox(-3.5F, -4.0F, -0.5F, 7, 5, 1, 0.0F);
      setRotateAngle(this.headJointChildChildChild_24, 0.36925507F, 0.0F, 0.0F);
      this.bodyBaseChildChild_23 = new ModelRenderer(this, 34, 96);
      this.bodyBaseChildChild_23.mirror = true;
      this.bodyBaseChildChild_23.setRotationPoint(0.0F, 0.4F, 9.5F);
      this.bodyBaseChildChild_23.addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_23, 0.0F, -0.07124778F, 0.0F);
      this.headJointChildChildChild_7 = new ModelRenderer(this, 105, 17);
      this.headJointChildChildChild_7.mirror = true;
      this.headJointChildChildChild_7.setRotationPoint(-2.0F, -5.5F, -0.7F);
      this.headJointChildChildChild_7.addBox(-0.5F, -0.5F, -3.7F, 1, 1, 5, 0.0F);
      setRotateAngle(this.headJointChildChildChild_7, -0.31415927F, -0.27925268F, -0.7853982F);
      this.bodyBaseChildChildChild_24 = new ModelRenderer(this, 0, 113);
      this.bodyBaseChildChildChild_24.setRotationPoint(0.0F, 0.4F, 8.6F);
      this.bodyBaseChildChildChild_24.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_24, 0.0F, -0.34968358F, 0.0F);
      this.bodyBaseChildChild_8 = new ModelRenderer(this, 94, 98);
      this.bodyBaseChildChild_8.mirror = true;
      this.bodyBaseChildChild_8.setRotationPoint(-4.7F, -8.9F, 3.4F);
      this.bodyBaseChildChild_8.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 7, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_8, -0.20644435F, 0.05651864F, 0.27925268F);
      this.headJointChildChildChild_8 = new ModelRenderer(this, 75, 30);
      this.headJointChildChildChild_8.setRotationPoint(2.3F, -3.2F, 2.9F);
      this.headJointChildChildChild_8.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChild_28 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChild_28.setRotationPoint(-0.9F, -3.3F, 3.8F);
      this.bodyBaseChildChild_28.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.headJointChildChild_7 = new ModelRenderer(this, 75, 30);
      this.headJointChildChild_7.setRotationPoint(-6.0F, -6.4F, 3.9F);
      this.headJointChildChild_7.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.headJointChildChildChildChild_4 = new ModelRenderer(this, 96, 19);
      this.headJointChildChildChildChild_4.mirror = true;
      this.headJointChildChildChildChild_4.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.headJointChildChildChildChild_4.addBox(-3.3F, -3.9F, 0.3F, 1, 4, 0, 0.0F);
      this.bodyBaseChildChildChildChild_13 = new ModelRenderer(this, 61, 30);
      this.bodyBaseChildChildChildChild_13.setRotationPoint(-1.0F, -2.0F, 0.7F);
      this.bodyBaseChildChildChildChild_13.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChild_15 = new ModelRenderer(this, 95, 99);
      this.bodyBaseChildChild_15.setRotationPoint(3.5F, -14.5F, 5.9F);
      this.bodyBaseChildChild_15.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 6, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_15, -0.5904168F, -0.056586314F, 0.0F);
      this.bodyBaseChildChildChild_9 = new ModelRenderer(this, 3, 116);
      this.bodyBaseChildChildChild_9.mirror = true;
      this.bodyBaseChildChildChild_9.setRotationPoint(0.0F, 0.0F, 6.3F);
      this.bodyBaseChildChildChild_9.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 7, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_9, -0.51910603F, 0.08477796F, 0.0F);
      this.bodyBaseChildChildChild_10 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChildChildChild_10.mirror = true;
      this.bodyBaseChildChildChild_10.setRotationPoint(0.0F, 0.0F, -7.8F);
      this.bodyBaseChildChildChild_10.addBox(-2.0F, -2.0F, -8.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_10, 0.06981317F, 0.13241434F, 0.0F);
      this.headJointChildChildChild_9 = new ModelRenderer(this, 105, 17);
      this.headJointChildChildChild_9.setRotationPoint(2.7F, -5.5F, -0.5F);
      this.headJointChildChildChild_9.addBox(-0.5F, -0.5F, -4.4F, 1, 1, 5, 0.0F);
      setRotateAngle(this.headJointChildChildChild_9, -0.31415927F, 0.27925268F, 0.7853982F);
      this.headJointChildChildChildChild_5 = new ModelRenderer(this, 80, 19);
      this.headJointChildChildChildChild_5.mirror = true;
      this.headJointChildChildChildChild_5.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.headJointChildChildChildChild_5.addBox(-2.7F, -3.9F, 0.3F, 6, 4, 0, 0.0F);
      this.bodyBaseChildChildChildChild_1 = new ModelRenderer(this, 16, 114);
      this.bodyBaseChildChildChildChild_1.setRotationPoint(0.0F, 0.0F, 6.6F);
      this.bodyBaseChildChildChildChild_1.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_1, -0.27326235F, -0.14853908F, 0.0F);
      this.bodyBaseChild_3 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChild_3.mirror = true;
      this.bodyBaseChild_3.setRotationPoint(-4.8F, 4.1F, 0.7F);
      this.bodyBaseChild_3.addBox(-2.0F, -2.0F, -8.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChild_3, 0.36710793F, 0.80827624F, -0.0F);
      this.bodyBaseChildChild_16 = new ModelRenderer(this, 6, 119);
      this.bodyBaseChildChild_16.setRotationPoint(4.8F, -7.8F, -6.6F);
      this.bodyBaseChildChild_16.addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_16, 0.13962634F, -0.26523897F, 0.0F);
      this.bodyBaseChildChild_3 = new ModelRenderer(this, 6, 119);
      this.bodyBaseChildChild_3.setRotationPoint(1.5F, -6.0F, -6.7F);
      this.bodyBaseChildChild_3.addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_3, 0.27925268F, 0.033037283F, 0.0F);
      this.bodyBaseChildChildChildChild_17 = new ModelRenderer(this, 92, 96);
      this.bodyBaseChildChildChildChild_17.mirror = true;
      this.bodyBaseChildChildChildChild_17.setRotationPoint(0.0F, 0.4F, 7.6F);
      this.bodyBaseChildChildChildChild_17.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_17, 0.0F, 0.099894054F, 0.0F);
      this.bodyBaseChildChild_14 = new ModelRenderer(this, 96, 100);
      this.bodyBaseChildChild_14.setRotationPoint(3.5F, -10.4F, -6.8F);
      this.bodyBaseChildChild_14.addBox(-1.5F, -1.5F, -5.0F, 3, 3, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_14, 0.05235988F, -0.05662015F, 0.0F);
      this.headJointChild = new ModelRenderer(this, 0, 1);
      this.headJointChild.setRotationPoint(0.0F, 3.1F, 2.0F);
      this.headJointChild.addBox(-5.5F, -17.0F, -5.5F, 11, 17, 11, 0.0F);
      setRotateAngle(this.headJointChild, 1.5707964F, 0.0F, 0.0F);
      this.bodyBaseChildChild_21 = new ModelRenderer(this, 92, 96);
      this.bodyBaseChildChild_21.mirror = true;
      this.bodyBaseChildChild_21.setRotationPoint(0.0F, 0.4F, 7.2F);
      this.bodyBaseChildChild_21.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_21, 0.0F, 0.32875228F, 0.0F);
      this.bodyBaseChildChildChildChild_8 = new ModelRenderer(this, 17, 115);
      this.bodyBaseChildChildChildChild_8.setRotationPoint(0.0F, 0.0F, -4.8F);
      this.bodyBaseChildChildChildChild_8.addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_8, 0.10471976F, -0.14862789F, 0.0F);
      this.bodyBaseChildChildChildChild_15 = new ModelRenderer(this, 92, 96);
      this.bodyBaseChildChildChildChild_15.setRotationPoint(0.0F, 0.4F, 7.6F);
      this.bodyBaseChildChildChildChild_15.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_15, 0.0F, -0.22610584F, 0.0F);
      this.bodyBaseChildChildChildChild_18 = new ModelRenderer(this, 92, 96);
      this.bodyBaseChildChildChildChild_18.setRotationPoint(0.0F, 0.4F, 7.6F);
      this.bodyBaseChildChildChildChild_18.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_18, 0.08726646F, -0.14859237F, 0.0F);
      this.bodyBaseChildChild_22 = new ModelRenderer(this, 66, 99);
      this.bodyBaseChildChild_22.mirror = true;
      this.bodyBaseChildChild_22.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bodyBaseChildChild_22.addBox(-2.0F, -2.0F, -3.5F, 4, 4, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_22, 0.0F, -0.7853982F, 0.0F);
      this.headJointChildChildChild_20 = new ModelRenderer(this, 81, 0);
      this.headJointChildChildChild_20.mirror = true;
      this.headJointChildChildChild_20.setRotationPoint(0.0F, -1.3F, 2.4F);
      this.headJointChildChildChild_20.addBox(-3.5F, -4.5F, -1.5F, 7, 6, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_20, 0.5235988F, 0.0F, 0.0F);
      this.bodyBaseChildChildChild = new ModelRenderer(this, 63, 96);
      this.bodyBaseChildChildChild.setRotationPoint(0.0F, 0.4F, 7.5F);
      this.bodyBaseChildChildChild.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild, 0.13962634F, -0.0496836F, 0.0F);
      this.bodyBase = new ModelRenderer(this, 0, 64);
      this.bodyBase.setRotationPoint(0.0F, 12.0F, 0.0F);
      this.bodyBase.addBox(-7.6F, 0.0F, -4.0F, 14, 12, 16, 0.0F);
      this.bodyBaseChildChild_1 = new ModelRenderer(this, 93, 97);
      this.bodyBaseChildChild_1.setRotationPoint(0.0F, 0.0F, -7.0F);
      this.bodyBaseChildChild_1.addBox(-1.5F, -1.5F, -8.0F, 3, 3, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_1, 0.05235988F, 0.047585607F, 0.0F);
      this.bodyBaseChildChildChild_19 = new ModelRenderer(this, 5, 118);
      this.bodyBaseChildChildChild_19.mirror = true;
      this.bodyBaseChildChildChild_19.setRotationPoint(0.0F, 0.0F, 5.2F);
      this.bodyBaseChildChildChild_19.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_19, -0.3445731F, 0.08477796F, 0.0F);
      this.bodyBaseChildChildChild_21 = new ModelRenderer(this, 0, 113);
      this.bodyBaseChildChildChild_21.mirror = true;
      this.bodyBaseChildChildChild_21.setRotationPoint(0.0F, 0.4F, 8.6F);
      this.bodyBaseChildChildChild_21.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_21, 0.0F, 0.27531648F, 0.0F);
      this.bodyBaseChildChild_18 = new ModelRenderer(this, 96, 100);
      this.bodyBaseChildChild_18.mirror = true;
      this.bodyBaseChildChild_18.setRotationPoint(-3.5F, -10.4F, -6.8F);
      this.bodyBaseChildChild_18.addBox(-1.5F, -1.5F, -5.0F, 3, 3, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_18, 0.05235988F, 0.056484804F, 0.0F);
      this.headJointChildChildChild_21 = new ModelRenderer(this, 79, 10);
      this.headJointChildChildChild_21.mirror = true;
      this.headJointChildChildChild_21.setRotationPoint(0.0F, -5.0F, 0.0F);
      this.headJointChildChildChild_21.addBox(-3.5F, -0.7F, -2.9F, 7, 5, 2, 0.0F);
      this.bodyBaseChild = new ModelRenderer(this, 0, 96);
      this.bodyBaseChild.setRotationPoint(4.1F, 6.7F, 4.9F);
      this.bodyBaseChild.addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10, 0.0F);
      setRotateAngle(this.bodyBaseChild, -0.13962634F, 0.085834816F, 0.0F);
      this.bodyBaseChildChild_26 = new ModelRenderer(this, 34, 96);
      this.bodyBaseChildChild_26.setRotationPoint(0.0F, 0.4F, 9.5F);
      this.bodyBaseChildChild_26.addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_26, 0.0F, 0.07875225F, 0.0F);
      this.bodyBaseChildChildChild_15 = new ModelRenderer(this, 5, 118);
      this.bodyBaseChildChildChild_15.setRotationPoint(0.0F, 0.0F, 5.2F);
      this.bodyBaseChildChildChild_15.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_15, -0.3445731F, -0.08487947F, 0.0F);
      this.bodyBaseChildChildChild_2 = new ModelRenderer(this, 1, 114);
      this.bodyBaseChildChildChild_2.setRotationPoint(0.0F, 0.0F, -7.2F);
      this.bodyBaseChildChildChild_2.addBox(-1.0F, -1.0F, -9.0F, 2, 2, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_2, 0.4537856F, -0.01672519F, 0.0F);
      this.bodyBaseChildChild_7 = new ModelRenderer(this, 34, 96);
      this.bodyBaseChildChild_7.setRotationPoint(4.0F, -11.9F, -2.7F);
      this.bodyBaseChildChild_7.addBox(-2.5F, -2.5F, -8.1F, 5, 5, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_7, -0.24493487F, -0.32827625F, 0.0F);
      this.bodyBaseChildChild_9 = new ModelRenderer(this, 34, 96);
      this.bodyBaseChildChild_9.mirror = true;
      this.bodyBaseChildChild_9.setRotationPoint(-4.0F, -11.9F, -2.7F);
      this.bodyBaseChildChild_9.addBox(-2.5F, -2.5F, -8.1F, 5, 5, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_9, -0.24375732F, 0.32827625F, -0.0F);
      this.headJointChildChildChild_15 = new ModelRenderer(this, 61, 30);
      this.headJointChildChildChild_15.setRotationPoint(0.4F, -3.0F, 3.5F);
      this.headJointChildChildChild_15.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChildChildChild_10 = new ModelRenderer(this, 16, 114);
      this.bodyBaseChildChildChildChild_10.setRotationPoint(0.0F, 0.0F, 5.8F);
      this.bodyBaseChildChildChildChild_10.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_10, -0.27326235F, -0.14853908F, 0.0F);
      this.headJointChildChildChild_3 = new ModelRenderer(this, 105, 10);
      this.headJointChildChildChild_3.setRotationPoint(-3.1F, -3.4F, -0.4F);
      this.headJointChildChildChild_3.addBox(-0.5F, -0.5F, -2.9F, 1, 1, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_3, 0.0F, 0.19198622F, 0.0F);
      this.headJointChildChildChildChild_2 = new ModelRenderer(this, 80, 19);
      this.headJointChildChildChildChild_2.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.headJointChildChildChildChild_2.addBox(-3.4F, -3.9F, 0.3F, 6, 4, 0, 0.0F);
      this.headJointChildChildChild_16 = new ModelRenderer(this, 105, 17);
      this.headJointChildChildChild_16.mirror = true;
      this.headJointChildChildChild_16.setRotationPoint(-3.4F, -5.5F, -0.7F);
      this.headJointChildChildChild_16.addBox(-0.5F, -0.5F, -3.7F, 1, 1, 5, 0.0F);
      setRotateAngle(this.headJointChildChildChild_16, -0.31415927F, -0.27925268F, -0.7853982F);
      this.bodyBaseChildChildChildChildChild_6 = new ModelRenderer(this, 0, 113);
      this.bodyBaseChildChildChildChildChild_6.setRotationPoint(0.0F, 0.4F, 8.9F);
      this.bodyBaseChildChildChildChildChild_6.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChildChild_6, 0.0F, -0.46351695F, 0.0F);
      this.headJointChildChildChildChild = new ModelRenderer(this, 96, 19);
      this.headJointChildChildChildChild.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.headJointChildChildChildChild.addBox(2.4F, -3.9F, 0.3F, 1, 4, 0, 0.0F);
      this.headJointChildChildChild_29 = new ModelRenderer(this, 105, 10);
      this.headJointChildChildChild_29.mirror = true;
      this.headJointChildChildChild_29.setRotationPoint(-3.3F, -5.5F, -0.4F);
      this.headJointChildChildChild_29.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_29, -0.31415927F, 0.19198622F, -0.95609134F);
      this.bodyBaseChildChildChild_25 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChild_25.setRotationPoint(2.1F, -2.5F, 3.5F);
      this.bodyBaseChildChildChild_25.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChildChildChildChild_7 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChildChildChild_7.setRotationPoint(1.6F, -0.2F, 3.6F);
      this.bodyBaseChildChildChildChildChild_7.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChild_10 = new ModelRenderer(this, 61, 30);
      this.bodyBaseChildChild_10.setRotationPoint(-6.7F, -6.6F, 0.7F);
      this.bodyBaseChildChild_10.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChild = new ModelRenderer(this, 34, 96);
      this.bodyBaseChildChild.setRotationPoint(0.0F, 0.4F, 9.5F);
      this.bodyBaseChildChild.addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChild, 0.0F, 0.02875223F, 0.0F);
      this.headJointChildChildChild_25 = new ModelRenderer(this, 75, 30);
      this.headJointChildChildChild_25.setRotationPoint(3.3F, -4.8F, 1.4F);
      this.headJointChildChildChild_25.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChild_2 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChild_2.setRotationPoint(1.6F, -1.7F, -3.4F);
      this.bodyBaseChildChild_2.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChild_20 = new ModelRenderer(this, 93, 97);
      this.bodyBaseChildChild_20.mirror = true;
      this.bodyBaseChildChild_20.setRotationPoint(0.0F, 0.0F, -7.0F);
      this.bodyBaseChildChild_20.addBox(-1.5F, -1.5F, -8.0F, 3, 3, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_20, 0.05235988F, -0.047585607F, 0.0F);
      this.bodyBaseChildChildChild_8 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChild_8.setRotationPoint(2.6F, -2.0F, -3.8F);
      this.bodyBaseChildChildChild_8.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChildChild_22 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChild_22.setRotationPoint(-1.1F, -1.2F, 3.4F);
      this.bodyBaseChildChildChild_22.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.headJointChildChildChild_10 = new ModelRenderer(this, 79, 10);
      this.headJointChildChildChild_10.setRotationPoint(0.0F, -5.0F, 0.0F);
      this.headJointChildChildChild_10.addBox(-3.5F, -0.7F, -2.9F, 7, 5, 2, 0.0F);
      this.headJointChildChildChild_12 = new ModelRenderer(this, 59, 11);
      this.headJointChildChildChild_12.setRotationPoint(0.0F, -1.3F, -3.5F);
      this.headJointChildChildChild_12.addBox(-3.5F, -4.0F, -0.5F, 7, 5, 1, 0.0F);
      setRotateAngle(this.headJointChildChildChild_12, 1.0802966F, 0.0F, 0.0F);
      this.bodyBaseChildChildChild_13 = new ModelRenderer(this, 17, 115);
      this.bodyBaseChildChildChild_13.mirror = true;
      this.bodyBaseChildChildChild_13.setRotationPoint(0.0F, 0.0F, -3.8F);
      this.bodyBaseChildChildChild_13.addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_13, 0.31415927F, -0.124758944F, 0.0F);
      this.headJoint = new ModelRenderer(this, 0, 0);
      this.headJoint.setRotationPoint(0.0F, 1.6F, -4.0F);
      this.headJoint.addBox(-1.1F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
      setRotateAngle(this.headJoint, 0.0F, -0.42149708F, 0.0F);
      this.headJointChildChild_10 = new ModelRenderer(this, 59, 0);
      this.headJointChildChild_10.mirror = true;
      this.headJointChildChild_10.setRotationPoint(-3.8F, -12.2F, -1.8F);
      this.headJointChildChild_10.addBox(-4.0F, -6.0F, -1.0F, 8, 6, 2, 0.0F);
      setRotateAngle(this.headJointChildChild_10, 0.44627216F, 0.0F, -0.9599311F);
      this.bodyBaseChild_8 = new ModelRenderer(this, 0, 96);
      this.bodyBaseChild_8.mirror = true;
      this.bodyBaseChild_8.setRotationPoint(-4.2F, 9.5F, -1.9F);
      this.bodyBaseChild_8.addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10, 0.0F);
      setRotateAngle(this.bodyBaseChild_8, 0.0F, -0.3141652F, 0.0F);
      this.bodyBaseChildChildChild_4 = new ModelRenderer(this, 3, 116);
      this.bodyBaseChildChildChild_4.setRotationPoint(0.0F, 0.0F, 6.3F);
      this.bodyBaseChildChildChild_4.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 7, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_4, -0.51910603F, -0.08487947F, 0.0F);
      this.bodyBaseChildChild_12 = new ModelRenderer(this, 61, 30);
      this.bodyBaseChildChild_12.setRotationPoint(-6.7F, -13.3F, 3.9F);
      this.bodyBaseChildChild_12.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChild_24 = new ModelRenderer(this, 92, 96);
      this.bodyBaseChildChild_24.setRotationPoint(0.0F, 0.4F, 7.2F);
      this.bodyBaseChildChild_24.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_24, 0.0F, -0.37124777F, 0.0F);
      this.headJointChildChildChild_1 = new ModelRenderer(this, 75, 30);
      this.headJointChildChildChild_1.setRotationPoint(-2.3F, -3.2F, 2.9F);
      this.headJointChildChildChild_1.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChild_5 = new ModelRenderer(this, 0, 96);
      this.bodyBaseChild_5.mirror = true;
      this.bodyBaseChild_5.setRotationPoint(-4.1F, 6.7F, 4.9F);
      this.bodyBaseChild_5.addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10, 0.0F);
      setRotateAngle(this.bodyBaseChild_5, -0.13962634F, -0.11416519F, 0.0F);
      this.bodyBaseChildChildChildChild_2 = new ModelRenderer(this, 93, 97);
      this.bodyBaseChildChildChildChild_2.setRotationPoint(0.0F, 0.0F, -7.0F);
      this.bodyBaseChildChildChildChild_2.addBox(-1.5F, -1.5F, -8.0F, 3, 3, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_2, 0.17453292F, -0.031725094F, 0.0F);
      this.bodyBaseChildChildChildChild_9 = new ModelRenderer(this, 17, 115);
      this.bodyBaseChildChildChildChild_9.setRotationPoint(0.0F, 0.0F, 4.7F);
      this.bodyBaseChildChildChildChild_9.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_9, 0.023443624F, -0.14853908F, 0.0F);
      this.bodyBaseChildChildChildChild_19 = new ModelRenderer(this, 61, 30);
      this.bodyBaseChildChildChildChild_19.setRotationPoint(1.0F, -2.0F, 3.8F);
      this.bodyBaseChildChildChildChild_19.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.headJointChildChildChild_14 = new ModelRenderer(this, 105, 10);
      this.headJointChildChildChild_14.setRotationPoint(0.3F, -5.5F, -0.5F);
      this.headJointChildChildChild_14.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_14, -0.31415927F, 0.27925268F, 0.7853982F);
      this.bodyBaseChildChildChild_7 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChild_7.setRotationPoint(2.6F, 1.1F, -7.6F);
      this.bodyBaseChildChildChild_7.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChildChild_11 = new ModelRenderer(this, 61, 30);
      this.bodyBaseChildChildChild_11.setRotationPoint(-2.6F, 1.7F, -3.4F);
      this.bodyBaseChildChildChild_11.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.headJointChildChild_8 = new ModelRenderer(this, 61, 30);
      this.headJointChildChild_8.setRotationPoint(3.4F, -7.7F, 6.0F);
      this.headJointChildChild_8.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChildChildChildChild = new ModelRenderer(this, 0, 113);
      this.bodyBaseChildChildChildChildChild.setRotationPoint(0.0F, 0.4F, 8.9F);
      this.bodyBaseChildChildChildChildChild.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChildChild, 0.0F, -0.2235762F, 0.0F);
      this.bodyBaseChildChildChild_20 = new ModelRenderer(this, 1, 114);
      this.bodyBaseChildChildChild_20.mirror = true;
      this.bodyBaseChildChildChild_20.setRotationPoint(0.0F, 0.0F, -7.2F);
      this.bodyBaseChildChildChild_20.addBox(-1.0F, -1.0F, -9.0F, 2, 2, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_20, 0.4537856F, 0.01672519F, 0.0F);
      this.bodyBaseChildChild_13 = new ModelRenderer(this, 6, 119);
      this.bodyBaseChildChild_13.mirror = true;
      this.bodyBaseChildChild_13.setRotationPoint(-1.5F, -6.0F, -6.7F);
      this.bodyBaseChildChild_13.addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_13, 0.27925268F, -0.08317263F, 0.0F);
      this.headJointChildChildChild_4 = new ModelRenderer(this, 81, 0);
      this.headJointChildChildChild_4.setRotationPoint(0.0F, -1.3F, 2.4F);
      this.headJointChildChildChild_4.addBox(-3.5F, -4.5F, -1.5F, 7, 6, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_4, 0.5235988F, 0.0F, 0.0F);
      this.bodyBaseChildChildChild_1 = new ModelRenderer(this, 61, 30);
      this.bodyBaseChildChildChild_1.setRotationPoint(0.8F, -2.8F, 3.8F);
      this.bodyBaseChildChildChild_1.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.headJointChildChildChild_22 = new ModelRenderer(this, 105, 17);
      this.headJointChildChildChild_22.mirror = true;
      this.headJointChildChildChild_22.setRotationPoint(3.4F, -5.1F, -0.4F);
      this.headJointChildChildChild_22.addBox(-0.5F, -0.5F, -3.8F, 1, 1, 5, 0.0F);
      setRotateAngle(this.headJointChildChildChild_22, 0.08726646F, -0.31415927F, 0.5235988F);
      this.bodyBaseChildChildChild_5 = new ModelRenderer(this, 17, 114);
      this.bodyBaseChildChildChild_5.mirror = true;
      this.bodyBaseChildChildChild_5.setRotationPoint(0.0F, 0.0F, -3.8F);
      this.bodyBaseChildChildChild_5.addBox(-0.5F, -0.5F, -5.0F, 1, 1, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_5, 0.19198622F, -0.43234456F, 0.0F);
      this.bodyBaseChildChildChildChild_11 = new ModelRenderer(this, 17, 115);
      this.bodyBaseChildChildChildChild_11.mirror = true;
      this.bodyBaseChildChildChildChild_11.setRotationPoint(0.0F, 0.0F, -4.8F);
      this.bodyBaseChildChildChildChild_11.addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_11, 0.10471976F, 0.1482726F, 0.0F);
      this.bodyBaseChildChildChildChild_5 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChildChild_5.setRotationPoint(2.0F, 1.1F, -8.2F);
      this.bodyBaseChildChildChildChild_5.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChildChild_17 = new ModelRenderer(this, 4, 117);
      this.bodyBaseChildChildChild_17.setRotationPoint(0.0F, 0.0F, 6.3F);
      this.bodyBaseChildChildChild_17.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 6, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_17, -0.51910603F, -0.08487947F, 0.0F);
      this.bodyBaseChildChild_29 = new ModelRenderer(this, 34, 96);
      this.bodyBaseChildChild_29.setRotationPoint(0.0F, 0.4F, 9.5F);
      this.bodyBaseChildChild_29.addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_29, -0.06981317F, -0.04245496F, 0.0F);
      this.bodyBaseChildChild_5 = new ModelRenderer(this, 61, 30);
      this.bodyBaseChildChild_5.setRotationPoint(6.7F, -10.7F, 1.7F);
      this.bodyBaseChildChild_5.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChildChildChildChild_1 = new ModelRenderer(this, 1, 114);
      this.bodyBaseChildChildChildChildChild_1.setRotationPoint(0.0F, 0.0F, -7.2F);
      this.bodyBaseChildChildChildChildChild_1.addBox(-1.0F, -1.0F, -9.0F, 2, 2, 9, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChildChild_1, 0.13962634F, 0.036549814F, 0.0F);
      this.headJointChildChildChild_18 = new ModelRenderer(this, 105, 17);
      this.headJointChildChildChild_18.mirror = true;
      this.headJointChildChildChild_18.setRotationPoint(-1.6F, -5.5F, -0.6F);
      this.headJointChildChildChild_18.addBox(-0.5F, -0.5F, -4.7F, 1, 1, 5, 0.0F);
      setRotateAngle(this.headJointChildChildChild_18, -0.31415927F, -0.27925268F, -0.7853982F);
      this.headJointChildChildChild_28 = new ModelRenderer(this, 75, 30);
      this.headJointChildChildChild_28.setRotationPoint(-1.8F, -3.0F, 3.5F);
      this.headJointChildChildChild_28.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.headJointChildChildChild = new ModelRenderer(this, 59, 11);
      this.headJointChildChildChild.setRotationPoint(0.0F, -1.3F, -3.5F);
      this.headJointChildChildChild.addBox(-3.5F, -4.0F, -0.5F, 7, 5, 1, 0.0F);
      setRotateAngle(this.headJointChildChildChild, 0.6713175F, 0.0F, 0.0F);
      this.bodyBaseChild_9 = new ModelRenderer(this, 0, 96);
      this.bodyBaseChild_9.setRotationPoint(0.0F, 3.7F, 11.3F);
      this.bodyBaseChild_9.addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10, 0.0F);
      setRotateAngle(this.bodyBaseChild_9, -0.27925268F, -0.028303308F, 0.0F);
      this.bodyBaseChildChildChildChildChild_8 = new ModelRenderer(this, 0, 113);
      this.bodyBaseChildChildChildChildChild_8.mirror = true;
      this.bodyBaseChildChildChildChildChild_8.setRotationPoint(0.0F, 0.4F, 8.9F);
      this.bodyBaseChildChildChildChildChild_8.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 10, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChildChild_8, 0.0F, 0.20478281F, 0.0F);
      this.bodyBaseChildChild_4 = new ModelRenderer(this, 94, 98);
      this.bodyBaseChildChild_4.setRotationPoint(4.7F, -8.9F, 3.4F);
      this.bodyBaseChildChild_4.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 7, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_4, -0.20644435F, -0.056586314F, -0.27925268F);
      this.headJointChildChild_4 = new ModelRenderer(this, 61, 30);
      this.headJointChildChild_4.setRotationPoint(5.6F, -5.4F, 2.6F);
      this.headJointChildChild_4.addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3, 0.0F);
      this.bodyBaseChildChildChildChild_7 = new ModelRenderer(this, 16, 114);
      this.bodyBaseChildChildChildChild_7.mirror = true;
      this.bodyBaseChildChildChildChild_7.setRotationPoint(0.0F, 0.0F, 5.8F);
      this.bodyBaseChildChildChildChild_7.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_7, -0.27326235F, 0.14836143F, 0.0F);
      this.bodyBaseChildChild_27 = new ModelRenderer(this, 34, 96);
      this.bodyBaseChildChild_27.mirror = true;
      this.bodyBaseChildChild_27.setRotationPoint(0.0F, 0.4F, 9.5F);
      this.bodyBaseChildChild_27.addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_27, 0.0F, -0.1212478F, 0.0F);
      this.headJointChildChildChild_13 = new ModelRenderer(this, 105, 17);
      this.headJointChildChildChild_13.setRotationPoint(2.7F, -5.5F, -0.5F);
      this.headJointChildChildChild_13.addBox(-0.5F, -0.5F, -4.4F, 1, 1, 5, 0.0F);
      setRotateAngle(this.headJointChildChildChild_13, -0.31415927F, 0.27925268F, 0.7853982F);
      this.headJointChildChildChildChild_1 = new ModelRenderer(this, 80, 19);
      this.headJointChildChildChildChild_1.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.headJointChildChildChildChild_1.addBox(-3.4F, -3.9F, 0.3F, 6, 4, 0, 0.0F);
      this.bodyBaseChildChildChildChild_12 = new ModelRenderer(this, 17, 115);
      this.bodyBaseChildChildChildChild_12.mirror = true;
      this.bodyBaseChildChildChildChild_12.setRotationPoint(0.0F, 0.0F, 4.7F);
      this.bodyBaseChildChildChildChild_12.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 4, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_12, 0.023443624F, 0.14836143F, 0.0F);
      this.bodyBaseChild_7 = new ModelRenderer(this, 0, 96);
      this.bodyBaseChild_7.setRotationPoint(4.2F, 9.5F, -1.9F);
      this.bodyBaseChild_7.addBox(-3.0F, -3.0F, 0.0F, 6, 6, 10, 0.0F);
      setRotateAngle(this.bodyBaseChild_7, 0.0F, 0.28583482F, 0.0F);
      this.bodyBaseChildChild_11 = new ModelRenderer(this, 94, 98);
      this.bodyBaseChildChild_11.mirror = true;
      this.bodyBaseChildChild_11.setRotationPoint(-5.4F, -0.7F, 5.8F);
      this.bodyBaseChildChild_11.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 7, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_11, -0.7998563F, 0.05651864F, 0.0F);
      this.bodyBaseChildChild_25 = new ModelRenderer(this, 66, 99);
      this.bodyBaseChildChild_25.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bodyBaseChildChild_25.addBox(-2.0F, -2.0F, -3.5F, 4, 4, 5, 0.0F);
      setRotateAngle(this.bodyBaseChildChild_25, 0.0F, 0.7853982F, 0.0F);
      this.bodyBaseChildChildChild_26 = new ModelRenderer(this, 63, 96);
      this.bodyBaseChildChildChild_26.setRotationPoint(0.0F, 0.4F, 7.5F);
      this.bodyBaseChildChildChild_26.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChild_26, 0.0F, -0.07005879F, 0.0F);
      this.headJointChildChildChild_27 = new ModelRenderer(this, 105, 10);
      this.headJointChildChildChild_27.setRotationPoint(2.2F, -5.5F, -0.4F);
      this.headJointChildChildChild_27.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
      setRotateAngle(this.headJointChildChildChild_27, -0.41887903F, 0.08726646F, 0.7853982F);
      this.bodyBaseChildChildChild_29 = new ModelRenderer(this, 75, 30);
      this.bodyBaseChildChildChild_29.setRotationPoint(-2.0F, -2.5F, 3.8F);
      this.bodyBaseChildChildChild_29.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
      this.bodyBaseChildChildChildChild_4 = new ModelRenderer(this, 93, 97);
      this.bodyBaseChildChildChildChild_4.mirror = true;
      this.bodyBaseChildChildChildChild_4.setRotationPoint(0.0F, 0.0F, -7.0F);
      this.bodyBaseChildChildChildChild_4.addBox(-1.5F, -1.5F, -8.0F, 3, 3, 8, 0.0F);
      setRotateAngle(this.bodyBaseChildChildChildChild_4, 0.17453292F, 0.031725094F, 0.0F);
      this.headJointChildChild_6.addChild(this.headJointChildChildChild_19);
      this.bodyBaseChildChild_23.addChild(this.bodyBaseChildChildChild_23);
      this.headJointChildChildChild_12.addChild(this.headJointChildChildChildChild_3);
      this.bodyBaseChildChild_11.addChild(this.bodyBaseChildChildChild_12);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_19);
      this.bodyBase.addChild(this.bodyBaseChild_1);
      this.headJointChild.addChild(this.headJointChildChild_9);
      this.bodyBaseChildChildChild_9.addChild(this.bodyBaseChildChildChildChild_3);
      this.bodyBaseChildChildChild_10.addChild(this.bodyBaseChildChildChildChild_6);
      this.bodyBaseChildChildChildChild_4.addChild(this.bodyBaseChildChildChildChildChild_3);
      this.headJointChild.addChild(this.headJointChildChild_3);
      this.headJointChild.addChild(this.headJointChildChild);
      this.bodyBaseChildChild_27.addChild(this.bodyBaseChildChildChild_27);
      this.headJointChild.addChild(this.headJointChildChild_5);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_23);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_26);
      this.headJointChildChild.addChild(this.headJointChildChildChild_6);
      this.bodyBaseChildChild_16.addChild(this.bodyBaseChildChildChild_16);
      this.bodyBaseChildChild_14.addChild(this.bodyBaseChildChildChild_14);
      this.headJointChildChild_6.addChild(this.headJointChildChildChild_17);
      this.bodyBase.addChild(this.bodyBaseChild_6);
      this.bodyBaseChildChildChildChild_14.addChild(this.bodyBaseChildChildChildChildChild_4);
      this.bodyBaseChildChild_18.addChild(this.bodyBaseChildChildChild_18);
      this.bodyBase.addChild(this.bodyBaseChild_4);
      this.headJointChildChild.addChild(this.headJointChildChildChild_2);
      this.headJointChild.addChild(this.headJointChildChild_2);
      this.bodyBaseChildChildChildChild_18.addChild(this.bodyBaseChildChildChildChildChild_9);
      this.headJointChild.addChild(this.headJointChildChild_1);
      this.bodyBaseChildChildChildChild_2.addChild(this.bodyBaseChildChildChildChildChild_2);
      this.bodyBaseChildChild_29.addChild(this.bodyBaseChildChildChild_28);
      this.bodyBaseChildChild_3.addChild(this.bodyBaseChildChildChild_3);
      this.bodyBaseChildChildChildChild_14.addChild(this.bodyBaseChildChildChildChildChild_5);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_6);
      this.bodyBaseChildChildChild.addChild(this.bodyBaseChildChildChildChild);
      this.bodyBase.addChild(this.bodyBaseChild_2);
      this.bodyBaseChildChild_7.addChild(this.bodyBaseChildChildChild_6);
      this.bodyBaseChildChildChild_27.addChild(this.bodyBaseChildChildChildChild_16);
      this.bodyBaseChildChildChild_23.addChild(this.bodyBaseChildChildChildChild_14);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_17);
      this.headJointChild.addChild(this.headJointChildChild_6);
      this.headJointChildChild_6.addChild(this.headJointChildChildChild_11);
      this.headJointChildChild.addChild(this.headJointChildChildChild_5);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_24);
      this.bodyBaseChild_5.addChild(this.bodyBaseChildChild_23);
      this.headJointChildChild.addChild(this.headJointChildChildChild_7);
      this.bodyBaseChildChild_24.addChild(this.bodyBaseChildChildChild_24);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_8);
      this.headJointChildChild.addChild(this.headJointChildChildChild_8);
      this.bodyBaseChild_9.addChild(this.bodyBaseChildChild_28);
      this.headJointChild.addChild(this.headJointChildChild_7);
      this.headJointChildChildChild_24.addChild(this.headJointChildChildChildChild_4);
      this.bodyBaseChildChildChild_23.addChild(this.bodyBaseChildChildChildChild_13);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_15);
      this.bodyBaseChildChild_8.addChild(this.bodyBaseChildChildChild_9);
      this.bodyBaseChildChild_9.addChild(this.bodyBaseChildChildChild_10);
      this.headJointChildChild.addChild(this.headJointChildChildChild_9);
      this.headJointChildChildChild_24.addChild(this.headJointChildChildChildChild_5);
      this.bodyBaseChildChildChild_4.addChild(this.bodyBaseChildChildChildChild_1);
      this.bodyBase.addChild(this.bodyBaseChild_3);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_16);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_3);
      this.bodyBaseChildChildChild_27.addChild(this.bodyBaseChildChildChildChild_17);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_14);
      this.headJoint.addChild(this.headJointChild);
      this.bodyBaseChild_4.addChild(this.bodyBaseChildChild_21);
      this.bodyBaseChildChildChild_14.addChild(this.bodyBaseChildChildChildChild_8);
      this.bodyBaseChildChildChild_26.addChild(this.bodyBaseChildChildChildChild_15);
      this.bodyBaseChildChildChild_28.addChild(this.bodyBaseChildChildChildChild_18);
      this.bodyBaseChild_4.addChild(this.bodyBaseChildChild_22);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_20);
      this.bodyBaseChildChild.addChild(this.bodyBaseChildChildChild);
      this.bodyBaseChild_1.addChild(this.bodyBaseChildChild_1);
      this.bodyBaseChildChild_19.addChild(this.bodyBaseChildChildChild_19);
      this.bodyBaseChildChild_21.addChild(this.bodyBaseChildChildChild_21);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_18);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_21);
      this.bodyBase.addChild(this.bodyBaseChild);
      this.bodyBaseChild_7.addChild(this.bodyBaseChildChild_26);
      this.bodyBaseChildChild_15.addChild(this.bodyBaseChildChildChild_15);
      this.bodyBaseChildChild_1.addChild(this.bodyBaseChildChildChild_2);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_7);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_9);
      this.headJointChildChild_6.addChild(this.headJointChildChildChild_15);
      this.bodyBaseChildChildChild_17.addChild(this.bodyBaseChildChildChildChild_10);
      this.headJointChildChild.addChild(this.headJointChildChildChild_3);
      this.headJointChildChildChild_12.addChild(this.headJointChildChildChildChild_2);
      this.headJointChildChild_6.addChild(this.headJointChildChildChild_16);
      this.bodyBaseChildChildChildChild_15.addChild(this.bodyBaseChildChildChildChildChild_6);
      this.headJointChildChildChild.addChild(this.headJointChildChildChildChild);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_29);
      this.bodyBaseChildChild_26.addChild(this.bodyBaseChildChildChild_25);
      this.bodyBaseChildChildChildChild_15.addChild(this.bodyBaseChildChildChildChildChild_7);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_10);
      this.bodyBaseChild.addChild(this.bodyBaseChildChild);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_25);
      this.bodyBaseChild_1.addChild(this.bodyBaseChildChild_2);
      this.bodyBaseChild_3.addChild(this.bodyBaseChildChild_20);
      this.bodyBaseChildChild_7.addChild(this.bodyBaseChildChildChild_8);
      this.bodyBaseChildChild_21.addChild(this.bodyBaseChildChildChild_22);
      this.headJointChildChild.addChild(this.headJointChildChildChild_10);
      this.headJointChildChild_6.addChild(this.headJointChildChildChild_12);
      this.bodyBaseChildChild_13.addChild(this.bodyBaseChildChildChild_13);
      this.headJointChild.addChild(this.headJointChildChild_10);
      this.bodyBase.addChild(this.bodyBaseChild_8);
      this.bodyBaseChildChild_4.addChild(this.bodyBaseChildChildChild_4);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_12);
      this.bodyBaseChild_6.addChild(this.bodyBaseChildChild_24);
      this.headJointChildChild.addChild(this.headJointChildChildChild_1);
      this.bodyBase.addChild(this.bodyBaseChild_5);
      this.bodyBaseChildChildChild_6.addChild(this.bodyBaseChildChildChildChild_2);
      this.bodyBaseChildChildChild_15.addChild(this.bodyBaseChildChildChildChild_9);
      this.bodyBaseChildChildChild_28.addChild(this.bodyBaseChildChildChildChild_19);
      this.headJointChildChild_6.addChild(this.headJointChildChildChild_14);
      this.bodyBaseChildChild_7.addChild(this.bodyBaseChildChildChild_7);
      this.bodyBaseChildChild_9.addChild(this.bodyBaseChildChildChild_11);
      this.headJointChild.addChild(this.headJointChildChild_8);
      this.bodyBaseChildChildChildChild.addChild(this.bodyBaseChildChildChildChildChild);
      this.bodyBaseChildChild_20.addChild(this.bodyBaseChildChildChild_20);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_13);
      this.headJointChildChild.addChild(this.headJointChildChildChild_4);
      this.bodyBaseChildChild.addChild(this.bodyBaseChildChildChild_1);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_22);
      this.bodyBaseChildChild_6.addChild(this.bodyBaseChildChildChild_5);
      this.bodyBaseChildChildChild_18.addChild(this.bodyBaseChildChildChildChild_11);
      this.bodyBaseChildChildChild_10.addChild(this.bodyBaseChildChildChildChild_5);
      this.bodyBaseChildChild_17.addChild(this.bodyBaseChildChildChild_17);
      this.bodyBaseChild_9.addChild(this.bodyBaseChildChild_29);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_5);
      this.bodyBaseChildChildChildChild_2.addChild(this.bodyBaseChildChildChildChildChild_1);
      this.headJointChildChild_6.addChild(this.headJointChildChildChild_18);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_28);
      this.headJointChildChild.addChild(this.headJointChildChildChild);
      this.bodyBase.addChild(this.bodyBaseChild_9);
      this.bodyBaseChildChildChildChild_17.addChild(this.bodyBaseChildChildChildChildChild_8);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_4);
      this.headJointChild.addChild(this.headJointChildChild_4);
      this.bodyBaseChildChildChild_12.addChild(this.bodyBaseChildChildChildChild_7);
      this.bodyBaseChild_8.addChild(this.bodyBaseChildChild_27);
      this.headJointChildChild_6.addChild(this.headJointChildChildChild_13);
      this.headJointChildChildChild.addChild(this.headJointChildChildChildChild_1);
      this.bodyBaseChildChildChild_19.addChild(this.bodyBaseChildChildChildChild_12);
      this.bodyBase.addChild(this.bodyBaseChild_7);
      this.bodyBaseChild_2.addChild(this.bodyBaseChildChild_11);
      this.bodyBaseChild_6.addChild(this.bodyBaseChildChild_25);
      this.bodyBaseChildChild_26.addChild(this.bodyBaseChildChildChild_26);
      this.headJointChildChild_10.addChild(this.headJointChildChildChild_27);
      this.bodyBaseChildChild_29.addChild(this.bodyBaseChildChildChild_29);
      this.bodyBaseChildChildChild_10.addChild(this.bodyBaseChildChildChildChild_4);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(this.bodyBase.offsetX, this.bodyBase.offsetY, this.bodyBase.offsetZ);
      GlStateManager.translate(this.bodyBase.rotationPointX * f5, this.bodyBase.rotationPointY * f5, this.bodyBase.rotationPointZ * f5);
      GlStateManager.scale(3.762512836380361D, 3.1070440929537932D, 2.5937424601000023D);
      GlStateManager.translate(-this.bodyBase.offsetX, -this.bodyBase.offsetY, -this.bodyBase.offsetZ);
      GlStateManager.translate(-this.bodyBase.rotationPointX * f5, -this.bodyBase.rotationPointY * f5, -this.bodyBase.rotationPointZ * f5);
      this.bodyBase.render(f5);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(this.headJoint.offsetX, this.headJoint.offsetY, this.headJoint.offsetZ);
      GlStateManager.translate(this.headJoint.rotationPointX * f5, this.headJoint.rotationPointY * f5, this.headJoint.rotationPointZ * f5);
      GlStateManager.scale(3.762512836380361D, 3.1070440929537932D, 2.5937424601000023D);
      GlStateManager.translate(-this.headJoint.offsetX, -this.headJoint.offsetY, -this.headJoint.offsetZ);
      GlStateManager.translate(-this.headJoint.rotationPointX * f5, -this.headJoint.rotationPointY * f5, -this.headJoint.rotationPointZ * f5);
      this.headJoint.render(f5);
      GlStateManager.popMatrix();
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
      this.headJoint.rotateAngleY = f3 / 57.295776F;
      this.headJoint.rotateAngleX = f4 / 57.295776F;
    }
  }
}

