package flaxbeard.immersivepetroleum.client.render;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
	
	@Override
	public boolean isGlobalRenderer(DistillationTowerTileEntity te){
		return true;
	}
	
	@Override
	public void render(DistillationTowerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage){
		if(te != null && te.formed && !te.isDummy()){
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder buf = tes.getBuffer();
			
			if(te.shouldRenderAsActive()){
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
					
					ClientUtils.bindTexture(activeTexture);
					GlStateManager.enableCull();
					GlStateManager.disableLighting();
					
					int a = te.getWorldNonnull().getCombinedLight(te.getPos(), 0);
					int b = a >> 16 & 0xFFFF;
					int c = a & 0xFFFF;
					
					float br=0.75F; // "Brightness"
					
					// Is it the most efficient way of doing this? Probably not.
					// Does it make me look smart af? hell yeah..
					if(te.getIsMirrored()){
						GlStateManager.pushMatrix();
						{
							GlStateManager.translated(-6.0, 0.0, -2.0);
							
							buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
							
							// Active Boiler Front
							int ux=96, vy=134;
							int w=32, h=24;
							double uw=w/256D, vh=h/256D, u0=ux/256D, v0=vy/256D, u1=u0+uw, v1=v0+vh;
							
							buf.pos(-0.0015, 0.5, w/16D).color(br,br,br, 1.0F)		.tex(u1, v1).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5+h/16D, w/16D).color(br,br,br, 1.0F).tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5+h/16D, 0.0).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5, 0.0).color(br,br,br, 1.0F)		.tex(u0, v1).lightmap(b, c).endVertex();
							
							// Active Boiler Back
							ux=96; vy=158;
							w=32; h=24;
							uw=w/256D; vh=h/256D; u0=ux/256D; v0=vy/256D; u1=u0+uw; v1=v0+vh;
							
							buf.pos(1.0015, 0.5+h/16D, 0.0).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5+h/16D, w/16D).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5, w/16D).color(br,br,br, 1.0F)		.tex(u0, v1).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5, 0.0).color(br,br,br, 1.0F)			.tex(u1, v1).lightmap(b, c).endVertex();
							
							// Active Boiler Side
							ux=80; vy=134;
							w=16; h=24;
							uw=w/256D; vh=h/256D; u0=ux/256D; v0=vy/256D; u1=u0+uw; v1=v0+vh;
							
							buf.pos(w/16D, 0.5, 2.0015).color(br,br,br, 1.0F)		.tex(u1, v1).lightmap(b, c).endVertex();
							buf.pos(w/16D, 0.5+h/16D, 2.0015).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(0.0, 0.5+h/16D, 2.0015).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(0.0, 0.5, 2.0015).color(br,br,br, 1.0F)			.tex(u0, v1).lightmap(b, c).endVertex();
							
							tes.draw();
						}
						GlStateManager.popMatrix();
					}else{
						GlStateManager.pushMatrix();
						{
							GlStateManager.translated(0.0, 0.0, -2.0);
							
							buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
							
							// Active Boiler Back
							int ux=96, vy=158;
							int w=32, h=24;
							double uw=w/256D, vh=h/256D, u0=ux/256D, v0=vy/256D, u1=u0+uw, v1=v0+vh;
							
							buf.pos(-0.0015, 0.5, w/16D).color(br,br,br, 1.0F)		.tex(u0, v1).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5+h/16D, w/16D).color(br,br,br, 1.0F).tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5+h/16D, 0.0).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(-0.0015, 0.5, 0.0).color(br,br,br, 1.0F)		.tex(u1, v1).lightmap(b, c).endVertex();
							
							// Active Boiler Front
							ux=96; vy=134;
							w=32; h=24;
							uw=w/256D; vh=h/256D; u0=ux/256D; v0=vy/256D; u1=u0+uw; v1=v0+vh;
							
							buf.pos(1.0015, 0.5+h/16D, 0.0).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5+h/16D, w/16D).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5, w/16D).color(br,br,br, 1.0F)		.tex(u1, v1).lightmap(b, c).endVertex();
							buf.pos(1.0015, 0.5, 0.0).color(br,br,br, 1.0F)			.tex(u0, v1).lightmap(b, c).endVertex();
							
							// Active Boiler Side
							ux=80; vy=134;
							w=16; h=24;
							uw=w/256D; vh=h/256D; u0=ux/256D; v0=vy/256D; u1=u0+uw; v1=v0+vh;
							
							buf.pos(w/16D, 0.5, 2.0015).color(br,br,br, 1.0F)		.tex(u0, v1).lightmap(b, c).endVertex();
							buf.pos(w/16D, 0.5+h/16D, 2.0015).color(br,br,br, 1.0F)	.tex(u0, v0).lightmap(b, c).endVertex();
							buf.pos(0.0, 0.5+h/16D, 2.0015).color(br,br,br, 1.0F)	.tex(u1, v0).lightmap(b, c).endVertex();
							buf.pos(0.0, 0.5, 2.0015).color(br,br,br, 1.0F)			.tex(u1, v1).lightmap(b, c).endVertex();
							
							tes.draw();
						}
						GlStateManager.popMatrix();
					}
					
					GlStateManager.disableCull();
					GlStateManager.enableLighting();
				}
				GlStateManager.popMatrix();
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
	
	@SubscribeEvent
	public static void modelStuff(ModelBakeEvent event){
		/*
		final ResourceLocation modelRLNormalIdle = new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillationtower_idle");
		final ResourceLocation modelRLNormalActive = new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillationtower_active");
		final ResourceLocation modelRLMirroredIdle = new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillationtower_mirrored_idle");
		final ResourceLocation modelRLMirroredActive = new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillationtower_mirrored_active");
		
		final ResourceLocation dtNormalActive = new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillingtower_normal_active");
		final ResourceLocation dtNormalInactive = new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillingtower_normal_inactive");
		final ResourceLocation dtMirroredActive = new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillingtower_mirrored_active");
		final ResourceLocation dtMirroredInactive = new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillingtower_mirrored_inactive");
		
		ModelLoader loader = event.getModelLoader();
		
		IUnbakedModel unbakedDTNormalIdle = loader.getUnbakedModel(modelRLNormalIdle);
		IUnbakedModel unbakedDTNormalActive = loader.getUnbakedModel(modelRLNormalActive);
		IUnbakedModel unbakedDTMirroredIdle = loader.getUnbakedModel(modelRLMirroredIdle);
		IUnbakedModel unbakedDTMirroredActive = loader.getUnbakedModel(modelRLMirroredActive);
		
		IBakedModel modelNormalInactive = bake(loader, unbakedDTNormalIdle);
		IBakedModel modelNormalActive = bake(loader, unbakedDTNormalActive);
		IBakedModel modelMirroredInactive = bake(loader, unbakedDTMirroredIdle);
		IBakedModel modelMirroredActive = bake(loader, unbakedDTMirroredActive);
		
		event.getModelRegistry().put(dtNormalInactive, modelNormalInactive);
		event.getModelRegistry().put(dtNormalActive, modelNormalActive);
		event.getModelRegistry().put(dtMirroredInactive, modelMirroredInactive);
		event.getModelRegistry().put(dtMirroredActive, modelMirroredActive);
		
		towerNormalInactive = modelNormalInactive;
		towerNormalActive = modelNormalActive;
		towerMirroredInactive = modelMirroredInactive;
		towerMirroredActive = modelMirroredActive;
		//*/
	}
	
	@SubscribeEvent
	public static void stitching(TextureStitchEvent.Pre event){
		if(event.getMap() != Minecraft.getInstance().getTextureMap())
			return;
		
		//ImmersivePetroleum.log.info("Stitching Distillation Tower Textures");
		//event.addSprite(new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillation_tower"));
		//event.addSprite(new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillation_tower_active"));
	}
	
	static BasicState state=new BasicState(new SimpleModelState(ImmutableMap.of()), false);
	@SuppressWarnings("unused")
	private static IBakedModel bake(ModelLoader modelLoader, IUnbakedModel unbaked){
		return unbaked.bake(
				modelLoader,
				ModelLoader.defaultTextureGetter(),
				state,
				DefaultVertexFormats.ITEM);
	}
}
