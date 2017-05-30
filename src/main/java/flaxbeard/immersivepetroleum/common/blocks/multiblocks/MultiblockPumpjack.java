package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;

public class MultiblockPumpjack implements IMultiblock
{
	public static MultiblockPumpjack instance = new MultiblockPumpjack();
	static ItemStack[][][] structure = new ItemStack[4][6][3];
	
	static{
		for(int h=0;h<4;h++)
			for(int l=0;l<6;l++)
				for(int w=0;w<3;w++)
				{
					if (h < 2 && w == 2 && l == 0)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					}
					else if (h < 3 && h > 0 && w == 1 && l == 1)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
					}
					else if (h == 1 && w == 0 && l == 0)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
					}
					else if (h == 0 && ((w == 1 && l > 2 && l < 5) || (l == 3)))
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
					}
					else if (l < 5 && l > 0 && w == 1 && h == 3)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.STEEL.getMeta());
					}
					else if ((h == 0 && (l < 5 || w == 1)))
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else if (w != 1 && l == 3)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockWoodenDecoration,1,BlockTypes_WoodenDecoration.FENCE.getMeta());
					}
					else if (h >= 2 && l == 5 && w == 1)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockStorage,1,BlockTypes_MetalsIE.STEEL.getMeta());
					}
					
					if (structure[h][l][w] == null) {
						structure[h][l][w] = ItemStack.EMPTY;
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
		if (iterator == 10)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("111000");
			return true;
		}
		else if (iterator == 11)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("120000");
			return true;
		}
		else if (iterator == 9)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("210000");
			return true;
		}
		else if (iterator == 13)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("002100");
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
		return 12;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	
	Object te;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if (te == null)
		{
			te = new TileEntityPumpjack.TileEntityPumpjackParent();
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.rotate(-90, 0, 1, 0);
		GlStateManager.translate(1, 1, -2);
		
		float pt = 0;
		if (Minecraft.getMinecraft().player != null)
		{
			((TileEntityPumpjack.TileEntityPumpjackParent) te).activeTicks = Minecraft.getMinecraft().player.ticksExisted;
			pt = Minecraft.getMinecraft().getRenderPartialTicks();
		}
		
		TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getSpecialRenderer((TileEntity) te);
		
		tesr.renderTileEntityAt((TileEntity) te, 0, 0, 0, pt, 0);
		GlStateManager.popMatrix();
	}

	@Override
	public String getUniqueName()
	{
		return "IP:Pumpjack";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockMetalDecoration0 && (state.getBlock().getMetaFromState(state)==BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		side = side.getOpposite();
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		boolean mirror = false;
		boolean b = this.structureCheck(world, pos.offset(side.getOpposite()), side, mirror);
		if(!b)
		{
			mirror = true;
			b = structureCheck(world, pos.offset(side.getOpposite()), side, mirror);
		}
		System.out.println(b);
		if(!b)
			return false;
		if (b)
			for (int h = -1; h <= 2; h++)
				for (int l = -1; l <= 4; l++)
					for (int w = -1; w <= 1; w++)
					{
					int ww = mirror?-w:w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);
					
					if ((h == -1 && l == 4 && w != 0)
							|| (h > -1 && w != 0 && l != 2 && (l != -1 || (w != -1 && w != 1) || h > 0))
							|| (h > -1 && h < 2 && w == 0 && l != 0 && (l != 4 || h != 1))
							|| (h == 2 && w == 0 && l == -1)) {
						continue;
					}
					
					if (l == 0 && w == 0 && h == 0)
					{
						world.setBlockState(pos2, IPContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_IPMetalMultiblock.PUMPJACK_PARENT.getMeta()));
					}
					else
					{
						world.setBlockState(pos2, IPContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_IPMetalMultiblock.PUMPJACK.getMeta()));
					}
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityPumpjack)
					{
						TileEntityPumpjack tile = (TileEntityPumpjack) curr;
						tile.facing=side;
						tile.formed=true;
						tile.pos = (h+1)*18 + (l+1)*3 + (w+1);
						tile.offset = new int[]{(side==EnumFacing.WEST?-l: side==EnumFacing.EAST?l: side==EnumFacing.NORTH?ww: -ww),h,(side==EnumFacing.NORTH?-l: side==EnumFacing.SOUTH?l: side==EnumFacing.EAST?ww : -ww)};
						tile.mirrored=mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, IPContent.blockMetalMultiblock, 255, 0);
					}
					
					/*if (h >= 0 && (h+1) % 4 != 0 
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
					}*/
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
		for (int h = -1; h <= 2; h++)
			for (int l = 0; l <= 5; l++)
				for (int w = -1; w <= 1; w++)
				{
					int ww = mirror?-w:w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);
					
					if (l == 0 && w == 1 && h < 1)
					{
						if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
							return false;
					}
					else if (l == 1 && w == 0 && h < 2 && h > 0)
					{
						if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
							return false;
					}
					else if (l == 0 && w == -1 && h == 0)
					{
						if(!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
							return false;
					}
					else if (h == -1 && ((l == 4 && w == 0) || l == 3))
					{
						if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
							return false;
					}
					else if (h == -1 && (l < 5 || w == 0))
					{
						if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
							return false;
					}
					else if (l == 3 && w != 0)
					{
						if (!Utils.isOreBlockAt(world, pos, "fenceTreatedWood"))
							return false;
					}
					else if (w == 0 && h == 2 && l > 0 && l < 5)
					{
						if (!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel"))
							return false;
					}
					else if (w == 0 && h >= 1 && l == 5)
					{
						if (!Utils.isOreBlockAt(world, pos, "blockSteel"))
							return false;
					}
					/*if (w == 0 && l == -1)
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
						}
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
						else if ((h + 1) % 4 == 0 && h > 0)
						{
							if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
						}
						else if (w == 3 && l == -2 && h < 11)
						{
							if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
								return false;
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
					}*/
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 11),
			new IngredientStack("fenceTreatedWood", 6),
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 4, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
			new IngredientStack("blockSteel", 2),
			new IngredientStack("blockSheetmetalSteel", 4)};
	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}