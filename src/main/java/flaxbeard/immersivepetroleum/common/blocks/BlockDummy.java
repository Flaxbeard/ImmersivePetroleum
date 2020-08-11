package flaxbeard.immersivepetroleum.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;

public class BlockDummy extends IPBlockBase{
	public BlockDummy(String name){
		super(name, Block.Properties.create(Material.ROCK));
	}
	
	@Override
	protected BlockItem createBlockItem(){
		return null;
	}
}
