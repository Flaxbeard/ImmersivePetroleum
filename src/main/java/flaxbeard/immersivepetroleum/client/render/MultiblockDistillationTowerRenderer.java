package flaxbeard.immersivepetroleum.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;

@SideOnly(Side.CLIENT)
public class MultiblockDistillationTowerRenderer extends TileEntitySpecialRenderer<TileEntityDistillationTower.TileEntityDistillationTowerParent>
{
	private static ModelDistillationTower model = new ModelDistillationTower(false);
	private static ModelDistillationTower modelM = new ModelDistillationTower(true);

	private static String texture = "immersivepetroleum:textures/models/distillation_tower.png";
	private static String textureOn = "immersivepetroleum:textures/models/furnace_hot.png";
	private static String textureM = "immersivepetroleum:textures/models/distillation_tower_m.png";
	private static String textureOnM = "immersivepetroleum:textures/models/furnace_hot_m.png";

	@Override
	public boolean isGlobalRenderer(TileEntityDistillationTower.TileEntityDistillationTowerParent te)
    {
        return true;
    }
	
	@Override
	public void render(TileEntityDistillationTower.TileEntityDistillationTowerParent te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (te != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y - 1, z);


			
			EnumFacing rotation = te.facing;
			if (rotation == EnumFacing.NORTH)
			{
				GlStateManager.rotate(90F, 0, 1, 0);
				GlStateManager.translate(-1, 0, 0);
			}
			else if (rotation == EnumFacing.WEST)
			{
				GlStateManager.rotate(180F, 0, 1, 0);
				GlStateManager.translate(-1, 0, -1);
			}
			else if (rotation == EnumFacing.SOUTH)
			{
				GlStateManager.rotate(270F, 0, 1, 0);
				GlStateManager.translate(0, 0, -1);
			}
			GlStateManager.translate(-3, 0, 0);
			if (te.mirrored)
			{
				GlStateManager.translate(0, 0, -3);
			}
			
			float ticks = Minecraft.getMinecraft().player.ticksExisted + partialTicks;

			if (te.mirrored)
			{
				ClientUtils.bindTexture(textureM);
				modelM.render(null, 0, 0, 0, 0, 0, 0.0625F);
				ClientUtils.bindTexture(te.shouldRenderAsActive() ? textureOnM : textureM);
				modelM.renderFurnace(null, 0, 0, 0, 0, 0, 0.0625F);
			}
			else
			{
				ClientUtils.bindTexture(texture);
				model.render(null, 0, 0, 0, 0, 0, 0.0625F);
				ClientUtils.bindTexture(te.shouldRenderAsActive() ? textureOn : texture);
				model.renderFurnace(null, 0, 0, 0, 0, 0, 0.0625F);
			}
			
			GlStateManager.popMatrix();
			
		}
	}

}
