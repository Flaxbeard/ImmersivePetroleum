package flaxbeard.immersivepetroleum.common;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Configuration;
import blusunrize.immersiveengineering.common.Config.Mapped;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;

public class Config
{

	@net.minecraftforge.common.config.Config(modid=ImmersivePetroleum.MODID)
	public static class IPConfig
	{
		@Comment({"Display chunk border while holding Core Samples"})
		public static boolean sample_displayBorder = true;
		
		public static Machines machines = new Machines();

		public static class Machines
		{
			//Multiblock Recipes
			@Comment({"A modifier to apply to the energy costs of every Distillation Tower recipe"})
			public static float distillationTower_energyModifier = 1;
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			public static int distillationTower_operationCost = (int) (4096 * distillationTower_energyModifier);
			@Comment({"A modifier to apply to the time of every Distillation recipe. Can't be lower than 1"})
			public static float distillationTower_timeModifier = 1;
			

			//Other Multiblock machines
			@Comment({"The Flux the Pumpjack requires each tick to pump"})
			public static int pumpjack_consumption = 2048;
			@Comment({"The amount of mB of oil a Pumpjack extracts per tick"})
			public static int pumpjack_speed = 5;

			@Comment({"The minimum amount of oil that a deposit can contain in mB"})
			public static int oil_min = 1000000;
			@Comment({"The maximum amount of oil that a deposit can contain in mB"})
			public static int oil_max = 5000000;
			@Comment({"The chance that a chunk contains oil"})
			public static float oil_chance = 0.25F;
			@Comment({"The maximum oil production, in mB/tick, offered by a depleted oil reservoir"})
			public static int oil_replenish = 3;
			@Comment({"List of dimensions that can't contain minerals. Default: The End."})
			public static int[] oil_dimBlacklist = new int[]{1};

		}
	}

	static Configuration config;
}
