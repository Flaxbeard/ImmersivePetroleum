package flaxbeard.immersivepetroleum.common;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Config
{

	@net.minecraftforge.common.config.Config(modid = ImmersivePetroleum.MODID)
	public static class IPConfig
	{
		@Comment({"Display chunk border while holding Core Samples, default=true"})
		public static boolean sample_displayBorder = true;

		public static Extraction extraction;

		public static class Extraction
		{
			@Comment({"List of reservoir types. Format: name, fluid_name, min_mb_fluid, max_mb_fluid, mb_per_tick_replenish, weight, [dim_blacklist], [dim_whitelist], [biome_dict_blacklist], [biome_dict_whitelist]"})
			public static String[] reservoirs = new String[]{
					"aquifer, water, 5000000, 10000000, 6, 30, [], [0], [], []",
					"oil, oil, 2500000, 15000000, 6, 40, [1], [], [], []",
					"lava, lava, 250000, 1000000, 0, 30, [1], [], [], []"
			};

			@Comment({"The chance that a chunk contains a fluid reservoir, default=0.5"})
			public static float reservoir_chance = 0.5F;


			@Comment({"The Flux the Pumpjack requires each tick to pump, default=1024"})
			public static int pumpjack_consumption = 1024;

			@Comment({"The amount of mB of oil a Pumpjack extracts per tick, default=15"})
			public static int pumpjack_speed = 15;

			@Comment({"Require a pumpjack to have pipes built down to Bedrock, default=false"})
			public static boolean req_pipes = false;

			@Comment({"Number of ticks between checking for pipes below pumpjack if required, default=100 (5 secs)"})
			public static int pipe_check_ticks = 100;
		}

		public static Refining refining;

		public static class Refining
		{
			@Comment({"A modifier to apply to the energy costs of every Distillation Tower recipe, default=1"})
			public static float distillationTower_energyModifier = 1;

			@Comment({"A modifier to apply to the time of every Distillation recipe. Can't be lower than 1, default=1"})
			public static float distillationTower_timeModifier = 1;

			@Comment({"Distillation Tower recipes. Format: power_cost, input_name, input_mb -> output1_name, output1_mb, output2_name, output2_mb"})
			public static String[] towerRecipes = new String[]{
					"2048, oil, 75 -> lubricant, 9, diesel, 27, gasoline, 39"
			};
			@Comment({"Distillation Tower byproducts. Need one for each recipe. Multiple solid outputs for a single recipe can be separated by semicolons. Format: item_name, stack_size, metadata, percent_chance"})
			public static String[] towerByproduct = new String[]{
					"immersivepetroleum:material, 1, 0, 7"
			};
		}

		public static Generation generation;

		public static class Generation
		{
			@Comment({"List of Portable Generator fuels. Format: fluid_name, mb_used_per_tick, flux_produced_per_tick"})
			public static String[] fuels = new String[]{
					"gasoline, 5, 256"
			};

		}

		public static Miscellaneous misc;

		public static class Miscellaneous
		{
			@Comment({"List of Motorboat fuels. Format: fluid_name, mb_used_per_tick"})
			public static String[] boat_fuels = new String[]{
					"gasoline, 1"
			};

			@Comment({"Automatically unlock IP recipes for new players, default=true"})
			public static boolean autounlock_recipes = true;
		}

		public static Tools tools;

		public static class Tools
		{

		}
	}

	static Configuration config;

	/**
	 * Loads all distillation recipes from config strings
	 *
	 * @param recipes    All recipe strings
	 * @param byproducts All byproduct strings
	 */
	public static void addDistillationRecipes(String[] recipes, String[] byproducts)
	{
		if (recipes.length != byproducts.length)
			throw new RuntimeException("Mismatch in number of distillation tower config recipes and byproducts");

		for (int i = 0; i < recipes.length; i++)
		{
			String byproductString = byproducts[i];
			String recipeString = recipes[i];
			if (recipeString.isEmpty()) continue;

			addDistillationRecipe(i + 1, recipeString, byproductString);
		}
	}

	/**
	 * Attempts to parse an integer from the given String, throwing exceptions if things go poorly
	 *
	 * @param text          The String to parse
	 * @param canBeZero     Whether the integer config can be zero
	 * @param canBeNegative Whether the integer config can be negative
	 * @param termName      The name of what's being read, for output purposes
	 * @return The parsed value
	 */
	private static int attemptParseInteger(String text, boolean canBeZero, boolean canBeNegative, String termName)
	{
		try
		{
			int value = Integer.parseInt(text);

			if (!canBeNegative && value < 0)
			{
				throw new RuntimeException("Negative value for " + termName);
			}
			else if (!canBeZero && value == 0)
			{
				throw new RuntimeException("Zero value for " + termName);
			}

			return value;
		} catch (NumberFormatException e)
		{
			throw new RuntimeException("Invalid value for " + termName);
		}
	}

	/**
	 * Attempts to parse an float from the given String, throwing exceptions if things go poorly
	 *
	 * @param text          The String to parse
	 * @param canBeZero     Whether the float config can be zero
	 * @param canBeNegative Whether the float config can be negative
	 * @param termName      The name of what's being read, for output purposes
	 * @return The parsed value
	 */
	private static float attemptParseFloat(String text, boolean canBeZero, boolean canBeNegative, String termName)
	{
		try
		{
			float value = Float.parseFloat(text);

			if (!canBeNegative && value < 0)
			{
				throw new RuntimeException("Negative value for " + termName);
			}
			else if (!canBeZero && value == 0)
			{
				throw new RuntimeException("Zero value for " + termName);
			}

			return value;
		} catch (NumberFormatException e)
		{
			throw new RuntimeException("Invalid value for " + termName);
		}
	}

	/**
	 * Parses one segment of a distillation output fluid config
	 *
	 * @param recipeNum     The recipe number of this recipe
	 * @param termIndex     The index of this term in the fluid recipe
	 * @param text          The text to parse
	 * @param outputs       A list of current fluid output names
	 * @param outputAmounts A list of current fluid output amounts
	 */
	private static void parseDistillationOutput(int recipeNum, int termIndex, String text, List<String> outputs, List<Integer> outputAmounts)
	{
		if (termIndex % 2 == 1)
		{
			outputs.add(text);
		}
		else
		{
			int num = attemptParseInteger(text, false, false, "distillation tower output for recipe " + recipeNum);
			outputAmounts.add(num);
		}
	}

	/**
	 * Parses a String associated with a distillation byproduct and returns the byproduct ItemStack and chance
	 *
	 * @param recipeNum       The recipe number of this recipe
	 * @param byproductString The String associated with this byproduct
	 * @return A tuple of the desired ItemStack and chance, as a Float
	 */
	private static Tuple<ItemStack, Float> parseDistillationByproduct(int recipeNum, String byproductString)
	{
		String itemName = null;
		int amount = 0;
		int meta = 0;
		float chance = 0;

		String current = "";

		String remain = byproductString;

		int termIndex = 0;

		while (remain.contains(","))
		{
			int endPos = remain.indexOf(",");

			current = remain.substring(0, endPos).trim();

			if (termIndex == 0) itemName = current;
			else if (termIndex == 1)
			{
				amount = attemptParseInteger(current, false, false, "distillation byproduct stacksize " + recipeNum);
			}
			else if (termIndex == 2)
			{
				meta = attemptParseInteger(current, true, false, "distillation byproduct meta " + recipeNum);
			}

			remain = remain.substring(endPos + 1);
			termIndex++;
		}

		current = remain.trim();

		chance = attemptParseFloat(current, true, false, "distillation byproduct chance " + recipeNum);

		Item item;
		try
		{
			item = CommandBase.getItemByText(null, itemName);
		} catch (NumberInvalidException e)
		{
			throw new RuntimeException("Item " + itemName + " does not exist for distillation byproduct " + recipeNum);
		}

		ItemStack stack = new ItemStack(item, amount, meta);

		return new Tuple<>(stack, chance);
	}

	/**
	 * Adds a distillation recipe based on recipe + byproduct string, throwing exceptions if invalid
	 *
	 * @param recipeNum       The recipe number (for exception output)
	 * @param recipeString    The string representing the recipe to add
	 * @param byproductString The string representing the byproducts for this recipe
	 */
	private static void addDistillationRecipe(int recipeNum, String recipeString, String byproductString)
	{
		String input = null;
		int inputAmount = 0;
		int powerCost = 0;
		List<String> outputs = new ArrayList<String>();
		List<Integer> outputAmounts = new ArrayList<Integer>();

		String remainingText = recipeString;

		int termIndex = 0;

		// Iterate through each comma (or arrow) separated term
		while (remainingText.contains(","))
		{
			int arrowIndex = remainingText.indexOf("->");
			int commaIndex = remainingText.indexOf(",");
			boolean arrow = (arrowIndex > 0 && arrowIndex < commaIndex);
			int endPos = arrow ? arrowIndex : commaIndex;

			String current = remainingText.substring(0, endPos).trim();

			if (termIndex == 0)
			{
				// Read power cost
				powerCost = attemptParseInteger(current, true, false, "distillation tower power cost for recipe " + recipeNum);
			}
			else if (termIndex == 1)
			{
				// Read input fluid name
				input = current;
			}
			else if (termIndex == 2)
			{
				// Read input fluid amount
				inputAmount = attemptParseInteger(current, false, false, "distillation tower input for recipe " + recipeNum);
			}
			else
			{
				// Read output fluid name / amount
				parseDistillationOutput(recipeNum, termIndex, current, outputs, outputAmounts);
			}

			remainingText = remainingText.substring(endPos + (arrow ? 2 : 1));
			termIndex++;
		}
		String current = remainingText.trim();

		// Read last output fluid name / amount
		parseDistillationOutput(recipeNum, termIndex, current, outputs, outputAmounts);

		// Validate that each output has an output amount
		if (outputs.size() != outputAmounts.size())
		{
			throw new RuntimeException("Mismatched outputs for distillation recipe " + recipeNum);
		}

		// Convert fluid names to Fluid values from the FluidRegistry
		FluidStack[] outputFluids = new FluidStack[outputs.size()];
		for (int n = 0; n < outputs.size(); n++)
		{
			outputs.set(n, outputs.get(n).toLowerCase(Locale.ENGLISH));
			if (FluidRegistry.getFluid(outputs.get(n)) == null)
			{
				throw new RuntimeException("Invalid output fluid name #" + (n + 1) + " for distillation recipe " + recipeNum);
			}
			Fluid f = FluidRegistry.getFluid(outputs.get(n));
			outputFluids[n] = new FluidStack(f, outputAmounts.get(n));
		}

		input = input.toLowerCase(Locale.ENGLISH);
		if (FluidRegistry.getFluid(input) == null)
		{
			throw new RuntimeException("Invalid input fluid name for distillation recipe " + recipeNum);
		}
		Fluid f = FluidRegistry.getFluid(input);
		FluidStack inputFluid = new FluidStack(f, inputAmount);

		String[] byproducts = byproductString.split(";");
		ItemStack[] itemOutputs = new ItemStack[byproducts.length];
		float[] chances = new float[byproducts.length];

		for (int i = 0; i < byproducts.length; i++)
		{
			Tuple<ItemStack, Float> result = parseDistillationByproduct(recipeNum, byproducts[i]);
			itemOutputs[i] = result.getFirst();
			chances[i] = result.getSecond() / 100F;
		}

		System.out.println("Added distillation recipe using " + input);
		for (ItemStack itemOutput : itemOutputs)
		{
			System.out.println(itemOutput.getDisplayName());
		}
		DistillationRecipe.addRecipe(outputFluids, itemOutputs, inputFluid, powerCost, 1, chances);
	}

	public static void addFuel(String[] fuels)
	{
		for (int i = 0; i < fuels.length; i++)
		{
			String str = fuels[i];

			if (str.isEmpty()) continue;

			String fluid = null;
			int amount = 0;
			int production = 0;

			String remain = str;

			int index = 0;

			while (remain.indexOf(",") != -1)
			{
				int endPos = remain.indexOf(",");

				String current = remain.substring(0, endPos).trim();

				if (index == 0) fluid = current;
				else if (index == 1)
				{
					try
					{
						amount = Integer.parseInt(current);
						if (amount <= 0)
						{
							throw new RuntimeException("Negative value for fuel mB/tick for generator fuel " + (i + 1));
						}
					} catch (NumberFormatException e)
					{
						throw new RuntimeException("Invalid value for fuel mB/tick for generator fuel " + (i + 1));
					}
				}

				remain = remain.substring(endPos + 1);
				index++;
			}
			String current = remain.trim();

			try
			{
				production = Integer.parseInt(current);
				if (production < 0)
				{
					throw new RuntimeException("Negative value for fuel RF/tick for generator fuel " + (i + 1));
				}
			} catch (NumberFormatException e)
			{
				throw new RuntimeException("Invalid value for fuel RF/tick for generator fuel " + (i + 1));
			}

			fluid = fluid.toLowerCase(Locale.ENGLISH);
			if (FluidRegistry.getFluid(fluid) == null)
			{
				throw new RuntimeException("Invalid fluid name for generator fuel " + (i + 1));
			}

			FuelHandler.registerPortableGeneratorFuel(FluidRegistry.getFluid(fluid), production, amount);
		}

	}

	public static void addBoatFuel(String[] fuels)
	{
		for (int i = 0; i < fuels.length; i++)
		{
			String str = fuels[i];

			if (str.isEmpty()) continue;

			String fluid = null;
			int amount = 0;

			String remain = str;

			int index = 0;

			while (remain.indexOf(",") != -1)
			{
				int endPos = remain.indexOf(",");

				String current = remain.substring(0, endPos).trim();

				if (index == 0) fluid = current;

				remain = remain.substring(endPos + 1);
				index++;
			}
			String current = remain.trim();

			try
			{
				amount = Integer.parseInt(current);
				if (amount <= 0)
				{
					throw new RuntimeException("Negative value for fuel mB/tick for boat fuel " + (i + 1));
				}
			} catch (NumberFormatException e)
			{
				throw new RuntimeException("Invalid value for fuel mB/tick for boat fuel " + (i + 1));
			}

			fluid = fluid.toLowerCase(Locale.ENGLISH);
			if (FluidRegistry.getFluid(fluid) == null)
			{
				throw new RuntimeException("Invalid fluid name for boat fuel " + (i + 1));
			}

			FuelHandler.registerMotorboatFuel(FluidRegistry.getFluid(fluid), amount);
		}

	}

	public static void addConfigReservoirs(String[] reservoirs)
	{
		for (int i = 0; i < reservoirs.length; i++)
		{
			String str = reservoirs[i];

			if (str.isEmpty()) continue;

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
					} catch (NumberFormatException e)
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
					} catch (NumberFormatException e)
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
					} catch (NumberFormatException e)
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
					} catch (NumberFormatException e)
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
						} catch (NumberFormatException e)
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
						} catch (NumberFormatException e)
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
				else if (index == 9)
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
						biomeWhitelist.add(PumpjackHandler.convertConfigName(value.trim()));
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

			fluid = fluid.toLowerCase(Locale.ENGLISH);
			if (FluidRegistry.getFluid(fluid) == null)
			{
				throw new RuntimeException("Invalid fluid name for reservoir " + (i + 1));
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
