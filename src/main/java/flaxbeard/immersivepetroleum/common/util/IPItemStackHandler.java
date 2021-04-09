//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package flaxbeard.immersivepetroleum.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class IPItemStackHandler extends ItemStackHandler implements ICapabilityProvider{
	private boolean first = true;
	@Nonnull
	private Runnable onChange = () -> {
	};
	
	public IPItemStackHandler(){
	}
	
	@Override
	public int getSlots(){
		return 4;
	}
	
	public void setTile(TileEntity tile){
		if(tile != null){
			this.onChange = tile::markDirty;
		}else{
			this.onChange = () -> {
			};
		}
		
	}
	
	public void setInventoryForUpdate(IInventory inv){
		if(inv != null){
			this.onChange = inv::markDirty;
		}else{
			this.onChange = () -> {
			};
		}
		
	}
	
	protected void onContentsChanged(int slot){
		super.onContentsChanged(slot);
		this.onChange.run();
	}
	
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing){
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}
	
	@Nullable
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing){
		if(this.first){
			int idealSize = getSlots();
			NonNullList<ItemStack> list = NonNullList.withSize(idealSize, ItemStack.EMPTY);
			for(int i = 0;i < Math.min(this.stacks.size(), idealSize);++i){
				list.set(i, this.stacks.get(i));
			}
			
			this.stacks = list;
			this.first = false;
		}
		
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return LazyOptional.of(() -> this).cast();
		}
		
		return LazyOptional.empty();
	}
	
	public NonNullList<ItemStack> getContainedItems(){
		return this.stacks;
	}
}
