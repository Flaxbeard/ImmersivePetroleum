package flaxbeard.immersivepetroleum.api.crafting;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.MessageReservoirListSync;


public class PumpjackHandler
{
	public static LinkedHashMap<ReservoirType, Integer> reservoirList = new LinkedHashMap<ReservoirType, Integer>();
	private static Map<Integer, HashMap<String, Integer>> totalWeightMap = new HashMap<Integer, HashMap<String, Integer>>();

	public static HashMap<DimensionChunkCoords, Long> timeCache = new HashMap<DimensionChunkCoords, Long>();
	public static HashMap<DimensionChunkCoords, OilWorldInfo> oilCache = new HashMap<DimensionChunkCoords, OilWorldInfo>();
	public static double oilChance = 100;
	
	private static int depositSize = 1;

	public static int getFluidAmount(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return 0;
		OilWorldInfo info = getOilWorldInfo(world, chunkX, chunkZ);
		if (info == null || (info.capacity == 0) || info.getType() == null || info.getType().fluid == null || (info.current == 0 && info.getType().replenishRate == 0))
			return 0;

		return info.current;
	}
	
	public static Fluid getFluid(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return null;
		
		OilWorldInfo info = getOilWorldInfo(world, chunkX, chunkZ);
		
		if (info.getType() == null)
		{
			return null;
		}
		else
		{
			return info.getType().getFluid();
		}
	}
	
	public static int getResidualFluid(World world, int chunkX, int chunkZ)
	{
		OilWorldInfo info = getOilWorldInfo(world, chunkX, chunkZ);
		
		if (info == null || info.getType() == null || info.getType().fluid == null || (info.capacity == 0) || (info.current == 0 && info.getType().replenishRate == 0))
			return 0;
		
		DimensionChunkCoords coords = new DimensionChunkCoords(world.provider.getDimension(), chunkX / depositSize, chunkZ / depositSize);

		Long l = timeCache.get(coords);
		if (l == null)
		{
			timeCache.put(coords, world.getTotalWorldTime());
			return info.getType().replenishRate;
		}
		
		long lastTime = world.getTotalWorldTime();
		timeCache.put(coords, world.getTotalWorldTime());
		return lastTime != l ? info.getType().replenishRate : 0;
	}
	
	public static OilWorldInfo getOilWorldInfo(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return null;

		int dim = world.provider.getDimension();
		DimensionChunkCoords coords = new DimensionChunkCoords(dim, chunkX / depositSize, chunkZ / depositSize);

		OilWorldInfo worldInfo = oilCache.get(coords);
		if (worldInfo == null)
		{
			ReservoirType res = null;

			Random r = world.getChunkFromChunkCoords(chunkX / depositSize, chunkZ / depositSize).getRandomWithSeed(90210); // Antidote
			double dd = r.nextDouble();
			boolean empty = dd > oilChance;
			double size = r.nextDouble();
			int query = r.nextInt();

			if (!empty)
			{
				Biome biome = world.getBiomeForCoordsBody(new BlockPos(chunkX << 4, 64, chunkZ << 4));
				int weight = Math.abs(query % getTotalWeight(dim, biome));
				for (Map.Entry<ReservoirType, Integer> e : reservoirList.entrySet())
					if (e.getKey().validDimension(dim) && e.getKey().validBiome(biome))
					{
						weight -= e.getValue();
						if (weight < 0)
						{
							res = e.getKey();
							break;
						}
					}
			}
			
			int capacity = 0;
			
			if (res != null)
			{
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

	public static void depleteFluid(World world, int chunkX, int chunkZ, int amount)
	{
		OilWorldInfo info = getOilWorldInfo(world,chunkX,chunkZ);
		info.current = Math.max(0, info.current - amount);
		IPSaveData.setDirty(world.provider.getDimension());
	}


	public static class OilWorldInfo
	{
		public ReservoirType type;
		public ReservoirType overrideType;
		public int capacity;
		public int current;
		
		public ReservoirType getType()
		{
			return (overrideType == null) ? type : overrideType;
		}
		
		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("capacity", capacity);
			tag.setInteger("oil", current);
			if (type != null)
			{
				tag.setString("type", type.name);
			}
			if (overrideType != null)
			{
				tag.setString("overrideType", overrideType.name);
			}
			return tag;
		}
		
		public static OilWorldInfo readFromNBT(NBTTagCompound tag)
		{
			OilWorldInfo info = new OilWorldInfo();
			info.capacity = tag.getInteger("capacity");
			info.current = tag.getInteger("oil");
			
			if (tag.hasKey("type"))
			{
				String s = tag.getString("type");
				for (ReservoirType res : reservoirList.keySet())
					if (s.equalsIgnoreCase(res.name))
						info.type = res;
			}
			else if (info.current > 0)
			{
				for (ReservoirType res : reservoirList.keySet())
					if (res.name.equalsIgnoreCase("oil"))
						info.type = res;
			
				if (info.type == null)
				{
					return null;
				}
			}
			
			if (tag.hasKey("overrideType"))
			{
				String s = tag.getString("overrideType");
				for (ReservoirType res : reservoirList.keySet())
					if (s.equalsIgnoreCase(res.name))
						info.overrideType = res;
			}
							
			return info;
		}
	}
	
	public static int getTotalWeight(int dim, Biome biome)
	{
		if (!totalWeightMap.containsKey(dim))
		{
			totalWeightMap.put(dim, new HashMap<String, Integer>());
		}
		
		Map<String, Integer> dimMap = totalWeightMap.get(dim);
		String biomeName = getBiomeName(biome);
		
		if (dimMap.containsKey(biomeName))
		{
			return dimMap.get(biomeName);
		}
		
		int totalWeight = 0;
		for(Map.Entry<ReservoirType, Integer> e : reservoirList.entrySet())
		{
			if (e.getKey().validDimension(dim) && e.getKey().validBiome(biome))
				totalWeight += e.getValue();
		}
		dimMap.put(biomeName, totalWeight);
		return totalWeight;
	}
	
	public static ReservoirType addReservoir(String name, String fluid, int minSize, int maxSize, int replenishRate, int weight)
	{
		ReservoirType mix = new ReservoirType(name, fluid, minSize, maxSize, replenishRate);
		reservoirList.put(mix, weight);
		return mix;
	}
	
	public static void recalculateChances(boolean mutePackets)
	{
		totalWeightMap.clear();
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !mutePackets)
		{
			HashMap<ReservoirType, Integer> packetMap = new HashMap<ReservoirType,Integer>();
			for (Entry<ReservoirType,Integer> e: PumpjackHandler.reservoirList.entrySet())
				if (e.getKey() != null && e.getValue() != null)
					packetMap.put(e.getKey(), e.getValue());
			IPPacketHandler.INSTANCE.sendToAll(new MessageReservoirListSync(packetMap));
		}
	}
	
	public static String getBiomeName(Biome biome)
	{
		return biome.getBiomeName().replace(" ", "").replace("_", "").toLowerCase();
	}
	
	public static String convertConfigName(String str)
	{
		return str.replace(" ", "").toUpperCase();
	}
	
	public static String getBiomeDisplayName(String str)
	{
		String ret = "";
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			if (Character.isUpperCase(c) && i != 0 && str.charAt(i - 1) != ' ')
			{
				ret = ret + " " + c;
			}
			else
			{
				ret = ret + c;
			}
		}
		return ret;
	}
	
