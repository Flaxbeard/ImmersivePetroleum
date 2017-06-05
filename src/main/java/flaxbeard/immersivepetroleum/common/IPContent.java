package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import blusunrize.immersiveengineering.common.util.IEPotions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPBase;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPFluid;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPMetalMultiblocks;
import flaxbeard.immersivepetroleum.common.blocks.BlockIPScaffoldSlab;
import flaxbeard.immersivepetroleum.common.blocks.ItemBlockIPBase;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_Dummy;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_MetalDecorationSlab;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityCoker;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockCoker;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.MultiblockPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.stone.BlockTypes_IPStoneDecoration;
import flaxbeard.immersivepetroleum.common.blocks.wood.BlockTypes_WoodDecorationSlab;
import flaxbeard.immersivepetroleum.common.items.ItemIPBase;
import flaxbeard.immersivepetroleum.common.items.ItemProjector;

public class IPContent
{
	public static ArrayList<Block> registeredIPBlocks = new ArrayList<Block>();

	public static BlockIPFluid blockFluidCrudeOil;
	public static BlockIPFluid blockFluidDiesel;
	
	public static BlockIPBase blockMetalMultiblock;
	
	public static BlockIPBase blockStoneDecoration;
	
	public static BlockIPBase blockMetalDecorationSlabs;
	public static BlockIPBase blockWoodenDecorationSlabs;

	public static BlockIPBase blockDummy;

	
	public static ArrayList<Item> registeredIPItems = new ArrayList<Item>();

	public static Item itemMaterial;
	public static Item itemProjector;

	public static Fluid fluidCrudeOil;
	public static Fluid fluidDiesel;

	public static void preInit()
	{
		fluidCrudeOil = new Fluid("oil", new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/oil_still"), new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/oil_flow")).setDensity(1000).setViscosity(2250);
		if(!FluidRegistry.registerFluid(fluidCrudeOil))
			fluidCrudeOil = FluidRegistry.getFluid("oil");
		FluidRegistry.addBucketForFluid(fluidCrudeOil);

		fluidDiesel = new Fluid("diesel", new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/diesel_still"), new ResourceLocation(ImmersivePetroleum.MODID + ":blocks/fluid/diesel_flow")).setDensity(789).setViscosity(1750);
		if(!FluidRegistry.registerFluid(fluidDiesel))
			fluidDiesel = FluidRegistry.getFluid("diesel");
		FluidRegistry.addBucketForFluid(fluidDiesel);
		
		blockFluidCrudeOil = new BlockIPFluid("fluid_crude_oil", fluidCrudeOil, Material.WATER).setFlammability(60, 200);
		blockFluidDiesel = new BlockIPFluid("fluid_diesel", fluidDiesel, Material.WATER).setFlammability(60, 200);
		blockMetalMultiblock = new BlockIPMetalMultiblocks();

		blockStoneDecoration = (BlockIPBase)new BlockIPBase("stone_decoration", Material.ROCK, PropertyEnum.create("type", BlockTypes_IPStoneDecoration.class), ItemBlockIPBase.class).setHardness(2.0F).setResistance(10.0F);
		blockDummy = (BlockIPBase)new BlockIPBase("dummy", Material.ROCK, PropertyEnum.create("type", BlockTypes_Dummy.class), ItemBlockIPBase.class);

		blockWoodenDecorationSlabs = (BlockIPBase) new BlockIPScaffoldSlab("wood_decoration_slabs", Material.WOOD, PropertyEnum.create("type", BlockTypes_WoodDecorationSlab.class)).setHardness(2.0F).setResistance(5.0F);
		blockMetalDecorationSlabs = (BlockIPBase) new BlockIPScaffoldSlab("metal_decoration_slabs", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDecorationSlab.class)).setHardness(3.0F).setResistance(15.0F);

		itemMaterial = new ItemIPBase("material", 64,
				"bitumen");
		
