package flaxbeard.immersivepetroleum.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelLubricantPipes
{
	public static class Pumpjack extends ModelBase
	{
		private ModelRenderer base;
		
		public Pumpjack()
		{
			this.textureWidth = 16;
			this.textureHeight = 16;
			refresh();
		}
		
		public void refresh()
		{	
			this.base = new ModelRenderer(this, 0, 0);
			this.base.addBox(21, 8, 0, 2, 2, 12);
			
			ModelRenderer p1 = new ModelRenderer(this, 0, 0);
			p1.addBox(21, 8, 12, 17, 2, 2);
			this.base.addChild(p1);
			
			ModelRenderer p2 = new ModelRenderer(this, 0, 0);
			p2.addBox(36, 8, 14, 2, 2, 11);
			this.base.addChild(p2);
			
			ModelRenderer p3 = new ModelRenderer(this, 0, 0);
			p3.addBox(34, 8, 23, 2, 2, 2);
			this.base.addChild(p3);
			
			ModelRenderer p4 = new ModelRenderer(this, 0, 0);
			p4.addBox(32, 8, 23, 2, 30, 2);
			this.base.addChild(p4);
			
			ModelRenderer p5 = new ModelRenderer(this, 0, 0);
			p5.addBox(24, 36, 23, 8, 2, 2);
			this.base.addChild(p5);
			
			ModelRenderer p6 = new ModelRenderer(this, 0, 0);
			p6.addBox(24, 8.01F, 0, 2, 2, 9);
			this.base.addChild(p6);
			
			ModelRenderer p7 = new ModelRenderer(this, 0, 0);
			p7.addBox(24, 8, 8.5F, 19, 2, 2);
			this.base.addChild(p7);
		
			ModelRenderer leg1 = new ModelRenderer(this, 176, 56);
			leg1.addBox(-1F, 1F, -4F, 2, 38, 2);
			leg1.setRotationPoint(56 - 13.6F, 8F, 12F);
			leg1.rotateAngleX = (float) Math.toRadians(10);
			leg1.rotateAngleZ = (float) Math.toRadians(-15);
			leg1.rotateAngleY = (float) Math.toRadians(20);
			this.base.addChild(leg1);
			
			ModelRenderer p8 = new ModelRenderer(this, 0, 0);
			p8.setRotationPoint(52.5F, 43.3F, 14.7F);
			p8.addBox(0, 0, 0, 4, 2, 2);
			p8.rotateAngleY = (float) Math.toRadians(30);
			this.base.addChild(p8);
		
			ModelRenderer p9 = new ModelRenderer(this, 0, 0);
			p9.addBox(55f, 43.3f, 13f, 2, 6, 2);
			this.base.addChild(p9);
		}
		

		boolean wasSneak = false;
		@Override
		public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
		{
			if (Minecraft.getMinecraft().thePlayer != null)
			{
				if (Minecraft.getMinecraft().thePlayer.isSneaking())
				{
					if (!wasSneak)
						refresh();
					wasSneak = true;
				}
				else
				{
					wasSneak = false;
				}
			}
			this.base.render(f5);
		}
	}
}
