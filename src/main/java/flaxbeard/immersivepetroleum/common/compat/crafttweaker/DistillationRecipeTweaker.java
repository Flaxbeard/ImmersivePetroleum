package flaxbeard.immersivepetroleum.common.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivepetroleum.Distillation")
@ZenRegister
public class DistillationRecipeTweaker
{
	@ZenMethod
	public static void addRecipe(ILiquidStack[] fluidOutputs, IItemStack[] itemOutputs, ILiquidStack fluidInput, int energy, int time, float[] chance)
	{

		FluidStack mcFluidInputStack;

		if (fluidInput == null)
		{
			CraftTweakerAPI.logError("Found null FluidStack in distillation recipe fluidInput");
		}
		else
		{
			FluidStack[] mcFluidOutputs = new FluidStack[fluidOutputs.length];
			for (int x = 0; x < fluidOutputs.length; x++)
			{
				ILiquidStack currentLiquidStack = fluidOutputs[x];
				if (currentLiquidStack == null)
				{
					CraftTweakerAPI.logError("Found null FluidStack in distillation recipe fluidOutputs");
				}
				else
				{
					mcFluidOutputs[x] = CraftTweakerMC.getLiquidStack(fluidOutputs[x]);
				}
			}

			ItemStack[] mcItemOutputs = new ItemStack[itemOutputs.length];
			for (int x = 0; x < mcItemOutputs.length; x++)
			{
				IItemStack currentItemStack = itemOutputs[x];
				if (currentItemStack == null)
				{
					CraftTweakerAPI.logError("Found null ItemStack in distillation recipe itemOutputs");
				}
				else
				{
					mcItemOutputs[x] = CraftTweakerMC.getItemStack(itemOutputs[x]);
				}
			}

			mcFluidInputStack = CraftTweakerMC.getLiquidStack(fluidInput);

			DistillationRecipe.addRecipe(mcFluidOutputs, mcItemOutputs, mcFluidInputStack, energy, time, chance);
		}


	}
}