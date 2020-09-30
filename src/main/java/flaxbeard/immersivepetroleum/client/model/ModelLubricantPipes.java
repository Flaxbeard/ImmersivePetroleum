package flaxbeard.immersivepetroleum.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelLubricantPipes{
	
	public static class Crusher extends IPModel{
		public static final String ID="crusher_lubepipes";
		
		private ModelRenderer origin;
		public Crusher(){
			super(RenderType::getEntitySolid);
			
			this.textureWidth=this.textureHeight=16;
		}
		
		@Override
		public void init(){
			this.origin = new ModelRenderer(this, 0, 0);
			this.origin.addBox(20, 8, 9, 12, 2, 2);
			
			ModelRenderer p1 = new ModelRenderer(this, 0, 0);
			p1.setRotationPoint(20, 9, 10);
			p1.addBox(-1, -1, 0, 12, 2, 2);
			p1.rotateAngleY = (float) Math.toRadians(270);
			this.origin.addChild(p1);
			
			ModelRenderer p2 = new ModelRenderer(this, 0, 0);
			p2.setRotationPoint(31, 9, -10);
			p2.addBox(-1, -1, 0, 18, 2, 2);
			p2.rotateAngleY = (float) Math.toRadians(270);
			this.origin.addChild(p2);
			
			ModelRenderer p3 = new ModelRenderer(this, 0, 0);
			p3.setRotationPoint(30, 10, -10);
			p3.addBox(0, -1, -1, 40, 2, 2);
			p3.rotateAngleZ = (float) Math.toRadians(90);
			this.origin.addChild(p3);
			
			ModelRenderer p5 = new ModelRenderer(this, 0, 0);
			p5.addBox(31, 8, 5, 1, 2, 2);
			this.origin.addChild(p5);
			
			ModelRenderer p6 = new ModelRenderer(this, 0, 0);
			p6.addBox(23, 48, -11, 6, 2, 2);
			this.origin.addChild(p6);
			
			ModelRenderer p7 = new ModelRenderer(this, 0, 0);
			p7.addBox(8, 8, 19, 10, 2, 2);
			this.origin.addChild(p7);
			
			ModelRenderer p8 = new ModelRenderer(this, 0, 0);
			p8.setRotationPoint(8, 9, 17);
			p8.addBox(-1, -1, 0, 5, 2, 2);
			p8.rotateAngleY = (float) Math.toRadians(270);
			this.origin.addChild(p8);
			
			ModelRenderer p9 = new ModelRenderer(this, 0, 0);
			p9.setRotationPoint(7, 10, 17);
			p9.addBox(0, -1, -1, 14, 2, 2);
			p9.rotateAngleZ = (float) Math.toRadians(90);
			this.origin.addChild(p9);
		}
		
		@Override
		public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha){
			this.origin.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		}
	}
	
	public static class Excavator extends IPModel{
		public static final String ID_NORMAL="excavator_lubepipes_normal";
		public static final String ID_MIRRORED="excavator_lubepipes_mirrored";
		
		private ModelRenderer origin;
		private boolean mirrored;
		public Excavator(boolean mirror){
			super(RenderType::getEntitySolid);
			this.mirrored = mirror;
			
			this.textureWidth=this.textureHeight=16;
		}
		
		@Override
		public void init(){
			if(this.mirrored){
				this.origin = new ModelRenderer(this, 0, 0);
				this.origin.addBox(51, 8, 6, 20, 2, 2);
				
				ModelRenderer p1 = new ModelRenderer(this, 0, 0);
				p1.setRotationPoint(71, 9, 1);
				p1.addBox(-1, -1, 0, 6, 2, 2);
				p1.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p1);
				
				ModelRenderer p2 = new ModelRenderer(this, 0, 0);
				p2.setRotationPoint(53, 9, 3);
				p2.addBox(-1, -1, 0, 4, 2, 2);
				p2.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p2);
				
				ModelRenderer p3 = new ModelRenderer(this, 0, 0);
				p3.setRotationPoint(52, 10, 3);
				p3.addBox(0, -1, -1, 6, 2, 2);
				p3.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p3);
				
				ModelRenderer p4 = new ModelRenderer(this, 0, 0);
				p4.setRotationPoint(52, 32, 8);
				p4.addBox(0, -1, -1, 9, 2, 2);
				p4.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p4);
				
				ModelRenderer p5 = new ModelRenderer(this, 0, 0);
				p5.addBox(48, 39, 7, 3, 2, 2);
				this.origin.addChild(p5);
				
				ModelRenderer p6 = new ModelRenderer(this, 0, 0);
				p6.setRotationPoint(52, 16, -1);
				p6.addBox(0, -1, -1, 18, 2, 2);
				p6.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p6);
				
				ModelRenderer p7 = new ModelRenderer(this, 0, 0);
				p7.setRotationPoint(53, 15, -1);
				p7.addBox(-1, -1, 0, 4, 2, 2);
				p7.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p7);
				
				ModelRenderer p8 = new ModelRenderer(this, 0, 0);
				p8.setRotationPoint(53, 33, 1);
				p8.addBox(-1, -1, 0, 7, 2, 2);
				p8.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p8);
				
				ModelRenderer p9 = new ModelRenderer(this, 0, 0);
				p9.addBox(48, 39, 39, 3, 2, 2);
				this.origin.addChild(p9);
				
				ModelRenderer p10 = new ModelRenderer(this, 0, 0);
				p10.setRotationPoint(75, 9, 1);
				p10.addBox(-1, -1, 0, 2, 2, 2);
				p10.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p10);
				
				ModelRenderer p11 = new ModelRenderer(this, 0, 0);
				p11.addBox(73, 8, 2, 16, 2, 2);
				this.origin.addChild(p11);
				
				ModelRenderer p12 = new ModelRenderer(this, 0, 0);
				p12.setRotationPoint(89, 9, 5);
				p12.addBox(-1, -1, 0, 4, 2, 2);
				p12.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p12);
				
			}else{
				this.origin = new ModelRenderer(this, 0, 0);
				this.origin.addBox(51, 8, 40, 20, 2, 2);
				
				ModelRenderer p1 = new ModelRenderer(this, 0, 0);
				p1.setRotationPoint(71, 9, 43);
				p1.addBox(-1, -1, 0, 6, 2, 2);
				p1.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p1);
				
				ModelRenderer p2 = new ModelRenderer(this, 0, 0);
				p2.setRotationPoint(53, 9, 43);
				p2.addBox(-1, -1, 0, 4, 2, 2);
				p2.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p2);
				
				ModelRenderer p3 = new ModelRenderer(this, 0, 0);
				p3.setRotationPoint(52, 10, 45);
				p3.addBox(0, -1, -1, 6, 2, 2);
				p3.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p3);
				
				ModelRenderer p4 = new ModelRenderer(this, 0, 0);
				p4.setRotationPoint(52, 32, 40);
				p4.addBox(0, -1, -1, 9, 2, 2);
				p4.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p4);
				
				ModelRenderer p5 = new ModelRenderer(this, 0, 0);
				p5.addBox(48, 39, 39, 3, 2, 2);
				this.origin.addChild(p5);
				
				ModelRenderer p6 = new ModelRenderer(this, 0, 0);
				p6.setRotationPoint(52, 16, 49);
				p6.addBox(0, -1, -1, 18, 2, 2);
				p6.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p6);
				
				ModelRenderer p7 = new ModelRenderer(this, 0, 0);
				p7.setRotationPoint(53, 15, 47);
				p7.addBox(-1, -1, 0, 4, 2, 2);
				p7.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p7);
				
				ModelRenderer p8 = new ModelRenderer(this, 0, 0);
				p8.setRotationPoint(53, 33, 42);
				p8.addBox(-1, -1, 0, 7, 2, 2);
				p8.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p8);
				
				ModelRenderer p9 = new ModelRenderer(this, 0, 0);
				p9.addBox(48, 39, 39, 3, 2, 2);
				this.origin.addChild(p9);
				
				ModelRenderer p10 = new ModelRenderer(this, 0, 0);
				p10.setRotationPoint(75, 9, 47);
				p10.addBox(-1, -1, 0, 2, 2, 2);
				p10.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p10);
				
				ModelRenderer p11 = new ModelRenderer(this, 0, 0);
				p11.addBox(73, 8, 44, 16, 2, 2);
				this.origin.addChild(p11);
				
				ModelRenderer p12 = new ModelRenderer(this, 0, 0);
				p12.setRotationPoint(89, 9, 41);
				p12.addBox(-1, -1, 0, 4, 2, 2);
				p12.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p12);
			}
		}
		
		@Override
		public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha){
			this.origin.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		}
	}
	
	public static class Pumpjack extends IPModel{
		public static final String ID_NORMAL="pumpjack_lubepipes_normal";
		public static final String ID_MIRRORED="pumpjack_lubepipes_mirrored";
		
		private boolean mirrored = false;
		private ModelRenderer origin;
		public Pumpjack(boolean mirror){
			super(RenderType::getEntitySolid);
			this.mirrored = mirror;
			
			this.textureWidth=this.textureHeight=16;
		}
		
		@Override
		public void init(){
			if(this.mirrored){
				this.origin = new ModelRenderer(this, 0, 0);
				this.origin.addBox(21, 8, 12, 15, 2, 2);
				
				ModelRenderer p1 = new ModelRenderer(this, 0, 0);
				p1.setRotationPoint(23, 9, 1);
				p1.addBox(-1, -1, 0, 12, 2, 2);
				p1.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p1);
				
				ModelRenderer p2 = new ModelRenderer(this, 0, 0);
				p2.setRotationPoint(38, 9, 13);
				p2.addBox(-1, -1, 0, 13, 2, 2);
				p2.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p2);
				
				ModelRenderer p3 = new ModelRenderer(this, 0, 0);
				p3.addBox(34, 8, 23, 2, 2, 2);
				this.origin.addChild(p3);
				
				ModelRenderer p4 = new ModelRenderer(this, 0, 0);
				p4.setRotationPoint(33, 8, 24);
				p4.addBox(0, -1, -1, 30, 2, 2);
				p4.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p4);
				
				ModelRenderer p5 = new ModelRenderer(this, 0, 0);
				p5.addBox(24, 36, 23, 8, 2, 2);
				this.origin.addChild(p5);
				
				ModelRenderer p6 = new ModelRenderer(this, 0, 0);
				p6.setRotationPoint(26, 9.01F, 0);
				p6.addBox(0, -1F, -1, 9, 2, 2);
				p6.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p6);
				
				ModelRenderer p7 = new ModelRenderer(this, 0, 0);
				p7.addBox(25, 8, 8.5F, 18, 2, 2);
				this.origin.addChild(p7);
				
				ModelRenderer leg1 = new ModelRenderer(this, 0, 0);
