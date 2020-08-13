package flaxbeard.immersivepetroleum.client;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.client.models.ModelCoresample;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.gui.GuiHandler;
import blusunrize.lib.manual.ManualElementCrafting;
import blusunrize.lib.manual.ManualElementTable;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.TextSplitter;
import blusunrize.lib.manual.Tree.InnerNode;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.client.gui.DistillationTowerScreen;
import flaxbeard.immersivepetroleum.client.page.ManualElementSchematicCrafting;
import flaxbeard.immersivepetroleum.client.render.MultiblockDistillationTowerRenderer;
import flaxbeard.immersivepetroleum.client.render.TileAutoLubricatorRenderer;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ImmersivePetroleum.MODID)
public class ClientProxy extends CommonProxy{
	@SuppressWarnings("unused")
	private static final Logger log=LogManager.getLogger(ImmersivePetroleum.MODID+"/ClientProxy");
	public static final String CAT_IP = "ip";
	
	@Override
	public void construct(){}
	
	@Override
	public void setup(){}
	
	@Override
	public void registerContainersAndScreens(){
		super.registerContainersAndScreens();
		
		registerScreen(new ResourceLocation(ImmersivePetroleum.MODID, "distillationtower"), DistillationTowerScreen::new);
	}
	
	@SuppressWarnings("unchecked")
	public <C extends Container, S extends Screen & IHasContainer<C>> void registerScreen(ResourceLocation name, IScreenFactory<C, S> factory){
		ContainerType<C> type=(ContainerType<C>)GuiHandler.getContainerType(name);
		ScreenManager.registerFactory(type, factory);
	}
	
	@Override
	public void completed(){
	}
	
	@Override
	public void preInit(){
		// RenderingRegistry.registerEntityRenderingHandler(EntitySpeedboat.class, RenderSpeedboat::new);
	}
	
	@Override
	public void preInitEnd(){
	}
	
	@Override
	public void init(){
		//ShaderUtil.init(); // Get's initialized befor the first time it's actualy used.
	}
	
	/** ImmersivePetroleum's Manual Category */
	private static InnerNode<ResourceLocation, ManualEntry> IP_CATEGORY;
	public void setupManualPages(){
		ManualInstance man=ManualHelper.getManual();
		
		IP_CATEGORY=man.getRoot().getOrCreateSubnode(modLoc("main"), 100);
		
		handleReservoirManual(modLoc("reservoir"), 0);
		distillation(modLoc("distillationTower"), 1);
		
		man.addEntry(IP_CATEGORY, modLoc("pumpjack"), 2);
		man.addEntry(IP_CATEGORY, modLoc("lubricant"), 3);
		man.addEntry(IP_CATEGORY, modLoc("asphalt"), 4);
		man.addEntry(IP_CATEGORY, modLoc("schematics"), 5);
		man.addEntry(IP_CATEGORY, modLoc("speedboat"), 6);
		man.addEntry(IP_CATEGORY, modLoc("napalm"), 7);
		man.addEntry(IP_CATEGORY, modLoc("portableGenerator"), 8);
		man.addEntry(IP_CATEGORY, modLoc("automaticLubricator"), 9);
		schematics(modLoc("schematics"), 10);
		
		//man.addEntry(cat, modLoc(""));
	}
	
	private static void schematics(ResourceLocation location, int priority){
		ManualInstance man=ManualHelper.getManual();
		
		ManualEntry.ManualEntryBuilder builder=new ManualEntry.ManualEntryBuilder(man);
		builder.addSpecialElement("schematics0", 0, new ManualElementCrafting(man, new ItemStack(IPContent.itemProjector, 1)));
		builder.addSpecialElement("schematics1", 0, new ManualElementSchematicCrafting(man, "schematics1", new ItemStack(IPContent.itemProjector, 1)));
		builder.readFromFile(location);
		man.addEntry(IP_CATEGORY, builder.create(), priority);
	}
	
	private static void distillation(ResourceLocation location, int priority){
		ManualInstance man=ManualHelper.getManual();
		
		Collection<DistillationRecipe> recipeList = DistillationRecipe.recipes.values();
		List<String[]> l = new ArrayList<String[]>();
		for(DistillationRecipe recipe:recipeList){
			boolean first = true;
			for(FluidStack output:recipe.fluidOutput){
				String inputName = recipe.input.getDisplayName().getUnformattedComponentText();
				String outputName = output.getDisplayName().getUnformattedComponentText();
				String[] test = new String[]{
						first ? recipe.input.getAmount() + " mB " + inputName : "",
						output.getAmount() + " mB " + outputName
				};
				l.add(test);
				first = false;
			}
		}
		
		ManualEntry.ManualEntryBuilder builder=new ManualEntry.ManualEntryBuilder(man);
		builder.addSpecialElement("distillationtower2", 0, new ManualElementTable(ManualHelper.getManual(), l.toArray(new String[0][]), false));
		builder.readFromFile(location);
		man.addEntry(IP_CATEGORY, builder.create(), priority);
	}
	
