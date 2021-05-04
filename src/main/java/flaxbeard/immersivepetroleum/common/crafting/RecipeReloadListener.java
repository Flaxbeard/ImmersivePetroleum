package flaxbeard.immersivepetroleum.common.crafting;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.SulfurRecoveryRecipe;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler.ReservoirType;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("deprecation")
public class RecipeReloadListener implements IResourceManagerReloadListener{
	private final DataPackRegistries dataPackRegistries;
	public RecipeReloadListener(DataPackRegistries dataPackRegistries){
		this.dataPackRegistries = dataPackRegistries;
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager){
		if(dataPackRegistries != null){
			lists(dataPackRegistries.getRecipeManager());
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void recipesUpdated(RecipesUpdatedEvent event){
		if(!Minecraft.getInstance().isSingleplayer()){
			lists(event.getRecipeManager());
		}
	}
	
	static void lists(RecipeManager recipeManager){
		Collection<IRecipe<?>> recipes = recipeManager.getRecipes();
		if(recipes.size() == 0){
			return;
		}
		
		ImmersivePetroleum.log.info("Loading Distillation Recipes.");
		DistillationRecipe.recipes = filterRecipes(recipes, DistillationRecipe.class, DistillationRecipe.TYPE);
		
		ImmersivePetroleum.log.info("Loading Reservoirs.");
		PumpjackHandler.reservoirs = filterRecipes(recipes, ReservoirType.class, ReservoirType.TYPE);
		
		ImmersivePetroleum.log.info("Loading Coker-Unit Recipes.");
		CokerUnitRecipe.recipes = filterRecipes(recipes, CokerUnitRecipe.class, CokerUnitRecipe.TYPE);
		
		ImmersivePetroleum.log.info("Loading Sulfur Recovery Recipes.");
		SulfurRecoveryRecipe.recipes = filterRecipes(recipes, SulfurRecoveryRecipe.class, SulfurRecoveryRecipe.TYPE);
	}
	
	static <R extends IRecipe<?>> Map<ResourceLocation, R> filterRecipes(Collection<IRecipe<?>> recipes, Class<R> recipeClass, IRecipeType<R> recipeType){
		return recipes.stream()
				.filter(iRecipe -> iRecipe.getType() == recipeType)
				.map(recipeClass::cast)
				.collect(Collectors.toMap(recipe -> recipe.getId(), recipe -> recipe));
	}
}
