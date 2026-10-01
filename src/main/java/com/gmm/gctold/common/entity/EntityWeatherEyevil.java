package com.gmm.gctold.common.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityWeatherEyevil {


  private Biome[] allbiomes(RegistryNamespaced<ResourceLocation, Biome> in) {
    Iterator<Biome> itr = in.iterator();
    ArrayList<Biome> ls = new ArrayList<>();
    while (itr.hasNext())
      ls.add(itr.next());
    return ls.<Biome>toArray(new Biome[ls.size()]);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(WeatherEyevilEntity.class, renderManager -> new RenderLiving(renderManager, new ModelWeatherBeholder(), 1.0F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/weathereyevil.png");
          }
        });
  }

  public static class WeatherEyevilEntity extends EntityMob {
    private final BossInfoServer bossInfo;

    public WeatherEyevilEntity(World world) {
      super(world);
      this.bossInfo = new BossInfoServer(getDisplayName(), BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS);
      setSize(1.0F, 1.5F);
      this.experienceValue = 500;
      this.isImmuneToFire = true;
      setNoAI(false);
      enablePersistence();
      this.navigator = (PathNavigate)new PathNavigateFlying((EntityLiving)this, this.world);
      this.moveHelper = (EntityMoveHelper)new EntityFlyHelper((EntityLiving)this);
    }

    protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, (EntityAIBase)new EntityAIAttackMelee((EntityCreature)this, 2.0D, false));
      this.targetTasks.addTask(2, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
      this.tasks.addTask(3, (EntityAIBase)new EntityAIWander((EntityCreature)this, 0.8D));
      this.tasks.addTask(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
      this.targetTasks.addTask(5, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityPlayer.class, false, false));
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
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(""));
    }

    public SoundEvent getHurtSound(DamageSource ds) {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.snowman.hurt"));
    }

    public SoundEvent getDeathSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.snowman.death"));
    }

    protected float getSoundVolume() {
      return 1.0F;
    }

    public void fall(float l, float d) {}

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
        List<EntityWeatherWaterRod.WeatherWaterRodEntity> waterRods = this.world.getEntities(EntityWeatherWaterRod.WeatherWaterRodEntity.class, entity -> true);
        for (EntityWeatherWaterRod.WeatherWaterRodEntity waterRod : waterRods)
          waterRod.setDead();
      }
    }

    @Nullable
    protected ResourceLocation getLootTable() {
      return new ResourceLocation("gctold:entities/weather_eyevil");
    }

    public void onEntityUpdate() {
      super.onEntityUpdate();
      if (!this.world.isRemote) {
        runWeatherSkills();
      }
    }

    private void runWeatherSkills() {
      if (this.rand.nextDouble() < 0.008D) {
        spawnWaterRod();
        spawnWaterRod();
      }

      Potion frosty = Potion.REGISTRY.getObject(new ResourceLocation("twilightforest", "frosty"));
      if (this.rand.nextDouble() < 0.005D) {
        applyFrostyToNearestPlayer(frosty, 60, 6);
      }
      if (this.rand.nextDouble() < 0.005D) {
        applyFrostyToNearestPlayer(frosty, 400, 0);
      }
    }

    private void spawnWaterRod() {
      EntityWeatherWaterRod.WeatherWaterRodEntity waterRod =
          new EntityWeatherWaterRod.WeatherWaterRodEntity(this.world);
      waterRod.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      this.world.spawnEntity(waterRod);
    }

    private void applyFrostyToNearestPlayer(Potion frosty, int ticks, int amplifier) {
      if (frosty == null) {
        return;
      }
      EntityPlayer player = getNearestPlayer(32.0D);
      if (player != null) {
        player.addPotionEffect(new PotionEffect(frosty, ticks, amplifier, false, true));
      }
    }

    private EntityPlayer getNearestPlayer(double radius) {
      AxisAlignedBB area = new AxisAlignedBB(getPosition()).grow(radius);
      double bestDistance = radius * radius;
      EntityPlayer nearest = null;
      for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, area,
          player -> player.getDistanceSq(this) <= radius * radius)) {
        double distance = player.getDistanceSq(this);
        if (distance <= bestDistance) {
          bestDistance = distance;
          nearest = player;
        }
      }
      return nearest;
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(28000.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
      getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
      getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.4D);
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
      setNoGravity(true);
      this.bossInfo.setPercent(getHealth() / getMaxHealth());
    }

    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {}

    public void setNoGravity(boolean ignored) {
      super.setNoGravity(true);
    }

    public void onLivingUpdate() {
      super.onLivingUpdate();
      int i = (int)this.posX;
      int j = (int)this.posY;
      int k = (int)this.posZ;
      Random random = this.rand;
      for (int l = 0; l < 12; l++) {
        double d0 = i + 0.5D + (random.nextFloat() - 0.5D) * 0.5D * 20.0D;
        double d1 = j + 0.7D + (random.nextFloat() - 0.5D) * 0.5D + 0.5D;
        double d2 = k + 0.5D + (random.nextFloat() - 0.5D) * 0.5D * 20.0D;
        this.world.spawnParticle(EnumParticleTypes.SNOWBALL, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
      }
    }
  }

  public static class ModelWeatherBeholder extends ModelBase {
    private final ModelRenderer main;

    private final ModelRenderer hair5;

    private final ModelRenderer cube_r28;

    private final ModelRenderer cube_r29;

    private final ModelRenderer hair4;

    private final ModelRenderer cube_r26;

    private final ModelRenderer cube_r27;

    private final ModelRenderer hair3;

    private final ModelRenderer cube_r24;

    private final ModelRenderer cube_r25;

    private final ModelRenderer hair2;

    private final ModelRenderer cube_r22;

    private final ModelRenderer cube_r23;

    private final ModelRenderer tentacle9;

    private final ModelRenderer cube_r18;

    private final ModelRenderer cube_r19;

    private final ModelRenderer hair1;

    private final ModelRenderer cube_r20;

    private final ModelRenderer cube_r21;

    private final ModelRenderer tentacle8;

    private final ModelRenderer cube_r16;

    private final ModelRenderer cube_r17;

    private final ModelRenderer tentacle7;

    private final ModelRenderer cube_r14;

    private final ModelRenderer cube_r15;

    private final ModelRenderer tentacle6;

    private final ModelRenderer cube_r12;

    private final ModelRenderer cube_r13;

    private final ModelRenderer tentacle5;

    private final ModelRenderer cube_r10;

    private final ModelRenderer cube_r11;

    private final ModelRenderer tentacle4;

    private final ModelRenderer cube_r8;

    private final ModelRenderer cube_r9;

    private final ModelRenderer tentacle3;

    private final ModelRenderer cube_r7;

    private final ModelRenderer tentacle2;

    private final ModelRenderer cube_r5;

    private final ModelRenderer cube_r6;

    private final ModelRenderer tentacle1;

    private final ModelRenderer cube_r3;

    private final ModelRenderer cube_r4;

    private final ModelRenderer down;

    private final ModelRenderer cube_r1;

    private final ModelRenderer cube_r2;

    private final ModelRenderer head;

    private final ModelRenderer circle1;

    private final ModelRenderer circle2;

    private final ModelRenderer circle3;

    public ModelWeatherBeholder() {
      this.textureWidth = 128;
      this.textureHeight = 128;
      this.main = new ModelRenderer(this);
      this.main.setRotationPoint(0.0F, 24.0F, 0.0F);
      this.hair5 = new ModelRenderer(this);
      this.hair5.setRotationPoint(8.0F, -1.0F, -3.0F);
      this.main.addChild(this.hair5);
      this.cube_r28 = new ModelRenderer(this);
      this.cube_r28.setRotationPoint(-1.0F, -35.0F, 5.0F);
      this.hair5.addChild(this.cube_r28);
      setRotationAngle(this.cube_r28, 0.0F, 0.0F, -0.4363F);
      this.cube_r28.cubeList.add(new ModelBox(this.cube_r28, 0, 0, -2.0F, -4.0F, 0.0F, 2, 4, 2, 0.0F, false));
      this.cube_r29 = new ModelRenderer(this);
      this.cube_r29.setRotationPoint(-3.0F, -28.0F, 5.0F);
      this.hair5.addChild(this.cube_r29);
      setRotationAngle(this.cube_r29, 0.0F, 0.0F, 0.3054F);
      this.cube_r29.cubeList.add(new ModelBox(this.cube_r29, 0, 0, -2.0F, -8.0F, 0.0F, 2, 8, 2, 0.0F, false));
      this.hair4 = new ModelRenderer(this);
      this.hair4.setRotationPoint(6.0F, -1.0F, -9.0F);
      this.main.addChild(this.hair4);
      this.cube_r26 = new ModelRenderer(this);
      this.cube_r26.setRotationPoint(-1.0F, -35.0F, 5.0F);
      this.hair4.addChild(this.cube_r26);
      setRotationAngle(this.cube_r26, 0.0F, 0.0F, -0.4363F);
      this.cube_r26.cubeList.add(new ModelBox(this.cube_r26, 0, 0, -2.0F, -5.0F, 0.0F, 2, 5, 2, 0.0F, false));
      this.cube_r27 = new ModelRenderer(this);
      this.cube_r27.setRotationPoint(-3.0F, -28.0F, 5.0F);
      this.hair4.addChild(this.cube_r27);
      setRotationAngle(this.cube_r27, 0.0F, 0.0F, 0.3054F);
      this.cube_r27.cubeList.add(new ModelBox(this.cube_r27, 0, 0, -2.0F, -8.0F, 0.0F, 2, 8, 2, 0.0F, false));
      this.hair3 = new ModelRenderer(this);
      this.hair3.setRotationPoint(-1.0F, 2.0F, -9.0F);
      this.main.addChild(this.hair3);
      this.cube_r24 = new ModelRenderer(this);
      this.cube_r24.setRotationPoint(-5.0F, -35.0F, 5.0F);
      this.hair3.addChild(this.cube_r24);
      setRotationAngle(this.cube_r24, 0.0F, 0.0F, 0.2618F);
      this.cube_r24.cubeList.add(new ModelBox(this.cube_r24, 0, 0, -2.0F, -5.0F, 0.0F, 2, 5, 2, 0.0F, false));
      this.cube_r25 = new ModelRenderer(this);
      this.cube_r25.setRotationPoint(-3.0F, -28.0F, 5.0F);
      this.hair3.addChild(this.cube_r25);
      setRotationAngle(this.cube_r25, 0.0F, 0.0F, -0.2618F);
      this.cube_r25.cubeList.add(new ModelBox(this.cube_r25, 0, 0, -2.0F, -8.0F, 0.0F, 2, 8, 2, 0.0F, false));
      this.hair2 = new ModelRenderer(this);
      this.hair2.setRotationPoint(2.0F, 0.0F, -5.0F);
      this.main.addChild(this.hair2);
      this.cube_r22 = new ModelRenderer(this);
      this.cube_r22.setRotationPoint(-5.0F, -35.0F, 5.0F);
      this.hair2.addChild(this.cube_r22);
      setRotationAngle(this.cube_r22, 0.0F, 0.0F, 0.2618F);
      this.cube_r22.cubeList.add(new ModelBox(this.cube_r22, 0, 0, -2.0F, -5.0F, 0.0F, 2, 5, 2, 0.0F, false));
      this.cube_r23 = new ModelRenderer(this);
      this.cube_r23.setRotationPoint(-3.0F, -28.0F, 5.0F);
      this.hair2.addChild(this.cube_r23);
      setRotationAngle(this.cube_r23, 0.0F, 0.0F, -0.2618F);
      this.cube_r23.cubeList.add(new ModelBox(this.cube_r23, 0, 0, -2.0F, -8.0F, 0.0F, 2, 8, 2, 0.0F, false));
      this.tentacle9 = new ModelRenderer(this);
      this.tentacle9.setRotationPoint(12.0F, -10.0F, 10.0F);
      this.main.addChild(this.tentacle9);
      this.cube_r18 = new ModelRenderer(this);
      this.cube_r18.setRotationPoint(-16.0F, -16.0F, 1.0F);
      this.tentacle9.addChild(this.cube_r18);
      setRotationAngle(this.cube_r18, -0.1745F, 0.0F, -0.1309F);
      this.cube_r18.cubeList.add(new ModelBox(this.cube_r18, 0, 0, -2.0F, -9.0F, 0.0F, 2, 9, 2, 0.0F, false));
      this.cube_r19 = new ModelRenderer(this);
      this.cube_r19.setRotationPoint(-15.0F, -8.0F, -3.0F);
      this.tentacle9.addChild(this.cube_r19);
      setRotationAngle(this.cube_r19, -0.4363F, 0.0F, -0.1745F);
      this.cube_r19.cubeList.add(new ModelBox(this.cube_r19, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.hair1 = new ModelRenderer(this);
      this.hair1.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.main.addChild(this.hair1);
      this.cube_r20 = new ModelRenderer(this);
      this.cube_r20.setRotationPoint(-5.0F, -35.0F, 5.0F);
      this.hair1.addChild(this.cube_r20);
      setRotationAngle(this.cube_r20, 0.0F, 0.0F, 0.2618F);
      this.cube_r20.cubeList.add(new ModelBox(this.cube_r20, 0, 0, -2.0F, -5.0F, 0.0F, 2, 5, 2, 0.0F, false));
      this.cube_r21 = new ModelRenderer(this);
      this.cube_r21.setRotationPoint(-3.0F, -28.0F, 5.0F);
      this.hair1.addChild(this.cube_r21);
      setRotationAngle(this.cube_r21, 0.0F, 0.0F, -0.2618F);
      this.cube_r21.cubeList.add(new ModelBox(this.cube_r21, 0, 0, -2.0F, -8.0F, 0.0F, 2, 8, 2, 0.0F, false));
      this.tentacle8 = new ModelRenderer(this);
      this.tentacle8.setRotationPoint(16.0F, -12.0F, 10.0F);
      this.main.addChild(this.tentacle8);
      setRotationAngle(this.tentacle8, -0.0436F, 0.0F, 0.0F);
      this.cube_r16 = new ModelRenderer(this);
      this.cube_r16.setRotationPoint(-16.0F, -8.0F, 1.0F);
      this.tentacle8.addChild(this.cube_r16);
      setRotationAngle(this.cube_r16, 0.0F, 0.0F, -0.1309F);
      this.cube_r16.cubeList.add(new ModelBox(this.cube_r16, 0, 0, -0.9568F, -19.924F, -0.349F, 2, 12, 2, 0.0F, false));
      this.cube_r17 = new ModelRenderer(this);
      this.cube_r17.setRotationPoint(-15.0F, 0.0F, -3.0F);
      this.tentacle8.addChild(this.cube_r17);
      setRotationAngle(this.cube_r17, -0.4363F, 0.0F, -0.1745F);
      this.cube_r17.cubeList.add(new ModelBox(this.cube_r17, 0, 0, -0.6121F, -17.986F, -3.6427F, 2, 11, 2, 0.0F, false));
      this.tentacle7 = new ModelRenderer(this);
      this.tentacle7.setRotationPoint(21.0F, -10.0F, 10.0F);
      this.main.addChild(this.tentacle7);
      this.cube_r14 = new ModelRenderer(this);
      this.cube_r14.setRotationPoint(-16.0F, -16.0F, 1.0F);
      this.tentacle7.addChild(this.cube_r14);
      setRotationAngle(this.cube_r14, -0.1745F, 0.0F, -0.1309F);
      this.cube_r14.cubeList.add(new ModelBox(this.cube_r14, 0, 0, -2.0F, -9.0F, 0.0F, 2, 9, 2, 0.0F, false));
      this.cube_r15 = new ModelRenderer(this);
      this.cube_r15.setRotationPoint(-15.0F, -8.0F, -3.0F);
      this.tentacle7.addChild(this.cube_r15);
      setRotationAngle(this.cube_r15, -0.4363F, 0.0F, -0.1745F);
      this.cube_r15.cubeList.add(new ModelBox(this.cube_r15, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.tentacle6 = new ModelRenderer(this);
      this.tentacle6.setRotationPoint(30.0F, -10.0F, 6.0F);
      this.main.addChild(this.tentacle6);
      this.cube_r12 = new ModelRenderer(this);
      this.cube_r12.setRotationPoint(-16.0F, -16.0F, 1.0F);
      this.tentacle6.addChild(this.cube_r12);
      setRotationAngle(this.cube_r12, -0.1745F, 0.0F, 0.1309F);
      this.cube_r12.cubeList.add(new ModelBox(this.cube_r12, 0, 0, -2.0F, -9.0F, 0.0F, 2, 9, 2, 0.0F, false));
      this.cube_r13 = new ModelRenderer(this);
      this.cube_r13.setRotationPoint(-22.0F, -8.0F, 0.0F);
      this.tentacle6.addChild(this.cube_r13);
      setRotationAngle(this.cube_r13, -0.1309F, 0.0F, 0.6109F);
      this.cube_r13.cubeList.add(new ModelBox(this.cube_r13, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.tentacle5 = new ModelRenderer(this);
      this.tentacle5.setRotationPoint(31.0F, -9.0F, 1.0F);
      this.main.addChild(this.tentacle5);
      this.cube_r10 = new ModelRenderer(this);
      this.cube_r10.setRotationPoint(-16.0F, -16.0F, 0.0F);
      this.tentacle5.addChild(this.cube_r10);
      setRotationAngle(this.cube_r10, 0.0436F, 0.0F, 0.1309F);
      this.cube_r10.cubeList.add(new ModelBox(this.cube_r10, 0, 0, -2.0F, -9.0F, 0.0F, 2, 9, 2, 0.0F, false));
      this.cube_r11 = new ModelRenderer(this);
      this.cube_r11.setRotationPoint(-22.0F, -8.0F, 0.0F);
      this.tentacle5.addChild(this.cube_r11);
      setRotationAngle(this.cube_r11, 0.0F, 0.0F, 0.6109F);
      this.cube_r11.cubeList.add(new ModelBox(this.cube_r11, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.tentacle4 = new ModelRenderer(this);
      this.tentacle4.setRotationPoint(31.0F, -12.0F, -4.0F);
      this.main.addChild(this.tentacle4);
      this.cube_r8 = new ModelRenderer(this);
      this.cube_r8.setRotationPoint(-16.0F, -16.0F, 0.0F);
      this.tentacle4.addChild(this.cube_r8);
      setRotationAngle(this.cube_r8, 0.0436F, 0.0F, 0.2618F);
      this.cube_r8.cubeList.add(new ModelBox(this.cube_r8, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.cube_r9 = new ModelRenderer(this);
      this.cube_r9.setRotationPoint(-22.0F, -8.0F, 0.0F);
      this.tentacle4.addChild(this.cube_r9);
      setRotationAngle(this.cube_r9, 0.0F, 0.0F, 0.6109F);
      this.cube_r9.cubeList.add(new ModelBox(this.cube_r9, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.tentacle3 = new ModelRenderer(this);
      this.tentacle3.setRotationPoint(4.0F, -12.0F, -4.0F);
      this.main.addChild(this.tentacle3);
      this.tentacle3.cubeList.add(new ModelBox(this.tentacle3, 0, 0, -20.0F, -26.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.cube_r7 = new ModelRenderer(this);
      this.cube_r7.setRotationPoint(-11.0F, -8.0F, 0.0F);
      this.tentacle3.addChild(this.cube_r7);
      setRotationAngle(this.cube_r7, 0.0F, 0.0F, -0.7418F);
      this.cube_r7.cubeList.add(new ModelBox(this.cube_r7, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.tentacle2 = new ModelRenderer(this);
      this.tentacle2.setRotationPoint(4.0F, -11.0F, 4.0F);
      this.main.addChild(this.tentacle2);
      this.cube_r5 = new ModelRenderer(this);
      this.cube_r5.setRotationPoint(-15.0F, -17.0F, 0.0F);
      this.tentacle2.addChild(this.cube_r5);
      setRotationAngle(this.cube_r5, 0.0F, 0.0F, -0.0873F);
      this.cube_r5.cubeList.add(new ModelBox(this.cube_r5, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.cube_r6 = new ModelRenderer(this);
      this.cube_r6.setRotationPoint(-11.0F, -8.0F, 0.0F);
      this.tentacle2.addChild(this.cube_r6);
      setRotationAngle(this.cube_r6, 0.0F, 0.0F, -0.3927F);
      this.cube_r6.cubeList.add(new ModelBox(this.cube_r6, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.tentacle1 = new ModelRenderer(this);
      this.tentacle1.setRotationPoint(4.0F, -10.0F, 0.0F);
      this.main.addChild(this.tentacle1);
      this.cube_r3 = new ModelRenderer(this);
      this.cube_r3.setRotationPoint(-15.0F, -17.0F, 0.0F);
      this.tentacle1.addChild(this.cube_r3);
      setRotationAngle(this.cube_r3, 0.0F, 0.0F, -0.0873F);
      this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.cube_r4 = new ModelRenderer(this);
      this.cube_r4.setRotationPoint(-11.0F, -8.0F, 0.0F);
      this.tentacle1.addChild(this.cube_r4);
      setRotationAngle(this.cube_r4, 0.0F, 0.0F, -0.3927F);
      this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 0, 0, -2.0F, -11.0F, 0.0F, 2, 11, 2, 0.0F, false));
      this.down = new ModelRenderer(this);
      this.down.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.main.addChild(this.down);
      this.cube_r1 = new ModelRenderer(this);
      this.cube_r1.setRotationPoint(0.0F, -12.0F, 1.0F);
      this.down.addChild(this.cube_r1);
      setRotationAngle(this.cube_r1, 0.48F, 0.0F, 0.0F);
      this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 0, 0, -8.0F, -3.0F, -7.0F, 16, 3, 15, 0.0F, false));
      this.cube_r2 = new ModelRenderer(this);
      this.cube_r2.setRotationPoint(-6.0F, -12.0F, -5.0F);
      this.down.addChild(this.cube_r2);
      setRotationAngle(this.cube_r2, 0.48F, 0.0F, 0.0F);
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, -1.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F, false));
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, 12.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F, false));
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, 1.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F, false));
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, 3.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F, false));
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, 5.0F, -3.0F, 0.0F, 2, 3, 1, 0.0F, false));
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, 10.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F, false));
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, 8.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F, false));
      this.head = new ModelRenderer(this);
      this.head.setRotationPoint(0.0F, -1.0F, -1.0F);
      this.main.addChild(this.head);
      setRotationAngle(this.head, -0.1745F, 0.0F, 0.0F);
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, -8.0F, -27.8785F, -9.3892F, 16, 12, 16, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 61, 51, -6.0F, -25.8785F, -10.3892F, 12, 7, 1, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 85, 82, -2.0F, -23.8785F, -11.3892F, 4, 3, 1, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, 7.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, 5.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, 3.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, -8.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, -6.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, -4.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, 1.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.head.cubeList.add(new ModelBox(this.head, 0, 0, -2.0F, -17.8785F, -7.3892F, 1, 4, 1, 0.0F, false));
      this.circle1 = new ModelRenderer(this);
      this.circle1.setRotationPoint(-2.0F, 6.0F, 0.0F);
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, -13.0F, -4.0F, -21.0F, 4, 4, 4, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, -17.0F, -4.0F, -17.0F, 4, 4, 4, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, 17.0F, -4.0F, 13.0F, 4, 4, 4, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, 17.0F, -4.0F, -17.0F, 4, 4, 4, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, -9.0F, -4.0F, -25.0F, 22, 4, 4, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, -9.0F, -4.0F, 21.0F, 22, 4, 4, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, -17.0F, -4.0F, 13.0F, 4, 4, 4, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, 21.0F, -4.0F, -13.0F, 4, 4, 26, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, -21.0F, -4.0F, -13.0F, 4, 4, 26, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, 13.0F, -4.0F, -21.0F, 4, 4, 4, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, 13.0F, -4.0F, 17.0F, 4, 4, 4, 0.0F, false));
      this.circle1.cubeList.add(new ModelBox(this.circle1, 0, 0, -13.0F, -4.0F, 17.0F, 4, 4, 4, 0.0F, false));
      this.circle2 = new ModelRenderer(this);
      this.circle2.setRotationPoint(-2.0F, 3.0F, -1.0F);
      setRotationAngle(this.circle2, -1.5272F, 0.0F, 0.0F);
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, -13.0F, -4.0F, -21.0F, 4, 4, 4, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, -17.0F, -4.0F, -17.0F, 4, 4, 4, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, 17.0F, -4.0F, 13.0F, 4, 4, 4, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, 17.0F, -4.0F, -17.0F, 4, 4, 4, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, -9.0F, -4.0F, -25.0F, 22, 4, 4, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, -9.0F, -4.0F, 21.0F, 22, 4, 4, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, -17.0F, -4.0F, 13.0F, 4, 4, 4, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, 21.0F, -4.0F, -13.0F, 4, 4, 26, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, -21.0F, -4.0F, -13.0F, 4, 4, 26, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, 13.0F, -4.0F, -21.0F, 4, 4, 4, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, 13.0F, -4.0F, 17.0F, 4, 4, 4, 0.0F, false));
      this.circle2.cubeList.add(new ModelBox(this.circle2, 0, 0, -13.0F, -4.0F, 17.0F, 4, 4, 4, 0.0F, false));
      this.circle3 = new ModelRenderer(this);
      this.circle3.setRotationPoint(-2.0F, 3.0F, -1.0F);
      setRotationAngle(this.circle3, -1.5272F, 1.5272F, 0.0F);
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, -13.0F, -4.0F, -21.0F, 4, 4, 4, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, -17.0F, -4.0F, -17.0F, 4, 4, 4, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, 17.0F, -4.0F, 13.0F, 4, 4, 4, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, 17.0F, -4.0F, -17.0F, 4, 4, 4, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, -9.0F, -4.0F, -25.0F, 22, 4, 4, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, -9.0F, -4.0F, 21.0F, 22, 4, 4, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, -17.0F, -4.0F, 13.0F, 4, 4, 4, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, 21.0F, -4.0F, -13.0F, 4, 4, 26, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, -21.0F, -4.0F, -13.0F, 4, 4, 26, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, 13.0F, -4.0F, -21.0F, 4, 4, 4, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, 13.0F, -4.0F, 17.0F, 4, 4, 4, 0.0F, false));
      this.circle3.cubeList.add(new ModelBox(this.circle3, 0, 0, -13.0F, -4.0F, 17.0F, 4, 4, 4, 0.0F, false));
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.main.render(f5);
      this.circle1.render(f5);
      this.circle2.render(f5);
      this.circle3.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
      this.main.rotateAngleY = f3 / 57.295776F;
      this.main.rotateAngleX = f4 / 57.295776F;
      this.circle2.rotateAngleY = f2 / 20.0F;
      this.circle3.rotateAngleZ = f2 / 20.0F;
      this.circle1.rotateAngleX = f2 / 20.0F;
    }
  }
}

