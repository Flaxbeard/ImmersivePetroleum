package flaxbeard.immersivepetroleum.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCoker extends ModelBase
{
	public ModelRenderer base;
	public boolean mirror;
	
	public ModelCoker(boolean mirror)
	{
		this.textureWidth = 512;
		this.textureHeight = 512;
		
		this.mirror = mirror;
		
		refresh();
	}
	
	public void refresh()
	{		
		this.base = new ModelRenderer(this, 0, 0);
		this.base.addBox(0, 32, 0, 16 * 5, 16, 16 * 9);
		
		ModelRenderer foot1 = new ModelRenderer(this, 0, 0);
		foot1.addBox(0, 0, 0, 16, 32, 16);
		this.base.addChild(foot1);
		
		ModelRenderer foot2 = new ModelRenderer(this, 0, 0);
		foot2.addBox(64, 0, 0, 16, 32, 16);
		this.base.addChild(foot2);
		
		ModelRenderer foot3 = new ModelRenderer(this, 0, 0);
		foot3.addBox(0, 0, 64, 16, 32, 16);
		this.base.addChild(foot3);
		
		ModelRenderer foot4 = new ModelRenderer(this, 0, 0);
		foot4.addBox(64, 0, 64, 16, 32, 16);
		this.base.addChild(foot4);
		
		ModelRenderer foot5 = new ModelRenderer(this, 0, 0);
		foot5.addBox(0, 0, 128, 16, 32, 16);
		this.base.addChild(foot5);
		
		ModelRenderer foot6 = new ModelRenderer(this, 0, 0);
		foot6.addBox(64, 0, 128, 16, 32, 16);
		this.base.addChild(foot6);
		
		
		ModelRenderer tank1 = new ModelRenderer(this, 0, 160);
		tank1.addBox(18, 48, 18, 44, 16 * 10, 44);
		this.base.addChild(tank1);
		
		ModelRenderer tank2 = new ModelRenderer(this, 0, 160);
		tank2.addBox(18, 48, 18 + 4 * 16, 44, 16 * 10, 44);
		this.base.addChild(tank2);
		
		ModelRenderer catwalk11 = new ModelRenderer(this, 160, 0);
		catwalk11.addBox(0, 120, 0, 16 * 5, 0, 16 * 9);
		this.base.addChild(catwalk11);
		
		ModelRenderer catwalk12 = new ModelRenderer(this, 160, 0);
		catwalk12.addBox(0, 128, 0, 16 * 5, 0, 16 * 9);
		this.base.addChild(catwalk12);
		
		ModelRenderer catwalk13 = new ModelRenderer(this, 132, 160);
		catwalk13.addBox(0, 120, 0, 16 * 5, 8, 0);
		this.base.addChild(catwalk13);
		
		ModelRenderer catwalk14 = new ModelRenderer(this, 132, 160);
		catwalk14.addBox(0, 120, 16 * 9, 16 * 5, 8, 0);
		this.base.addChild(catwalk14);
		
		ModelRenderer catwalk15 = new ModelRenderer(this, 132, 160-144);
		catwalk15.addBox(0, 120, 0, 0, 8, 16*9);
		this.base.addChild(catwalk15);
		
		ModelRenderer catwalk16 = new ModelRenderer(this, 132, 160-144);
		catwalk16.addBox(16 * 5, 120, 0, 0, 8, 16*9);
		this.base.addChild(catwalk16);
		
		ModelRenderer catwalk21 = new ModelRenderer(this, 160, 0);
		catwalk21.addBox(0, 200 - 0.01F, 0, 16 * 5, 0, 16 * 9);
		this.base.addChild(catwalk21);
		
		ModelRenderer catwalk22 = new ModelRenderer(this, 160, 0);
		catwalk22.addBox(0, 208 - 0.01F, 0, 16 * 5, 0, 16 * 9);
		this.base.addChild(catwalk22);
		
		ModelRenderer catwalk23 = new ModelRenderer(this, 132, 160);
		catwalk23.addBox(0, 200, 0, 16 * 5, 8, 0);
		this.base.addChild(catwalk23);
		
		ModelRenderer catwalk24 = new ModelRenderer(this, 132, 160);
		catwalk24.addBox(0, 200, 16 * 9, 16 * 5, 8, 0);
		this.base.addChild(catwalk24);
		
		ModelRenderer catwalk25 = new ModelRenderer(this, 132, 160-144);
		catwalk25.addBox(0, 200, 0, 0, 8, 16*9);
		this.base.addChild(catwalk25);
		
		ModelRenderer catwalk26 = new ModelRenderer(this, 132, 160-144);
		catwalk26.addBox(16 * 5, 200, 0, 0, 8, 16*9);
		this.base.addChild(catwalk26);
		
		
		ModelRenderer post1 = new ModelRenderer(this, 464, 0);
		post1.addBox(2, 48, 2, 4, 16 * 10, 4);
		this.base.addChild(post1);
		
		ModelRenderer post2 = new ModelRenderer(this, 464, 0);
		post2.addBox(74, 48, 2, 4, 16 * 10, 4);
		this.base.addChild(post2);
		
		ModelRenderer post3 = new ModelRenderer(this, 464, 0);
		post3.addBox(2, 48, 70, 4, 16 * 10, 4);
		this.base.addChild(post3);
		
		ModelRenderer post4 = new ModelRenderer(this, 464, 0);
		post4.addBox(74, 48, 70, 4, 16 * 10, 4);
		this.base.addChild(post4);
		
		ModelRenderer post5 = new ModelRenderer(this, 464, 0);
		post5.addBox(2, 48, 138, 4, 16 * 10, 4);
		this.base.addChild(post5);
		
		ModelRenderer post6 = new ModelRenderer(this, 464, 0);
		post6.addBox(74, 48, 138, 4, 16 * 10, 4);
		this.base.addChild(post6);
		
		ModelRenderer catwalk31 = new ModelRenderer(this, 184-112, 168);
		catwalk31.addBox(16, 56 + 16 * 14, 16, 16 * 3, 0, 16 * 7);
		this.base.addChild(catwalk31);
		
		ModelRenderer catwalk32 = new ModelRenderer(this, 184-112, 168);
		catwalk32.addBox(16, 56 + 16 * 14 + 8, 16, 16 * 3, 0, 16 * 7);
		this.base.addChild(catwalk32);
		
		ModelRenderer catwalk33 = new ModelRenderer(this, 132, 160);
		catwalk33.addBox(16, 56 + 16 * 14, 16, 16 * 3, 8, 0);
		this.base.addChild(catwalk33);
		
		ModelRenderer catwalk34 = new ModelRenderer(this, 132, 160);
		catwalk34.addBox(16, 56 + 16 * 14, 16 * 8, 16 * 3, 8, 0);
		this.base.addChild(catwalk34);
		
		ModelRenderer catwalk35 = new ModelRenderer(this, 132, 160-(16*7));
		catwalk35.addBox(16, 56 + 16 * 14, 16, 0, 8, 16*7);
		this.base.addChild(catwalk35);
		
		ModelRenderer catwalk36 = new ModelRenderer(this, 132, 160-(16*7));
		catwalk36.addBox(16 * 4, 56 + 16 * 14, 16, 0, 8, 16*7);
		this.base.addChild(catwalk36);
		
		ModelRenderer pipe1 = new ModelRenderer(this, 480, 0);
		pipe1.addBox(36, 16 * 13, 48, 8, 16 * 10, 8);
		this.base.addChild(pipe1);
		
		ModelRenderer pipe2 = new ModelRenderer(this, 480, 0);
		pipe2.addBox(36, 16 * 13, 40 + 48, 8, 16 * 10, 8);
		this.base.addChild(pipe2);
		
		ModelRenderer crosspost1 = new ModelRenderer(this, 44, 232);
		crosspost1.addBox(2.001F, 82, 6, 4, 4, 132);
		this.base.addChild(crosspost1);
		
		ModelRenderer crosspost2 = new ModelRenderer(this, 44, 232);
		crosspost2.addBox(73.999F, 82, 6, 4, 4, 132);
		this.base.addChild(crosspost2);
	
		ModelRenderer crosspost3 = new ModelRenderer(this, 0, 136);
		crosspost3.addBox(6, 82, 2, 68, 4, 4);
		this.base.addChild(crosspost3);
		
		ModelRenderer crosspost4 = new ModelRenderer(this, 0, 136);
		crosspost4.addBox(6, 82, 138, 68, 4, 4);
		this.base.addChild(crosspost4);
		
		
		ModelRenderer crosspost5 = new ModelRenderer(this, 44, 232);
		crosspost5.addBox(2.001F, 162, 6, 4, 4, 132);
		this.base.addChild(crosspost5);
		
		ModelRenderer crosspost6 = new ModelRenderer(this, 44, 232);
		crosspost6.addBox(73.999F, 162, 6, 4, 4, 132);
		this.base.addChild(crosspost6);
	
		ModelRenderer crosspost7 = new ModelRenderer(this, 0, 136);
		crosspost7.addBox(6, 162, 2, 68, 4, 4);
		this.base.addChild(crosspost7);
		
		ModelRenderer crosspost8 = new ModelRenderer(this, 0, 136);
		crosspost8.addBox(6, 162, 138, 68, 4, 4);
		this.base.addChild(crosspost8);
		
		float deg = 4;
		
		ModelRenderer tS11 = new ModelRenderer(this, 464, 0);
		tS11.addBox(-2, 0, -2, 4, 16 * 10, 4);
		tS11.setRotationPoint(22, 16 * 13, (16 * 9) / 2F);
		tS11.rotateAngleZ = (float) Math.toRadians(-deg);
		this.base.addChild(tS11);
		
		ModelRenderer tS12 = new ModelRenderer(this, 464, 0);
		tS12.addBox(-2, 0, -2 -40, 4, 16 * 10, 4);
		tS11.addChild(tS12);

		ModelRenderer tS13 = new ModelRenderer(this, 464, 0);
		tS13.addBox(-2, 0, -2 + 40, 4, 16 * 10, 4);
		tS11.addChild(tS13);
		
		ModelRenderer tS21 = new ModelRenderer(this, 464, 0);
		tS21.addBox(-2, 0, -2, 4, 16 * 10, 4);
		tS21.setRotationPoint(16 * 5 - 22, 16 * 13, (16 * 9) / 2F);
		tS21.rotateAngleZ = (float) Math.toRadians(deg);
		this.base.addChild(tS21);
		
		ModelRenderer tS22 = new ModelRenderer(this, 464, 0);
		tS22.addBox(-2, 0, -2 -40, 4, 16 * 10, 4);
		tS21.addChild(tS22);

		ModelRenderer tS23 = new ModelRenderer(this, 464, 0);
		tS23.addBox(-2, 0, -2 + 40, 4, 16 * 10, 4);
		tS21.addChild(tS23);
		
		ModelRenderer crosspostTop1 = new ModelRenderer(this, 464, 0);
		crosspostTop1.addBox(-1.99F, -38F, 2.25F * 16, 4, 76, 4);
		crosspostTop1.rotateAngleX = (float) -Math.toRadians(90);
		tS11.addChild(crosspostTop1);
		
		ModelRenderer crosspostTop2 = new ModelRenderer(this, 464, 0);
		crosspostTop2.addBox(-2.01F, -38F, 2.25F * 16, 4, 76, 4);
		crosspostTop2.rotateAngleX = (float) -Math.toRadians(90);
		tS21.addChild(crosspostTop2);
		
		ModelRenderer crosspostTop3 = new ModelRenderer(this, 464, 0);
		crosspostTop3.addBox(-1.99F, -38F, 7.25F * 16, 4, 76, 4);
		crosspostTop3.rotateAngleX = (float) -Math.toRadians(90);
		tS11.addChild(crosspostTop3);
		
		ModelRenderer crosspostTop4 = new ModelRenderer(this, 464, 0);
		crosspostTop4.addBox(-2.01F, -38F, 7.25F * 16, 4, 76, 4);
		crosspostTop4.rotateAngleX = (float) -Math.toRadians(90);
		tS21.addChild(crosspostTop4);
		
		ModelRenderer crosspostTop5 = new ModelRenderer(this, 464, 0);
		crosspostTop5.addBox(-1.99F, -38F, 10F * 16 - 4, 4, 76, 4);
		crosspostTop5.rotateAngleX = (float) -Math.toRadians(90);
		tS11.addChild(crosspostTop5);
		
		ModelRenderer crosspostTop6 = new ModelRenderer(this, 464, 0);
		crosspostTop6.addBox(-2.01F, -38F, 10F * 16 - 4, 4, 76, 4);
		crosspostTop6.rotateAngleX = (float) -Math.toRadians(90);
		tS21.addChild(crosspostTop6);
		
		ModelRenderer crosspostTop7 = new ModelRenderer(this, 464, 0);
		crosspostTop1.addBox(-1.99F, -64F, 0, 4, 128, 4);
		crosspostTop1.rotateAngleX = (float) -Math.toRadians(90);
		tS11.addChild(crosspostTop1);
		
		ModelRenderer crosspostTop8 = new ModelRenderer(this, 464, 0);
		crosspostTop8.addBox(-2.01F, -64F, 0, 4, 128, 4);
		crosspostTop8.rotateAngleX = (float) -Math.toRadians(90);
		tS21.addChild(crosspostTop8);
		
		ModelRenderer crosspostTop9 = new ModelRenderer(this, 464, 0);
		crosspostTop1.addBox(-1.99F, -38F, (5 * 16 - 6), 4, 76, 4);
		crosspostTop1.rotateAngleX = (float) -Math.toRadians(90);
		tS11.addChild(crosspostTop1);
		
		ModelRenderer crosspostTop10 = new ModelRenderer(this, 464, 0);
		crosspostTop10.addBox(-2.01F, -38F, (5 * 16 - 6), 4, 76, 4);
		crosspostTop10.rotateAngleX = (float) -Math.toRadians(90);
		tS21.addChild(crosspostTop10);
		
		ModelRenderer crosspostTopSide11 = new ModelRenderer(this, 464, 0);
		crosspostTopSide11.addBox(30.01F, 40F - 28F, 16 * 13, 4, 56, 4);
		crosspostTopSide11.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide11.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide11);

		ModelRenderer crosspostTopSide12 = new ModelRenderer(this, 464, 0);
		crosspostTopSide12.addBox(30.01F + 40F, 40F - 28F, 16 * 13, 4, 56, 4);
		crosspostTopSide12.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide12.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide12);

		ModelRenderer crosspostTopSide13 = new ModelRenderer(this, 464, 0);
		crosspostTopSide13.addBox(30.01F + 80, 40F - 28F, 16 * 13, 4, 56, 4);
		crosspostTopSide13.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide13.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide13);
		
		float mod = (float) Math.cos(Math.toRadians(deg));
		ModelRenderer crosspostTopSide21 = new ModelRenderer(this, 464, 0);
		crosspostTopSide21.addBox(30.01F, 40F - 16F, 16 * 13 + (2.25F * 16 * mod), 4, 32, 4);
		crosspostTopSide21.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide21.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide21);

		ModelRenderer crosspostTopSide22 = new ModelRenderer(this, 464, 0);
		crosspostTopSide22.addBox(30.01F + 40F, 40F - 16F, 16 * 13 + (2.25F * 16 * mod), 4, 32, 4);
		crosspostTopSide22.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide22.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide22);

		ModelRenderer crosspostTopSide23 = new ModelRenderer(this, 464, 0);
		crosspostTopSide23.addBox(30.01F + 80, 40F - 16F, 16 * 13 + (2.25F * 16 * mod), 4, 32, 4);
		crosspostTopSide23.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide23.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide23);
		
		ModelRenderer crosspostTopSide31 = new ModelRenderer(this, 464, 0);
		crosspostTopSide31.addBox(30.01F, 40F - 9F, 16 * 13 + (7.25F * 16 * mod), 4, 18, 4);
		crosspostTopSide31.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide31.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide31);

		ModelRenderer crosspostTopSide32 = new ModelRenderer(this, 464, 0);
		crosspostTopSide32.addBox(30.01F + 40F, 40F - 9F, 16 * 13 + (7.25F * 16 * mod), 4, 18, 4);
		crosspostTopSide32.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide32.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide32);

		ModelRenderer crosspostTopSide33 = new ModelRenderer(this, 464, 0);
		crosspostTopSide33.addBox(30.01F + 80, 40F - 9F, 16 * 13 + (7.25F * 16 * mod), 4, 18, 4);
		crosspostTopSide33.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide33.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide33);
		
		ModelRenderer crosspostTopSide41 = new ModelRenderer(this, 464, 0);
		crosspostTopSide41.addBox(30.01F, 40F - 9F, 16 * 13 + ((10F * 16 - 4) * mod), 4, 18, 4);
		crosspostTopSide41.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide41.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide41);

		ModelRenderer crosspostTopSide42 = new ModelRenderer(this, 464, 0);
		crosspostTopSide42.addBox(30.01F + 40F, 40F - 9F, 16 * 13 + ((10F * 16 - 4) * mod), 4, 18, 4);
		crosspostTopSide42.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide42.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide42);

		ModelRenderer crosspostTopSide43 = new ModelRenderer(this, 464, 0);
		crosspostTopSide43.addBox(30.01F + 80, 40F - 9F, 16 * 13 + ((10F * 16 - 4) * mod), 4, 18, 4);
		crosspostTopSide43.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide43.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide43);
		
		ModelRenderer crosspostTopSide51 = new ModelRenderer(this, 464, 0);
		crosspostTopSide51.addBox(30.01F, 40F - 12F, 16 * 13 + ((5 * 16 - 6)  * mod), 4, 24, 4);
		crosspostTopSide51.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide51.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide51);

		ModelRenderer crosspostTopSide52 = new ModelRenderer(this, 464, 0);
		crosspostTopSide52.addBox(30.01F + 40F, 40F - 12F, 16 * 13 + ((5 * 16 - 6)  * mod), 4, 24, 4);
		crosspostTopSide52.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide52.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide52);

		ModelRenderer crosspostTopSide53 = new ModelRenderer(this, 464, 0);
		crosspostTopSide53.addBox(30.01F + 80, 40F - 12F, 16 * 13 + ((5 * 16 - 6) * mod), 4, 24, 4);
		crosspostTopSide53.rotateAngleX = (float) -Math.toRadians(90);
		crosspostTopSide53.rotateAngleY = (float) -Math.toRadians(90);
		base.addChild(crosspostTopSide53);

	}	

	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		if (Minecraft.getMinecraft().thePlayer != null)
		{
			if (Minecraft.getMinecraft().thePlayer.isSneaking())
			{
				refresh();
			}
		}
		this.base.render(f5);
	}
	
}
