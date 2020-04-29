package flaxbeard.immersivepetroleum.common.compat.crafttweaker;

import com.google.common.collect.Lists;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

@ZenClass("mods.immersivepetroleum.Reservoir")
@ZenRegister
public class ReservoirTweaker
{

	@ZenMethod
	public static void registerReservoir(String name, ILiquidStack fluid, int minSize, int maxSize, int replenishRate, int weight, int[] dimBlacklist, int[] dimWhitelist, String[] biomeBlacklist, String[] biomeWhitelist)
	{
		List<String> biomeBlacklistList = Lists.newArrayList();
		List<String> biomeWhitelistList = Lists.newArrayList();

		if (name.isEmpty())
		{
			CraftTweakerAPI.logError("Reservoir name can not be empty string!");
		}
		else if (minSize <= 0)
		{
			CraftTweakerAPI.logError("Reservoir minSize has to be at least 1mb!");
		}
		else if (maxSize < minSize)
		{
			CraftTweakerAPI.logError("Reservoir maxSize can not be smaller than minSize!");
		}
		else if (weight <= 1)
		{
			CraftTweakerAPI.logError("Reservoir weight has to be greater than or equal to 1!");
		}

		String rFluid = fluid.getName();

		PumpjackHandler.ReservoirType res = PumpjackHandler.addReservoir(name, rFluid, minSize, maxSize, replenishRate, weight);

		for (int x = 0; x < biomeBlacklist.length; x++)
		{
			String string = biomeBlacklist[x];
			if (string == null || string.isEmpty())
			{
				CraftTweakerAPI.logError("String '" + biomeBlacklist[x] + "' in biomeBlacklist is either Empty or Null");
			}
			else
			{
				biomeBlacklistList.add(biomeBlacklist[x]);
			}
		}

		for (int x = 0; x < biomeWhitelist.length; x++)
		{
			String string = biomeWhitelist[x];
			if (string == null || string.isEmpty())
			{
				CraftTweakerAPI.logError("String '" + biomeWhitelist[x] + "' in biomeBlacklist is either Empty or Null");
			}
			else
			{
				biomeWhitelistList.add(biomeWhitelist[x]);
			}
		}

		res.dimensionBlacklist = dimBlacklist;
		res.dimensionWhitelist = dimWhitelist;
		res.biomeBlacklist = biomeBlacklistList.toArray(new String[0]);
		res.biomeWhitelist = biomeWhitelistList.toArray(new String[0]);

		CraftTweakerAPI.logInfo("Added Reservoir Type: " + name);
	}
}