package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

public abstract class ItemIPUpgradableTool extends ItemIPInternalStorage implements IUpgradeableTool
{
	String upgradeType;

	public ItemIPUpgradableTool(String name, int stackSize, String upgradeType, String... subNames)
	{
		super(name, stackSize, subNames);
		this.upgradeType = upgradeType;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return !OreDictionary.itemMatches(oldStack, newStack, true);
	}

	@Override
	public NBTTagCompound getUpgrades(ItemStack stack)
	{
		return ItemNBTHelper.getTagCompound(stack, "upgrades");
	}

	@Override
	public void clearUpgrades(ItemStack stack)
	{
		ItemNBTHelper.remove(stack, "upgrades");
	}

	@Override
	public void finishUpgradeRecalculation(ItemStack stack)
	{
	}

	@Override
	public void recalculateUpgrades(ItemStack stack)
	{
		clearUpgrades(stack);
		IItemHandler inv = (IItemHandler) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, (EnumFacing) null);
		NBTTagCompound upgradeTag = getUpgradeBase(stack).copy();
		if (inv != null)
		{
			for (int i = 0; i < inv.getSlots(); ++i)
			{
				ItemStack u = inv.getStackInSlot(i);
				if (!u.isEmpty() && u.getItem() instanceof IUpgrade)
				{
					IUpgrade upg = (IUpgrade) u.getItem();
					if (upg.getUpgradeTypes(u).contains(this.upgradeType) && upg.canApplyUpgrades(stack, u))
					{
						upg.applyUpgrades(stack, u, upgradeTag);
					}
				}
			}

			ItemNBTHelper.setTagCompound(stack, "upgrades", upgradeTag);
			this.finishUpgradeRecalculation(stack);
		}
	}

	public NBTTagCompound getUpgradeBase(ItemStack stack)
	{
		return new NBTTagCompound();
	}

	@Override
	public boolean canTakeFromWorkbench(ItemStack stack)
	{
		return true;
	}

	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
	}

	@Override
	public abstract boolean canModify(ItemStack stack);

	@Override
	public abstract Slot[] getWorkbenchSlots(Container var1, ItemStack var2);
}