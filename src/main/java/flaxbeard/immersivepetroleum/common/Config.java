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
		@Comment({"Display chunk border while holding Core Samples, default=true"})
		public static boolean sample_displayBorder = true;

		public static Machines machines;

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

			@Comment({"The minimum amount of oil that a deposit can contain in mB, default=1000000"})
			public static int oil_min = 1000000;
			@Comment({"The maximum amount of oil that a deposit can contain in mB, default=5000000"})
			public static int oil_max = 5000000;
			@Comment({"The chance that a chunk contains oil, default=0.25"})
			public static float oil_chance = 0.25F;
			@Comment({"The maximum oil production, in mB/tick, offered by a depleted oil reservoir, default=3"})
			public static int oil_replenish = 3;
			@Comment({"List of dimensions that can't contain oil, default=1 (the end)"})
			public static int[] oil_dimBlacklist = new int[]{1};

		}
	}

	static Configuration config;
}
