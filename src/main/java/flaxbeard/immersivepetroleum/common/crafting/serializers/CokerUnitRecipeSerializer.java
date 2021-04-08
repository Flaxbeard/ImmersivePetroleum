package flaxbeard.immersivepetroleum.common.crafting.serializers;

import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class CokerUnitRecipeSerializer extends IERecipeSerializer<CokerUnitRecipe>{
	
	@Override
	public CokerUnitRecipe readFromJson(ResourceLocation recipeId, JsonObject json){
		FluidTagInput outputFluid = FluidTagInput.deserialize(JSONUtils.getJsonObject(json, "resultfluid"));
		FluidTagInput inputFluid = FluidTagInput.deserialize(JSONUtils.getJsonObject(json, "inputfluid"));
		
		ItemStack outputItem = readOutput(json.get("result"));
		IngredientWithSize inputItem = IngredientWithSize.deserialize(JSONUtils.getJsonObject(json, "input"));
		
		int energy = JSONUtils.getInt(json, "energy");
		int time = JSONUtils.getInt(json, "time");
		
		return new CokerUnitRecipe(recipeId, outputItem, outputFluid, inputItem, inputFluid, energy, time);
	}
	
	@Override
	public CokerUnitRecipe read(ResourceLocation recipeId, PacketBuffer buffer){
		IngredientWithSize inputItem = IngredientWithSize.read(buffer);
		ItemStack outputItem = buffer.readItemStack();
		
		FluidTagInput inputFluid = FluidTagInput.read(buffer);
		FluidTagInput outputFluid = FluidTagInput.read(buffer);
		
		int energy = buffer.readInt();
		int time = buffer.readInt();
		
		return new CokerUnitRecipe(recipeId, outputItem, outputFluid, inputItem, inputFluid, energy, time);
	}
	
	@Override
	public void write(PacketBuffer buffer, CokerUnitRecipe recipe){
		recipe.inputItem.write(buffer);
		buffer.writeItemStack(recipe.outputItem);
		
		recipe.inputFluid.write(buffer);
		recipe.outputFluid.write(buffer);
		
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.getTotalProcessTime());
	}
	
	@Override
	public ItemStack getIcon(){
		return new ItemStack(IPContent.Multiblock.cokerunit);
	}
}
