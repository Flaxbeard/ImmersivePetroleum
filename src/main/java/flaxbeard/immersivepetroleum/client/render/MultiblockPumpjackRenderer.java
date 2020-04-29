package flaxbeard.immersivepetroleum.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MultiblockPumpjackRenderer extends TileEntitySpecialRenderer<TileEntityPumpjack.TileEntityPumpjackParent>
{
	private static ModelPumpjack model = new ModelPumpjack(false);
	private static ModelPumpjack modelM = new ModelPumpjack(true);

	private static String texture = "immersivepetroleum:textures/models/pumpjack.png";

	@Override
	public void render(TileEntityPumpjack.TileEntityPumpjackParent te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
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
			GlStateManager.translate(-1, 0, -1);

			if (te.mirrored)
			{
			}

			ClientUtils.bindTexture(texture);

			float ticks = te.activeTicks + (te.wasActive ? partialTicks : 0);
			;
			model.ticks = modelM.ticks = 1.5F * ticks;

			if (te.mirrored)
			{
				modelM.render(null, 0, 0, 0, 0, 0, 0.0625F);
			}
			else
			{
				model.render(null, 0, 0, 0, 0, 0, 0.0625F);
			}
			GlStateManager.popMatrix();

		}
	}

}
