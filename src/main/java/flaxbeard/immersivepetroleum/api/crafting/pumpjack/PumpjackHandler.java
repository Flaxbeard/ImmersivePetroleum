package flaxbeard.immersivepetroleum.api.crafting.pumpjack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistries;

public class PumpjackHandler{
	public static Map<ResourceLocation, ReservoirType> reservoirs = new HashMap<>();
	
	private static Map<ResourceLocation, Map<ResourceLocation, Integer>> totalWeightMap = new HashMap<>();
	
	public static Map<DimensionChunkCoords, Long> timeCache = new HashMap<>();
	public static Map<DimensionChunkCoords, ReservoirWorldInfo> reservoirsCache = new HashMap<>();
	
	private static int depositSize = 1;
	
	/**
	 * Gets amount of fluid in a specific chunk's reservoir in mB
	 *
	 * @param world The world to test
	 * @param chunkX X coordinate of desired chunk
	 * @param chunkZ Z coordinate of desired chunk
	 * @return mB of fluid in the given reservoir
	 */
	public static int getFluidAmount(World world, int chunkX, int chunkZ){
		if(world.isRemote)
			return 0;
		
		ReservoirWorldInfo info = getOrCreateOilWorldInfo(world, chunkX, chunkZ);
		if(info == null || (info.capacity == 0) || info.getType() == null || info.getType().fluidLocation == null || (info.current == 0 && info.getType().replenishRate == 0))
			return 0;
		
		return info.current;
	}
	
	/**
	 * Gets Fluid type in a specific chunk's reservoir
	 *
	 * @param world The world to test
	 * @param chunkX X coordinate of desired chunk
	 * @param chunkZ Z coordinate of desired chunk
	 * @return Fluid in given reservoir (or null if none)
	 */
	public static Fluid getFluid(World world, int chunkX, int chunkZ){
		if(world.isRemote)
			return null;
		
		ReservoirWorldInfo info = getOrCreateOilWorldInfo(world, chunkX, chunkZ);
		
		if(info == null || info.getType() == null){
			return null;
		}else{
			return info.getType().getFluid();
		}
	}
	
	/**
	 * Gets the mB/tick of fluid that is produced "residually" in the chunk (can
	 * be extracted while empty)
	 *
	 * @param world The world to test
	 * @param chunkX X coordinate of desired chunk
	 * @param chunkZ Z coordinate of desired chunk
	 * @return mB of fluid that can be extracted "residually"
	 */
	public static int getResidualFluid(World world, int chunkX, int chunkZ){
		ReservoirWorldInfo info = getOrCreateOilWorldInfo(world, chunkX, chunkZ);
		
		if(info == null || info.getType() == null || info.getType().fluidLocation == null || (info.capacity == 0) || (info.current == 0 && info.getType().replenishRate == 0))
			return 0;
		
		DimensionChunkCoords coords = new DimensionChunkCoords(world.getDimensionKey(), chunkX / depositSize, chunkZ / depositSize);
		
		Long l = timeCache.get(coords);
		if(l == null){
			timeCache.put(coords, world.getGameTime());
			return info.getType().replenishRate;
		}
		
		long lastTime = world.getGameTime();
		timeCache.put(coords, world.getGameTime());
		return lastTime != l ? info.getType().replenishRate : 0;
	}
	
	/**
	 * Gets the OilWorldInfo object associated with the given chunk
	 * 
	 * @param world The world to retrieve
	 * @param chunkX X coordinate of desired chunk
	 * @param chunkZ Z coordinate of desired chunk
	 * @return The OilWorldInfo corresponding w/ given chunk
	 */
	public static ReservoirWorldInfo getOrCreateOilWorldInfo(World world, int chunkX, int chunkZ){
		return getOrCreateOilWorldInfo(world, new DimensionChunkCoords(world.getDimensionKey(), chunkX, chunkZ), false);
	}
	
