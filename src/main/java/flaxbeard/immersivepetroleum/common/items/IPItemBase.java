package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.item.Item;

public class IPItemBase extends Item implements IColouredItem{
	/** For basic items */
	public IPItemBase(String name){
		this(name, new Item.Properties());
	}
	
	/** For items that require special attention */
	public IPItemBase(String name, Item.Properties properties){
		super(properties.group(ImmersivePetroleum.creativeTab));
		setRegistryName(ResourceUtils.ip(name));
		
		IPContent.registeredIPItems.add(this);
	}
}
