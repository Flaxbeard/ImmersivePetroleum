package flaxbeard.immersivepetroleum.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockAsphalt extends IPBlockBase{
	public BlockAsphalt(){
		super("asphalt", Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F, 10.0F));
	}
}
