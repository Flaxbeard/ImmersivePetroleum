package flaxbeard.immersivepetroleum.api.crafting;

import java.util.function.DoubleSupplier;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;

public abstract class IPMultiblockRecipe extends MultiblockRecipe{
	Lazy<Integer> totalProcessTime;
	Lazy<Integer> totalProcessEnergy;
	protected IPMultiblockRecipe(ItemStack outputDummy, IRecipeType<?> type, ResourceLocation id){
		super(outputDummy, type, id);
	}
	
	protected void timeAndEnergy(int time, int energy){
		this.totalProcessEnergy = Lazy.of(() -> energy);
		this.totalProcessTime = Lazy.of(() -> time);
	}
	
	@Override
	public void modifyTimeAndEnergy(DoubleSupplier timeModifier, DoubleSupplier energyModifier){
		final Lazy<Integer> oldTime = this.totalProcessTime;
		final Lazy<Integer> oldEnergy = this.totalProcessEnergy;
		this.totalProcessTime = Lazy.of(() -> (int) (Math.max(1, oldTime.get() * timeModifier.getAsDouble())));
		this.totalProcessEnergy = Lazy.of(() -> (int) (Math.max(1, oldEnergy.get() * energyModifier.getAsDouble())));
	}
	
	@Override
	public int getTotalProcessTime(){
		return this.totalProcessTime.get();
	}
	
	@Override
	public int getTotalProcessEnergy(){
		return this.totalProcessEnergy.get();
	}
}
