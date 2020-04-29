package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricatedTileInfo;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.OilWorldInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

public class IPSaveData extends WorldSavedData
{
	//	private static HashMap<Integer, IESaveData> INSTANCE = new HashMap<Integer, IESaveData>();
	private static IPSaveData INSTANCE;
	public static final String dataName = "ImmersivePetroleum-SaveData";

	public IPSaveData(String s)
	{
		super(s);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagList oilList = nbt.getTagList("oilInfo", 10);
		PumpjackHandler.oilCache.clear();
		for (int i = 0; i < oilList.tagCount(); i++)
		{
			NBTTagCompound tag = oilList.getCompoundTagAt(i);
			DimensionChunkCoords coords = DimensionChunkCoords.readFromNBT(tag);
			if (coords != null)
			{
				OilWorldInfo info = OilWorldInfo.readFromNBT(tag.getCompoundTag("info"));
				PumpjackHandler.oilCache.put(coords, info);
			}
		}

		NBTTagList lubricatedList = nbt.getTagList("lubricated", 10);
		LubricatedHandler.lubricatedTiles.clear();
		for (int i = 0; i < lubricatedList.tagCount(); i++)
		{
			NBTTagCompound tag = lubricatedList.getCompoundTagAt(i);
			LubricatedTileInfo info = LubricatedTileInfo.readFromNBT(tag);
			LubricatedHandler.lubricatedTiles.add(info);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList oilList = new NBTTagList();
		for (Map.Entry<DimensionChunkCoords, OilWorldInfo> e : PumpjackHandler.oilCache.entrySet())
		{
			if (e.getKey() != null && e.getValue() != null)
			{
				NBTTagCompound tag = e.getKey().writeToNBT();
				tag.setTag("info", e.getValue().writeToNBT());
				oilList.appendTag(tag);
			}
		}
		nbt.setTag("oilInfo", oilList);

		NBTTagList lubricatedList = new NBTTagList();
		for (LubricatedTileInfo info : LubricatedHandler.lubricatedTiles)
		{
			if (info != null)
			{
				NBTTagCompound tag = info.writeToNBT();
				lubricatedList.appendTag(tag);
			}
		}
		nbt.setTag("lubricated", lubricatedList);

		return nbt;
	}


	public static void setDirty(int dimension)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && INSTANCE != null)
			INSTANCE.markDirty();
	}

	public static void setInstance(int dimension, IPSaveData in)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			INSTANCE = in;
	}

}