	/**
	 * Gets the OilWorldInfo object associated with the given chunk
	 *
	 * @param world The world to retrieve
	 * @param coords Coordinates of desired chunk
	 * @param force Force creation on an empty chunk
	 * @return The OilWorldInfo corresponding w/ given chunk
	 */
	public static ReservoirWorldInfo getOrCreateOilWorldInfo(World world, DimensionChunkCoords coords, boolean force){
		if(world.isRemote)
			return null;
		
		ReservoirWorldInfo worldInfo = reservoirsCache.get(coords);
		if(worldInfo == null){
			ReservoirType res = null;
			
			Random r = SharedSeedRandom.createSlimeChunkSpawningSeed(coords.x, coords.z, ((ISeedReader) world).getSeed(), 90210L);
			boolean empty = (r.nextDouble() > IPServerConfig.EXTRACTION.reservoir_chance.get());
			double size = r.nextDouble();
			int query = r.nextInt();
			
			ImmersivePetroleum.log.debug("Empty? {}. Forced? {}. Size: {}, Query: {}", empty ? "Yes" : "No", force ? "Yes" : "No", size, query);
			
			if(!empty || force){
				ResourceLocation biome = world.getBiome(new BlockPos(coords.x << 4, 64, coords.z << 4)).getRegistryName();
				ResourceLocation dimension = coords.dimension.getLocation();
				ImmersivePetroleum.log.debug(coords.dimension.getLocation());
				
				int totalWeight = getTotalWeight(dimension, biome);
				ImmersivePetroleum.log.debug("Total Weight: " + totalWeight);
				if(totalWeight > 0){
					int weight = Math.abs(query % totalWeight);
					for(ReservoirType type:reservoirs.values()){
						if(type.isValidDimension(dimension) && type.isValidBiome(biome)){
							weight -= type.weight;
							if(weight < 0){
								res = type;
								break;
							}
						}
					}
				}
			}
			
			int capacity = 0;
			
			if(res != null){
				ImmersivePetroleum.log.debug("Using: {}", res.name);
				
				capacity = (int) ((res.maxSize - res.minSize) * size + res.minSize);
			}
			
			ImmersivePetroleum.log.debug("Capacity: {}", capacity);
			
			worldInfo = new ReservoirWorldInfo();
			worldInfo.capacity = capacity;
			worldInfo.current = capacity;
			worldInfo.type = res;
			
			ImmersivePetroleum.log.debug("Storing {} for {}", worldInfo, coords);
			reservoirsCache.put(coords, worldInfo);
		}
		
		return worldInfo;
	}
	
	/**
	 * Depletes fluid from a given chunk
	 *
	 * @param world World whose chunk to drain
	 * @param chunkX Chunk x
	 * @param chunkZ Chunk z
	 * @param amount Amount of fluid in mB to drain
	 */
	public static void depleteFluid(World world, int chunkX, int chunkZ, int amount){
		ReservoirWorldInfo info = getOrCreateOilWorldInfo(world, chunkX, chunkZ);
		info.current = Math.max(info.current - amount, 0);
		IPSaveData.setDirty();
	}
	
	/**
	 * Gets the total weight of reservoir types for the given dimension ID and
	 * biome type
	 *
	 * @param dimension The dimension to check
	 * @param biome The biome to check
	 * @return The total weight associated with the dimension/biome pair
	 */
	public static int getTotalWeight(ResourceLocation dimension, ResourceLocation biome){
		if(!totalWeightMap.containsKey(dimension)){
			totalWeightMap.put(dimension, new HashMap<>());
		}
		
		Map<ResourceLocation, Integer> dimMap = totalWeightMap.get(dimension);
		
		if(dimMap.containsKey(biome))
			return dimMap.get(biome);
		
		int totalWeight = 0;
		for(ReservoirType type:reservoirs.values()){
			if(type.isValidDimension(dimension) && type.isValidBiome(biome))
				totalWeight += type.weight;
		}
		return totalWeight;
	}
	
	/**
	 * Adds a reservoir type to the pool of valid reservoirs
	 * 
	 * @param id The "recipeId" of the reservoir type
	 * @param reservoir The reservoir type to add
	 * @return
	 */
	public static ReservoirType addReservoir(ResourceLocation id, ReservoirType reservoir){
		reservoirs.put(id, reservoir);
		return reservoir;
	}
	
	public static void recalculateChances(){
		totalWeightMap.clear();
	}
	
	public static class ReservoirType extends IESerializableRecipe{
		public static final IRecipeType<ReservoirType> TYPE = IRecipeType.register(ImmersivePetroleum.MODID + ":reservoirtype");
		
		public String name;
		public ResourceLocation fluidLocation;
		
		public int minSize;
		public int maxSize;
		public int replenishRate;
		
		public int weight;
		
		// ForgeRegistries.MOD_DIMENSIONS.getValue(resourceIn)
		public List<ResourceLocation> dimWhitelist = new ArrayList<>(0);
		public List<ResourceLocation> dimBlacklist = new ArrayList<>(0);
		
		// ForgeRegistries.BIOMES.getValue(resourceIn)
		public List<ResourceLocation> bioWhitelist = new ArrayList<>(0);
		public List<ResourceLocation> bioBlacklist = new ArrayList<>(0);
		
		private Fluid fluid;
		
		/**
		 * Creates a new reservoir.
		 * 
		 * @param name The name of this reservoir type
		 * @param id The "recipeId" of this reservoir
		 * @param fluidLocation The registry name of the fluid this reservoir is
		 *        containing
		 * @param minSize Minimum amount of fluid in this reservoir
		 * @param maxSize Maximum amount of fluid in this reservoir
		 * @param traceAmount Leftover fluid amount after depletion
		 * @param weight The weight for this reservoir
		 */
		public ReservoirType(String name, ResourceLocation id, ResourceLocation fluidLocation, int minSize, int maxSize, int traceAmount, int weight){
			this(name, id, ForgeRegistries.FLUIDS.getValue(fluidLocation), minSize, maxSize, traceAmount, weight);
		}
		
