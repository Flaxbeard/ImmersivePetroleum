package flaxbeard.immersivepetroleum;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import blusunrize.immersiveengineering.common.Config;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
import flaxbeard.immersivepetroleum.common.EventHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;

@Mod(modid = ImmersivePetroleum.MODID, version = ImmersivePetroleum.VERSION, dependencies = "required-after:immersiveengineering;")
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
		DistillationRecipe.energyModifier = IPConfig.Machines.distillationTower_energyModifier;
		DistillationRecipe.timeModifier = IPConfig.Machines.distillationTower_timeModifier;
		
		PumpjackHandler.dimensionBlacklist = IPConfig.Machines.oil_dimBlacklist;
		PumpjackHandler.minDeposit = IPConfig.Machines.oil_min;
		PumpjackHandler.maxDeposit = IPConfig.Machines.oil_max;
		PumpjackHandler.oilChance = IPConfig.Machines.oil_chance;
		PumpjackHandler.replenishAmount = IPConfig.Machines.oil_replenish;
		
		Config.manual_int.put("distillationTower_operationCost", (int) (2048 * IPConfig.Machines.distillationTower_energyModifier));
		Config.manual_int.put("pumpjack_consumption", IPConfig.Machines.pumpjack_consumption);
		Config.manual_int.put("pumpjack_speed", IPConfig.Machines.pumpjack_speed);
		Config.manual_int.put("pumpjack_days", (((IPConfig.Machines.oil_max + IPConfig.Machines.oil_min) / 2) + IPConfig.Machines.oil_min) / (IPConfig.Machines.pumpjack_speed * 24000));
		Config.manual_int.put("oil_replenish", IPConfig.Machines.oil_replenish);

		IPContent.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, proxy);
		proxy.init();
		
		MinecraftForge.EVENT_BUS.register(new EventHandler());
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit();
	}
	
	

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
	}
	
	public static CreativeTabs creativeTab = new CreativeTabs(MODID)
	{
		@Override
		public Item getTabIconItem()
		{
			return null;
		}
		
		@Override
		public ItemStack getIconItemStack()
		{
			UniversalBucket bucket = ForgeModContainer.getInstance().universalBucket;
			ItemStack stack = new ItemStack(bucket);
            FluidStack fs = new FluidStack(IPContent.fluidCrudeOil, bucket.getCapacity());
			if (bucket.fill(stack, fs, true) == fs.amount)
			{
				return stack;
			}
			
			return new ItemStack(IPContent.blockFluidDiesel,1,0);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void displayAllRelevantItems(List<ItemStack> list)
		{
			UniversalBucket bucket = ForgeModContainer.getInstance().universalBucket;
			ItemStack stack = new ItemStack(bucket);
            FluidStack fs = new FluidStack(IPContent.fluidCrudeOil, bucket.getCapacity());
			if (bucket.fill(stack, fs, true) == fs.amount)
			{
				list.add(stack);
			}
			
			stack = new ItemStack(bucket);
            fs = new FluidStack(IPContent.fluidDiesel, bucket.getCapacity());
			if (bucket.fill(stack, fs, true) == fs.amount)
			{
				list.add(stack);
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
				IPSaveData worldData = (IPSaveData) world.loadItemData(IPSaveData.class, IPSaveData.dataName);
				if(worldData == null)
				{
					worldData = new IPSaveData(IPSaveData.dataName);
					world.setItemData(IPSaveData.dataName, worldData);
				}
				IPSaveData.setInstance(world.provider.getDimension(), worldData);
			}
		}
	}

}
