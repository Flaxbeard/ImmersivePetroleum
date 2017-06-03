package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Configuration;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;

public class Config
{

	@net.minecraftforge.common.config.Config(modid=ImmersivePetroleum.MODID)
	public static class IPConfig
	{
		@Comment({"Display chunk border while holding Core Samples, default=true"})
		public static boolean sample_displayBorder = true;
		
		public static Reservoirs reservoirs = new Reservoirs();

		public static class Reservoirs
		{
			@Comment({"List of reservoir types. Format: name, fluid_name, min_mb_fluid, max_mb_fluid, mb_per_tick_replenish, weight, [dim_blacklist], [dim_whitelist], [biome_blacklist], [biome_whitelist]"})
			public static String[] reservoirs = new String[] {
					"aquifer, water, 5000000, 10000000, 3, 30, [], [0], [], []",
					"oil, oil, 1000000, 5000000, 3, 40, [1], [], [], []",
					"lava, lava, 250000, 1000000, 0, 30, [1], [], [], []"
			};
			
			@Comment({"The chance that a chunk contains a fluid reservoir, default=0.25"})
			public static float reservoir_chance = 0.5F;
		}

		public static Machines machines = new Machines();

		public static class Machines
		{
			//Multiblock Recipes
			@Comment({"A modifier to apply to the energy costs of every Distillation Tower recipe, default=1"})
			public static float distillationTower_energyModifier = 1;

			@Comment({"A modifier to apply to the time of every Distillation recipe. Can't be lower than 1, default=1"})
			public static float distillationTower_timeModifier = 1;
			

			//Other Multiblock machines
			@Comment({"The Flux the Pumpjack requires each tick to pump, default=1024"})
			public static int pumpjack_consumption = 1024;
			@Comment({"The amount of mB of oil a Pumpjack extracts per tick, default=5"})
			public static int pumpjack_speed = 5;
			
			@Comment({"Disable formation and manual page for Distillation Tower"})
			public static boolean disable_tower = false;
			@Comment({"Disable formation and manual page for Pumpjack"})
			public static boolean disable_pumpjack = false;
		}
		
		public static Tools tools = new Tools();
		
		public static class Tools
		{
			@Comment({"Disable crafting recipe and manual page for Projectors"})
			public static boolean disable_projector = false;
		}
	}

	static Configuration config;
	
