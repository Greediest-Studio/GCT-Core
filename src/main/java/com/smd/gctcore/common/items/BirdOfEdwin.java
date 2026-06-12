package com.smd.gctcore.common.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.smd.gctcore.Tags;
import com.smd.gctcore.misc.SoundRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class BirdOfEdwin extends Item implements IBauble {

    public BirdOfEdwin() {
        setRegistryName("bird_of_edwin");
        setTranslationKey(Tags.MOD_ID + ".bird_of_edwin");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
            player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 600, 1));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.AQUA + "" + TextFormatting.BOLD + I18n.format("tooltip.gctcore.bird_of_edwin.1"));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.gctcore.bird_of_edwin.2"));
    }

    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.HEAD;
    }

    @Override
    @Optional.Method(modid = "baubles")
    public void onWornTick(ItemStack itemstack, EntityLivingBase entity) {
        if (entity.world.isRemote || !(entity instanceof EntityPlayer)) {
            return;
        }
        if (entity.world.getTotalWorldTime() % 200L == 0L && entity.world.rand.nextFloat() < 0.3F) {
            entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundRegistry.BIRD,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }
}
