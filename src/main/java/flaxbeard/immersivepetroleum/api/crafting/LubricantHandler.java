package flaxbeard.immersivepetroleum.api.crafting;

import java.util.HashMap;

import net.minecraftforge.fluids.Fluid;

public class LubricantHandler
{
	static final HashMap<String, Integer> lubricantAmounts = new HashMap<String, Integer>();

	/**
	 * @param lube the fluid to be used as lubricant
	 * @param amount of mB of lubricant to spend every 4 ticks
	 */
	public static void registerLubricant(Fluid lube, int amount)
	{
		if (lube != null)
			lubricantAmounts.put(lube.getName(), amount);
	}
	

	public static int getLubeAmount(Fluid lube)
	{
		if (lube != null)
		{
			String s = lube.getName();
			if (lubricantAmounts.containsKey(s))
				return lubricantAmounts.get(s);
		}
		return 0;
	}

	public static boolean isValidLube(Fluid lube)
	{
		if (lube != null)
			return lubricantAmounts.containsKey(lube.getName());
		return false;
	}
}
