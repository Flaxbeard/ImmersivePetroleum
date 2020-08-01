package flaxbeard.immersivepetroleum.api.crafting;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;


public class PumpjackHandler
{
	public static LinkedHashMap<ReservoirType, Integer> reservoirList = new LinkedHashMap<>();
	private static Map<Integer, HashMap<ResourceLocation, Integer>> totalWeightMap = new HashMap<>();

	public static HashMap<DimensionChunkCoords, Long> timeCache = new HashMap<>();
	public static HashMap<DimensionChunkCoords, OilWorldInfo> oilCache = new HashMap<>();
	public static double oilChance = 100;

	private static int depositSize = 1;

	/**
	 * Gets amount of fluid in a specific chunk's reservoir in mB
	 *
	 * @param world  The world to test
	 * @param chunkX X coordinate of desired chunk
	 * @param chunkZ Z coordinate of desired chunk
	 * @return mB of fluid in the given reservoir
	 */
	public static int getFluidAmount(World world, int chunkX, int chunkZ)
	{
		if (world.isRemote)
			return 0;
		OilWorldInfo info = getOilWorldInfo(world, chunkX, chunkZ);
		if (info == null || (info.capacity == 0) || info.getType() == null || info.getType().fluidLocation == null || (info.current == 0 && info.getType().replenishRate == 0))
			return 0;

		return info.current;
	}

	/**
	 * Gets Fluid type in a specific chunk's reservoir
	 *
	 * @param world  The world to test
	 * @param chunkX X coordinate of desired chunk
	 * @param chunkZ Z coordinate of desired chunk
	 * @return Fluid in given reservoir (or null if none)
	 */
	public static Fluid getFluid(World world, int chunkX, int chunkZ)
	{
		if (world.isRemote)
			return null;

		OilWorldInfo info = getOilWorldInfo(world, chunkX, chunkZ);

		if (info == null || info.getType() == null)
		{
			return null;
		}
		else
		{
			return info.getType().getFluid();
		}
	}

