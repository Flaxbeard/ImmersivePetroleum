package flaxbeard.immersivepetroleum.api.crafting;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import flaxbeard.immersivepetroleum.common.IPSaveData;

/**
 * @author BluSunrize - 03.06.2015
 *
 * The Handler for the Excavator. Chunk->Ore calculation is done here, as is registration
 */
public class PumpjackHandler
{
	public static HashMap<DimensionChunkCoords, Long> timeCache = new HashMap<DimensionChunkCoords, Long>();
	public static HashMap<DimensionChunkCoords, OilWorldInfo> oilCache = new HashMap<DimensionChunkCoords, OilWorldInfo>();
	public static double oilChance = 100;
	public static int replenishAmount = 10;
	public static int minDeposit = 1000;
	public static int maxDeposit = 5000;
	public static int[] dimensionBlacklist = new int[0];
	
	private static int depositSize = 1;

	public static int getOilAmount(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return 0;
		OilWorldInfo info = getOilWorldInfo(world, chunkX, chunkZ);
		if (info == null || (info.capacity == 0) || (info.oil == 0 && replenishAmount == 0))
			return 0;

		return info.oil;
	}
	
	public static boolean canGetResidualOil(World world, int chunkX, int chunkZ)
	{
		OilWorldInfo info = getOilWorldInfo(world, chunkX, chunkZ);
		
		if (info == null || (info.capacity == 0) || (info.oil == 0 && replenishAmount == 0))
			return false;
		
		DimensionChunkCoords coords = new DimensionChunkCoords(world.provider.getDimension(), chunkX / depositSize, chunkZ / depositSize);

		Long l = timeCache.get(coords);
		if (l == null)
		{
			timeCache.put(coords, world.getTotalWorldTime());
			return true;
		}
		
		long lastTime = world.getTotalWorldTime();
		timeCache.put(coords, world.getTotalWorldTime());
		return lastTime != l;
	}
	
	public static OilWorldInfo getOilWorldInfo(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return null;

		int dim = world.provider.getDimension();
		DimensionChunkCoords coords = new DimensionChunkCoords(dim, chunkX / depositSize, chunkZ / depositSize);
		OilWorldInfo worldInfo = oilCache.get(coords);
		if(worldInfo == null)
		{
			Random r = world.getChunkFromChunkCoords(chunkX / depositSize, chunkZ / depositSize).getRandomWithSeed(90210); // Antidote
			double dd = r.nextDouble();
			boolean empty = dd > oilChance;
			double size = r.nextDouble();
			
			int capacity = 0;
			
			if(!empty && isValidDimension(dim))
			{
				capacity = (int) (size * (maxDeposit - minDeposit)) + minDeposit;
			}
			worldInfo = new OilWorldInfo();
			worldInfo.capacity = capacity;
			worldInfo.oil = capacity;
			oilCache.put(coords, worldInfo);
		}
		return worldInfo;
	}
	
	private static boolean isValidDimension(int dim)
	{
		if(dimensionBlacklist != null && dimensionBlacklist.length > 0)
		{
			for(int black : dimensionBlacklist)
				if(dim == black)
					return false;
		}
		return true;
	}
	public static void depleteOil(World world, int chunkX, int chunkZ, int amount)
	{
		OilWorldInfo info = getOilWorldInfo(world,chunkX,chunkZ);
		info.oil = Math.max(0, info.oil - amount);
		IPSaveData.setDirty(world.provider.getDimension());
	}


	public static class OilWorldInfo
	{
		public int capacity;
		public int oil;

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("capacity", capacity);
			tag.setInteger("oil", oil);
			return tag;
		}
		public static OilWorldInfo readFromNBT(NBTTagCompound tag)
		{
			OilWorldInfo info = new OilWorldInfo();
			info.capacity = tag.getInteger("capacity");
			info.oil = tag.getInteger("oil");
			return info;
		}
	}
}