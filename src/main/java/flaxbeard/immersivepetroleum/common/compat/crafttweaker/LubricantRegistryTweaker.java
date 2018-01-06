package flaxbeard.immersivepetroleum.common.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivepetroleum.Lubricant")
@ZenRegister
public class LubricantRegistryTweaker
{

	@ZenMethod
	public static void registerLubricant(ILiquidStack lubricantEntry, int amount)
	{
		Fluid mcFluid;
		FluidStack mcFluidStack;
		if (lubricantEntry == null)
		{
			CraftTweakerAPI.logError("Found null FluidStack in lubricant entry");
		}
		else
		{
			mcFluidStack = CraftTweakerMC.getLiquidStack(lubricantEntry);
			mcFluid = mcFluidStack.getFluid();
			LubricantHandler.registerLubricant(mcFluid, amount);
		}
	}
}
