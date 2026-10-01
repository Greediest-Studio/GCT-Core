package com.gmm.gctold.common.entity;

import com.gmm.gctold.misc.registry.GctAllItems;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityShadowBase {


  public static void init(FMLInitializationEvent event) {
    Biome[] spawnBiomes = { (Biome)Biome.REGISTRY.getObject(new ResourceLocation("gctcore:darkerrealm")) };
    EntityRegistry.addSpawn(ShadowBaseEntity.class, 10, 1, 1, EnumCreatureType.MONSTER, spawnBiomes);
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenderers(FMLPreInitializationEvent event) {
    RenderingRegistry.registerEntityRenderingHandler(ShadowBaseEntity.class, renderManager -> new RenderLiving(renderManager, new ModelShadowbaseModel(), 0.5F) {
          protected ResourceLocation getEntityTexture(Entity entity) {
            return new ResourceLocation("gctold:textures/modelshadowbase.png");
          }
        });
  }

  public static class ShadowBaseEntity extends EntityCreature {
    public ShadowBaseEntity(World world) {
      super(world);
      setSize(0.6F, 1.8F);
      this.experienceValue = 30;
      this.isImmuneToFire = false;
      setNoAI(true);
    }

    public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEFINED;
    }

    protected Item getDropItem() {
      return (new ItemStack(GctAllItems.SHADOW_NUCLEAR, 1)).getItem();
    }

    public SoundEvent getAmbientSound() {
      return null;
    }

    public SoundEvent getHurtSound(DamageSource ds) {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.shulker.hurt"));
    }

    public SoundEvent getDeathSound() {
      return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.shulker.death"));
    }

    protected float getSoundVolume() {
      return 1.0F;
    }

    public void onDeath(DamageSource source) {
      super.onDeath(source);
      if (!this.world.isRemote) {
        spawnAbyssalcraftShadow();
      }
    }

    public void onEntityUpdate() {
      super.onEntityUpdate();
      if (!this.world.isRemote) {
        clearNearbyShadowBasePotions();
      }
    }

    private void spawnAbyssalcraftShadow() {
      String entityId;
      double roll = this.rand.nextDouble();
      if (roll < 0.7D) {
        entityId = "abyssalcraft:shadowcreature";
      } else if (roll < 0.91D) {
        entityId = "abyssalcraft:shadowmonster";
      } else {
        entityId = "abyssalcraft:shadowbeast";
      }

      Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityId), this.world);
      if (entity != null) {
        entity.setLocationAndAngles(this.posX, this.posY + 1.0D, this.posZ, this.rotationYaw, this.rotationPitch);
        this.world.spawnEntity(entity);
      }
    }

    private void clearNearbyShadowBasePotions() {
      AxisAlignedBB area = new AxisAlignedBB(getPosition()).grow(64.0D);
      for (ShadowBaseEntity entity : this.world.getEntitiesWithinAABB(ShadowBaseEntity.class, area,
          entity -> entity.getDistanceSq(this) <= 4096.0D)) {
        entity.clearActivePotions();
      }
    }

    protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
      if (getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
      if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(150.0D);
      if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
    }
  }

  public static class ModelShadowbaseModel extends ModelBase {
    private final ModelRenderer bone;

    public ModelShadowbaseModel() {
      this.textureWidth = 128;
      this.textureHeight = 128;
      this.bone = new ModelRenderer(this);
      this.bone.setRotationPoint(0.0F, 24.0F, 0.0F);
      this.bone.cubeList.add(new ModelBox(this.bone, 0, 32, -6.0F, -2.0F, -6.0F, 12, 1, 12, 0.0F, false));
      this.bone.cubeList.add(new ModelBox(this.bone, 0, 32, -6.0F, -21.0F, -6.0F, 12, 1, 12, 0.0F, false));
      this.bone.cubeList.add(new ModelBox(this.bone, 0, 45, -5.0F, -1.0F, -5.0F, 10, 1, 10, 0.0F, false));
      this.bone.cubeList.add(new ModelBox(this.bone, 0, 0, -7.0F, -20.0F, -7.0F, 14, 18, 14, 0.0F, false));
      this.bone.cubeList.add(new ModelBox(this.bone, 0, 45, -5.0F, -22.0F, -5.0F, 10, 1, 10, 0.0F, false));
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.bone.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
      super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
    }
  }
}

