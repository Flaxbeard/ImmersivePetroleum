package flaxbeard.immersivepetroleum.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelHydrotreater extends ModelBase
{
	public ModelRenderer base;
	public ModelRenderer part1;
	public ModelRenderer part2;
	public ModelRenderer part3;
	public ModelRenderer part4;
	public ModelRenderer part5;



	public boolean mirror;
	private boolean wasSneaking = false;

	public ModelHydrotreater(boolean mirror)
	{
		this.textureWidth = 16;
		this.textureHeight = 16;
		this.mirror = mirror;
		
		refresh();
	}
	
	public void refresh()
	{		
		this.base = new ModelRenderer(this, 0, 0);
		this.base.addBox(0, 0, 0, 16 * 4, 8, 16 * 3);

		ModelRenderer redControlsF2 = new ModelRenderer(this, 0, 0);
		redControlsF2.addBox(2, 8, mirror ? 34 : 2, 4, 8, 2);
		this.base.addChild(redControlsF2);

		ModelRenderer redControlsF1 = new ModelRenderer(this, 0, 0);
		redControlsF1.addBox(2, 8, mirror ? 44 : 12, 4, 8, 2);
		base.addChild(redControlsF1);

		ModelRenderer redControls = new ModelRenderer(this, 0, 0);
		redControls.addBox(0, 16, mirror ? 32 : 0, 8, 16, 16);
		base.addChild(redControls);

		ModelRenderer tank2 = new ModelRenderer(this, 0, 0);
		tank2.addBox(16, 16, mirror ? 32 : 0, 48, 16, 16);
		base.addChild(tank2);

		ModelRenderer tank1 = new ModelRenderer(this, 0, 0);
		tank1.addBox(16, 18, mirror ? 2 : 18, 47, 28, 28);
		base.addChild(tank1);

		ModelRenderer tank1Base = new ModelRenderer(this, 0, 0);
		tank1Base.addBox(16, 16, mirror ? 1 : 17, 48, 2, 30);
		base.addChild(tank1Base);

		ModelRenderer tank1Leg1 = new ModelRenderer(this, 0, 0);
		tank1Leg1.addBox(59, 8, mirror ? 2 : 18, 4, 8, 4);
		base.addChild(tank1Leg1);

		ModelRenderer tank1Leg2 = new ModelRenderer(this, 0, 0);
		tank1Leg2.addBox(59, 8, mirror ? 26 : 32, 4, 8, 4);
		base.addChild(tank1Leg2);

		ModelRenderer tank1Leg3 = new ModelRenderer(this, 0, 0);
		tank1Leg3.addBox(30, 8, mirror ? 2 : 18, 4, 8, 4);
		base.addChild(tank1Leg3);

		ModelRenderer tank1Leg4 = new ModelRenderer(this, 0, 0);
		tank1Leg4.addBox(30, 8, mirror ? 26 : 32, 4, 8, 4);
		base.addChild(tank1Leg4);

		ModelRenderer heater = new ModelRenderer(this, 0, 0);
		heater.addBox(0, 8, mirror ? 0 : 16, 16, 40, 32);
		base.addChild(heater);
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		if (Minecraft.getMinecraft().player != null)
		{
			if (Minecraft.getMinecraft().player.isSneaking())
			{
				if (!wasSneaking)
				{
					refresh();
				}
				wasSneaking = true;
			}
			else
			{
				wasSneaking = false;
			}
		}

		this.base.render(f5);
	}
}
