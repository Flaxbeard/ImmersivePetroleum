package flaxbeard.immersivepetroleum.api.crafting.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;

public class ReservoirTypeBuilder extends IEFinishedRecipe<ReservoirTypeBuilder>{
	private String fluid;
	private int fluidMinimum;
	private int fluidMaximum;
	private int fluidTrace;
	private int weight;
	private final JsonArray dimWhitelist = new JsonArray();
	private final JsonArray dimBlacklist = new JsonArray();
	private final JsonArray bioWhitelist = new JsonArray();
	private final JsonArray bioBlacklist = new JsonArray();
	private ReservoirTypeBuilder(){
		super(Serializers.RESERVOIR_SERIALIZER.get());
		addWriter(writer -> writer.addProperty("fluid", this.fluid));
		addWriter(writer -> writer.addProperty("fluidminimum", this.fluidMinimum));
		addWriter(writer -> writer.addProperty("fluidcapacity", this.fluidMaximum));
		addWriter(writer -> writer.addProperty("fluidtrace", this.fluidTrace));
		addWriter(writer -> writer.addProperty("weight", this.weight));
		
		// Writes both even if there is nothing. Helpful for datapack makers.
		addWriter(writer -> {
			JsonObject dimension = new JsonObject();
			dimension.add("whitelist", this.dimWhitelist);
			dimension.add("blacklist", this.dimBlacklist);
			writer.add("dimension", dimension);
		});
		addWriter(writer -> {
			JsonObject biome = new JsonObject();
			biome.add("whitelist", this.bioWhitelist);
			biome.add("blacklist", this.bioBlacklist);
			writer.add("biome", biome);
		});
	}
	
	/**
	 * Creates a new ReservoirType builder instance.
	 * 
	 * @param name The name of the reservoir
	 * @return new builder instance
	 */
	public static ReservoirTypeBuilder builder(String name){
		return new ReservoirTypeBuilder().addWriter(writer -> writer.addProperty("name", name));
	}
	
	/**
	 * Creates a new ReservoirType builder instance. This is a shorthand.
	 * 
	 * @param name The name of the reservoir
	 * @param fluid The type of fluid it holds
	 * @param min The minimum amount of fluid the reservoir can hold
	 * @param max The capacity of the reservoir
	 * @param trace Trace amount of the fluid after being depleted
	 * @param weight chance for this reservoir to spawn
	 * @return
	 */
	public static ReservoirTypeBuilder builder(String name, Fluid fluid, double min, double max, double trace, int weight){
		return builder(name).setFluid(fluid).min(min).max(max).trace(trace).weight(weight);
	}
	
	/**
	 * Sets the fluid for this Reservoir.
	 * 
	 * @param fluid The fluid to set.
	 * @return self
	 */
	public ReservoirTypeBuilder setFluid(Fluid fluid){
		this.fluid = fluid.getRegistryName().toString();
		return this;
	}
	
	/**
	 * Sets minimum <code>amount</code> of fluid for this Reservoir. <code><pre>
	 * 1.000 = 1 Bucket
	 * 0.001 = 1 Millibucket
	 * </pre></code>
	 * 
	 * @param amount The amount to set.
	 * @return self
	 */
	public ReservoirTypeBuilder min(double amount){
		this.fluidMinimum = (int) Math.floor(amount * 1000D);
		return this;
	}
	
	/**
	 * Sets maximum/capacity <code>amount</code> of fluid for this Reservoir.
	 * <code><pre>
	 * 1.000 = 1 Bucket
	 * 0.001 = 1 Millibucket
	 * </pre></code>
	 * 
	 * @param amount The amount to set.
	 * @return self
	 */
	public ReservoirTypeBuilder max(double amount){
		this.fluidMaximum = (int) Math.floor(amount * 1000D);
		return this;
	}
	
	/**
	 * Replenish <code>amount</code> per tick. <code><pre>
	 * 1.000 = 1 Bucket
	 * 0.001 = 1 Millibucket
	 * </pre></code>
	 * 
	 * @param amount The amount to set.
	 * @return self
	 */
	public ReservoirTypeBuilder trace(double amount){
		this.fluidTrace = (int) Math.floor(amount * 1000D);
		return this;
	}
	
	/**
	 * Reservoir Weight
	 * 
	 * @param amount
	 * @return
	 */
	public ReservoirTypeBuilder weight(int amount){
		this.weight = amount;
		return this;
	}
	
	/**
	 * Dimension check for this Reservior. Only one may be added per instance,
	 * but not both.
	 * 
	 * @param isBlacklist Marks this as a blacklist when true. Whilelist
	 *        otherwise.
	 * @param dimensions Dimensions to blacklist/whitelist
	 * @return self
	 * @throws IllegalArgumentException when attempting to add a blacklist and
	 *         whitelist in the same instance.
	 */
	public ReservoirTypeBuilder addDimensions(boolean isBlacklist, ResourceLocation... dimensions){
		if(isBlacklist){
			if(dimensions != null && dimensions.length > 0){
				if(this.dimWhitelist.size() > 0)
					throw new IllegalArgumentException("Cannot set a whitelist and blacklist at the same time.");
				
				// Avoid duplicates
				for(ResourceLocation rl:dimensions){
					if(rl != null && !this.dimBlacklist.contains(new JsonPrimitive(rl.toString()))){
						this.dimBlacklist.add(rl.toString());
					}
				}
			}
		}else{
			if(dimensions != null && dimensions.length > 0){
				if(this.dimBlacklist.size() > 0)
					throw new IllegalArgumentException("Cannot set a whitelist and blacklist at the same time.");
				
				// Avoid duplicates
				for(ResourceLocation rl:dimensions){
					if(rl != null && !this.dimWhitelist.contains(new JsonPrimitive(rl.toString()))){
						this.dimWhitelist.add(rl.toString());
					}
				}
			}
		}
		return this;
	}
	
	/**
	 * Biome check for this Reservior. Only one may be added per instance, but
	 * not both.
	 * 
	 * @param isBlacklist Marks this as a blacklist when true. Whilelist
	 *        otherwise.
	 * @param biomes Biome to blacklist/whitelist
	 * @return self
	 * @throws IllegalArgumentException when attempting to add a blacklist and
	 *         whitelist in the same instance.
	 */
	public ReservoirTypeBuilder addBiomes(boolean isBlacklist, ResourceLocation... biomes){
		if(isBlacklist){
			if(biomes != null && biomes.length > 0){
				if(this.bioWhitelist.size() > 0)
					throw new IllegalArgumentException("Cannot set a whitelist and blacklist at the same time.");
				
				// Avoid duplicates
				for(ResourceLocation rl:biomes){
					if(rl != null && !this.bioBlacklist.contains(new JsonPrimitive(rl.toString()))){
						this.bioBlacklist.add(rl.toString());
					}
				}
			}
		}else{
			if(biomes != null && biomes.length > 0){
				if(this.bioBlacklist.size() > 0)
					throw new IllegalArgumentException("Cannot set a whitelist and blacklist at the same time.");
				
				// Avoid duplicates
				for(ResourceLocation rl:biomes){
					if(rl != null && !this.bioWhitelist.contains(new JsonPrimitive(rl.toString()))){
						this.bioWhitelist.add(rl.toString());
					}
				}
			}
		}
		return this;
	}
}
