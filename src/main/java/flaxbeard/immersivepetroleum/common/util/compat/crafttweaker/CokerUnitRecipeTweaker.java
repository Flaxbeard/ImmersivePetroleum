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
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

@ZenRegister
@Document("mods/immersivepetroleum/Coker")
@Name("mods.immersivepetroleum.CokerUnit")
public class CokerUnitRecipeTweaker implements IRecipeManager{
	@Override
	public IRecipeType<CokerUnitRecipe> getRecipeType(){
		return CokerUnitRecipe.TYPE;
	}
	
	/**
	 * Removes all recipes
	 */
	@Method
	public void removeAll(){
		CokerUnitRecipe.recipes.clear();
	}
	
	/**
	 * Removes all recipes that output the given IIngredient.
	 * 
	 * @param output
	 * @docParam output <item:immersivepetroleum:petcoke>
	 * @docParam output <tag:items:forge:coal_petcoke>
	 */
	@Method
	public void remove(IIngredient output){
		CokerUnitRecipe.recipes.values().removeIf(recipe -> output.matches(new MCItemStack(recipe.outputItem)));
	}
	
	/**
	 * Removes all recipes that output the given IFluidStack.
	 * 
	 * @param output <fluid:immersivepetroleum:diesel>
	 */
	@Method
	public void remove(IFluidStack output){
		CokerUnitRecipe.recipes.values().removeIf(recipe -> recipe.outputFluid.testIgnoringAmount(output.getInternal()));
	}
	
	/**
	 * 
	 * @param recipePath The recipe name, without the resource location
	 * @param inputItem The input ingredient
	 * @param outputItem The output ingredient
	 * @param inputFluid The input fluid
	 * @param outputFluid The output fluid
	 * @param energy energy required per tick
	 * 
	 * @docParam recipePath "clay_from_sand"
	 * @docParam inputItem <item:minecraft:sand>
	 * @docParam outputItem <item:minecraft:clay_ball>
	 * @docParam inputFluid <tag:fluids:minecraft:water> * 125
	 * @docParam outputFluid <tag:fluids:minecraft:water> * 25
	 * @docParam energy 1024
	 */
	@Method
	public void addRecipe(String recipePath, IItemStack inputItem, IItemStack outputItem, MCTagWithAmount<Fluid> inputFluid, MCTagWithAmount<Fluid> outputFluid, int energy){
		ResourceLocation id = TweakerUtils.ctLoc("cokerunit/" + recipePath);
		FluidTagInput outFluid = new FluidTagInput(outputFluid.getTag().getId(), outputFluid.getAmount());
		FluidTagInput inFluid = new FluidTagInput(inputFluid.getTag().getId(), inputFluid.getAmount());
		
		IngredientWithSize inStack = new IngredientWithSize(Ingredient.fromStacks(inputItem.getInternal()), inputItem.getAmount());
		ItemStack outStack = outputItem.getInternal();
		
		CokerUnitRecipe recipe = new CokerUnitRecipe(id, outStack, outFluid, inStack, inFluid, energy, 30);
		
		// Does NOT work with this
		//CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
		
		// This however does, while it may not be the safest thing to do..
		CokerUnitRecipe.recipes.put(id, recipe);
	}
}
