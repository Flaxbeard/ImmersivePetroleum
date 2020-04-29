package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.util.IEPotions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricantEffect;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
import flaxbeard.immersivepetroleum.common.blocks.*;
import flaxbeard.immersivepetroleum.common.blocks.metal.*;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator.CrusherLubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator.ExcavatorLubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator.PumpjackLubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.stone.BlockTypes_IPStoneDecoration;
import flaxbeard.immersivepetroleum.common.entity.EntitySpeedboat;
import flaxbeard.immersivepetroleum.common.items.*;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID)
public class IPContent
{
	public static ArrayList<Block> registeredIPBlocks = new ArrayList<Block>();

	public static BlockIPFluid blockFluidCrudeOil;
	public static BlockIPFluid blockFluidDiesel;
	public static BlockIPFluid blockFluidLubricant;
	public static BlockIPFluid blockFluidGasoline;
	public static BlockIPFluid blockFluidNapalm;

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
	public static Fluid fluidNapalm;

	public static void preInit()
	{
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersivePetroleum.MODID, "speedboat"), EntitySpeedboat.class, "speedboat", 0, ImmersivePetroleum.INSTANCE, 80, 3, true);
		//EntityRegistry.registerModEntity(EntityBoatDrill.class, "boat_drill", 1, ImmersivePetroleum.INSTANCE, 80, 3, true);

		fluidCrudeOil = new Fluid(
				"oil",
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/oil_still"),
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/oil_flow")
		).setDensity(1000).setViscosity(2250);
		if (!FluidRegistry.registerFluid(fluidCrudeOil))
			fluidCrudeOil = FluidRegistry.getFluid("oil");
		FluidRegistry.addBucketForFluid(fluidCrudeOil);

		fluidDiesel = new Fluid(
				"diesel",
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/diesel_still"),
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/diesel_flow")
		).setDensity(789).setViscosity(1750);
		if (!FluidRegistry.registerFluid(fluidDiesel))
			fluidDiesel = FluidRegistry.getFluid("diesel");
		FluidRegistry.addBucketForFluid(fluidDiesel);

		fluidLubricant = new Fluid(
				"lubricant",
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/lubricant_still"),
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/lubricant_flow")
		).setDensity(925).setViscosity(1000);
		if (!FluidRegistry.registerFluid(fluidLubricant))
			fluidLubricant = FluidRegistry.getFluid("lubricant");
		FluidRegistry.addBucketForFluid(fluidLubricant);

		fluidGasoline = new Fluid(
				"gasoline",
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/gasoline_still"),
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/gasoline_flow")
		).setDensity(789).setViscosity(1200);
		if (!FluidRegistry.registerFluid(fluidGasoline))
			fluidGasoline = FluidRegistry.getFluid("gasoline");
		FluidRegistry.addBucketForFluid(fluidGasoline);

		fluidNapalm = new Fluid(
				"napalm",
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/napalm_still"),
				new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/napalm_flow")
		).setDensity(1000).setViscosity(4000);
		if (!FluidRegistry.registerFluid(fluidNapalm))
			fluidNapalm = FluidRegistry.getFluid("napalm");
		FluidRegistry.addBucketForFluid(fluidNapalm);

		blockFluidCrudeOil = new BlockIPFluid("fluid_crude_oil", fluidCrudeOil, Material.WATER).setFlammability(60, 200);
		blockFluidDiesel = new BlockIPFluid("fluid_diesel", fluidDiesel, Material.WATER).setFlammability(60, 600);
		blockFluidLubricant = new BlockIPFluid("fluid_lubricant", fluidLubricant, Material.WATER);
		blockFluidGasoline = new BlockIPFluid("fluid_gasoline", fluidGasoline, Material.WATER).setFlammability(60, 200);
		blockFluidNapalm = new BlockNapalm("fluid_napalm", fluidNapalm, Material.WATER).setFlammability(100, 600);

		blockMetalMultiblock = new BlockIPMetalMultiblocks();

		blockMetalDevice = new BlockIPMetalDevice();

		blockStoneDecoration = (BlockIPBase) new BlockIPBase("stone_decoration", Material.ROCK, PropertyEnum.create("type", BlockTypes_IPStoneDecoration.class), ItemBlockIPBase.class).setHardness(2.0F).setResistance(10.0F);
		blockDummy = (BlockIPBase) new BlockIPBase("dummy", Material.ROCK, PropertyEnum.create("type", BlockTypes_Dummy.class), ItemBlockIPBase.class);

		itemMaterial = new ItemIPBase("material", 64,
				"bitumen");

		itemProjector = new ItemProjector("schematic");
		itemSpeedboat = new ItemSpeedboat("speedboat");

		itemUpgrades = new ItemIPUpgrade("upgrades");

		itemOilCan = new ItemOilCan("oil_can");

	}

	public static void init()
	{
		blockFluidCrudeOil.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 1));
		blockFluidDiesel.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 1));
		blockFluidLubricant.setPotionEffects(new PotionEffect(IEPotions.slippery, 100, 1));
		blockFluidNapalm.setPotionEffects(new PotionEffect(IEPotions.flammable, 140, 2));

		ChemthrowerHandler.registerEffect("lubricant", new LubricantEffect());
		ChemthrowerHandler.registerEffect("plantoil", new LubricantEffect());
		ChemthrowerHandler.registerEffect("gasoline", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 1));
		ChemthrowerHandler.registerFlammable("gasoline");
		ChemthrowerHandler.registerEffect("lubricant", new ChemthrowerEffect_Potion(null, 0, IEPotions.slippery, 60, 1));
		ChemthrowerHandler.registerEffect("napalm", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 2));
		ChemthrowerHandler.registerFlammable("napalm");

		registerTile(TileEntityDistillationTower.class);
		registerTile(TileEntityDistillationTower.TileEntityDistillationTowerParent.class);
		registerTile(TileEntityPumpjack.class);
		registerTile(TileEntityPumpjack.TileEntityPumpjackParent.class);
		registerTile(TileEntityAutoLubricator.class);
		registerTile(TileEntityGasGenerator.class);

		MultiblockHandler.registerMultiblock(MultiblockDistillationTower.instance);
		MultiblockHandler.registerMultiblock(MultiblockPumpjack.instance);


		ForgeRegistries.RECIPES.register(new SchematicCraftingHandler().setRegistryName(ImmersivePetroleum.MODID, "projector"));

		Config.addConfigReservoirs(IPConfig.extraction.reservoirs);
		Config.addFuel(IPConfig.Generation.fuels);
		Config.addBoatFuel(IPConfig.Miscellaneous.boat_fuels);
		Config.addDistillationRecipes(IPConfig.Refining.towerRecipes, IPConfig.Refining.towerByproduct);

		MixerRecipe.addRecipe(new FluidStack(fluidNapalm, 500), new FluidStack(fluidGasoline, 500), new Object[]{"dustAluminum", "dustAluminum", "dustAluminum"}, 3200);

		LubricantHandler.registerLubricant(fluidLubricant, 3);
		LubricantHandler.registerLubricant(IEContent.fluidPlantoil, 12);

		LubricatedHandler.registerLubricatedTile(TileEntityPumpjack.class, new PumpjackLubricationHandler());
		LubricatedHandler.registerLubricatedTile(TileEntityExcavator.class, new ExcavatorLubricationHandler());
		LubricatedHandler.registerLubricatedTile(TileEntityCrusher.class, new CrusherLubricationHandler());
	}

	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity") + "TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersivePetroleum.MODID + ":" + s);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		for (Block block : registeredIPBlocks)
		{
			event.getRegistry().register(block.setRegistryName(createRegistryName(block.getTranslationKey())));
		}
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		for (Item item : registeredIPItems)
		{
			event.getRegistry().register(item.setRegistryName(createRegistryName(item.getTranslationKey())));
		}
	}

	private static ResourceLocation createRegistryName(String unlocalized)
	{
		unlocalized = unlocalized.substring(unlocalized.indexOf("immersive"));
		unlocalized = unlocalized.replaceFirst("\\.", ":");
		return new ResourceLocation(unlocalized);
	}

}
