package flaxbeard.immersivepetroleum.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.items.IEItems;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipeBuilder;
import flaxbeard.immersivepetroleum.api.crafting.ReservoirTypeBuilder;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Blocks;
import flaxbeard.immersivepetroleum.common.IPContent.BoatUpgrades;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;

public class IPRecipes extends RecipeProvider{
	private final Map<String, Integer> PATH_COUNT=new HashMap<>();
	
	protected Consumer<IFinishedRecipe> out;
	public IPRecipes(DataGenerator generatorIn){
		super(generatorIn);
	}
	
	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> out){
		this.out=out;
		
		itemRecipes();
		blockRecipes();
		speedboatUpgradeRecipes();
		multiblockRecipes();
		reservoirs();
	}
	
	private void reservoirs(){
		// name, fluid_name, min_mb_fluid, max_mb_fluid, mb_per_tick_replenish, weight, [dim_blacklist], [dim_whitelist], [biome_dict_blacklist], [biome_dict_whitelist]
		// aquifer, water, 5000000, 10000000, 6, 30, [], [0], [], []
		// lava, lava, 250000, 1000000, 0, 30, [1], [], [], []
		// oil, oil, 2500000, 15000000, 6, 40, [1], [], [], []
		
		ReservoirTypeBuilder.builder("aquifer")
			.setFluid(Fluids.WATER)
			.min(5000.000)
			.max(10000.000)
			.trace(0.006)
			.weight(30)
			.addDimensions(false, DimensionType.OVERWORLD.getRegistryName()) // false = Whitelist, true = blacklist
//			.addDimensions(true, DimensionType.OVERWORLD.getRegistryName()) // Will crash the generator, only one or the other but not both at the same time
//			.addBiomes(false, new ResourceLocation[]{}) // Just for demonstration purposes.
			.build(this.out, rl("reservoirs/aquifer"));
		
		// Shorthand for the above. (name   fluid                           min       max        trace  weight)
		ReservoirTypeBuilder.builder("oil", IPContent.Fluids.fluidCrudeOil, 2500.000, 15000.000, 0.006, 40)
			.addDimensions(true, DimensionType.THE_END.getRegistryName()) // false = Whitelist, true = blacklist
			.build(this.out, rl("reservoirs/oil"));
		
		ReservoirTypeBuilder.builder("lava", Fluids.LAVA, 250.000, 1000.000, 0.0, 30)
			.addDimensions(true, DimensionType.THE_END.getRegistryName()) // false = Whitelist, true = blacklist
			.build(this.out, rl("reservoirs/lava"));
	}
	
	private void multiblockRecipes(){
		// power_cost, input_name, input_mb -> output1_name, output1_mb, output2_name, output2_mb
		// 2048, oil, 75 -> lubricant, 9, diesel, 27, gasoline, 39
		
		// item_name, stack_size, percent_chance
		// immersivepetroleum:bitumen, 1, 7
		
		// setEnergy and setTime are 2048 and 1 by default. But still allows to be customized.
		
		DistillationRecipeBuilder.builder(new FluidStack[]{
				new FluidStack(flaxbeard.immersivepetroleum.common.IPContent.Fluids.fluidLubricant, 9),
				new FluidStack(flaxbeard.immersivepetroleum.common.IPContent.Fluids.fluidDiesel, 27),
				new FluidStack(flaxbeard.immersivepetroleum.common.IPContent.Fluids.fluidGasoline, 39)})
			.addByproduct(new ItemStack(flaxbeard.immersivepetroleum.common.IPContent.Items.itemBitumen), 0.07)
			.addInput(flaxbeard.immersivepetroleum.common.IPContent.Fluids.fluidCrudeOil, 75)
			.setEnergy(2048)
			.setTime(1)
			.build(this.out, rl("distillationtower/oilcracking"));
	}
	
	private void speedboatUpgradeRecipes(){
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.itemUpgradeHull)
			.key('P', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.key('B', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage))
			.patternLine("P P")
			.patternLine("PBP")
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_steel_block", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage)))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.itemUpgradeBreaker)
			.key('P', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.key('I', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('B', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage))
			.patternLine("I P")
			.patternLine(" IP")
			.patternLine("PPB")
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_steel_block", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage)))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.itemUpgradeTank)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('T', IEBlocks.MetalDevices.barrel)
			.patternLine(" P ")
			.patternLine("PTP")
			.patternLine(" P ")
			.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.itemUpgradeRudders)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('R', IETags.ironRod)
			.patternLine(" RR")
			.patternLine("PPP")
			.patternLine("PPP")
			.addCriterion("has_iron_rod", hasItem(IETags.ironRod))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.itemUpgradePaddles)
			.key('P', IETags.getItemTag(IETags.treatedWood))
			.key('S', IETags.treatedStick)
			.patternLine("S S")
			.patternLine("S S")
			.patternLine("P P")
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(this.out);
	}
	
	private void blockRecipes(){
		ShapedRecipeBuilder.shapedRecipe(Blocks.blockAsphalt, 8)
			.key('C', flaxbeard.immersivepetroleum.common.IPContent.Items.itemBitumen)
			.key('S', Tags.Items.SAND)
			.key('G', Tags.Items.GRAVEL)
			.key('B', Items.WATER_BUCKET)
			.patternLine("SCS")
			.patternLine("GBG")
			.patternLine("SCS")
			.addCriterion("has_bitumen", hasItem(flaxbeard.immersivepetroleum.common.IPContent.Items.itemBitumen))
			.build(this.out, rl("asphalt"));
		
		ShapedRecipeBuilder.shapedRecipe(Blocks.blockAsphalt, 12)
			.key('C', flaxbeard.immersivepetroleum.common.IPContent.Items.itemBitumen)
			.key('S', IEItems.Ingredients.slag)
			.key('G', Tags.Items.GRAVEL)
			.key('B', Items.WATER_BUCKET)
			.patternLine("SCS")
			.patternLine("GBG")
			.patternLine("SCS")
			.addCriterion("has_bitumen", hasItem(flaxbeard.immersivepetroleum.common.IPContent.Items.itemBitumen))
			.addCriterion("has_slag", hasItem(IEItems.Ingredients.slag))
			.build(this.out, rl("asphalt"));
	}
	
	private void itemRecipes(){
		ShapedRecipeBuilder.shapedRecipe(flaxbeard.immersivepetroleum.common.IPContent.Items.itemOilCan)
			.key('R', Tags.Items.DYES_RED)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('B', Items.BUCKET)
			.patternLine(" R ")
			.patternLine("PBP")
			.addCriterion("has_rose_red", this.hasItem(Items.RED_DYE))
			.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
			.build(out);
		
		ShapedRecipeBuilder.shapedRecipe(flaxbeard.immersivepetroleum.common.IPContent.Items.itemProjector)
			.key('I', Tags.Items.INGOTS_IRON)
			.key('W', IETags.getItemTag(IETags.treatedWood))
			.key('L', IEBlocks.MetalDecoration.lantern)
			.key('S', Tags.Items.GLASS)
			.patternLine("S  ")
			.patternLine("IL ")
			.patternLine(" IW")
			.addCriterion("has_iron_ingot", hasItem(Items.IRON_INGOT))
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);
		
		ShapedRecipeBuilder.shapedRecipe(flaxbeard.immersivepetroleum.common.IPContent.Items.itemSpeedboat)
			.key('P', IETags.getItemTag(IETags.treatedWood))
			.key('E', IEBlocks.MetalDecoration.engineeringLight)
			.key('M', IEItems.Ingredients.componentIron)
			.patternLine("PME")
			.patternLine("PPP")
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.addCriterion("has_"+toPath(MetalDecoration.engineeringLight), hasItem(MetalDecoration.engineeringLight))
			.build(this.out);
	}
	
	private ResourceLocation rl(String str){
		if(PATH_COUNT.containsKey(str)){
			int count=PATH_COUNT.get(str)+1;
			PATH_COUNT.put(str, count);
			return new ResourceLocation(ImmersivePetroleum.MODID, str+count);
		}
		PATH_COUNT.put(str, 1);
		return new ResourceLocation(ImmersivePetroleum.MODID, str);
	}
	
	private String toPath(IItemProvider src){
		return src.asItem().getRegistryName().getPath();
	}
	
}