package flaxbeard.immersivepetroleum.client.render;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderState.LineState;
import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class IPRenderTypes{
	static final ResourceLocation activeTexture = new ResourceLocation(ImmersivePetroleum.MODID, "textures/multiblock/distillation_tower_active.png");
	
	/**
	 * Intended to only be used by {@link MultiblockDistillationTowerRenderer}
	 */
	public static final RenderType DISTILLATION_TOWER_ACTIVE;
	public static final RenderType TRANSLUCENT_LINES;
	
	static final RenderState.TextureState TEXTURE_ACTIVE_TOWER = new RenderState.TextureState(activeTexture, false, false);
	static final RenderState.ShadeModelState SHADE_ENABLED = new RenderState.ShadeModelState(true);
	static final RenderState.LightmapState LIGHTMAP_ENABLED = new RenderState.LightmapState(true);
	static final RenderState.OverlayState OVERLAY_ENABLED = new RenderState.OverlayState(true);
	static final RenderState.OverlayState OVERLAY_DISABLED = new RenderState.OverlayState(false);
	static final RenderState.DepthTestState DEPTH_ALWAYS = new RenderState.DepthTestState("always", GL11.GL_ALWAYS);
	static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = new RenderState.TransparencyState("translucent_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}, RenderSystem::disableBlend);
	static final RenderState.TransparencyState NO_TRANSPARENCY = new RenderState.TransparencyState("no_transparency", () -> {
		RenderSystem.disableBlend();
	}, () -> {
	});
	static final RenderState.DiffuseLightingState DIFFUSE_LIGHTING_ENABLED = new RenderState.DiffuseLightingState(true);
	
	static{
		TRANSLUCENT_LINES = RenderType.makeType(
				ImmersivePetroleum.MODID+":translucent_lines",
				DefaultVertexFormats.POSITION_COLOR,
				GL11.GL_LINES,
				256,
				RenderType.State.getBuilder().transparency(TRANSLUCENT_TRANSPARENCY)
					.line(new LineState(OptionalDouble.of(3.5)))
					.texture(new TextureState())
					.depthTest(DEPTH_ALWAYS)
					.build(false)
		);
		
		DISTILLATION_TOWER_ACTIVE = RenderType.makeType(
				ImmersivePetroleum.MODID+":distillation_tower_active",
				DefaultVertexFormats.BLOCK,
				GL11.GL_QUADS,
				256,
				true,
				false,
				RenderType.State.getBuilder()
					.texture(TEXTURE_ACTIVE_TOWER)
					.shadeModel(SHADE_ENABLED)
					.lightmap(LIGHTMAP_ENABLED)
					.overlay(OVERLAY_DISABLED)
					.build(false)
		);
	}
	
	/** Same as vanilla, just without an overlay */
	public static RenderType getEntitySolid(ResourceLocation locationIn){
		RenderType.State renderState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(locationIn, false, false))
				.transparency(NO_TRANSPARENCY)
				.diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
				.lightmap(LIGHTMAP_ENABLED)
				.overlay(OVERLAY_DISABLED)
				.build(true);
		return RenderType.makeType("entity_solid", DefaultVertexFormats.ENTITY, 7, 256, true, false, renderState);
	}
	
	public static IRenderTypeBuffer disableLighting(IRenderTypeBuffer in){
		return type -> {
			@SuppressWarnings("deprecation")
			RenderType rt = new RenderType(
					ImmersivePetroleum.MODID + ":" + type + "_no_lighting",
					type.getVertexFormat(),
					type.getDrawMode(),
					type.getBufferSize(),
					type.isUseDelegate(),
					false,
					() -> {
						type.setupRenderState();
						
						RenderSystem.disableLighting();
					}, () -> {
						type.clearRenderState();
					}){};
			return in.getBuffer(rt);
		};
	}
}
