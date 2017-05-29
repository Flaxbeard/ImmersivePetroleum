package flaxbeard.immersivepetroleum.client;

import java.util.Locale;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.client.IECustomStateMapper;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.lib.manual.ManualPages;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.page.ManualPageBigMultiblock;
import flaxbeard.immersivepetroleum.client.render.MultiblockDistillationTowerRenderer;
import flaxbeard.immersivepetroleum.client.render.MultiblockPumpjackRenderer;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPFluid;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.stone.BlockTypes_IPStoneDecoration;
import flaxbeard.immersivepetroleum.common.items.ItemIPBase;


public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit() {
		
	}
	
	@Override
	public void preInitEnd()
	{
		//Going through registered stuff at the end of preInit, because of compat modules possibly adding items
		for(Block block : IPContent.registeredIPBlocks)
		{
			Item blockItem = Item.getItemFromBlock(block);
			final ResourceLocation loc = GameData.getBlockRegistry().getNameForObject(block);
			if(block instanceof IIEMetaBlock)
			{
				IIEMetaBlock ieMetaBlock = (IIEMetaBlock) block;
				if(ieMetaBlock.useCustomStateMapper())
					ModelLoader.setCustomStateMapper(block, IECustomStateMapper.getStateMapper(ieMetaBlock));
				ModelLoader.setCustomMeshDefinition(blockItem, new ItemMeshDefinition()
				{
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack)
					{
						return new ModelResourceLocation(loc, "inventory");
					}
				});
				for(int meta = 0; meta < ieMetaBlock.getMetaEnums().length; meta++)
				{
					String location = loc.toString();
					String prop = ieMetaBlock.appendPropertiesToState() ? ("inventory," + ieMetaBlock.getMetaProperty().getName() + "=" + ieMetaBlock.getMetaEnums()[meta].toString().toLowerCase(Locale.US)) : null;
					if(ieMetaBlock.useCustomStateMapper())
					{
						String custom = ieMetaBlock.getCustomStateMapping(meta, true);
						if(custom != null)
							location += "_" + custom;
					}
					try
					{
						ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
					} catch(NullPointerException npe)
					{
						throw new RuntimeException("WELP! apparently " + ieMetaBlock + " lacks an item!", npe);
					}
				}
			} else if(block instanceof BlockIPFluid)
				mapFluidState(block, ((BlockIPFluid) block).getFluid());
			else
				ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(loc, "inventory"));
		}

		for(Item item : IPContent.registeredIPItems)
		{
			if(item instanceof ItemIPBase)
			{
				ItemIPBase ipMetaItem = (ItemIPBase) item;
				if(ipMetaItem.registerSubModels && ipMetaItem.getSubNames() != null && ipMetaItem.getSubNames().length > 0)
				{
					for(int meta = 0; meta < ipMetaItem.getSubNames().length; meta++)
					{
						ResourceLocation loc = new ResourceLocation("immersivepetroleum", ipMetaItem.itemName + "/" + ipMetaItem.getSubNames()[meta]);
						System.out.println(loc);

						ModelBakery.registerItemVariants(ipMetaItem, loc);
						ModelLoader.setCustomModelResourceLocation(ipMetaItem, meta, new ModelResourceLocation(loc, "inventory"));
					}
				} else
				{
					final ResourceLocation loc = new ResourceLocation("immersivepetroleum", ipMetaItem.itemName);
					ModelBakery.registerItemVariants(ipMetaItem, loc);
					ModelLoader.setCustomMeshDefinition(ipMetaItem, new ItemMeshDefinition()
					{
						@Override
						public ModelResourceLocation getModelLocation(ItemStack stack)
						{
							return new ModelResourceLocation(loc, "inventory");
						}
					});
				}
			} else
			{
				final ResourceLocation loc = GameData.getItemRegistry().getNameForObject(item);
				ModelBakery.registerItemVariants(item, loc);
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition()
				{
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack)
					{
						return new ModelResourceLocation(loc, "inventory");
					}
				});
			}
		}
	}

	@Override
	public void init()
	{
		ShaderUtil.init();
	}

	@Override
	public void postInit()
	{
		String CAT_IP = "ip";
		
		
		ManualHelper.addEntry("oil", CAT_IP,
				new ManualPages.Text(ManualHelper.getManual(), "oil0"),
				new ManualPages.Text(ManualHelper.getManual(), "oil1"));
		
		ManualHelper.addEntry("pumpjack", CAT_IP,
				new ManualPageMultiblock(ManualHelper.getManual(), "pumpjack0", MultiblockPumpjack.instance),
				new ManualPages.Text(ManualHelper.getManual(), "pumpjack1"));
		
		ManualHelper.addEntry("distillationTower", CAT_IP,
				new ManualPageBigMultiblock(ManualHelper.getManual(), MultiblockDistillationTower.instance),
				new ManualPages.Text(ManualHelper.getManual(), "distillationTower0"),
				new ManualPages.Text(ManualHelper.getManual(), "distillationTower1"));
		
		ManualHelper.addEntry("asphalt", CAT_IP, new ManualPages.Crafting(ManualHelper.getManual(), "asphalt0", new ItemStack(IPContent.blockStoneDecoration,1,BlockTypes_IPStoneDecoration.ASPHALT.getMeta())));

		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDistillationTower.TileEntityDistillationTowerParent.class, new MultiblockDistillationTowerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPumpjack.TileEntityPumpjackParent.class, new MultiblockPumpjackRenderer());

	}

	private static void mapFluidState(Block block, Fluid fluid)
	{
		Item item = Item.getItemFromBlock(block);
		FluidStateMapper mapper = new FluidStateMapper(fluid);
		if(item != null)
		{
			ModelLoader.registerItemVariants(item);
			ModelLoader.setCustomMeshDefinition(item, mapper);
		}
		ModelLoader.setCustomStateMapper(block, mapper);
	}

	static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition
	{
		public final ModelResourceLocation location;

		public FluidStateMapper(Fluid fluid)
		{
			this.location = new ModelResourceLocation(ImmersivePetroleum.MODID + ":fluid_block", fluid.getName());
		}

		@Nonnull
		@Override
		protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state)
		{
			return location;
		}

		@Nonnull
		@Override
		public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack)
		{
			return location;
		}
	}
}