package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.IUpgrade;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Set;

public class ItemIPUpgrade extends ItemIPBase implements IUpgrade
{

	public ItemIPUpgrade(String name)
	{
		super(name, 1, "reinforced_hull", "icebreaker", "tank", "rudders", "paddles");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		if (stack.getItemDamage() < getSubNames().length)
		{
			String[] flavour = ImmersiveEngineering.proxy.splitStringOnWidth(I18n.format("desc.immersivepetroleum.flavour.upgrades." + this.getSubNames()[stack.getItemDamage()]), 200);
			for (String s : flavour)
			{
				tooltip.add(s);
			}
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
	public void applyUpgrades(ItemStack target, ItemStack upgrade, NBTTagCompound modifications)
	{
		// TODO Auto-generated method stub

	}


}
