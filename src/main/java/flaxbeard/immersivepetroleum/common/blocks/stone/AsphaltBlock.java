package flaxbeard.immersivepetroleum.common.blocks.stone;

import java.util.List;
import java.util.Locale;

import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class AsphaltBlock extends IPBlockBase{
	protected static final float SPEED_FACTOR = 1.20F;
	
	public AsphaltBlock(){
		this("asphalt");
	}
	
	protected AsphaltBlock(String name){
		this(name, Block.Properties.create(Material.ROCK).speedFactor(SPEED_FACTOR).hardnessAndResistance(2.0F, 10.0F).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE));
	}
	
	protected AsphaltBlock(String name, Block.Properties props){
		super(name, props);
	}
	
	@Override
	public float getSpeedFactor(){
		return speedFactor();
	}
	
	@Override
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip(stack, worldIn, tooltip, flagIn);
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
	static void tooltip(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		if(IPServerConfig.MISCELLANEOUS.asphalt_speed.get()){
			IFormattableTextComponent out = new TranslationTextComponent("desc.immersivepetroleum.flavour.asphalt", String.format(Locale.ENGLISH, "%.1f%%", (SPEED_FACTOR * 100 - 100))).mergeStyle(TextFormatting.GRAY);
			
			tooltip.add(out);
		}
	}
	
	static float speedFactor(){
		if(!IPServerConfig.MISCELLANEOUS.asphalt_speed.get()){
			return 1.0F;
		}
		
		return SPEED_FACTOR;
	}
}
