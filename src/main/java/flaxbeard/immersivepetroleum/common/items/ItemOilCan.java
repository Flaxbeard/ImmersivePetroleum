package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemOilCan extends ItemIPBase
{

	public ItemOilCan(String name)
	{
		super(name, 1, new String[0]);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		FluidStack fs = FluidUtil.getFluidContained(stack);
		if (fs != null)
		{
			TextFormatting rarity = fs.getFluid().getRarity() == EnumRarity.COMMON ? TextFormatting.GRAY : fs.getFluid().getRarity().color;
			tooltip.add(rarity + fs.getLocalizedName() + TextFormatting.GRAY + ": " + fs.amount + "/" + 8000 + "mB");
		}
		else
			tooltip.add(I18n.format(Lib.DESC_FLAVOUR + "drill.empty"));
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand)
	{
		if (target instanceof EntityIronGolem)
		{
			EntityIronGolem golem = (EntityIronGolem) target;

			FluidHandlerItemStack handler = (FluidHandlerItemStack) FluidUtil.getFluidHandler(stack);
			if (handler.getFluid() != null && LubricantHandler.isValidLube(handler.getFluid().getFluid()))
			{
				int amountNeeded = (LubricantHandler.getLubeAmount(handler.getFluid().getFluid()) * 5 * 20);
				if (handler.getFluid().amount >= amountNeeded)
				{
					player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1f, 1f);
					golem.setHealth(Math.max(golem.getHealth() + 2f, golem.getMaxHealth()));
					golem.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 1));
					golem.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 60 * 20, 1));
					if (!player.isCreative())
					{
						handler.drain(amountNeeded, true);
					}
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
	{
		this.itemInteractionForEntity(stack, (EntityPlayer) null, target, EnumHand.MAIN_HAND);
		return true;
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		EnumActionResult ret = EnumActionResult.PASS;
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
		{
			if (FluidUtil.interactWithFluidHandler(player, hand, tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)))
			{
				ret = EnumActionResult.SUCCESS;
			}
			else
				ret = EnumActionResult.FAIL;
		}
		else
		{
			FluidHandlerItemStack handler = (FluidHandlerItemStack) FluidUtil.getFluidHandler(stack);
			if (handler.getFluid() != null && LubricantHandler.isValidLube(handler.getFluid().getFluid()))
			{
				int amountNeeded = (LubricantHandler.getLubeAmount(handler.getFluid().getFluid()) * 5 * 20);
				if (handler.getFluid().amount >= amountNeeded && LubricatedHandler.lubricateTile(world.getTileEntity(pos), 20 * 30))
				{
					player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1f, 1f);
					if (!player.isCreative())
					{
						handler.drain(amountNeeded, true);
					}
				}
			}
		}
		return ret;
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return ItemNBTHelper.hasKey(stack, "jerrycanDrain") || FluidUtil.getFluidContained(stack) != null;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if (ItemNBTHelper.hasKey(stack, "jerrycanDrain"))
		{
			ItemStack ret = stack.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret);
			handler.drain(ItemNBTHelper.getInt(ret, "jerrycanDrain"), true);
			ItemNBTHelper.remove(ret, "jerrycanDrain");
			return ret;
		}
		else if (FluidUtil.getFluidContained(stack) != null)
		{
			ItemStack ret = stack.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret);
			handler.drain(1000, true);
			return ret;
		}
		return stack;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		return new FluidHandlerItemStack(stack, 8000)
		{
			@Override
			public boolean canFillFluidType(FluidStack fluid)
			{
				return fluid != null && LubricantHandler.isValidLube(fluid.getFluid());
			}
		};
	}

	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}
}
