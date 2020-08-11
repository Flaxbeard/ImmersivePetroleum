package flaxbeard.immersivepetroleum.common.crafting.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class DistillationRecipeSerializer extends IERecipeSerializer<DistillationRecipe>{
	@Override
	public DistillationRecipe readFromJson(ResourceLocation recipeId, JsonObject json){
		if(!json.has("input"))
			throw new com.google.gson.JsonSyntaxException("Missing \"input\" fluid in a distillation recipe.");
		if(!json.has("results"))
			throw new com.google.gson.JsonSyntaxException("Missing \"results\" array in a distillation recipe.");
		
		FluidStack input=ApiUtils.jsonDeserializeFluidStack(JSONUtils.getJsonObject(json, "input"));
		JsonArray fluidResults=JSONUtils.getJsonArray(json, "results");
		JsonArray itemResults=JSONUtils.getJsonArray(json, "byproducts");
		JsonArray chancesResults=JSONUtils.getJsonArray(json, "chances");
		
		FluidStack[] fluidOutput=new FluidStack[fluidResults.size()];
		for(int i=0;i<fluidOutput.length;i++)
			fluidOutput[i]=ApiUtils.jsonDeserializeFluidStack(fluidResults.get(i).getAsJsonObject());
		
		ItemStack[] byproducts=new ItemStack[itemResults.size()];
		for(int i=0;i<byproducts.length;i++)
			byproducts[i]=ShapedRecipe.deserializeItem(itemResults.get(i).getAsJsonObject());
		
		double[] chances=new double[chancesResults.size()];
		for(int i=0;i<chances.length;i++)
			chances[i]=chancesResults.getAsDouble();
		
		int energy=2048;
		if(json.has("energy"))
			energy=JSONUtils.getInt(json, "energy");
		
		int time=1;
		if(json.has("time")){
			time=JSONUtils.getInt(json, "time");
		}
		
		return new DistillationRecipe(recipeId, fluidOutput, byproducts, input, energy, time, chances);
	}
	
	@Override
	public DistillationRecipe read(ResourceLocation recipeId, PacketBuffer buffer){
		FluidStack[] fluidOutput=new FluidStack[buffer.readInt()];
		for(int i=0;i<fluidOutput.length;i++)
			fluidOutput[i]=buffer.readFluidStack();
		
		ItemStack[] byproducts=new ItemStack[buffer.readInt()];
		for(int i=0;i<byproducts.length;i++)
			byproducts[i]=buffer.readItemStack();
		
		double[] chances=new double[buffer.readInt()];
		for(int i=0;i<chances.length;i++)
			chances[i]=buffer.readDouble();
		
		FluidStack input=buffer.readFluidStack();
		int energy=buffer.readInt();
		int time=buffer.readInt();
		
		return new DistillationRecipe(recipeId, fluidOutput, byproducts, input, energy, time, chances);
	}
	
	@Override
	public void write(PacketBuffer buffer, DistillationRecipe recipe){
		buffer.writeInt(recipe.fluidOutput.length);
		for(FluidStack stack:recipe.fluidOutput)
			buffer.writeFluidStack(stack);
		
		buffer.writeInt(recipe.itemOutput.length);
		for(ItemStack stack:recipe.itemOutput)
			buffer.writeItemStack(stack);
		
		buffer.writeInt(recipe.chances.length);
		for(double d:recipe.chances)
			buffer.writeDouble(d);
		
		buffer.writeFluidStack(recipe.input);
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.getTotalProcessTime());
	}
	
	@Override
	public ItemStack getIcon(){
		return ItemStack.EMPTY; // TODO Icon for Distillation Recipes
	}
}
