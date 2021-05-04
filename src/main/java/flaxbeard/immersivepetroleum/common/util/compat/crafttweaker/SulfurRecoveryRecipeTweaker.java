package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.Document;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import flaxbeard.immersivepetroleum.api.crafting.SulfurRecoveryRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

@ZenRegister
@Document("mods/immersivepetroleum/SRU")
@Name("mods.immersivepetroleum.Hydrotreater")
public class SulfurRecoveryRecipeTweaker implements IRecipeManager{
	@Override
	public IRecipeType<SulfurRecoveryRecipe> getRecipeType(){
		return SulfurRecoveryRecipe.TYPE;
	}
	
	/**
	 * Removes all recipes
	 */
	@Method
	public void removeAll(){
		SulfurRecoveryRecipe.recipes.clear();
	}
	
	@Method
	public void removeByOutputItem(IIngredient output){
		SulfurRecoveryRecipe.recipes.values().removeIf(recipe -> output.matches(new MCItemStack(recipe.outputItem)));
	}
	
	@Method
	public void removeByOutputFluid(IFluidStack output){
		SulfurRecoveryRecipe.recipes.values().removeIf(recipe -> recipe.output.isFluidEqual(output.getInternal()));
	}
	
	@Method
	public void addRecipe(String recipePath, IFluidStack output, IItemStack outputItem, double chance, MCTagWithAmount<Fluid> inputFluid, int energy){
		ResourceLocation id = TweakerUtils.ctLoc("hydrotreater/" + recipePath);
		
		FluidTagInput primary = new FluidTagInput(inputFluid.getTag().getId(), inputFluid.getAmount());
		
		newRecipe(id, output, outputItem, chance, primary, null, energy);
	}
	
	@Method
	public void addRecipeWithSecondary(String recipePath, IFluidStack output, IItemStack outputItem, double chance, MCTagWithAmount<Fluid> inputFluid, MCTagWithAmount<Fluid> inputFluidSecondary, int energy){
		ResourceLocation id = TweakerUtils.ctLoc("hydrotreater/" + recipePath);
		
		FluidTagInput primary = new FluidTagInput(inputFluid.getTag().getId(), inputFluid.getAmount());
		FluidTagInput secondary = new FluidTagInput(inputFluidSecondary.getTag().getId(), inputFluidSecondary.getAmount());
		
		newRecipe(id, output, outputItem, chance, primary, secondary, energy);
	}
	
	private void newRecipe(ResourceLocation id, IFluidStack output, IItemStack outputItem, double chance, FluidTagInput primary, FluidTagInput secondary, int energy){
		SulfurRecoveryRecipe recipe = new SulfurRecoveryRecipe(id, output.getInternal(), outputItem.getInternal(), primary, secondary, chance, energy, 1);
		// Does NOT work with this
		//CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
		
		// This however does, while it may not be the safest thing to do..
		SulfurRecoveryRecipe.recipes.put(id, recipe);
	}
}
