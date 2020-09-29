package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import flaxbeard.immersivepetroleum.client.model.IPModels;
import flaxbeard.immersivepetroleum.client.model.ModelPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiblockPumpjackRenderer extends TileEntityRenderer<PumpjackTileEntity>{
	public MultiblockPumpjackRenderer(TileEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}
	
	private static ModelPumpjack pumpjackarm;
	
	@Override
	public void render(PumpjackTileEntity te, float partialTicks, MatrixStack transform, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn){
		if(te != null && !te.isDummy()){
			if(pumpjackarm==null)pumpjackarm=(ModelPumpjack)IPModels.getModel(ModelPumpjack.ID);
			
			transform.push();
			Direction rotation = te.getFacing();
			switch(rotation){
				case NORTH:
					transform.rotate(new Quaternion(0, 90F, 0, true));
					transform.translate(-6, 0, -1);
					break;
				case EAST:
					transform.translate(-5, 0, -1);
					break;
				case SOUTH:
					transform.rotate(new Quaternion(0, 270F, 0, true));
					transform.translate(-5, 0, -2);
					break;
				case WEST:
					transform.rotate(new Quaternion(0, 180F, 0, true));
					transform.translate(-6, 0, -2);
					break;
				default:
					break;
				
			}
			
			float ticks = te.activeTicks + (te.wasActive ? partialTicks : 0);
			pumpjackarm.ticks = 1.5F * ticks;
			
			pumpjackarm.render(transform, buffer.getBuffer(RenderType.getTranslucent()), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
			transform.pop();
		}
	}
}
