package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ExcavatorTileEntity;
import blusunrize.immersiveengineering.common.util.IEPotions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricantEffect;
import flaxbeard.immersivepetroleum.common.blocks.BlockAsphalt;
import flaxbeard.immersivepetroleum.common.blocks.BlockDummy;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity.CrusherLubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity.ExcavatorLubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity.PumpjackLubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DistillationTowerMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.PumpjackMultiblock;
import flaxbeard.immersivepetroleum.common.items.IPItemBase;
import flaxbeard.immersivepetroleum.common.items.IPUpgradeItem;
import flaxbeard.immersivepetroleum.common.items.ItemOilCan;
import flaxbeard.immersivepetroleum.common.items.ItemProjector;
import flaxbeard.immersivepetroleum.common.items.ItemSpeedboat;
import flaxbeard.immersivepetroleum.common.util.fluids.IPFluid;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus=Bus.MOD)
public class IPContent{
	public static final Logger log=LogManager.getLogger(ImmersivePetroleum.MODID+"/Content");
	
	public static final List<Block> registeredIPBlocks = new ArrayList<>();
	public static final List<Item> registeredIPItems = new ArrayList<>();
	public static final List<Fluid> registeredIPFluids = new ArrayList<>();

	@Deprecated
	public static IPBlockBase blockStoneDecoration;
	public static IPBlockBase blockAsphalt;
	public static IPBlockBase blockMetalMultiblock; // BlockIPMultiblock
	public static IPBlockBase blockMetalDevice; // BlockIPMetalDevice
	
	public static BlockDummy dummyBlockOilOre;
	public static BlockDummy dummyBlockPipe;
	public static BlockDummy dummyBlockConveyor;

	public static IPFluid fluidCrudeOil;
	public static IPFluid fluidDiesel;
	public static IPFluid fluidLubricant;
	public static IPFluid fluidGasoline;
	public static IPFluid fluidNapalm;

	public static IPItemBase itemBitumen;
	public static IPItemBase itemProjector;
	public static IPItemBase itemSpeedboat;
	public static IPItemBase itemOilCan;
	
	public static IPUpgradeItem itemUpgradeHull;
	public static IPUpgradeItem itemUpgradeBreaker;
	public static IPUpgradeItem itemUpgradeTank;
	public static IPUpgradeItem itemUpgradeRudders;
	public static IPUpgradeItem itemUpgradePaddles;
	
