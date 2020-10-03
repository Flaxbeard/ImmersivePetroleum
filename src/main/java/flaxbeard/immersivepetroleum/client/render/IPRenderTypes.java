package flaxbeard.immersivepetroleum.client.render;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderState.LineState;
import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class IPRenderTypes{
	static final ResourceLocation activeTexture=new ResourceLocation(ImmersivePetroleum.MODID, "textures/multiblock/distillation_tower_active.png");
	
	/** Intended to only be used by {@link MultiblockDistillationTowerRenderer} */
	public static final RenderType DISTILLATION_TOWER_ACTIVE;
	
	static final RenderState.TextureState ACTIVE_TOWER_TEXTURE=new RenderState.TextureState(activeTexture, false, false);
	static final RenderState.ShadeModelState SHADE_ENABLED=new RenderState.ShadeModelState(true);
	static final RenderState.LightmapState LIGHTMAP_ENABLED=new RenderState.LightmapState(true);
	static final RenderState.OverlayState OVERLAY_ENABLED=new RenderState.OverlayState(false);
	
	static {
		RenderType.State renderState=RenderType.State.getBuilder()
				.texture(ACTIVE_TOWER_TEXTURE)
				.shadeModel(SHADE_ENABLED)
				.lightmap(LIGHTMAP_ENABLED)
				.overlay(OVERLAY_ENABLED)
				.build(false);
		
		DISTILLATION_TOWER_ACTIVE = RenderType.makeType(
				ImmersivePetroleum.MODID+":customsolid",
				DefaultVertexFormats.BLOCK,
				GL11.GL_QUADS,
				256,
				true,
				false,
				renderState);
	}
}