	/**
	 * Gets the mB/tick of fluid that is produced "residually" in the chunk (can be extracted while empty)
	 *
	 * @param world  The world to test
	 * @param chunkX X coordinate of desired chunk
	 * @param chunkZ Z coordinate of desired chunk
	 * @return mB of fluid that can be extracted "residually"
	 */
	public static int getResidualFluid(World world, int chunkX, int chunkZ)
	{
		OilWorldInfo info = getOilWorldInfo(world, chunkX, chunkZ);

		if (info == null || info.getType() == null || info.getType().fluidLocation == null || (info.capacity == 0) || (info.current == 0 && info.getType().replenishRate == 0))
			return 0;

		DimensionChunkCoords coords = new DimensionChunkCoords(world.getDimension().getType(), chunkX / depositSize, chunkZ / depositSize);

		Long l = timeCache.get(coords);
		if (l == null)
		{
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
	 * @param world  The world to retrieve
	 * @param chunkX X coordinate of desired chunk
	 * @param chunkZ Z coordinate of desired chunk
	 * @return The OilWorldInfo corresponding w/ given chunk
	 */
	public static OilWorldInfo getOilWorldInfo(World world, int chunkX, int chunkZ){
		if(world.isRemote) return null;
		
		int dim = world.getDimension().getType().getId();
		DimensionChunkCoords coords = new DimensionChunkCoords(world.getDimension().getType(), chunkX / depositSize, chunkZ / depositSize);
		
		OilWorldInfo worldInfo = oilCache.get(coords);
		if(worldInfo == null){
			ReservoirType res = null;
			
			// TODO Don't think you can access the random generator of a chunk anymore?
			// Random(coords.asLong()+90210L) Is temporary until i figure shit out
			Random r = new Random(coords.asLong()*90210L); //world.getChunk(chunkX / depositSize, chunkZ / depositSize).getRandomWithSeed(90210); // Antidote
			double dd = r.nextDouble();
			boolean empty = dd > oilChance;
			double size = r.nextDouble();
			int query = r.nextInt();
			
			if(!empty){
				Biome biome = world.getBiomeBody(new BlockPos(chunkX << 4, 64, chunkZ << 4));
				int totalWeight = getTotalWeight(dim, biome);
				if(totalWeight > 0){
					int weight = Math.abs(query % totalWeight);
					for(Map.Entry<ReservoirType, Integer> e:reservoirList.entrySet()){
						if(e.getKey().validDimension(dim) && e.getKey().validBiome(biome)){
							weight -= e.getValue();
							if(weight < 0){
								res = e.getKey();
								break;
							}
						}
					}
				}
			}
			
			int capacity = 0;
			
			if(res != null){
				capacity = (int) (size * (res.maxSize - res.minSize)) + res.minSize;
			}
			
			worldInfo = new OilWorldInfo();
			worldInfo.capacity = capacity;
			worldInfo.current = capacity;
			worldInfo.type = res;
			oilCache.put(coords, worldInfo);
		}
		
		return worldInfo;
	}

	/**
	 * Depletes fluid from a given chunk
	 *
	 * @param world  World whose chunk to drain
	 * @param chunkX Chunk x
	 * @param chunkZ Chunk z
	 * @param amount Amount of fluid in mB to drain
	 */
	public static void depleteFluid(World world, int chunkX, int chunkZ, int amount)
	{
		OilWorldInfo info = getOilWorldInfo(world, chunkX, chunkZ);
		info.current = Math.max(0, info.current - amount);
		IPSaveData.setDirty();
	}

	/**
	 * Gets the total weight of reservoir types for the given dimension ID and biome type
	 *
	 * @param dim   The dimension id to check
	 * @param biome The biome type to check
	 * @return The total weight associated with the dimension/biome pair
	 */
	public static int getTotalWeight(int dim, Biome biome)
	{
		if (!totalWeightMap.containsKey(dim))
		{
			totalWeightMap.put(dim, new HashMap<>());
		}

		Map<ResourceLocation, Integer> dimMap = totalWeightMap.get(dim);
		ResourceLocation biomeName = getBiomeName(biome);

		if (dimMap.containsKey(biomeName))
		{
			return dimMap.get(biomeName);
		}

		int totalWeight = 0;
		for (Map.Entry<ReservoirType, Integer> e : reservoirList.entrySet())
		{
			if (e.getKey().validDimension(dim) && e.getKey().validBiome(biome))
				totalWeight += e.getValue();
		}
		dimMap.put(biomeName, totalWeight);
		return totalWeight;
	}

	/**
	 * Adds a reservoir type to the pool of valid reservoirs
	 *
	 * @param name          The name of the reservoir type
	 * @param fluid         The String fluidid of the fluid for this reservoir
	 * @param minSize       The minimum reservoir size, in mB
	 * @param maxSize       The maximum reservoir size, in mB
	 * @param replenishRate The rate at which fluid can be drained from this reservoir when empty, in mB/tick
	 * @param weight        The weight for this reservoir to spawn
	 * @return The created ReservoirType
	 */
	public static ReservoirType addReservoir(String name, String fluid, int minSize, int maxSize, int replenishRate, int weight)
	{
		ReservoirType mix = new ReservoirType(name, fluid, minSize, maxSize, replenishRate);
		reservoirList.put(mix, weight);
		return mix;
	}

	public static void recalculateChances(boolean mutePackets){
		totalWeightMap.clear();
		if(!mutePackets){
			HashMap<ReservoirType, Integer> packetMap = new HashMap<>();

			for(Entry<ReservoirType, Integer> e:PumpjackHandler.reservoirList.entrySet())
				if(e.getKey() != null && e.getValue() != null)
					packetMap.put(e.getKey(), e.getValue());
			// TODO Figure out how to send to All
			//IPPacketHandler.INSTANCE.sendToAll(new MessageReservoirListSync(packetMap));
		}
	}

	private static HashMap<Biome, ResourceLocation> biomeNames = new HashMap<>();

	/**
	 * Get the biome name associated with a given biome
	 *
	 * @param biome The biome to get the name
	 * @return The biome's name
	 */
	public static ResourceLocation getBiomeName(Biome biome)
	{
		if (!biomeNames.containsKey(biome))
		{
			biomeNames.put(biome, biome.getRegistryName());
		}
		return biomeNames.get(biome);
	}

	public static String convertConfigName(String str)
	{
		return str.replace(" ", "").toUpperCase();
	}

	public static String getTagDisplayName(String tag)
	{
		return tag.charAt(0) + tag.substring(1).toLowerCase();
	}

	public static String getBiomeDisplayName(String str){
		String ret = "";
		for(int i = 0;i < str.length();i++){
			char c = str.charAt(i);
			if(Character.isUpperCase(c) && i != 0 && str.charAt(i - 1) != ' '){
				ret = ret + " " + c;
			}else{
				ret = ret + c;
			}
		}
		return ret;
	}

	public static class ReservoirType{
		public String name;
		public ResourceLocation fluidLocation;
		
		public int minSize;
		public int maxSize;
		public int replenishRate;
		
		public int[] dimensionWhitelist = new int[0];
		public int[] dimensionBlacklist = new int[0];
		
		public String[] biomeWhitelist = new String[0];
		public String[] biomeBlacklist = new String[0];
		
		private Fluid fluid;
		
		@Deprecated
		public ReservoirType(String name, String fluidName, int minSize, int maxSize, int replenishRate){
		}
		
		public ReservoirType(String name, ResourceLocation fluidLocation, int minSize, int maxSize, int replenishRate){
			this.name = name;
			this.fluidLocation = fluidLocation;
			this.replenishRate = replenishRate;
			this.minSize = minSize;
			this.maxSize = maxSize;
		}
		
		public ReservoirType(CompoundNBT nbt){
			this.name = nbt.getString("name");
			this.fluidLocation = new ResourceLocation(nbt.getString("fluidname"));
			
			this.minSize = nbt.getInt("minSize");
			this.maxSize = nbt.getInt("maxSize");
			this.replenishRate = nbt.getInt("replenishRate");
			
			this.dimensionWhitelist = nbt.getIntArray("dimensionWhitelist");
			this.dimensionBlacklist = nbt.getIntArray("dimensionBlacklist");
			
			ListNBT wl = (ListNBT) nbt.get("biomeWhitelist");
			this.biomeWhitelist = new String[wl.size()];
			for(int i = 0;i < wl.size();i++){
				this.biomeWhitelist[i] = wl.getString(i);
			}
			
			ListNBT bl = (ListNBT) nbt.get("biomeBlacklist");
			this.biomeBlacklist = new String[bl.size()];
			for(int i = 0;i < bl.size();i++){
				this.biomeBlacklist[i] = bl.getString(i);
			}
		}
		
		public Fluid getFluid(){
			if(fluidLocation == null) return null;
			
			if(fluid == null){
				if((this.fluid = ForgeRegistries.FLUIDS.getValue(this.fluidLocation)) == null){
					// Backup if the above fails to get anything.
					// If even the backup fails then there's something wrong.
					Collection<Entry<ResourceLocation, Fluid>> fluids = ForgeRegistries.FLUIDS.getEntries();
					fluids.forEach(fluid -> {
						if(fluid.getKey().toString().contains(ReservoirType.this.fluidLocation.toString())){
							ReservoirType.this.fluid = fluid.getValue();
							return; // Kill this if we found something
						}
					});
				}
			}
			
			return fluid;
		}
		
		// ForgeRegistries.MOD_DIMENSIONS.getValue(resourceIn);
		
		// TODO Use ResourceLocation for dimension
		public boolean validDimension(int dim){
			if(dimensionWhitelist != null && dimensionWhitelist.length > 0){
				for(int white:dimensionWhitelist){
					if(dim == white) return true;
				}
				return false;
				
			}else if(dimensionBlacklist != null && dimensionBlacklist.length > 0){
				for(int black:dimensionBlacklist){
					if(dim == black) return false;
				}
				return true;
			}
			return true;
		}
		
		public boolean validBiome(Biome biome){
			if(biome == null) return false;
			
			if(biomeWhitelist != null && biomeWhitelist.length > 0){
				for(String white:biomeWhitelist){
					for(BiomeDictionary.Type biomeType:BiomeDictionary.getTypes(biome)){
						if(convertConfigName(white).equals(biomeType.getName())) return true;
					}
				}
				return false;
				
			}else if(biomeBlacklist != null && biomeBlacklist.length > 0){
				for(String black:biomeBlacklist){
					for(BiomeDictionary.Type biomeType:BiomeDictionary.getTypes(biome)){
						if(convertConfigName(black).equals(biomeType.getName())) return false;
					}
				}
				return true;
			}
			return true;
		}
		
		public CompoundNBT writeToNBT(){
			CompoundNBT tag = new CompoundNBT();
			tag.putString("name", this.name);
			
			tag.putString("fluid", this.fluidLocation.toString());
			
			tag.putInt("minSize", this.minSize);
			tag.putInt("maxSize", this.maxSize);
			tag.putInt("replenishRate", this.replenishRate);
			
			tag.putIntArray("dimensionWhitelist", this.dimensionWhitelist);
			tag.putIntArray("dimensionBlacklist", this.dimensionBlacklist);
			
			ListNBT wl = new ListNBT();
			for(String s:this.biomeWhitelist){
				wl.add(new StringNBT(s));
			}
			tag.put("biomeWhitelist", wl);
			
			ListNBT bl = new ListNBT();
			for(String s:this.biomeBlacklist){
				bl.add(new StringNBT(s));
			}
			tag.put("biomeBlacklist", bl);
			
			return tag;
		}
		
		@Deprecated
		public static ReservoirType readFromNBT(CompoundNBT nbt){
			return new ReservoirType(nbt);
		}
	}
	
	public static class OilWorldInfo{
		public ReservoirType type;
		public ReservoirType overrideType;
		public int capacity;
		public int current;
		
		public ReservoirType getType(){
			return (overrideType == null) ? type : overrideType;
		}
		
		public CompoundNBT writeToNBT(){
			CompoundNBT tag = new CompoundNBT();
			tag.putInt("capacity", capacity);
			tag.putInt("oil", current);
			if(type != null){
				tag.putString("type", type.name);
			}
			if(overrideType != null){
				tag.putString("overrideType", overrideType.name);
			}
			return tag;
		}
		
		public static OilWorldInfo readFromNBT(CompoundNBT tag){
			OilWorldInfo info = new OilWorldInfo();
			info.capacity = tag.getInt("capacity");
			info.current = tag.getInt("oil");
			
			if(tag.contains("type")){
				String s = tag.getString("type");
				for(ReservoirType res:reservoirList.keySet()){
					if(s.equalsIgnoreCase(res.name)) info.type = res;
				}
			}else if(info.current > 0){
				for(ReservoirType res:reservoirList.keySet()){
					if(res.name.equalsIgnoreCase("oil")) info.type = res;
				}
				
				if(info.type == null){
					return null;
				}
			}
			
			if(tag.contains("overrideType")){
				String s = tag.getString("overrideType");
				for(ReservoirType res:reservoirList.keySet()){
					if(s.equalsIgnoreCase(res.name)) info.overrideType = res;
				}
			}
			
			return info;
		}
	}
}