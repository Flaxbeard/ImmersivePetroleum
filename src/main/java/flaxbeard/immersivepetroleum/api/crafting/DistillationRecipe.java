package flaxbeard.immersivepetroleum.api.crafting;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;

public class DistillationRecipe extends MultiblockRecipe{
	public static final IRecipeType<DistillationRecipe> TYPE=IRecipeType.register(ImmersivePetroleum.MODID+":distillationtower");
	public static Map<ResourceLocation, DistillationRecipe> recipes=new HashMap<>();
	
	/** Initialized in {@link Serializers} */
	public static RegistryObject<IERecipeSerializer<DistillationRecipe>> SERIALIZER;
	
	/** May return null! */
	public static DistillationRecipe findRecipe(FluidStack input){
		if(!recipes.isEmpty()){
			for(DistillationRecipe r:recipes.values()){
				if(r.input!=null && r.input.isFluidEqual(input)){
					return r;
				}
			}
		}
		return null;
	}
	
	public final FluidStack input;
	public final FluidStack[] fluidOutput;
	public final ItemStack[] itemOutput;
	public final double[] chances;
	
	public DistillationRecipe(ResourceLocation id, FluidStack[] fluidOutput, ItemStack[] itemOutput, FluidStack input, int energy, int time, double[] chances){
		super(ItemStack.EMPTY, TYPE, id);
		this.fluidOutput = fluidOutput;
		this.itemOutput = itemOutput;
		this.input = input;
		this.totalProcessEnergy = (int) Math.floor(energy * IPConfig.REFINING.distillationTower_energyModifier.get());
		this.totalProcessTime = (int) Math.floor(time * IPConfig.REFINING.distillationTower_timeModifier.get());
		this.fluidInputList = Collections.singletonList(this.input);
		this.fluidOutputList = Arrays.asList(this.fluidOutput);
		this.outputList = NonNullList.from(ItemStack.EMPTY, itemOutput);
		this.chances = chances;
	}
	
	/**
	 * @deprecated <pre>in favour of JSON Recipes. See {@link DistillationRecipeBuilder}</pre>
	 */
	@Deprecated
	public static DistillationRecipe addRecipe(FluidStack[] fluidOutputs, ItemStack[] itemOutputs, FluidStack input, int energy, int time, double[] chances){
		return null;
	}
	
	@Override
	protected IERecipeSerializer<DistillationRecipe> getIESerializer(){
		return SERIALIZER.get();
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT nbt){
		nbt.put("input", input.writeToNBT(new CompoundNBT()));
		return nbt;
	}
	
	public static DistillationRecipe loadFromNBT(CompoundNBT nbt){
		FluidStack input = FluidStack.loadFluidStackFromNBT(nbt.getCompound("input"));
		return findRecipe(input);
	}
	
	int totalProcessTime;
	
	@Override
	public int getTotalProcessTime(){
		return this.totalProcessTime;
	}
	
	int totalProcessEnergy;
	
	@Override
	public int getTotalProcessEnergy(){
		return this.totalProcessEnergy;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile){
		NonNullList<ItemStack> output = NonNullList.create();
		for(int i = 0;i < itemOutput.length;i++){
			if(tile.getWorld().rand.nextFloat() <= chances[i]){
				output.add(itemOutput[i]);
			}
		}
		return output;
	}
}