package flaxbeard.immersivepetroleum.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.client.IECustomStateMapper;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import blusunrize.lib.manual.ManualPages;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.client.model.ModelCoresampleExtended;
import flaxbeard.immersivepetroleum.client.page.ManualPageBigMultiblock;
import flaxbeard.immersivepetroleum.client.page.ManualPageSchematicCrafting;
import flaxbeard.immersivepetroleum.client.render.MultiblockDistillationTowerRenderer;
import flaxbeard.immersivepetroleum.client.render.MultiblockPumpjackRenderer;
import flaxbeard.immersivepetroleum.client.render.RenderSpeedboat;
import flaxbeard.immersivepetroleum.client.render.TileAutoLubricatorRenderer;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPFluid;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.stone.BlockTypes_IPStoneDecoration;
import flaxbeard.immersivepetroleum.common.entity.EntitySpeedboat;
import flaxbeard.immersivepetroleum.common.items.ItemIPBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ImmersivePetroleum.MODID)
public class ClientProxy extends CommonProxy
{
	public static final String CAT_IP = "ip";
	
	@Override
	public void preInit()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntitySpeedboat.class, RenderSpeedboat::new);
	}
	
	@Override
	public void preInitEnd()
	{
		
	}
		
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent evt)
	{
		//Going through registered stuff at the end of preInit, because of compat modules possibly adding items
		for (Block block : IPContent.registeredIPBlocks)
		{
			Item blockItem = Item.getItemFromBlock(block);
			final ResourceLocation loc = Block.REGISTRY.getNameForObject(block);
			if (loc != null)
				if (block instanceof IIEMetaBlock)
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
					boolean isMD = block == IPContent.blockMetalDevice;
					for (int meta = isMD ? 1 : 0; meta < ieMetaBlock.getMetaEnums().length; meta++)
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
					if (isMD)
					{
						ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(new ResourceLocation("immersivepetroleum", "auto_lube"), "inventory"));
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

						ModelBakery.registerItemVariants(ipMetaItem, loc);
						ModelLoader.setCustomModelResourceLocation(ipMetaItem, meta, new ModelResourceLocation(loc, "inventory"));
					}
				}
				else
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
			} 
			else
			{
				final ResourceLocation loc = Item.REGISTRY.getNameForObject(item);
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
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onModelBakeEvent(ModelBakeEvent event)
	{
		
		ModelResourceLocation mLoc = new ModelResourceLocation(new ResourceLocation("immersiveengineering", IEContent.itemCoresample.itemName), "inventory");
		event.getModelRegistry().putObject(mLoc, new ModelCoresampleExtended());
		
	}

	@Override
	public void postInit()
	{
	
		ManualHelper.addEntry("schematics", CAT_IP,
				new ManualPages.Crafting(ManualHelper.getManual(), "schematics0", new ItemStack(IPContent.itemProjector, 1, 0)),
				new ManualPageSchematicCrafting(ManualHelper.getManual(), "schematics1", new ItemStack(IPContent.itemProjector, 1, 0)),
				new ManualPages.Text(ManualHelper.getManual(), "schematics2"));

		handleReservoirManual();

		ManualHelper.addEntry("pumpjack", CAT_IP,
				new ManualPageMultiblock(ManualHelper.getManual(), "pumpjack0", MultiblockPumpjack.instance),
				new ManualPages.Text(ManualHelper.getManual(), "pumpjack1"));
	
	
		ArrayList<DistillationRecipe> recipeList = DistillationRecipe.recipeList;
		List<String[]> l = new ArrayList<String[]>();
		for (DistillationRecipe recipe : recipeList)
		{
			boolean first = true;
			for (FluidStack output : recipe.fluidOutput)
			{
				String inputName = recipe.input.getFluid().getLocalizedName(recipe.input);
				String outputName = output.getFluid().getLocalizedName(output);
				String[] test = new String[] {
						first ? recipe.input.amount + " mB " + inputName : "",
								output.amount + " mB " + outputName
				};
				l.add(test);
				first = false;
			}
		}
		
		String[][] table = l.toArray(new String[0][]);
		ManualHelper.addEntry("distillationTower", CAT_IP,
				new ManualPageBigMultiblock(ManualHelper.getManual(), MultiblockDistillationTower.instance),
				new ManualPages.Text(ManualHelper.getManual(), "distillationTower0"),
				new ManualPages.Text(ManualHelper.getManual(), "distillationTower1"),
				new ManualPages.Table(ManualHelper.getManual(), "distillationTower2", table, false));
	
	
	
		ManualHelper.addEntry("portableGenerator", CAT_IP,
				new ManualPages.Crafting(ManualHelper.getManual(), "portableGenerator0", new ItemStack(IPContent.blockMetalDevice, 1, 1)),
				new ManualPages.Text(ManualHelper.getManual(), "portableGenerator1"));
		
		ManualHelper.addEntry("speedboat", CAT_IP,
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat0", new ItemStack(IPContent.itemSpeedboat, 1, 0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat1", new ItemStack(IPContent.itemUpgrades, 1, 2)),
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat2", new ItemStack(IPContent.itemUpgrades, 1, 3)),
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat3", new ItemStack(IPContent.itemUpgrades, 1, 1)),
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat4", new ItemStack(IPContent.itemUpgrades, 1, 0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat5", new ItemStack(IPContent.itemUpgrades, 1, 4))
		);

		ManualHelper.addEntry("napalm", CAT_IP,
				new ManualPages.Text(ManualHelper.getManual(), "napalm0"));
	

		ManualHelper.addEntry("asphalt", CAT_IP,
				new ManualPages.Crafting(ManualHelper.getManual(), "asphalt0", new ItemStack(IPContent.blockStoneDecoration,1,BlockTypes_IPStoneDecoration.ASPHALT.getMeta())));
			

		ManualHelper.addEntry("lubricant", CAT_IP,
				new ManualPages.Text(ManualHelper.getManual(), "lubricant0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "lubricant1", new ItemStack(IPContent.itemOilCan)),
				new ManualPages.Text(ManualHelper.getManual(), "lubricant2"));
		
		ManualHelper.addEntry("automaticLubricator", CAT_IP,
				new ManualPages.Crafting(ManualHelper.getManual(), "automaticLubricator0", new ItemStack(IPContent.blockMetalDevice, 1, 0)),
				new ManualPages.Text(ManualHelper.getManual(), "automaticLubricator1"));
	
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDistillationTower.TileEntityDistillationTowerParent.class, new MultiblockDistillationTowerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPumpjack.TileEntityPumpjackParent.class, new MultiblockPumpjackRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAutoLubricator.class, new TileAutoLubricatorRenderer());
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(IPContent.blockMetalDevice), 0, TileEntityAutoLubricator.class);

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
	
	
	static ManualEntry resEntry;
	public static void handleReservoirManual()
	{
		if(ManualHelper.getManual()!=null)
		{
			ArrayList<IManualPage> pages = new ArrayList();
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "oil0"));
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "oil1"));

			final ReservoirType[] minerals = PumpjackHandler.reservoirList.keySet().toArray(new ReservoirType[0]);

			for (ReservoirType type : minerals)
			{
				String name = "desc.immersivepetroleum.info.reservoir."+ type.name;
				String localizedName = I18n.format(name);
				if(localizedName.equalsIgnoreCase(name))
					localizedName = type.name;

				boolean isVowel = (localizedName.toLowerCase().charAt(0) == 'a'
						|| localizedName.toLowerCase().charAt(0) == 'e'
						|| localizedName.toLowerCase().charAt(0) == 'i'
						|| localizedName.toLowerCase().charAt(0) == 'o'
						|| localizedName.toLowerCase().charAt(0) == 'u');
				String aOrAn = I18n.format(isVowel ? "ie.manual.entry.oilVowel" : "ie.manual.entry.oilConsonant");
				
				String s0 = "";
				if (type.dimensionWhitelist != null && type.dimensionWhitelist.length > 0)
				{
					String validDims = "";
					for (int dim : type.dimensionWhitelist)
						validDims += (!validDims.isEmpty() ? ", ":"") + "<dim;" + dim + ">";
					s0 = I18n.format("ie.manual.entry.oilDimValid", localizedName, validDims, aOrAn);
				}
				else if (type.dimensionBlacklist != null && type.dimensionBlacklist.length > 0)
				{
					String invalidDims = "";
					for (int dim : type.dimensionBlacklist)
						invalidDims += (!invalidDims.isEmpty() ? ", ":"") + "<dim;" + dim + ">";
					s0 = I18n.format("ie.manual.entry.oilDimInvalid", localizedName, invalidDims, aOrAn);
				}
				else
					s0 = I18n.format("ie.manual.entry.oilDimAny", localizedName, aOrAn);
				
				String s4 = "";
				if (type.biomeWhitelist != null && type.biomeWhitelist.length > 0)
				{
					String validBiomes = "";
					for (String biome : type.biomeWhitelist)
					{
						for (ResourceLocation test : Biome.REGISTRY.getKeys())
						{
							Biome testBiome = Biome.REGISTRY.getObject(test);
							String testName = PumpjackHandler.getBiomeName(testBiome);
							if (testName != null && testName.equals(biome))
							{
								validBiomes += (!validBiomes.isEmpty() ? ", " : "") + PumpjackHandler.getBiomeDisplayName(testBiome.getBiomeName());
							}
						}
					}
					s4 = I18n.format("ie.manual.entry.oilBiomeValid", validBiomes);
				}
				else if (type.biomeBlacklist != null && type.biomeBlacklist.length > 0)
				{
					String invalidBiomes = "";
					for (String biome : type.biomeBlacklist)
					{
						for (ResourceLocation test : Biome.REGISTRY.getKeys())
						{
							Biome testBiome = Biome.REGISTRY.getObject(test);
							String testName = PumpjackHandler.getBiomeName(testBiome);
							if (testName != null && testName.equals(biome))
							{
								invalidBiomes += (!invalidBiomes.isEmpty() ? ", " : "") + PumpjackHandler.getBiomeDisplayName(testBiome.getBiomeName());
							}
						}
					}
					s4 = I18n.format("ie.manual.entry.oilBiomeInvalid", invalidBiomes);
				}
				else
					s4 = I18n.format("ie.manual.entry.oilBiomeAny");



				String s1 = "";
				Fluid fluid = FluidRegistry.getFluid(type.fluid);
				if (fluid != null)
				{
					s1 = fluid.getLocalizedName(new FluidStack(fluid, 1));
				}
				DecimalFormat f = new DecimalFormat("#,###.##");
				
				String s3 = "";
				if (type.replenishRate > 0)
				{
					s3 = I18n.format("ie.manual.entry.oilReplenish", type.replenishRate, s1);
				}
				String s2 = I18n.format("ie.manual.entry.oil2", s0, s1, f.format(type.minSize), f.format(type.maxSize), s3, s4);
				
				UniversalBucket bucket = ForgeModContainer.getInstance().universalBucket;
				ItemStack stack = new ItemStack(bucket);
	            FluidStack fs = new FluidStack(fluid, bucket.getCapacity());
	            
	            IFluidHandlerItem fluidHandler = new FluidBucketWrapper(stack);
				fluidHandler.fill(fs, true);
					            
				ItemStack[] displayStacks = new ItemStack[] { fluidHandler.getContainer() };
				pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), s2, displayStacks ));
			}

