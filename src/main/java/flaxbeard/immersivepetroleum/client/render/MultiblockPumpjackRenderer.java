package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity.PumpjackParentTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiblockPumpjackRenderer extends TileEntityRenderer<PumpjackParentTileEntity>{
	private static ModelPumpjack model = new ModelPumpjack(false);
	private static ModelPumpjack modelM = new ModelPumpjack(true);
	
	private static String texture = "immersivepetroleum:textures/models/pumpjack.png";
	
	@Override
	public void render(PumpjackParentTileEntity te, double x, double y, double z, float partialTicks, int destroyStage){
		if(te != null){
			GlStateManager.pushMatrix();
			GlStateManager.translated(x, y - 1, z);
			
			Direction rotation = te.getFacing();
			switch(rotation){
				case NORTH:
					GlStateManager.rotated(90F, 0, 1, 0);
					GlStateManager.translated(-1, 0, 0);
					break;
				case EAST:
					break;
				case SOUTH:
					GlStateManager.rotated(270F, 0, 1, 0);
					GlStateManager.translated(0, 0, -1);
					break;
				case WEST:
					GlStateManager.rotated(180F, 0, 1, 0);
					GlStateManager.translated(-1, 0, -1);
					break;
				default:
					break;
				
			}
			GlStateManager.translated(-1, 0, -1);
			
			ClientUtils.bindTexture(texture);
			
			float ticks = te.activeTicks + (te.wasActive ? partialTicks : 0);;
			model.ticks = modelM.ticks = 1.5F * ticks;
			
			if(te.getIsMirrored()){
				modelM.render(0.0625F);
			}else{
				model.render(0.0625F);
			}
			GlStateManager.popMatrix();
		}
	}
}
