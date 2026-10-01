package com.gmm.gctold.common.entity;

import java.util.ArrayList;
import java.util.Iterator;
import com.gmm.gctold.common.potions.PotionCurseOfTwilight;
import com.gmm.gctold.misc.registry.GctAllItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

public final class EntityApocalypseCube {


  private Biome[] allbiomes(RegistryNamespaced<ResourceLocation, Biome> in) {
    Iterator<Biome> itr = in.iterator();
    ArrayList<Biome> ls = new ArrayList<>();
    while (itr.hasNext())
      ls.add(itr.next());
    return ls.<Biome>toArray(new Biome[ls.size()]);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(ApocalypseCubeEntity.class, renderManager -> new RenderLiving(renderManager, new ModelTFApocalypseCube(), 1.0F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/modeltfapocalypsecube-texture.png");
          }
        });
  }

  public static class ApocalypseCubeEntity extends EntityMob {
    private final BossInfoServer bossInfo;

    public ApocalypseCubeEntity(World world) {
      super(world);
      this.bossInfo = new BossInfoServer(getDisplayName(), BossInfo.Color.RED, BossInfo.Overlay.PROGRESS);
      setSize(2.0F, 2.0F);
      this.experienceValue = 200;
      this.isImmuneToFire = true;
      setNoAI(false);
      enablePersistence();
      this.navigator = (PathNavigate)new PathNavigateFlying((EntityLiving)this, this.world);
      this.moveHelper = (EntityMoveHelper)new EntityFlyHelper((EntityLiving)this);
    }

    protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
      this.targetTasks.addTask(2, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
      this.tasks.addTask(3, (EntityAIBase)new EntityAIAttackMelee((EntityCreature)this, 4.0D, true));
      this.targetTasks.addTask(4, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityPlayer.class, false, false));
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
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.irongolem.hurt"));
    }

    public SoundEvent getDeathSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.irongolem.death"));
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
      return super.attackEntityFrom(source, amount);
    }

    public void onDeath(DamageSource source) {
      super.onDeath(source);
      if (!this.world.isRemote) {
        spawnApocalypsiumScraps();
      }
    }

    public void onEntityUpdate() {
      super.onEntityUpdate();
      if (!this.world.isRemote) {
        runApocalypseSkills();
      }
    }

    public void onCollideWithPlayer(EntityPlayer entity) {
      super.onCollideWithPlayer(entity);
      if (!this.world.isRemote && this.rand.nextDouble() < 0.3D) {
        entity.attackEntityFrom(DamageSource.GENERIC, entity.getMaxHealth() / 15.0F);
        entity.setPositionAndUpdate(
            this.posX + this.rand.nextInt(11),
            this.posY + this.rand.nextInt(11),
            this.posZ + this.rand.nextInt(11));
      }
    }

    private void runApocalypseSkills() {
      if (this.rand.nextDouble() < 0.001D) {
        spawnAdherent(this.posX + 6.0D, this.posY + 1.0D, this.posZ);
        spawnAdherent(this.posX - 6.0D, this.posY + 1.0D, this.posZ);
      }
      if (this.rand.nextDouble() < 0.001D) {
        spawnAdherent(this.posX, this.posY + 1.0D, this.posZ + 6.0D);
        spawnAdherent(this.posX, this.posY + 1.0D, this.posZ - 6.0D);
      }
      if (this.rand.nextDouble() < 0.005D) {
        sendMessageToNearbyPlayers(32.0D, "你过于靠近天启立方，受到了天启诅咒……");
        applyEffectToNearbyPlayers(32.0D, MobEffects.BLINDNESS, 100, 0);
        applyEffectToNearbyPlayers(32.0D, MobEffects.WITHER, 100, 2);
        applyEffectToNearbyPlayers(32.0D, MobEffects.SLOWNESS, 100, 4);
        applyEffectToNearbyPlayers(32.0D, PotionCurseOfTwilight.potion, 100, 0);
      }
      if (this.rand.nextDouble() < 0.005D && this.getHealth() <= 40.0F) {
        sendMessageToNearbyPlayers(8.0D, "天启不死……");
        setHealth(1000.0F);
      }
    }

    private void spawnAdherent(double x, double y, double z) {
      Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation("twilightforest:adherent"), this.world);
      if (entity != null) {
        entity.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
        this.world.spawnEntity(entity);
      }
    }

    private void applyEffectToNearbyPlayers(double radius, net.minecraft.potion.Potion potion, int ticks, int amplifier) {
      if (potion == null) {
        return;
      }
      for (EntityPlayer player : getNearbyPlayers(radius)) {
        player.addPotionEffect(new PotionEffect(potion, ticks, amplifier, false, true));
      }
    }

    private void sendMessageToNearbyPlayers(double radius, String message) {
      TextComponentString component = new TextComponentString(message);
      for (EntityPlayer player : getNearbyPlayers(radius)) {
        player.sendMessage(component);
      }
    }

    private java.util.List<EntityPlayer> getNearbyPlayers(double radius) {
      AxisAlignedBB area = new AxisAlignedBB(getPosition()).grow(radius);
      double maxDistance = radius * radius;
      return this.world.getEntitiesWithinAABB(EntityPlayer.class, area,
          player -> player.getDistanceSq(this) <= maxDistance);
    }

    private void spawnApocalypsiumScraps() {
      ItemStack stack = new ItemStack(GctAllItems.APOCALYPSIUM_SCRAP, this.rand.nextInt(16) + 10);
      EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, stack);
      item.setPickupDelay(10);
      this.world.spawnEntity(item);
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(5.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(9000.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
      getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
      getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(5.0D);
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
  }

  public static class ModelTFApocalypseCube extends ModelBase {
    public ModelRenderer body;

    public ModelRenderer leg2;

    public ModelRenderer leg4;

    public ModelRenderer leg1;

    public ModelRenderer leg3;

    public ModelRenderer head;

    public ModelTFApocalypseCube() {
      this.textureWidth = 128;
      this.textureHeight = 64;
      this.body = new ModelRenderer(this, 0, 0);
      this.body.setRotationPoint(0.0F, 0.0F, -2.0F);
      this.body.addBox(-16.0F, -16.0F, -16.0F, 32, 32, 32, 0.0F);
      this.leg3 = new ModelRenderer(this, 0, 0);
      this.leg3.setRotationPoint(-9.0F, 16.0F, -14.0F);
      this.leg3.addBox(-4.0F, 0.0F, -4.0F, 8, 8, 8, 0.0F);
      this.leg4 = new ModelRenderer(this, 0, 0);
      this.leg4.setRotationPoint(9.0F, 16.0F, -14.0F);
      this.leg4.addBox(-4.0F, 0.0F, -4.0F, 8, 8, 8, 0.0F);
      this.leg1 = new ModelRenderer(this, 0, 0);
      this.leg1.setRotationPoint(-6.0F, 16.0F, 9.0F);
      this.leg1.addBox(-4.0F, 0.0F, -4.0F, 8, 8, 8, 0.0F);
      this.leg2 = new ModelRenderer(this, 0, 0);
      this.leg2.setRotationPoint(6.0F, 16.0F, 9.0F);
      this.leg2.addBox(-4.0F, 0.0F, -4.0F, 8, 8, 8, 0.0F);
      this.head = new ModelRenderer(this, 0, 0);
      this.head.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.head.addBox(0.0F, 0.0F, 0.0F, 0, 0, 0, 0.0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.body.render(f5);
      this.leg3.render(f5);
      this.leg4.render(f5);
      this.leg1.render(f5);
      this.leg2.render(f5);
      this.head.render(f5);
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
      this.body.rotateAngleY = f3 / 57.295776F;
      this.body.rotateAngleX = f4 / 57.295776F;
      this.leg2.rotateAngleX = MathHelper.cos(f * 1.0F) * 1.0F * f1;
      this.leg4.rotateAngleX = MathHelper.cos(f * 1.0F) * 1.0F * f1;
      this.leg1.rotateAngleX = MathHelper.cos(f * 1.0F) * -1.0F * f1;
      this.leg3.rotateAngleX = MathHelper.cos(f * 1.0F) * -1.0F * f1;
    }
  }
}

