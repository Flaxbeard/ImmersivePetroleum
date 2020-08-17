package flaxbeard.immersivepetroleum.client.model;

import com.mojang.blaze3d.platform.GlStateManager;

import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelSpeedboat extends Model{
	public RendererModel[] boatSides = new RendererModel[5];
	/** Part of the model rendered to make it seem like there's no water in the boat */
	public RendererModel noWater;
	public RendererModel motor;
	public RendererModel propeller;
	public RendererModel propellerAssembly;
	public RendererModel icebreak;
	public RendererModel coreSampleBoat;
	public RendererModel coreSampleBoatDrill;
	public RendererModel tank;
	public RendererModel rudder1;
	public RendererModel rudder2;
	public RendererModel ruddersBase;
	public RendererModel[] paddles = new RendererModel[2];

	public ModelSpeedboat()
	{
		this.boatSides[0] = (new RendererModel(this, 0, 0)).setTextureSize(128, 64);
		this.boatSides[1] = (new RendererModel(this, 0, 19)).setTextureSize(128, 64);
		this.boatSides[2] = (new RendererModel(this, 0, 27)).setTextureSize(128, 64);
		this.boatSides[3] = (new RendererModel(this, 0, 35)).setTextureSize(128, 64);
		this.boatSides[4] = (new RendererModel(this, 0, 43)).setTextureSize(128, 64);
		this.boatSides[0].addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
		this.boatSides[0].setRotationPoint(0.0F, 3.0F, 1.0F);
		this.boatSides[1].addBox(-13.0F, -7.0F, -1.0F, 18, 6, 2, 0.0F);
		this.boatSides[1].setRotationPoint(-15.0F, 4.0F, 4.0F);
		this.boatSides[2].addBox(-8.0F, -7.0F, -1.0F, 16, 6, 2, 0.0F);
		this.boatSides[2].setRotationPoint(15.0F, 4.0F, 0.0F);
		this.boatSides[3].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
		this.boatSides[3].setRotationPoint(0.0F, 4.0F, -9.0F);
		this.boatSides[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
		this.boatSides[4].setRotationPoint(0.0F, 4.0F, 9.0F);
		this.boatSides[0].rotateAngleX = ((float) Math.PI / 2F);
		this.boatSides[1].rotateAngleY = ((float) Math.PI * 3F / 2F);
		this.boatSides[2].rotateAngleY = ((float) Math.PI / 2F);
		this.boatSides[3].rotateAngleY = (float) Math.PI;

		this.noWater = (new RendererModel(this, 0, 0)).setTextureSize(128, 64);
		this.noWater.addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
		this.noWater.setRotationPoint(0.0F, -3.0F, 1.0F);
		this.noWater.rotateAngleX = ((float) Math.PI / 2F);
		refresh();

		this.paddles[0] = this.makePaddle(true);
		this.paddles[0].setRotationPoint(3.0F, -5.0F, 9.0F);
		this.paddles[1] = this.makePaddle(false);
		this.paddles[1].setRotationPoint(3.0F, -5.0F, -9.0F);
		this.paddles[1].rotateAngleY = (float) Math.PI;
		this.paddles[0].rotateAngleZ = 0.19634955F;
		this.paddles[1].rotateAngleZ = 0.19634955F;
	}


	public void refresh()
	{

		motor = new RendererModel(this, 104, 0).setTextureSize(128, 64);
		motor.addBox(-19.0F, -8.0F, -3, 6, 5, 6, 0.0F);

		propellerAssembly = new RendererModel(this, 96, 0).setTextureSize(128, 64);
		propellerAssembly.setRotationPoint(-17F, 5F, 0);

		propellerAssembly.addBox(-1, -8.1F, -1, 2, 10, 2, 0.0F);

		RendererModel handle = new RendererModel(this, 72, 0).setTextureSize(128, 64);
		handle.addBox(4F, -9.7F, -0.5F, 6, 1, 1);
		handle.rotateAngleX = 0;
		handle.rotateAngleZ = (float) Math.toRadians(-5);
		propellerAssembly.addChild(handle);


		propeller = new RendererModel(this, 86, 0).setTextureSize(128, 64);
		propeller.addBox(-1F, -1F, -1F, 3, 2, 2, 0.0F);
		propeller.setRotationPoint(-3F, 0, 0);
		propellerAssembly.addChild(propeller);

		RendererModel propeller1 = new RendererModel(this, 90, 4).setTextureSize(128, 64);
		propeller1.addBox(0F, 0F, -1F, 1, 4, 2, 0.0F);
		propeller1.rotateAngleY = (float) Math.toRadians(15);
		propeller.addChild(propeller1);


		RendererModel propeller2B = new RendererModel(this, 90, 4).setTextureSize(128, 64);
		propeller.addChild(propeller2B);
		propeller2B.rotateAngleX = (float) Math.toRadians(360F / 3F);

		RendererModel propeller2 = new RendererModel(this, 90, 4).setTextureSize(128, 64);
		propeller2.addBox(0F, 0F, -1F, 1, 4, 2, 0.0F);
		propeller2.rotateAngleY = (float) Math.toRadians(15);
		propeller2B.addChild(propeller2);

		RendererModel propeller3B = new RendererModel(this, 90, 4).setTextureSize(128, 64);
		propeller.addChild(propeller3B);
		propeller3B.rotateAngleX = (float) Math.toRadians(2 * 360F / 3F);

		RendererModel propeller3 = new RendererModel(this, 90, 4).setTextureSize(128, 64);
		propeller3.addBox(0F, 0F, -1F, 1, 4, 2, 0.0F);
		propeller3.rotateAngleY = (float) Math.toRadians(15);
		propeller3B.addChild(propeller3);
		// this.boatSides[4] = (new RendererModel(this, 0, 43)).setTextureSize(128, 64);
		//this.boatSides[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
		//this.boatSides[4].setRotationPoint(0.0F, 4.0F, 9.0F);

		icebreak = (new RendererModel(this, 34, 56)).setTextureSize(128, 64);
		icebreak.addBox(16, -2, -2, 7, 4, 4, 0.0F);

		tank = (new RendererModel(this, 86, 24)).setTextureSize(128, 64);
		tank.addBox(-14, -2, -8, 5, 5, 16, 0.0F);

		ruddersBase = (new RendererModel(this, 92, 29)).setTextureSize(128, 64);
		ruddersBase.addBox(-18, -3, -8, 2, 6, 3, 0.0F);

		RendererModel ruddersBase2 = (new RendererModel(this, 92, 29)).setTextureSize(128, 64);
		ruddersBase2.addBox(-18, -3, 6, 2, 6, 3, 0.0F);
		ruddersBase.addChild(ruddersBase2);

		rudder1 = (new RendererModel(this, 112, 23)).setTextureSize(128, 64);
		rudder1.setRotationPoint(-15, 3, -6.5f);
		rudder1.addBox(-4, 0, -.5f, 4, 6, 1, 0.0F);

		rudder2 = (new RendererModel(this, 112, 23)).setTextureSize(128, 64);
		rudder2.setRotationPoint(-15, 3, 7.5f);
		rudder2.addBox(-4, 0, -.5f, 4, 6, 1, 0.0F);


		RendererModel pipe1 = (new RendererModel(this, 112, 38)).setTextureSize(128, 64);
		pipe1.addBox(-13, -3, 4, 1, 1, 1, 0.0F);
		tank.addChild(pipe1);

		RendererModel pipe2 = (new RendererModel(this, 116, 38)).setTextureSize(128, 64);
		pipe2.addBox(-15, -4, 4, 3, 1, 1, 0.0F);
		tank.addChild(pipe2);

		RendererModel pip3 = (new RendererModel(this, 112, 38)).setTextureSize(128, 64);
		pip3.addBox(-15, -4, 3, 1, 1, 1, 0.0F);
		tank.addChild(pip3);

		coreSampleBoat = (new RendererModel(this, 10, 0)).setTextureSize(128, 64);
		coreSampleBoat.addBox(-10, -1, -13, 4, 2, 2, 0.0F);

		RendererModel core2 = (new RendererModel(this, 10, 0)).setTextureSize(128, 64);
		core2.addBox(-11, -2, -14, 1, 4, 4, 0.0F);
		coreSampleBoat.addChild(core2);

		RendererModel core3 = (new RendererModel(this, 10, 0)).setTextureSize(128, 64);
		core3.addBox(-6, -2, -14, 1, 4, 4, 0.0F);
		coreSampleBoat.addChild(core3);

		coreSampleBoatDrill = (new RendererModel(this, 10, 0)).setTextureSize(128, 64);
		coreSampleBoatDrill.addBox(-3, -8, -16, 6, 18, 6, 0.0F);


		RendererModel iS1 = (new RendererModel(this, 56, 52)).setTextureSize(128, 64);
		iS1.addBox(0.01f, -7.01F, -0.01F, 16, 10, 2, 0.0F);
		iS1.setRotationPoint(26.0F, 3.0F, 0.0F);
		iS1.rotateAngleY = (float) Math.toRadians(180 + 45);
		icebreak.addChild(iS1);

		RendererModel iS1T = (new RendererModel(this, 100, 45)).setTextureSize(128, 64);
		iS1T.addBox(4, 0, -2F, 12, 5, 2, 0.0F);
		iS1T.setRotationPoint(0F, -7F, 0F);
		iS1T.rotateAngleX = (float) Math.toRadians(180 - 23);
		iS1.addChild(iS1T);


		RendererModel iS2 = (new RendererModel(this, 56, 52)).setTextureSize(128, 64);
		iS2.addBox(0, -7.0F, -2F, 16, 10, 2, 0.0F);
		iS2.setRotationPoint(26.0F, 3.0F, 0.0F);
		iS2.rotateAngleY = (float) Math.toRadians(180 - 45);
		icebreak.addChild(iS2);

		RendererModel iS2T = (new RendererModel(this, 100, 45)).setTextureSize(128, 64);
		iS2T.addBox(4, 0, 0F, 12, 5, 2, 0.0F);
		iS2T.setRotationPoint(0F, -7F, 0F);
		iS2T.rotateAngleX = (float) Math.toRadians(180 + 23);
		iS2.addChild(iS2T);
	}
	
	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void renderPaddle(BoatEntity boat, int paddle, float scale, float limbSwing, boolean rowing){
		if(rowing){
			float f = boat.getRowingTime(paddle, limbSwing);// * 40.0F;
			RendererModel rendererModel = this.paddles[paddle];
			rendererModel.rotateAngleX = (float) MathHelper.clampedLerp((double)(-(float)Math.PI / 3F), (double)-0.2617994F, (double)((MathHelper.sin(-f) + 1.0F) / 2.0F));
			rendererModel.rotateAngleY = (float) MathHelper.clampedLerp((double)(-(float)Math.PI / 4F), (double)((float)Math.PI / 4F), (double)((MathHelper.sin(-f + 1.0F) + 1.0F) / 2.0F));
			
			rendererModel.setRotationPoint(3.0F, -5.0F, 9.0F);
			
			if(paddle == 1){
				rendererModel.setRotationPoint(3.0F, -5.0F, -9.0F);
				rendererModel.rotateAngleY = (float) Math.PI - rendererModel.rotateAngleY;
			}
			
			rendererModel.render(scale);
		}else{
			RendererModel RendererModel = this.paddles[paddle];
			RendererModel.rotateAngleX = (float) Math.toRadians(-25);
			RendererModel.rotateAngleY = (float) Math.toRadians(-90);
			
			RendererModel.setRotationPoint(3.0F, -2.0F, 11.0F);
			
			if(paddle == 1){
				RendererModel.setRotationPoint(3.0F, -2.0F, -11.0F);
				RendererModel.rotateAngleY = (float) Math.PI - RendererModel.rotateAngleY;
			}
			
			RendererModel.render(scale);
		}
	}
	
	public void renderPaddles(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale){
		SpeedboatEntity boatEntity = (SpeedboatEntity) entityIn;
		this.renderPaddle(boatEntity, 0, scale, limbSwing, boatEntity.isEmergency());
		this.renderPaddle(boatEntity, 1, scale, limbSwing, boatEntity.isEmergency());
	}
	
	RendererModel makePaddle(boolean p_187056_1_){
		RendererModel RendererModel = (new RendererModel(this, 62, p_187056_1_ ? 2 : 22)).setTextureSize(128, 64);
		RendererModel.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18);
		RendererModel.addBox(p_187056_1_ ? -1.001F : 0.001F, -3.0F, 8.0F, 1, 6, 7);
		return RendererModel;
	}
	
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale){
		GlStateManager.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
		SpeedboatEntity BoatEntity = (SpeedboatEntity) entityIn;
		
		for(int i = 0;i < 5;++i){
			this.boatSides[i].render(scale);
		}
		
		float f1 = ((SpeedboatEntity) entityIn).getRowingTime(0, limbSwing) * 100.0F;
		this.propeller.rotateAngleX = BoatEntity.isEmergency() ? 0 : f1;
		float pr = BoatEntity.isEmergency() ? 0f : BoatEntity.propellerRotation;
		
		if(BoatEntity.leftInputDown && pr > -1)
			pr = pr - 0.1F * Minecraft.getInstance().getRenderPartialTicks();
		
		if(BoatEntity.rightInputDown && pr < 1)
			pr = pr + 0.1F * Minecraft.getInstance().getRenderPartialTicks();
		
		if(!BoatEntity.leftInputDown && !BoatEntity.rightInputDown)
			pr = (float) (pr * Math.pow(0.7, Minecraft.getInstance().getRenderPartialTicks()));
		
		this.propellerAssembly.rotateAngleY = (float) Math.toRadians(pr * 15);
		this.propellerAssembly.render(scale);
		
		//this.coreSampleBoat.render(scale);
		
		GlStateManager.pushMatrix();
		
		if(BoatEntity.isBeingRidden() && !BoatEntity.isEmergency())
			GlStateManager.translatef((entityIn.world.rand.nextFloat() - 0.5F) * 0.01F, (entityIn.world.rand.nextFloat() - 0.5F) * 0.01F, (entityIn.world.rand.nextFloat() - 0.5F) * 0.01F);
		
		this.motor.render(scale);
		GlStateManager.popMatrix();
	}
	
	public void renderIcebreaker(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale){
		this.icebreak.render(scale);
	}
	
	public void renderTank(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale){
		this.tank.render(scale);
	}
	
	public void renderRudders(Entity entityIn, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale){
		this.ruddersBase.render(scale);
		
		SpeedboatEntity BoatEntity = (SpeedboatEntity) entityIn;
		float pr = BoatEntity.propellerRotation;
		
		if(BoatEntity.leftInputDown && pr > -1)
			pr = pr - 0.1F * Minecraft.getInstance().getRenderPartialTicks();
		
		if(BoatEntity.rightInputDown && pr < 1)
			pr = pr + 0.1F * Minecraft.getInstance().getRenderPartialTicks();
		
		if(!BoatEntity.leftInputDown && !BoatEntity.rightInputDown)
			pr = (float) (pr * Math.pow(0.7F, Minecraft.getInstance().getRenderPartialTicks()));
		
		this.rudder2.rotateAngleY = (float) Math.toRadians(pr * 20f);
		this.rudder1.rotateAngleY = (float) Math.toRadians(pr * 20f);
		
		this.rudder1.render(scale);
		this.rudder2.render(scale);
	}
	
	public void renderBoatDrill(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale){
		this.coreSampleBoatDrill.render(scale);
	}
	
	public void renderMultipass(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale){
		GlStateManager.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.colorMask(false, false, false, false);
		this.noWater.render(scale);
		GlStateManager.colorMask(true, true, true, true);
	}
	
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn){
	}
}