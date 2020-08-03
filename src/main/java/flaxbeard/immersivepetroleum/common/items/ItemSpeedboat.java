package flaxbeard.immersivepetroleum.common.items;


import java.util.List;
import java.util.function.Supplier;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemSpeedboat extends IPItemBase implements IUpgradeableTool{
	String upgradeType;
	public ItemSpeedboat(String name){
		super(name, new Item.Properties().maxStackSize(1).group(ImmersivePetroleum.creativeTab));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		float f1 = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch) * 1.0F;
		float f2 = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw) * 1.0F;
		double d0 = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX) * 1.0D;
		double d1 = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) * 1.0D + (double) playerIn.getEyeHeight();
		double d2 = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ) * 1.0D;
		Vec3d vec3d = new Vec3d(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		Vec3d vec3d1 = vec3d.add((double) f7 * 5.0D, (double) f6 * 5.0D, (double) f8 * 5.0D);
		RayTraceResult raytraceresult = worldIn.rayTraceBlocks(new RayTraceContext(vec3d, vec3d1, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, playerIn));
		
		if(raytraceresult == null){
			return new ActionResult<ItemStack>(ActionResultType.PASS, itemstack);
		}else{
			Vec3d vec3d2 = playerIn.getLook(1.0F);
			boolean flag = false;
			List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(playerIn, playerIn.getCollisionBoundingBox().expand(vec3d2.x * 5.0D, vec3d2.y * 5.0D, vec3d2.z * 5.0D).grow(1.0D));
			
			for(int i = 0;i < list.size();++i){
				Entity entity = (Entity) list.get(i);
				
				if(entity.canBeCollidedWith()){
					AxisAlignedBB axisalignedbb = entity.getCollisionBoundingBox().grow((double) entity.getCollisionBorderSize());
					
					if(axisalignedbb.contains(vec3d)){
						flag = true;
					}
				}
			}
			
			if(flag){
				return new ActionResult<ItemStack>(ActionResultType.PASS, itemstack);
			}else if(raytraceresult.getType() != RayTraceResult.Type.BLOCK){
				return new ActionResult<ItemStack>(ActionResultType.PASS, itemstack);
			}else{
				Block block = worldIn.getBlockState(new BlockPos(raytraceresult.getHitVec())).getBlock();
				boolean flag1 = block == Blocks.WATER;
				SpeedboatEntity entityboat = new SpeedboatEntity(worldIn, raytraceresult.getHitVec().x, flag1 ? raytraceresult.getHitVec().y - 0.12D : raytraceresult.getHitVec().y, raytraceresult.getHitVec().z);
				{
					NonNullList<ItemStack> items=NonNullList.create();
					items.add(getContainerItem(itemstack));
					entityboat.rotationYaw = playerIn.rotationYaw;
					entityboat.setUpgrades(items);
					entityboat.readTank(itemstack.getTag());
				}
				
				if(worldIn.getCollisionShapes(entityboat, entityboat.getCollisionBoundingBox().grow(-0.1D))!=null){
					return new ActionResult<ItemStack>(ActionResultType.FAIL, itemstack);
				}else{
					if(!worldIn.isRemote){
						worldIn.addEntity(entityboat);
					}
					
					if(!playerIn.isCreative()){
						itemstack.shrink(1);
					}
					
					//playerIn.addStat(StatList.getObjectUseStats(this));
					return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemstack);
				}
			}
		}
	}
	
	@Override
	public CompoundNBT getUpgrades(ItemStack stack){
		return ItemNBTHelper.getTagCompound(stack, "upgrades");
	}
	
	@Override
	public void clearUpgrades(ItemStack stack){
		ItemNBTHelper.remove(stack, "upgrades");
	}
	
	@Override
	public void finishUpgradeRecalculation(ItemStack stack){
		clearUpgrades(stack);
		IItemHandler inv = (IItemHandler) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		CompoundNBT upgradeTag = new CompoundNBT();
		if(inv != null){
			for(int i = 0;i < inv.getSlots();++i){
				ItemStack u = inv.getStackInSlot(i);
				if(!u.isEmpty() && u.getItem() instanceof IUpgrade){
					IUpgrade upg = (IUpgrade) u.getItem();
					if(upg.getUpgradeTypes(u).contains(this.upgradeType) && upg.canApplyUpgrades(stack, u)){
						upg.applyUpgrades(stack, u, upgradeTag);
					}
				}
			}
			
			ItemNBTHelper.setTagCompound(stack, "upgrades", upgradeTag);
			this.finishUpgradeRecalculation(stack);
		}
	}
	
	@Override
	public void recalculateUpgrades(ItemStack stack, World w){
	}
	
	@Override
	public boolean canTakeFromWorkbench(ItemStack stack){
		return false;
	}
	
	@Override
	public void removeFromWorkbench(PlayerEntity player, ItemStack stack){
	}
	
	@Override
	public boolean canModify(ItemStack stack){
		return true;
	}
	
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, Supplier<World> getWorld){
		IItemHandler inv=(IItemHandler)stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		return new Slot[]{
				new IESlot.Upgrades(container, inv, 0, 78, 35 - 5, "BOAT", stack, true, getWorld),
				new IESlot.Upgrades(container, inv, 1, 98, 35 + 5, "BOAT", stack, true, getWorld),
				new IESlot.Upgrades(container, inv, 2, 118, 35 - 5, "BOAT", stack, true, getWorld)
		};
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if(ItemNBTHelper.hasKey(stack, "tank")){
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs != null) tooltip.add(new StringTextComponent(fs.getDisplayName() + ": " + fs.getAmount() + "mB"));
		}
	}
	
	/*
	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack)
	{
		IItemHandler inv = (IItemHandler) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, (EnumFacing) null);
		return new Slot[]
				{
						new IESlot.Upgrades(container, inv, 0, 78, 35 - 5, "BOAT", stack, true),
						new IESlot.Upgrades(container, inv, 1, 98, 35 + 5, "BOAT", stack, true),
						new IESlot.Upgrades(container, inv, 2, 118, 35 - 5, "BOAT", stack, true)
				};
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 4;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		if (ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if (fs != null)
				tooltip.add(fs.getLocalizedName() + ": " + fs.amount + "mB");
		}
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 4;
	}
	 */
}
