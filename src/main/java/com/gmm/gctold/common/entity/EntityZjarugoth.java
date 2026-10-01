package com.gmm.gctold.common.entity;

import com.gmm.gctold.misc.registry.GctAllItems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import com.gmm.gctold.common.blocks.BlockBlueFire;
import com.gmm.gctold.common.potions.PotionStop;
import com.gmm.gctold.common.potions.PotionZjarugothDamage;
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
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityZjarugoth {


  private Biome[] allbiomes(RegistryNamespaced<ResourceLocation, Biome> in) {
    Iterator<Biome> itr = in.iterator();
    ArrayList<Biome> ls = new ArrayList<>();
    while (itr.hasNext())
      ls.add(itr.next());
    return ls.<Biome>toArray(new Biome[ls.size()]);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(ZjarugothEntity.class, renderManager -> new RenderLiving(renderManager, new ModelZjarugoth(), 0.5F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/zjarugorth.png");
          }
        });
  }

  public static class ZjarugothEntity extends EntityMob {
    private final BossInfoServer bossInfo;

    public ZjarugothEntity(World world) {
      super(world);
      this.bossInfo = new BossInfoServer(getDisplayName(), BossInfo.Color.BLUE, BossInfo.Overlay.NOTCHED_6);
      setSize(2.0F, 4.0F);
      this.experienceValue = 100;
      this.isImmuneToFire = true;
      setNoAI(false);
      enablePersistence();
      this.navigator = (PathNavigate)new PathNavigateFlying((EntityLiving)this, this.world);
      this.moveHelper = (EntityMoveHelper)new EntityFlyHelper((EntityLiving)this);
    }

    protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, new EntityAIBase() {
            public boolean shouldExecute() {
              if (EntityZjarugoth.ZjarugothEntity.this.getAttackTarget() != null && !EntityZjarugoth.ZjarugothEntity.this.getMoveHelper().isUpdating())
                return true;
              return false;
            }

            public boolean shouldContinueExecuting() {
              return (EntityZjarugoth.ZjarugothEntity.this.getMoveHelper().isUpdating() && EntityZjarugoth.ZjarugothEntity.this.getAttackTarget() != null && EntityZjarugoth.ZjarugothEntity.this.getAttackTarget().isEntityAlive());
            }

            public void startExecuting() {
              EntityLivingBase livingentity = EntityZjarugoth.ZjarugothEntity.this.getAttackTarget();
              Vec3d vec3d = livingentity.getPositionEyes(1.0F);
              EntityZjarugoth.ZjarugothEntity.this.moveHelper.setMoveTo(vec3d.x, vec3d.y, vec3d.z, 5.0D);
            }

            public void updateTask() {
              EntityLivingBase livingentity = EntityZjarugoth.ZjarugothEntity.this.getAttackTarget();
              double d0 = EntityZjarugoth.ZjarugothEntity.this.getDistanceSq((Entity)livingentity);
              if (d0 <= getAttackReachSq(livingentity)) {
                EntityZjarugoth.ZjarugothEntity.this.attackEntityAsMob((Entity)livingentity);
              } else if (d0 < 256.0D) {
                Vec3d vec3d = livingentity.getPositionEyes(1.0F);
                EntityZjarugoth.ZjarugothEntity.this.moveHelper.setMoveTo(vec3d.x, vec3d.y, vec3d.z, 5.0D);
              }
            }

            protected double getAttackReachSq(EntityLivingBase attackTarget) {
              return EntityZjarugoth.ZjarugothEntity.this.width * 1.5D * EntityZjarugoth.ZjarugothEntity.this.height * 1.5D + attackTarget.height;
            }
          });
      this.tasks.addTask(2, (EntityAIBase)new EntityAIWander((EntityCreature)this, 3.0D, 20) {
            protected Vec3d getPosition() {
              Random random = EntityZjarugoth.ZjarugothEntity.this.getRNG();
              double dir_x = EntityZjarugoth.ZjarugothEntity.this.posX + ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
              double dir_y = EntityZjarugoth.ZjarugothEntity.this.posY + ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
              double dir_z = EntityZjarugoth.ZjarugothEntity.this.posZ + ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
              return new Vec3d(dir_x, dir_y, dir_z);
            }
          });
      this.tasks.addTask(3, (EntityAIBase)new EntityAIAttackMelee((EntityCreature)this, 1.6D, false));
      this.tasks.addTask(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
      this.targetTasks.addTask(5, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityPlayer.class, false, false));
      this.targetTasks.addTask(6, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
    }

    public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEFINED;
    }

    protected boolean canDespawn() {
      return false;
    }

    protected Item getDropItem() {
      return (new ItemStack(GctAllItems.FURTHER_SOUL, 1)).getItem();
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

    public void fall(float l, float d) {}

    public boolean attackEntityFrom(DamageSource source, float amount) {
      if (!this.world.isRemote && this.rand.nextDouble() < 0.02D) {
        applyPotionToNearbyPlayers(128.0D, PotionStop.potion, 60, 0, false, false);
      }
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
      sendMessageToNearbyPlayers(64.0D, "<Zjarugoth>不好意思，我似乎不是一个合格的旧日支配者。");
    }

    public void onEntityUpdate() {
      super.onEntityUpdate();
      if (!this.world.isRemote) {
        clearNearbyZjarugothPotions();
        runZjarugothSkills();
      }
    }

    private void clearNearbyZjarugothPotions() {
      AxisAlignedBB area = new AxisAlignedBB(getPosition()).grow(10.0D);
      for (ZjarugothEntity entity : this.world.getEntitiesWithinAABB(ZjarugothEntity.class, area,
          entity -> entity.getDistanceSq(this) <= 100.0D)) {
        entity.clearActivePotions();
      }
    }

    private void runZjarugothSkills() {
      if (this.rand.nextDouble() < 0.005D) {
        sendMessageToNearbyPlayers(64.0D, "<Zjarugoth>哈，你似乎认为我比扎哈尔那家伙弱？");
      }
      if (this.rand.nextDouble() < 0.008D) {
        sendMessageToNearbyPlayers(64.0D, "<Zjarugoth>Ymg' uaaah ya lloigazath!");
        createRandomExplosion();
      }
      if (this.rand.nextDouble() < 0.008D) {
        sendMessageToNearbyPlayers(64.0D, "<Zjarugoth>Ymg' uaaah ya lloigazath!");
        applyPotionToNearbyPlayers(64.0D, PotionZjarugothDamage.potion, 1, 0, false, false);
        SoundEvent roar = SoundEvent.REGISTRY.getObject(new ResourceLocation("gctcore:zjarugoth_roar"));
        if (roar != null) {
          this.world.playSound(null, this.posX, this.posY, this.posZ, roar, SoundCategory.NEUTRAL, 4.0F, 1.0F);
        }
      }
      if (this.rand.nextDouble() < 0.002D) {
        sendMessageToNearbyPlayers(64.0D, "<Zjarugoth>Fm'latghor, fm'latgh!");
        placeBlueFireRing();
      }
    }

    private void createRandomExplosion() {
      int x = (int)this.posX + (this.rand.nextBoolean() ? 6 : -6) + this.rand.nextInt(10);
      int y = (int)this.posY - 5 + this.rand.nextInt(10);
      int z = (int)this.posZ + (this.rand.nextBoolean() ? 6 : -6) + this.rand.nextInt(10);
      this.world.createExplosion(null, x, y, z, 4.0F, true);
    }

    private void placeBlueFireRing() {
      BlockPos center = getPosition().up(6);
      int[][] offsets = {
          { 3, 0 }, { 3, 1 }, { 3, -1 }, { -3, 0 }, { -3, 1 }, { -3, -1 },
          { 0, 3 }, { 1, 3 }, { -1, 3 }, { 0, -3 }, { 1, -3 }, { -1, -3 },
          { 2, 2 }, { -2, 2 }, { 2, -2 }, { -2, -2 }
      };
      for (int[] offset : offsets) {
        this.world.setBlockState(center.add(offset[0], 0, offset[1]), BlockBlueFire.block.getDefaultState(), 3);
      }
    }

    private void sendMessageToNearbyPlayers(double radius, String message) {
      if (this.world.isRemote) {
        return;
      }
      AxisAlignedBB area = new AxisAlignedBB(getPosition()).grow(radius);
      double maxDistance = radius * radius;
      TextComponentString component = new TextComponentString(message);
      for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, area,
          player -> player.getDistanceSq(this) <= maxDistance)) {
        player.sendMessage(component);
      }
    }

    private void applyPotionToNearbyPlayers(double radius, Potion potion, int ticks, int amplifier,
        boolean ambient, boolean particles) {
      if (this.world.isRemote || potion == null) {
        return;
      }
      AxisAlignedBB area = new AxisAlignedBB(getPosition()).grow(radius);
      double maxDistance = radius * radius;
      for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, area,
          player -> player.getDistanceSq(this) <= maxDistance)) {
        player.addPotionEffect(new PotionEffect(potion, ticks, amplifier, ambient, particles));
      }
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(108000.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
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
  }

  public static class ModelZjarugoth extends ModelBase {
    private final ModelRenderer main;

    private final ModelRenderer mainChild10;

    private final ModelRenderer mainChild0;

    private final ModelRenderer mainChild4;

    private final ModelRenderer mainChild8;

    private final ModelRenderer mainChild2;

    private final ModelRenderer mainChild6;

    private final ModelRenderer mainChild9;

    private final ModelRenderer mainChild;

    private final ModelRenderer mainChild_r1;

    private final ModelRenderer mainChildChild_r1;

    private final ModelRenderer mainChild_r2;

    private final ModelRenderer mainChildChild_r2;

    private final ModelRenderer mainChild_r3;

    private final ModelRenderer mainChild_r4;

    private final ModelRenderer mainChildChild_r3;

    private final ModelRenderer mainChild_r5;

    private final ModelRenderer mainChildChild;

    private final ModelRenderer mainChildChild_r4;

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

    public ModelZjarugoth() {
      this.textureWidth = 32;
      this.textureHeight = 32;
      this.main = new ModelRenderer(this);
      this.main.setRotationPoint(0.0F, -24.0F, 0.0F);
      this.main.cubeList.add(new ModelBox(this.main, 0, 0, 0.0F, 30.0F, 0.0F, 0, 0, 0, 0.0F, false));
      this.mainChild10 = new ModelRenderer(this);
      this.mainChild10.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild10);
      setRotationAngle(this.mainChild10, -0.1745F, 0.0F, 0.0F);
      this.mainChild10.cubeList.add(new ModelBox(this.mainChild10, 0, 0, -4.0F, -6.2126F, -9.5699F, 2, 8, 2, 0.0F, false));
      this.mainChild0 = new ModelRenderer(this);
      this.mainChild0.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild0);
      setRotationAngle(this.mainChild0, -0.1745F, 0.0F, 0.0F);
      this.mainChild0.cubeList.add(new ModelBox(this.mainChild0, 0, 0, -16.0F, -26.2126F, -13.5699F, 32, 24, 32, 0.0F, false));
      this.mainChild4 = new ModelRenderer(this);
      this.mainChild4.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild4);
      setRotationAngle(this.mainChild4, -0.1745F, 0.0F, 0.0F);
      this.mainChild4.cubeList.add(new ModelBox(this.mainChild4, 0, 0, 10.0F, -6.2126F, -9.5699F, 2, 8, 2, 0.0F, false));
      this.mainChild8 = new ModelRenderer(this);
      this.mainChild8.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild8);
      setRotationAngle(this.mainChild8, -0.1745F, 0.0F, 0.0F);
      this.mainChild8.cubeList.add(new ModelBox(this.mainChild8, 0, 0, -8.0F, -6.2126F, -9.5699F, 2, 8, 2, 0.0F, false));
      this.mainChild2 = new ModelRenderer(this);
      this.mainChild2.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild2);
      setRotationAngle(this.mainChild2, -0.1745F, 0.0F, 0.0F);
      this.mainChild2.cubeList.add(new ModelBox(this.mainChild2, 8, 7, -4.0F, -18.2126F, -17.5699F, 8, 6, 2, 0.0F, false));
      this.mainChild6 = new ModelRenderer(this);
      this.mainChild6.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild6);
      setRotationAngle(this.mainChild6, -0.1745F, 0.0F, 0.0F);
      this.mainChild6.cubeList.add(new ModelBox(this.mainChild6, 0, 0, -16.0F, -6.2126F, -9.5699F, 2, 8, 2, 0.0F, false));
      this.mainChild9 = new ModelRenderer(this);
      this.mainChild9.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild9);
      setRotationAngle(this.mainChild9, -0.1745F, 0.0F, 0.0F);
      this.mainChild9.cubeList.add(new ModelBox(this.mainChild9, 0, 0, 2.0F, -6.2126F, -9.5699F, 2, 8, 2, 0.0F, false));
      this.mainChild = new ModelRenderer(this);
      this.mainChild.setRotationPoint(8.0F, -24.0F, -8.0F);
      this.main.addChild(this.mainChild);
      this.mainChild.cubeList.add(new ModelBox(this.mainChild, 0, 0, -40.0F, -22.0F, 0.0F, 4, 22, 4, 0.0F, false));
      this.mainChild.cubeList.add(new ModelBox(this.mainChild, 0, 0, 18.0F, -16.0F, 10.0F, 4, 18, 4, 0.0F, false));
      this.mainChild.cubeList.add(new ModelBox(this.mainChild, 0, 0, 18.0F, -19.0F, 19.0F, 4, 18, 4, 0.0F, false));
      this.mainChild_r1 = new ModelRenderer(this);
      this.mainChild_r1.setRotationPoint(12.0F, 98.0F, 52.0F);
      this.mainChild.addChild(this.mainChild_r1);
      setRotationAngle(this.mainChild_r1, -0.0873F, 0.0F, 0.0F);
      this.mainChild_r1.cubeList.add(new ModelBox(this.mainChild_r1, 0, 0, -12.0F, -104.0F, -24.0F, 4, 20, 4, 0.0F, false));
      this.mainChild_r1.cubeList.add(new ModelBox(this.mainChild_r1, 0, 0, -12.0F, -110.0F, -24.0F, 4, 26, 4, 0.0F, false));
      this.mainChildChild_r1 = new ModelRenderer(this);
      this.mainChildChild_r1.setRotationPoint(12.0F, 68.0F, 52.0F);
      this.mainChild.addChild(this.mainChildChild_r1);
      setRotationAngle(this.mainChildChild_r1, -1.667F, -0.4346F, 1.4805F);
      this.mainChildChild_r1.cubeList.add(new ModelBox(this.mainChildChild_r1, 0, 0, -60.7172F, -13.018F, 2.0F, 4, 22, 4, 0.0F, false));
      this.mainChild_r2 = new ModelRenderer(this);
      this.mainChild_r2.setRotationPoint(7.0F, 94.0F, 52.0F);
      this.mainChild.addChild(this.mainChild_r2);
      setRotationAngle(this.mainChild_r2, -0.0873F, 0.0F, 0.0F);
      this.mainChild_r2.cubeList.add(new ModelBox(this.mainChild_r2, 0, 0, -12.0F, -110.0F, -24.0F, 4, 26, 4, 0.0F, false));
      this.mainChild_r2.cubeList.add(new ModelBox(this.mainChild_r2, 0, 0, -12.0F, -110.0F, -24.0F, 4, 26, 4, 0.0F, false));
      this.mainChildChild_r2 = new ModelRenderer(this);
      this.mainChildChild_r2.setRotationPoint(7.0F, 64.0F, 52.0F);
      this.mainChild.addChild(this.mainChildChild_r2);
      setRotationAngle(this.mainChildChild_r2, -1.667F, -0.4346F, 1.4805F);
      this.mainChildChild_r2.cubeList.add(new ModelBox(this.mainChildChild_r2, 0, 0, -60.7172F, -13.018F, 2.0F, 4, 22, 4, 0.0F, false));
      this.mainChild_r3 = new ModelRenderer(this);
      this.mainChild_r3.setRotationPoint(-8.0F, 102.0F, 52.0F);
      this.mainChild.addChild(this.mainChild_r3);
      setRotationAngle(this.mainChild_r3, -0.0873F, 0.0F, 0.0F);
      this.mainChild_r3.cubeList.add(new ModelBox(this.mainChild_r3, 0, 0, -12.0F, -110.0F, -24.0F, 4, 26, 4, 0.0F, false));
      this.mainChild_r4 = new ModelRenderer(this);
      this.mainChild_r4.setRotationPoint(-8.0F, 102.0F, 52.0F);
      this.mainChild.addChild(this.mainChild_r4);
      setRotationAngle(this.mainChild_r4, -0.1745F, 0.0F, 0.0F);
      this.mainChild_r4.cubeList.add(new ModelBox(this.mainChild_r4, 0, 0, -12.0F, -104.0F, -32.0F, 4, 22, 4, 0.0F, false));
      this.mainChildChild_r3 = new ModelRenderer(this);
      this.mainChildChild_r3.setRotationPoint(-8.0F, 72.0F, 52.0F);
      this.mainChild.addChild(this.mainChildChild_r3);
      setRotationAngle(this.mainChildChild_r3, -1.667F, -0.4346F, 1.4805F);
      this.mainChildChild_r3.cubeList.add(new ModelBox(this.mainChildChild_r3, 0, 0, -60.7172F, -13.018F, 2.0F, 4, 22, 4, 0.0F, false));
      this.mainChild_r5 = new ModelRenderer(this);
      this.mainChild_r5.setRotationPoint(-8.0F, 97.0F, 23.0F);
      this.mainChild.addChild(this.mainChild_r5);
      setRotationAngle(this.mainChild_r5, 0.0F, 0.0F, 0.0436F);
      this.mainChild_r5.cubeList.add(new ModelBox(this.mainChild_r5, 0, 0, -36.0F, -118.0F, -8.0F, 4, 19, 4, 0.0F, false));
      this.mainChildChild = new ModelRenderer(this);
      this.mainChildChild.setRotationPoint(-22.0F, -16.0F, 0.0F);
      this.mainChild.addChild(this.mainChildChild);
      setRotationAngle(this.mainChildChild, 0.0F, 0.0F, -0.7418F);
      this.mainChildChild.cubeList.add(new ModelBox(this.mainChildChild, 0, 0, -24.2685F, 0.1176F, 0.0F, 4, 22, 4, 0.0F, false));
      this.mainChildChild.cubeList.add(new ModelBox(this.mainChildChild, 0, 0, -20.8904F, -3.5687F, 15.0F, 4, 22, 4, 0.0F, false));
      this.mainChildChild_r4 = new ModelRenderer(this);
      this.mainChildChild_r4.setRotationPoint(14.0F, 88.0F, 8.0F);
      this.mainChildChild.addChild(this.mainChildChild_r4);
      setRotationAngle(this.mainChildChild_r4, 0.0F, 0.0F, 1.4399F);
      this.mainChildChild_r4.cubeList.add(new ModelBox(this.mainChildChild_r4, 0, 0, -50.6455F, -15.3162F, 11.0F, 4, 22, 4, 0.0F, false));
      this.mainChildChild_r4.cubeList.add(new ModelBox(this.mainChildChild_r4, 0, 0, -48.7172F, -13.018F, 2.0F, 4, 22, 4, 0.0F, false));
      this.mainChild1 = new ModelRenderer(this);
      this.mainChild1.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild1);
      setRotationAngle(this.mainChild1, -0.1745F, 0.0F, 0.0F);
      this.mainChild1.cubeList.add(new ModelBox(this.mainChild1, 0, 3, -12.0F, -22.2126F, -15.5699F, 24, 14, 2, 0.0F, false));
      this.mainChild5 = new ModelRenderer(this);
      this.mainChild5.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild5);
      setRotationAngle(this.mainChild5, -0.1745F, 0.0F, 0.0F);
      this.mainChild5.cubeList.add(new ModelBox(this.mainChild5, 0, 0, 6.0F, -6.2126F, -9.5699F, 2, 8, 2, 0.0F, false));
      this.mainChild7 = new ModelRenderer(this);
      this.mainChild7.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild7);
      setRotationAngle(this.mainChild7, -0.1745F, 0.0F, 0.0F);
      this.mainChild7.cubeList.add(new ModelBox(this.mainChild7, 0, 0, -12.0F, -6.2126F, -9.5699F, 2, 8, 2, 0.0F, false));
      this.mainChild3 = new ModelRenderer(this);
      this.mainChild3.setRotationPoint(0.0F, -2.0F, -2.0F);
      this.main.addChild(this.mainChild3);
      setRotationAngle(this.mainChild3, -0.1745F, 0.0F, 0.0F);
      this.mainChild3.cubeList.add(new ModelBox(this.mainChild3, 0, 0, 14.0F, -6.2126F, -9.5699F, 2, 8, 2, 0.0F, false));
      this.mainChild3_r1 = new ModelRenderer(this);
      this.mainChild3_r1.setRotationPoint(0.0F, 50.0F, 2.0F);
      this.mainChild3.addChild(this.mainChild3_r1);
      setRotationAngle(this.mainChild3_r1, 0.4363F, 0.0F, 0.0F);
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, -14.0F, -50.7795F, 9.4561F, 2, 8, 2, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, -10.0F, -50.7795F, 9.4561F, 2, 8, 2, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, -6.0F, -50.7795F, 9.4561F, 2, 8, 2, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, -2.0F, -50.7795F, 9.4561F, 2, 8, 2, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, 0.0F, -50.7795F, 9.4561F, 2, 8, 2, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, 4.0F, -50.7795F, 9.4561F, 2, 8, 2, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, 8.0F, -50.7795F, 9.4561F, 2, 8, 2, 0.0F, false));
      this.mainChild3_r1.cubeList.add(new ModelBox(this.mainChild3_r1, 0, 0, 12.0F, -50.7795F, 9.4561F, 2, 8, 2, 0.0F, false));
      this.bb_main = new ModelRenderer(this);
      this.bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
      this.bb_main.cubeList.add(new ModelBox(this.bb_main, 5, 3, -32.0F, -97.0F, 6.0F, 6, 6, 6, 0.0F, false));
      this.bb_main.cubeList.add(new ModelBox(this.bb_main, 5, 3, -32.0F, -100.0F, -9.0F, 6, 6, 6, 0.0F, false));
      this.bb_main.cubeList.add(new ModelBox(this.bb_main, 5, 3, -10.0F, -106.0F, -1.0F, 6, 6, 6, 0.0F, false));
      this.bb_main.cubeList.add(new ModelBox(this.bb_main, 5, 3, 7.0F, -105.0F, -1.0F, 6, 6, 6, 0.0F, false));
      this.bb_main.cubeList.add(new ModelBox(this.bb_main, 5, 3, 25.0F, -91.0F, 1.0F, 6, 6, 6, 0.0F, false));
      this.bb_main.cubeList.add(new ModelBox(this.bb_main, 5, 3, 25.0F, -95.0F, 10.0F, 6, 6, 6, 0.0F, false));
      this.bb_main.cubeList.add(new ModelBox(this.bb_main, 5, 3, 2.0F, -94.0F, 28.0F, 6, 6, 6, 0.0F, false));
      this.bb_main.cubeList.add(new ModelBox(this.bb_main, 5, 3, 8.0F, -92.0F, 29.0F, 6, 6, 6, 0.0F, false));
      this.bb_main.cubeList.add(new ModelBox(this.bb_main, 5, 3, -14.0F, -87.0F, 29.0F, 6, 6, 6, 0.0F, false));
      this.cube_r1 = new ModelRenderer(this);
      this.cube_r1.setRotationPoint(0.0F, 30.0F, 0.0F);
      this.bb_main.addChild(this.cube_r1);
      setRotationAngle(this.cube_r1, 0.0F, 0.0F, -0.2182F);
      this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 0, 0, 36.0F, -130.0F, 0.0F, 4, 20, 4, 0.0F, false));
      this.cube_r2 = new ModelRenderer(this);
      this.cube_r2.setRotationPoint(0.0F, 30.0F, 0.0F);
      this.bb_main.addChild(this.cube_r2);
      setRotationAngle(this.cube_r2, 4.0E-4F, 0.0057F, 0.3489F);
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, -30.0F, -114.0F, 0.0F, 4, 16, 4, 0.0F, false));
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 0, -54.0F, -120.0F, 0.0F, 4, 16, 4, 0.0F, false));
      this.cube_r3 = new ModelRenderer(this);
      this.cube_r3.setRotationPoint(0.0F, 30.0F, 0.0F);
      this.bb_main.addChild(this.cube_r3);
      setRotationAngle(this.cube_r3, 0.0F, 0.0F, -0.1745F);
      this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 0, 0, 6.0F, -118.0F, 0.0F, 4, 24, 4, 0.0F, false));
      this.cube_r4 = new ModelRenderer(this);
      this.cube_r4.setRotationPoint(0.0F, -20.0F, -2.0F);
      this.bb_main.addChild(this.cube_r4);
      setRotationAngle(this.cube_r4, 0.1309F, 0.0F, 0.0F);
      this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 0, 0, -16.0F, -28.0F, -10.0F, 32, 6, 32, 0.0F, false));
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
      this.mainChild6.rotateAngleY = f3 / 57.295776F;
      this.mainChild6.rotateAngleX = f4 / 57.295776F;
      this.mainChild_r4.rotateAngleY = f3 / 57.295776F;
      this.mainChild_r4.rotateAngleX = f4 / 57.295776F;
      this.cube_r4.rotateAngleY = f3 / 57.295776F;
      this.cube_r4.rotateAngleX = f4 / 57.295776F;
      this.mainChild_r5.rotateAngleY = f3 / 57.295776F;
      this.mainChild_r5.rotateAngleX = f4 / 57.295776F;
      this.mainChild5.rotateAngleY = f3 / 57.295776F;
      this.mainChild5.rotateAngleX = f4 / 57.295776F;
      this.main.rotateAngleY = f3 / 57.295776F;
      this.main.rotateAngleX = f4 / 57.295776F;
      this.mainChild4.rotateAngleY = f3 / 57.295776F;
      this.mainChild4.rotateAngleX = f4 / 57.295776F;
      this.mainChild_r2.rotateAngleY = f3 / 57.295776F;
      this.mainChild_r2.rotateAngleX = f4 / 57.295776F;
      this.mainChild_r3.rotateAngleY = f3 / 57.295776F;
      this.mainChild_r3.rotateAngleX = f4 / 57.295776F;
      this.mainChild3.rotateAngleY = f3 / 57.295776F;
      this.mainChild3.rotateAngleX = f4 / 57.295776F;
      this.mainChild9.rotateAngleY = f3 / 57.295776F;
      this.mainChild9.rotateAngleX = f4 / 57.295776F;
      this.mainChild_r1.rotateAngleY = f3 / 57.295776F;
      this.mainChild_r1.rotateAngleX = f4 / 57.295776F;
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
      this.mainChild.rotateAngleY = f3 / 57.295776F;
      this.mainChild.rotateAngleX = f4 / 57.295776F;
      this.mainChildChild_r2.rotateAngleY = f3 / 57.295776F;
      this.mainChildChild_r2.rotateAngleX = f4 / 57.295776F;
      this.mainChildChild_r3.rotateAngleY = f3 / 57.295776F;
      this.mainChildChild_r3.rotateAngleX = f4 / 57.295776F;
      this.mainChildChild_r1.rotateAngleY = f3 / 57.295776F;
      this.mainChildChild_r1.rotateAngleX = f4 / 57.295776F;
      this.mainChildChild_r4.rotateAngleY = f3 / 57.295776F;
      this.mainChildChild_r4.rotateAngleX = f4 / 57.295776F;
      this.mainChild10.rotateAngleY = f3 / 57.295776F;
      this.mainChild10.rotateAngleX = f4 / 57.295776F;
    }
  }
}

