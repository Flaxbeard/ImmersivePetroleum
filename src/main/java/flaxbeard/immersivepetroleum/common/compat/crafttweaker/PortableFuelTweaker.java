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

@ZenClass("mods.immersivepetroleum.PortableGenerator")
@ZenRegister
public class PortableFuelTweaker
{
	@ZenMethod
	public static void registerPortableGenFuel(ILiquidStack fuelEntry, int fluxPerTick, int mbPerTick)
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
			FuelHandler.registerPortableGeneratorFuel(mcFluid, fluxPerTick, mbPerTick);
		}
	}
}
