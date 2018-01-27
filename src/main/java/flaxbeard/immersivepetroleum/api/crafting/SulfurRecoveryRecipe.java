package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;


public class SulfurRecoveryRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final FluidStack output;
	public final FluidStack input;
	private final ItemStack itemOutput;
	private final float chance;

	public SulfurRecoveryRecipe(FluidStack output, ItemStack itemOutput, FluidStack input, int energy, float chance)
	{
		this.output = output;
		this.input = input;
		this.itemOutput = itemOutput;
		this.totalProcessEnergy = (int) Math.floor(energy * energyModifier);
		this.totalProcessTime = (int) Math.floor(1 * timeModifier);
		this.chance = chance;

		this.fluidInputList = Lists.newArrayList(this.input);
		this.fluidOutputList = Lists.newArrayList(this.output);
		this.outputList = NonNullList.from(ItemStack.EMPTY, itemOutput);
	}

	public static ArrayList<SulfurRecoveryRecipe> recipeList = new ArrayList();

	public static SulfurRecoveryRecipe addRecipe(FluidStack output, ItemStack itemOutput, FluidStack input, int energy, float chance)
	{
		SulfurRecoveryRecipe r = new SulfurRecoveryRecipe(output, itemOutput, input, energy, chance);
		recipeList.add(r);
		return r;
	}

	public static SulfurRecoveryRecipe findRecipe(FluidStack input)
	{
		for (SulfurRecoveryRecipe recipe : recipeList)
		{
			if (input != null)
			{
				if (recipe.input != null && input.containsFluid(recipe.input))
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

	public static SulfurRecoveryRecipe loadFromNBT(NBTTagCompound nbt)
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
		if (tile.getWorld().rand.nextFloat() <= chance)
		{
			output.add(itemOutput);
		}
		return output;
	}
}