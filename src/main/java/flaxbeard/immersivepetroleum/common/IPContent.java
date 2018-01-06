package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;

import flaxbeard.immersivepetroleum.common.fluid.FluidDiesel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.util.IEPotions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.items.ItemOilCan;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricantEffect;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPBase;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPFluid;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPMetalDevice;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPMetalMultiblocks;
import flaxbeard.immersivepetroleum.common.blocks.ItemBlockIPBase;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_Dummy;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator.CrusherLubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator.ExcavatorLubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator.PumpjackLubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityGasGenerator;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.stone.BlockTypes_IPStoneDecoration;
import flaxbeard.immersivepetroleum.common.entity.EntitySpeedboat;
import flaxbeard.immersivepetroleum.common.items.ItemIPBase;
import flaxbeard.immersivepetroleum.common.items.ItemIPUpgrade;
import flaxbeard.immersivepetroleum.common.items.ItemProjector;
import flaxbeard.immersivepetroleum.common.items.ItemSpeedboat;

@Mod.EventBusSubscriber(modid=ImmersivePetroleum.MODID)
public class IPContent
{
	public static ArrayList<Block> registeredIPBlocks = new ArrayList<Block>();

	public static BlockIPFluid blockFluidCrudeOil;
	public static BlockIPFluid blockFluidDiesel;
	public static BlockIPFluid blockFluidLubricant;
	public static BlockIPFluid blockFluidGasoline;

	public static BlockIPBase blockMetalMultiblock;
	
	public static BlockIPBase blockStoneDecoration;
	
	public static BlockIPBase blockMetalDevice;

	public static BlockIPBase blockDummy;

	
	public static ArrayList<Item> registeredIPItems = new ArrayList<Item>();

	public static Item itemMaterial;
	public static Item itemProjector;
	public static Item itemSpeedboat;
	public static Item itemUpgrades;
	public static Item itemOilCan;

	public static Fluid fluidCrudeOil;
	public static Fluid fluidDiesel;
	public static Fluid fluidLubricant;
	public static Fluid fluidGasoline;

