package flaxbeard.immersivepetroleum.api.crafting.builders;

import java.util.Objects;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;

public class CokerUnitRecipeBuilder extends IEFinishedRecipe<CokerUnitRecipeBuilder>{
	public static CokerUnitRecipeBuilder builder(ItemStack output, ITag.INamedTag<Fluid> outputFluid, int fluidOutAmount){
		Objects.requireNonNull(output);
		if(output.isEmpty()) throw new IllegalArgumentException("Input stack cannot be empty.");
		
		return new CokerUnitRecipeBuilder()
				.addResult(output)
				.addOutputFluid(outputFluid, fluidOutAmount);
	}
	
	private CokerUnitRecipeBuilder(){
		super(Serializers.COKER_SERIALIZER.get());
	}
	
	public CokerUnitRecipeBuilder addInputItem(ITag.INamedTag<Item> item, int amount){
		return addInput(new IngredientWithSize(item, amount));
	}
	
	public CokerUnitRecipeBuilder addInputFluid(ITag.INamedTag<Fluid> fluidTag, int amount){
		return addFluidTag("inputfluid", new FluidTagInput(fluidTag.getName(), amount));
	}
	
	public CokerUnitRecipeBuilder addOutputFluid(ITag.INamedTag<Fluid> fluidTag, int amount){
		return addFluidTag("resultfluid", new FluidTagInput(fluidTag.getName(), amount));
	}
	
	public CokerUnitRecipeBuilder setTimeAndEnergy(int time, int energy){
		return setTime(time).setEnergy(energy);
	}
}
