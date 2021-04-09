package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import flaxbeard.immersivepetroleum.client.model.ModelMotorboat;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;;

@OnlyIn(Dist.CLIENT)
public class MotorboatRenderer extends EntityRenderer<MotorboatEntity>{
	private static ResourceLocation texture = rl("textures/models/boat_motor.png");
	private static ResourceLocation textureArmor = rl("textures/models/boat_motor_armor.png");
	
	/** instance of ModelBoat for rendering */
	protected final ModelMotorboat modelBoat = new ModelMotorboat();
	
	public MotorboatRenderer(EntityRendererManager renderManagerIn){
		super(renderManagerIn);
		this.shadowSize = 0.8F;
	}
	
	@Override
	public void render(MotorboatEntity entity, float entityYaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLight){
		matrix.push();
		{
			matrix.translate(0.0D, 0.375D, 0.0D);
			this.setupRotation(entity, entityYaw, partialTicks, matrix);
			this.modelBoat.setRotationAngles(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
			
			if(entity.isInLava()){
				matrix.translate(0, -3.9F / 16F, 0);
			}
			
			{
				if(!entity.isEmergency()){
					float a = entity.getRowingTime(0, partialTicks);
					float b = entity.getRowingTime(1, partialTicks);
					
					modelBoat.propeller.rotateAngleX = (a > 0 ? b : a) * 15.0F;
				}else{
					modelBoat.propeller.rotateAngleX = 0;
				}
				
				float pr = entity.isEmergency() ? 0F : entity.propellerRotation;
				if(entity.isLeftInDown() && pr > -1)
					pr = pr - 0.1F * Minecraft.getInstance().getRenderPartialTicks();
				
				if(entity.isRightInDown() && pr < 1)
					pr = pr + 0.1F * Minecraft.getInstance().getRenderPartialTicks();
				
				if(!entity.isLeftInDown() && !entity.isRightInDown())
					pr = (float) (pr * Math.pow(0.7, Minecraft.getInstance().getRenderPartialTicks()));
				
				modelBoat.propellerAssembly.rotateAngleY = (float) Math.toRadians(pr * 15);
			}
			
			this.modelBoat.render(matrix, bufferIn.getBuffer(this.modelBoat.getRenderType(getEntityTexture(entity.isFireproof))), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			
			if(entity.hasPaddles){
				IVertexBuilder vbuilder_normal = bufferIn.getBuffer(this.modelBoat.getRenderType(texture));
				
				this.modelBoat.paddles[0].render(matrix, vbuilder_normal, packedLight, OverlayTexture.NO_OVERLAY);
				this.modelBoat.paddles[1].render(matrix, vbuilder_normal, packedLight, OverlayTexture.NO_OVERLAY);
			}
			
			IVertexBuilder vbuilder_armored = bufferIn.getBuffer(this.modelBoat.getRenderType(textureArmor));
			
			if(entity.hasIcebreaker){
				this.modelBoat.icebreak.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
			}
			
			if(entity.hasRudders){
				this.modelBoat.ruddersBase.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
				
				float pr = entity.propellerRotation;
				if(entity.isLeftInDown() && pr > -1){
					pr = pr - 0.1F * Minecraft.getInstance().getRenderPartialTicks();
				}
				
				if(entity.isRightInDown() && pr < 1){
					pr = pr + 0.1F * Minecraft.getInstance().getRenderPartialTicks();
				}
				
				if(!entity.isLeftInDown() && !entity.isRightInDown()){
					pr = (float) (pr * Math.pow(0.7F, Minecraft.getInstance().getRenderPartialTicks()));
				}
				
				this.modelBoat.rudder1.rotateAngleY = (float) Math.toRadians(pr * 20f);
				this.modelBoat.rudder2.rotateAngleY = (float) Math.toRadians(pr * 20f);
				
				this.modelBoat.rudder1.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
				this.modelBoat.rudder2.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
			}
			
			if(entity.hasTank){
				this.modelBoat.tank.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
			}
			
			if(!entity.canSwim()){
				IVertexBuilder vbuilder_mask = bufferIn.getBuffer(RenderType.getWaterMask());
				this.modelBoat.noWaterRenderer().render(matrix, vbuilder_mask, packedLight, OverlayTexture.NO_OVERLAY);
			}
		}
		matrix.pop();
		
		super.render(entity, entityYaw, partialTicks, matrix, bufferIn, packedLight);
	}
	
	@Override
	public ResourceLocation getEntityTexture(MotorboatEntity entity){
		return texture;
	}
	
	public ResourceLocation getEntityTexture(boolean armored){
		return armored ? textureArmor : texture;
	}
	
	public void setupRotation(MotorboatEntity boat, float entityYaw, float partialTicks, MatrixStack matrix){
		matrix.rotate(Vector3f.YP.rotationDegrees(180.0F - entityYaw));
		float f = (float) boat.getTimeSinceHit() - partialTicks;
		float f1 = boat.getDamageTaken() - partialTicks;
		
		if(f1 < 0.0F){
			f1 = 0.0F;
		}
		
		if(f > 0.0F){
			matrix.rotate(new Quaternion(MathHelper.sin(f) * f * f1 / 10.0F * (float) boat.getForwardDirection(), 0.0F, 0.0F, true));
		}
		
		if(boat.isBoosting){
			matrix.rotate(new Quaternion(3, 0, 0, true));
		}
		
		matrix.scale(-1.0F, -1.0F, 1.0F);
		matrix.rotate(Vector3f.YP.rotationDegrees(90.0F));
	}
	
	private static ResourceLocation rl(String str){
		return new ResourceLocation("immersivepetroleum", str);
	}
}
