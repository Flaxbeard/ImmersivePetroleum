package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/**
 * @author BluSunrize - 02.03.2016
 * <p>
 * The recipe for the Refinery
 */


public class DistillationRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final float[] chances;

	public final FluidStack[] fluidOutput;
	public final ItemStack[] itemOutput;
	public final FluidStack input;

	public static ArrayList<DistillationRecipe> recipeList = new ArrayList();

	public DistillationRecipe(FluidStack[] fluidOutput, ItemStack[] itemOutput, FluidStack input, int energy, int time, float[] chances)
	{
		this.fluidOutput = fluidOutput;
		this.itemOutput = itemOutput;
		this.input = input;
		this.totalProcessEnergy = (int) Math.floor(energy * energyModifier);
		this.totalProcessTime = (int) Math.floor(time * timeModifier);

		this.chances = chances;

		this.fluidInputList = Collections.singletonList(this.input);
		this.fluidOutputList = Arrays.asList(this.fluidOutput);
		this.outputList = NonNullList.from(ItemStack.EMPTY, itemOutput);
	}

	/**
	 * Add a Distillation Tower recipe
	 *
	 * @param fluidOutputs A list of FluidStacks, each an output from the recipe
	 * @param itemOutputs  A list of ItemStacks, each an output as well
	 * @param input        The fluid input fo this recipe
	 * @param energy       The amount of energy one operation takes
	 * @param time         How long this operation takes
	 * @param chances      A list of chances of obtaining each solid byproduct
	 * @return The created/registered recipe matching the input
	 */
	public static DistillationRecipe addRecipe(FluidStack[] fluidOutputs, ItemStack[] itemOutputs, FluidStack input, int energy, int time, float[] chances)
	{
		DistillationRecipe r = new DistillationRecipe(fluidOutputs, itemOutputs, input, energy, time, chances);
		recipeList.add(r);
		return r;
	}

	public static DistillationRecipe findRecipe(FluidStack input)
	{
		for (DistillationRecipe recipe : recipeList)
		{
			if (input != null)
			{
				if (recipe.input != null && (input.containsFluid(recipe.input)))
				{
					return recipe;
				}

			}
		}
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	public static DistillationRecipe loadFromNBT(NBTTagCompound nbt)
	{
		FluidStack input = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input"));
		return findRecipe(input);
	}

	int totalProcessTime;


	@Override
	public int getTotalProcessTime()
	{
		return this.totalProcessTime;
	}

	int totalProcessEnergy;

	@Override
	public int getTotalProcessEnergy()
	{
		return this.totalProcessEnergy;
	}

	@Override
	public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile)
	{
		NonNullList<ItemStack> output = NonNullList.create();
		for (int i = 0; i < itemOutput.length; i++)
		{
			if (tile.getWorld().rand.nextFloat() <= chances[i])
			{
				output.add(itemOutput[i]);
			}
		}
		return output;
	}
}