//			String[][][] multiTables = formatToTable_ExcavatorMinerals();
//			for(String[][] minTable : multiTables)
//				pages.add(new ManualPages.Table(ManualHelper.getManual(), "", minTable,true));
			if(resEntry!=null)
				resEntry.setPages(pages.toArray(new IManualPage[pages.size()]));
			else
			{
				ManualHelper.addEntry("oil", CAT_IP, pages.toArray(new IManualPage[pages.size()]));
				resEntry = ManualHelper.getManual().getEntry("oil");
			}
		}
	}
	
	public void renderTile(TileEntity te)
	{
		if (te instanceof TileEntityPumpjack.TileEntityPumpjackParent)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(-90, 0, 1, 0);
			GlStateManager.translate(1, 1, -2);
			
			float pt = 0;
			if (Minecraft.getMinecraft().player != null)
			{
				((TileEntityPumpjack.TileEntityPumpjackParent) te).activeTicks = Minecraft.getMinecraft().player.ticksExisted;
				pt = Minecraft.getMinecraft().getRenderPartialTicks();
			}
			
			
			TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer((TileEntity) te);
			
			tesr.render((TileEntity) te, 0, 0, 0, pt, 0, 0);
			GlStateManager.popMatrix();
		}
		else
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(-90, 0, 1, 0);
			GlStateManager.translate(0, 1, -4);
			
			
			TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer((TileEntity) te);
			
			tesr.render((TileEntity) te, 0, 0, 0, 0, 0, 0);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawUpperHalfSlab(ItemStack stack) {
		// Render slabs on top half
		BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = IEContent.blockMetalDecorationSlabs1.getStateFromMeta(stack.getMetadata());
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState((IBlockState)state);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.5F, 1.0F);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if (Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(7425);
		} else {
			GlStateManager.shadeModel(7424);
		}

		blockRenderer.getBlockModelRenderer().renderModelBrightness(model, (IBlockState)state, 0.75F, false);
		GlStateManager.popMatrix();
	}
}