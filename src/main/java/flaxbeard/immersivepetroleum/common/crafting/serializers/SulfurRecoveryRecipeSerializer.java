package flaxbeard.immersivepetroleum.common.crafting.serializers;

import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.api.crafting.SulfurRecoveryRecipe;
import flaxbeard.immersivepetroleum.api.crafting.builders.DistillationRecipeBuilder;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;

public class SulfurRecoveryRecipeSerializer extends IERecipeSerializer<SulfurRecoveryRecipe>{
	
	@Override
	public SulfurRecoveryRecipe readFromJson(ResourceLocation id, JsonObject json){
		FluidStack output = ApiUtils.jsonDeserializeFluidStack(JSONUtils.getJsonObject(json, "result"));
		FluidTagInput inputFluid0 = FluidTagInput.deserialize(JSONUtils.getJsonObject(json, "input"));
		FluidTagInput inputFluid1 = null;
		
		if(json.has("secondary_input")){
			inputFluid1 = FluidTagInput.deserialize(JSONUtils.getJsonObject(json, "secondary_input"));
		}
		
		Tuple<ItemStack, Double> itemWithChance = DistillationRecipeBuilder.deserializeItemStackWithChance(json.get("secondary_result").getAsJsonObject());
		
		int energy = JSONUtils.getInt(json, "energy");
		int time = JSONUtils.getInt(json, "time");
		
		return new SulfurRecoveryRecipe(id, output, itemWithChance.getA(), inputFluid0, inputFluid1, itemWithChance.getB(), energy, time);
	}
	
	@Override
	public SulfurRecoveryRecipe read(ResourceLocation id, PacketBuffer buffer){
		ItemStack outputItem = buffer.readItemStack();
		double chance = buffer.readDouble();
		
		FluidStack output = buffer.readFluidStack();
		FluidTagInput inputFluid0 = FluidTagInput.read(buffer);
		FluidTagInput inputFluid1 = FluidTagInput.read(buffer);
		
		int energy = buffer.readInt();
		int time = buffer.readInt();
		
		return new SulfurRecoveryRecipe(id, output, outputItem, inputFluid0, inputFluid1, chance, energy, time);
	}
	
	@Override
	public void write(PacketBuffer buffer, SulfurRecoveryRecipe recipe){
		buffer.writeItemStack(recipe.outputItem);
		buffer.writeDouble(recipe.chance);
		
		buffer.writeFluidStack(recipe.output);
		recipe.inputFluid.write(buffer);
		recipe.inputFluidSecondary.write(buffer);
		
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.getTotalProcessTime());
	}
	
	@Override
	public ItemStack getIcon(){
		return new ItemStack(IPContent.Multiblock.hydrotreater);
	}
}