		/**
		 * Creates a new reservoir.
		 * 
		 * @param name The name of this reservoir type
		 * @param id The "recipeId" of this reservoir
		 * @param fluid The fluid this reservoir is containing
		 * @param minSize Minimum amount of fluid in this reservoir
		 * @param maxSize Maximum amount of fluid in this reservoir
		 * @param traceAmount Leftover fluid amount after depletion
		 * @param weight The weight for this reservoir
		 */
		public ReservoirType(String name, ResourceLocation id, Fluid fluid, int minSize, int maxSize, int traceAmount, int weight){
			super(ItemStack.EMPTY, TYPE, id);
			this.name = name;
			this.fluidLocation = fluid.getRegistryName();
			this.fluid = fluid;
			this.replenishRate = traceAmount;
			this.minSize = minSize;
			this.maxSize = maxSize;
			this.weight = weight;
		}
		
		public ReservoirType(CompoundNBT nbt){
			super(ItemStack.EMPTY, TYPE, new ResourceLocation(nbt.getString("id")));
			
			this.name = nbt.getString("name");
			
			this.fluidLocation = new ResourceLocation(nbt.getString("fluid"));
			this.fluid = ForgeRegistries.FLUIDS.getValue(this.fluidLocation);
			
			this.minSize = nbt.getInt("minSize");
			this.maxSize = nbt.getInt("maxSize");
			this.replenishRate = nbt.getInt("replenishRate");
			
			this.dimWhitelist = toList(nbt.getList("dimensionWhitelist", NBT.TAG_STRING));
			this.dimBlacklist = toList(nbt.getList("dimensionBlacklist", NBT.TAG_STRING));
			
			this.bioWhitelist = toList(nbt.getList("biomeWhitelist", NBT.TAG_STRING));
			this.bioBlacklist = toList(nbt.getList("biomeBlacklist", NBT.TAG_STRING));
		}
		
		public CompoundNBT writeToNBT(){
			return writeToNBT(new CompoundNBT());
		}
		
		public CompoundNBT writeToNBT(CompoundNBT nbt){
			nbt.putString("name", this.name);
			nbt.putString("id", this.id.toString());
			nbt.putString("fluid", this.fluidLocation.toString());
			
			nbt.putInt("minSize", this.minSize);
			nbt.putInt("maxSize", this.maxSize);
			nbt.putInt("replenishRate", this.replenishRate);
			
			nbt.put("dimensionWhitelist", toNbt(this.dimWhitelist));
			nbt.put("dimensionBlacklist", toNbt(this.dimBlacklist));
			
			nbt.put("biomeWhitelist", toNbt(this.bioWhitelist));
			nbt.put("biomeBlacklist", toNbt(this.bioBlacklist));
			
			return nbt;
		}
		
		public boolean addDimension(boolean blacklist, ResourceLocation... names){
			return addDimension(blacklist, Arrays.asList(names));
		}
		
		public boolean addDimension(boolean blacklist, List<ResourceLocation> names){
			if(blacklist){
				return this.dimBlacklist.addAll(names);
			}else{
				return this.dimWhitelist.addAll(names);
			}
		}
		
		public boolean addBiome(boolean blacklist, ResourceLocation... names){
			return addBiome(blacklist, Arrays.asList(names));
		}
		
		public boolean addBiome(boolean blacklist, List<ResourceLocation> names){
			if(blacklist){
				return this.bioBlacklist.addAll(names);
			}else{
				return this.bioWhitelist.addAll(names);
			}
		}
		
		public boolean isValidDimension(@Nonnull World world){
			if(world == null)
				return false;
			
			return isValidDimension(world.getDimensionKey().getRegistryName());
		}
		
		public boolean isValidDimension(@Nonnull ResourceLocation rl){
			if(this.dimWhitelist.size() > 0){
				return this.dimWhitelist.contains(rl);
				
			}else if(this.dimBlacklist.size() > 0){
				return !this.dimBlacklist.contains(rl);
			}
			
			return true;
		}
		
		public boolean isValidBiome(@Nonnull Biome biome){
			return isValidBiome(biome.getRegistryName());
		}
		
		public boolean isValidBiome(@Nonnull ResourceLocation rl){
			if(this.bioWhitelist.size() > 0){
				return this.bioWhitelist.contains(rl);
				
			}else if(this.bioBlacklist.size() > 0){
				return !this.bioBlacklist.contains(rl);
			}
			
			return true;
		}
		
		@Override
		protected IERecipeSerializer<ReservoirType> getIESerializer(){
			return Serializers.RESERVOIR_SERIALIZER.get();
		}
		
		@Override
		public ItemStack getRecipeOutput(){
			return ItemStack.EMPTY;
		}
		
		public Fluid getFluid(){
			return this.fluid;
		}
		
		@Override
		public String toString(){
			return this.writeToNBT().toString();
		}
		
		private List<ResourceLocation> toList(ListNBT nbtList){
			List<ResourceLocation> list = new ArrayList<>(0);
			if(nbtList.size() > 0){
				for(INBT tag:nbtList)
					if(tag instanceof StringNBT)
						list.add(new ResourceLocation(((StringNBT) tag).getString()));
			}
			return list;
		}
		
		private ListNBT toNbt(List<ResourceLocation> list){
			ListNBT nbtList = new ListNBT();
			for(ResourceLocation rl:list){
				nbtList.add(StringNBT.valueOf(rl.toString()));
			}
			return nbtList;
		}
	}
}
