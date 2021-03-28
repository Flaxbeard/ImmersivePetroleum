package flaxbeard.immersivepetroleum.api.crafting;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class DistillationRecipe extends IPMultiblockRecipe{
	public static final IRecipeType<DistillationRecipe> TYPE = IRecipeType.register(ImmersivePetroleum.MODID + ":distillationtower");
	public static Map<ResourceLocation, DistillationRecipe> recipes = new HashMap<>();
	
	/** May return null! */
	public static DistillationRecipe findRecipe(FluidStack input){
		if(!recipes.isEmpty()){
			for(DistillationRecipe r:recipes.values()){
				if(r.input != null && r.input.testIgnoringAmount(input)){
					return r;
				}
			}
		}
		return null;
	}
	
	public static DistillationRecipe loadFromNBT(CompoundNBT nbt){
		FluidStack input = FluidStack.loadFluidStackFromNBT(nbt.getCompound("input"));
		return findRecipe(input);
	}
	
	protected final FluidTagInput input;
	protected final FluidStack[] fluidOutput;
	protected final ItemStack[] itemOutput;
	protected final double[] chances;
	
	public DistillationRecipe(ResourceLocation id, FluidStack[] fluidOutput, ItemStack[] itemOutput, FluidTagInput input, int energy, int time, double[] chances){
		super(ItemStack.EMPTY, TYPE, id);
		this.fluidOutput = fluidOutput;
		this.itemOutput = itemOutput;
		this.chances = chances;
		
		this.input = input;
		this.fluidInputList = Collections.singletonList(input);
		this.fluidOutputList = Arrays.asList(this.fluidOutput);
		this.outputList = NonNullList.from(ItemStack.EMPTY, itemOutput);
		
		timeAndEnergy(time, energy);
		modifyTimeAndEnergy(IPServerConfig.REFINING.distillationTower_energyModifier::get, IPServerConfig.REFINING.distillationTower_timeModifier::get);
	}
	
	@Override
	protected IERecipeSerializer<DistillationRecipe> getIESerializer(){
		return Serializers.DISTILLATION_SERIALIZER.get();
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
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
	
	public FluidTagInput getInputFluid(){
		return this.input;
	}
	
	public double[] chances(){
		return this.chances;
	}
}
