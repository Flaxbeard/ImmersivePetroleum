package flaxbeard.immersivepetroleum;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.client.ClientProxy;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.IPConfig;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Fluids;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.crafting.RecipeReloadListener;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.util.commands.ReservoirCommand;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ImmersivePetroleum.MODID)
public class ImmersivePetroleum{
	public static final String MODID = "immersivepetroleum";

	public static final Logger log=LogManager.getLogger(MODID);
	
	public static final ItemGroup creativeTab = new ItemGroup(MODID){
		@Override
		public ItemStack createIcon(){
			return new ItemStack(Fluids.fluidCrudeOil.getFilledBucket());
		}
	};

	public static CommonProxy proxy=DistExecutor.runForDist(()->ClientProxy::new, ()->CommonProxy::new);

	public static ImmersivePetroleum INSTANCE;

	public ImmersivePetroleum(){
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, IPConfig.ALL);
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
		MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		
		Serializers.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
		
		IPContent.populate();
		Serializers.construct();
		
		proxy.construct();
		proxy.registerContainersAndScreens();
	}
	
	public void setup(FMLCommonSetupEvent event){
		proxy.setup();
		
		// ---------------------------------------------------------------------------------------------------------------------------------------------
		
		proxy.preInit();
		
		IPContent.preInit();
		IPPacketHandler.preInit();
		
		DieselHandler.registerFuel(Fluids.fluidDiesel, 150);
		
		proxy.preInitEnd();
		
		// ---------------------------------------------------------------------------------------------------------------------------------------------
		
		DistillationRecipe.energyModifier = IPConfig.REFINING.distillationTower_energyModifier.get();
		DistillationRecipe.timeModifier = IPConfig.REFINING.distillationTower_timeModifier.get();
		
		PumpjackHandler.oilChance = IPConfig.EXTRACTION.reservoir_chance.get();
		
		// TODO See issue #4215 in the ImmersiveEngineering GitHub
		/*
		IEConfig.manual_int.put("distillationTower_operationCost", (int) (2048 * IPConfig.REFINING.distillationTower_energyModifier.get()));
		IEConfig.manual_int.put("pumpjack_consumption", IPConfig.EXTRACTION.pumpjack_consumption.get());
		IEConfig.manual_int.put("pumpjack_speed", IPConfig.EXTRACTION.pumpjack_speed.get());

		int oil_min = 1000000;
		int oil_max = 5000000;
		for(ReservoirType type:PumpjackHandler.reservoirList.keySet()){
			if(type.name.equals("oil")){
				oil_min = type.minSize;
				oil_max = type.maxSize;
				break;
			}
		}
		IEConfig.manual_int.put("pumpjack_days", (((oil_max + oil_min) / 2) + oil_min) / (IPConfig.EXTRACTION.pumpjack_speed.get() * 24000));
		IEConfig.manual_double.put("autoLubricant_speedup", 1.25);


		Map<ResourceLocation, Integer> map = FuelHandler.getFuelFluxesPerTick();
		if(map.size() > 0 && map.containsKey("gasoline")){
			IEConfig.manual_int.put("portableGenerator_flux", map.get("gasoline"));
			
		}else{
			IEConfig.manual_int.put("portableGenerator_flux", -1);
		}
		*/
		
		IPContent.init();
		
		proxy.init();
		
		// ---------------------------------------------------------------------------------------------------------------------------------------------
		
		proxy.postInit();
		
		PumpjackHandler.recalculateChances(true);
	}
	
	public void loadComplete(FMLLoadCompleteEvent event){
		proxy.completed();
	}
	
	public void serverAboutToStart(FMLServerAboutToStartEvent event){
		proxy.serverAboutToStart();
		
		event.getServer().getResourceManager().addReloadListener(new RecipeReloadListener());
	}

	public void serverStarting(FMLServerStartingEvent event){
		proxy.serverStarting();
		
		event.getCommandDispatcher().register(Commands.literal("ip").then(ReservoirCommand.create()));
	}
	
	public void serverStarted(FMLServerStartedEvent event){
		proxy.serverStarted();
		
		ServerWorld world = event.getServer().getWorld(DimensionType.OVERWORLD);
		if(!world.isRemote){
			IPSaveData worldData = world.getSavedData().getOrCreate(IPSaveData::new, IPSaveData.dataName);
			IPSaveData.setInstance(worldData);
		}
		
		PumpjackHandler.recalculateChances(true);
	}
}