	public static class ReservoirType
	{
		public String name;
		public String fluid;
		
		public int minSize;
		public int maxSize;
		public int replenishRate;
				
		public int[] dimensionWhitelist = new int[0];
		public int[] dimensionBlacklist = new int[0];
		
		public String[] biomeWhitelist = new String[0];
		public String[] biomeBlacklist = new String[0];
		
		private Fluid f;

		public ReservoirType(String name, String fluid, int minSize, int maxSize, int replenishRate)
		{
			this.name = name;
			this.fluid = fluid;
			this.minSize = minSize;
			this.maxSize = maxSize;
			this.replenishRate = replenishRate;
		}
		
		public Fluid getFluid()
		{
			if (fluid == null) return null;
			
			if (f == null)
			{		
				f = FluidRegistry.getFluid(fluid);
			}
			
			return f;
		}

		public boolean validDimension(int dim)
		{
			if (dimensionWhitelist != null && dimensionWhitelist.length > 0)
			{
				for (int white : dimensionWhitelist)
					if (dim == white)
						return true;
				return false;
			}
			else if (dimensionBlacklist != null && dimensionBlacklist.length > 0)
			{
				for (int black : dimensionBlacklist)
					if (dim == black)
						return false;
				return true;
			}
			return true;
		}
		
		public boolean validBiome(Biome biome)
		{
			if (biome == null) return false;
			if (biomeWhitelist != null && biomeWhitelist.length > 0)
			{
				for (String white : biomeWhitelist)
				{
					for (BiomeDictionary.Type biomeType : BiomeDictionary.getTypes(biome))
						if (convertConfigName(white).equals(biomeType.getName()))
							return true;
				}
				return false;
			}
			else if (biomeBlacklist != null && biomeBlacklist.length > 0)
			{
				for (String black : biomeBlacklist)
				{
					for (BiomeDictionary.Type biomeType : BiomeDictionary.getTypes(biome))
						if (convertConfigName(black).equals(biomeType.getName()))
							return false;
				}
				return true;
			}
			return true;
		}

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("name", this.name);
			
			tag.setString("fluid", fluid);
			
			tag.setInteger("minSize", minSize);
			tag.setInteger("maxSize", maxSize);
			tag.setInteger("replenishRate", replenishRate);

			tag.setIntArray("dimensionWhitelist", dimensionWhitelist);
			tag.setIntArray("dimensionBlacklist", dimensionBlacklist);
			
			NBTTagList wl = new NBTTagList();
			for (String s : biomeWhitelist)
			{
				wl.appendTag(new NBTTagString(s));
			}
			tag.setTag("biomeWhitelist", wl);
			
			NBTTagList bl = new NBTTagList();
			for (String s : biomeBlacklist)
			{
				bl.appendTag(new NBTTagString(s));
			}
			tag.setTag("biomeBlacklist", wl);
			
			return tag;
		}
		
		public static ReservoirType readFromNBT(NBTTagCompound tag)
		{
			String name = tag.getString("name");
			String fluid = tag.getString("fluid");
			
			int minSize = tag.getInteger("minSize");
			int maxSize = tag.getInteger("maxSize");
			int replenishRate = tag.getInteger("replenishRate");

			ReservoirType res = new ReservoirType(name, fluid, minSize, maxSize, replenishRate);
			
			res.dimensionWhitelist = tag.getIntArray("dimensionWhitelist");
			res.dimensionBlacklist = tag.getIntArray("dimensionBlacklist");
			
			NBTTagList wl = (NBTTagList) tag.getTag("biomeWhitelist");
			res.biomeWhitelist = new String[wl.tagCount()];
			for (int i = 0; i < wl.tagCount(); i++)
			{
				res.biomeWhitelist[i] = wl.getStringTagAt(i);
			}
			
			NBTTagList bl = (NBTTagList) tag.getTag("biomeBlacklist");
			res.biomeBlacklist = new String[bl.tagCount()];
			for (int i = 0; i < bl.tagCount(); i++)
			{
				res.biomeBlacklist[i] = bl.getStringTagAt(i);
			}
			
			
			return res;
		}
	}
}