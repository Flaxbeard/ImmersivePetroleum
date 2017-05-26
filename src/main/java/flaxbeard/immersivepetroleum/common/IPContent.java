package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.util.IEPotions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPBase;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPFluid;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPMetalMultiblocks;
import flaxbeard.immersivepetroleum.common.blocks.ItemBlockIPBase;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.stone.BlockTypes_IPStoneDecoration;
import flaxbeard.immersivepetroleum.common.items.ItemIPBase;

public class IPContent
{
	public static ArrayList<Block> registeredIPBlocks = new ArrayList<Block>();

	public static BlockIPFluid blockFluidCrudeOil;
	public static BlockIPFluid blockFluidDiesel;
	public static BlockIPFluid blockFluidLubricant;

	public static BlockIPBase blockMetalMultiblock;
	
	public static BlockIPBase blockStoneDecoration;
	
	public static ArrayList<Item> registeredIPItems = new ArrayList<Item>();

	public static Item itemMaterial;

	public static Fluid fluidCrudeOil;
	public static Fluid fluidDiesel;
	public static Fluid fluidLubricant;

	public static void preInit()
	{
		fluidCrudeOil = new Fluid("oil", new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/oil_still"), new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/oil_flow")).setDensity(1000).setViscosity(2250);
		if(!FluidRegistry.registerFluid(fluidCrudeOil))
			fluidCrudeOil = FluidRegistry.getFluid("oil");
		FluidRegistry.addBucketForFluid(fluidCrudeOil);

		fluidDiesel = new Fluid("diesel", new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/diesel_still"), new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/diesel_flow")).setDensity(789).setViscosity(1750);
		if(!FluidRegistry.registerFluid(fluidDiesel))
			fluidDiesel = FluidRegistry.getFluid("diesel");
		FluidRegistry.addBucketForFluid(fluidDiesel);
		
		fluidLubricant = new Fluid("lubricant", new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/lubricant_still"), new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/lubricant_flow")).setDensity(925).setViscosity(2000);
		if(!FluidRegistry.registerFluid(fluidLubricant))
			fluidLubricant = FluidRegistry.getFluid("lubricant");
		FluidRegistry.addBucketForFluid(fluidLubricant);
		
		blockFluidCrudeOil = new BlockIPFluid("fluid_crude_oil", fluidCrudeOil, Material.WATER).setFlammability(60, 200);
		blockFluidDiesel = new BlockIPFluid("fluid_diesel", fluidDiesel, Material.WATER).setFlammability(60, 200);
		blockFluidLubricant = new BlockIPFluid("fluid_lubricant", fluidLubricant, Material.WATER);

		blockMetalMultiblock = new BlockIPMetalMultiblocks();

		blockStoneDecoration = (BlockIPBase)new BlockIPBase("stone_decoration", Material.ROCK, PropertyEnum.create("type", BlockTypes_IPStoneDecoration.class), ItemBlockIPBase.class).setHardness(2.0F).setResistance(10.0F);

		itemMaterial = new ItemIPBase("material", 64,
				"bitumen");

	}
	
	public static void init()
	{
		blockFluidCrudeOil.setPotionEffects(new PotionEffect(IEPotions.flammable,100,1));
		blockFluidDiesel.setPotionEffects(new PotionEffect(IEPotions.flammable,100,1));
		blockFluidLubricant.setPotionEffects(new PotionEffect(IEPotions.slippery,100,1));

		registerTile(TileEntityDistillationTower.class);
		registerTile(TileEntityDistillationTower.TileEntityDistillationTowerParent.class);
		registerTile(TileEntityPumpjack.class);
		registerTile(TileEntityPumpjack.TileEntityPumpjackParent.class);
		
		DistillationRecipe.addRecipe(new FluidStack[] { new FluidStack(fluidLubricant, 25), new FluidStack(fluidDiesel, 25) }, new ItemStack(itemMaterial, 1, 0), new FluidStack(fluidCrudeOil, 25), 2048, 1, .07F);

		MultiblockHandler.registerMultiblock(MultiblockDistillationTower.instance);
		MultiblockHandler.registerMultiblock(MultiblockPumpjack.instance);
		
		IERecipes.addIngredientRecipe(new ItemStack(blockStoneDecoration, 8, BlockTypes_IPStoneDecoration.ASPHALT.getMeta()), "SCS", "GBG", "SCS", 'C', new ItemStack(itemMaterial, 1, 0), 'S', "sand", 'G', Blocks.GRAVEL, 'B', new FluidStack(FluidRegistry.WATER,1000)).allowQuarterTurn();
		IERecipes.addIngredientRecipe(new ItemStack(blockStoneDecoration, 12, BlockTypes_IPStoneDecoration.ASPHALT.getMeta()), "SCS", "GBG", "SCS", 'C', new ItemStack(itemMaterial, 1, 0), 'S', "itemSlag", 'G', Blocks.GRAVEL, 'B', new FluidStack(FluidRegistry.WATER,1000)).allowQuarterTurn();
	

	}
	
	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersivePetroleum.MODID+":"+ s);
	}
}
