package flaxbeard.immersivepetroleum.common.gui;

import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class IPSlot extends Slot{
	
	public IPSlot(IInventory inventoryIn, int index, int xPosition, int yPosition){
		super(inventoryIn, index, xPosition, yPosition);
	}
	
	public static class ItemOutput extends IPSlot{
		public ItemOutput(IInventory inventoryIn, int index, int xPosition, int yPosition){
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		@Override
		public boolean isItemValid(ItemStack stack){
			return false;
		}
	}
	
	public static class CokerInput extends IPSlot{
		public CokerInput(Container container, IInventory inv, int id, int x, int y){
			super(inv, id, x, y);
		}
		
		@Override
		public boolean isItemValid(ItemStack stack){
			return !stack.isEmpty() && CokerUnitRecipe.hasRecipeWithInput(stack, true);
		}
	}
	
	public static class FluidContainer extends IPSlot{
		FluidFilter filter;
		public FluidContainer(IInventory inv, int id, int x, int y, FluidFilter filter){
			super(inv, id, x, y);
			this.filter = filter;
		}
		
		@Override
		public boolean isItemValid(ItemStack itemStack){
			LazyOptional<IFluidHandlerItem> handlerCap = FluidUtil.getFluidHandler(itemStack);
			return handlerCap.map(handler -> {
				if(handler.getTanks() <= 0)
					return false;
				
				switch(filter){
					case FULL:
						return !handler.getFluidInTank(0).isEmpty();
					case EMPTY:
						return handler.getFluidInTank(0).isEmpty();
					case ANY:
					default:
						return true;
				}
			}).orElse(false);
		}
		
		public static enum FluidFilter{
			ANY, EMPTY, FULL;
		}
	}
}
