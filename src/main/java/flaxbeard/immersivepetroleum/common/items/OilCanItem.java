package flaxbeard.immersivepetroleum.common.items;

import java.util.List;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

public class OilCanItem extends IPItemBase{
	public OilCanItem(String name){
		super(name, new Item.Properties().maxStackSize(1));
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		if(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY == null)
			return;
		
		FluidUtil.getFluidContained(stack).ifPresent(fluid -> {
			if(fluid != null && fluid.getAmount() > 0){
				FluidAttributes att = fluid.getFluid().getAttributes();
				TextFormatting rarity = att.getRarity() == Rarity.COMMON ? TextFormatting.GRAY : att.getRarity().color;
				
				ITextComponent out = ((IFormattableTextComponent) fluid.getDisplayName()).mergeStyle(rarity)
						.appendSibling(new StringTextComponent(": " + fluid.getAmount() + "/8000mB").mergeStyle(TextFormatting.GRAY));
				tooltip.add(out);
			}else{
				tooltip.add(new StringTextComponent(I18n.format(Lib.DESC_FLAVOUR + "drill.empty")));
			}
		});
	}
	
	@Override
	public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand){
		if(target instanceof IronGolemEntity){
			IronGolemEntity golem = (IronGolemEntity) target;
			
			FluidUtil.getFluidHandler(stack).ifPresent(con -> {
				if(con instanceof FluidHandlerItemStack){
					FluidHandlerItemStack handler = (FluidHandlerItemStack) con;
					
					if(handler.getFluid() != null && LubricantHandler.isValidLube(handler.getFluid().getFluid())){
						int amountNeeded = (LubricantHandler.getLubeAmount(handler.getFluid().getFluid()) * 5 * 20);
						if(handler.getFluid().getAmount() >= amountNeeded){
							player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1f, 1f);
							golem.setHealth(Math.max(golem.getHealth() + 2f, golem.getMaxHealth()));
							golem.addPotionEffect(new EffectInstance(Effects.SPEED, 60 * 20, 1));
							golem.addPotionEffect(new EffectInstance(Effects.STRENGTH, 60 * 20, 1));
							if(!player.isCreative()){
								handler.drain(amountNeeded, FluidAction.EXECUTE);
							}
						}
					}
				}
			});
			
			return ActionResultType.SUCCESS;
		}else
			return ActionResultType.FAIL;
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker){
		this.itemInteractionForEntity(stack, (PlayerEntity) null, target, Hand.MAIN_HAND);
		return true;
	}
	
	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context){
		PlayerEntity player = context.getPlayer();
		Hand hand = context.getHand();
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		
		ActionResultType ret = ActionResultType.PASS;
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity != null){
			IFluidHandler cap = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
			
			if(cap != null && FluidUtil.interactWithFluidHandler(player, hand, cap)){
				ret = ActionResultType.SUCCESS;
			}else{
				ret = ActionResultType.FAIL;
			}
		}else{
			FluidUtil.getFluidHandler(stack).ifPresent(con -> {
				if(con instanceof FluidHandlerItemStack){
					FluidHandlerItemStack handler = (FluidHandlerItemStack) con;
					
					if(handler.getFluid() != null && LubricantHandler.isValidLube(handler.getFluid().getFluid())){
						int amountNeeded = (LubricantHandler.getLubeAmount(handler.getFluid().getFluid()) * 5 * 20);
						if(handler.getFluid().getAmount() >= amountNeeded && LubricatedHandler.lubricateTile(world.getTileEntity(pos), 20 * 30)){
							player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1f, 1f);
							if(!player.isCreative()){
								handler.drain(amountNeeded, FluidAction.EXECUTE);
							}
						}
					}
				}
			});
		}
		return ret;
	}
	
	@Override
	public boolean hasContainerItem(ItemStack stack){
		return ItemNBTHelper.hasKey(stack, "jerrycanDrain") || FluidUtil.getFluidContained(stack).isPresent();
	}
	
	@Override
	public ItemStack getContainerItem(ItemStack stack){
		if(ItemNBTHelper.hasKey(stack, "jerrycanDrain")){
			ItemStack ret = stack.copy();
			FluidUtil.getFluidHandler(ret).ifPresent(handler -> {
				handler.drain(ItemNBTHelper.getInt(ret, "jerrycanDrain"), FluidAction.EXECUTE);
				ItemNBTHelper.remove(ret, "jerrycanDrain");
			});
			return ret;
		}else if(FluidUtil.getFluidContained(stack) != null){
			ItemStack ret = stack.copy();
			FluidUtil.getFluidHandler(ret).ifPresent(handler -> handler.drain(1000, FluidAction.EXECUTE));
			return ret;
		}
		return stack;
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt){
		return new FluidHandlerItemStack(stack, 8000);
	}
}
