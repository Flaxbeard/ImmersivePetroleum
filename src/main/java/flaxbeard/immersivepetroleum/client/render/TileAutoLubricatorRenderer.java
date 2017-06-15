package flaxbeard.immersivepetroleum.client.render;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes.Pumpjack;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityPumpjack;

public class TileAutoLubricatorRenderer extends TileEntitySpecialRenderer<TileEntityAutoLubricator>
{
	private static Pumpjack model = new ModelLubricantPipes.Pumpjack();

	@Override
	public void renderTileEntityAt(TileEntityAutoLubricator te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (te.isDummy() || !te.getWorld().isBlockLoaded(te.getPos(), false))
			return;
		
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);

		RenderHelper.disableStandardItemLighting();

		float scale = .0625f;
		FluidStack fs = te.tank.getFluid();
		fs = new FluidStack(IPContent.fluidLubricant, 10);
		if(fs!=null || true)
		{
			GlStateManager.pushMatrix();
			float level = 1; //fs.amount / (float)te.tank.getCapacity();
			GlStateManager.translate(-4.49F/16F, -1.5/16F, -4.49F/16F);
			GlStateManager.scale(scale, scale, scale);
			float h = level * 23.99F;
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 9, h);
			GlStateManager.rotate(90,0,1,0);
			GlStateManager.translate(-8.98, 0, 0);
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 9, h);
			GlStateManager.rotate(90,0,1,0);
			GlStateManager.translate(-8.98,0,0);
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 9, h);
			GlStateManager.rotate(90,0,1,0);
			GlStateManager.translate(-8.98,0,0);
			ClientUtils.drawRepeatedFluidSprite(fs, 0,0, 9,h);

			GlStateManager.rotate(90,1,0,0);
			GlStateManager.translate(0,0,-h);
			ClientUtils.drawRepeatedFluidSprite(fs, 0,0, 9,9);
			GlStateManager.rotate(180, 1, 0, 0);
			GlStateManager.translate(0, -9, -h);
			ClientUtils.drawRepeatedFluidSprite(fs, 0,0, 9,9);

			GlStateManager.scale(1/scale, 1/scale, 1/scale);
			GlStateManager.translate(0,-1,-1);
			GlStateManager.popMatrix();
		}


		GlStateManager.popMatrix();
		
		BlockPos pos = te.getPos().offset(te.getFacing());
		TileEntity pj = te.getWorld().getTileEntity(pos);
		if (pj instanceof TileEntityPumpjack)
		{
			TileEntityPumpjack base = ((TileEntityPumpjack)pj).master();
			if (base != null)
			{

				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y - 1, z);
				Vec3i offset = base.getPos().subtract(te.getPos());
				GlStateManager.translate(offset.getX(), offset.getY(), offset.getZ());

				EnumFacing rotation = base.facing;
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
				ClientUtils.bindTexture("immersivepetroleum:textures/blocks/lube_pipe12.png");
				model.render(null, 0, 0, 0, 0, 0, 0.0625F);
				GlStateManager.popMatrix();
				

			}
		}
	}
}