package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class MultiblockDistillationTowerRenderer extends TileEntityRenderer<DistillationTowerTileEntity>{
	public MultiblockDistillationTowerRenderer(TileEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}
	
	@Override
	public boolean isGlobalRenderer(DistillationTowerTileEntity te){
		return true;
	}
	
	@Override
	public void render(DistillationTowerTileEntity te, float partialTicks, MatrixStack transform, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn){
		if(te != null && te.formed && !te.isDummy()){
			if(te.shouldRenderAsActive()){
				combinedOverlayIn = OverlayTexture.NO_OVERLAY;
				
				transform.push();
				{
					Direction rotation = te.getFacing();
					switch(rotation){
						case NORTH:{
							// transform.rotate(new Quaternion(0, 0, 0, true));
							transform.translate(3, 0, 4);
							break;
						}
						case SOUTH:{
							transform.rotate(new Quaternion(0F, 180F, 0F, true));
							transform.translate(2, 0, 3);
							break;
						}
						case EAST:{
							transform.rotate(new Quaternion(0, 270F, 0, true));
							transform.translate(3, 0, 3);
							break;
						}
						case WEST:{
							transform.rotate(new Quaternion(0, 90F, 0, true));
							transform.translate(2, 0, 4);
							break;
						}
						default:
							break;
					}
					
					float br = 0.75F; // "Brightness"
					
					// Is it the most efficient way of doing this? Probably not.
					// Does it make me look smart af? hell yeah..
					IVertexBuilder buf = bufferIn.getBuffer(IPRenderTypes.DISTILLATION_TOWER_ACTIVE);
					if(te.getIsMirrored()){
						transform.push();
						{
							transform.translate(-4.0, 0.0, -4.0);
							Matrix4f mat = transform.getLast().getMatrix();
							
							// Active Boiler Front
							int ux=96, vy=134;
							int w=32, h=24;
							float uw=w/256F, vh=h/256F, u0=ux/256F, v0=vy/256F, u1=u0+uw, v1=v0+vh;
							
							buf.pos(mat, -0.0015F, 0.5F, w/16F)			.color(br,br,br, 1.0F).tex(u1, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, -0.0015F, 0.5F+h/16F, w/16F)	.color(br,br,br, 1.0F).tex(u1, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, -0.0015F, 0.5F+h/16F, 0.0F)	.color(br,br,br, 1.0F).tex(u0, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, -0.0015F, 0.5F, 0.0F)			.color(br,br,br, 1.0F).tex(u0, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							
							// Active Boiler Back
							ux=96; vy=158;
							w=32; h=24;
							uw=w/256F; vh=h/256F; u0=ux/256F; v0=vy/256F; u1=u0+uw; v1=v0+vh;
							
							buf.pos(mat, 1.0015F, 0.5F+h/16F, 0.0F)		.color(br,br,br, 1.0F).tex(u1, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 1.0015F, 0.5F+h/16F, w/16F)	.color(br,br,br, 1.0F).tex(u0, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 1.0015F, 0.5F, w/16F)			.color(br,br,br, 1.0F).tex(u0, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 1.0015F, 0.5F, 0.0F)			.color(br,br,br, 1.0F).tex(u1, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							
							// Active Boiler Side
							ux=80; vy=134;
							w=16; h=24;
							uw=w/256F; vh=h/256F; u0=ux/256F; v0=vy/256F; u1=u0+uw; v1=v0+vh;
							
							buf.pos(mat, w/16F, 0.5F, 2.0015F)			.color(br,br,br, 1.0F).tex(u1, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, w/16F, 0.5F+h/16F, 2.0015F)	.color(br,br,br, 1.0F).tex(u1, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 0.0F, 0.5F+h/16F, 2.0015F)		.color(br,br,br, 1.0F).tex(u0, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 0.0F, 0.5F, 2.0015F)			.color(br,br,br, 1.0F).tex(u0, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
						}
						transform.pop();
						
					}else{
						transform.push();
						{
							transform.translate(-2.0, 0.0, -4.0);
							Matrix4f mat = transform.getLast().getMatrix();
							
							// Active Boiler Back
							int ux=96, vy=158;
							int w=32, h=24;
							float uw=w/256F, vh=h/256F, u0=ux/256F, v0=vy/256F, u1=u0+uw, v1=v0+vh;
							
							buf.pos(mat, -0.0015F, 0.5F, w/16F)			.color(br,br,br, 1.0F).tex(u0, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, -0.0015F, 0.5F+h/16F, w/16F)	.color(br,br,br, 1.0F).tex(u0, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, -0.0015F, 0.5F+h/16F, 0.0F)	.color(br,br,br, 1.0F).tex(u1, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, -0.0015F, 0.5F, 0.0F)			.color(br,br,br, 1.0F).tex(u1, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							
							// Active Boiler Front
							ux=96; vy=134;
							w=32; h=24;
							uw=w/256F; vh=h/256F; u0=ux/256F; v0=vy/256F; u1=u0+uw; v1=v0+vh;
							
							buf.pos(mat, 1.0015F, 0.5F+h/16F, 0.0F)		.color(br,br,br, 1.0F).tex(u0, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 1.0015F, 0.5F+h/16F, w/16F)	.color(br,br,br, 1.0F).tex(u1, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 1.0015F, 0.5F, w/16F)			.color(br,br,br, 1.0F).tex(u1, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 1.0015F, 0.5F, 0.0F)			.color(br,br,br, 1.0F).tex(u0, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							
							// Active Boiler Side
							ux=80; vy=134;
							w=16; h=24;
							uw=w/256F; vh=h/256F; u0=ux/256F; v0=vy/256F; u1=u0+uw; v1=v0+vh;
							
							buf.pos(mat, w/16F, 0.5F, 2.0015F)			.color(br,br,br, 1.0F).tex(u0, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, w/16F, 0.5F+h/16F, 2.0015F)	.color(br,br,br, 1.0F).tex(u0, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 0.0F, 0.5F+h/16F, 2.0015F)		.color(br,br,br, 1.0F).tex(u1, v0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
							buf.pos(mat, 0.0F, 0.5F, 2.0015F)			.color(br,br,br, 1.0F).tex(u1, v1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(1, 1, 1).endVertex();
						}
						transform.pop();
					}
				}
				transform.pop();
			}
		}
	}
}
