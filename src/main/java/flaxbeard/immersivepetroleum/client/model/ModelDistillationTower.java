package flaxbeard.immersivepetroleum.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelDistillationTower extends ModelBase
{
	public ModelRenderer base;
	public ModelRenderer redControls;
	public ModelRenderer redControlsF1;
	public ModelRenderer redControlsF2;
	
	public ModelRenderer pipe;
	
	public ModelRenderer tank;
	
	public ModelRenderer boil;

	
	public ModelRenderer floor1b;
	public ModelRenderer floor2b;
	public ModelRenderer floor3b;
	
	public ModelRenderer floor1t;
	public ModelRenderer floor2t;
	public ModelRenderer floor3t;
	
	public ModelRenderer floor1s1;
	public ModelRenderer floor2s1;
	public ModelRenderer floor3s1;
	public ModelRenderer floor1s2;
	public ModelRenderer floor2s2;
	public ModelRenderer floor3s2;
	public ModelRenderer floor1s3;
	public ModelRenderer floor2s3;
	public ModelRenderer floor3s3;
	public ModelRenderer floor1s4;
	public ModelRenderer floor2s4;
	public ModelRenderer floor3s4;

	public ModelRenderer ladder;

	public ModelRenderer ladderS1;
	public ModelRenderer ladderS2;
	public ModelRenderer ladderS3;

	public boolean mirror;
	
	public ModelDistillationTower(boolean mirror)
	{
		this.textureWidth = 512;
		this.textureHeight = 512;
		
		this.mirror = mirror;
		
		refresh();
	}
	
	public void refresh()
	{		
		this.base = new ModelRenderer(this, 120, 0);
		this.base.addBox(0, 0, 0, 64, 8, 64);
		
		this.redControlsF2 = new ModelRenderer(this, 120, 72);
		this.redControlsF2.addBox(58, 8, mirror ? 50 : 2, 4, 8, 2);
		this.base.addChild(redControlsF2);
		
		this.redControlsF1 = new ModelRenderer(this, 120, 72);
		this.redControlsF1.addBox(58, 8, mirror ? 60 : 12, 4, 8, 2);
		this.base.addChild(redControlsF1);
		
		this.redControls = new ModelRenderer(this, 120, 72);
		this.redControls.addBox(56, 16, mirror ? 48 : 0, 8, 16, 16);
		this.base.addChild(redControls);
		
		this.tank = new ModelRenderer(this, 0, 0);
		this.tank.addBox(17, 18, 17, 30, 238, 30);
		this.base.addChild(tank);

		this.floor1b = new ModelRenderer(this, 120, 72);
		this.floor1b.addBox(0, 64 + 8, 0, 64, 0, 64);
		this.base.addChild(floor1b);
		this.floor2b = new ModelRenderer(this, 120, 72);
		this.floor2b.addBox(0, 128 + 8, 0, 64, 0, 64);
		this.base.addChild(floor2b);
		this.floor3b = new ModelRenderer(this, 120, 72);
		this.floor3b.addBox(0, 192 + 8, 0, 64, 0, 64);
		this.base.addChild(floor3b);
		
		this.floor1t = new ModelRenderer(this, 120, 72);
		this.floor1t.addBox(0, 64 + 16, 0, 64, 0, 64);
		this.base.addChild(floor1t);
		this.floor2t = new ModelRenderer(this, 120, 72);
		this.floor2t.addBox(0, 128 + 16, 0, 64, 0, 64);
		this.base.addChild(floor2t);
		this.floor3t = new ModelRenderer(this, 120, 72);
		this.floor3t.addBox(0, 192 + 16, 0, 64, 0, 64);
		this.base.addChild(floor3t);
		
		this.floor1s1 = new ModelRenderer(this, 120, 136-64);
		this.floor1s1.addBox(0, 64 + 8, 0, 0, 8, 64);
		this.base.addChild(floor1s1);
		this.floor2s1 = new ModelRenderer(this, 120, 136-64);
		this.floor2s1.addBox(0, 128 + 8, 0, 0, 8, 64);
		this.base.addChild(floor2s1);
		this.floor3s1 = new ModelRenderer(this, 120, 136-64);
		this.floor3s1.addBox(0, 192 + 8, 0, 0, 8, 64);
		this.base.addChild(floor3s1);
		
		this.floor1s2 = new ModelRenderer(this, 120, 136-64);
		this.floor1s2.addBox(64, 64 + 8, 0, 0, 8, 64);
		this.base.addChild(floor1s2);
		this.floor2s2 = new ModelRenderer(this, 120, 136-64);
		this.floor2s2.addBox(64, 128 + 8, 0, 0, 8, 64);
		this.base.addChild(floor2s2);
		this.floor3s2 = new ModelRenderer(this, 120, 136-64);
		this.floor3s2.addBox(64, 192 + 8, 0, 0, 8, 64);
		this.base.addChild(floor3s2);
		
		this.floor1s3 = new ModelRenderer(this, 120, 136);
		this.floor1s3.addBox(0, 64 + 8, 0, 64, 8, 0);
		this.base.addChild(floor1s3);
		this.floor2s3 = new ModelRenderer(this, 120, 136);
		this.floor2s3.addBox(0, 128 + 8, 0, 64, 8, 0);
		this.base.addChild(floor2s3);
		this.floor3s3 = new ModelRenderer(this, 120, 136);
		this.floor3s3.addBox(0, 192 + 8, 0, 64, 8, 0);
		this.base.addChild(floor3s3);
		
		this.floor1s4 = new ModelRenderer(this, 120, 136);
		this.floor1s4.addBox(0, 64 + 8, 64, 64, 8, 0);
		this.base.addChild(floor1s4);
		this.floor2s4 = new ModelRenderer(this, 120, 136);
		this.floor2s4.addBox(0, 128 + 8, 64, 64, 8, 0);
		this.base.addChild(floor2s4);
		this.floor3s4 = new ModelRenderer(this, 120, 136);
		this.floor3s4.addBox(0, 192 + 8, 64, 64, 8, 0);
		this.base.addChild(floor3s4);
		
		this.ladder = new ModelRenderer(this, 376, 0);
		this.ladder.addBox(18, 24, mirror ? 16.01F : 46.99F, 12, 192, 1);
		this.base.addChild(ladder);
		
		this.ladderS1 = new ModelRenderer(this, 402, -64);
		this.ladderS1.addBox(16.01F, 0, mirror ? 0 : 47, 0, 152 + 64, 16);
		this.base.addChild(ladderS1);
		
		this.ladderS2 = new ModelRenderer(this, 402, -64);
		this.ladderS2.addBox(32, 0, mirror ? 0 : 47, 0, 152 + 64, 16);
		this.base.addChild(ladderS2);
		
		this.ladderS3 = new ModelRenderer(this, 434, 0);
		this.ladderS3.addBox(16, 48, mirror ? 0.1F : 47 + 15.9F, 16, 154, 0);
		this.base.addChild(ladderS3);
		
		ModelRenderer pform = new ModelRenderer(this, 120, 144);
		pform.addBox(14, 7.99F, 14, 36, 10, 36);
		this.base.addChild(pform);
		
		boil = new ModelRenderer(this, 264, 144);
		boil.addBox(0, 8, mirror ? 32 : 0, 16, 24, 32);
		
		ModelRenderer boilPipeBase1 = new ModelRenderer(this, 268, 144);
		boilPipeBase1.addBox(0, 0, 0, 2, 12, 12);
		boilPipeBase1.setRotationPoint(2, 34, mirror ? 34 : 18);
		boilPipeBase1.rotateAngleZ = 3F * (float) Math.PI / 2F;
		this.base.addChild(boilPipeBase1);
		
		ModelRenderer boilPipe1 = new ModelRenderer(this, 120, 144);
		boilPipe1.addBox(4, 34, mirror ? 36 : 20, 8, 14, 8);
		this.base.addChild(boilPipe1);
		
		ModelRenderer boilPipe2 = new ModelRenderer(this, 228, 144);
		boilPipe2.addBox(12, 40, mirror ? 36 : 20, 3, 8, 8);
		this.base.addChild(boilPipe2);
		
		ModelRenderer boilPipeBase2 = new ModelRenderer(this, 268, 144);
		boilPipeBase2.addBox(15, 38, mirror ? 34 : 18, 2, 12, 12);
		this.base.addChild(boilPipeBase2);
		
		ModelRenderer bigPipe = new ModelRenderer(this, 466, 0);
		bigPipe.addBox(40 - 5, 16, mirror ? 51 : 3, 10, 234, 10);
		this.base.addChild(bigPipe);
		
		ModelRenderer bigPipeTop = new ModelRenderer(this, 120, 166);
		bigPipeTop.addBox(40 - 5, 240, mirror ? 47 : 13, 10, 10, 4);
		this.base.addChild(bigPipeTop);
		
		ModelRenderer bigPipeOut = new ModelRenderer(this, 312, 104);
		bigPipeOut.addBox(32, 0.01F, mirror ? 48.01F : -0.01F, 16, 16, 16);
		this.base.addChild(bigPipeOut);
		
		this.pipe = new ModelRenderer(this, 120, 104);
		this.pipe.addBox(48.01F, 0.01F, mirror ? 16 : 32, 16, 16, 16);
		this.base.addChild(pipe);
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		this.base.render(f5);
	}
	
	public void renderFurnace(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		this.boil.render(f5);
	}
}
