package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ContainerDistillationTower extends ContainerIEBase<TileEntityDistillationTower>
{
	public ContainerDistillationTower(InventoryPlayer inventoryPlayer, TileEntityDistillationTower tile)
	{
		super(inventoryPlayer, tile);

		final TileEntityDistillationTower tileF = tile;
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 0, 12, 17, 0)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				IFluidHandler h = (IFluidHandler) itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing) null);
				if (h == null || h.getTankProperties().length == 0)
					return false;
				FluidStack fs = h.getTankProperties()[0].getContents();
				if (fs == null)
					return false;
				if (RefineryRecipe.findIncompleteRefineryRecipe(fs, null) == null)
					return false;
				if (tileF.tanks[0].getFluidAmount() > 0 && !fs.isFluidEqual(tileF.tanks[0].getFluid()))
					return false;
				DistillationRecipe incomplete = DistillationRecipe.findRecipe(fs);
				return incomplete != null;
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 12, 53));

		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 2, 134, 17, 2)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				return super.isItemValid(itemStack) || (!itemStack.isEmpty() && itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));
			}
		});

		this.addSlotToContainer(new IESlot.Output(this, this.inv, 3, 134, 53));


		//this.addSlotToContainer(new IESlot.Output(this, this.inv, 4, 133,15));

		//this.addSlotToContainer(new IESlot.Output(this, this.inv, 5, 133,54));

		slotCount = 4;

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 85 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 143));
		}
	}
}