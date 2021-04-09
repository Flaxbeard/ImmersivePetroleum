package flaxbeard.immersivepetroleum.client.model;

import java.util.Arrays;

import com.google.common.collect.ImmutableList;

import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelMotorboat extends SegmentedModel<MotorboatEntity>{
	private final ImmutableList<ModelRenderer> list;
	
	/**
	 * Part of the model rendered to make it seem like there's no water in the
	 * boat
	 */
	public ModelRenderer noWater;
	
	public ModelRenderer[] boatSides = new ModelRenderer[5];
	public ModelRenderer motor;
	public ModelRenderer propeller;
	public ModelRenderer propellerAssembly;
	
	public ModelRenderer icebreak;
	public ModelRenderer coreSampleBoat;
	public ModelRenderer coreSampleBoatDrill;
	public ModelRenderer tank;
	public ModelRenderer rudder1;
	public ModelRenderer rudder2;
	public ModelRenderer ruddersBase;
	public ModelRenderer[] paddles = new ModelRenderer[2];
	
	public ModelMotorboat(){
		this.boatSides[0] = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
		this.boatSides[1] = (new ModelRenderer(this, 0, 19)).setTextureSize(128, 64);
		this.boatSides[2] = (new ModelRenderer(this, 0, 27)).setTextureSize(128, 64);
		this.boatSides[3] = (new ModelRenderer(this, 0, 35)).setTextureSize(128, 64);
		this.boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
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
		
		this.noWater = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
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
		
		ImmutableList.Builder<ModelRenderer> builder = ImmutableList.builder();
		
		builder.addAll(Arrays.asList(this.boatSides));
		builder.addAll(Arrays.asList(
				this.motor,
				this.propellerAssembly));
		
		this.list = builder.build();
	}
	
	public void refresh(){
		motor = new ModelRenderer(this, 104, 0).setTextureSize(128, 64);
		motor.addBox(-19.0F, -8.0F, -3, 6, 5, 6, 0.0F);
		
		propellerAssembly = new ModelRenderer(this, 96, 0).setTextureSize(128, 64);
		propellerAssembly.setRotationPoint(-17F, 5F, 0);
		
		propellerAssembly.addBox(-1, -8.1F, -1, 2, 10, 2, 0.0F);
		
		ModelRenderer handle = new ModelRenderer(this, 72, 0).setTextureSize(128, 64);
		handle.addBox(4F, -9.7F, -0.5F, 6, 1, 1);
		handle.rotateAngleX = 0;
		handle.rotateAngleZ = (float) Math.toRadians(-5);
		propellerAssembly.addChild(handle);
		
		propeller = new ModelRenderer(this, 86, 0).setTextureSize(128, 64);
		propeller.addBox(-1F, -1F, -1F, 3, 2, 2, 0.0F);
		propeller.setRotationPoint(-3F, 0, 0);
		propellerAssembly.addChild(propeller);
		
		ModelRenderer propeller1 = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
		propeller1.addBox(0F, 0F, -1F, 1, 4, 2, 0.0F);
		propeller1.rotateAngleY = (float) Math.toRadians(15);
		propeller.addChild(propeller1);
		
		ModelRenderer propeller2B = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
		propeller.addChild(propeller2B);
		propeller2B.rotateAngleX = (float) Math.toRadians(360F / 3F);
		
		ModelRenderer propeller2 = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
		propeller2.addBox(0F, 0F, -1F, 1, 4, 2, 0.0F);
		propeller2.rotateAngleY = (float) Math.toRadians(15);
		propeller2B.addChild(propeller2);
		
		ModelRenderer propeller3B = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
		propeller.addChild(propeller3B);
		propeller3B.rotateAngleX = (float) Math.toRadians(2 * 360F / 3F);
		
		ModelRenderer propeller3 = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
		propeller3.addBox(0F, 0F, -1F, 1, 4, 2, 0.0F);
		propeller3.rotateAngleY = (float) Math.toRadians(15);
		propeller3B.addChild(propeller3);
//		this.boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
//		this.boatSides[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
//		this.boatSides[4].setRotationPoint(0.0F, 4.0F, 9.0F);
		
		icebreak = (new ModelRenderer(this, 34, 56)).setTextureSize(128, 64);
		icebreak.addBox(16, -2, -2, 7, 4, 4, 0.0F);
		
		tank = (new ModelRenderer(this, 86, 24)).setTextureSize(128, 64);
		tank.addBox(-14, -2, -8, 5, 5, 16, 0.0F);
		
		ruddersBase = (new ModelRenderer(this, 92, 29)).setTextureSize(128, 64);
		ruddersBase.addBox(-18, -3, -8, 2, 6, 3, 0.0F);
		
		ModelRenderer ruddersBase2 = (new ModelRenderer(this, 92, 29)).setTextureSize(128, 64);
		ruddersBase2.addBox(-18, -3, 6, 2, 6, 3, 0.0F);
		ruddersBase.addChild(ruddersBase2);
		
		rudder1 = (new ModelRenderer(this, 112, 23)).setTextureSize(128, 64);
		rudder1.setRotationPoint(-15, 3, -6.5f);
		rudder1.addBox(-4, 0, -.5f, 4, 6, 1, 0.0F);
		
		rudder2 = (new ModelRenderer(this, 112, 23)).setTextureSize(128, 64);
		rudder2.setRotationPoint(-15, 3, 7.5f);
		rudder2.addBox(-4, 0, -.5f, 4, 6, 1, 0.0F);
		
		ModelRenderer pipe1 = (new ModelRenderer(this, 112, 38)).setTextureSize(128, 64);
		pipe1.addBox(-13, -3, 4, 1, 1, 1, 0.0F);
		tank.addChild(pipe1);
		
		ModelRenderer pipe2 = (new ModelRenderer(this, 116, 38)).setTextureSize(128, 64);
		pipe2.addBox(-15, -4, 4, 3, 1, 1, 0.0F);
		tank.addChild(pipe2);
		
		ModelRenderer pip3 = (new ModelRenderer(this, 112, 38)).setTextureSize(128, 64);
		pip3.addBox(-15, -4, 3, 1, 1, 1, 0.0F);
		tank.addChild(pip3);
		
		coreSampleBoat = (new ModelRenderer(this, 10, 0)).setTextureSize(128, 64);
		coreSampleBoat.addBox(-10, -1, -13, 4, 2, 2, 0.0F);
		
		ModelRenderer core2 = (new ModelRenderer(this, 10, 0)).setTextureSize(128, 64);
		core2.addBox(-11, -2, -14, 1, 4, 4, 0.0F);
		coreSampleBoat.addChild(core2);
		
		ModelRenderer core3 = (new ModelRenderer(this, 10, 0)).setTextureSize(128, 64);
		core3.addBox(-6, -2, -14, 1, 4, 4, 0.0F);
		coreSampleBoat.addChild(core3);
		
		coreSampleBoatDrill = (new ModelRenderer(this, 10, 0)).setTextureSize(128, 64);
		coreSampleBoatDrill.addBox(-3, -8, -16, 6, 18, 6, 0.0F);
		
		ModelRenderer iS1 = (new ModelRenderer(this, 56, 52)).setTextureSize(128, 64);
		iS1.addBox(0.01f, -7.01F, -0.01F, 16, 10, 2, 0.0F);
		iS1.setRotationPoint(26.0F, 3.0F, 0.0F);
		iS1.rotateAngleY = (float) Math.toRadians(180 + 45);
		icebreak.addChild(iS1);
		
		ModelRenderer iS1T = (new ModelRenderer(this, 100, 45)).setTextureSize(128, 64);
		iS1T.addBox(4, 0, -2F, 12, 5, 2, 0.0F);
		iS1T.setRotationPoint(0F, -7F, 0F);
		iS1T.rotateAngleX = (float) Math.toRadians(180 - 23);
		iS1.addChild(iS1T);
		
		ModelRenderer iS2 = (new ModelRenderer(this, 56, 52)).setTextureSize(128, 64);
		iS2.addBox(0, -7.0F, -2F, 16, 10, 2, 0.0F);
		iS2.setRotationPoint(26.0F, 3.0F, 0.0F);
		iS2.rotateAngleY = (float) Math.toRadians(180 - 45);
		icebreak.addChild(iS2);
		
		ModelRenderer iS2T = (new ModelRenderer(this, 100, 45)).setTextureSize(128, 64);
		iS2T.addBox(4, 0, 0F, 12, 5, 2, 0.0F);
		iS2T.setRotationPoint(0F, -7F, 0F);
		iS2T.rotateAngleX = (float) Math.toRadians(180 + 23);
		iS2.addChild(iS2T);
	}
	
	ModelRenderer makePaddle(boolean left){
		ModelRenderer model = (new ModelRenderer(this, 62, left ? 2 : 22)).setTextureSize(128, 64);
		model.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18);
		model.addBox(left ? -1.001F : 0.001F, -3.0F, 8.0F, 1, 6, 7);
		return model;
	}
	
	@Override
	public void setRotationAngles(MotorboatEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		MotorboatEntity boatEntity = (MotorboatEntity) entityIn;
		
		this.setPaddleRotationAngles(boatEntity, 0, limbSwing, boatEntity.isEmergency());
		this.setPaddleRotationAngles(boatEntity, 1, limbSwing, boatEntity.isEmergency());
	}
	
	public void setPaddleRotationAngles(BoatEntity boat, int paddle, float limbSwing, boolean rowing){
		if(rowing){
			float f = boat.getRowingTime(paddle, limbSwing);
			ModelRenderer model = this.paddles[paddle];
			model.rotateAngleX = (float) MathHelper.clampedLerp((double) (-(float) Math.PI / 3F), (double) -0.2617994F, (double) ((MathHelper.sin(-f) + 1.0F) / 2.0F));
			model.rotateAngleY = (float) MathHelper.clampedLerp((double) (-(float) Math.PI / 4F), (double) ((float) Math.PI / 4F), (double) ((MathHelper.sin(-f + 1.0F) + 1.0F) / 2.0F));
			
			model.setRotationPoint(3.0F, -5.0F, 9.0F);
			
			if(paddle == 1){
				model.setRotationPoint(3.0F, -5.0F, -9.0F);
				model.rotateAngleY = (float) Math.PI - model.rotateAngleY;
			}
		}else{
			ModelRenderer model = this.paddles[paddle];
			model.rotateAngleX = (float) Math.toRadians(-25);
			model.rotateAngleY = (float) Math.toRadians(-90);
			
			model.setRotationPoint(3.0F, -2.0F, 11.0F);
			
			if(paddle == 1){
				model.setRotationPoint(3.0F, -2.0F, -11.0F);
				model.rotateAngleY = (float) Math.PI - model.rotateAngleY;
			}
		}
	}
	
	/**
	 * Only contains the base shape
	 */
	@Override
	public Iterable<ModelRenderer> getParts(){
		return this.list;
	}
	
	public ModelRenderer noWaterRenderer(){
		return this.noWater;
	}
}
