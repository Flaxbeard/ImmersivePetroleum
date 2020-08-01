package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class IPBlockBase extends Block{
	public IPBlockBase(String name, Block.Properties props){
		super(props);
		setRegistryName(new ResourceLocation(ImmersivePetroleum.MODID, name));
		
		IPContent.registeredIPBlocks.add(this);
	}
}
