package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import java.util.List;
import java.util.stream.Collectors;

import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.helper.CraftTweakerHelper;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.fluids.FluidStack;

@ZenRegister
@Name("mods.immersivepetroleum.DistillationTower")
public class DistillationRecipeTweaker{
	
	@Method
	public static void addRecipe(String name, String[] fluidsOutput, String fluidInputTag, IItemStack[] byproducts, double[] chances, int energy, int time){
		boolean isValid = true;
		
		if(name.contains(":")){
			CraftTweakerAPI.logError("§cDistillation recipe name should not be a resource location. (name)§r");
			isValid = false;
			
		}else if(!name.chars().allMatch(c->ResourceLocation.isValidPathCharacter((char)c))){
			CraftTweakerAPI.logError("§cDistillation recipe name contains invalid characters. (name)§r");
			isValid = false;
		}
		
		if(fluidInputTag == null || fluidInputTag.isEmpty()){
			CraftTweakerAPI.logError("§cFound null FluidStack in distillation recipe. (fluidInputTag)§r");
			isValid = false;
		}
		
		FluidTagInput fluidInTag = null;
		try{
			fluidInTag = TweakerUtils.getFluidTagInput(fluidInputTag);
		}catch(NumberFormatException e){
			String[] split = fluidInputTag.split(", {0,}");
			CraftTweakerAPI.logError("§c\"%s\" is not a number. For \"%s\"§r", split[1], split[0]);
			isValid = false;
		}catch(ResourceLocationException | IllegalArgumentException e){
			CraftTweakerAPI.logError("§c%s", e.getMessage());
			isValid = false;
		}
		
		if(fluidInTag == null){
			CraftTweakerAPI.logError("§cUnable to find \"%s\". (fluidInputTag)§r", fluidInputTag);
			isValid = false;
		}
		
		FluidStack[] outputFluidStacks;
		if((outputFluidStacks = getFluidsFrom(fluidsOutput)) == null){
			CraftTweakerAPI.logError("§cNo distillation recipe output given. (fluidsOutput)§r");
			isValid = false;
		}
		
		ItemStack[] itemOutputs = CraftTweakerHelper.getItemStacks(byproducts);
		if(chances.length != itemOutputs.length){
			CraftTweakerAPI.logError("§cChances size must equal amount of byproducts. (chances)§r");
			isValid = false;
		}
		
		
		if(isValid){
			ResourceLocation id = TweakerUtils.ctLoc("distillationtower/"+name);
			DistillationRecipe recipe = new DistillationRecipe(id, outputFluidStacks, itemOutputs, fluidInTag, energy, time, chances);
			
			DistillationRecipe.recipes.put(id, recipe);
		}
	}
	
	@Method
	public static void remove(String recipeName){
		List<ResourceLocation> test = DistillationRecipe.recipes.keySet().stream()
				.filter(loc -> loc.getPath().contains(recipeName))
				.collect(Collectors.toList());
		
		if(test.size() > 1){
			CraftTweakerAPI.logError("§cMultiple results for \"%s\"§r", recipeName);
		}else if(test.size()==1){
			ResourceLocation id = test.get(0);
			if(DistillationRecipe.recipes.containsKey(id)){
				DistillationRecipe.recipes.remove(id);
			}else{
				CraftTweakerAPI.logError("§c%s does not exist, or was already removed.§r", id);
			}
		}else{
			CraftTweakerAPI.logInfo("\"%s\" does not exist or could not be found.", recipeName);
		}
	}
	
	private static FluidStack[] getFluidsFrom(String... array){
		if(array == null || array.length == 0){
			return null;
		}
		
		FluidStack[] out = new FluidStack[array.length];
		for(int i = 0,j = 0;i < array.length;i++){
			String str = array[i];
			
			try{
				FluidStack fluidstack = TweakerUtils.getFluidStack(str);
				out[j++] = fluidstack;
			}catch(NumberFormatException e){
				String[] split = str.split(", {0,}");
				CraftTweakerAPI.logError("§c\"%s\" is not a number. For \"%s\"§r", split[1], split[0]);
			}catch(ResourceLocationException | IllegalArgumentException e){
				CraftTweakerAPI.logError("§c%s§r", e.getMessage());
			}
		}
		
		return out;
	}
}
