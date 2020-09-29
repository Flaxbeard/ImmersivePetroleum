package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.model.ModelPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(Dist.CLIENT)
public class MultiblockPumpjackRenderer extends TileEntityRenderer<PumpjackTileEntity>{
	public static ModelPumpjack model; // Set by SpriteFetcher below.
	
	public MultiblockPumpjackRenderer(TileEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}
	
	@Override
	public void render(PumpjackTileEntity te, float partialTicks, MatrixStack transform, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn){
		if(te != null && !te.isDummy()){
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
			
			boolean fakeActive=true;
			te.activeTicks+=0.05;
			float ticks = te.activeTicks + (fakeActive ? partialTicks : 0);
//			float ticks = te.activeTicks + (te.wasActive ? partialTicks : 0);
			model.ticks = 1.5F * ticks;
			
			model.render(transform, buffer.getBuffer(RenderType.getTranslucent()), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
			transform.pop();
		}
	}
	

	@SuppressWarnings("deprecation")
	@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT, bus=Bus.MOD)
	public static class SpriteFetcher{
		static ResourceLocation arm_texture=new ResourceLocation("immersivepetroleum", "models/pumpjack_armature");
		
		@SubscribeEvent
		public static void pre(TextureStitchEvent.Pre event){
			if(event.getMap().getTextureLocation()==AtlasTexture.LOCATION_BLOCKS_TEXTURE){
				event.addSprite(arm_texture);
			}
		}
		
		@SubscribeEvent
		public static void post(TextureStitchEvent.Post event){
			if(event.getMap().getTextureLocation()==AtlasTexture.LOCATION_BLOCKS_TEXTURE){
				TextureAtlasSprite sprite=event.getMap().getSprite(arm_texture);
				event.getMap();
				MultiblockPumpjackRenderer.model=new ModelPumpjack(sprite);
			}
		}
	}
}
