package flaxbeard.immersivepetroleum.api.energy;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;

public class FuelHandler
{
	static final Map<ResourceLocation, Integer> portableGenAmountTick = new HashMap<>();
	static final Map<ResourceLocation, Integer> portableGenPowerTick = new HashMap<>();

	static final Map<ResourceLocation, Integer> motorboatAmountTick = new HashMap<>();


	public static void registerPortableGeneratorFuel(Fluid fuel, int fluxPerTick, int mbPerTick)
	{
		if (fuel != null)
		{
			portableGenAmountTick.put(fuel.getRegistryName(), mbPerTick);
			portableGenPowerTick.put(fuel.getRegistryName(), fluxPerTick);
		}
	}

	public static void registerMotorboatFuel(Fluid fuel, int mbPerTick)
	{
		if (fuel != null)
		{
			motorboatAmountTick.put(fuel.getRegistryName(), mbPerTick);
		}
	}

	public static boolean isValidBoatFuel(Fluid fuel)
	{
		if (fuel != null)
			return motorboatAmountTick.containsKey(fuel.getRegistryName());
		return false;
	}

	public static int getBoatFuelUsedPerTick(Fluid fuel)
	{
		if (!isValidBoatFuel(fuel)) return 0;
		return motorboatAmountTick.get(fuel.getRegistryName());
	}


	public static int getFuelUsedPerTick(Fluid fuel)
	{
		if (!isValidFuel(fuel)) return 0;
		return portableGenAmountTick.get(fuel.getRegistryName());
	}

	public static int getFluxGeneratedPerTick(Fluid fuel)
	{
		if (!isValidFuel(fuel)) return 0;
		return portableGenPowerTick.get(fuel.getRegistryName());
	}

	public static boolean isValidFuel(Fluid fuel)
	{
		if (fuel != null)
			return portableGenAmountTick.containsKey(fuel.getRegistryName());
		return false;
	}

	public static Map<ResourceLocation, Integer> getFuelAmountsPerTick()
	{
		return portableGenAmountTick;
	}

	public static Map<ResourceLocation, Integer> getFuelFluxesPerTick()
	{
		return portableGenPowerTick;
	}

}