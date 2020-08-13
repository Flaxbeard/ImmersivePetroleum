package flaxbeard.immersivepetroleum.api.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;

/**
 * Distillation Recipe creation using DataGeneration
 * 
 * @author TwistedGate
 */
public class DistillationRecipeBuilder extends IEFinishedRecipe<DistillationRecipeBuilder>{
	// TODO Refine this whole thing if nessesary
	
	/** Temporary storage for byproducts */
	private List<Tuple<ItemStack, Double>> byproducts=new ArrayList<>();
	
	private DistillationRecipeBuilder(){
		super(DistillationRecipe.SERIALIZER.get());
	}
	
	public static DistillationRecipeBuilder builder(FluidStack... fluidOutput){
		if(fluidOutput==null || ((fluidOutput!=null && fluidOutput.length==0)))
			throw new IllegalArgumentException("Missing required fluid output.");
		
		DistillationRecipeBuilder b=new DistillationRecipeBuilder();
		if(fluidOutput!=null && fluidOutput.length>0)
			b.addFluids("results", fluidOutput);
		return b;
	}
	
	/**
	 * Can be called multiple times to add more byproducts to the recipe
	 * 
	 * @param byproduct
	 * @param chance 0 to 100 (clamped)
	 * @return self for chaining
	 */
	public DistillationRecipeBuilder addByproduct(ItemStack byproduct, int chance){
		return addByproduct(byproduct, chance/100D);
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
	
	/** Defaults to 1 when loading the recipe in-game (including reload) */
	@Override
	public DistillationRecipeBuilder setTime(int time){
		return super.setTime(time);
	}
	
	/** Defaults to 2048 when loading the recipe in-game (including reload) */
	@Override
	public DistillationRecipeBuilder setEnergy(int energy){
		return super.setEnergy(energy);
	}
	
	public DistillationRecipeBuilder addInput(Fluid fluid, int amount){
		return addInput(new FluidStack(fluid, amount));
	}
	
	public DistillationRecipeBuilder addInput(FluidStack fluidStack){
		return addFluid("input", fluidStack);
	}
	
	public DistillationRecipeBuilder addFluids(String key, FluidStack... fluidStacks){
		return addWriter(jsonObject->{
			JsonArray array=new JsonArray();
			for(FluidStack stack:fluidStacks)
				array.add(ApiUtils.jsonSerializeFluidStack(stack));
			jsonObject.add(key, array);
		});
	}
	
	public DistillationRecipeBuilder addItems(String key, ItemStack... itemStacks){
		return addWriter(jsonObject->{
			JsonArray array=new JsonArray();
			for(ItemStack stack:itemStacks){
				array.add(serializeItemStack(stack));
			}
			jsonObject.add(key, array);
		});
	}
	
	@Override
	public void build(Consumer<IFinishedRecipe> out, ResourceLocation id){
		if(this.byproducts.size()>0){
			addWriter(jsonObject->{
				final JsonArray main=new JsonArray();
				this.byproducts.forEach(by->main.add(serializerItemStackWithChance(by)));
				jsonObject.add("byproducts", main);
				this.byproducts.clear();
			});
		}
		super.build(out, id);
	}
	
	public static Tuple<ItemStack, Double> deserializeItemStackWithChance(JsonObject jsonObject){
		if(jsonObject.has("chance") && jsonObject.has("item")){
			double chance=jsonObject.get("chance").getAsDouble();
			jsonObject.remove("chance");
			ItemStack stack=ShapedRecipe.deserializeItem(jsonObject);
			return new Tuple<ItemStack, Double>(stack, chance);
		}
		
		throw new IllegalArgumentException("Unexpected json object.");
	}
	
	private static final DistillationRecipeBuilder dummy=new DistillationRecipeBuilder();
	public static JsonObject serializerItemStackWithChance(@Nonnull Tuple<ItemStack, Double> tuple){
		JsonObject itemJson=dummy.serializeItemStack(tuple.getA());
		itemJson.addProperty("chance", tuple.getB().toString());
		return itemJson;
	}
}
