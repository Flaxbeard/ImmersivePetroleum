package flaxbeard.immersivepetroleum.common.util;

import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;

/**
 * Central place for Minecraft instance related stuff.<br>
 * <br>
 * Primarly so that the
 * <code>"Resource leak: 'unassigned Closeable value' is not closed at this
 * location"</code> warning is contained in here and not scattered all over the
 * place, if and when it apears.
 *
 * @author TwistedGate
 */
public class MCUtil{
	private static final Minecraft MC = Minecraft.getInstance();
	
	public static void bindTexture(ResourceLocation texture){
		getTextureManager().bindTexture(texture);
	}
	
	public static float getPartialTicks(){
		return MC.getTickLength();
	}
	
	public static Entity getRenderViewEntity(){
		return MC.getRenderViewEntity();
	}
	
	public static BlockColors getBlockColors(){
		return MC.getBlockColors();
	}
	
	public static Screen getScreen(){
		return MC.currentScreen;
	}
	
	public static ParticleManager getParticleEngine(){
		return MC.particles;
	}
	
	public static TextureManager getTextureManager(){
		return MC.textureManager;
	}
	
	public static BlockRendererDispatcher getBlockRenderer(){
		return MC.getBlockRendererDispatcher();
	}
	
	public static GameRenderer getGameRenderer(){
		return MC.gameRenderer;
	}
	
	public static ClientWorld getWorld(){
		return MC.world;
	}
	
	public static FontRenderer getFont(){
		return MC.fontRenderer;
	}
	
	public static ClientPlayerEntity getPlayer(){
		return MC.player;
	}
	
	public static RayTraceResult getHitResult(){
		return MC.objectMouseOver;
	}
	
	public static GameSettings getOptions(){
		return MC.gameSettings;
	}
	
	public static MainWindow getWindow(){
		return MC.getMainWindow();
	}

	public static ItemRenderer getItemRenderer(){
		return MC.getItemRenderer();
	}
}
