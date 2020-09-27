package flaxbeard.immersivepetroleum.client.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class MultiblockDistillationTowerRenderer extends TileEntityRenderer<DistillationTowerTileEntity>{
	/*private static IBakedModel towerNormalInactive;
	private static IBakedModel towerNormalActive;
	private static IBakedModel towerMirroredInactive;
	private static IBakedModel towerMirroredActive;*/
	
	private static String activeTexture="immersivepetroleum:textures/multiblock/distillation_tower_active.png";
	
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
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder buf = tes.getBuffer();
			
			if(te.shouldRenderAsActive()){
				transform.push();
				{
					transform.translate(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
					
					Direction rotation = te.getFacing();
					switch(rotation){
						case NORTH:{
//							transform.rotate(new Quaternion(0, 0, 0, true));
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
					
					ClientUtils.bindTexture(activeTexture);
					
					// TODO Fix lighting?
					int a = -1;//te.getWorldNonnull().getCombinedLight(te.getPos(), 0);
					int b = a >> 16 & 0xFFFF;
					int c = a & 0xFFFF;
					
					float br=0.75F; // "Brightness"
					
					// Is it the most efficient way of doing this? Probably not.
					// Does it make me look smart af? hell yeah..
					if(te.getIsMirrored()){
						transform.push();
						{
							transform.translate(-6.0, 0.0, -2.0);
							
							buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
							
							// Active Boiler Front
							int ux=96, vy=134;
							int w=32, h=24;
							float uw=w/256F, vh=h/256F, u0=ux/256F, v0=vy/256F, u1=u0+uw, v1=v0+vh;
							
							buf.pos(-0.0015, 0.5, w/16D).color(br,br,br, 1.0F)		.tex(u1, v1).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5+h/16D, w/16D).color(br,br,br, 1.0F).tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5+h/16D, 0.0).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5, 0.0).color(br,br,br, 1.0F)		.tex(u0, v1).lightmap(b, c).endVertex();
							
							// Active Boiler Back
							ux=96; vy=158;
							w=32; h=24;
							uw=w/256F; vh=h/256F; u0=ux/256F; v0=vy/256F; u1=u0+uw; v1=v0+vh;
							
							buf.pos(1.0015, 0.5+h/16D, 0.0).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5+h/16D, w/16D).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5, w/16D).color(br,br,br, 1.0F)		.tex(u0, v1).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5, 0.0).color(br,br,br, 1.0F)			.tex(u1, v1).lightmap(b, c).endVertex();
							
							// Active Boiler Side
							ux=80; vy=134;
							w=16; h=24;
							uw=w/256F; vh=h/256F; u0=ux/256F; v0=vy/256F; u1=u0+uw; v1=v0+vh;
							
							buf.pos(w/16D, 0.5, 2.0015).color(br,br,br, 1.0F)		.tex(u1, v1).lightmap(b, c).endVertex();
							buf.pos(w/16D, 0.5+h/16D, 2.0015).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(0.0, 0.5+h/16D, 2.0015).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(0.0, 0.5, 2.0015).color(br,br,br, 1.0F)			.tex(u0, v1).lightmap(b, c).endVertex();
							
							tes.draw();
						}
						transform.pop();
					}else{
						transform.push();
						{
							transform.translate(0.0, 0.0, -2.0);
							
							buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
							
							// Active Boiler Back
							int ux=96, vy=158;
							int w=32, h=24;
							float uw=w/256F, vh=h/256F, u0=ux/256F, v0=vy/256F, u1=u0+uw, v1=v0+vh;
							
							buf.pos(-0.0015, 0.5, w/16D).color(br,br,br, 1.0F)		.tex(u0, v1).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5+h/16D, w/16D).color(br,br,br, 1.0F).tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5+h/16D, 0.0).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5, 0.0).color(br,br,br, 1.0F)		.tex(u1, v1).lightmap(b, c).endVertex();
							
							// Active Boiler Front
							ux=96; vy=134;
							w=32; h=24;
							uw=w/256F; vh=h/256F; u0=ux/256F; v0=vy/256F; u1=u0+uw; v1=v0+vh;
							
							buf.pos(1.0015, 0.5+h/16D, 0.0).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5+h/16D, w/16D).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5, w/16D).color(br,br,br, 1.0F)		.tex(u1, v1).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5, 0.0).color(br,br,br, 1.0F)			.tex(u0, v1).lightmap(b, c).endVertex();
							
							// Active Boiler Side
							ux=80; vy=134;
							w=16; h=24;
							uw=w/256F; vh=h/256F; u0=ux/256F; v0=vy/256F; u1=u0+uw; v1=v0+vh;
							
							buf.pos(w/16D, 0.5, 2.0015).color(br,br,br, 1.0F)		.tex(u0, v1).lightmap(b, c).endVertex();
							buf.pos(w/16D, 0.5+h/16D, 2.0015).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(0.0, 0.5+h/16D, 2.0015).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(0.0, 0.5, 2.0015).color(br,br,br, 1.0F)			.tex(u1, v1).lightmap(b, c).endVertex();
							
							tes.draw();
						}
						transform.pop();
					}
				}
				transform.pop();
			}
			
			// TODO Might reuse the stuff below
			/*
			if(towerNormalInactive==null || towerNormalActive==null || towerMirroredInactive==null || towerMirroredActive==null)
				return;
			
			GlStateManager.pushMatrix();
			{
				GlStateManager.translated(x, y, z);
				
				Direction rotation = te.getFacing();
				switch(rotation){
					case NORTH:{
						GlStateManager.rotatef(0F, 0, 1, 0);
						GlStateManager.translated(3, 0, 4);
						break;
					}
					case SOUTH:{
						GlStateManager.rotatef(180F, 0, 1, 0);
						GlStateManager.translated(2, 0, 3);
						break;
					}
					case EAST:{
						GlStateManager.rotatef(270F, 0, 1, 0);
						GlStateManager.translated(3, 0, 3);
						break;
					}
					case WEST:{
						GlStateManager.rotatef(90F, 0, 1, 0);
						GlStateManager.translated(2, 0, 4);
						break;
					}
					default:
						break;
				}
				
				if(te.getIsMirrored()){
					GlStateManager.translated(-3, 0, 0);
				}
				GlStateManager.translatef(-3, 0, 0);
				
				BlockState state = te.getWorldNonnull().getBlockState(te.getPos());
				List<BakedQuad> list;
				if(te.getIsMirrored()){
					if(te.shouldRenderAsActive()){
						list = towerMirroredActive.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
					}else{
						list = towerMirroredInactive.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
					}
				}else{
					if(te.shouldRenderAsActive()){
						list = towerNormalActive.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
					}else{
						list = towerNormalInactive.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
					}
				}
				
				if(list==null || list.isEmpty())
					return;
				
				buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				ClientUtils.renderModelTESRFast(list, buf, te.getWorldNonnull(), te.getPos(), 0xBABABA);
				
				GlStateManager.disableCull();
				GlStateManager.disableLighting();
				ClientUtils.bindAtlas();
				tes.draw();
				GlStateManager.enableLighting();
				GlStateManager.enableCull();
			}
			GlStateManager.popMatrix();
			*/
		}
	}
}
