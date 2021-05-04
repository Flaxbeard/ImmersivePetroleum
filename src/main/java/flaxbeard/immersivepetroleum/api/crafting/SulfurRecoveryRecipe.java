package flaxbeard.immersivepetroleum.api.crafting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class SulfurRecoveryRecipe extends MultiblockRecipe{
	public static final IRecipeType<SulfurRecoveryRecipe> TYPE = IRecipeType.register(ImmersivePetroleum.MODID + ":hydrotreater");
	
	public static Map<ResourceLocation, SulfurRecoveryRecipe> recipes = new HashMap<>();
	
	public static SulfurRecoveryRecipe findRecipe(@Nonnull FluidStack input, @Nonnull FluidStack secondary){
		Objects.requireNonNull(input);
		Objects.requireNonNull(secondary);
		
		for(SulfurRecoveryRecipe recipe:recipes.values()){
			if((recipe.inputFluid != null && recipe.inputFluid.test(input)) && (secondary.isEmpty() || (recipe.inputFluidSecondary != null && secondary != null && recipe.inputFluidSecondary.test(secondary)))){
				return recipe;
			}
		}
		return null;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(SulfurRecoveryRecipe recipe:recipes.values()){
				if(recipe.inputFluid != null){
					if((!ignoreAmount && recipe.inputFluid.test(fluid)) || (ignoreAmount && recipe.inputFluid.testIgnoringAmount(fluid))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean hasRecipeWithSecondaryInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(SulfurRecoveryRecipe recipe:recipes.values()){
				if(recipe.inputFluidSecondary != null){
					if((!ignoreAmount && recipe.inputFluidSecondary.test(fluid)) || (ignoreAmount && recipe.inputFluidSecondary.testIgnoringAmount(fluid))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public final ItemStack outputItem;
	public final double chance;
	
	public final FluidStack output;
	
	public final FluidTagInput inputFluid;
	@Nullable
	public final FluidTagInput inputFluidSecondary;
	
	
	protected int totalProcessTime;
	protected int totalProcessEnergy;
	
	public SulfurRecoveryRecipe(ResourceLocation id, FluidStack output, ItemStack outputItem, FluidTagInput inputFluid, @Nullable FluidTagInput inputFluidSecondary, double chance, int energy, int time){
		super(ItemStack.EMPTY, TYPE, id);
		this.output = output;
		this.outputItem = outputItem;
		this.inputFluid = inputFluid;
		this.inputFluidSecondary = inputFluidSecondary;
		this.chance = chance;
		
		this.totalProcessEnergy = (int) Math.floor(energy * IPServerConfig.REFINING.hydrotreater_energyModifier.get());
		this.totalProcessTime = (int) Math.floor(time * IPServerConfig.REFINING.hydrotreater_timeModifier.get());
		
		this.fluidOutputList = Arrays.asList(output);
		this.fluidInputList = Arrays.asList(inputFluidSecondary != null ? new FluidTagInput[]{inputFluid, inputFluidSecondary} : new FluidTagInput[]{inputFluid});
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	@Override
	public int getTotalProcessTime(){
		return this.totalProcessTime;
	}
	
	@Override
	public int getTotalProcessEnergy(){
		return this.totalProcessEnergy;
	}
	
	public FluidTagInput getInputFluid(){
		return this.inputFluid;
	}
	
	@Nullable
	public FluidTagInput getSecondaryInputFluid(){
		return this.inputFluidSecondary;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile){
		NonNullList<ItemStack> list = NonNullList.create();
		if(tile.getWorld().rand.nextFloat() <= chance){
			list.add(this.outputItem);
		}
		return list;
	}
	
	@Override
	protected IERecipeSerializer<SulfurRecoveryRecipe> getIESerializer(){
		return Serializers.HYDROTREATER_SERIALIZER.get();
	}
}
