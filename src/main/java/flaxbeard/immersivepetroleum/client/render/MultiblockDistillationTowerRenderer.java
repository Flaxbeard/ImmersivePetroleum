package flaxbeard.immersivepetroleum.client.render;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class MultiblockDistillationTowerRenderer extends TileEntityRenderer<DistillationTowerTileEntity>{
	private static IBakedModel towerNormalInactive;
	private static IBakedModel towerNormalActive;
	private static IBakedModel towerMirroredInactive;
	private static IBakedModel towerMirroredActive;
	
	@Override
	public boolean isGlobalRenderer(DistillationTowerTileEntity te){
		return true;
	}
	
	@Override
	public void render(DistillationTowerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage){
		if(te != null && te.formed && !te.isDummy()){
			GlStateManager.pushMatrix();
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
			
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder buf = tes.getBuffer();
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			ClientUtils.renderModelTESRFast(list, buf, te.getWorldNonnull(), te.getPos(), 0xBABABA);
			
			GlStateManager.disableCull();
			GlStateManager.disableLighting();
			ClientUtils.bindAtlas();
			tes.draw();
			GlStateManager.enableLighting();
			GlStateManager.enableCull();
			
			GlStateManager.popMatrix();
		}
	}
	
	@SubscribeEvent
	public static void modelStuff(ModelBakeEvent event){
		final ResourceLocation modelRLNormalIdle = new ResourceLocation(ImmersivePetroleum.MODID, "block/obj/distillationtower_idle");
		final ResourceLocation modelRLMirroredIdle = new ResourceLocation(ImmersivePetroleum.MODID, "block/obj/distillationtower_mirrored_idle");
		final ResourceLocation modelRLNormalActive = new ResourceLocation(ImmersivePetroleum.MODID, "block/obj/distillationtower_active");
		final ResourceLocation modelRLMirroredActive = new ResourceLocation(ImmersivePetroleum.MODID, "block/obj/distillationtower_mirrored_active");
		
		final ResourceLocation dtNormalActive = new ResourceLocation(ImmersivePetroleum.MODID, "distillingtower_normal_active");
		final ResourceLocation dtNormalInactive = new ResourceLocation(ImmersivePetroleum.MODID, "distillingtower_normal_inactive");
		final ResourceLocation dtMirroredActive = new ResourceLocation(ImmersivePetroleum.MODID, "distillingtower_mirrored_active");
		final ResourceLocation dtMirroredInactive = new ResourceLocation(ImmersivePetroleum.MODID, "distillingtower_mirrored_inactive");
		
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
	}
	
	@SubscribeEvent
	public static void stitching(TextureStitchEvent.Pre event){
		if(event.getMap() != Minecraft.getInstance().getTextureMap())
			return;
		
		ImmersivePetroleum.log.info("Stitching Distillation Tower Textures");
		event.addSprite(new ResourceLocation(ImmersivePetroleum.MODID, "block/obj/distillation_tower"));
		event.addSprite(new ResourceLocation(ImmersivePetroleum.MODID, "block/obj/distillation_tower_active"));
	}
	
	static BasicState state=new BasicState(new SimpleModelState(ImmutableMap.of()), false);
	private static IBakedModel bake(ModelLoader modelLoader, IUnbakedModel unbaked){
		return unbaked.bake(
				modelLoader,
				ModelLoader.defaultTextureGetter(),
				state,
				DefaultVertexFormats.ITEM);
	}
}