//				leg1.addBox(-1F, 1F, -4F, 38, 2, 2);
				leg1.setRotationPoint(56 - 13.6F, 8F, 12F);
				leg1.rotateAngleX = (float) Math.toRadians(9);
				leg1.rotateAngleY = (float) Math.toRadians(20);
				leg1.rotateAngleZ = (float) Math.toRadians(-14);
				this.origin.addChild(leg1);
				
				ModelRenderer leg2 = new ModelRenderer(this, 0, 0);
				leg2.addBox(1F, -1F, -4F, 38, 2, 2);
				leg2.rotateAngleZ = (float) Math.toRadians(90);
				leg1.addChild(leg2);
				
				ModelRenderer p8 = new ModelRenderer(this, 0, 0);
				p8.setRotationPoint(52.5F, 43.3F, 14.7F);
				p8.addBox(0, 0, 0, 4, 2, 2);
				p8.rotateAngleY = (float) Math.toRadians(30);
				this.origin.addChild(p8);
				
				ModelRenderer p10 = new ModelRenderer(this, 0, 0);
				p10.setRotationPoint(55f, 43.3f, 13f);
				p10.addBox(0, -2, 0, 6, 2, 2);
				p10.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p10);
				
			}else{
				
				this.origin = new ModelRenderer(this, 0, 0);
				this.origin.addBox(21, 8, 48 - 12 - 2, 15, 2, 2);
				
				ModelRenderer p1 = new ModelRenderer(this, 0, 0);
				p1.setRotationPoint(23, 9, 37);
				p1.addBox(-1, -1, 0, 12, 2, 2);
				p1.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p1);
				
				ModelRenderer p2 = new ModelRenderer(this, 0, 0);
				p2.setRotationPoint(38, 9, 24);
				p2.addBox(-1, -1, 0, 13, 2, 2);
				p2.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p2);
				
				ModelRenderer p3 = new ModelRenderer(this, 0, 0);
				p3.addBox(34, 8, 23, 2, 2, 2);
				this.origin.addChild(p3);
				
				ModelRenderer p4 = new ModelRenderer(this, 0, 0);
				p4.setRotationPoint(33, 8, 24);
				p4.addBox(0, -1, -1, 30, 2, 2);
				p4.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p4);
				
				ModelRenderer p5 = new ModelRenderer(this, 0, 0);
				p5.addBox(24, 36, 23, 8, 2, 2);
				this.origin.addChild(p5);
				
				ModelRenderer p6 = new ModelRenderer(this, 0, 0);
				p6.setRotationPoint(26, 9.01F, 0);
				p6.addBox(39, -1F, -1, 9, 2, 2);
				p6.rotateAngleY = (float) Math.toRadians(270);
				this.origin.addChild(p6);
				
				ModelRenderer p7 = new ModelRenderer(this, 0, 0);
				p7.addBox(25, 8, 38.5F, 18, 2, 2);
				this.origin.addChild(p7);
				
				ModelRenderer leg1 = new ModelRenderer(this, 0, 0);
