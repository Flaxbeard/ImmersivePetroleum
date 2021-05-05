package flaxbeard.immersivepetroleum.common.gui;

import static flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity.TANK_INPUT;

import blusunrize.immersiveengineering.common.gui.IESlot;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity.Inventory;
import flaxbeard.immersivepetroleum.common.multiblocks.CokerUnitMultiblock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class CokerUnitContainer extends MultiblockAwareGuiContainer<CokerUnitTileEntity>{
	public CokerUnitContainer(int id, PlayerInventory playerInventory, final CokerUnitTileEntity tile){
		super(playerInventory, tile, id, CokerUnitMultiblock.INSTANCE);
		
		addSlot(new CokerInput(this, this.inv, Inventory.INPUT.id(), 20, 71));
		addSlot(new IESlot.FluidContainer(this, this.inv, Inventory.INPUT_FILLED.id(), 9, 14, 2){
			@Override
			public boolean isItemValid(ItemStack stack){
				return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(h -> {
					if(h.getTanks() <= 0 || h.getFluidInTank(0).isEmpty()){
						return false;
					}
					
					FluidStack fs = h.getFluidInTank(0);
					if(tile.bufferTanks[TANK_INPUT].getFluidAmount() > 0 && !fs.isFluidEqual(tile.bufferTanks[TANK_INPUT].getFluid())){
						return false;
					}
					
					return CokerUnitRecipe.hasRecipeWithInput(fs, true);
				}).orElse(false);
			}
		});
		addSlot(new IESlot.Output(this, this.inv, Inventory.INPUT_EMPTY.id(), 9, 45));
		
		addSlot(new IESlot.FluidContainer(this, this.inv, Inventory.OUTPUT_EMPTY.id(), 175, 14, 1));
		addSlot(new IESlot.Output(this, this.inv, Inventory.OUTPUT_FILLED.id(), 175, 45));
		
		this.slotCount = Inventory.values().length;
		
		// Player Inventory
		for(int i = 0;i < 3;i++){
			for(int j = 0;j < 9;j++){
				addSlot(new Slot(playerInventory, j + i * 9 + 9, 20 + j * 18, 105 + i * 18));
			}
		}
		
		// Hotbar
		for(int i = 0;i < 9;i++){
			addSlot(new Slot(playerInventory, i, 20 + i * 18, 163));
		}
	}
	
	class CokerInput extends IESlot{
		public CokerInput(Container container, IInventory inv, int id, int x, int y){
			super(container, inv, id, x, y);
		}
		
		@Override
		public boolean isItemValid(ItemStack stack){
			return !stack.isEmpty() && CokerUnitRecipe.hasRecipeWithInput(stack, true);
		}
	}
}
