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
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
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
	}
	
	private void multiblockRecipes(){
		// power_cost, input_name, input_mb -> output1_name, output1_mb, output2_name, output2_mb
		// 2048, oil, 75 -> lubricant, 9, diesel, 27, gasoline, 39
		
		// item_name, stack_size, percent_chance
		// immersivepetroleum:bitumen, 1, 7
		
		// setEnergy and setTime are 2048 and 1 by default. But still allows to be customized.
		
		DistillationRecipeBuilder.builder(new FluidStack[]{
				new FluidStack(IPContent.fluidLubricant, 9),
				new FluidStack(IPContent.fluidDiesel, 27),
				new FluidStack(IPContent.fluidGasoline, 39)})
			.addByproduct(new ItemStack(IPContent.itemBitumen), 0.07)
			.addInput(IPContent.fluidCrudeOil, 75)
			//.setEnergy(2048) // See method description.
			//.setTime(1) // See method description.
			.build(this.out, rl("distillationtower/oilcracking"));
	}
	
	private void speedboatUpgradeRecipes(){
		ShapedRecipeBuilder.shapedRecipe(IPContent.itemUpgradeHull)
			.key('P', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.key('B', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage))
			.patternLine("P P")
			.patternLine("PBP")
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_steel_block", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage)))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(IPContent.itemUpgradeBreaker)
			.key('P', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.key('I', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('B', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage))
			.patternLine("I P")
			.patternLine(" IP")
			.patternLine("PPB")
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_steel_block", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage)))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(IPContent.itemUpgradeTank)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('T', IEBlocks.MetalDevices.barrel)
			.patternLine(" P ")
			.patternLine("PTP")
			.patternLine(" P ")
			.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(IPContent.itemUpgradeRudders)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('R', IETags.ironRod)
			.patternLine(" RR")
			.patternLine("PPP")
			.patternLine("PPP")
			.addCriterion("has_iron_rod", hasItem(IETags.ironRod))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(IPContent.itemUpgradePaddles)
			.key('P', IETags.getItemTag(IETags.treatedWood))
			.key('S', IETags.treatedStick)
			.patternLine("S S")
			.patternLine("S S")
			.patternLine("P P")
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(this.out);
	}
	
	private void blockRecipes(){
		ShapedRecipeBuilder.shapedRecipe(IPContent.blockAsphalt, 8)
			.key('C', IPContent.itemBitumen)
			.key('S', Tags.Items.SAND)
			.key('G', Tags.Items.GRAVEL)
			.key('B', Items.WATER_BUCKET)
			.patternLine("SCS")
			.patternLine("GBG")
			.patternLine("SCS")
			.addCriterion("has_bitumen", hasItem(IPContent.itemBitumen))
			.build(this.out, rl("asphalt"));
		
		ShapedRecipeBuilder.shapedRecipe(IPContent.blockAsphalt, 12)
			.key('C', IPContent.itemBitumen)
			.key('S', IEItems.Ingredients.slag)
			.key('G', Tags.Items.GRAVEL)
			.key('B', Items.WATER_BUCKET)
			.patternLine("SCS")
			.patternLine("GBG")
			.patternLine("SCS")
			.addCriterion("has_bitumen", hasItem(IPContent.itemBitumen))
			.addCriterion("has_slag", hasItem(IEItems.Ingredients.slag))
			.build(this.out, rl("asphalt"));
	}
	
	private void itemRecipes(){
		ShapedRecipeBuilder.shapedRecipe(IPContent.itemOilCan)
			.key('R', Tags.Items.DYES_RED)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('B', Items.BUCKET)
			.patternLine(" R ")
			.patternLine("PBP")
			.addCriterion("has_rose_red", this.hasItem(Items.RED_DYE))
			.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
			.build(out);
		
		ShapedRecipeBuilder.shapedRecipe(IPContent.itemProjector)
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
		
		ShapedRecipeBuilder.shapedRecipe(IPContent.itemSpeedboat)
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
