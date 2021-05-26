package flaxbeard.immersivepetroleum.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.ArcFurnaceRecipeBuilder;
import blusunrize.immersiveengineering.api.crafting.builders.BlastFurnaceFuelBuilder;
import blusunrize.immersiveengineering.api.crafting.builders.CrusherRecipeBuilder;
import blusunrize.immersiveengineering.api.crafting.builders.MixerRecipeBuilder;
import blusunrize.immersiveengineering.api.crafting.builders.SqueezerRecipeBuilder;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.data.recipebuilder.FluidAwareShapedRecipeBuilder;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.api.crafting.builders.CokerUnitRecipeBuilder;
import flaxbeard.immersivepetroleum.api.crafting.builders.DistillationRecipeBuilder;
import flaxbeard.immersivepetroleum.api.crafting.builders.ReservoirTypeBuilder;
import flaxbeard.immersivepetroleum.api.crafting.builders.SulfurRecoveryRecipeBuilder;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Blocks;
import flaxbeard.immersivepetroleum.common.IPContent.BoatUpgrades;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.data.SingleItemRecipeBuilder;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class IPRecipes extends RecipeProvider{
	private final Map<String, Integer> PATH_COUNT = new HashMap<>();
	
	protected Consumer<IFinishedRecipe> out;
	public IPRecipes(DataGenerator generatorIn){
		super(generatorIn);
	}
	
	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> out){
		this.out = out;
		
		itemRecipes();
		blockRecipes();
		speedboatUpgradeRecipes();
		distillationRecipes();
		cokerRecipes();
		hydrotreaterRecipes();
		reservoirs();
		
		MixerRecipeBuilder.builder(IPContent.Fluids.napalm, 500)
			.addFluidTag(IPTags.Fluids.gasoline, 500)
			.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.ALUMINUM).dust, 3))
			.setEnergy(3200)
			.build(this.out, rl("mixer/napalm"));
	}
	
	private void reservoirs(){
		ReservoirTypeBuilder.builder("aquifer", Fluids.WATER, 5000.000, 10000.000, 0.006, 30)
			.addDimensions(false, DimensionType.OVERWORLD.getLocation())
			.build(this.out, rl("reservoirs/aquifer"));
		
		ReservoirTypeBuilder.builder("oil", IPContent.Fluids.crudeOil, 2500.000, 15000.000, 0.006, 40)
			.addDimensions(true, DimensionType.THE_END.getLocation())
			.build(this.out, rl("reservoirs/oil"));
		
		ReservoirTypeBuilder.builder("lava", Fluids.LAVA, 250.000, 1000.000, 0.0, 30)
			.addDimensions(true, DimensionType.THE_END.getLocation())
			.build(this.out, rl("reservoirs/lava"));
	}
	
	private void distillationRecipes(){
		// setEnergy and setTime are 2048 and 1 by default. But still allows to be customized.
		
		DistillationRecipeBuilder.builder(new FluidStack[]{
				new FluidStack(IPContent.Fluids.lubricant, 9),
				new FluidStack(IPContent.Fluids.diesel_sulfur, 14),
				new FluidStack(IPContent.Fluids.gasoline, 39)})
			.addByproduct(new ItemStack(IPContent.Items.bitumen), 0.07)
			.addInput(IPTags.Fluids.crudeOil, 75)
			.setTimeAndEnergy(1, 2048)
			.build(this.out, rl("distillationtower/oilcracking"));
	}
	
	/** Contains everything related to Petcoke */
	private void cokerRecipes(){
		CokerUnitRecipeBuilder.builder(new ItemStack(IPContent.Items.petcoke), IPTags.Fluids.diesel_sulfur, 27)
			.addInputItem(IPTags.Items.bitumen, 2)
			.addInputFluid(FluidTags.WATER, 125)
			.setTimeAndEnergy(30, 1024)
			.build(this.out, rl("coking/petcoke"));
		
		// Petcoke Compression and Decompression
		ShapedRecipeBuilder.shapedRecipe(IPContent.Blocks.petcoke)
			.key('c', IPTags.Items.petcoke)
			.patternLine("ccc")
			.patternLine("ccc")
			.patternLine("ccc")
			.addCriterion("has_petcoke_item", hasItem(IPTags.Items.petcoke))
			.build(this.out, rl("petcoke_items_to_block"));
		ShapelessRecipeBuilder.shapelessRecipe(IPContent.Items.petcoke, 9)
			.addIngredient(IPTags.getItemTag(IPTags.Blocks.petcoke))
			.addCriterion("has_petcoke_block", hasItem(IPTags.getItemTag(IPTags.Blocks.petcoke)))
			.build(this.out, rl("petcoke_block_to_items"));
		
		// Registering Petcoke as Fuel for the Blastfurnace
		BlastFurnaceFuelBuilder.builder(IPTags.Items.petcoke)
			.setTime(1200)
			.build(this.out, rl("blastfurnace/fuel_petcoke"));
		BlastFurnaceFuelBuilder.builder(IPTags.getItemTag(IPTags.Blocks.petcoke))
			.setTime(12000)
			.build(this.out, rl("blastfurnace/fuel_petcoke_block"));
		
		// Petcoke Dust recipes
		CrusherRecipeBuilder.builder(IPTags.Items.petcokeDust, 1)
			.addInput(IPTags.Items.petcoke)
			.setEnergy(2400)
			.build(this.out, rl("crusher/petcoke"));
		CrusherRecipeBuilder.builder(IPTags.Items.petcokeDust, 9)
			.addInput(IPTags.Items.petcokeStorage)
			.setEnergy(4800)
			.build(this.out, rl("crusher/petcoke_block"));
		
		// Petcoke dust and Iron Ingot to make Steel Ingot
		ArcFurnaceRecipeBuilder.builder(IETags.getTagsFor(EnumMetals.STEEL).ingot, 1)
			.addIngredient("input", Tags.Items.INGOTS_IRON)
			.addInput(IPTags.Items.petcokeDust)
			.addSlag(IETags.slag, 1)
			.setTime(400)
			.setEnergy(204800)
			.build(out, rl("arcfurnace/steel"));
		
		// 8 Petcoke Dust to 1 HOP Graphite Dust
		SqueezerRecipeBuilder.builder()
			.addResult(new IngredientWithSize(IETags.hopGraphiteDust))
			.addInput(new IngredientWithSize(IPTags.Items.petcokeDust, 8))
			.setEnergy(19200)
			.build(out, rl("squeezer/graphite_dust"));
	}
	
	private void hydrotreaterRecipes(){
		SulfurRecoveryRecipeBuilder.builder(new FluidStack(IPContent.Fluids.diesel, 7), 512, 1)
			.addInputFluid(new FluidTagInput(IPTags.Fluids.diesel_sulfur, 7))
			.addSecondaryInputFluid(FluidTags.WATER, 7)
			.addItemWithChance(new ItemStack(IEItems.Ingredients.dustSulfur), 0.02)
			.build(out, rl("hydrotreater/sulfur_recovery"));
	}
	
	private void speedboatUpgradeRecipes(){
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.reinforced_hull)
			.key('P', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.key('B', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage))
			.patternLine("P P")
			.patternLine("PBP")
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_steel_block", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage)))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.ice_breaker)
			.key('P', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.key('I', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('B', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage))
			.patternLine("I P")
			.patternLine(" IP")
			.patternLine("PPB")
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_steel_block", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage)))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.tank)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('T', IEBlocks.MetalDevices.barrel)
			.patternLine(" P ")
			.patternLine("PTP")
			.patternLine(" P ")
			.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.rudders)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('R', IETags.ironRod)
			.patternLine(" RR")
			.patternLine("PPP")
			.patternLine("PPP")
			.addCriterion("has_iron_rod", hasItem(IETags.ironRod))
			.build(this.out);
		
		ShapedRecipeBuilder.shapedRecipe(BoatUpgrades.paddles)
			.key('P', IETags.getItemTag(IETags.treatedWood))
			.key('S', IETags.treatedStick)
			.patternLine("S S")
			.patternLine("S S")
			.patternLine("P P")
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(this.out);
	}
	
	private void blockRecipes(){
		FluidAwareShapedRecipeBuilder.builder(Blocks.asphalt, 8)
			.key('C', IPContent.Items.bitumen)
			.key('S', Tags.Items.SAND)
			.key('G', Tags.Items.GRAVEL)
			.key('B', new IngredientFluidStack(FluidTags.WATER, FluidAttributes.BUCKET_VOLUME))
			.patternLine("SCS")
			.patternLine("GBG")
			.patternLine("SCS")
			.addCriterion("has_bitumen", hasItem(IPContent.Items.bitumen))
			.addCriterion("has_slag", hasItem(IEItems.Ingredients.slag))
			.build(this.out, rl("asphalt"));
		
		FluidAwareShapedRecipeBuilder.builder(Blocks.asphalt, 12)
			.key('C', IPContent.Items.bitumen)
			.key('S', IEItems.Ingredients.slag)
			.key('G', Tags.Items.GRAVEL)
			.key('B', new IngredientFluidStack(FluidTags.WATER, FluidAttributes.BUCKET_VOLUME))
			.patternLine("SCS")
			.patternLine("GBG")
			.patternLine("SCS")
			.addCriterion("has_bitumen", hasItem(IPContent.Items.bitumen))
			.addCriterion("has_slag", hasItem(IEItems.Ingredients.slag))
			.build(this.out, rl("asphalt"));
		
		ShapedRecipeBuilder.shapedRecipe(Blocks.asphalt_stair, 6)
			.key('A', IPTags.getItemTag(IPTags.Blocks.asphalt))
			.patternLine("A  ")
			.patternLine("AA ")
			.patternLine("AAA")
			.addCriterion("has_bitumen", hasItem(IPContent.Items.bitumen))
			.addCriterion("has_slag", hasItem(IEItems.Ingredients.slag))
			.build(this.out, rl("asphalt_stair"));
		
		ShapedRecipeBuilder.shapedRecipe(Blocks.asphalt_slab, 6)
			.key('A', IPTags.getItemTag(IPTags.Blocks.asphalt))
			.patternLine("AAA")
			.addCriterion("has_bitumen", hasItem(IPContent.Items.bitumen))
			.addCriterion("has_slag", hasItem(IEItems.Ingredients.slag))
			.build(this.out, rl("asphalt_slab"));
		
		FluidAwareShapedRecipeBuilder.builder(Blocks.asphalt, 1)
			.key('S', Blocks.asphalt_slab)
			.patternLine("S")
			.patternLine("S")
			.addCriterion("has_bitumen", hasItem(IPContent.Items.bitumen))
			.addCriterion("has_slag", hasItem(IEItems.Ingredients.slag))
			.build(this.out, rl("asphalt"));
		
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(Blocks.asphalt), Blocks.asphalt_slab, 2)
			.addCriterion("has_asphalt", hasItem(Blocks.asphalt))
			.build(this.out, "asphalt_slab_from_asphalt_stonecutting");
		
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(Blocks.asphalt), Blocks.asphalt_stair)
			.addCriterion("has_asphalt", hasItem(Blocks.asphalt))
			.build(this.out, "asphalt_stairs_from_asphalt_stonecutting");
		
		
		ShapedRecipeBuilder.shapedRecipe(Blocks.gas_generator)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('G', IEBlocks.MetalDecoration.generator)
			.key('C', IEBlocks.MetalDevices.capacitorLV)
			.patternLine("PPP")
			.patternLine("PGC")
			.patternLine("PPP")
			.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
			.addCriterion("has_"+toPath(IEBlocks.MetalDevices.capacitorLV), hasItem(IEBlocks.MetalDevices.capacitorLV))
			.addCriterion("has_"+toPath(IEBlocks.MetalDecoration.generator), hasItem(IEBlocks.MetalDecoration.generator))
			.build(this.out, rl("gas_generator"));
		
		ShapedRecipeBuilder.shapedRecipe(Blocks.auto_lubricator)
			.key('G', Tags.Items.GLASS)
			.key('T', IETags.getItemTag(IETags.treatedWood))
			.key('P', IEBlocks.MetalDevices.fluidPipe)
			.patternLine(" G ")
			.patternLine("G G")
			.patternLine("TPT")
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.addCriterion("has_"+toPath(IEBlocks.MetalDevices.fluidPipe), hasItem(IEBlocks.MetalDevices.fluidPipe))
			.build(this.out, rl("auto_lubricator"));
		
		ShapedRecipeBuilder.shapedRecipe(Blocks.flarestack)
			.key('I', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('C', IEItems.Ingredients.componentSteel)
			.key('P', IEBlocks.MetalDevices.fluidPipe)
			.key('A', IEBlocks.MetalDevices.fluidPlacer)
			.key('F', Items.FLINT_AND_STEEL)
			.patternLine("IFI")
			.patternLine("CAC")
			.patternLine("IPI")
			.addCriterion("has_bitumen", hasItem(IPContent.Items.bitumen))
			.addCriterion("has_"+toPath(IEBlocks.MetalDevices.fluidPipe), hasItem(IEBlocks.MetalDevices.fluidPipe))
			.build(this.out, rl("flarestack"));
	}
	
	private void itemRecipes(){
		ShapedRecipeBuilder.shapedRecipe(IPContent.Items.oil_can)
			.key('R', Tags.Items.DYES_RED)
			.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('B', Items.BUCKET)
			.patternLine(" R ")
			.patternLine("PBP")
			.addCriterion("has_rose_red", hasItem(Items.RED_DYE))
			.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
			.build(out);
		
		ShapedRecipeBuilder.shapedRecipe(IPContent.Items.projector)
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
		
		ShapedRecipeBuilder.shapedRecipe(IPContent.Items.speedboat)
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
			int count = PATH_COUNT.get(str) + 1;
			PATH_COUNT.put(str, count);
			return new ResourceLocation(ImmersivePetroleum.MODID, str + count);
		}
		PATH_COUNT.put(str, 1);
		return new ResourceLocation(ImmersivePetroleum.MODID, str);
	}
	
	private String toPath(IItemProvider src){
		return src.asItem().getRegistryName().getPath();
	}
	
}
