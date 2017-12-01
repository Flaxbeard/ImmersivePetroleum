package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.blocks.BlockIEScaffoldSlab;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;

public class MultiblockDistillationTower implements IMultiblock
{
	public static MultiblockDistillationTower instance = new MultiblockDistillationTower();

	static ItemStack[][][] structure = new ItemStack[16][4][4];
	static{
		for(int h=0;h<16;h++)
			for(int l=0;l<4;l++)
				for(int w=0;w<4;w++)
				{
					
					if (h > 0 && l > 0 && l < 3 && w > 0 && w < 3)
					{
						structure[h][w][3-l] = new ItemStack(IEContent.blockSheetmetal,1, BlockTypes_MetalsAll.IRON.getMeta());
					}
					else if (l == 3 && w == 2)
					{
						structure[h][w][3-l] = new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
					}
					else if (h > 0 && (l == 0 && w == 1 && h < 13))
					{
						structure[h][w][3-l] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta());
					}
					else if (h > 0 && (h % 4 == 0))
					{
						structure[h][w][3-l] = new ItemStack(IEContent.blockMetalDecorationSlabs1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta());
					}
					else if (h == 0)
					{
						//if (l == 1 && w > 1)
						//	structure[h][w][3-l] = new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
						//else
						if (l > 1 && w == 0)
							structure[h][w][3-l] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
						else
							structure[h][w][3-l] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else if (h == 1)
					{
						if (l == 3 && w == 3)
							structure[h][w][3-l] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						else if (l > 1 && w == 0)
							structure[h][w][3-l] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
					}
					else if (h == 2)
					{
						if (w == 0 && l == 2)
							structure[h][w][3-l] = new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
					}
		
					if (structure[h][w][3-l] == null) {
						structure[h][w][3-l] = ItemStack.EMPTY;
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
		if (stack.getItem() == Item.getItemFromBlock(IEContent.blockMetalDecorationSlabs1)) {

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
			return true;
		}
		if (iterator == 25)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("100100");
			return true;
		}
		else if (iterator == 8)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("010001");
			return true;
		}
		else if (iterator == 134)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("100010");
			return true;
		}
		else if (iterator == 21 || iterator == 29
				|| iterator == 114
				|| iterator == 124
				|| iterator == 129
				|| (iterator < 100 && iterator > 29 && (iterator - 29) % 34 == 0)
				|| (iterator < 110 && iterator > 29 && (iterator - 29 - 6) % 34 == 0)
				|| (iterator < 100 && iterator > 29 && (iterator - 29 + 6) % 34 == 0)
				|| (iterator < 100 && iterator > 29 && (iterator - 29 - 17) % 34 == 0))
		{
			GlStateManager.pushMatrix();
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("110000");
			GlStateManager.popMatrix();
			return true;
		}
		return false;
	}
	@Override
	public IBlockState getBlockstateFromStack(int index, ItemStack stack)
	{
		if(!stack.isEmpty() && stack.getItem() instanceof ItemBlock)
		{
			return ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(stack.getItemDamage());
		}
		return null;
	}
	@Override
	public float getManualScale()
	{
		return 9;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	
	public Object te = null;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if (te == null)
		{
			 te = new TileEntityDistillationTower.TileEntityDistillationTowerParent();
		}
		
		ImmersivePetroleum.proxy.renderTile((TileEntity) te);
	}

	@Override
	public String getUniqueName()
	{
		return "IP:DistillationTower";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockMetalDecoration0 && (state.getBlock().getMetaFromState(state)==BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{

		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		boolean mirror = false;
		boolean b = this.structureCheck(world, pos, side, mirror);
		if(!b)
		{
			mirror = true;
			b = structureCheck(world, pos, side, mirror);
		}
		if(!b)
			return false;
		
		for (int h = -1; h <= 14; h++)
			for (int l = -3; l <= 0; l++)
				for (int w = 0; w <= 3; w++)
				{
					int ww = mirror?-w:w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

					if (h >= 0 && (h+1) % 4 != 0 
							&& (w == 0 || w == 3 || l == -3 || l == 0) 
							&& ((w != 3 || l != -2) || h >= 11)
							&& (h > 0 || (!(l == -3 && w == 0) && !(l == -3 && w == 1) && !(l == 0 && w == 0)))
							&& (h != 1 || !(l == -3 && w == 1))
							&& !(l == -1 && w == 0))
					{
						continue;
					}
					
					if (l == 0 && w == 0 && h == 0)
					{
						world.setBlockState(pos2, IPContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_IPMetalMultiblock.DISTILLATION_TOWER_PARENT.getMeta()));
					}
					else
					{
						world.setBlockState(pos2, IPContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_IPMetalMultiblock.DISTILLATION_TOWER.getMeta()));
					}
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityDistillationTower)
					{
						TileEntityDistillationTower tile = (TileEntityDistillationTower)curr;
						tile.facing=side;
						tile.formed=true;
						tile.pos = (h+1)*16 + (l+3)*4 + (w);
						tile.offset = new int[]{(side==EnumFacing.WEST?-l: side==EnumFacing.EAST?l: side==EnumFacing.NORTH?ww: -ww),h,(side==EnumFacing.NORTH?-l: side==EnumFacing.SOUTH?l: side==EnumFacing.EAST?ww : -ww)};
						tile.mirrored=mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, IPContent.blockMetalMultiblock, 255, 0);
					}
				}
		
		
		/*side = side.getOpposite();
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		boolean mirror = false;
		boolean b = this.structureCheck(world, pos, side, mirror);
		if(!b)
		{
			mirror = true;
			b = structureCheck(world, pos, side, mirror);
		}
		

		for(int h=-1;h<=1;h++)
			for(int l=-1;l<=1;l++)
				for(int w=-1;w<=1;w++)
				{
					if((h==0&&w==0&&l==-1)||(h==0&&w==1&&l>-1)||(h==1&&(l<0||w>0)))
						continue;

					int ww = mirror?-w:w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

					world.setBlockState(pos2, IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.FERMENTER.getMeta()));
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityFermenter)
					{
						TileEntityFermenter tile = (TileEntityFermenter)curr;
						tile.facing=side;
						tile.formed=true;
						tile.pos = (h+1)*9 + (l+1)*3 + (w+1);
						tile.offset = new int[]{(side==EnumFacing.WEST?-l: side==EnumFacing.EAST?l: side==EnumFacing.NORTH?ww: -ww),h,(side==EnumFacing.NORTH?-l: side==EnumFacing.SOUTH?l: side==EnumFacing.EAST?ww : -ww)};
						tile.mirrored=mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
					}
				}
		return true;*/
		return false;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{
		for (int h = -1; h <= 14; h++)
			for (int l = -3; l <= 0; l++)
				for (int w = 0; w <= 3; w++)
				{
					int ww = mirror?-w:w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);
					
					if (w == 0 && l == -1)
					{
						if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
							return false;
					}
					else if (h == -1)
					{
						if (l == -3 && w < 2)
						{
							if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
								return false;
						}
						/*else if (l > -2 && w == 2)
						{
							if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
								return false;
						}*/
						else
						{
							if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
					}
					else
					{
						if (l > -3 && l < 0 && w > 0 && w < 3)
						{
							if(!Utils.isOreBlockAt(world, pos, "blockSheetmetalIron"))
								return false;
						}
						else if (w == 3 && l == -2 && h < 12)
						{
							if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
						else if ((h + 1) % 4 == 0 && h > 0)
						{
							if (!(world.getBlockState(pos).getBlock() instanceof BlockIEScaffoldSlab)) {
								System.out.println(pos + " " + world.getBlockState(pos).getBlock());
								return false;

							}
						}
						else if (h == 0)
						{
							if (l == -3 && w < 2)
							{
								if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
									return false;
							}
						}
						else if (h == 1)
						{
							if (l == -3 && w == 1) 
							{
								if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
									return false;
							}
						}
					}
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 55),
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 17, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 4, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
			new IngredientStack("blockSheetmetalIron", 60)};
	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}