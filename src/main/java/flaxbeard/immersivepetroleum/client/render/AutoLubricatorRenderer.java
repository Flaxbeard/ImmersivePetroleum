package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

public class AutoLubricatorRenderer extends TileEntityRenderer<AutoLubricatorTileEntity>{
	
	@Override
	public boolean isGlobalRenderer(AutoLubricatorTileEntity te){
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	@Override
	public void render(AutoLubricatorTileEntity te, double x, double y, double z, float partialTicks, int destroyStage){
		if(te == null){
			return;
		}
		
		if(te.isSlave)
			return;
		
		float height = 16;
		FluidStack fs = te.tank.getFluid();
		float level = 0;
		if(fs != null && !fs.isEmpty()){
			level = fs.getAmount() / (float) te.tank.getCapacity();
		}
		float scale = 0.0625f;
		
		if(level>0){
			GlStateManager.pushMatrix();
			{
				GlStateManager.translated(x + .5, y + .5, z + .5);
				GlStateManager.translated(-0.25F, 0.375F, -0.25F);
				GlStateManager.scaled(scale, scale, scale);
				
				ClientUtils.bindAtlas();
				
//				GlStateManager.depthMask(false);
//				GlStateManager.disableCull();
//				GlStateManager.enableBlend();
//				GlStateManager.blendFuncSeparate(770, 771, 1, 0);
//				ShaderUtil.alpha_static(0.25f, 1);
				
				float h = height * level;
				ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 8, h);
				GlStateManager.rotated(90, 0, 1, 0);
				GlStateManager.translated(-7.98, 0, 0);
				ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 8, h);
				GlStateManager.rotated(90, 0, 1, 0);
				GlStateManager.translated(-7.98, 0, 0);
				ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 8, h);
				GlStateManager.rotated(90, 0, 1, 0);
				GlStateManager.translated(-7.98, 0, 0);
				ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 8, h);
				GlStateManager.rotated(90, 1, 0, 0);
				GlStateManager.translated(0, 0, -h);
				ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 8, 8);
				GlStateManager.rotated(180, 1, 0, 0);
				GlStateManager.translated(0, -9, -h);
				ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 8, 8);
				
//				ShaderUtil.releaseShader();
//				GlStateManager.disableBlend();
//				GlStateManager.enableCull();
//				GlStateManager.depthMask(true);
			}
			GlStateManager.popMatrix();
		}
		
		GlStateManager.pushMatrix();
		{
			GlStateManager.translated(x, y, z);
			
			BlockPos target = te.getPos().offset(te.getFacing());
			TileEntity test = te.getWorld().getTileEntity(target);
			
			ILubricationHandler<TileEntity> handler = (ILubricationHandler<TileEntity>) LubricatedHandler.getHandlerForTile(test);
			if(handler != null){
				TileEntity master = handler.isPlacedCorrectly(te.getWorld(), te, te.getFacing());
				if(master != null){
					handler.renderPipes(te.getWorld(), te, te.getFacing(), master);
				}
			}
		}
		GlStateManager.popMatrix();
		
		/*
		GlStateManager.pushMatrix();
		{
			GlStateManager.disableAlphaTest();
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			
			GlStateManager.translated(x, y, z);
			ClientUtils.bindTexture(lubeTexture);
			base.renderTank(0.0625F);
			
			GlStateManager.disableBlend();
			GlStateManager.enableAlphaTest();
		}
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		{
			int rotate;
			switch(te.getFacing()){
				case NORTH: rotate = 1; break;
				case SOUTH: rotate = 3; break;
				case WEST:  rotate = 2; break;
				default:    rotate = 0; break;
			}
			
			GlStateManager.enableTexture();
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			
			GlStateManager.translated(x + .5F, y + .5F, z + .5F);
			GlStateManager.rotated(rotate * 90, 0, 1, 0);
			GlStateManager.translated(-.5F, -.5F, -.5F);
			
			ClientUtils.bindTexture(lubeTexture);
			base.render(0.0625F);
			GlStateManager.translated(0, yOffset, 0);
			// base.renderPlunger(0.0625F);
			
			GlStateManager.disableBlend();
		}
		GlStateManager.popMatrix();//*/
	}
}