	/** block/item/fluid population */
	public static void populate(){
		fluidCrudeOil = new IPFluid("oil",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/oil_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/oil_flow"), IPFluid.createBuilder(1000, 2250));

		fluidDiesel = new IPFluid("diesel",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/diesel_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/diesel_flow"), IPFluid.createBuilder(789, 1750));

		fluidLubricant = new IPFluid("lubricant",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/lubricant_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/lubricant_flow"), IPFluid.createBuilder(925, 1000));

		fluidGasoline = new IPFluid("gasoline",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/gasoline_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/gasoline_flow"), IPFluid.createBuilder(789, 1200));
		
		fluidNapalm = new IPFluid("napalm",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/napalm_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/napalm_flow"), IPFluid.createBuilder(1000, 4000));

		//blockMetalMultiblock = new BlockIPMetalMultiblocks();

		//blockMetalDevice = new BlockIPMetalDevice();
		
		blockAsphalt=new BlockAsphalt();
		
		dummyBlockOilOre=new BlockDummy("dummy_oil_ore");
		dummyBlockPipe=new BlockDummy("dummy_pipe");
		dummyBlockConveyor=new BlockDummy("dummy_conveyor");
		
		itemBitumen = new IPItemBase("bitumen");

		itemProjector = new ItemProjector("projector");
		itemSpeedboat = new ItemSpeedboat("speedboat");

		itemUpgradeHull = new IPUpgradeItem("reinforced_hull", "BOAT");
		itemUpgradeBreaker = new IPUpgradeItem("icebreaker", "BOAT");
		itemUpgradeTank = new IPUpgradeItem("tank", "BOAT");
		itemUpgradeRudders = new IPUpgradeItem("rudders", "BOAT");
		itemUpgradePaddles = new IPUpgradeItem("paddles", "BOAT");

		itemOilCan = new ItemOilCan("oil_can");
	}
	
	public static void preInit(){
		//EntityRegistry.registerModEntity(new ResourceLocation(ImmersivePetroleum.MODID, "speedboat"), EntitySpeedboat.class, "speedboat", 0, ImmersivePetroleum.INSTANCE, 80, 3, true);
		//EntityRegistry.registerModEntity(EntityBoatDrill.class, "boat_drill", 1, ImmersivePetroleum.INSTANCE, 80, 3, true);
	}
	
	public static void init(){
		//blockFluidCrudeOil.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 1));
		//blockFluidDiesel.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 1));
		//blockFluidLubricant.setPotionEffects(new PotionEffect(IEPotions.slippery, 100, 1));
		//blockFluidNapalm.setPotionEffects(new PotionEffect(IEPotions.flammable, 140, 2));

		ChemthrowerHandler.registerEffect(fluidLubricant, new LubricantEffect());
		ChemthrowerHandler.registerEffect(fluidLubricant, new ChemthrowerEffect_Potion(null, 0, IEPotions.slippery, 60, 1));
		ChemthrowerHandler.registerEffect(IEContent.fluidPlantoil, new LubricantEffect());
		ChemthrowerHandler.registerEffect(fluidGasoline, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 1));
		ChemthrowerHandler.registerFlammable(fluidGasoline);
		ChemthrowerHandler.registerEffect(fluidNapalm, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 2));
		ChemthrowerHandler.registerFlammable(fluidNapalm);

		//MultiblockHandler.registerMultiblock(MultiblockDistillationTower.instance);
		//MultiblockHandler.registerMultiblock(MultiblockPumpjack.instance);
		MultiblockHandler.registerMultiblock(DistillationTowerMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(PumpjackMultiblock.INSTANCE);


		//ForgeRegistries.RECIPES.register(new SchematicCraftingHandler().setRegistryName(ImmersivePetroleum.MODID, "projector"));

		IPConfig.addConfigReservoirs(IPConfig.EXTRACTION.reservoirs.get());
		IPConfig.addFuel(IPConfig.GENERATION.fuels.get());
		IPConfig.addBoatFuel(IPConfig.MISCELLANEOUS.boat_fuels.get());
		IPConfig.addDistillationRecipes(IPConfig.REFINING.towerRecipes.get(), IPConfig.REFINING.towerByproduct.get());

		ResourceLocation aluDust=IETags.getTagsFor(EnumMetals.ALUMINUM).dust.getId();
		MixerRecipe.addRecipe(
				new FluidStack(fluidNapalm, 500),
				new FluidStack(fluidGasoline, 500), new Object[]{aluDust, aluDust, aluDust}, 3200);

		LubricantHandler.registerLubricant(fluidLubricant, 3);
		LubricantHandler.registerLubricant(IEContent.fluidPlantoil, 12);

		LubricatedHandler.registerLubricatedTile(PumpjackTileEntity.class, new PumpjackLubricationHandler());
		LubricatedHandler.registerLubricatedTile(ExcavatorTileEntity.class, new ExcavatorLubricationHandler());
		LubricatedHandler.registerLubricatedTile(CrusherTileEntity.class, new CrusherLubricationHandler());
	}

	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event){
		//registerTile(event, DistillationTowerTileEntity.class);
		//registerTile(event, DistillationTowerTileEntity.TileEntityDistillationTowerParent.class);
		//registerTile(event, PumpjackTileEntity.class);
		//registerTile(event, PumpjackTileEntity.TileEntityPumpjackParent.class); // FIXME Causing problems
		//registerTile(event, GeneratorTileEntity.class);
		//registerTile(event, TileEntityAutoLubricator.class);
		//registerTile(event, TileEntityGasGenerator.class);
	}

	public static <T extends TileEntity> void registerTile(RegistryEvent.Register<TileEntityType<?>> event, Class<T> tile, Block... valid){
		String s = tile.getSimpleName();
		s = s.substring(0, s.indexOf("TileEntity")).toLowerCase(Locale.ENGLISH);
		
		Set<Block> validSet = new HashSet<>(Arrays.asList(valid));
		TileEntityType<T> type = new TileEntityType<>(() -> {
			try{
				return tile.newInstance();
			}catch(InstantiationException | IllegalAccessException e){
				e.printStackTrace();
			}
			return null;
		}, validSet, null);
		type.setRegistryName(ImmersivePetroleum.MODID, s);
		event.getRegistry().register(type);
		
		try{
			tile.getField("TYPE").set(null, type);
		}catch(NoSuchFieldException | IllegalAccessException e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event){
		for(Block block:registeredIPBlocks){
			try{
				event.getRegistry().register(block);
			}catch(Throwable e) {
				log.error("Failed to register a block. ({})", block);
				throw e;
			}
//			event.getRegistry().register(block.setRegistryName(createRegistryName(block.getTranslationKey())));
		}
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event){
		for(Item item:registeredIPItems){
			try{
				event.getRegistry().register(item);
			}catch(Throwable e) {
				log.error("Failed to register an item. ({}, {})", item, item.getRegistryName());
				throw e;
			}
//			event.getRegistry().register(item.setRegistryName(createRegistryName(item.getTranslationKey())));
		}
	}
	
	@SuppressWarnings("unused")
	private static ResourceLocation createRegistryName(String unlocalized){
		unlocalized = unlocalized.substring(unlocalized.indexOf("immersive"));
		unlocalized = unlocalized.replaceFirst("\\.", ":");
		return new ResourceLocation(unlocalized);
	}
}
