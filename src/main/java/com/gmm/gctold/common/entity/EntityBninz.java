package com.gmm.gctold.common.entity;

import com.gmm.gctold.misc.registry.GctAllItems;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityBninz {


  private Biome[] allbiomes(RegistryNamespaced<ResourceLocation, Biome> in) {
    Iterator<Biome> itr = in.iterator();
    ArrayList<Biome> ls = new ArrayList<>();
    while (itr.hasNext())
      ls.add(itr.next());
    return ls.<Biome>toArray(new Biome[ls.size()]);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(BninzEntity.class, renderManager -> new RenderLiving(renderManager, new ModelBligtz(), 0.5F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/bninz.png");
          }
        });
    RenderingRegistry.registerEntityRenderingHandler(BninzProjectileEntity.class, renderManager -> new RenderSnowball<BninzProjectileEntity>(renderManager, null, Minecraft.getMinecraft().getRenderItem()) {
          public ItemStack getStackToRender(EntityBninz.BninzProjectileEntity entity) {
            return new ItemStack(GctAllItems.STORM_BNINZ_DUST);
          }
        });
  }

  public static class BninzEntity extends EntityMob implements IRangedAttackMob {
    public BninzEntity(World world) {
      super(world);
      setSize(0.6F, 1.8F);
      this.experienceValue = 0;
      this.isImmuneToFire = false;
      setNoAI(false);
      enablePersistence();
      this.navigator = (PathNavigate)new PathNavigateFlying((EntityLiving)this, this.world);
      this.moveHelper = (EntityMoveHelper)new EntityFlyHelper((EntityLiving)this);
    }

    protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, new EntityAIBase() {
            public boolean shouldExecute() {
              if (EntityBninz.BninzEntity.this.getAttackTarget() != null && !EntityBninz.BninzEntity.this.getMoveHelper().isUpdating())
                return true;
              return false;
            }

            public boolean shouldContinueExecuting() {
              return (EntityBninz.BninzEntity.this.getMoveHelper().isUpdating() && EntityBninz.BninzEntity.this.getAttackTarget() != null && EntityBninz.BninzEntity.this
                .getAttackTarget().isEntityAlive());
            }

            public void startExecuting() {
              EntityLivingBase livingentity = EntityBninz.BninzEntity.this.getAttackTarget();
              Vec3d vec3d = livingentity.getPositionEyes(1.0F);
              EntityBninz.BninzEntity.this.moveHelper.setMoveTo(vec3d.x, vec3d.y, vec3d.z, 1.0D);
            }

            public void updateTask() {
              EntityLivingBase livingentity = EntityBninz.BninzEntity.this.getAttackTarget();
              double d0 = EntityBninz.BninzEntity.this.getDistanceSq((Entity)livingentity);
              if (d0 <= getAttackReachSq(livingentity)) {
                EntityBninz.BninzEntity.this.attackEntityAsMob((Entity)livingentity);
              } else if (d0 < 16.0D) {
                Vec3d vec3d = livingentity.getPositionEyes(1.0F);
                EntityBninz.BninzEntity.this.moveHelper.setMoveTo(vec3d.x, vec3d.y, vec3d.z, 1.0D);
              }
            }

            protected double getAttackReachSq(EntityLivingBase attackTarget) {
              return EntityBninz.BninzEntity.this.width * 1.5D * EntityBninz.BninzEntity.this.height * 1.5D + attackTarget.height;
            }
          });
      this.tasks.addTask(2, (EntityAIBase)new EntityAIWander((EntityCreature)this, 0.8D, 20) {
            protected Vec3d getPosition() {
              Random random = EntityBninz.BninzEntity.this.getRNG();
              double dir_x = EntityBninz.BninzEntity.this.posX + ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
              double dir_y = EntityBninz.BninzEntity.this.posY + ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
              double dir_z = EntityBninz.BninzEntity.this.posZ + ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
              return new Vec3d(dir_x, dir_y, dir_z);
            }
          });
      this.tasks.addTask(3, (EntityAIBase)new EntityAIAttackMelee((EntityCreature)this, 1.2D, false));
      this.tasks.addTask(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
      this.targetTasks.addTask(5, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityPlayer.class, false, false));
      this.targetTasks.addTask(6, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
      this.tasks.addTask(1, (EntityAIBase)new EntityAIAttackRanged(this, 1.25D, 20, 10.0F));
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
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("gctcore:bninz_br"));
    }

    public SoundEvent getHurtSound(DamageSource ds) {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("gctcore:bninz_ht"));
    }

    public SoundEvent getDeathSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("gctcore:bninz_de"));
    }

    protected float getSoundVolume() {
      return 1.0F;
    }

    public void fall(float l, float d) {}

    public boolean attackEntityFrom(DamageSource source, float amount) {
      if (source == DamageSource.FALL)
        return false;
      return super.attackEntityFrom(source, amount);
    }
    

    @Nullable
    protected ResourceLocation getLootTable() {
      return new ResourceLocation("gctold:entities/bninz");
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
      getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
      getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.3D);
    }

    public void setSwingingArms(boolean swingingArms) {}

    public void attackEntityWithRangedAttack(EntityLivingBase target, float flval) {
      EntityBninz.BninzProjectileEntity entityarrow = new EntityBninz.BninzProjectileEntity(this.world, (EntityLivingBase)this);
      double d0 = target.posY + target.getEyeHeight() - 1.1D;
      double d1 = target.posX - this.posX;
      double d3 = target.posZ - this.posZ;
      entityarrow.shoot(d1, d0 - entityarrow.posY + MathHelper.sqrt(d1 * d1 + d3 * d3) * 0.20000000298023224D, d3, 1.6F, 12.0F);
      this.world.spawnEntity((Entity)entityarrow);
    }

    public void onUpdate() {
      super.onUpdate();
      setNoGravity(true);
    }

    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {}

    public void setNoGravity(boolean ignored) {
      super.setNoGravity(true);
    }
  }

  public static class BninzProjectileEntity extends EntityTippedArrow {
    public BninzProjectileEntity(World a) {
      super(a);
    }

    public BninzProjectileEntity(World worldIn, double x, double y, double z) {
      super(worldIn, x, y, z);
    }

    public BninzProjectileEntity(World worldIn, EntityLivingBase shooter) {
      super(worldIn, shooter);
    }
  }

  public static class ModelBligtz extends ModelBase {
    public ModelRenderer spike11;

    public ModelRenderer spike10;

    public ModelRenderer spike1;

    public ModelRenderer spike2;

    public ModelRenderer spike12;

    public ModelRenderer blazeHead;

    public ModelRenderer spike7;

    public ModelRenderer spike8;

    public ModelRenderer spike9;

    public ModelRenderer spike3;

    public ModelRenderer spike4;

    public ModelRenderer spike5;

    public ModelRenderer spike6;

    public ModelBligtz() {
      this.textureWidth = 64;
      this.textureHeight = 32;
      this.spike6 = new ModelRenderer(this, 0, 16);
      this.spike6.setRotationPoint(-4.9497476F, 1.1989107F, 4.9497476F);
      this.spike6.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike2 = new ModelRenderer(this, 0, 16);
      this.spike2.setRotationPoint(1.1021821E-15F, -1.1224087F, 9.0F);
      this.spike2.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike1 = new ModelRenderer(this, 0, 16);
      this.spike1.setRotationPoint(9.0F, -1.0F, 0.0F);
      this.spike1.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike8 = new ModelRenderer(this, 0, 16);
      this.spike8.setRotationPoint(4.9497476F, 1.0635324F, -4.9497476F);
      this.spike8.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike4 = new ModelRenderer(this, 0, 16);
      this.spike4.setRotationPoint(0.0F, -1.9292085F, -9.0F);
      this.spike4.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike12 = new ModelRenderer(this, 0, 16);
      this.spike12.setRotationPoint(2.269867F, 10.614307F, -4.455076F);
      this.spike12.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.blazeHead = new ModelRenderer(this, 0, 0);
      this.blazeHead.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.blazeHead.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, 0.0F);
      this.spike9 = new ModelRenderer(this, 0, 16);
      this.spike9.setRotationPoint(4.455076F, 11.960163F, 2.269867F);
      this.spike9.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike3 = new ModelRenderer(this, 0, 16);
      this.spike3.setRotationPoint(-9.0F, -1.4596672F, 1.1021821E-15F);
      this.spike3.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike5 = new ModelRenderer(this, 0, 16);
      this.spike5.setRotationPoint(4.9497476F, 1.5839192F, 4.9497476F);
      this.spike5.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike7 = new ModelRenderer(this, 0, 16);
      this.spike7.setRotationPoint(-4.9497476F, 1.0100093F, -4.9497476F);
      this.spike7.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike11 = new ModelRenderer(this, 0, 16);
      this.spike11.setRotationPoint(-4.455076F, 11.34671F, -2.269867F);
      this.spike11.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
      this.spike10 = new ModelRenderer(this, 0, 16);
      this.spike10.setRotationPoint(-2.269867F, 11.893008F, 4.455076F);
      this.spike10.addBox(0.0F, 0.0F, 0.0F, 2, 8, 2, 0.0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.spike6.render(f5);
      this.spike2.render(f5);
      this.spike1.render(f5);
      this.spike8.render(f5);
      this.spike4.render(f5);
      this.spike12.render(f5);
      this.blazeHead.render(f5);
      this.spike9.render(f5);
      this.spike3.render(f5);
      this.spike5.render(f5);
      this.spike7.render(f5);
      this.spike11.render(f5);
      this.spike10.render(f5);
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
      this.spike11.rotateAngleY = f2;
      this.spike10.rotateAngleY = f2;
      this.spike1.rotateAngleY = f2;
      this.spike2.rotateAngleY = f2;
      this.spike12.rotateAngleY = f2;
      this.blazeHead.rotateAngleY = f3 / 57.295776F;
      this.blazeHead.rotateAngleX = f4 / 57.295776F;
      this.spike7.rotateAngleY = f2;
      this.spike8.rotateAngleY = f2;
      this.spike9.rotateAngleY = f2;
      this.spike3.rotateAngleY = f2;
      this.spike4.rotateAngleY = f2;
      this.spike5.rotateAngleY = f2;
      this.spike6.rotateAngleY = f2;
    }
  }
}


