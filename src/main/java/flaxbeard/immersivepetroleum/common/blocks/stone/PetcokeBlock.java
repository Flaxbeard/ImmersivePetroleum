package flaxbeard.immersivepetroleum.common.blocks.stone;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockItemBase;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;

public class PetcokeBlock extends IPBlockBase{
	public PetcokeBlock(){
		super("petcoke_block", Block.Properties.create(Material.ROCK).sound(SoundType.STONE).setRequiresTool().harvestTool(ToolType.PICKAXE).hardnessAndResistance(2, 10));
	}
	
	@Override
	protected BlockItem createBlockItem(){
		return new IPBlockItemBase(this, new Item.Properties().group(ImmersivePetroleum.creativeTab)){
			@Override
			public int getBurnTime(ItemStack itemStack){
				return 32000;
			}
		};
	}
}
