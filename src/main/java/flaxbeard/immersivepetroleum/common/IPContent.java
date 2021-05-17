package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ExcavatorTileEntity;
import blusunrize.immersiveengineering.common.util.IEPotions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.api.crafting.FlarestackHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricantEffect;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockDummy;
import flaxbeard.immersivepetroleum.common.blocks.metal.CokerUnitBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.FlarestackBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.GasGeneratorBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.HydrotreaterBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackBlock;
import flaxbeard.immersivepetroleum.common.blocks.stone.AsphaltBlock;
import flaxbeard.immersivepetroleum.common.blocks.stone.AsphaltSlab;
import flaxbeard.immersivepetroleum.common.blocks.stone.AsphaltStairs;
import flaxbeard.immersivepetroleum.common.blocks.stone.PetcokeBlock;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.PumpjackTileEntity;
import flaxbeard.immersivepetroleum.common.cfg.ConfigUtils;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.fluids.CrudeOilFluid;
import flaxbeard.immersivepetroleum.common.fluids.DieselFluid;
import flaxbeard.immersivepetroleum.common.fluids.IPFluid;
import flaxbeard.immersivepetroleum.common.fluids.NapalmFluid;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import flaxbeard.immersivepetroleum.common.items.IPItemBase;
import flaxbeard.immersivepetroleum.common.items.IPUpgradeItem;
import flaxbeard.immersivepetroleum.common.items.MotorboatItem;
import flaxbeard.immersivepetroleum.common.items.OilCanItem;
import flaxbeard.immersivepetroleum.common.items.ProjectorItem;
import flaxbeard.immersivepetroleum.common.lubehandlers.CrusherLubricationHandler;
import flaxbeard.immersivepetroleum.common.lubehandlers.ExcavatorLubricationHandler;
import flaxbeard.immersivepetroleum.common.lubehandlers.PumpjackLubricationHandler;
import flaxbeard.immersivepetroleum.common.multiblocks.CokerUnitMultiblock;
import flaxbeard.immersivepetroleum.common.multiblocks.DistillationTowerMultiblock;
import flaxbeard.immersivepetroleum.common.multiblocks.HydroTreaterMultiblock;
import flaxbeard.immersivepetroleum.common.multiblocks.PumpjackMultiblock;
import flaxbeard.immersivepetroleum.common.util.IPEffects;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPContent{
	public static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/Content");
	
	public static final List<Block> registeredIPBlocks = new ArrayList<>();
	public static final List<Item> registeredIPItems = new ArrayList<>();
	public static final List<Fluid> registeredIPFluids = new ArrayList<>();
	
	public static class Multiblock{
		public static Block distillationtower;
		public static Block pumpjack;
		public static Block cokerunit;
		public static Block hydrotreater;
	}
	
	public static class Fluids{
		public static IPFluid crudeOil;
		public static IPFluid diesel;
		public static IPFluid diesel_sulfur;
		public static IPFluid lubricant;
		public static IPFluid gasoline;
		public static IPFluid napalm;
	}
	
	public static class Blocks{
		public static IPBlockBase asphalt;
		public static AsphaltSlab asphalt_slab;
		public static AsphaltStairs asphalt_stair;
		public static IPBlockBase petcoke;
		
		public static IPBlockBase gas_generator;
		public static IPBlockBase auto_lubricator;
		public static IPBlockBase flarestack;
		
		public static BlockDummy dummyOilOre;
		public static BlockDummy dummyPipe;
		public static BlockDummy dummyConveyor;
	}
	
	public static class Items{
		public static IPItemBase bitumen;
		public static IPItemBase projector;
		public static IPItemBase speedboat;
		public static IPItemBase oil_can;
		public static IPItemBase petcoke;
		public static IPItemBase petcokedust;
	}
	
	public static class BoatUpgrades{
		public static IPUpgradeItem reinforced_hull;
		public static IPUpgradeItem ice_breaker;
		public static IPUpgradeItem tank;
		public static IPUpgradeItem rudders;
		public static IPUpgradeItem paddles;
	}
	
	public static DebugItem debugItem;
	
	/** block/item/fluid population */
	public static void populate(){
		IPContent.debugItem = new DebugItem();
		
		Fluids.crudeOil = new CrudeOilFluid();
		Fluids.diesel = new DieselFluid("diesel");
		Fluids.diesel_sulfur = new DieselFluid("diesel_sulfur");
		Fluids.lubricant = new IPFluid("lubricant", 925, 1000);
		Fluids.gasoline = new IPFluid("gasoline", 789, 1200);
		Fluids.napalm = new NapalmFluid();
		
		Blocks.dummyOilOre = new BlockDummy("dummy_oil_ore");
		Blocks.dummyPipe = new BlockDummy("dummy_pipe");
		Blocks.dummyConveyor = new BlockDummy("dummy_conveyor");
		
		Blocks.petcoke = new PetcokeBlock();
		Blocks.gas_generator = new GasGeneratorBlock();
		
		AsphaltBlock asphalt = new AsphaltBlock();
		Blocks.asphalt = asphalt;
		Blocks.asphalt_slab = new AsphaltSlab(asphalt);
		Blocks.asphalt_stair = new AsphaltStairs(asphalt);
		
		Blocks.auto_lubricator = new AutoLubricatorBlock("auto_lubricator");
		Blocks.flarestack = new FlarestackBlock();
		
		Multiblock.distillationtower = new DistillationTowerBlock();
		Multiblock.pumpjack = new PumpjackBlock();
		Multiblock.cokerunit = new CokerUnitBlock();
		Multiblock.hydrotreater = new HydrotreaterBlock();
		
		Items.bitumen = new IPItemBase("bitumen");
		Items.oil_can = new OilCanItem("oil_can");
		Items.speedboat = new MotorboatItem("speedboat");
		Items.petcoke = new IPItemBase("petcoke"){
			@Override
			public int getBurnTime(ItemStack itemStack){
				return 3200;
			}
		};
		Items.petcokedust = new IPItemBase("petcoke_dust");
		
		BoatUpgrades.reinforced_hull = new IPUpgradeItem("reinforced_hull", MotorboatItem.UPGRADE_TYPE);
		BoatUpgrades.ice_breaker = new IPUpgradeItem("icebreaker", MotorboatItem.UPGRADE_TYPE);
		BoatUpgrades.tank = new IPUpgradeItem("tank", MotorboatItem.UPGRADE_TYPE);
		BoatUpgrades.rudders = new IPUpgradeItem("rudders", MotorboatItem.UPGRADE_TYPE);
		BoatUpgrades.paddles = new IPUpgradeItem("paddles", MotorboatItem.UPGRADE_TYPE);
		
		Items.projector = new ProjectorItem("projector");
	}
	
	public static void preInit(){
	}
	
	public static void init(){
		//blockFluidCrudeOil.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 1));
		//blockFluidDiesel.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 1));
		//blockFluidLubricant.setPotionEffects(new PotionEffect(IEPotions.slippery, 100, 1));
		//blockFluidNapalm.setPotionEffects(new PotionEffect(IEPotions.flammable, 140, 2));
		
		ChemthrowerHandler.registerEffect(IPTags.Fluids.lubricant, new LubricantEffect());
		ChemthrowerHandler.registerEffect(IPTags.Fluids.lubricant, new ChemthrowerEffect_Potion(null, 0, IEPotions.slippery, 60, 1));
		ChemthrowerHandler.registerEffect(IETags.fluidPlantoil, new LubricantEffect());
		
		ChemthrowerHandler.registerFlammable(IPTags.Fluids.crudeOil);
		ChemthrowerHandler.registerEffect(IPTags.Fluids.crudeOil, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 1));
		
		ChemthrowerHandler.registerFlammable(IPTags.Fluids.gasoline);
		ChemthrowerHandler.registerEffect(IPTags.Fluids.gasoline, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 1));
		
		ChemthrowerHandler.registerFlammable(IPTags.Fluids.napalm);
		ChemthrowerHandler.registerEffect(IPTags.Fluids.napalm, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 2));
		
		MultiblockHandler.registerMultiblock(DistillationTowerMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(PumpjackMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(CokerUnitMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(HydroTreaterMultiblock.INSTANCE);
		
		ConfigUtils.addFuel(IPServerConfig.GENERATION.fuels.get());
		ConfigUtils.addBoatFuel(IPServerConfig.MISCELLANEOUS.boat_fuels.get());
		
		DieselHandler.registerFuel(IPTags.Fluids.diesel, 320);
		DieselHandler.registerDrillFuel(IPTags.Fluids.diesel);
		
		DieselHandler.registerFuel(IPTags.Fluids.diesel_sulfur, 320);
		DieselHandler.registerDrillFuel(IPTags.Fluids.diesel_sulfur);
		
		LubricantHandler.register(IPTags.Fluids.lubricant, 3);
		LubricantHandler.register(IETags.fluidPlantoil, 12);
		
		FlarestackHandler.register(IPTags.Utility.burnableInFlarestack);
		
		LubricatedHandler.registerLubricatedTile(PumpjackTileEntity.class, PumpjackLubricationHandler::new);
		LubricatedHandler.registerLubricatedTile(ExcavatorTileEntity.class, ExcavatorLubricationHandler::new);
		LubricatedHandler.registerLubricatedTile(CrusherTileEntity.class, CrusherLubricationHandler::new);
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event){
		for(Block block:registeredIPBlocks){
			try{
				event.getRegistry().register(block);
			}catch(Throwable e){
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
			}catch(Throwable e){
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
			}catch(Throwable e){
				log.error("Failed to register a fluid. ({}, {})", fluid, fluid.getRegistryName());
				throw e;
			}
		}
	}
	
	@SubscribeEvent
	public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event){
		try{
			event.getRegistry().register(MotorboatEntity.TYPE);
		}catch(Throwable e){
			log.error("Failed to register Speedboat Entity. {}", e.getMessage());
			throw e;
		}
	}
	
	@SubscribeEvent
	public static void registerEffects(RegistryEvent.Register<Effect> event){
		IPEffects.init();
	}
}
