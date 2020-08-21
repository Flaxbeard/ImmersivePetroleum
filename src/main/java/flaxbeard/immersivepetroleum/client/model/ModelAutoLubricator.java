package flaxbeard.immersivepetroleum.client.model;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ModelAutoLubricator extends Model{
	private RendererModel base;
	private RendererModel tank;
	private RendererModel plunger;
	
	public ModelAutoLubricator(){
		this.textureWidth = 64;
		this.textureHeight = 64;
		
		this.tank = new RendererModel(this, 0, 0);
		tank.addBox(3, 14, 3, 10, 17, 10);
		
		base = new RendererModel(this, 24, 42);
		base.addBox(12, 8, 5, 4, 2, 2);
		
		RendererModel tankSide1 = new RendererModel(this, 32, 46);
		tankSide1.addBox(3, 14.01F, 2.99F, 10, 17, 1);
		base.addChild(tankSide1);
		
		RendererModel tankSide2 = new RendererModel(this, 32, 46);
		tankSide2.addBox(3, 14.01F, 12.01F, 10, 17, 1);
		base.addChild(tankSide2);
		
		RendererModel tankSide3 = new RendererModel(this, 32, 46);
		tankSide3.setRotationPoint(2.99F, 14.01F, 13);
		tankSide3.rotateAngleY = (float) Math.toRadians(90);
		tankSide3.addBox(0, 0, 0, 10, 17, 1);
		base.addChild(tankSide3);
		
		RendererModel tankSide4 = new RendererModel(this, 32, 46);
		tankSide4.setRotationPoint(12.01F, 14.01F, 13);
		tankSide4.rotateAngleY = (float) Math.toRadians(90);
		tankSide4.addBox(0, 0, 0, 10, 17, 1);
		base.addChild(tankSide4);
		
		RendererModel p2 = new RendererModel(this, 24, 42);
		p2.addBox(12, 8, 9, 4, 2, 2);
		this.base.addChild(p2);
		
		RendererModel p3 = new RendererModel(this, 0, 28);
		p3.addBox(2, 12, 2, 12, 2, 12);
		this.base.addChild(p3);
		
		RendererModel p4 = new RendererModel(this, 0, 42);
		p4.addBox(4, 7, 4, 8, 5, 8);
		this.base.addChild(p4);
		
		RendererModel p5 = new RendererModel(this, 32, 19);
		p5.addBox(4, 31, 4, 8, 1, 8);
		this.base.addChild(p5);
		
		RendererModel leg1 = new RendererModel(this, 48, 28);
		leg1.setRotationPoint(2, 0, 2);
		leg1.addBox(-1.5F, 0, -1.5F, 3, 12, 3);
		leg1.rotateAngleY = (float) Math.toRadians(45);
		leg1.rotateAngleX = (float) Math.toRadians(9.5);
		this.base.addChild(leg1);
		
		RendererModel leg2 = new RendererModel(this, 48, 28);
		leg2.setRotationPoint(2, 0, 14);
		leg2.addBox(-1.5F, 0, -1.5F, 3, 12, 3);
		leg2.rotateAngleY = (float) Math.toRadians(135);
		leg2.rotateAngleX = (float) Math.toRadians(9.5);
		this.base.addChild(leg2);
		
		RendererModel leg3 = new RendererModel(this, 48, 28);
		leg3.setRotationPoint(14, 0, 14);
		leg3.addBox(-1.5F, 0, -1.5F, 3, 12, 3);
		leg3.rotateAngleY = (float) Math.toRadians(225);
		leg3.rotateAngleX = (float) Math.toRadians(9.5);
		this.base.addChild(leg3);
		
		RendererModel leg4 = new RendererModel(this, 48, 28);
		leg4.setRotationPoint(14, 0, 2);
		leg4.addBox(-1.5F, 0, -1.5F, 3, 12, 3);
		leg4.rotateAngleY = (float) Math.toRadians(315);
		leg4.rotateAngleX = (float) Math.toRadians(9.5);
		this.base.addChild(leg4);
		
		RendererModel connector1 = new RendererModel(this, 40, 0);
		connector1.addBox(3, 9, 1.75F, 10, 2, 2);
		this.base.addChild(connector1);
		
		RendererModel connector2 = new RendererModel(this, 40, 0);
		connector2.addBox(3, 9, 14 - 1.75F, 10, 2, 2);
		this.base.addChild(connector2);
		
		RendererModel connector3 = new RendererModel(this, 40, 4);
		connector3.addBox(1.75F, 9, 3, 2, 2, 10);
		this.base.addChild(connector3);
		
		RendererModel connector4 = new RendererModel(this, 40, 0);
		connector4.addBox(3, 3.5F, 1F, 10, 2, 2);
		this.base.addChild(connector4);
		
		RendererModel connector5 = new RendererModel(this, 40, 0);
		connector5.addBox(3, 3.5F, 13F, 10, 2, 2);
		this.base.addChild(connector5);
		
		RendererModel connector6 = new RendererModel(this, 40, 4);
		connector6.addBox(1, 3.5F, 3, 2, 2, 10);
		this.base.addChild(connector6);
		
		RendererModel connector7 = new RendererModel(this, 40, 4);
		connector7.addBox(13, 3.5F, 3, 2, 2, 10);
		this.base.addChild(connector7);
		
		plunger = new RendererModel(this, 23, 46);
		plunger.addBox(3.5F, 27, 3.5F, 9, 2, 9);
	}
	
	public void render(float f){
		this.base.render(f);
	}
	
	public void renderTank(float f){
		this.tank.render(f);
	}
	
	public void renderPlunger(float f){
		this.plunger.render(f);
	}
}