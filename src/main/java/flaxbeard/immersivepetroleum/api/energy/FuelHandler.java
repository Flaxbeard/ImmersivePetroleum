package flaxbeard.immersivepetroleum.api.energy;

import net.minecraftforge.fluids.Fluid;

import java.util.HashMap;

public class FuelHandler
{
	static final HashMap<String, Integer> portableGenAmountTick = new HashMap<String, Integer>();
	static final HashMap<String, Integer> portableGenPowerTick = new HashMap<String, Integer>();

	static final HashMap<String, Integer> motorboatAmountTick = new HashMap<String, Integer>();


	public static void registerPortableGeneratorFuel(Fluid fuel, int fluxPerTick, int mbPerTick)
	{
		if (fuel != null)
		{
			portableGenAmountTick.put(fuel.getName(), mbPerTick);
			portableGenPowerTick.put(fuel.getName(), fluxPerTick);
		}
	}

	public static void registerMotorboatFuel(Fluid fuel, int mbPerTick)
	{
		if (fuel != null)
		{
			motorboatAmountTick.put(fuel.getName(), mbPerTick);
		}
	}

	public static boolean isValidBoatFuel(Fluid fuel)
	{
		if (fuel != null)
			return motorboatAmountTick.containsKey(fuel.getName());
		return false;
	}

	public static int getBoatFuelUsedPerTick(Fluid fuel)
	{
		if (!isValidBoatFuel(fuel)) return 0;
		return motorboatAmountTick.get(fuel.getName());
	}


	public static int getFuelUsedPerTick(Fluid fuel)
	{
		if (!isValidFuel(fuel)) return 0;
		return portableGenAmountTick.get(fuel.getName());
	}

	public static int getFluxGeneratedPerTick(Fluid fuel)
	{
		if (!isValidFuel(fuel)) return 0;
		return portableGenPowerTick.get(fuel.getName());
	}

	public static boolean isValidFuel(Fluid fuel)
	{
		if (fuel != null)
			return portableGenAmountTick.containsKey(fuel.getName());
		return false;
	}

	public static HashMap<String, Integer> getFuelAmountsPerTick()
	{
		return portableGenAmountTick;
	}

	public static HashMap<String, Integer> getFuelFluxesPerTick()
	{
		return portableGenPowerTick;
	}

}