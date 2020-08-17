package flaxbeard.immersivepetroleum.common.crafting.serializers;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class ReservoirTypeSerializer extends IERecipeSerializer<ReservoirType>{
	@Override
	public ReservoirType readFromJson(ResourceLocation recipeId, JsonObject json){
		String name=JSONUtils.getString(json, "name");
		ResourceLocation fluid=new ResourceLocation(JSONUtils.getString(json, "fluid"));
		int min=JSONUtils.getInt(json, "fluidminimum");
		int max=JSONUtils.getInt(json, "fluidcapacity");
		int trace=JSONUtils.getInt(json, "fluidtrace");
		int weight=JSONUtils.getInt(json, "weight");
		
		ReservoirType type=new ReservoirType(name, recipeId, fluid, min, max, trace, weight);
		
		if(JSONUtils.hasField(json, "dimension")){
			JsonObject dimension=JSONUtils.getJsonObject(json, "dimension");
			
			List<ResourceLocation> names=new ArrayList<>();
			boolean blacklist=false;
			if(JSONUtils.hasField(dimension, "whitelist")){
				blacklist=false;
				JsonArray array=JSONUtils.getJsonArray(dimension, "whitelist");
				for(JsonElement obj:array){
					names.add(new ResourceLocation(obj.getAsString()));
				}
				
			}else if(JSONUtils.hasField(dimension, "blacklist")){
				blacklist=true;
				JsonArray array=JSONUtils.getJsonArray(dimension, "blacklist");
				for(JsonElement obj:array){
					names.add(new ResourceLocation(obj.getAsString()));
				}
				
			}
			
			type.addDimension(blacklist, names);
			
		}else if(JSONUtils.hasField(json, "biome")){
			JsonObject biome=JSONUtils.getJsonObject(json, "biome");
			
			List<ResourceLocation> names=new ArrayList<>();
			boolean blacklist=false;
			if(JSONUtils.hasField(biome, "whitelist")){
				blacklist=true;
				JsonArray array=JSONUtils.getJsonArray(biome, "whitelist");
				for(JsonElement obj:array){
					names.add(new ResourceLocation(obj.getAsString()));
				}
				
			}else if(JSONUtils.hasField(biome, "blacklist")){
				blacklist=true;
				JsonArray array=JSONUtils.getJsonArray(biome, "blacklist");
				for(JsonElement obj:array){
					names.add(new ResourceLocation(obj.getAsString()));
				}
			}
			
			type.addBiome(blacklist, names);
		}
		
		return type;
	}
	
	@Override
	public ReservoirType read(ResourceLocation recipeId, PacketBuffer buffer){
		return new ReservoirType(buffer.readCompoundTag()); // Very convenient having the NBT stuff already.
	}
	
	@Override
	public void write(PacketBuffer buffer, ReservoirType recipe){
		buffer.writeCompoundTag(recipe.writeToNBT());
	}
	
	@Override
	public ItemStack getIcon(){
		return ItemStack.EMPTY;
	}
}
