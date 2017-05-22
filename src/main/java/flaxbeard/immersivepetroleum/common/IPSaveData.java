package flaxbeard.immersivepetroleum.common;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.common.IESaveData;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.OilWorldInfo;

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
		for(int i = 0; i < oilList.tagCount(); i++)
		{
			NBTTagCompound tag = oilList.getCompoundTagAt(i);
			DimensionChunkCoords coords = DimensionChunkCoords.readFromNBT(tag);
			if(coords!=null)
			{
				OilWorldInfo info = OilWorldInfo.readFromNBT(tag.getCompoundTag("info"));
				PumpjackHandler.oilCache.put(coords, info);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList oilList = new NBTTagList();
		for(Map.Entry<DimensionChunkCoords, OilWorldInfo> e: PumpjackHandler.oilCache.entrySet())
			if(e.getKey() != null && e.getValue() != null)
			{
				NBTTagCompound tag = e.getKey().writeToNBT();
				tag.setTag("info", e.getValue().writeToNBT());
				oilList.appendTag(tag);
			}
		nbt.setTag("oilInfo", oilList);
		
		return nbt;
	}


	public static void setDirty(int dimension)
	{
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER && INSTANCE!=null)
			INSTANCE.markDirty();
	}
	
	public static void setInstance(int dimension, IPSaveData in)
	{
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
			INSTANCE=in;
	}

}