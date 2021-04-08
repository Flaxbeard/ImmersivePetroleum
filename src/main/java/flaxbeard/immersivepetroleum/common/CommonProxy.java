package flaxbeard.immersivepetroleum.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import blusunrize.immersiveengineering.common.gui.GuiHandler;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.gui.CokerUnitContainer;
import flaxbeard.immersivepetroleum.common.gui.DistillationTowerContainer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class CommonProxy{
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/CommonProxy");
	
	/** Fired at {@link FMLCommonSetupEvent} */
	public void setup(){
	}
	
	public void registerContainersAndScreens(){
		GuiHandler.register(DistillationTowerTileEntity.class, new ResourceLocation(ImmersivePetroleum.MODID, "distillationtower"), DistillationTowerContainer::new);
		GuiHandler.register(CokerUnitTileEntity.class, new ResourceLocation(ImmersivePetroleum.MODID, "cokerunit"), CokerUnitContainer::new);
	}
	
	public void preInit(){
	}
	
	public void preInitEnd(){
	}
	
	public void init(){
	}
	
	public void postInit(){
	}
	
	/** Fired at {@link FMLLoadCompleteEvent} */
	public void completed(){
	}
	
	public void serverAboutToStart(){
	}
	
	public void serverStarting(){
	}
	
	public void serverStarted(){
	}
	
	public void renderTile(TileEntity te, IVertexBuilder iVertexBuilder, MatrixStack transform, IRenderTypeBuffer buffer){
	}
	
	public void handleEntitySound(SoundEvent soundEvent, Entity e, boolean active, float volume, float pitch){
	}
	
	public void drawUpperHalfSlab(MatrixStack transform, ItemStack stack){
	}
	
	public void openProjectorGui(Hand hand, ItemStack held){
	}
	
	public World getClientWorld(){
		return null;
	}
	
	public PlayerEntity getClientPlayer(){
		return null;
	}
	
	protected static ResourceLocation modLoc(String str){
		return new ResourceLocation(ImmersivePetroleum.MODID, str);
	}
}
