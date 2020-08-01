package flaxbeard.immersivepetroleum.common.data;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;

public class IPRecipes extends RecipeProvider{
	protected Consumer<IFinishedRecipe> out;
	public IPRecipes(DataGenerator generatorIn){
		super(generatorIn);
	}
	
	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> consumer){
		this.out=consumer;
		
		
	}
}
