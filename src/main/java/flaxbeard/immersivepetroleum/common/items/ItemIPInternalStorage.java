package flaxbeard.immersivepetroleum.common.items;

import javax.annotation.Nullable;

import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.common.util.IPItemStackHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public abstract class ItemIPInternalStorage extends IPItemBase{
	public ItemIPInternalStorage(String name, int stackSize, String... subNames){
		super(name);
	}
	
	public abstract int getSlotCount(ItemStack var1);
	
	@Nullable
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt){
		return !stack.isEmpty() ? new IPItemStackHandler(stack) : null;
	}
	
	public void setContainedItems(ItemStack stack, NonNullList<ItemStack> inventory){
		IItemHandler handler = (IItemHandler) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if(handler instanceof IItemHandlerModifiable){
			if(inventory.size() != handler.getSlots()){
				throw new IllegalArgumentException("Parameter inventory has " + inventory.size() + " slots, capability inventory has " + handler.getSlots());
			}
			
			for(int i = 0;i < handler.getSlots();++i){
				((IItemHandlerModifiable) handler).setStackInSlot(i, (ItemStack) inventory.get(i));
			}
		}else{
			IELogger.warn("No valid inventory handler found for " + stack);
		}
		
	}
	
	public NonNullList<ItemStack> getContainedItems(ItemStack stack){
		IItemHandler handler = (IItemHandler) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if(handler instanceof IPItemStackHandler){
			return ((IPItemStackHandler) handler).getContainedItems();
		}else if(handler == null){
			IELogger.info("No valid inventory handler found for " + stack);
			return NonNullList.create();
		}else{
			IELogger.warn("Inefficiently getting contained items. Why does " + stack + " have a non-IE IItemHandler?");
			NonNullList<ItemStack> inv = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			
			for(int i = 0;i < handler.getSlots();++i){
				inv.set(i, handler.getStackInSlot(i));
			}
			
			return inv;
		}
	}
	

	public void ech(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected){
		if(ItemNBTHelper.hasKey(stack, "Inv")){
			CompoundNBT invTag = ItemNBTHelper.getTagCompound(stack, "Inv");
			
			NonNullList<ItemStack> list=NonNullList.create();
			ItemStackHelper.loadAllItems(invTag, list);
			
			this.setContainedItems(stack, Utils.readInventory(invTag, this.getSlotCount(stack)));
			ItemNBTHelper.remove(stack, "Inv");
			if(entityIn instanceof ServerPlayerEntity && !worldIn.isRemote){
				((ServerPlayerEntity) entityIn).connection.sendPacket(new SPacketSetSlot(-2, itemSlot, stack));
			}
		}
	}
}
