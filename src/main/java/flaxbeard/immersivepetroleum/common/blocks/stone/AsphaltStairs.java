package flaxbeard.immersivepetroleum.common.blocks.stone;

import java.util.List;

import flaxbeard.immersivepetroleum.common.blocks.IPBlockStairs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

public class AsphaltStairs extends IPBlockStairs<AsphaltBlock>{
	public AsphaltStairs(AsphaltBlock base){
		super(base);
	}
	
	@Override
	public float getSpeedFactor(){
		return AsphaltBlock.speedFactor();
	}
	
	@Override
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		AsphaltBlock.tooltip(stack, worldIn, tooltip, flagIn);
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
}