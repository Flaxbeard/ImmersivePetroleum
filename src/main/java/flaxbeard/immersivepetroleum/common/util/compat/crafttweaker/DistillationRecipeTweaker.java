package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType.Field;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.helper.CraftTweakerHelper;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.fluids.FluidStack;

@ZenRegister
@Name("mods.immersivepetroleum.DistillationTower")
public class DistillationRecipeTweaker implements IRecipeManager{
	@Field
	public static final DistillationRecipeTweaker instance=new DistillationRecipeTweaker();
	
	@Override
	public IRecipeType<DistillationRecipe> getRecipeType(){
		return DistillationRecipe.TYPE;
	}
	
	@Method
	public void addRecipe(String name, String[] fluidsOutput, String fluidInputTag, IItemStack[] byproducts, double[] chances, int energy, int time){
		if(fluidInputTag==null || fluidInputTag.isEmpty()){
			CraftTweakerAPI.logError("§cFound null FluidStack in distillation recipe. (fluidInputTag)§r");
			return;
		}
		
		FluidTagInput fluidInTag=null;
		try{
			fluidInTag=TweakerUtils.getFluidTagInput(fluidInputTag);
		}catch(NumberFormatException e){
			String[] split=fluidInputTag.split(", {0,}");
			CraftTweakerAPI.logError("§c\"%s\" is not a number. For \"%s\"§r", split[1], split[0]);
			return;
		}catch(ResourceLocationException | IllegalArgumentException e){
			CraftTweakerAPI.logError("§c%s", e.getMessage());
			return;
		}
		if(fluidInTag==null){
			CraftTweakerAPI.logError("§cUnable to find \"%s\". (fluidInputTag)§r", fluidInputTag);
		}
		
		FluidStack[] outputFluidStacks;
		if((outputFluidStacks=getFluidsFrom(fluidsOutput))==null){
			CraftTweakerAPI.logError("§cNo distillation recipe output given. (fluidsOutput)§r");
			return;
		}
		
		ItemStack[] itemOutputs=CraftTweakerHelper.getItemStacks(byproducts);
		if(chances.length!=itemOutputs.length){
			CraftTweakerAPI.logError("§cChances size must equal amount of byproducts. (chances)§r");
			return;
		}
		
		ResourceLocation id=new ResourceLocation("crafttweaker", name);
		DistillationRecipe recipe=new DistillationRecipe(id, outputFluidStacks, itemOutputs, fluidInTag, energy, time, chances);
		
		DistillationRecipe.recipes.put(id, recipe);
		CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, ""));
		
		CraftTweakerAPI.logInfo("Added Distillation Recipe: %s", id.getPath());
	}
	
	@Method
	public void remove(String recipeId){
		ResourceLocation id=new ResourceLocation(recipeId);
		if(DistillationRecipe.recipes.containsKey(id)){
			if(DistillationRecipe.recipes.remove(id)!=null){
				CraftTweakerAPI.logInfo("§cRemoved Distillation Recipe: %s§r", id);
			}else{
				CraftTweakerAPI.logInfo("§c%s does not exist, or was already removed.§r", id);
			}
		}
	}
	
	
	private static FluidStack[] getFluidsFrom(String... array){
		if(array==null || array.length==0){
			return null;
		}
		
		FluidStack[] out=new FluidStack[array.length];
		for(int i=0,j=0;i<array.length;i++){
			String str=array[i];
			
			try{
				FluidStack fluidstack=TweakerUtils.getFluidStack(str);
				out[j++]=fluidstack;
			}catch(NumberFormatException e){
				String[] split=str.split(", {0,}");
				CraftTweakerAPI.logError("§c\"%s\" is not a number. For \"%s\"§r", split[1], split[0]);
			}catch(ResourceLocationException | IllegalArgumentException e){
				CraftTweakerAPI.logError("§c%s§r", e.getMessage());
			}
		}
		
		return out;
	}
}
