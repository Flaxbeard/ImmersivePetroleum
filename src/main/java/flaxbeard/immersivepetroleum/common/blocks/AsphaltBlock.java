package flaxbeard.immersivepetroleum.common.blocks;

import java.util.List;
import java.util.Locale;

import flaxbeard.immersivepetroleum.common.IPConfig;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class AsphaltBlock extends IPBlockBase{
	private static final double SPEED_MULTIPLIER = 1.125D;
	
	public AsphaltBlock(){
		super("asphalt", Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F, 10.0F).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE));
	}
	
	@Override
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		if(IPConfig.MISCELLANEOUS.asphalt_speed.get()){
			IFormattableTextComponent out = new TranslationTextComponent(
					"desc.immersivepetroleum.flavour.asphalt",
					String.format(Locale.ENGLISH, "%.1f", (SPEED_MULTIPLIER * 100 - 100)) + "%"
			).mergeStyle(TextFormatting.GRAY);
			
			tooltip.add(out);
		}
		
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
	@Override
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn){
		if(IPConfig.MISCELLANEOUS.asphalt_speed.get()){
			if(entityIn instanceof PlayerEntity){
				PlayerEntity player = (PlayerEntity) entityIn;
				Vector3d motion = player.getMotion();
				double speedMultiplier = SPEED_MULTIPLIER;
				player.setMotion(new Vector3d(motion.x * speedMultiplier, motion.y * speedMultiplier, motion.z * speedMultiplier));
			}
		}
	}
}
