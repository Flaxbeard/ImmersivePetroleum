package flaxbeard.immersivepetroleum.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelPumpjack extends ModelBase
{
	public ModelRenderer base;
	public ModelRenderer swingy;
	public ModelRenderer connector;
	public ModelRenderer arm;
	public ModelRenderer wellConnector;
	public ModelRenderer wellConnector2;

	public boolean mirror;
	public float ticks = 0;

	public ModelPumpjack(boolean mirror)
	{
		this.textureWidth = 512;
		this.textureHeight = 128;
		this.mirror = mirror;

		refresh();
	}

	public void refresh()
	{
		this.base = new ModelRenderer(this, 0, 0);
		this.base.addBox(0, 0, 0, 16 * 5, 8, 16 * 3);

		ModelRenderer box = new ModelRenderer(this, 128, 56);
		box.addBox(16, 8F, 20, 16, 28, 8);
		this.base.addChild(box);


		arm = new ModelRenderer(this, 0, 92);
		arm.addBox(-24 - 16, 0, -4, 70, 10, 8);
		arm.setRotationPoint(56, 48, 24);
		this.base.addChild(arm);

		ModelRenderer head = new ModelRenderer(this, 272, 0);
		head.addBox(30, -15, -5, 12, 30, 10);
		this.arm.addChild(head);

		ModelRenderer barBack = new ModelRenderer(this, 260, 40);
		barBack.addBox(-35F, 3F, -11F, 4, 4, 22);
		this.arm.addChild(barBack);


		ModelRenderer leg1 = new ModelRenderer(this, 176, 56);
		leg1.addBox(-2F, -2F, -2F, 4, 43, 4);
		leg1.setRotationPoint(56 - 13.6F, 8F, 12F);
		leg1.rotateAngleX = (float) Math.toRadians(10);
		leg1.rotateAngleZ = (float) Math.toRadians(-15);
		leg1.rotateAngleY = (float) Math.toRadians(20);
		this.base.addChild(leg1);

		ModelRenderer leg2 = new ModelRenderer(this, 176, 56);
		leg2.addBox(-2F, -2F, -2F, 4, 43, 4);
		leg2.setRotationPoint(56 + 13.6F, 8F, 12F);
		leg2.rotateAngleX = (float) Math.toRadians(10);
		leg2.rotateAngleZ = (float) Math.toRadians(15);
		leg2.rotateAngleY = (float) Math.toRadians(-20);
		this.base.addChild(leg2);

		ModelRenderer leg3 = new ModelRenderer(this, 176, 56);
		leg3.addBox(-2F, -2F, -1F, 4, 43, 4);
		leg3.setRotationPoint(56 - 13.6F, 8F, 36F);
		leg3.rotateAngleX = (float) Math.toRadians(-10);
		leg3.rotateAngleZ = (float) Math.toRadians(-15);
		leg3.rotateAngleY = (float) Math.toRadians(-20);
		this.base.addChild(leg3);

		ModelRenderer leg4 = new ModelRenderer(this, 176, 56);
		leg4.addBox(-2F, -2F, -2F, 4, 43, 4);
		leg4.setRotationPoint(56 + 13.6F, 8F, 36F);
		leg4.rotateAngleX = (float) Math.toRadians(-10);
		leg4.rotateAngleZ = (float) Math.toRadians(15);
		leg4.rotateAngleY = (float) Math.toRadians(20);
		this.base.addChild(leg4);

		ModelRenderer bar = new ModelRenderer(this, 238, 40);
		bar.addBox(54, 46F, 15F, 4, 4, 18);
		this.base.addChild(bar);


		swingy = new ModelRenderer(this, 290, 40);
		swingy.addBox(-4F, -2F, -14F, 8, 10, 4);
		swingy.setRotationPoint(24, 30, 30);
		this.base.addChild(swingy);

		ModelRenderer counter = new ModelRenderer(this, 200, 62);
		counter.addBox(-12F, 8F, -14F, 24, 10, 4);
		this.swingy.addChild(counter);

		ModelRenderer swingy2 = new ModelRenderer(this, 290, 40);
		swingy2.addBox(-4F, -2F, -2F, 8, 10, 4);
		this.swingy.addChild(swingy2);

		ModelRenderer counter2 = new ModelRenderer(this, 200, 62);
		counter2.addBox(-12F, 8F, -2F, 24, 10, 4);
		this.swingy.addChild(counter2);

		connector = new ModelRenderer(this, 192, 56);
		connector.addBox(-1F, -1F, -12F, 2, 24, 2);
		this.base.addChild(connector);

		ModelRenderer connector2 = new ModelRenderer(this, 192, 56);
		connector2.addBox(-1F, -1F, 6F, 2, 24, 2);
		this.connector.addChild(connector2);

		ModelRenderer out1 = new ModelRenderer(this, 0, 56);
		out1.addBox(48F, 0.01F, -0.01F, 16, 16, 16);
		this.base.addChild(out1);

		ModelRenderer out2 = new ModelRenderer(this, 64, 56);
		out2.addBox(48F, 0.01F, 32.01F, 16, 16, 16);
		this.base.addChild(out2);

		ModelRenderer well = new ModelRenderer(this, 192, 89);
		well.addBox(83F, 0F, 19F, 10, 16, 10);
		this.base.addChild(well);

		ModelRenderer pipe1 = new ModelRenderer(this, 194, 77);
		pipe1.addBox(59F, 8F, 21F, 24, 6, 6);
		this.base.addChild(pipe1);

		ModelRenderer pipe2 = new ModelRenderer(this, 140, 94);
		pipe2.addBox(53F, 8F, 16F, 6, 6, 16);
		this.base.addChild(pipe2);

		ModelRenderer redControlsF2 = new ModelRenderer(this, 0, 0);
		redControlsF2.addBox(2, 8, mirror ? 34 : 2, 4, 8, 2);
		this.base.addChild(redControlsF2);

		ModelRenderer redControlsF1 = new ModelRenderer(this, 0, 0);
		redControlsF1.addBox(2, 8, mirror ? 44 : 12, 4, 8, 2);
		base.addChild(redControlsF1);

		ModelRenderer redControls = new ModelRenderer(this, 0, 0);
		redControls.addBox(0, 16, mirror ? 32 : 0, 8, 16, 16);
		base.addChild(redControls);

		ModelRenderer powerThing = new ModelRenderer(this, 208, 0);
		powerThing.addBox(0, 8, mirror ? 0 : 32, 16, 24, 16);
		base.addChild(powerThing);

		wellConnector = new ModelRenderer(this, 256, 66);
		wellConnector.addBox(-1F, 0F, -1F, 2, 30, 2);

		wellConnector2 = new ModelRenderer(this, 256, 66);
		wellConnector2.addBox(-1F, 0F, -1F, 2, 16, 2);

		this.base.addChild(wellConnector);
		this.base.addChild(wellConnector2);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		arm.rotateAngleZ = (float) Math.toRadians(15 * Math.sin(ticks / 25D));
		swingy.rotateAngleZ = (float) (2 * (Math.PI / 4) + (ticks / 25D));

		float dist = 8.5F;

		float sin = (float) Math.sin(swingy.rotateAngleZ);
		float cos = (float) Math.cos(swingy.rotateAngleZ);
		connector.setRotationPoint(24 - dist * sin, 30 + dist * cos, 26);
		if (sin < 0)
		{
			connector.rotateAngleZ = (float) (1F * (Math.PI / 2) + Math.atan(25F / (dist * sin)));
		}
		else if (sin > 0)
		{
			connector.rotateAngleZ = (float) (3F * (Math.PI / 2) + Math.atan(25F / (dist * sin)));
		}

		float sin2 = (float) Math.sin(arm.rotateAngleZ);
		float cos2 = (float) Math.cos(arm.rotateAngleZ);


		float x = 24 - dist * sin;
		float y = 30 + dist * cos;

		float w = 33F;
		float h = 4F;

		float tx = 56 + w * -cos2 - h * sin2;
		float ty = 48 + w * -sin2 + h * cos2;

		connector.setRotationPoint(x, y, 26);
		connector.rotateAngleZ = (float) (3F * (Math.PI / 2) + Math.atan2(ty - y, tx - x));

		wellConnector.setRotationPoint(88F, 16F, 24F);
		wellConnector2.setRotationPoint(88F, 16F, 24F);

		float w2 = -34F;
		float h2 = -13F;

		float x2 = w2 * -cos2 - h2 * sin2;
		float y2 = w2 * -sin2 + h2 * cos2;

		float tx2 = 32F;
		float ty2 = -32F;
		wellConnector.setRotationPoint(56 + x2, 48 + y2, 24);
		wellConnector.rotateAngleZ = (float) (3F * (Math.PI / 2) + Math.atan2(ty2 - y2, tx2 - x2));

		wellConnector2.setRotationPoint(56 + x2, 48 + y2, 24);
		wellConnector2.rotateAngleZ = (float) (3F * (Math.PI / 2) + Math.atan2(ty2 - y2, tx2 - x2));

		if (Math.sqrt((tx2 - x2) * (tx2 - x2) + (ty2 - y2) * (ty2 - y2)) <= 16)
		{
			wellConnector.isHidden = true;
			wellConnector2.isHidden = false;
		}
		else
		{
			wellConnector2.isHidden = true;
			wellConnector.isHidden = false;
		}

		this.base.render(f5);
	}
}
