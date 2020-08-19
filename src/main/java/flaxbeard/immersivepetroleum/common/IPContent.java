package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ExcavatorTileEntity;
import blusunrize.immersiveengineering.common.util.IEPotions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricantEffect;
import flaxbeard.immersivepetroleum.common.blocks.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.blocks.BlockDummy;
import flaxbeard.immersivepetroleum.common.blocks.GasGeneratorBlock;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorNewTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity.DistillationTowerParentTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.GasGeneratorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity.PumpjackParentTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DistillationTowerMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.PumpjackMultiblock;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import flaxbeard.immersivepetroleum.common.items.IPItemBase;
import flaxbeard.immersivepetroleum.common.items.IPUpgradeItem;
import flaxbeard.immersivepetroleum.common.items.OilCanItem;
import flaxbeard.immersivepetroleum.common.items.ProjectorItem;
import flaxbeard.immersivepetroleum.common.items.SpeedboatItem;
import flaxbeard.immersivepetroleum.common.lubehandlers.CrusherLubricationHandler;
import flaxbeard.immersivepetroleum.common.lubehandlers.ExcavatorLubricationHandler;
import flaxbeard.immersivepetroleum.common.lubehandlers.PumpjackLubricationHandler;
import flaxbeard.immersivepetroleum.common.util.fluids.IPFluid;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus=Bus.MOD)
public class IPContent{
	public static final Logger log=LogManager.getLogger(ImmersivePetroleum.MODID+"/Content");
	
	public static final List<Block> registeredIPBlocks = new ArrayList<>();
	public static final List<Item> registeredIPItems = new ArrayList<>();
	public static final List<Fluid> registeredIPFluids = new ArrayList<>();
	
	public static class Multiblock{
		public static Block distillationtower;
		public static Block pumpjack;
	}
	
	public static class Fluids{
		public static IPFluid fluidCrudeOil;
		public static IPFluid fluidDiesel;
		public static IPFluid fluidLubricant;
		public static IPFluid fluidGasoline;
		public static IPFluid fluidNapalm;
	}
	
	public static class Blocks{
		public static IPBlockBase blockAsphalt;
		
		public static IPBlockBase blockGasGenerator;
		public static IPBlockBase blockAutolubricator;
		
		public static BlockDummy dummyBlockOilOre;
		public static BlockDummy dummyBlockPipe;
		public static BlockDummy dummyBlockConveyor;
	}
	
	public static class Items{
		public static IPItemBase itemBitumen;
		public static IPItemBase itemProjector;
		public static IPItemBase itemSpeedboat;
		public static IPItemBase itemOilCan;
	}
	
	public static class BoatUpgrades{
		public static IPUpgradeItem itemUpgradeHull;
		public static IPUpgradeItem itemUpgradeBreaker;
		public static IPUpgradeItem itemUpgradeTank;
		public static IPUpgradeItem itemUpgradeRudders;
		public static IPUpgradeItem itemUpgradePaddles;
	}

	public static DebugItem debugItem;
	
