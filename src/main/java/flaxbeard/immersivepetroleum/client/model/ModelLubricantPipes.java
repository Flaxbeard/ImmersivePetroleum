package flaxbeard.immersivepetroleum.client.model;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ModelLubricantPipes{
	
	public static class Base extends Model{
		private RendererModel base;
		private RendererModel tank;
		private RendererModel plunger;
		
		public Base(){
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
	
	public static class Crusher extends Model{
		private RendererModel base;
		
		public Crusher(boolean mirror){
			this.textureWidth = 16;
			this.textureHeight = 16;
			
			this.base = new RendererModel(this, 0, 0);
			base.addBox(20, 8, 9, 12, 2, 2);
			
			RendererModel p1 = new RendererModel(this, 0, 0);
			p1.setRotationPoint(20, 9, 10);
			p1.addBox(-1, -1, 0, 12, 2, 2);
			p1.rotateAngleY = (float) Math.toRadians(270);
			this.base.addChild(p1);
			
			RendererModel p2 = new RendererModel(this, 0, 0);
			p2.setRotationPoint(31, 9, -10);
			p2.addBox(-1, -1, 0, 18, 2, 2);
			p2.rotateAngleY = (float) Math.toRadians(270);
			this.base.addChild(p2);
			
			RendererModel p3 = new RendererModel(this, 0, 0);
			p3.setRotationPoint(30, 10, -10);
			p3.addBox(0, -1, -1, 40, 2, 2);
			p3.rotateAngleZ = (float) Math.toRadians(90);
			this.base.addChild(p3);
			
			RendererModel p5 = new RendererModel(this, 0, 0);
			p5.addBox(31, 8, 5, 1, 2, 2);
			this.base.addChild(p5);
			
			RendererModel p6 = new RendererModel(this, 0, 0);
			p6.addBox(23, 48, -11, 6, 2, 2);
			this.base.addChild(p6);
			
			RendererModel p7 = new RendererModel(this, 0, 0);
			p7.addBox(8, 8, 19, 10, 2, 2);
			this.base.addChild(p7);
			
			RendererModel p8 = new RendererModel(this, 0, 0);
			p8.setRotationPoint(8, 9, 17);
			p8.addBox(-1, -1, 0, 5, 2, 2);
			p8.rotateAngleY = (float) Math.toRadians(270);
			this.base.addChild(p8);
			
			RendererModel p9 = new RendererModel(this, 0, 0);
			p9.setRotationPoint(7, 10, 17);
			p9.addBox(0, -1, -1, 14, 2, 2);
			p9.rotateAngleZ = (float) Math.toRadians(90);
			this.base.addChild(p9);
		}
		
		public void render(float f){
			this.base.render(f);
		}
	}
	
	public static class Excavator extends Model{
		private RendererModel base;
		
		public Excavator(boolean mirror){
			this.textureWidth = 16;
			this.textureHeight = 16;
			
			if(mirror){
				this.base = new RendererModel(this, 0, 0);
				base.addBox(51, 8, 6, 20, 2, 2);
				
				RendererModel p1 = new RendererModel(this, 0, 0);
				p1.setRotationPoint(71, 9, 1);
				p1.addBox(-1, -1, 0, 6, 2, 2);
				p1.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p1);
				
				RendererModel p2 = new RendererModel(this, 0, 0);
				p2.setRotationPoint(53, 9, 3);
				p2.addBox(-1, -1, 0, 4, 2, 2);
				p2.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p2);
				
				RendererModel p3 = new RendererModel(this, 0, 0);
				p3.setRotationPoint(52, 10, 3);
				p3.addBox(0, -1, -1, 6, 2, 2);
				p3.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p3);
				
				RendererModel p4 = new RendererModel(this, 0, 0);
				p4.setRotationPoint(52, 32, 8);
				p4.addBox(0, -1, -1, 9, 2, 2);
				p4.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p4);
				
				RendererModel p5 = new RendererModel(this, 0, 0);
				p5.addBox(48, 39, 7, 3, 2, 2);
				this.base.addChild(p5);
				
				RendererModel p6 = new RendererModel(this, 0, 0);
				p6.setRotationPoint(52, 16, -1);
				p6.addBox(0, -1, -1, 18, 2, 2);
				p6.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p6);
				
				RendererModel p7 = new RendererModel(this, 0, 0);
				p7.setRotationPoint(53, 15, -1);
				p7.addBox(-1, -1, 0, 4, 2, 2);
				p7.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p7);
				
				RendererModel p8 = new RendererModel(this, 0, 0);
				p8.setRotationPoint(53, 33, 1);
				p8.addBox(-1, -1, 0, 7, 2, 2);
				p8.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p8);
				
				RendererModel p9 = new RendererModel(this, 0, 0);
				p9.addBox(48, 39, 39, 3, 2, 2);
				this.base.addChild(p9);
				
				RendererModel p10 = new RendererModel(this, 0, 0);
				p10.setRotationPoint(75, 9, 1);
				p10.addBox(-1, -1, 0, 2, 2, 2);
				p10.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p10);
				
				RendererModel p11 = new RendererModel(this, 0, 0);
				p11.addBox(73, 8, 2, 16, 2, 2);
				this.base.addChild(p11);
				
				RendererModel p12 = new RendererModel(this, 0, 0);
				p12.setRotationPoint(89, 9, 5);
				p12.addBox(-1, -1, 0, 4, 2, 2);
				p12.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p12);
				
			}else{
				this.base = new RendererModel(this, 0, 0);
				base.addBox(51, 8, 40, 20, 2, 2);
				
				RendererModel p1 = new RendererModel(this, 0, 0);
				p1.setRotationPoint(71, 9, 43);
				p1.addBox(-1, -1, 0, 6, 2, 2);
				p1.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p1);
				
				RendererModel p2 = new RendererModel(this, 0, 0);
				p2.setRotationPoint(53, 9, 43);
				p2.addBox(-1, -1, 0, 4, 2, 2);
				p2.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p2);
				
				RendererModel p3 = new RendererModel(this, 0, 0);
				p3.setRotationPoint(52, 10, 45);
				p3.addBox(0, -1, -1, 6, 2, 2);
				p3.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p3);
				
				RendererModel p4 = new RendererModel(this, 0, 0);
				p4.setRotationPoint(52, 32, 40);
				p4.addBox(0, -1, -1, 9, 2, 2);
				p4.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p4);
				
				RendererModel p5 = new RendererModel(this, 0, 0);
				p5.addBox(48, 39, 39, 3, 2, 2);
				this.base.addChild(p5);
				
				RendererModel p6 = new RendererModel(this, 0, 0);
				p6.setRotationPoint(52, 16, 49);
				p6.addBox(0, -1, -1, 18, 2, 2);
				p6.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p6);
				
				RendererModel p7 = new RendererModel(this, 0, 0);
				p7.setRotationPoint(53, 15, 47);
				p7.addBox(-1, -1, 0, 4, 2, 2);
				p7.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p7);
				
				RendererModel p8 = new RendererModel(this, 0, 0);
				p8.setRotationPoint(53, 33, 42);
				p8.addBox(-1, -1, 0, 7, 2, 2);
				p8.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p8);
				
				RendererModel p9 = new RendererModel(this, 0, 0);
				p9.addBox(48, 39, 39, 3, 2, 2);
				this.base.addChild(p9);
				
				RendererModel p10 = new RendererModel(this, 0, 0);
				p10.setRotationPoint(75, 9, 47);
				p10.addBox(-1, -1, 0, 2, 2, 2);
				p10.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p10);
				
				RendererModel p11 = new RendererModel(this, 0, 0);
				p11.addBox(73, 8, 44, 16, 2, 2);
				this.base.addChild(p11);
				
				RendererModel p12 = new RendererModel(this, 0, 0);
				p12.setRotationPoint(89, 9, 41);
				p12.addBox(-1, -1, 0, 4, 2, 2);
				p12.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p12);
			}
			
		}
		
		public void render(float f){
			this.base.render(f);
		}
		
	}
	
	public static class Pumpjack extends Model{
		private RendererModel base;
		
		boolean mirror = false;
		
		public Pumpjack(boolean mirror){
			this.textureWidth = 16;
			this.textureHeight = 16;
			this.mirror = mirror;
			
			if(mirror){
				this.base = new RendererModel(this, 0, 0);
				base.addBox(21, 8, 12, 15, 2, 2);
				
				RendererModel p1 = new RendererModel(this, 0, 0);
				p1.setRotationPoint(23, 9, 1);
				p1.addBox(-1, -1, 0, 12, 2, 2);
				p1.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p1);
				
				RendererModel p2 = new RendererModel(this, 0, 0);
				p2.setRotationPoint(38, 9, 13);
				p2.addBox(-1, -1, 0, 13, 2, 2);
				p2.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p2);
				
				RendererModel p3 = new RendererModel(this, 0, 0);
				p3.addBox(34, 8, 23, 2, 2, 2);
				this.base.addChild(p3);
				
				RendererModel p4 = new RendererModel(this, 0, 0);
				p4.setRotationPoint(33, 8, 24);
				p4.addBox(0, -1, -1, 30, 2, 2);
				p4.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p4);
				
				RendererModel p5 = new RendererModel(this, 0, 0);
				p5.addBox(24, 36, 23, 8, 2, 2);
				this.base.addChild(p5);
				
				RendererModel p6 = new RendererModel(this, 0, 0);
				p6.setRotationPoint(26, 9.01F, 0);
				p6.addBox(0, -1F, -1, 9, 2, 2);
				p6.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p6);
				
				RendererModel p7 = new RendererModel(this, 0, 0);
				p7.addBox(25, 8, 8.5F, 18, 2, 2);
				this.base.addChild(p7);
				
				RendererModel leg1 = new RendererModel(this, 176, 56);
				// leg1.addBox(-1F, 1F, -4F, 38, 2, 2);
				leg1.setRotationPoint(56 - 13.6F, 8F, 12F);
				leg1.rotateAngleX = (float) Math.toRadians(10);
				leg1.rotateAngleY = (float) Math.toRadians(20);
				leg1.rotateAngleZ = (float) Math.toRadians(-15);
				this.base.addChild(leg1);
				
				RendererModel leg2 = new RendererModel(this, 176, 56);
				leg2.addBox(1F, -1F, -4F, 38, 2, 2);
				leg2.rotateAngleZ = (float) Math.toRadians(90);
				leg1.addChild(leg2);
				
				RendererModel p8 = new RendererModel(this, 0, 0);
				p8.setRotationPoint(52.5F, 43.3F, 14.7F);
				p8.addBox(0, 0, 0, 4, 2, 2);
				p8.rotateAngleY = (float) Math.toRadians(30);
				this.base.addChild(p8);
				
				RendererModel p10 = new RendererModel(this, 0, 0);
				p10.setRotationPoint(55f, 43.3f, 13f);
				p10.addBox(0, -2, 0, 6, 2, 2);
				p10.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p10);
			}else{
				this.base = new RendererModel(this, 0, 0);
				base.addBox(21, 8, 48 - 12 - 2, 15, 2, 2);
				
				RendererModel p1 = new RendererModel(this, 0, 0);
				p1.setRotationPoint(23, 9, 37);
				p1.addBox(-1, -1, 0, 12, 2, 2);
				p1.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p1);
				
				RendererModel p2 = new RendererModel(this, 0, 0);
				p2.setRotationPoint(38, 9, 24);
				p2.addBox(-1, -1, 0, 13, 2, 2);
				p2.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p2);
				
				RendererModel p3 = new RendererModel(this, 0, 0);
				p3.addBox(34, 8, 23, 2, 2, 2);
				this.base.addChild(p3);
				
				RendererModel p4 = new RendererModel(this, 0, 0);
				p4.setRotationPoint(33, 8, 24);
				p4.addBox(0, -1, -1, 30, 2, 2);
				p4.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p4);
				
				RendererModel p5 = new RendererModel(this, 0, 0);
				p5.addBox(24, 36, 23, 8, 2, 2);
				this.base.addChild(p5);
				
				RendererModel p6 = new RendererModel(this, 0, 0);
				p6.setRotationPoint(26, 9.01F, 0);
				p6.addBox(39, -1F, -1, 9, 2, 2);
				p6.rotateAngleY = (float) Math.toRadians(270);
				this.base.addChild(p6);
				
				RendererModel p7 = new RendererModel(this, 0, 0);
				p7.addBox(25, 8, 38.5F, 18, 2, 2);
				this.base.addChild(p7);
				
				RendererModel leg1 = new RendererModel(this, 176, 56);
				// leg1.addBox(-1F, 1F, -4F, 38, 2, 2);
				leg1.setRotationPoint(56 - 13.6F, 8F, 36F);
				leg1.rotateAngleX = (float) Math.toRadians(-10);
				leg1.rotateAngleY = (float) Math.toRadians(-20);
				leg1.rotateAngleZ = (float) Math.toRadians(-15);
				this.base.addChild(leg1);
				
				RendererModel leg2 = new RendererModel(this, 176, 56);
				leg2.addBox(1F, -1F, 3F, 38, 2, 2);
				leg2.rotateAngleZ = (float) Math.toRadians(90);
				leg1.addChild(leg2);
				
				RendererModel p8 = new RendererModel(this, 0, 0);
				p8.setRotationPoint(53F, 43.3F, 46 - 14.3F);
				p8.addBox(0, 0, 0, 4, 2, 2);
				p8.rotateAngleY = (float) Math.toRadians(-30);
				this.base.addChild(p8);
				
				RendererModel p10 = new RendererModel(this, 0, 0);
				p10.setRotationPoint(55f, 43.3f, 33f);
				p10.addBox(0, -2, 0, 6, 2, 2);
				p10.rotateAngleZ = (float) Math.toRadians(90);
				this.base.addChild(p10);
			}
		}
		
		public void render(float f){
			this.base.render(f);
		}
	}
}
