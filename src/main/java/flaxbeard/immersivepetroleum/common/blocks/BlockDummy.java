package flaxbeard.immersivepetroleum.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockDummy extends IPBlockBase{
	public BlockDummy(String name){
		super(name, Block.Properties.create(Material.ROCK));
	}
}