		itemProjector = new ItemProjector("schematic");

	}
	
	public static void init()
	{
		blockFluidCrudeOil.setPotionEffects(new PotionEffect(IEPotions.flammable,100,1));
		blockFluidDiesel.setPotionEffects(new PotionEffect(IEPotions.flammable,100,1));
		
		registerTile(TileEntityDistillationTower.class);
		registerTile(TileEntityDistillationTower.TileEntityDistillationTowerParent.class);
		registerTile(TileEntityPumpjack.class);
		registerTile(TileEntityPumpjack.TileEntityPumpjackParent.class);
		registerTile(TileEntityCoker.class);
		registerTile(TileEntityCoker.TileEntityCokerParent.class);
		
		DistillationRecipe.addRecipe(new FluidStack(fluidDiesel, 25), new ItemStack(itemMaterial, 1, 0), new FluidStack(fluidCrudeOil, 25), 2048, 1, .07F);

		MultiblockHandler.registerMultiblock(MultiblockDistillationTower.instance);
		MultiblockHandler.registerMultiblock(MultiblockPumpjack.instance);
		MultiblockHandler.registerMultiblock(MultiblockCoker.instance);

		IERecipes.addIngredientRecipe(new ItemStack(blockStoneDecoration, 8, BlockTypes_IPStoneDecoration.ASPHALT.getMeta()), "SCS", "GBG", "SCS", 'C', new ItemStack(itemMaterial, 1, 0), 'S', "sand", 'G', Blocks.GRAVEL, 'B', new FluidStack(FluidRegistry.WATER,1000)).allowQuarterTurn();
		IERecipes.addIngredientRecipe(new ItemStack(blockStoneDecoration, 12, BlockTypes_IPStoneDecoration.ASPHALT.getMeta()), "SCS", "GBG", "SCS", 'C', new ItemStack(itemMaterial, 1, 0), 'S', "itemSlag", 'G', Blocks.GRAVEL, 'B', new FluidStack(FluidRegistry.WATER,1000)).allowQuarterTurn();
	
		IERecipes.addTwoWaySlabRecipe(new ItemStack(blockWoodenDecorationSlabs, 1, 0), new ItemStack(IEContent.blockWoodenDecoration, 1, 1));
		IERecipes.addTwoWaySlabRecipe(new ItemStack(blockWoodenDecorationSlabs, 1, 0), new ItemStack(IEContent.blockWoodenDecoration, 1, 1));

		if (!IPConfig.Tools.disable_projector)
		{
			IERecipes.addIngredientRecipe(new ItemStack(itemProjector, 1, 0), "S  ", "IL ", " IW", 
					'W', "plankTreatedWood", 
					'L', new ItemStack(IEContent.blockMetalDecoration2, 1, BlockTypes_MetalDecoration2.LANTERN.getMeta()), 
					'S', new ItemStack(IEContent.itemToolUpgrades, 1, 8),
					'I', "ingotIron");

			GameRegistry.addRecipe(new SchematicCraftingHandler());
		}
		
		Config.addConfigReservoirs(IPConfig.reservoirs.reservoirs);

		IERecipes.addTwoWaySlabRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.STEEL_SCAFFOLDING_0.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()));
		IERecipes.addTwoWaySlabRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.STEEL_SCAFFOLDING_1.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()));
		IERecipes.addTwoWaySlabRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.STEEL_SCAFFOLDING_2.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()));

		IERecipes.addTwoWaySlabRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.ALUMINUM_SCAFFOLDING_0.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()));
		IERecipes.addTwoWaySlabRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.ALUMINUM_SCAFFOLDING_1.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()));
		IERecipes.addTwoWaySlabRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.ALUMINUM_SCAFFOLDING_2.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta()));

		IERecipes.addShapelessOredictRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.STEEL_SCAFFOLDING_1.getMeta()), new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.STEEL_SCAFFOLDING_0.getMeta()));
		IERecipes.addShapelessOredictRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.STEEL_SCAFFOLDING_2.getMeta()), new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.STEEL_SCAFFOLDING_1.getMeta()));
		IERecipes.addShapelessOredictRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.STEEL_SCAFFOLDING_0.getMeta()), new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.STEEL_SCAFFOLDING_2.getMeta()));
	
		IERecipes.addShapelessOredictRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.ALUMINUM_SCAFFOLDING_1.getMeta()), new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.ALUMINUM_SCAFFOLDING_0.getMeta()));
		IERecipes.addShapelessOredictRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.ALUMINUM_SCAFFOLDING_2.getMeta()), new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.ALUMINUM_SCAFFOLDING_1.getMeta()));
		IERecipes.addShapelessOredictRecipe(new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.ALUMINUM_SCAFFOLDING_0.getMeta()), new ItemStack(blockMetalDecorationSlabs, 1, BlockTypes_MetalDecorationSlab.ALUMINUM_SCAFFOLDING_2.getMeta()));
	}
	
	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersivePetroleum.MODID+":"+ s);
	}
}