//				leg1.addBox(-1F, 1F, -4F, 38, 2, 2);
				leg1.setRotationPoint(56 - 13.6F, 8F, 36F);
				leg1.rotateAngleX = (float) Math.toRadians(-10);
				leg1.rotateAngleY = (float) Math.toRadians(-20);
				leg1.rotateAngleZ = (float) Math.toRadians(-15);
				this.origin.addChild(leg1);
				
				ModelRenderer leg2 = new ModelRenderer(this, 0, 0);
				leg2.addBox(1F, -1F, 3F, 38, 2, 2);
				leg2.rotateAngleZ = (float) Math.toRadians(90);
				leg1.addChild(leg2);
				
				ModelRenderer p8 = new ModelRenderer(this, 0, 0);
				p8.setRotationPoint(53F, 43.3F, 46 - 14.3F);
				p8.addBox(0, 0, 0, 4, 2, 2);
				p8.rotateAngleY = (float) Math.toRadians(-30);
				this.origin.addChild(p8);
				
				ModelRenderer p10 = new ModelRenderer(this, 0, 0);
				p10.setRotationPoint(55f, 43.3f, 33f);
				p10.addBox(0, -2, 0, 6, 2, 2);
				p10.rotateAngleZ = (float) Math.toRadians(90);
				this.origin.addChild(p10);
			}
		}
		
		@Override
		public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha){
			this.origin.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		}
	}
}
