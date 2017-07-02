package flaxbeard.immersivepetroleum.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IMultipassModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.immersivepetroleum.common.entity.EntitySpeedboat;

@SideOnly(Side.CLIENT)
public class ModelSpeedboat extends ModelBase implements IMultipassModel
{
    public ModelRenderer[] boatSides = new ModelRenderer[5];
    /** Part of the model rendered to make it seem like there's no water in the boat */
    public ModelRenderer noWater;
    private final int patchList = GLAllocation.generateDisplayLists(1);
    public ModelRenderer motor;
    public ModelRenderer propeller;
    public ModelRenderer propellerAssembly;

    public ModelSpeedboat()
    {
        this.boatSides[0] = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
        this.boatSides[1] = (new ModelRenderer(this, 0, 19)).setTextureSize(128, 64);
        this.boatSides[2] = (new ModelRenderer(this, 0, 27)).setTextureSize(128, 64);
        this.boatSides[3] = (new ModelRenderer(this, 0, 35)).setTextureSize(128, 64);
        this.boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
        int i = 32;
        int j = 6;
        int k = 20;
        int l = 4;
        int i1 = 28;
        this.boatSides[0].addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
        this.boatSides[0].setRotationPoint(0.0F, 3.0F, 1.0F);
        this.boatSides[1].addBox(-13.0F, -7.0F, -1.0F, 18, 6, 2, 0.0F);
        this.boatSides[1].setRotationPoint(-15.0F, 4.0F, 4.0F);
        this.boatSides[2].addBox(-8.0F, -7.0F, -1.0F, 16, 6, 2, 0.0F);
        this.boatSides[2].setRotationPoint(15.0F, 4.0F, 0.0F);
        this.boatSides[3].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
        this.boatSides[3].setRotationPoint(0.0F, 4.0F, -9.0F);
        this.boatSides[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
        this.boatSides[4].setRotationPoint(0.0F, 4.0F, 9.0F);
        this.boatSides[0].rotateAngleX = ((float)Math.PI / 2F);
        this.boatSides[1].rotateAngleY = ((float)Math.PI * 3F / 2F);
        this.boatSides[2].rotateAngleY = ((float)Math.PI / 2F);
        this.boatSides[3].rotateAngleY = (float)Math.PI;
       
        this.noWater = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
        this.noWater.addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
        this.noWater.setRotationPoint(0.0F, -3.0F, 1.0F);
        this.noWater.rotateAngleX = ((float)Math.PI / 2F);
        refresh();
    }
    
    public void refresh()
    {

    	motor = new ModelRenderer(this, 104, 0).setTextureSize(128, 64);
    	motor.addBox(-19.0F, -8.0F, -3, 6, 5, 6, 0.0F);
    	
    	propellerAssembly = new ModelRenderer(this, 96, 0).setTextureSize(128, 64);
    	propellerAssembly.setRotationPoint(-17F, 5F, 0);
    	
    	propellerAssembly.addBox(-1, -8.1F, -1, 2, 10, 2, 0.0F);
    	
    	ModelRenderer handle = new ModelRenderer(this, 72, 0).setTextureSize(128, 64);
    	handle.addBox(4F, -9.7F, -0.5F, 6, 1, 1);
    	handle.rotateAngleX = 0;
    	handle.rotateAngleZ = (float) Math.toRadians(-5);
    	propellerAssembly.addChild(handle);
    	
    	
     	propeller = new ModelRenderer(this, 86, 0).setTextureSize(128, 64);
     	propeller.addBox(-1F, -1F, -1F, 3, 2, 2, 0.0F);
     	propeller.setRotationPoint(-3F, 0, 0);
     	propellerAssembly.addChild(propeller);
     	
    	ModelRenderer propeller1 = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
     	propeller1.addBox(0F, 0F, -1F, 1, 4, 2, 0.0F);
     	propeller1.rotateAngleY = (float) Math.toRadians(15);
     	propeller.addChild(propeller1);
     	

     	ModelRenderer propeller2B = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
     	propeller.addChild(propeller2B);
     	propeller2B.rotateAngleX = (float) Math.toRadians(360F / 3F);

     	ModelRenderer propeller2 = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
     	propeller2.addBox(0F, 0F, -1F, 1, 4, 2, 0.0F);
     	propeller2.rotateAngleY = (float) Math.toRadians(15);
     	propeller2B.addChild(propeller2);
     	
     	ModelRenderer propeller3B = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
     	propeller.addChild(propeller3B);
     	propeller3B.rotateAngleX = (float) Math.toRadians(2 * 360F / 3F);
     	
     	ModelRenderer propeller3 = new ModelRenderer(this, 90, 4).setTextureSize(128, 64);
     	propeller3.addBox(0F, 0F, -1F, 1, 4, 2, 0.0F);
     	propeller3.rotateAngleY = (float) Math.toRadians(15);
     	propeller3B.addChild(propeller3);
       // this.boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
        //this.boatSides[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
        //this.boatSides[4].setRotationPoint(0.0F, 4.0F, 9.0F);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
	boolean wasSneak = false;

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        EntitySpeedboat entityboat = (EntitySpeedboat) entityIn;
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

        for (int i = 0; i < 5; ++i)
        {
            this.boatSides[i].render(scale);
        }
        
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
        
    
        float f1 = ((EntitySpeedboat) entityIn).getRowingTime(0, limbSwing) * 100.0F;
        this.propeller.rotateAngleX = f1;
        float pr = entityboat.propellerRotation;
        if (entityboat.leftInputDown && pr > -1) pr = pr - 0.1F * Minecraft.getMinecraft().getRenderPartialTicks();
        if (entityboat.rightInputDown && pr < 1) pr = pr + 0.1F * Minecraft.getMinecraft().getRenderPartialTicks();
        if (!entityboat.leftInputDown && !entityboat.rightInputDown) pr = (float) (pr * Math.pow(0.7, Minecraft.getMinecraft().getRenderPartialTicks()));
        this.propellerAssembly.rotateAngleY = (float) Math.toRadians(pr * 15);
        this.propellerAssembly.render(scale);

        if (entityboat.isBeingRidden())
        	GlStateManager.translate((entityIn.worldObj.rand.nextFloat() - 0.5F) * 0.01F, (entityIn.worldObj.rand.nextFloat() - 0.5F) * 0.01F, (entityIn.worldObj.rand.nextFloat() - 0.5F) * 0.01F);
        this.motor.render(scale);

    }

    public void renderMultipass(Entity p_187054_1_, float p_187054_2_, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale)
    {
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.colorMask(false, false, false, false);
        this.noWater.render(scale);
        GlStateManager.colorMask(true, true, true, true);
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
    }


}