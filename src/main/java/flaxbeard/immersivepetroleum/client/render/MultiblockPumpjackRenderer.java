package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiblockPumpjackRenderer extends TileEntityRenderer<PumpjackTileEntity>{
	private static ModelPumpjack model = new ModelPumpjack();
	
	private static String texture = "immersivepetroleum:textures/models/pumpjack_armature.png";
	
	public MultiblockPumpjackRenderer(TileEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}
	
	@Override
	public void render(PumpjackTileEntity te, float partialTicks, MatrixStack transform, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn){
		if(te != null && !te.isDummy()){
			transform.push();
			transform.translate(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
			
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
			model.ticks = 1.5F * ticks;
			
			ClientUtils.bindTexture(texture);
			model.render(0.0625F);
			transform.pop();
		}
	}
}
