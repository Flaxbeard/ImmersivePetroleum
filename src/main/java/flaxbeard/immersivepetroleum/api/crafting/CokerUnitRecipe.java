package flaxbeard.immersivepetroleum.api.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class CokerUnitRecipe extends IPMultiblockRecipe{
	public static final IRecipeType<CokerUnitRecipe> TYPE = IRecipeType.register(ImmersivePetroleum.MODID + ":cokerunit");
	
	public static Map<ResourceLocation, CokerUnitRecipe> recipes = new HashMap<>();
	
	public static CokerUnitRecipe findRecipe(ItemStack stack, FluidStack fluid){
		for(CokerUnitRecipe recipe:recipes.values()){
			if((recipe.inputItem != null && recipe.inputItem.test(stack)) && (recipe.inputFluid != null && recipe.inputFluid.test(fluid))){
				return recipe;
			}
		}
		
		return null;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull ItemStack stack, @Nonnull FluidStack fluid){
		Objects.requireNonNull(stack);
		Objects.requireNonNull(fluid);
		
		if(!stack.isEmpty() && !fluid.isEmpty()){
			for(CokerUnitRecipe recipe:recipes.values()){
				if(recipe.inputItem != null && recipe.inputFluid != null && recipe.inputItem.test(stack) && recipe.inputFluid.test(fluid)){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull ItemStack stack, boolean ignoreSize){
		Objects.requireNonNull(stack);
		
		if(!stack.isEmpty()){
			for(CokerUnitRecipe recipe:recipes.values()){
				if(recipe.inputItem != null){
					if((!ignoreSize && recipe.inputItem.test(stack)) || (ignoreSize && recipe.inputItem.testIgnoringSize(stack))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(CokerUnitRecipe recipe:recipes.values()){
				if(recipe.inputFluid != null){
					if((!ignoreAmount && recipe.inputFluid.test(fluid)) || (ignoreAmount && recipe.inputFluid.testIgnoringAmount(fluid))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// just a "Reference"
	// Water Input   -> FluidIn
	// Bitumen Input -> Item In
	// Coke Output   -> Item Out
	// Diesel Output -> Fluid Out
	
	public final ItemStack outputItem;
	public final FluidTagInput outputFluid;
	
	public final IngredientWithSize inputItem;
	public final FluidTagInput inputFluid;
	
	public CokerUnitRecipe(ResourceLocation id, ItemStack outputItem, FluidTagInput outputFluid, IngredientWithSize inputItem, FluidTagInput inputFluid, int energy, int time){
		super(ItemStack.EMPTY, TYPE, id);
		this.inputFluid = inputFluid;
		this.inputItem = inputItem;
		this.outputFluid = outputFluid;
		this.outputItem = outputItem;
		
		timeAndEnergy(time, energy);
		modifyTimeAndEnergy(IPServerConfig.REFINING.cokerUnit_energyModifier::get, IPServerConfig.REFINING.cokerUnit_timeModifier::get);
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile){
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(this.outputItem);
		return list;
	}
	
	@Override
	protected IERecipeSerializer<CokerUnitRecipe> getIESerializer(){
		return Serializers.COKER_SERIALIZER.get();
	}
}
