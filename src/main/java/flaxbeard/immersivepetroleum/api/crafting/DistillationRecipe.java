package flaxbeard.immersivepetroleum.api.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.ListUtils;

import com.google.common.collect.Lists;

/**
 * @author BluSunrize - 02.03.2016
 *
 * The recipe for the Refinery
 */
public class DistillationRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;
	
	public final float chance;

	public final FluidStack fluidOutput;
	public final ItemStack itemOutput;
	public final FluidStack input;
	public DistillationRecipe(FluidStack fluidOutput, ItemStack itemOutput, FluidStack input, int energy, int time, float chance)
	{
		this.fluidOutput = fluidOutput;
		this.itemOutput = itemOutput;
		this.input = input;
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(time*timeModifier);
		
		this.chance = chance;

		this.fluidInputList = Lists.newArrayList(this.input);
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
		this.outputList = ListUtils.fromItem(this.itemOutput);
	}

	public static ArrayList<DistillationRecipe> recipeList = new ArrayList();
	public static DistillationRecipe addRecipe(FluidStack fluidOutput, ItemStack itemOutput,  FluidStack input, int energy, int time, float chance)
	{
		DistillationRecipe r = new DistillationRecipe(fluidOutput, itemOutput, input, energy, time, chance);
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
		if (tile.getWorld().rand.nextFloat() <= chance)
		{
			return getItemOutputs();
		}
		else
		{
			return ListUtils.fromItems();
		}
	}
}