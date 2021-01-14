package flaxbeard.immersivepetroleum.client.model;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ModelPumpjack extends Model{
	public RendererModel base;
	public RendererModel swingy;
	public RendererModel connector;
	public RendererModel arm;
	public RendererModel wellConnector;
	public RendererModel wellConnector2;
	
	public float ticks = 0;
	
	public ModelPumpjack(){
		this.textureWidth = 190;
		this.textureHeight = 58;
		
		refresh();
	}
	
	public void refresh(){
		this.base = new RendererModel(this, 0, 0);
		this.base.addBox(8, 1, 8, 1, 1, 1);
		
		arm = new RendererModel(this, 0, 40);
		arm.addBox(-24 - 16, 0, -4, 70, 10, 8);
		arm.setRotationPoint(56, 48, 24);
		this.base.addChild(arm);
		
		RendererModel head = new RendererModel(this, 0, 0);
		head.addBox(30, -15, -5, 12, 30, 10);
		this.arm.addChild(head);
		
		RendererModel barBack = new RendererModel(this, 138, 0);
		barBack.addBox(-35F, 3F, -11F, 4, 4, 22);
		this.arm.addChild(barBack);
		
		swingy = new RendererModel(this, 44, 14);
		swingy.addBox(-4F, -2F, -14F, 8, 10, 4);
		swingy.setRotationPoint(24, 30, 30);
		this.base.addChild(swingy);
		
		RendererModel swingy2 = new RendererModel(this, 44, 14);
		swingy2.addBox(-4F, -2F, -2F, 8, 10, 4);
		this.swingy.addChild(swingy2);
		
		RendererModel counter = new RendererModel(this, 44, 0);
		counter.addBox(-12F, 8F, -14F, 24, 10, 4);
		this.swingy.addChild(counter);
		
		RendererModel counter2 = new RendererModel(this, 44, 0);
		counter2.addBox(-12F, 8F, -2F, 24, 10, 4);
		this.swingy.addChild(counter2);
		
		connector = new RendererModel(this, 108, 0);
		connector.addBox(-1F, -1F, -12F, 2, 24, 2);
		this.base.addChild(connector);
		
		RendererModel connector2 = new RendererModel(this, 100, 0);
		connector2.addBox(-1F, -1F, 6F, 2, 24, 2);
		this.connector.addChild(connector2);
		
		wellConnector = new RendererModel(this, 108, 0);
		wellConnector.addBox(-1F, 0F, -1F, 2, 30, 2);
		
		wellConnector2 = new RendererModel(this, 108, 0);
		wellConnector2.addBox(-1F, 0F, -1F, 2, 16, 2);
		
		this.base.addChild(wellConnector);
		this.base.addChild(wellConnector2);
	}
	
	public void render(float f5){
		arm.rotateAngleZ = (float) Math.toRadians(15 * Math.sin(ticks / 25D));
		swingy.rotateAngleZ = (float) (2 * (Math.PI / 4) + (ticks / 25D));
		
		float dist = 8.5F;
		
		float sin = (float) Math.sin(swingy.rotateAngleZ);
		float cos = (float) Math.cos(swingy.rotateAngleZ);
		connector.setRotationPoint(24 - dist * sin, 30 + dist * cos, 26);
		if(sin < 0){
			connector.rotateAngleZ = (float) (1F * (Math.PI / 2) + Math.atan(25F / (dist * sin)));
		}else if(sin > 0){
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
		
		if(Math.sqrt((tx2 - x2) * (tx2 - x2) + (ty2 - y2) * (ty2 - y2)) <= 16){
			wellConnector.isHidden = true;
			wellConnector2.isHidden = false;
		}else{
			wellConnector2.isHidden = true;
			wellConnector.isHidden = false;
		}
		
		this.base.render(f5);
	}
}
