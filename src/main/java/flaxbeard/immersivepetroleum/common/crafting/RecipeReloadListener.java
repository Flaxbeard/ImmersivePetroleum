package flaxbeard.immersivepetroleum.common.crafting;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@SuppressWarnings("deprecation")
public class RecipeReloadListener implements IResourceManagerReloadListener{
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager){
		if(EffectiveSide.get().isServer()){
			lists(ServerLifecycleHooks.getCurrentServer().getRecipeManager());
		}
	}
	
	@SubscribeEvent
	public void recipesUpdated(RecipesUpdatedEvent event){
		lists(event.getRecipeManager());
	}
	
	static void lists(RecipeManager recipeManager){
		Collection<IRecipe<?>> recipes=recipeManager.getRecipes();
		
		DistillationRecipe.recipes=filterRecipes(recipes, DistillationRecipe.class, DistillationRecipe.TYPE);
		
		PumpjackHandler.reservoirs=new LinkedHashMap<>(filterRecipes(recipes, ReservoirType.class, ReservoirType.TYPE));
	}
	
	static <R extends IRecipe<?>> Map<ResourceLocation, R> filterRecipes(Collection<IRecipe<?>> recipes, Class<R> recipeClass, IRecipeType<R> recipeType){
		return recipes.stream()
				.filter(iRecipe -> iRecipe.getType()==recipeType)
				.map(recipeClass::cast)
				.collect(Collectors.toMap(recipe -> recipe.getId(), recipe -> recipe));
	}
}
