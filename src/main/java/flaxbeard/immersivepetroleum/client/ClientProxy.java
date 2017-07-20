package flaxbeard.immersivepetroleum.client;

import static blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection.vertices;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.IECustomStateMapper;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import blusunrize.lib.manual.ManualPages;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.client.page.ManualPageBigMultiblock;
import flaxbeard.immersivepetroleum.client.page.ManualPageSchematicCrafting;
import flaxbeard.immersivepetroleum.client.render.MultiblockDistillationTowerRenderer;
import flaxbeard.immersivepetroleum.client.render.MultiblockPumpjackRenderer;
import flaxbeard.immersivepetroleum.client.render.RenderSpeedboat;
import flaxbeard.immersivepetroleum.client.render.TileAutoLubricatorRenderer;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
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
import flaxbeard.immersivepetroleum.common.sound.IPEntitySound;


public class ClientProxy extends CommonProxy
{
	public static SoundEvent projector;
	public static final String CAT_IP = "ip";
	
	@Override
	public void preInit()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntitySpeedboat.class, RenderSpeedboat::new);
	}
	
	HashMap<Integer, IPEntitySound> entitySoundMap = new HashMap<Integer, IPEntitySound>();
	@Override
	public void handleEntitySound(SoundEvent soundEvent, Entity e, boolean active, float volume, float pitch)
	{
		IPEntitySound sound = entitySoundMap.get(e.getEntityId());
		if (sound == null && active)
		{
			sound = generateEntitySound(soundEvent, volume, pitch, true, 0, e);
			entitySoundMap.put(e.getEntityId(), sound);
		}
		else if(sound!=null && !active)
		{
			sound.donePlaying=true;
			ClientUtils.mc().getSoundHandler().stopSound(sound);
			entitySoundMap.remove(e.getEntityId());
		}
	}
	
	public static IPEntitySound generateEntitySound(SoundEvent soundEvent, float volume, float pitch, boolean repeat, int delay, Entity e)
	{
		IPEntitySound sound = new IPEntitySound(soundEvent, volume, pitch, repeat, delay, e, AttenuationType.LINEAR, SoundCategory.AMBIENT);
//		sound.evaluateVolume();
		ClientUtils.mc().getSoundHandler().playSound(sound);
		return sound;
	}
	
	@Override
	public void preInitEnd()
	{
		//Going through registered stuff at the end of preInit, because of compat modules possibly adding items
		for (Block block : IPContent.registeredIPBlocks)
		{
			Item blockItem = Item.getItemFromBlock(block);
			final ResourceLocation loc = GameData.getBlockRegistry().getNameForObject(block);
			if (block == IPContent.blockMetalDevice)
			{
				ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(new ResourceLocation("immersivepetroleum", "auto_lube"), "inventory"));
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
				for(int meta = 1; meta < ieMetaBlock.getMetaEnums().length; meta++)
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
			}
			else if (block instanceof IIEMetaBlock)
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
		ResourceLocation location = new ResourceLocation(ImmersivePetroleum.MODID, "projector");
		projector = new SoundEvent(location);
		projector.setRegistryName(location);
		GameRegistry.register(projector);
		MinecraftForge.EVENT_BUS.register(IPCoreSampleModelHandler.instance);
	}

	@Override
	public void postInit()
	{
		
		if (!IPConfig.Tools.disable_projector)
		{
			ManualHelper.addEntry("schematics", CAT_IP,
					new ManualPages.Crafting(ManualHelper.getManual(), "schematics0", new ItemStack(IPContent.itemProjector, 1, 0)),
					new ManualPageSchematicCrafting(ManualHelper.getManual(), "schematics1", new ItemStack(IPContent.itemProjector, 1, 0)),
					new ManualPages.Text(ManualHelper.getManual(), "schematics2"));
		}

		handleReservoirManual();

		if (!IPConfig.Extraction.disable_pumpjack)
			ManualHelper.addEntry("pumpjack", CAT_IP,
					new ManualPageMultiblock(ManualHelper.getManual(), "pumpjack0", MultiblockPumpjack.instance),
					new ManualPages.Text(ManualHelper.getManual(), "pumpjack1"));
		
		if (!IPConfig.Refining.disable_tower)
		{
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
		}
		
		ManualHelper.addEntry("portableGenerator", CAT_IP,
				new ManualPages.Crafting(ManualHelper.getManual(), "portableGenerator0", new ItemStack(IPContent.blockMetalDevice, 1, 1)),
				new ManualPages.Text(ManualHelper.getManual(), "portableGenerator1"));
		
		ManualHelper.addEntry("speedboat", CAT_IP,
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat0", new ItemStack(IPContent.itemSpeedboat, 1, 0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat1", new ItemStack(IPContent.itemUpgrades, 1, 2)),
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat2", new ItemStack(IPContent.itemUpgrades, 1, 3)),
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat3", new ItemStack(IPContent.itemUpgrades, 1, 1)),
				new ManualPages.Crafting(ManualHelper.getManual(), "speedboat4", new ItemStack(IPContent.itemUpgrades, 1, 0))
		);
		
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
	            bucket.fill(stack, fs, true);
	            
				ItemStack[] displayStacks = new ItemStack[] { stack };
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
	
	public static Vec3d[] getConnectionCatenary(Vec3d start, Vec3d end, float slack)
	{
		boolean vertical = false;

		if (vertical)
			return new Vec3d[]{new Vec3d(start.xCoord, start.yCoord, start.zCoord), new Vec3d(end.xCoord, end.yCoord, end.zCoord)};

		double dx = (end.xCoord)-(start.xCoord);
		double dy = (end.yCoord)-(start.yCoord);
		double dz = (end.zCoord)-(start.zCoord);
		double dw = Math.sqrt(dx*dx + dz*dz);
		double k = Math.sqrt(dx*dx + dy*dy + dz*dz) * slack;
		double l = 0;
		int limiter = 0;
		while(!vertical && limiter<300)
		{
			limiter++;
			l += 0.01;
			if (Math.sinh(l)/l >= Math.sqrt(k*k - dy*dy)/dw)
				break;
		}
		double a = dw/2/l;
		double p = (0+dw-a*Math.log((k+dy)/(k-dy)))*0.5;
		double q = (dy+0-k*Math.cosh(l)/Math.sinh(l))*0.5;

		Vec3d[] vex = new Vec3d[vertices+1];

		vex[0] = new Vec3d(start.xCoord, start.yCoord, start.zCoord);
		for(int i=1; i<vertices; i++)
		{
			float n1 = i/(float)vertices;
			double x1 = 0 + dx * n1;
			double z1 = 0 + dz * n1;
			double y1 = a * Math.cosh((( Math.sqrt(x1*x1+z1*z1) )-p)/a)+q;
			vex[i] = new Vec3d(start.xCoord+x1, start.yCoord+y1, start.zCoord+z1);
		}
		vex[vertices] = new Vec3d(end.xCoord, end.yCoord, end.zCoord);

		return vex;
	}
	
	public static void tessellateConnection(World world, WireType type, float xs, float ys, float zs, float xe, float ye, float ze)
	{
		int col = type.getColour(null);
		double r = type.getRenderDiameter() / 2;
		int[] rgba = new int[]{col >> 16 & 255, col >> 8 & 255, col & 255, 255};
		tessellateConnection(world, type, xs, ys, zs, xe, ye, ze, rgba, r, type.getIcon(null));
	}
	
	public static void tessellateConnection(World world, WireType type, float xs, float ys, float zs, float xe, float ye, float ze, int[] rgba, double radius, TextureAtlasSprite sprite)
	{
		double dx = xe - xs;
		double dy = ye - ys;
		double dz = ze - zs;
		double dw = Math.sqrt(dx * dx + dz * dz);
		double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
		Tessellator tes = ClientUtils.tes();

		double rmodx = dz / dw;
		double rmodz = dx / dw;

		Vec3d[] vertex = getConnectionCatenary(new Vec3d(xs, ys, zs), new Vec3d(xe, ye, ze), 1.5f);
		//		Vec3 iniPos = new Vec3(connection.start.getX()+startOffset.xCoord, connection.start.getY()+startOffset.yCoord, connection.start.getZ()+startOffset.zCoord);
		Vec3d initPos = new Vec3d(xs, ys, zs);

		double uMin = sprite.getMinU();
		double uMax = sprite.getMaxU();
		double vMin = sprite.getMinV();
		double vMax = sprite.getMaxV();
		double uD = uMax - uMin;
		boolean vertical = false;
		boolean b = (dx < 0 && dz <= 0) || (dz < 0 && dx <= 0) || (dz < 0 && dx > 0);


		VertexBuffer worldrenderer = tes.getBuffer();
		//		worldrenderer.pos(x, y+h, 0).tex(uv[0], uv[3]).endVertex();
		//		worldrenderer.pos(x+w, y+h, 0).tex(uv[1], uv[3]).endVertex();
		//		worldrenderer.pos(x+w, y, 0).tex(uv[1], uv[2]).endVertex();
		//		worldrenderer.pos(x, y, 0).tex(uv[0], uv[2]).endVertex();
		if(vertical)
		{
			//			double uShift = Math.abs(dy)/ * uD;
			//			worldrenderer.pos(x, y, z)
			worldrenderer.setTranslation(initPos.xCoord, initPos.yCoord, initPos.zCoord);

			//			tes.addVertexWithUV(0-radius, 0, 0, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0 - radius, 0, 0).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx-radius, dy, dz, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx - radius, dy, dz).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx+radius, dy, dz, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx + radius, dy, dz).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0+radius, 0, 0, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0 + radius, 0, 0).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

			//			tes.addVertexWithUV(dx-radius, dy, dz, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx - radius, dy, dz).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0-radius, 0, 0, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0 - radius, 0, 0).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0+radius, 0, 0, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0 + radius, 0, 0).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx+radius, dy, dz, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx + radius, dy, dz).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();


			//			tes.addVertexWithUV(0, 0, 0-radius, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0, 0, 0 - radius).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx, dy, dz-radius, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx, dy, dz - radius).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx, dy, dz+radius, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx, dy, dz + radius).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0, 0, 0+radius, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0, 0, 0 + radius).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

			//			tes.addVertexWithUV(dx, dy, dz-radius, b?uMin:uMin+uShift,vMin);
			worldrenderer.pos(dx, dy, dz - radius).tex(uMax, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0, 0, 0-radius, b?uMax-uShift:uMin,vMin);
			worldrenderer.pos(0, 0, 0 - radius).tex(uMin, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(0, 0, 0+radius, b?uMax-uShift:uMin,vMax);
			worldrenderer.pos(0, 0, 0 + radius).tex(uMin, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			//			tes.addVertexWithUV(dx, dy, dz+radius, b?uMin:uMin+uShift,vMax);
			worldrenderer.pos(dx, dy, dz + radius).tex(uMax, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
			worldrenderer.setTranslation(0, 0, 0);
		} else
		{
			double u0 = uMin;
			double u1 = uMin;
			for(int i = b ? (vertex.length - 1) : 0; (b ? (i >= 0) : (i < vertex.length)); i += (b ? -1 : 1))
			{
				Vec3d v0 = i > 0 ? vertex[i - 1].subtract(xs, ys, zs) : initPos;
				Vec3d v1 = vertex[i].subtract(xs, ys, zs);

				//				double u0 = uMin;
				//				double u1 = uMax;
				u0 = u1;
				u1 = u0 + (v0.distanceTo(v1) / d) * uD;
				if((dx < 0 && dz <= 0) || (dz < 0 && dx <= 0) || (dz < 0 && dx > 0))
				{
					u1 = uMin;
					u0 = uMax;
				}
				worldrenderer.pos(v0.xCoord, v0.yCoord + radius, v0.zCoord).tex(u0, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord, v1.yCoord + radius, v1.zCoord).tex(u1, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord, v1.yCoord - radius, v1.zCoord).tex(u1, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord, v0.yCoord - radius, v0.zCoord).tex(u0, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

				worldrenderer.pos(v1.xCoord, v1.yCoord + radius, v1.zCoord).tex(u1, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord, v0.yCoord + radius, v0.zCoord).tex(u0, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord, v0.yCoord - radius, v0.zCoord).tex(u0, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord, v1.yCoord - radius, v1.zCoord).tex(u1, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

				worldrenderer.pos(v0.xCoord - radius * rmodx, v0.yCoord, v0.zCoord + radius * rmodz).tex(u0, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord - radius * rmodx, v1.yCoord, v1.zCoord + radius * rmodz).tex(u1, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord + radius * rmodx, v1.yCoord, v1.zCoord - radius * rmodz).tex(u1, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord + radius * rmodx, v0.yCoord, v0.zCoord - radius * rmodz).tex(u0, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

				worldrenderer.pos(v1.xCoord - radius * rmodx, v1.yCoord, v1.zCoord + radius * rmodz).tex(u1, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord - radius * rmodx, v0.yCoord, v0.zCoord + radius * rmodz).tex(u0, vMax).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v0.xCoord + radius * rmodx, v0.yCoord, v0.zCoord - radius * rmodz).tex(u0, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
				worldrenderer.pos(v1.xCoord + radius * rmodx, v1.yCoord, v1.zCoord - radius * rmodz).tex(u1, vMin).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();

			}
		}
		//		tes.setColorRGBA_I(0xffffff, 0xff);
	}
	//
	//	public static int calcBrightness(IBlockAccess world, double x, double y, double z)
	//	{
	//		return world.getLightBrightnessForSkyBlocks((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z), 0);
	//	}
	//
	//
	//	public static void tessellateBox(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, IIcon icon)
	//	{
	//		tes().addVertexWithUV(xMin,yMin,zMax, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(zMax*16));
	//		tes().addVertexWithUV(xMin,yMin,zMin, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(zMin*16));
	//		tes().addVertexWithUV(xMax,yMin,zMin, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(zMin*16));
	//		tes().addVertexWithUV(xMax,yMin,zMax, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(zMax*16));
	//
	//		tes().addVertexWithUV(xMin,yMax,zMin, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(zMin*16));
	//		tes().addVertexWithUV(xMin,yMax,zMax, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(zMax*16));
	//		tes().addVertexWithUV(xMax,yMax,zMax, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(zMax*16));
	//		tes().addVertexWithUV(xMax,yMax,zMin, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(zMin*16));
	//
	//		tes().addVertexWithUV(xMax,yMin,zMin, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMin,yMin,zMin, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMin,yMax,zMin, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(yMin*16));
	//		tes().addVertexWithUV(xMax,yMax,zMin, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(yMin*16));
	//
	//		tes().addVertexWithUV(xMin,yMin,zMax, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMax,yMin,zMax, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMax,yMax,zMax, icon.getInterpolatedU(xMin*16),icon.getInterpolatedV(yMin*16));
	//		tes().addVertexWithUV(xMin,yMax,zMax, icon.getInterpolatedU(xMax*16),icon.getInterpolatedV(yMin*16));
	//
	//		tes().addVertexWithUV(xMin,yMin,zMin, icon.getInterpolatedU(zMin*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMin,yMin,zMax, icon.getInterpolatedU(zMax*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMin,yMax,zMax, icon.getInterpolatedU(zMax*16),icon.getInterpolatedV(yMin*16));
	//		tes().addVertexWithUV(xMin,yMax,zMin, icon.getInterpolatedU(zMin*16),icon.getInterpolatedV(yMin*16));
	//
	//		tes().addVertexWithUV(xMax,yMin,zMax, icon.getInterpolatedU(zMax*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMax,yMin,zMin, icon.getInterpolatedU(zMin*16),icon.getInterpolatedV(yMax*16));
	//		tes().addVertexWithUV(xMax,yMax,zMin, icon.getInterpolatedU(zMin*16),icon.getInterpolatedV(yMin*16));
	//		tes().addVertexWithUV(xMax,yMax,zMax, icon.getInterpolatedU(zMax*16),icon.getInterpolatedV(yMin*16));
	//	}
	//
}