	public static void preInit()
	{
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersivePetroleum.MODID, "speedboat"), EntitySpeedboat.class, "speedboat", 0, ImmersivePetroleum.INSTANCE, 80, 3, true);
		//EntityRegistry.registerModEntity(EntityBoatDrill.class, "boat_drill", 1, ImmersivePetroleum.INSTANCE, 80, 3, true);

		fluidCrudeOil = new Fluid("oil", new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/oil_still"), new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/oil_flow")).setDensity(1000).setViscosity(2250);
		if(!FluidRegistry.registerFluid(fluidCrudeOil))
			fluidCrudeOil = FluidRegistry.getFluid("oil");
		FluidRegistry.addBucketForFluid(fluidCrudeOil);

		fluidDiesel = new FluidDiesel("diesel", new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/diesel_still"), new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/diesel_flow")).setDensity(789).setViscosity(1750);
		if(!FluidRegistry.registerFluid(fluidDiesel))
			fluidDiesel = FluidRegistry.getFluid("diesel");
		FluidRegistry.addBucketForFluid(fluidDiesel);
		
		fluidLubricant = new Fluid("lubricant", new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/lubricant_still"), new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/lubricant_flow")).setDensity(925).setViscosity(1000);
		if(!FluidRegistry.registerFluid(fluidLubricant))
			fluidLubricant = FluidRegistry.getFluid("lubricant");
		FluidRegistry.addBucketForFluid(fluidLubricant);
		
		fluidGasoline = new Fluid("gasoline", new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/gasoline_still"), new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/gasoline_flow")).setDensity(789).setViscosity(1200);
		if(!FluidRegistry.registerFluid(fluidGasoline))
			fluidGasoline = FluidRegistry.getFluid("gasoline");
		FluidRegistry.addBucketForFluid(fluidGasoline);
		
		blockFluidCrudeOil = new BlockIPFluid("fluid_crude_oil", fluidCrudeOil, Material.WATER).setFlammability(60, 200);
		blockFluidDiesel = new BlockIPFluid("fluid_diesel", fluidDiesel, Material.WATER).setFlammability(60, 200);
		blockFluidLubricant = new BlockIPFluid("fluid_lubricant", fluidLubricant, Material.WATER);
		blockFluidGasoline = new BlockIPFluid("fluid_gasoline", fluidGasoline, Material.WATER).setFlammability(60, 200);

		blockMetalMultiblock = new BlockIPMetalMultiblocks();
		
		blockMetalDevice = new BlockIPMetalDevice();

		blockStoneDecoration = (BlockIPBase)new BlockIPBase("stone_decoration", Material.ROCK, PropertyEnum.create("type", BlockTypes_IPStoneDecoration.class), ItemBlockIPBase.class).setHardness(2.0F).setResistance(10.0F);
		blockDummy = (BlockIPBase)new BlockIPBase("dummy", Material.ROCK, PropertyEnum.create("type", BlockTypes_Dummy.class), ItemBlockIPBase.class);

		itemMaterial = new ItemIPBase("material", 64,
				"bitumen");
		
		itemProjector = new ItemProjector("schematic");
		itemSpeedboat = new ItemSpeedboat("speedboat");
		
		itemUpgrades = new ItemIPUpgrade("upgrades");
		
		itemOilCan = new ItemOilCan("oil_can");

	}
	
	public static void init()
	{
		blockFluidCrudeOil.setPotionEffects(new PotionEffect(IEPotions.flammable,100,1));
		blockFluidDiesel.setPotionEffects(new PotionEffect(IEPotions.flammable,100,1));
		blockFluidLubricant.setPotionEffects(new PotionEffect(IEPotions.slippery,100,1));
		ChemthrowerHandler.registerEffect("lubricant", new LubricantEffect());
		ChemthrowerHandler.registerEffect("plantoil", new LubricantEffect());
		ChemthrowerHandler.registerEffect("gasoline", new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,60,1));
		ChemthrowerHandler.registerFlammable("gasoline");
		ChemthrowerHandler.registerEffect("lubricant", new ChemthrowerEffect_Potion(null,0, IEPotions.slippery,60,1));
		
		registerTile(TileEntityDistillationTower.class);
		registerTile(TileEntityDistillationTower.TileEntityDistillationTowerParent.class);
		registerTile(TileEntityPumpjack.class);
		registerTile(TileEntityPumpjack.TileEntityPumpjackParent.class);
		registerTile(TileEntityAutoLubricator.class);
		registerTile(TileEntityGasGenerator.class);
