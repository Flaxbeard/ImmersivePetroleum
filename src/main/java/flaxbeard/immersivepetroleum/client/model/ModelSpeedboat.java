package flaxbeard.immersivepetroleum.client.model;

import flaxbeard.immersivepetroleum.common.entity.EntitySpeedboat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IMultipassModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelSpeedboat extends ModelBase implements IMultipassModel
{
	public ModelRenderer[] boatSides = new ModelRenderer[5];
	/**
	 * Part of the model rendered to make it seem like there's no water in the boat
	 */
	public ModelRenderer noWater;
	private final int patchList = GLAllocation.generateDisplayLists(1);
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

	public ModelSpeedboat()
	{
		this.boatSides[0] = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
		this.boatSides[1] = (new ModelRenderer(this, 0, 19)).setTextureSize(128, 64);
		this.boatSides[2] = (new ModelRenderer(this, 0, 27)).setTextureSize(128, 64);
		this.boatSides[3] = (new ModelRenderer(this, 0, 35)).setTextureSize(128, 64);
		this.boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
		int i = 32;
		int j = 6;
		int k = 20;
		int l = 4;
		int i1 = 28;
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
	}


	public void refresh()
	{

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
		// this.boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
		//this.boatSides[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
		//this.boatSides[4].setRotationPoint(0.0F, 4.0F, 9.0F);

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

	/**
	 * Sets the models various rotation angles then renders the model.
	 */

	public void renderPaddle(EntityBoat boat, int paddle, float scale, float limbSwing, boolean rowing)
	{
		if (rowing)
		{
			float f = 40.0F;
			float f1 = boat.getRowingTime(paddle, limbSwing) * 40.0F;
			ModelRenderer modelrenderer = this.paddles[paddle];
			modelrenderer.rotateAngleX = (float) MathHelper.clampedLerp(-1.0471975803375244D, -0.2617993950843811D, (double) ((MathHelper.sin(-f1) + 1.0F) / 2.0F));
			modelrenderer.rotateAngleY = (float) MathHelper.clampedLerp(-(Math.PI / 4D), (Math.PI / 4D), (double) ((MathHelper.sin(-f1 + 1.0F) + 1.0F) / 2.0F));

			modelrenderer.setRotationPoint(3.0F, -5.0F, 9.0F);


			if (paddle == 1)
			{
				modelrenderer.setRotationPoint(3.0F, -5.0F, -9.0F);
				modelrenderer.rotateAngleY = (float) Math.PI - modelrenderer.rotateAngleY;
			}

			modelrenderer.render(scale);
		}
		else
		{
			ModelRenderer modelrenderer = this.paddles[paddle];
			modelrenderer.rotateAngleX = (float) Math.toRadians(-25);
			modelrenderer.rotateAngleY = (float) Math.toRadians(-90);

			modelrenderer.setRotationPoint(3.0F, -2.0F, 11.0F);

			if (paddle == 1)
			{
				modelrenderer.setRotationPoint(3.0F, -2.0F, -11.0F);
				modelrenderer.rotateAngleY = (float) Math.PI - modelrenderer.rotateAngleY;
			}

			modelrenderer.render(scale);
		}
	}

	public void renderPaddles(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
		EntitySpeedboat entityboat = (EntitySpeedboat) entityIn;
		this.renderPaddle(entityboat, 0, scale, limbSwing, entityboat.isEmergency());
		this.renderPaddle(entityboat, 1, scale, limbSwing, entityboat.isEmergency());
	}

	ModelRenderer makePaddle(boolean p_187056_1_)
	{
		ModelRenderer modelrenderer = (new ModelRenderer(this, 62, p_187056_1_ ? 2 : 22)).setTextureSize(128, 64);
		int i = 20;
		int j = 7;
		int k = 6;
		float f = -5.0F;
		modelrenderer.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18);
		modelrenderer.addBox(p_187056_1_ ? -1.001F : 0.001F, -3.0F, 8.0F, 1, 6, 7);
		return modelrenderer;
	}

	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{

		GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
		EntitySpeedboat entityboat = (EntitySpeedboat) entityIn;
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

		for (int i = 0; i < 5; ++i)
		{
			this.boatSides[i].render(scale);
		}


		float f1 = ((EntitySpeedboat) entityIn).getRowingTime(0, limbSwing) * 100.0F;
		this.propeller.rotateAngleX = entityboat.isEmergency() ? 0 : f1;
		float pr = entityboat.isEmergency() ? 0f : entityboat.propellerRotation;
		if (entityboat.leftInputDown && pr > -1) pr = pr - 0.1F * Minecraft.getMinecraft().getRenderPartialTicks();
		if (entityboat.rightInputDown && pr < 1) pr = pr + 0.1F * Minecraft.getMinecraft().getRenderPartialTicks();
		if (!entityboat.leftInputDown && !entityboat.rightInputDown)
			pr = (float) (pr * Math.pow(0.7, Minecraft.getMinecraft().getRenderPartialTicks()));
		this.propellerAssembly.rotateAngleY = (float) Math.toRadians(pr * 15);
		this.propellerAssembly.render(scale);

		//this.coreSampleBoat.render(scale);

		GlStateManager.pushMatrix();

		if (entityboat.isBeingRidden() && !entityboat.isEmergency())
			GlStateManager.translate((entityIn.world.rand.nextFloat() - 0.5F) * 0.01F, (entityIn.world.rand.nextFloat() - 0.5F) * 0.01F, (entityIn.world.rand.nextFloat() - 0.5F) * 0.01F);
		this.motor.render(scale);
		GlStateManager.popMatrix();


	}

	public void renderIcebreaker(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale)
	{
		this.icebreak.render(scale);
	}

	public void renderTank(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale)
	{
		this.tank.render(scale);
	}

	public void renderRudders(Entity entityIn, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale)
	{
		this.ruddersBase.render(scale);

		EntitySpeedboat entityboat = (EntitySpeedboat) entityIn;
		float pr = entityboat.propellerRotation;
		if (entityboat.leftInputDown && pr > -1) pr = pr - 0.1F * Minecraft.getMinecraft().getRenderPartialTicks();
		if (entityboat.rightInputDown && pr < 1) pr = pr + 0.1F * Minecraft.getMinecraft().getRenderPartialTicks();
		if (!entityboat.leftInputDown && !entityboat.rightInputDown)
			pr = (float) (pr * Math.pow(0.7, Minecraft.getMinecraft().getRenderPartialTicks()));
		this.rudder2.rotateAngleY = (float) Math.toRadians(pr * 20f);
		this.rudder1.rotateAngleY = (float) Math.toRadians(pr * 20f);

		this.rudder1.render(scale);
		this.rudder2.render(scale);
	}

	public void renderBoatDrill(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale)
	{
		this.coreSampleBoatDrill.render(scale);
	}

	public void renderMultipass(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale)
	{
		GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.colorMask(false, false, false, false);
		this.noWater.render(scale);
		GlStateManager.colorMask(true, true, true, true);
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
	{
	}


}