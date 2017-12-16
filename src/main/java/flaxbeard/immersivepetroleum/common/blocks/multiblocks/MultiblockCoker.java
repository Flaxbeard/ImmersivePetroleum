package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.blocks.BlockIEScaffoldSlab;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityCoker;

public class MultiblockCoker implements IMultiblock
{
	public static MultiblockCoker instance = new MultiblockCoker();
	static ItemStack[][][] structure = new ItemStack[23][5][9];
	static{
		for(int h=0;h<23;h++)
			for(int l=0;l<5;l++)
				for(int w=0;w<9;w++)
				{
					if (h <= 1)
					{
						if ((l == 0 || l == 4) && (w == 0 || w == 4 || w == 8))
						{
							structure[h][l][w] = new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE.getMeta());
						}
					}
					else if (h == 2)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE.getMeta());
					}
					else if (h <= 12)
					{
						if ((l == 0 || l == 4) && (w == 0 || w == 4 || w == 8))
						{
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
						}
						else if (l > 0 && l < 4 && w > 0 && w < 8 && w != 4 && !(h != 12 && ((w == 2 && l == 2) || (w == 6 && l == 2))))
						{
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal,1, BlockTypes_MetalsAll.IRON.getMeta());
						}
						else if (w == 0 && l == 2)
						{
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						}
						else if (((h == 7 || h == 12)) && (l != 2 || (w != 2 && w != 6)))
						{
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecorationSlabs1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						}
						else if (((h == 5 || h == 10) || (w == 0 && l == 2)) && (l != 2 || (w != 2 && w != 6)) && w != 4)
						{
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
						}
					}
					else
					{
						if (((h == 22 && w >= 2 && w <= 6) || (w == 2 || w == 6 || w == 4)) && l == 2)
						{
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						}
						else if (l == 2 && (w == 3 || w == 5))
						{
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
						}
						else if (h == 17 && l >= 1 && l <= 3 && w >= 1 && w <= 7)
						{
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecorationSlabs1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
						}
					}
				}
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		/*if (iterator >= 330 && iterator < 350 && (((iterator - (330)) % 5) == 4 || ((iterator - (330)) % 5) == 2))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 1, 0);
			GlStateManager.rotate(90, 1, 0, 0);
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("000011");
			GlStateManager.popMatrix();
			return true;
		}
		else if (iterator == 360 || iterator == 362)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 1, 0);
			GlStateManager.rotate(90, 1, 0, 0);
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("000011");
			GlStateManager.popMatrix();
			return true;
		}
		else if (iterator >= 372 && iterator < 391 && (((iterator - (370)) % 5) == 0 || ((iterator - (370)) % 5) == 3))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 1, 0);
			GlStateManager.rotate(90, 1, 0, 0);
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("000011");
			GlStateManager.popMatrix();
			return true;
		}*/
		if (stack.getItem() == Item.getItemFromBlock(IEContent.blockMetalDecorationSlabs1)) {

			ImmersivePetroleum.proxy.drawUpperHalfSlab(stack);
			return true;
		}
		return false;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null)
			renderStack = new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.EXCAVATOR.getMeta());
		GlStateManager.translate(2, 1.5, 2.875);
		GlStateManager.rotate(-225, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(5.25, 5.25, 5.25);

		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}
	@Override
	public float getManualScale()
	{
		return 6;
	}

	@Override
	public String getUniqueName()
	{
		return "IP:Coker";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		return Utils.compareToOreName(stack, "concrete");
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		side = side.getOpposite();
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		boolean mirror = false;
		boolean b = this.structureCheck(world, pos, side, mirror);
		if(!b)
		{
			mirror = true;
			b = structureCheck(world, pos, side, mirror);
		}
		System.out.println(b);
		if(!b)
			return false;


		if(b)
		{
			for(int l=0;l<5;l++)
				for(int w=-4;w<=4;w++)
					for(int h=-1;h<=21;h++)
					{
						int ww = mirror?-w:w;
						BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

						
						if ((h <= 0 && ((l > 0 && l < 4) || (w > 0 && w < 4) || (w < 0 && w > -4)))
								|| (h > 11 && h != 16 && (w < -2 || w > 2 || l != 2))
								|| (h == 16 && (w == -4 || w == 4 || l == 0 || l == 4))
								|| (h > 1 && h < 11 && h != 6 && h != 4 && h != 9 &&
									((l == 2 && (w == -2 || w == 2)) || (w == -4 && (l == 1 || l == 3)) || ((w == 0 || w == 4) && l > 0 && l < 4) || ((l == 0 || l == 4) && w > -4 && w < 4 && w != 0)))
								|| (h == 6 && l == 2 && (w == -2 || w == 2))
								|| ((h == 4 || h == 9) && l != 0 && l != 4 &&  (w == 0 || (l == 2 && (w == -2 || w == 2)))))
							continue;
						
						//	continue;
					
						if (l == 0 && w == 0 && h == 0)
						{
							world.setBlockState(pos2, IPContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_IPMetalMultiblock.COKER_PARENT.getMeta()));
						}
						else
						{
							world.setBlockState(pos2, IPContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_IPMetalMultiblock.COKER.getMeta()));
						}
						TileEntity curr = world.getTileEntity(pos2);
						if(curr instanceof TileEntityCoker)
						{
							TileEntityCoker tile = (TileEntityCoker) curr;
							tile.facing=side;
							tile.formed=true;
							tile.pos = (h+1)*(9*5) + (l)*9 + (w+4);
							tile.offset = new int[]{(side==EnumFacing.WEST?-l: side==EnumFacing.EAST?l: side==EnumFacing.NORTH?ww: -ww),h,(side==EnumFacing.NORTH?-l: side==EnumFacing.SOUTH?l: side==EnumFacing.EAST?ww : -ww)};
							tile.mirrored=mirror;
							tile.markDirty();
							world.addBlockEvent(pos2, IPContent.blockMetalMultiblock, 255, 0);
						}
					}			
		}
		return b;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{

		for(int l=0;l<5;l++)
			for(int w=-4;w<=4;w++)
				for(int h=-1;h<=21;h++)
				{

					int ww = mirror?-w:w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);
					
					if (h <= 0 && (l == 0 || l == 4) && (w == 0 || w == -4 || w == 4))
					{
						if(!Utils.isOreBlockAt(world, pos, "concrete"))
							return false;
					}
					else if (h == 1)
					{
						if(!Utils.isOreBlockAt(world, pos, "concrete"))
							return false;
					}
					else if (h > 1 && h < 12)
					{
						if (h == 11 && l > 0 && l < 4 && (w != 0 && w != 4 && w != -4))
						{
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalIron"))
								return false;
						}
						else if (l > 0 && l < 4 && (w != 0 && w != 4 && w != -4) && ((w != -2 && w != 2) || (l == 1 || l == 3)))
						{
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalIron"))
								return false;
						}
						else if ((w == -4 || w == 0 || w == 4) && (l == 0 || l == 4))
						{
							if (!Utils.isOreBlockAt(world, pos, "fenceSteel"))
								return false;
						}
						else if (w == -4 && l == 2)
						{
							if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
						else if ((h == 11 || h == 6) && (l == 0 || l == 4 || w == 0 || w == 4 || w == -4))
						{
							if (!(world.getBlockState(pos).getBlock() instanceof BlockIEScaffoldSlab))
								return false;
						}
						else if ((h == 9 || h == 4) && (l == 0 || l == 4 || w == 4 || w == -4))
						{
							if (!Utils.isOreBlockAt(world, pos, "fenceSteel"))
								return false;
						}
					}
					else if (h >= 11)
					{
						if (l == 2 && (w == -2 || w == 2 || w == 0))
						{
							if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
						else if (h == 21 && l == 2 && (w >= -2 && w <= 2))
						{
							if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
						else if (l == 2 && (w == -1 || w == 1))
						{
							if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
								return false;
						}
						else if (h == 16 && l > 0 && l < 4 && w > -4 && w < 4)
						{
							if (!(world.getBlockState(pos).getBlock() instanceof BlockIEScaffoldSlab))
								return false;
						}
					}

				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 6),
			new IngredientStack("blockSheetmetalSteel", 15),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 9, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 5, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 3, BlockTypes_MetalDecoration0.RADIATOR.getMeta()))};
	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}