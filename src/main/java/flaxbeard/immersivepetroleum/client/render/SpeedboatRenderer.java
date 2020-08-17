package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelSpeedboat;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;;


@OnlyIn(Dist.CLIENT)
public class SpeedboatRenderer extends EntityRenderer<SpeedboatEntity>{
	private static String texture = "immersivepetroleum:textures/models/boat_motor.png";
	private static String textureArmor = "immersivepetroleum:textures/models/boat_motor_armor.png";
	
	/** instance of ModelBoat for rendering */
	protected ModelSpeedboat modelBoat = new ModelSpeedboat();
	
	public SpeedboatRenderer(EntityRendererManager renderManagerIn){
		super(renderManagerIn);
		this.shadowSize = 0.5F;
	}
	
	@Override
	public void doRender(SpeedboatEntity entity, double x, double y, double z, float entityYaw, float partialTicks){
		
		GlStateManager.pushMatrix();
		this.setupTranslation(x, y, z);
		
		this.setupRotation(entity, entityYaw, partialTicks);
		ClientUtils.bindTexture(entity.isFireproof ? textureArmor : texture);
		if(entity.isInLava()){
			GlStateManager.translatef(0, -3.9F / 16F, 0);
		}
		
		if(this.renderOutlines){
			GlStateManager.enableColorMaterial();
			//GlStateManager.enableOutlineMode(this.getTeamColor(entity));
		}
		
		this.modelBoat.render(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		
		if(entity.hasIcebreaker){
			ClientUtils.bindTexture(textureArmor);
			this.modelBoat.renderIcebreaker(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		}
		
		if(entity.hasRudders){
			ClientUtils.bindTexture(textureArmor);
			this.modelBoat.renderRudders(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		}
		
		if(entity.hasTank){
			ClientUtils.bindTexture(textureArmor);
			this.modelBoat.renderTank(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		}
		
		if(entity.hasPaddles){
			ClientUtils.bindTexture(texture);
			this.modelBoat.renderPaddles(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		}
		
		if(this.renderOutlines){
			//GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}
		
		GlStateManager.popMatrix();
		
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	public void setupRotation(SpeedboatEntity boat, float p_188311_2_, float p_188311_3_){
		GlStateManager.rotatef(180.0F - p_188311_2_, 0.0F, 1.0F, 0.0F);
		float f = (float) boat.getTimeSinceHit() - p_188311_3_;
		float f1 = boat.getDamageTaken() - p_188311_3_;
		
		if(f1 < 0.0F){
			f1 = 0.0F;
		}
		
		if(f > 0.0F){
			GlStateManager.rotatef(MathHelper.sin(f) * f * f1 / 10.0F * (float) boat.getForwardDirection(), 1.0F, 0.0F, 0.0F);
		}
		
		if(boat.isBoosting){
			GlStateManager.rotatef(3, 1, 0, 0);
		}
		
		GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
	}
	
	public void setupTranslation(double p_188309_1_, double p_188309_3_, double p_188309_5_){
		GlStateManager.translatef((float) p_188309_1_, (float) p_188309_3_ + 0.375F, (float) p_188309_5_);
	}
	
	public boolean isMultipass(){
		return true;
	}
	
	public void renderMultipass(SpeedboatEntity p_188300_1_, double p_188300_2_, double p_188300_4_, double p_188300_6_, float p_188300_8_, float p_188300_9_){
		GlStateManager.pushMatrix();
		this.setupTranslation(p_188300_2_, p_188300_4_, p_188300_6_);
		this.setupRotation(p_188300_1_, p_188300_8_, p_188300_9_);
		
		ClientUtils.bindTexture(texture);
		
		modelBoat.renderMultipass(p_188300_1_, p_188300_9_, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(SpeedboatEntity entity){
		return new ResourceLocation(texture);
	}
}