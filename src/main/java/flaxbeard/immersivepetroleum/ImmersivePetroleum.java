package flaxbeard.immersivepetroleum;

import java.util.HashMap;

import flaxbeard.immersivepetroleum.common.fluid.FluidDiesel;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;
import blusunrize.immersiveengineering.common.Config;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
import flaxbeard.immersivepetroleum.common.EventHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.util.CommandHandler;

@Mod(modid = ImmersivePetroleum.MODID, version = ImmersivePetroleum.VERSION, dependencies = "required-after:immersiveengineering@[0.12,);")
public class ImmersivePetroleum
{
	public static final String MODID = "immersivepetroleum";
	public static final String VERSION = "@VERSION@";
	
	@SidedProxy(clientSide="flaxbeard.immersivepetroleum.client.ClientProxy", serverSide="flaxbeard.immersivepetroleum.common.CommonProxy")
	public static CommonProxy proxy;
	
	@Instance(MODID)
	public static ImmersivePetroleum INSTANCE;
	
	static
	{
		FluidRegistry.enableUniversalBucket();
	}
		
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		IPContent.preInit();
		proxy.preInit();
		proxy.preInitEnd();
		
		IPPacketHandler.preInit();
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		DistillationRecipe.energyModifier = IPConfig.Refining.distillationTower_energyModifier;
		DistillationRecipe.timeModifier = IPConfig.Refining.distillationTower_timeModifier;
		
		PumpjackHandler.oilChance = IPConfig.Extraction.reservoir_chance;
		
		Config.manual_int.put("distillationTower_operationCost", (int) (2048 * IPConfig.Refining.distillationTower_energyModifier));
		Config.manual_int.put("pumpjack_consumption", IPConfig.Extraction.pumpjack_consumption);
		Config.manual_int.put("pumpjack_speed", IPConfig.Extraction.pumpjack_speed);

		int oil_min = 1000000;
		int oil_max = 5000000;
		for (ReservoirType type : PumpjackHandler.reservoirList.keySet())
		{
			if (type.name.equals("oil"))
			{
				oil_min = type.minSize;
				oil_max = type.maxSize;
				break;
			}
		}
		Config.manual_int.put("pumpjack_days", (((oil_max + oil_min) / 2) + oil_min) / (IPConfig.Extraction.pumpjack_speed * 24000));
		Config.manual_double.put("autoLubricant_speedup", 1.25);

		IPContent.init();
		
		
		HashMap<String, Integer> map = FuelHandler.getFuelFluxesPerTick();
		if (map.size() > 0 && map.containsKey("gasoline"))
		{
			Config.manual_int.put("portableGenerator_flux", map.get("gasoline"));

		}
		else
		{
			Config.manual_int.put("portableGenerator_flux", -1);
		}
		
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, proxy);
		proxy.init();
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit();
		PumpjackHandler.recalculateChances(true);
	}
	
	
	/*
	public static <T extends IForgeRegistryEntry<?>> T register(T object, String name)
	{
		return registerByFullName(object, MODID+":"+name);
	}
	public static <T extends IForgeRegistryEntry<?>> T registerByFullName(T object, String name)
	{
		object.setRegistryName(new ResourceLocation(name));
		return GameRegistry.register(object);
	}
	public static Block registerBlockByFullName(Block block, ItemBlock itemBlock, String name)
	{
		block = registerByFullName(block, name);
		registerByFullName(itemBlock, name);
		return block;
	}
	public static Block registerBlockByFullName(Block block, Class<? extends ItemBlock> itemBlock, String name)
	{
		try{
			return registerBlockByFullName(block, itemBlock.getConstructor(Block.class).newInstance(block), name);
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	public static Block registerBlock(Block block, Class<? extends ItemBlock> itemBlock, String name)
	{
		try{
			return registerBlockByFullName(block, itemBlock.getConstructor(Block.class).newInstance(block), MODID+":"+name);
		}catch(Exception e){e.printStackTrace();}
		return null;
	}*/
	
	public static CreativeTabs creativeTab = new CreativeTabs(MODID)
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return ItemStack.EMPTY;
		}
		
		@Override
		public ItemStack getIconItemStack()
		{
			UniversalBucket bucket = ForgeModContainer.getInstance().universalBucket;
			ItemStack stack = new ItemStack(bucket);
            FluidStack fs = new FluidStack(IPContent.fluidCrudeOil, bucket.getCapacity());
            IFluidHandlerItem fluidHandler = new FluidBucketWrapper(stack);
			if (fluidHandler.fill(fs, true) == fs.amount)
			{
				return fluidHandler.getContainer();
			}
			
			return new ItemStack(IPContent.blockFluidDiesel,1,0);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void displayAllRelevantItems(NonNullList<ItemStack> list)
		{
			UniversalBucket bucket = ForgeModContainer.getInstance().universalBucket;
			ItemStack stack = new ItemStack(bucket);
            FluidStack fs = new FluidStack(IPContent.fluidCrudeOil, bucket.getCapacity());
            IFluidHandlerItem fluidHandler = new FluidBucketWrapper(stack);
			if (fluidHandler.fill(fs, true) == fs.amount)
			{
				list.add(fluidHandler.getContainer());
			}

			stack = new ItemStack(bucket);
			fs = new FluidStack(IPContent.fluidDiesel, bucket.getCapacity());
			fs = FluidDiesel.addSulfur(fs);
			fluidHandler = new FluidBucketWrapper(stack);
			if (fluidHandler.fill(fs, true) == fs.amount)
			{
				list.add(fluidHandler.getContainer());
			}

			stack = new ItemStack(bucket);
            fs = new FluidStack(IPContent.fluidDiesel, bucket.getCapacity());
            fluidHandler = new FluidBucketWrapper(stack);
        	if (fluidHandler.fill(fs, true) == fs.amount)
			{
				list.add(fluidHandler.getContainer());
			}
			
        	stack = new ItemStack(bucket);
            fs = new FluidStack(IPContent.fluidGasoline, bucket.getCapacity());
            fluidHandler = new FluidBucketWrapper(stack);
        	if (fluidHandler.fill(fs, true) == fs.amount)
			{
				list.add(fluidHandler.getContainer());
			}
        	
        	stack = new ItemStack(bucket);
            fs = new FluidStack(IPContent.fluidLubricant, bucket.getCapacity());
            fluidHandler = new FluidBucketWrapper(stack);
        	if (fluidHandler.fill(fs, true) == fs.amount)
			{
				list.add(fluidHandler.getContainer());
			}
			
			super.displayAllRelevantItems(list);
		}
	};
	
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
			if(!world.isRemote)
			{
				IPSaveData worldData = (IPSaveData) world.loadData(IPSaveData.class, IPSaveData.dataName);
				if(worldData == null)
				{
					worldData = new IPSaveData(IPSaveData.dataName);
					world.setData(IPSaveData.dataName, worldData);
				}
				IPSaveData.setInstance(world.provider.getDimension(), worldData);
			}
		}
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandHandler());
	}

}