	public static void addConfigReservoirs(String[] reservoirs)
	{
		for (int i = 0; i < reservoirs.length; i++)
	    {
			String str = reservoirs[i];
			String[] data = str.split(",");

			String name = null;
			String fluid = null;
			int min = 0;
			int max = 0;
			int replenish = 0;
			int weight = 0;
			List<Integer> dimBlacklist = new ArrayList<Integer>();
			List<Integer> dimWhitelist = new ArrayList<Integer>();
			List<String> biomeBlacklist = new ArrayList<String>();
			List<String> biomeWhitelist = new ArrayList<String>();

			String remain = str;
			
			boolean inParens = false;
			int index = 0;
			
			while (remain.indexOf(",") != -1)
			{
				int endPos = remain.indexOf(",");
				
				String current = remain.substring(0, endPos).trim();
				
				if (index == 0) name = current;
				else if (index == 1) fluid = current;
				else if (index == 2)
				{
					try
					{
						min = Integer.parseInt(current);
						if (min < 0)
						{
							throw new RuntimeException("Negative value for minimum mB fluid for reservoir " + (i + 1));
						}
					}
					catch (NumberFormatException e)
					{
						throw new RuntimeException("Invalid value for minimum mB fluid for reservoir " + (i + 1));
					}
				}
				else if (index == 3)
				{
					try
					{
						max = Integer.parseInt(current);
						if (max < 0)
						{
							throw new RuntimeException("Negative value for maximum mB fluid for reservoir " + (i + 1));
						}
					}
					catch (NumberFormatException e)
					{
						throw new RuntimeException("Invalid value for maximum mB fluid for reservoir " + (i + 1));
					}
				}
				else if (index == 4)
				{
					try
					{
						replenish = Integer.parseInt(current);
						if (replenish < 0)
						{
							throw new RuntimeException("Negative value for mB replenished per tick for reservoir " + (i + 1));
						}
					}
					catch (NumberFormatException e)
					{
						throw new RuntimeException("Invalid value for mB replenished per tick for reservoir " + (i + 1));
					}
				}
				else if (index == 5)
				{
					try
					{
						weight = Integer.parseInt(current);
						if (weight < 0)
						{
							throw new RuntimeException("Negative value for weight for reservoir " + (i + 1));
						}
					}
					catch (NumberFormatException e)
					{
						throw new RuntimeException("Invalid value for weight for reservoir " + (i + 1));
					}
				}
				else if (index == 6)
				{
					if (!inParens)
					{
						current = current.substring(1);
						inParens = true;
					}
					
					int cI = current.indexOf(",");
					int bI = current.indexOf("]");
					
					String value = current;
					if (bI >= 0 && (cI == -1 || bI < cI))
					{
						value = value.substring(0, bI);
						inParens = false;
					}
					if (value.length() > 0)
					{
						try
						{
							int dim = Integer.parseInt(value);
							dimBlacklist.add(dim);
						}
						catch (NumberFormatException e)
						{
							throw new RuntimeException(value + "Invalid blacklist dimension for reservoir " + (i + 1));
						}
					}
				}
				else if (index == 7)
				{
					if (!inParens)
					{
						current = current.substring(1);
						inParens = true;
					}
					
					int cI = current.indexOf(",");
					int bI = current.indexOf("]");
					
					String value = current;
					if (bI >= 0 && (cI == -1 || bI < cI))
					{
						value = value.substring(0, bI);
						inParens = false;
					}
					if (value.length() > 0)
					{
						try
						{
							int dim = Integer.parseInt(value);
							dimWhitelist.add(dim);
						}
						catch (NumberFormatException e)
						{
							throw new RuntimeException("Invalid whitelist dimension for reservoir " + (i + 1));
						}
					}
				}
				else if (index == 8)
				{
					if (!inParens)
					{
						current = current.substring(1);
						inParens = true;
					}
					
					int cI = current.indexOf(",");
					int bI = current.indexOf("]");
					
					String value = current;
					if (bI >= 0 && (cI == -1 || bI < cI))
					{
						value = value.substring(0, bI);
						inParens = false;
					}
					if (value.length() > 0)
					{
						biomeBlacklist.add(PumpjackHandler.convertConfigName(value.trim()));
					}
				}
				
				
				remain = remain.substring(endPos + 1);
				if (!inParens) index++;
			}
			
			String current = remain.trim();
			
			if (!inParens)
			{
				current = current.substring(1);
				inParens = true;
			}
			
			int cI = current.indexOf(",");
			int bI = current.indexOf("]");
			
			String value = current;
			if (cI == -1 || bI < cI)
			{
				value = value.substring(0, bI);
				inParens = false;
			}
			if (value.length() > 0)
			{
				biomeWhitelist.add(PumpjackHandler.convertConfigName(value.trim()));
			}
			
			ReservoirType res = PumpjackHandler.addReservoir(name, fluid, min, max, replenish, weight);
			res.dimensionWhitelist = ArrayUtils.toPrimitive((Integer[]) dimWhitelist.toArray(new Integer[0]));
			res.dimensionBlacklist = ArrayUtils.toPrimitive((Integer[]) dimBlacklist.toArray(new Integer[0]));
			res.biomeWhitelist = (String[]) biomeWhitelist.toArray(new String[0]);
			res.biomeBlacklist = (String[]) biomeBlacklist.toArray(new String[0]);
			
			System.out.println("Added resevoir type " + name);
	    }
	}
}
