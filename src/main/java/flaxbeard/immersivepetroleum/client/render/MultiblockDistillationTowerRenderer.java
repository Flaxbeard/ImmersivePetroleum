package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity.DistillationTowerParentTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiblockDistillationTowerRenderer extends TileEntityRenderer<DistillationTowerParentTileEntity>{
	private static ModelDistillationTower model = new ModelDistillationTower(false);
	private static ModelDistillationTower modelM = new ModelDistillationTower(true);
	
	private static String texture = "immersivepetroleum:textures/models/distillation_tower.png";
	private static String textureOn = "immersivepetroleum:textures/models/furnace_hot.png";
	private static String textureM = "immersivepetroleum:textures/models/distillation_tower_m.png";
	private static String textureOnM = "immersivepetroleum:textures/models/furnace_hot_m.png";
	
	@Override
	public boolean isGlobalRenderer(DistillationTowerParentTileEntity te){
		return true;
	}
	
	@Override
	public void render(DistillationTowerParentTileEntity te, double x, double y, double z, float partialTicks, int destroyStage){
		if(te != null){
			GlStateManager.pushMatrix();
			GlStateManager.translated(x, y, z);
			
			Direction rotation = te.getFacing();
			switch(rotation){
				case NORTH:{
					GlStateManager.rotatef(180F, 0, 1, 0);
					GlStateManager.translated(-1, 0, -4);
					break;
				}
				case EAST:{
					GlStateManager.rotatef(90F, 0, 1, 0);
					GlStateManager.translated(-1, 0, -3);
					break;
				}
				case SOUTH:{
					GlStateManager.translated(0, 0, -3);
					break;
				}
				case WEST:{
					GlStateManager.rotatef(270F, 0, 1, 0);
					GlStateManager.translated(0, 0, -4);
					break;
				}
				default:
					break;
			}
			
			if(te.getIsMirrored()){
				GlStateManager.rotatef(180F, 0, 1, 0);
				GlStateManager.translated(-1, 0, -4);
			}
			
			GlStateManager.translatef(-3, 0, 0);
			
			if(te.getIsMirrored()){
				ClientUtils.bindTexture(textureM);
				modelM.renderMain(0.0625F);
				ClientUtils.bindTexture(te.shouldRenderAsActive() ? textureOnM : textureM);
				modelM.renderFurnace(0.0625F);
			}else{
				ClientUtils.bindTexture(texture);
				model.renderMain(0.0625F);
				ClientUtils.bindTexture(te.shouldRenderAsActive() ? textureOn : texture);
				model.renderFurnace(0.0625F);
			}
			
			GlStateManager.popMatrix();
		}
	}
}
