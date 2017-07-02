package flaxbeard.immersivepetroleum.client.render;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes.Base;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator;

public class TileAutoLubricatorRenderer extends TileEntitySpecialRenderer<TileEntityAutoLubricator>
{

	private static Base base = new ModelLubricantPipes.Base();

	@Override
	public boolean isGlobalRenderer(TileEntityAutoLubricator te)
    {
        return true;
    }
	
	@Override
	public void renderTileEntityAt(TileEntityAutoLubricator te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (te == null)
		{
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			
			
			GL11.glScalef(0.65F, 0.65F, 0.65F);
			GL11.glTranslatef(0.25F, -0.1F, 0.25F);
			
			ClientUtils.bindTexture("immersivepetroleum:textures/models/lubricator.png");
			base.render(null, 0, 0, 0, 0, 0, 0.0625F);
			
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			base.renderTank(null, 0, 0, 0, 0, 0, 0.0625F);
			
			//base.renderPlunger(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.popMatrix();
			return;
		}
		
		if (te.isDummy() || !te.getWorld().isBlockLoaded(te.getPos(), false))
			return;
	
		int pass = net.minecraftforge.client.MinecraftForgeClient.getRenderPass();
		
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);

		
		float height = 16;
		FluidStack fs = te.tank.getFluid();
		float level = 0;
		if (fs != null)
		{
			level = fs.amount / (float)te.tank.getCapacity();
		}
		float yOffset = (1 - level) * height * -1F/16F;
		float scale = .0625f;
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		if (pass == 1 && level > 0)
		{
			RenderHelper.disableStandardItemLighting();

			GlStateManager.pushMatrix();
			//ShaderUtil.alpha_static(0.25f, 1);
			GlStateManager.translate(-4F/16F, 6/16F, -4F/16F);
			GlStateManager.scale(scale, scale, scale);
			float h = level * height;
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 8, h);
			GlStateManager.rotate(90,0,1,0);
			GlStateManager.translate(-7.98, 0, 0);
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 8, h);
			GlStateManager.rotate(90,0,1,0);
			GlStateManager.translate(-7.98,0,0);
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 8, h);
			GlStateManager.rotate(90,0,1,0);
			GlStateManager.translate(-7.98,0,0);
			ClientUtils.drawRepeatedFluidSprite(fs, 0,0, 8,h);

			GlStateManager.rotate(90,1,0,0);
			GlStateManager.translate(0,0,-h);
			ClientUtils.drawRepeatedFluidSprite(fs, 0,0, 8,8);
			GlStateManager.rotate(180, 1, 0, 0);
			GlStateManager.translate(0, -9, -h);
			ClientUtils.drawRepeatedFluidSprite(fs, 0,0, 8,8);

			GlStateManager.scale(1/scale, 1/scale, 1/scale);
			GlStateManager.translate(0,-1,-1);
			GlStateManager.popMatrix();
				//ShaderUtil.releaseShader();
			
			RenderHelper.enableStandardItemLighting();

		}

		GlStateManager.popMatrix();
		
		if (pass == 1)
		{
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			ClientUtils.bindTexture("immersivepetroleum:textures/models/lubricator.png");
			base.renderTank(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.popMatrix();
		}

		if (pass == 0)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			
			BlockPos target = te.getPos().offset(te.getFacing());
			TileEntity test = te.getWorld().getTileEntity(target);

			ILubricationHandler handler = LubricatedHandler.getHandlerForTile(test);
			if (handler != null)
			{
				TileEntity master = handler.isPlacedCorrectly(te.getWorld(), te, te.getFacing());
				if (master != null)
				{
					handler.renderPipes(te.getWorld(), te, te.getFacing(), master);
				}
			}
			GlStateManager.popMatrix();

			/*BlockPos pos = te.getPos().offset(te.getFacing());
			TileEntity pj = te.getWorld().getTileEntity(pos);
			if (pj instanceof TileEntityPumpjack)
			{
				BlockPos masterPos = te.getPos().offset(te.getFacing(), 2).up();
				TileEntityPumpjack base = ((TileEntityPumpjack)pj).master();
				TileEntity target = te.getWorld().getTileEntity(masterPos);
				
				EnumFacing facing = base.mirrored ? te.getFacing() : te.getFacing().getOpposite() ;
				if (base != null && base == target && base.getFacing().rotateY() == facing)
				{
	
					GlStateManager.pushMatrix();
					GlStateManager.translate(x, y - 1, z);
					Vec3i offset = base.getPos().subtract(te.getPos());
					GlStateManager.translate(offset.getX(), offset.getY(), offset.getZ());
	
					EnumFacing rotation = base.facing;
					if (rotation == EnumFacing.NORTH)
					{
						GlStateManager.rotate(90F, 0, 1, 0);
						GlStateManager.translate(-1, 0, 0);
					}
					else if (rotation == EnumFacing.WEST)
					{
						GlStateManager.rotate(180F, 0, 1, 0);
						GlStateManager.translate(-1, 0, -1);
					}
					else if (rotation == EnumFacing.SOUTH)
					{
						GlStateManager.rotate(270F, 0, 1, 0);
						GlStateManager.translate(0, 0, -1);
					}
					GlStateManager.translate(-1, 0, -1);
					ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
					if (base.mirrored)
					{
						pumpjackM.render(null, 0, 0, 0, 0, 0, 0.0625F);
					}
					else
					{
						pumpjack.render(null, 0, 0, 0, 0, 0, 0.0625F);
					}
					GlStateManager.popMatrix();
					
	
				}
			}
			else if (pj instanceof TileEntityExcavator)
			{
				TileEntityExcavator base = ((TileEntityExcavator)pj).master();
				EnumFacing facing = base.mirrored ? te.getFacing().rotateY() : te.getFacing().rotateY().getOpposite();
				BlockPos masterPos = pos.offset(te.getFacing(), 1).offset(facing, 4).up();
				TileEntity target = te.getWorld().getTileEntity(masterPos);

				if (target == base && base != null && base.getFacing().getOpposite() == facing)
				{
					GlStateManager.pushMatrix();
					GlStateManager.translate(x, y - 1, z);
					Vec3i offset = base.getPos().subtract(te.getPos());
					GlStateManager.translate(offset.getX(), offset.getY(), offset.getZ());
	
					EnumFacing rotation = base.facing;
					if (rotation == EnumFacing.NORTH)
					{
						GlStateManager.rotate(90F, 0, 1, 0);
					}
					else if (rotation == EnumFacing.WEST)
					{
						GlStateManager.rotate(180F, 0, 1, 0);
						GlStateManager.translate(0, 0, -1);

					}
					else if (rotation == EnumFacing.SOUTH)
					{
						GlStateManager.rotate(270F, 0, 1, 0);
					}
					GlStateManager.translate(-1, 0, -1);
					ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
					excavator.render(null, 0, 0, 0, 0, 0, 0.0625F);
					GlStateManager.popMatrix();
					
	
				}
			}*/
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + .5F, y + .5F, z + .5F);
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			int rotate = 0;
			if (te.getFacing() == EnumFacing.NORTH) rotate = 1;
			if (te.getFacing() == EnumFacing.SOUTH) rotate = 3;
			if (te.getFacing() == EnumFacing.WEST) rotate = 2;
			GlStateManager.rotate(rotate * 90, 0, 1, 0);
			GlStateManager.translate(-.5F, -.5F, -.5F);
			ClientUtils.bindTexture("immersivepetroleum:textures/models/lubricator.png");
			base.render(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.translate(0, yOffset, 0);
			//base.renderPlunger(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.popMatrix();
		}

	}
}