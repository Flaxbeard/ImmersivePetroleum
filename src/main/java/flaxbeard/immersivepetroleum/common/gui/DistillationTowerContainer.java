package flaxbeard.immersivepetroleum.common.gui;

import static flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity.INV_0;
import static flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity.INV_1;
import static flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity.INV_2;
import static flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity.INV_3;
import static flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity.TANK_INPUT;

import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.gui.IPSlot.FluidContainer.FluidFilter;
import flaxbeard.immersivepetroleum.common.multiblocks.DistillationTowerMultiblock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class DistillationTowerContainer extends MultiblockAwareGuiContainer<DistillationTowerTileEntity>{
	public DistillationTowerContainer(int id, PlayerInventory playerInventory, final DistillationTowerTileEntity tile){
		super(playerInventory, tile, id, DistillationTowerMultiblock.INSTANCE);
		
		addSlot(new IPSlot(this.inv, INV_0, 12, 17){
			@Override
			public boolean isItemValid(ItemStack stack){
				return FluidUtil.getFluidHandler(stack).map(h -> {
					if(h.getTanks() <= 0){
						return false;
					}
					
					FluidStack fs = h.getFluidInTank(0);
					if(fs.isEmpty() || (tile.tanks[TANK_INPUT].getFluidAmount() > 0 && !fs.isFluidEqual(tile.tanks[TANK_INPUT].getFluid()))){
						return false;
					}
					
					DistillationRecipe recipe = DistillationRecipe.findRecipe(fs);
					return recipe != null;
				}).orElse(false);
			}
		});
		addSlot(new IPSlot.ItemOutput(this.inv, INV_1, 12, 53));
		
		addSlot(new IPSlot.FluidContainer(this.inv, INV_2, 134, 17, FluidFilter.EMPTY));
		addSlot(new IPSlot.ItemOutput(this.inv, INV_3, 134, 53));
		
		slotCount = 4;
		
		for(int i = 0;i < 3;i++){
			for(int j = 0;j < 9;j++){
				addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 85 + i * 18));
			}
		}
		for(int i = 0;i < 9;i++){
			addSlot(new Slot(playerInventory, i, 8 + i * 18, 143));
		}
	}
}
