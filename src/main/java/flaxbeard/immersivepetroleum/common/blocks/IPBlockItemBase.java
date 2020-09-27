package flaxbeard.immersivepetroleum.common.blocks;

import java.util.List;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class IPBlockItemBase extends BlockItem{
	public IPBlockItemBase(Block blockIn, Properties builder){
		super(blockIn, builder);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		if(stack.hasTag()){
			if(stack.getTag().contains("tank")){ // Display Stored Tank Information
				CompoundNBT tank=stack.getTag().getCompound("tank");
				
				FluidStack fluidstack=FluidStack.loadFluidStackFromNBT(tank);
				if(fluidstack.getAmount()>0){
					tooltip.add(((IFormattableTextComponent)fluidstack.getDisplayName()).appendString(" "+fluidstack.getAmount()+"mB").mergeStyle(TextFormatting.GRAY));
				}else{
					tooltip.add(new TranslationTextComponent(Lib.GUI + "empty").mergeStyle(TextFormatting.GRAY));
				}
			}
			if(stack.getTag().contains("energy")){ // Display Stored Energy Information
				CompoundNBT energy=stack.getTag().getCompound("energy");
				int flux=energy.getInt("ifluxEnergy");
				tooltip.add(new StringTextComponent(flux+"RF").mergeStyle(TextFormatting.GRAY));
			}
		}
		
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
}