//
//		DistillationRecipe.addRecipe(
//				new FluidStack[] { 
//					new FluidStack(fluidLubricant, 3),
//					new FluidStack(fluidDiesel, 9),
//					new FluidStack(fluidGasoline, 13)
//				},
//				new ItemStack(itemMaterial, 1, 0), new FluidStack(fluidCrudeOil, 25), 2048, 1, .07F);

		MultiblockHandler.registerMultiblock(MultiblockDistillationTower.instance);
		MultiblockHandler.registerMultiblock(MultiblockPumpjack.instance);
		
		//IERecipes.addIngredientRecipe(new ItemStack(blockStoneDecoration, 8, BlockTypes_IPStoneDecoration.ASPHALT.getMeta()), "SCS", "GBG", "SCS", 'C', new ItemStack(itemMaterial, 1, 0), 'S', "sand", 'G', Blocks.GRAVEL, 'B', new FluidStack(FluidRegistry.WATER,1000)).allowQuarterTurn();
		//IERecipes.addIngredientRecipe(new ItemStack(blockStoneDecoration, 12, BlockTypes_IPStoneDecoration.ASPHALT.getMeta()), "SCS", "GBG", "SCS", 'C', new ItemStack(itemMaterial, 1, 0), 'S', "itemSlag", 'G', Blocks.GRAVEL, 'B', new FluidStack(FluidRegistry.WATER,1000)).allowQuarterTurn();
	
	
			/*IERecipes.addIngredientRecipe(new ItemStack(itemProjector, 1, 0), "S  ", "IL ", " IW", 
					'W', "plankTreatedWood", 
					'L', new ItemStack(IEContent.blockMetalDecoration2, 1, BlockTypes_MetalDecoration2.LANTERN.getMeta()), 
					'S', "blockGlassColorless",
					'I', "ingotIron");*/
		ForgeRegistries.RECIPES.register(new SchematicCraftingHandler().setRegistryName(ImmersivePetroleum.MODID, "projector"));

		/*
		
<<<<<<< HEAD
		IERecipes.addIngredientRecipe(new ItemStack(itemSpeedboat, 1, 0), "PME", "PPP", 
				'P', "plankTreatedWood", 
				'E', new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()), 
				'M', new ItemStack(IEContent.itemMaterial, 1, 8));
=======
		if (!IPConfig.Miscellaneous.disable_motorboat)
		{
			IERecipes.addIngredientRecipe(new ItemStack(itemSpeedboat, 1, 0), "PME", "PPP", 
					'P', "plankTreatedWood", 
					'E', new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()), 
					'M', new ItemStack(IEContent.itemMaterial, 1, 8));
			
			IERecipes.addIngredientRecipe(new ItemStack(itemUpgrades, 1, 0), "P P", "PBP", 
					'P', "plateSteel",
					'B', "blockSteel");
			
			IERecipes.addIngredientRecipe(new ItemStack(itemUpgrades, 1, 1), "I P", " IP", "PPB", 
					'P', "plateSteel",
					'B', "blockSteel",
					'I', "ingotSteel");
			
			IERecipes.addIngredientRecipe(new ItemStack(itemUpgrades, 1, 2), " P ", "PTP", " P ",
					'P', "plateIron",
					'T', new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.BARREL.getMeta()));
			
			IERecipes.addIngredientRecipe(new ItemStack(itemUpgrades, 1, 3), " RR", "PPP", "PPP",
					'P', "plateIron",
					'R', "stickIron");
			
			IERecipes.addIngredientRecipe(new ItemStack(itemUpgrades, 1, 4), "S S", "S S", "P P",
					'P', "plankTreatedWood",
					'S', "stickTreatedWood");
		}
>>>>>>> 1.11
		
		IERecipes.addIngredientRecipe(new ItemStack(itemUpgrades, 1, 0), "P P", "PBP", 
				'P', "plateSteel",
				'B', "blockSteel");
		
		IERecipes.addIngredientRecipe(new ItemStack(itemUpgrades, 1, 1), "I P", " IP", "PPB", 
				'P', "plateSteel",
				'B', "blockSteel",
				'I', "ingotSteel");
		
		IERecipes.addIngredientRecipe(new ItemStack(itemUpgrades, 1, 2), " P ", "PTP", " P ",
				'P', "plateIron",
				'T', new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.BARREL.getMeta()));
		
		IERecipes.addIngredientRecipe(new ItemStack(itemUpgrades, 1, 3), " RR", "PPP", "PPP",
				'P', "plateIron",
				'R', "stickIron");
				
				*/


			
		Config.addConfigReservoirs(IPConfig.extraction.reservoirs);
		Config.addFuel(IPConfig.Generation.fuels);
		Config.addBoatFuel(IPConfig.Miscellaneous.boat_fuels);
		Config.addDistillationRecipes(IPConfig.Refining.towerRecipes, IPConfig.Refining.towerByproduct);

		LubricantHandler.registerLubricant(fluidLubricant, 3);
		LubricantHandler.registerLubricant(IEContent.fluidPlantoil, 12);

		LubricatedHandler.registerLubricatedTile(TileEntityPumpjack.class, new PumpjackLubricationHandler());
		LubricatedHandler.registerLubricatedTile(TileEntityExcavator.class, new ExcavatorLubricationHandler());
		LubricatedHandler.registerLubricatedTile(TileEntityCrusher.class, new CrusherLubricationHandler());

	}
	
	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersivePetroleum.MODID+":"+ s);
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		for(Block block : registeredIPBlocks)
			event.getRegistry().register(block.setRegistryName(createRegistryName(block.getUnlocalizedName())));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		for(Item item : registeredIPItems)
			event.getRegistry().register(item.setRegistryName(createRegistryName(item.getUnlocalizedName())));
	}

	private static ResourceLocation createRegistryName(String unlocalized)
	{
		unlocalized = unlocalized.substring(unlocalized.indexOf("immersive"));
		unlocalized = unlocalized.replaceFirst("\\.", ":");
		return new ResourceLocation(unlocalized);
	}
	
}
