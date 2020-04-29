//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package flaxbeard.immersivepetroleum.common.util;

import flaxbeard.immersivepetroleum.common.items.ItemIPInternalStorage;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IPItemStackHandler extends ItemStackHandler implements ICapabilityProvider
{
	private boolean first = true;
	private ItemStack stack;
	@Nonnull
	private Runnable onChange = () ->
	{
	};

	public IPItemStackHandler(ItemStack stack)
	{
		this.stack = stack;
	}

	public void setTile(TileEntity tile)
	{
		if (tile != null)
		{
			this.onChange = tile::markDirty;
		}
		else
		{
			this.onChange = () ->
			{
			};
		}

	}

	public void setInventoryForUpdate(IInventory inv)
	{
		if (inv != null)
		{
			this.onChange = inv::markDirty;
		}
		else
		{
			this.onChange = () ->
			{
			};
		}

	}

	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.onChange.run();
	}

	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Nullable
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		if (this.first)
		{
			int idealSize = ((ItemIPInternalStorage) this.stack.getItem()).getSlotCount(this.stack);
			NonNullList<ItemStack> newList = NonNullList.withSize(idealSize, ItemStack.EMPTY);

			for (int i = 0; i < Math.min(this.stacks.size(), idealSize); ++i)
			{
				newList.set(i, this.stacks.get(i));
			}

			this.stacks = newList;
			this.stack = ItemStack.EMPTY;
			this.first = false;
		}

		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : null;
	}

	public NonNullList<ItemStack> getContainedItems()
	{
		return this.stacks;
	}
}
