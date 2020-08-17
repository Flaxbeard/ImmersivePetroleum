package flaxbeard.immersivepetroleum.common.items;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IPUpgradeItem extends IPItemBase implements IUpgrade{
	private Set<String> set;
	public IPUpgradeItem(String name, String type){
		super(name, new Item.Properties().maxStackSize(1));
		this.set=ImmutableSet.of(type);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("desc.immersivepetroleum.flavour.upgrades." + getRegistryName().getPath()));
	}

	@Override
	public Set<String> getUpgradeTypes(ItemStack upgrade){
		return this.set;
	}

	@Override
	public boolean canApplyUpgrades(ItemStack target, ItemStack upgrade){
		return true;
	}

	@Override
	public void applyUpgrades(ItemStack target, ItemStack upgrade, CompoundNBT modifications){
	}
}
