package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelSpeedboat;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
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
	public void render(SpeedboatEntity entity, float entityYaw, float partialTicks, MatrixStack transform, IRenderTypeBuffer bufferIn, int packedLightIn){
		transform.push();
		{
			transform.translate(entity.getPositionVec().x, entity.getPositionVec().y, entity.getPositionVec().z);
			
			this.setupRotation(entity, entityYaw, partialTicks, transform);
			ClientUtils.bindTexture(entity.isFireproof ? textureArmor : texture);
			if(entity.isInLava()){
				transform.translate(0, -3.9F / 16F, 0);
			}
			
			//this.modelBoat.render(entityIn, transform, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			//this.modelBoat.render(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
			
			if(entity.hasIcebreaker){
				ClientUtils.bindTexture(textureArmor);
				this.modelBoat.renderIcebreaker(0.0625F);
			}
			
			if(entity.hasRudders){
				ClientUtils.bindTexture(textureArmor);
				this.modelBoat.renderRudders(entity, 0.0625F);
			}
			
			if(entity.hasTank){
				ClientUtils.bindTexture(textureArmor);
				this.modelBoat.renderTank(0.0625F);
			}
			
			if(entity.hasPaddles){
				ClientUtils.bindTexture(texture);
				this.modelBoat.renderPaddles(entity, 0.0625F, partialTicks);
			}
		}
		transform.pop();
		
		super.render(entity, entityYaw, partialTicks, transform, bufferIn, packedLightIn);
	}
	
	public void renderMultipass(SpeedboatEntity entity, double x, double y, double z, float yaw, float partialTicks, MatrixStack transform){
		transform.push();
		{
			transform.translate(x, y + 0.375F, z);
			this.setupRotation(entity, yaw, partialTicks, transform);
			
			ClientUtils.bindTexture(texture);
			
			modelBoat.renderMultipass(0.0625F);
		}
		transform.pop();
	}
	
	@Override
	public ResourceLocation getEntityTexture(SpeedboatEntity entity){
		return new ResourceLocation(texture);
	}
	
	public void setupRotation(SpeedboatEntity boat, float yaw, float partialTicks, MatrixStack transform){
		transform.rotate(new Quaternion(0.0F, 180.0F - yaw, 0.0F, true));
		float f = (float) boat.getTimeSinceHit() - partialTicks;
		float f1 = boat.getDamageTaken() - partialTicks;
		
		if(f1 < 0.0F){
			f1 = 0.0F;
		}
		
		if(f > 0.0F){
			transform.rotate(new Quaternion(MathHelper.sin(f) * f * f1 / 10.0F * (float) boat.getForwardDirection(), 0.0F, 0.0F, true));
		}
		
		if(boat.isBoosting){
			transform.rotate(new Quaternion(3, 0, 0, true));
		}
		
		transform.scale(-1.0F, -1.0F, 1.0F);
	}
	
	public void setupTranslation(MatrixStack transform, double x, double y, double z){
		transform.translate(x, y + 0.375F, z);
	}
}
