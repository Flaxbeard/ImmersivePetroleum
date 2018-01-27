package flaxbeard.immersivepetroleum.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelHydrotreater;
import flaxbeard.immersivepetroleum.client.model.ModelPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityHydrotreater;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MultiblockHydrotreaterRenderer extends TileEntitySpecialRenderer<TileEntityHydrotreater.TileEntityHydrotreaterParent>
{
	private static ModelHydrotreater model = new ModelHydrotreater(false);
	private static ModelHydrotreater modelM = new ModelHydrotreater(true);

	private static String texture = "minecraft:textures/blocks/cobblestone.png";

	@Override
	public void render(TileEntityHydrotreater.TileEntityHydrotreaterParent te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (te != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y - 1, z - 1);

			ClientUtils.bindTexture(texture);
			

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
