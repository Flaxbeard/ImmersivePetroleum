package flaxbeard.immersivepetroleum.api.crafting.builders;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.tags.ITag;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;

/**
 * Distillation Recipe creation using DataGeneration
 * 
 * @author TwistedGate
 */
public class DistillationRecipeBuilder extends IEFinishedRecipe<DistillationRecipeBuilder>{
	
	public static DistillationRecipeBuilder builder(FluidStack... fluidOutput){
		if(fluidOutput == null || ((fluidOutput != null && fluidOutput.length == 0)))
			throw new IllegalArgumentException("Fluid output missing. It's required.");
		
		DistillationRecipeBuilder b = new DistillationRecipeBuilder();
		if(fluidOutput != null && fluidOutput.length > 0)
			b.addFluids("results", fluidOutput);
		return b;
	}
	
	/** Temporary storage for byproducts */
	private List<Tuple<ItemStack, Double>> byproducts = new ArrayList<>();
	
	private DistillationRecipeBuilder(){
		super(Serializers.DISTILLATION_SERIALIZER.get());
		addWriter(jsonObject -> {
			if(this.byproducts.size() > 0){
				final JsonArray main = new JsonArray();
				this.byproducts.forEach(by -> main.add(serializerItemStackWithChance(by)));
				jsonObject.add("byproducts", main);
				this.byproducts.clear();
			}
		});
	}
	
	/**
	 * Can be called multiple times to add more byproducts to the recipe
	 * 
	 * @param byproduct
	 * @param chance 0 to 100 (clamped)
	 * @return self for chaining
	 */
	public DistillationRecipeBuilder addByproduct(ItemStack byproduct, int chance){
		return addByproduct(byproduct, chance / 100D);
	}
	
	/**
	 * Can be called multiple times to add more byproducts to the recipe.
	 * 
	 * @param byproduct
	 * @param chance 0.0 to 1.0 (clamped)
	 * @return self for chaining
	 */
	public DistillationRecipeBuilder addByproduct(ItemStack byproduct, double chance){
		this.byproducts.add(new Tuple<ItemStack, Double>(byproduct, Math.max(Math.min(chance, 1.0), 0.0)));
		return this;
	}
	
	public DistillationRecipeBuilder setTimeAndEnergy(int time, int energy){
		return setTime(time).setEnergy(energy);
	}
	
	public DistillationRecipeBuilder addInput(ITag.INamedTag<Fluid> fluidTag, int amount){
		return addFluidTag("input", fluidTag, amount);
	}
	
	public DistillationRecipeBuilder addInput(Fluid fluid, int amount){
		return addInput(new FluidStack(fluid, amount));
	}
	
	public DistillationRecipeBuilder addInput(FluidStack fluidStack){
		return addFluid("input", fluidStack);
	}
	
	public DistillationRecipeBuilder addFluids(String key, FluidStack... fluidStacks){
		return addWriter(jsonObject -> {
			JsonArray array = new JsonArray();
			for(FluidStack stack:fluidStacks)
				array.add(ApiUtils.jsonSerializeFluidStack(stack));
			jsonObject.add(key, array);
		});
	}
	
	public DistillationRecipeBuilder addItems(String key, ItemStack... itemStacks){
		return addWriter(jsonObject -> {
			JsonArray array = new JsonArray();
			for(ItemStack stack:itemStacks){
				array.add(serializeItemStack(stack));
			}
			jsonObject.add(key, array);
		});
	}
	
	public static Tuple<ItemStack, Double> deserializeItemStackWithChance(JsonObject jsonObject){
		if(jsonObject.has("chance") && jsonObject.has("item")){
			double chance = jsonObject.get("chance").getAsDouble();
			jsonObject.remove("chance");
			ItemStack stack = ShapedRecipe.deserializeItem(jsonObject);
			return new Tuple<ItemStack, Double>(stack, chance);
		}
		
		throw new IllegalArgumentException("Unexpected json object.");
	}
	
	private static final DistillationRecipeBuilder dummy = new DistillationRecipeBuilder();
	public static JsonObject serializerItemStackWithChance(@Nonnull Tuple<ItemStack, Double> tuple){
		JsonObject itemJson = dummy.serializeItemStack(tuple.getA());
		itemJson.addProperty("chance", tuple.getB().toString());
		return itemJson;
	}
}
