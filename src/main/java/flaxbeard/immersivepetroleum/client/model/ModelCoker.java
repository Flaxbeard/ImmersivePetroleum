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
		this.textureWidth = 16;
		this.textureHeight = 16;
		
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
		
		
		ModelRenderer tank1 = new ModelRenderer(this, 0, 0);
		tank1.addBox(17, 48, 17, 46, 16 * 10, 46);
		this.base.addChild(tank1);
		
		ModelRenderer tank2 = new ModelRenderer(this, 0, 0);
		tank2.addBox(17, 48, 17 + 4 * 16, 46, 16 * 10, 46);
		this.base.addChild(tank2);
		
		ModelRenderer catwalk1 = new ModelRenderer(this, 0, 0);
		catwalk1.addBox(0, 56 + 16 * 4, 0, 16 * 5, 8, 16 * 9);
		this.base.addChild(catwalk1);
		
		ModelRenderer catwalk2 = new ModelRenderer(this, 0, 0);
		catwalk2.addBox(0, 56 + 16 * 9 - 0.01F, 0, 16 * 5, 8, 16 * 9);
		this.base.addChild(catwalk2);
		
		ModelRenderer post1 = new ModelRenderer(this, 0, 0);
		post1.addBox(2, 48, 2, 4, 16 * 10, 4);
		this.base.addChild(post1);
		
		ModelRenderer post2 = new ModelRenderer(this, 0, 0);
		post2.addBox(74, 48, 2, 4, 16 * 10, 4);
		this.base.addChild(post2);
		
		ModelRenderer post3 = new ModelRenderer(this, 0, 0);
		post3.addBox(2, 48, 70, 4, 16 * 10, 4);
		this.base.addChild(post3);
		
		ModelRenderer post4 = new ModelRenderer(this, 0, 0);
		post4.addBox(74, 48, 70, 4, 16 * 10, 4);
		this.base.addChild(post4);
		
		ModelRenderer post5 = new ModelRenderer(this, 0, 0);
		post5.addBox(2, 48, 138, 4, 16 * 10, 4);
		this.base.addChild(post5);
		
		ModelRenderer post6 = new ModelRenderer(this, 0, 0);
		post6.addBox(74, 48, 138, 4, 16 * 10, 4);
		this.base.addChild(post6);
		
		ModelRenderer catwalk3 = new ModelRenderer(this, 0, 0);
		catwalk3.addBox(16, 56 + 16 * 14, 16, 16 * 3, 8, 16 * 7);
		this.base.addChild(catwalk3);
		
		ModelRenderer pipe1 = new ModelRenderer(this, 0, 0);
		pipe1.addBox(36, 16 * 13, 44, 8, 16 * 10, 8);
		this.base.addChild(pipe1);
		
		ModelRenderer pipe2 = new ModelRenderer(this, 0, 0);
		pipe2.addBox(36, 16 * 13, 44 + 48, 8, 16 * 10, 8);
		this.base.addChild(pipe2);
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
