package flaxbeard.immersivepetroleum.common.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivepetroleum.Motorboat")
@ZenRegister
public class MotorboatFuelTweaker
{
	@ZenMethod
	public static void registerMotorboatFuel(ILiquidStack fuelEntry, int mbPerTick)
	{
		Fluid mcFluid;
		FluidStack mcFluidStack;
		if (fuelEntry == null)
		{
			CraftTweakerAPI.logError("Found null FluidStack in fuel entry");
		}
		else
		{
			mcFluidStack = CraftTweakerMC.getLiquidStack(fuelEntry);
			mcFluid = mcFluidStack.getFluid();
			FuelHandler.registerMotorboatFuel(mcFluid, mbPerTick);
		}
	}
}
