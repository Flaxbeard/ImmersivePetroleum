package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemIPUpgradableTool extends ItemIPInternalStorage implements IUpgradeableTool{
	String upgradeType;
	
	public ItemIPUpgradableTool(String name, int stackSize, String upgradeType, String... subNames){
		super(name, stackSize, subNames);
		this.upgradeType = upgradeType;
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
	}
	
	public void recalculateUpgrades(ItemStack stack){
		clearUpgrades(stack);
		IItemHandler inv = (IItemHandler) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		CompoundNBT upgradeTag = getUpgradeBase(stack).copy();
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
	
	public CompoundNBT getUpgradeBase(ItemStack stack){
		return new CompoundNBT();
	}
	
	@Override
	public boolean canTakeFromWorkbench(ItemStack stack){
		return true;
	}
	
	@Override
	public void removeFromWorkbench(PlayerEntity player, ItemStack stack){
	}
	
	@Override
	public boolean canModify(ItemStack stack){
		return false;
	}
}
