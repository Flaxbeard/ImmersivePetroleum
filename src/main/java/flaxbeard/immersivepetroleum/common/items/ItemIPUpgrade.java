package flaxbeard.immersivepetroleum.common.items;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

@Deprecated
public class ItemIPUpgrade extends ItemIPBase implements IUpgrade
{

	public ItemIPUpgrade(String name)
	{
		super(name, 1, "reinforced_hull", "icebreaker", "tank", "rudders", "paddles");
	}
/*
	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		if (stack.getItemDamage() < getSubNames().length)
		{
			String[] flavour = ImmersiveEngineering.proxy.splitStringOnWidth(I18n.format("desc.immersivepetroleum.flavour.upgrades." + this.getSubNames()[stack.getItemDamage()]), 200);
			for (String s : flavour)
			{
				tooltip.add(s);
			}
		}
	}
*/
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
	public void applyUpgrades(ItemStack target, ItemStack upgrade, CompoundNBT modifications)
	{
	}
}
