package flaxbeard.immersivepetroleum.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelCoker;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityCoker;

public class MultiblockCokerRenderer extends TileEntitySpecialRenderer<TileEntityCoker.TileEntityCokerParent>
{
	private static ModelCoker model = new ModelCoker(false);

	private static String texture = "immersivepetroleum:textures/models/coker.png";

	@Override
	public boolean isGlobalRenderer(TileEntityCoker.TileEntityCokerParent te)
    {
        return true;
    }
	
	@Override
	public void renderTileEntityAt(TileEntityCoker.TileEntityCokerParent te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (te != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y - 1, z);

			
			EnumFacing rotation = te.facing;
			
			float ticks = Minecraft.getMinecraft().thePlayer.ticksExisted + partialTicks;

			if (rotation == EnumFacing.SOUTH)
			{
				GlStateManager.rotate(270F, 0, 1, 0);
				GlStateManager.translate(0, 0, -5);
			}

			ClientUtils.bindTexture(texture);
			model.render(null, 0, 0, 0, 0, 0, 0.0625F);

			GlStateManager.popMatrix();
			
		}
	}

}
