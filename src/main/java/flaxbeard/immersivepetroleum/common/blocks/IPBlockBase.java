package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class IPBlockBase extends Block{
	public IPBlockBase(String name, Block.Properties props){
		super(props);
		setRegistryName(ResourceUtils.ip(name));
		
		IPContent.registeredIPBlocks.add(this);
		
		BlockItem bItem = createBlockItem();
		if(bItem != null)
			IPContent.registeredIPItems.add(bItem.setRegistryName(getRegistryName()));
	}
	
	protected BlockItem createBlockItem(){
		return new IPBlockItemBase(this, new Item.Properties().group(ImmersivePetroleum.creativeTab));
	}
}
