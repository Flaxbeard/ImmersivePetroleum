package flaxbeard.immersivepetroleum.common.items;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IUpgrade;

public class ItemIPUpgrade extends ItemIPBase implements IUpgrade
{

	public ItemIPUpgrade(String name)
	{
		super(name, 1, "reinforced_hull", "icebreaker", "tank", "rudders", "paddles");
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if (stack.getItemDamage()<getSubNames().length)
		{
			String[] flavour = ImmersiveEngineering.proxy.splitStringOnWidth(I18n.format("desc.immersivepetroleum.flavour.upgrades." + this.getSubNames()[stack.getItemDamage()]), 200);
			for (String s : flavour)
				list.add(s);
		}
	}

	@Override
	public Set<String> getUpgradeTypes(ItemStack upgrade)
	{
		return ImmutableSet.of("BOAT");
	}

	@Override
	public boolean canApplyUpgrades(ItemStack target, ItemStack upgrade)
	{
		return true;
	}

	@Override
	public void applyUpgrades(ItemStack target, ItemStack upgrade, NBTTagCompound modifications) {
		// TODO Auto-generated method stub
		
	}


}
