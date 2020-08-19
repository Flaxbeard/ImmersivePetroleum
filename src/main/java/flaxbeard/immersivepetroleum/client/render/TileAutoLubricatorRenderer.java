package flaxbeard.immersivepetroleum.client.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.ShaderUtil;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes.Base;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorNewTileEntity;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

public class TileAutoLubricatorRenderer extends TileEntityRenderer<AutoLubricatorNewTileEntity>{
	
	private static Base base = new ModelLubricantPipes.Base();
	
	@Override
	public boolean isGlobalRenderer(AutoLubricatorNewTileEntity te){
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	@Override
	public void render(AutoLubricatorNewTileEntity te, double x, double y, double z, float partialTicks, int destroyStage){
		if(te == null){
			GlStateManager.pushMatrix();
			GlStateManager.translated(x, y, z);
			GlStateManager.enableBlend();
			GlStateManager.enableAlphaTest();
			
			GL11.glScalef(0.65F, 0.65F, 0.65F);
			GL11.glTranslatef(0.25F, -0.1F, 0.25F);
			
			ClientUtils.bindTexture("immersivepetroleum:textures/models/lubricator");
			base.render(0.0625F);
			
			GlStateManager.enableBlend();
			GlStateManager.enableAlphaTest();
			base.renderTank(0.0625F);
			
			// base.renderPlunger(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.popMatrix();
			return;
		}
		
		if(te.isSlave || !te.getWorld().isBlockPresent(te.getPos()))
			return;
		
		float height = 16;
		FluidStack fs = te.tank.getFluid();
		float level = 0;
		if(fs != null){
			level = fs.getAmount() / (float) te.tank.getCapacity();
		}
		float yOffset = (1 - level) * height * -1F / 16F;
		float scale = .0625f;
		
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		{
			GlStateManager.translated(x + .5, y + .5, z + .5);
			GlStateManager.enableBlend();
			GlStateManager.enableAlphaTest();
			RenderHelper.disableStandardItemLighting();
			
			GlStateManager.pushMatrix();
			{
				ShaderUtil.alpha_static(0.25f, 1);
				
				GlStateManager.translated(-4F / 16F, 6 / 16F, -4F / 16F);
				GlStateManager.scaled(scale, scale, scale);
				float h = level * height;
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
				
				GlStateManager.scaled(1 / scale, 1 / scale, 1 / scale);
				GlStateManager.translated(0, -1, -1);
				
				ShaderUtil.releaseShader();
			}
			GlStateManager.popMatrix();
			
			RenderHelper.enableStandardItemLighting();
			
		}
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		{
			GlStateManager.translated(x, y, z);
			GlStateManager.enableBlend();
			GlStateManager.enableAlphaTest();
			
			ClientUtils.bindTexture("immersivepetroleum:textures/models/lubricator");
			base.renderTank(0.0625F);
		}
		GlStateManager.popMatrix();
		
		GlStateManager.disableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.pushMatrix();
		{	
			GlStateManager.translated(x, y, z);
			
			BlockPos target = te.getPos().offset(te.getFacing());
			TileEntity test = te.getWorld().getTileEntity(target);
			
			ILubricationHandler<TileEntity> handler = (ILubricationHandler<TileEntity>)LubricatedHandler.getHandlerForTile(test);
			if(handler != null){
				TileEntity master = handler.isPlacedCorrectly(te.getWorld(), te, te.getFacing());
				if(master != null){
					handler.renderPipes(te.getWorld(), te, te.getFacing(), master);
				}
			}
		}
		GlStateManager.popMatrix();
		
		GlStateManager.enableAlphaTest();
		
		GlStateManager.pushMatrix();
		{
			GlStateManager.translated(x + .5F, y + .5F, z + .5F);
			
			int rotate = 0;
			if(te.getFacing() == Direction.NORTH) rotate = 1;
			if(te.getFacing() == Direction.SOUTH) rotate = 3;
			if(te.getFacing() == Direction.WEST) rotate = 2;
			GlStateManager.rotated(rotate * 90, 0, 1, 0);
			GlStateManager.translated(-.5F, -.5F, -.5F);
			ClientUtils.bindTexture("immersivepetroleum:textures/models/lubricator");
			base.render(0.0625F);
			GlStateManager.translated(0, yOffset, 0);
			// base.renderPlunger(null, 0, 0, 0, 0, 0, 0.0625F);
		}
		GlStateManager.popMatrix();
		
		GlStateManager.disableBlend();
	}
}