	public static void handleReservoirManual(ResourceLocation location, int priority){
		ManualInstance man=ManualHelper.getManual();
		
		ManualEntry.ManualEntryBuilder builder=new ManualEntry.ManualEntryBuilder(man);
		builder.setContent(ClientProxy::createContent);
		builder.setLocation(location);
		man.addEntry(IP_CATEGORY, builder.create(), priority);
	}

	static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
	static ManualEntry entry;
	private static String[] createContent(TextSplitter splitter){
		ArrayList<ItemStack> list = new ArrayList<>();
		final ReservoirType[] minerals = PumpjackHandler.reservoirList.keySet().toArray(new ReservoirType[0]);
		
		StringBuilder content=new StringBuilder();
		content.append(I18n.format("ie.manual.entry.reservoirs.oil0"));
		content.append(I18n.format("ie.manual.entry.reservoirs.oil1"));
		
		for(ReservoirType type:minerals){
			String name = "desc.immersivepetroleum.info.reservoir." + type.name;
			String localizedName = I18n.format(name);
			if(localizedName.equalsIgnoreCase(name))
				localizedName = type.name;
			
			boolean isVowel = (localizedName.toLowerCase().charAt(0) == 'a' || localizedName.toLowerCase().charAt(0) == 'e' || localizedName.toLowerCase().charAt(0) == 'i' || localizedName.toLowerCase().charAt(0) == 'o' || localizedName.toLowerCase().charAt(0) == 'u');
			String aOrAn = I18n.format(isVowel ? "ie.manual.entry.reservoirs.vowel" : "ie.manual.entry.reservoirs.consonant");
			
			String dimBLWL = "";
			if(type.dimensionWhitelist != null && type.dimensionWhitelist.length > 0){
				String validDims = "";
				for(int dim:type.dimensionWhitelist){
					validDims += (!validDims.isEmpty() ? ", " : "") + "<dim;" + dim + ">";
				}
				dimBLWL = I18n.format("ie.manual.entry.reservoirs.dim.valid", localizedName, validDims, aOrAn);
			}else if(type.dimensionBlacklist != null && type.dimensionBlacklist.length > 0){
				String invalidDims = "";
				for(int dim:type.dimensionBlacklist){
					invalidDims += (!invalidDims.isEmpty() ? ", " : "") + "<dim;" + dim + ">";
				}
				dimBLWL = I18n.format("ie.manual.entry.reservoirs.dim.invalid", localizedName, invalidDims, aOrAn);
			}else{
				dimBLWL = I18n.format("ie.manual.entry.reservoirs.dim.any", localizedName, aOrAn);
			}
			content.append(dimBLWL);
			
			String bioBLWL = "";
			if(type.biomeWhitelist != null && type.biomeWhitelist.length > 0){
				String validBiomes = "";
				for(String biome:type.biomeWhitelist){
					validBiomes += (!validBiomes.isEmpty() ? ", " : "") + PumpjackHandler.getTagDisplayName(biome);
				}
				bioBLWL = I18n.format("ie.manual.entry.reservoirs.bio.valid", validBiomes);
			}else if(type.biomeBlacklist != null && type.biomeBlacklist.length > 0){
				String invalidBiomes = "";
				for(String biome:type.biomeBlacklist){
					invalidBiomes += (!invalidBiomes.isEmpty() ? ", " : "") + PumpjackHandler.getTagDisplayName(biome);
				}
				bioBLWL = I18n.format("ie.manual.entry.reservoirs.bio.invalid", invalidBiomes);
			}else{
				bioBLWL = I18n.format("ie.manual.entry.reservoirs.bio.any");
			}
			content.append(bioBLWL);
			
			String fluidName = "";
			Fluid fluid = type.getFluid();
			if(fluid != null){
				fluidName = new FluidStack(fluid, 1).getDisplayName().getUnformattedComponentText();
			}
			
			String repRate = "";
			if(type.replenishRate > 0){
				repRate = I18n.format("ie.manual.entry.reservoirs.replenish", type.replenishRate, fluidName);
			}
			content.append(I18n.format("ie.manual.entry.reservoirs.oil", dimBLWL, fluidName, FORMATTER.format(type.minSize), FORMATTER.format(type.maxSize), repRate, bioBLWL));
			
			list.add(new ItemStack(fluid.getFilledBucket()));
		}
		
		// This no longer works, there's no way to do this legit!
		/*
		ManualElementItem[] items=pages.toArray(new ManualElementItem[list.size()]);
		pages.toArray(new ManualElementItem[pages.size()])
		if(resEntry != null){
			resEntry.setPages(items);
		}else{
			resEntry = man.addEntry(ipCat, modLoc("oil"), ep++);
		}
		*/
		
		return new String[]{
				"title",
				"sub",
				content.toString()
		};
	}
	@Override
	public void postInit(){
		// TODO TileEntityRenderer Registration
		ClientRegistry.bindTileEntitySpecialRenderer(DistillationTowerTileEntity.DistillationTowerParentTileEntity.class, new MultiblockDistillationTowerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(AutoLubricatorTileEntity.class, new TileAutoLubricatorRenderer());
		//ClientRegistry.bindTileEntitySpecialRenderer(PumpjackTileEntity.TileEntityPumpjackParent.class, new MultiblockPumpjackRenderer());
		
		// Don't think this is needed anymore
		//ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(IPContent.blockMetalDevice), 0, AutoLubricatorTileEntity.class);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onModelBakeEvent(ModelBakeEvent event){
		ModelResourceLocation mLoc = new ModelResourceLocation(IEBlocks.StoneDecoration.coresample.getRegistryName(), "inventory");
		IBakedModel model=event.getModelRegistry().get(mLoc);
		if(model instanceof ModelCoresample){
			//event.getModelRegistry().put(mLoc, new ModelCoresampleExtended());
		}
	}
	
	/*
	private static void mapFluidState(Block block, Fluid fluid){
		Item item = Item.getItemFromBlock(block);
		FluidStateMapper mapper = new FluidStateMapper(fluid);
		if(item != null){
			ModelLoader.registerItemVariants(item);
			ModelLoader.setCustomMeshDefinition(item, mapper);
		}
		ModelLoader.setCustomStateMapper(block, mapper);
	}
	*/
	
	static ManualEntry resEntry;
	
	public void renderTile(TileEntity te){
		
		if(te instanceof PumpjackTileEntity.PumpjackParentTileEntity){
			GlStateManager.pushMatrix();
			GlStateManager.rotatef(-90, 0, 1, 0);
			GlStateManager.translatef(1, 1, -2);
			
			float pt = 0;
			if(Minecraft.getInstance().player != null){
				((PumpjackTileEntity.PumpjackParentTileEntity) te).activeTicks = Minecraft.getInstance().player.ticksExisted;
				pt = Minecraft.getInstance().getRenderPartialTicks();
			}
			
			TileEntityRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer((TileEntity) te);
			
			tesr.render((TileEntity) te, 0, 0, 0, pt, 0);
			GlStateManager.popMatrix();
		}else{
			GlStateManager.pushMatrix();
			GlStateManager.rotatef(-90, 0, 1, 0);
			GlStateManager.translatef(0, 1, -4);
			
			TileEntityRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer((TileEntity) te);
			
			tesr.render((TileEntity) te, 0, 0, 0, 0, 0);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawUpperHalfSlab(ItemStack stack){
		
		// Render slabs on top half
		BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = IEBlocks.MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD).getDefaultState();
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.0F, 0.5F, 1.0F);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled()){
			GlStateManager.shadeModel(7425);
		}else{
			GlStateManager.shadeModel(7424);
		}
		
		blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 0.75F, false);
		GlStateManager.popMatrix();
	}
	
	/*// Data Generators are responsible for dealing with this now
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
					if (ieMetaBlock.useCustomStateMapper())
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
						if (ieMetaBlock.useCustomStateMapper())
						{
							String custom = ieMetaBlock.getCustomStateMapping(meta, true);
							if (custom != null)
								location += "_" + custom;
						}
						try
						{
							ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
						} catch (NullPointerException npe)
						{
							throw new RuntimeException("WELP! apparently " + ieMetaBlock + " lacks an item!", npe);
						}
					}
					if (isMD)
					{
						ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(new ResourceLocation("immersivepetroleum", "auto_lube"), "inventory"));
					}
				}
				else if (block instanceof BlockIPFluid)
					mapFluidState(block, ((BlockIPFluid) block).getFluid());
				else
					ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(loc, "inventory"));
		}

		for (Item item : IPContent.registeredIPItems)
		{
			if (item instanceof ItemIPBase)
			{
				ItemIPBase ipMetaItem = (ItemIPBase) item;
				if (ipMetaItem.registerSubModels && ipMetaItem.getSubNames() != null && ipMetaItem.getSubNames().length > 0)
				{
					for (int meta = 0; meta < ipMetaItem.getSubNames().length; meta++)
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
	*/
	
	@Override
	public World getClientWorld(){
		return Minecraft.getInstance().world;
	}
	
	@Override
	public PlayerEntity getClientPlayer(){
		return Minecraft.getInstance().player;
	}
	
	/*
	static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition{
		public final ModelResourceLocation location;
		
		public FluidStateMapper(Fluid fluid){
			this.location = new ModelResourceLocation(ImmersivePetroleum.MODID + ":fluid_block", fluid.getName());
		}
		
		@Nonnull
		@Override
		protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state){
			return location;
		}
		
		@Nonnull
		@Override
		public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack){
			return location;
		}
	}
	*/
}