package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openzen.zencode.java.ZenCodeType.Constructor;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;

@ZenRegister
@Name("mods.immersivepetroleum.DistillationTower")
public class DistillationRecipeTweaker{
	
	@Method
	public static boolean remove(String name){
		List<ResourceLocation> test = DistillationRecipe.recipes.keySet().stream()
				.filter(loc -> loc.getPath().contains(name))
				.collect(Collectors.toList());
		
		if(test.size() > 1){
			CraftTweakerAPI.logError("§cMultiple results for \"%s\"§r", name);
		}else if(test.size() == 1){
			ResourceLocation id = test.get(0);
			if(DistillationRecipe.recipes.containsKey(id)){
				DistillationRecipe.recipes.remove(id);
				return true;
			}else{
				CraftTweakerAPI.logError("§c%s does not exist, or was already removed.§r", id);
			}
		}else{
			CraftTweakerAPI.logInfo("\"%s\" does not exist or could not be found.", name);
		}
		
		return false;
	}
	
	@Method
	public static void removeAll(){
		DistillationRecipe.recipes.clear();
	}
	
	@ZenRegister
	@Name("mods.immersivepetroleum.DistillationBuilder")
	public static class DistillationRecipeBuilder{
		
		private boolean isValid = true;
		
		private List<Tuple<ItemStack, Double>> byproducts = new ArrayList<>();
		private List<FluidStack> fluidOutputs = new ArrayList<>();
		private MCTag<Fluid> inputFluidTag = null;
		private int inputFluidAmount = 1;
		private int fluxEnergy = 2048;
		private int timeTicks = 1;
		
		@Constructor
		public DistillationRecipeBuilder(){
		}
		
		@Method
		public DistillationRecipeBuilder setOutputFluids(IFluidStack[] fluidsOutput){
			if(fluidsOutput == null || fluidsOutput.length == 0){
				CraftTweakerAPI.logError("§cDistillationBuilder output fluids can not be null!§r");
				this.isValid = false;
			}else{
				this.fluidOutputs = Arrays.asList(fluidsOutput).stream().map(f -> f.getInternal()).collect(Collectors.toList());
			}
			return this;
		}
		
		@Method
		public DistillationRecipeBuilder setInputFluid(MCTag<Fluid> tag, int amount){
			if(tag == null){
				CraftTweakerAPI.logError("§cDistillationBuilder expected fluidtag as input fluid!§r");
				this.isValid = false;
			}else if(amount <= 0){
				CraftTweakerAPI.logError("§ccDistillationBuilder fluidtag amount must atleast be 1mB!§r");
				this.isValid = false;
			}else{
				this.inputFluidTag = tag;
				this.inputFluidAmount = amount;
			}
			return this;
		}
		
		@Method
		public DistillationRecipeBuilder addByproduct(IItemStack item, int chance){
			return addByproduct(item, chance / 100D);
		}
		
		@Method
		public DistillationRecipeBuilder addByproduct(IItemStack item, double chance){
			if(item == null){
				CraftTweakerAPI.logError("§cByproduct item can not be null!§r");
				this.isValid = false;
			}else{
				// Clamping between 0.0 - 1.0
				chance = Math.max(Math.min(chance, 1.0), 0.0);
				
				this.byproducts.add(new Tuple<ItemStack, Double>(item.getInternal(), chance));
			}
			return this;
		}
		
		@Method
		public DistillationRecipeBuilder setEnergyAndTime(int flux, int ticks){
			setEnergy(flux);
			setTime(ticks);
			return this;
		}
		
		@Method
		public DistillationRecipeBuilder setEnergy(int flux){
			if(flux < 1){
				CraftTweakerAPI.logError("§cEnergy usage must be atleast 1 flux/tick!§r");
				this.isValid = false;
			}else{
				this.fluxEnergy = flux;
			}
			return this;
		}
		
		@Method
		public DistillationRecipeBuilder setTime(int ticks){
			if(ticks < 1){
				CraftTweakerAPI.logError("§cProcessing time must be atleast 1 tick!§r");
				this.isValid = false;
			}else{
				this.timeTicks = ticks;
			}
			return this;
		}
		
		@Method
		public void build(String name){
			if(name.isEmpty()){
				CraftTweakerAPI.logError("§cDistillation name can not be empty string!§r");
				this.isValid = false;
			}
			
			FluidTagInput fluidInTag = null;
			if(this.inputFluidTag != null){
				fluidInTag = new FluidTagInput(this.inputFluidTag.getId(), this.inputFluidAmount);
			}else{
				CraftTweakerAPI.logError("§cOutput fluid tag should not be null!§r");
				this.isValid = false;
			}
			
			if(this.isValid){
				ItemStack[] outStacks = new ItemStack[this.byproducts.size()];
				double[] chances = new double[this.byproducts.size()];
				if(!this.byproducts.isEmpty()){
					for(int i = 0;i < this.byproducts.size();i++){
						outStacks[i] = this.byproducts.get(i).getA();
						chances[i] = this.byproducts.get(i).getB().doubleValue();
					}
				}
				
				FluidStack[] fluidOutStacks = new FluidStack[0];
				if(!this.fluidOutputs.isEmpty()){
					fluidOutStacks = this.fluidOutputs.toArray(new FluidStack[0]);
				}
				
				ResourceLocation id = ResourceUtils.ct("distillationtower/" + name);
				
				DistillationRecipe recipe = new DistillationRecipe(id, fluidOutStacks, outStacks, fluidInTag, this.fluxEnergy, this.timeTicks, chances);
				DistillationRecipe.recipes.put(id, recipe);
			}
		}
	}
}
