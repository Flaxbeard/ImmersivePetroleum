package flaxbeard.immersivepetroleum.common.items;

import java.util.List;
import java.util.function.Supplier;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.gui.IESlot;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.util.IPItemStackHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MotorboatItem extends IPItemBase implements IUpgradeableTool{
	public static final String UPGRADE_TYPE = "MOTORBOAT";
	
	public MotorboatItem(String name){
		super(name, new Item.Properties().maxStackSize(1).group(ImmersivePetroleum.creativeTab));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt){
		return new IPItemStackHandler(4);
	}
	
	@Override
	public CompoundNBT getUpgrades(ItemStack stack){
		return new CompoundNBT();
	}
	
	@Override
	public void clearUpgrades(ItemStack stack){
	}
	
	@Override
	public boolean canTakeFromWorkbench(ItemStack stack){
		return true;
	}
	
	@Override
	public boolean canModify(ItemStack stack){
		return true;
	}
	
	@Override
	public void recalculateUpgrades(ItemStack stack, World w, PlayerEntity player){
		if(w.isRemote){
			return;
		}
		
		clearUpgrades(stack);
		
		LazyOptional<IItemHandler> lazy = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		lazy.ifPresent(handler -> {
			CompoundNBT nbt = new CompoundNBT();
			
			for(int i = 0;i < handler.getSlots();i++){
				ItemStack u = handler.getStackInSlot(i);
				if(!u.isEmpty() && u.getItem() instanceof IUpgrade){
					IUpgrade upg = (IUpgrade) u.getItem();
					if(upg.getUpgradeTypes(u).contains(UPGRADE_TYPE) && upg.canApplyUpgrades(stack, u)){
						upg.applyUpgrades(stack, u, nbt);
					}
				}
			}
			
			stack.getOrCreateTag().put("upgrades", nbt);
			finishUpgradeRecalculation(stack);
		});
	}
	
	@Override
	public void removeFromWorkbench(PlayerEntity player, ItemStack stack){
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		float f1 = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch) * 1.0F;
		float f2 = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw) * 1.0F;
		double d0 = playerIn.prevPosX + (playerIn.getPosX() - playerIn.prevPosX) * 1.0D;
		double d1 = playerIn.prevPosY + (playerIn.getPosY() - playerIn.prevPosY) * 1.0D + (double) playerIn.getEyeHeight();
		double d2 = playerIn.prevPosZ + (playerIn.getPosZ() - playerIn.prevPosZ) * 1.0D;
		Vector3d vec3d = new Vector3d(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		
		Vector3d vec3d1 = vec3d.add((double) f7 * 5.0D, (double) f6 * 5.0D, (double) f8 * 5.0D);
		RayTraceResult raytraceresult = worldIn.rayTraceBlocks(new RayTraceContext(vec3d, vec3d1, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, playerIn));
		
		if(raytraceresult != null){
			Vector3d vec3d2 = playerIn.getLook(1.0F);
			boolean flag = false;
			AxisAlignedBB bb = playerIn.getBoundingBox();
			if(bb == null)
				bb = playerIn.getBoundingBox();
			
			if(bb != null){
				List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(playerIn, bb.expand(vec3d2.x * 5.0D, vec3d2.y * 5.0D, vec3d2.z * 5.0D).grow(1.0D));
				for(int i = 0;i < list.size();++i){
					Entity entity = (Entity) list.get(i);
					
					if(entity.canBeCollidedWith()){
						AxisAlignedBB axisalignedbb = entity.getBoundingBox();
						if(axisalignedbb != null && axisalignedbb.grow((double) entity.getCollisionBorderSize()).contains(vec3d)){
							flag = true;
						}
					}
				}
			}
			
			if(flag){
				return new ActionResult<ItemStack>(ActionResultType.PASS, itemstack);
			}else if(raytraceresult.getType() != RayTraceResult.Type.BLOCK){
				return new ActionResult<ItemStack>(ActionResultType.PASS, itemstack);
			}else{
				Vector3d hit = raytraceresult.getHitVec();
				Block block = worldIn.getBlockState(new BlockPos(hit.add(0, .5, 0))).getBlock();
				boolean flag1 = block == Blocks.WATER;
				MotorboatEntity entityboat = new MotorboatEntity(worldIn, hit.x, flag1 ? hit.y - 0.12D : hit.y, hit.z);
				{
					entityboat.rotationYaw = playerIn.rotationYaw;
					entityboat.setUpgrades(getContainedItems(itemstack));
					entityboat.readTank(itemstack.getTag());
				}
				
				if(worldIn.getBlockCollisionShapes(entityboat, entityboat.getBoundingBox().grow(-0.1D)).findFirst().isPresent()){
					return new ActionResult<ItemStack>(ActionResultType.FAIL, itemstack);
				}else{
					if(!worldIn.isRemote){
						worldIn.addEntity(entityboat);
					}
					
					if(!playerIn.isCreative()){
						itemstack.shrink(1);
					}
					
					// playerIn.addStat(net.minecraft.stats.Stats.CUSTOM.get(getRegistryName()));
					return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemstack);
				}
			}
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, itemstack);
	}
	
	protected NonNullList<ItemStack> getContainedItems(ItemStack stack){
		IItemHandler handler = (IItemHandler) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
		
		if(handler == null){
			ImmersivePetroleum.log.info("No valid inventory handler found for " + stack);
			return NonNullList.create();
		}
		
		if(handler instanceof IPItemStackHandler){
			return ((IPItemStackHandler) handler).getContainedItems();
		}
		
		ImmersivePetroleum.log.warn("Inefficiently getting contained items. Why does " + stack + " have a non-IE IItemHandler?");
		NonNullList<ItemStack> inv = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
		for(int i = 0;i < handler.getSlots();++i){
			inv.set(i, handler.getStackInSlot(i));
		}
		
		return inv;
	}
	
	@Override
	public void finishUpgradeRecalculation(ItemStack stack){
	}
	
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, World world, Supplier<PlayerEntity> getPlayer, IItemHandler inv){
		if(inv != null){
			return new Slot[]{
					new IESlot.Upgrades(container, inv, 0, 78, 35 - 5, UPGRADE_TYPE, stack, true, world, getPlayer),
					new IESlot.Upgrades(container, inv, 1, 98, 35 + 5, UPGRADE_TYPE, stack, true, world, getPlayer),
					new IESlot.Upgrades(container, inv, 2, 118, 35 - 5, UPGRADE_TYPE, stack, true, world, getPlayer)
			};
		}else{
			return new Slot[0];
		}
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack){
		boolean hasUpgrades = getContainedItems(stack).stream().anyMatch(s -> s != ItemStack.EMPTY);
		
		ITextComponent c = super.getDisplayName(stack);
		if(hasUpgrades){
			c = new TranslationTextComponent("desc.immersivepetroleum.flavour.speedboat.prefix").appendSibling(c).mergeStyle(TextFormatting.GOLD);
		}
		return c;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		if(stack.hasTag()){
			CompoundNBT tag = stack.getTag();
			
			if(tag.contains("tank")){
				FluidStack fs = FluidStack.loadFluidStackFromNBT(tag.getCompound("tank"));
				if(fs != null){
					tooltip.add(((IFormattableTextComponent) fs.getDisplayName()).appendString(": " + fs.getAmount() + "mB").mergeStyle(TextFormatting.GRAY));
				}
			}
			
			NonNullList<ItemStack> items = getContainedItems(stack);
			if(items != null && !items.isEmpty()){
				for(int i = 0;i < items.size();i++){
					ItemStack upgrade = items.get(i);
					if(upgrade != ItemStack.EMPTY){
						tooltip.add(new TranslationTextComponent("desc.immersivepetroleum.flavour.speedboat.upgrade", i + 1).appendSibling(upgrade.getDisplayName()));
					}
				}
			}
		}
		
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
}