	/** block/item/fluid population */
	public static void populate(){
		IPContent.debugItem=new DebugItem();
		
		Fluids.fluidCrudeOil = new IPFluid("oil",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/oil_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/oil_flow"), IPFluid.createBuilder(1000, 2250));
		
		Fluids.fluidDiesel = new IPFluid("diesel",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/diesel_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/diesel_flow"), IPFluid.createBuilder(789, 1750));
		
		Fluids.fluidLubricant = new IPFluid("lubricant",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/lubricant_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/lubricant_flow"), IPFluid.createBuilder(925, 1000));
		
		Fluids.fluidGasoline = new IPFluid("gasoline",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/gasoline_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/gasoline_flow"), IPFluid.createBuilder(789, 1200));
		
		Fluids.fluidNapalm = new IPFluid("napalm",
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/napalm_still"),
				new ResourceLocation(ImmersivePetroleum.MODID, "block/fluid/napalm_flow"), IPFluid.createBuilder(1000, 4000));
		
		//blockMetalDevice = new BlockIPMetalDevice();
		
		Blocks.blockAsphalt=new IPBlockBase("asphalt", Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F, 10.0F));
		Blocks.blockGasGenerator=new GasGeneratorBlock();
		
		Blocks.blockAutolubricator=new AutoLubricatorBlock("auto_lubricator");
		
		Blocks.dummyBlockOilOre=new BlockDummy("dummy_oil_ore");
		Blocks.dummyBlockPipe=new BlockDummy("dummy_pipe");
		Blocks.dummyBlockConveyor=new BlockDummy("dummy_conveyor");
		
		Items.itemBitumen = new IPItemBase("bitumen");
		
		Items.itemProjector = new ProjectorItem("projector");
		Items.itemSpeedboat = new SpeedboatItem("speedboat");
		
		BoatUpgrades.itemUpgradeHull = new IPUpgradeItem("reinforced_hull", "BOAT");
		BoatUpgrades.itemUpgradeBreaker = new IPUpgradeItem("icebreaker", "BOAT");
		BoatUpgrades.itemUpgradeTank = new IPUpgradeItem("tank", "BOAT");
		BoatUpgrades.itemUpgradeRudders = new IPUpgradeItem("rudders", "BOAT");
		BoatUpgrades.itemUpgradePaddles = new IPUpgradeItem("paddles", "BOAT");
		
		Items.itemOilCan = new OilCanItem("oil_can");
		
		Multiblock.distillationtower=new DistillationTowerBlock();
		Multiblock.pumpjack=new PumpjackBlock();
	}
	
	public static void preInit(){
	}
	
	public static void init(){
		//blockFluidCrudeOil.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 1));
		//blockFluidDiesel.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 1));
		//blockFluidLubricant.setPotionEffects(new PotionEffect(IEPotions.slippery, 100, 1));
		//blockFluidNapalm.setPotionEffects(new PotionEffect(IEPotions.flammable, 140, 2));
		
		ChemthrowerHandler.registerEffect(Fluids.fluidLubricant, new LubricantEffect());
		ChemthrowerHandler.registerEffect(Fluids.fluidLubricant, new ChemthrowerEffect_Potion(null, 0, IEPotions.slippery, 60, 1));
		ChemthrowerHandler.registerEffect(IEContent.fluidPlantoil, new LubricantEffect());
		ChemthrowerHandler.registerEffect(Fluids.fluidGasoline, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 1));
		ChemthrowerHandler.registerFlammable(Fluids.fluidGasoline);
		ChemthrowerHandler.registerEffect(Fluids.fluidNapalm, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 2));
		ChemthrowerHandler.registerFlammable(Fluids.fluidNapalm);
		
		MultiblockHandler.registerMultiblock(DistillationTowerMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(PumpjackMultiblock.INSTANCE);
		
		IPConfig.Utils.addFuel(IPConfig.GENERATION.fuels.get());
		IPConfig.Utils.addBoatFuel(IPConfig.MISCELLANEOUS.boat_fuels.get());
		
		// TODO SchematicCraftingHandler
		//ForgeRegistries.RECIPES.register(new SchematicCraftingHandler().setRegistryName(ImmersivePetroleum.MODID, "projector"));
		
		LubricantHandler.registerLubricant(Fluids.fluidLubricant, 3);
		LubricantHandler.registerLubricant(IEContent.fluidPlantoil, 12);
		
		LubricatedHandler.registerLubricatedTile(PumpjackTileEntity.class, new PumpjackLubricationHandler());
		LubricatedHandler.registerLubricatedTile(ExcavatorTileEntity.class, new ExcavatorLubricationHandler());
		LubricatedHandler.registerLubricatedTile(CrusherTileEntity.class, new CrusherLubricationHandler());
	}

	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event){
		registerTile(event, DistillationTowerTileEntity.class, Multiblock.distillationtower);
		registerTile(event, DistillationTowerParentTileEntity.class, Multiblock.distillationtower);
		
		registerTile(event, PumpjackTileEntity.class, Multiblock.pumpjack);
		registerTile(event, PumpjackParentTileEntity.class, Multiblock.pumpjack);
		
		registerTile(event, GasGeneratorTileEntity.class, Blocks.blockGasGenerator);
		
		//registerTile(event, AutoLubricatorTileEntity.class, blockhere);
		registerTile(event, AutoLubricatorNewTileEntity.class, Blocks.blockAutolubricator);
	}
	
	/**
	 * 
	 * @param event
	 * @param tile the TileEntity class to register.<pre>Requires <code>public static TileEntityType TYPE;</code> field in the class.</pre>
	 * @param valid
	 */
	public static <T extends TileEntity> void registerTile(RegistryEvent.Register<TileEntityType<?>> event, Class<T> tile, Block... valid){
		String s = tile.getSimpleName();
		s = s.substring(0, s.indexOf("TileEntity")).toLowerCase(Locale.ENGLISH);
		
		TileEntityType<T> type = createType(tile, valid);
		type.setRegistryName(ImmersivePetroleum.MODID, s);
		event.getRegistry().register(type);
		
		try{
			tile.getField("TYPE").set(null, type);
		}catch(NoSuchFieldException | IllegalAccessException e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		log.info("Registered TileEntity: {} as {}", tile, type.getRegistryName());
	}
	
	private static  <T extends TileEntity> TileEntityType<T> createType(Class<T> typeClass, Block... valid){
		Set<Block> validSet = new HashSet<>(Arrays.asList(valid));
		TileEntityType<T> type = new TileEntityType<>(() -> {
			try{
				return typeClass.newInstance();
			}catch(InstantiationException | IllegalAccessException e){
				e.printStackTrace();
			}
			return null;
		}, validSet, null);
		
		return type;
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
		}
	}
	
	@SubscribeEvent
	public static void registerFluids(RegistryEvent.Register<Fluid> event){
		for(Fluid fluid:registeredIPFluids){
			try{
				event.getRegistry().register(fluid);
			}catch(Throwable e) {
				log.error("Failed to register a fluid. ({}, {})", fluid, fluid.getRegistryName());
				throw e;
			}
		}
	}
	
	@SubscribeEvent
	public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event){
		try{
			event.getRegistry().register(SpeedboatEntity.TYPE);
		}catch(Throwable e){
			log.error("Failed to register Speedboat Entity. {}", e.getMessage());
			throw e;
		}
	}
}
