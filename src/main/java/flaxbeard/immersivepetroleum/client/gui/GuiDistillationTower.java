package flaxbeard.immersivepetroleum.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import flaxbeard.immersivepetroleum.common.gui.ContainerDistillationTower;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiDistillationTower extends GuiContainer
{
	TileEntityDistillationTower tile;

	public GuiDistillationTower(InventoryPlayer inventoryPlayer, TileEntityDistillationTower tile)
	{
		super(new ContainerDistillationTower(inventoryPlayer, tile));
		this.tile = tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		this.drawDefaultBackground();
		super.drawScreen(mx, my, partial);
		this.renderHoveredToolTip(mx, my);

		ArrayList<String> tooltip = new ArrayList();
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 62, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivepetroleum:textures/gui/distillation.png", tooltip);


		//ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+112,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersivepetroleum:textures/gui/distillation.png", tooltip);
		if (mx >= guiLeft + 112 && mx <= guiLeft + 112 + 16 && my >= guiTop + 21 && my <= guiTop + 21 + 47)
		{
			float capacity = tile.tanks[1].getCapacity();
			int yy = guiTop + 21 + 47;
			if (tile.tanks[1].getFluidTypes() == 0)
				tooltip.add(I18n.format("gui.immersiveengineering.empty"));
			else
				for (int i = tile.tanks[1].getFluidTypes() - 1; i >= 0; i--)
				{
					FluidStack fs = tile.tanks[1].fluids.get(i);
					if (fs != null && fs.getFluid() != null)
					{
						int fluidHeight = (int) (47 * (fs.amount / capacity));
						yy -= fluidHeight;
						if (my >= yy && my < yy + fluidHeight)
							ClientUtils.addFluidTooltip(fs, tooltip, (int) capacity);
					}
				}
		}

		if (mx > guiLeft + 157 && mx < guiLeft + 164 && my > guiTop + 21 && my < guiTop + 67)
			tooltip.add(tile.getEnergyStored(null) + "/" + tile.getMaxEnergyStored(null) + " RF");

		if (!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft + xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersivepetroleum:textures/gui/distillation.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		//		if(tile.tick>0)
		//		{
		//			int h = (int)(18*(tile.tick/80f));
		//			ClientUtils.drawGradientRect(guiLeft+83,guiTop+34+h, guiLeft+90,guiTop+52, 0xffd4d2ab, 0xffc4c29e);
		//		}

		int stored = (int) (46 * (tile.getEnergyStored(null) / (float) tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft + 158, guiTop + 22 + (46 - stored), guiLeft + 165, guiTop + 68, 0xffb51500, 0xff600b00);

		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 62, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivepetroleum:textures/gui/distillation.png", null);
		//ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+112,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersivepetroleum:textures/gui/distillation.png", null);

		float capacity = tile.tanks[1].getCapacity();
		int yy = guiTop + 21 + 47;
		for (int i = tile.tanks[1].getFluidTypes() - 1; i >= 0; i--)
		{
			FluidStack fs = tile.tanks[1].fluids.get(i);
			if (fs != null && fs.getFluid() != null)
			{
				int fluidHeight = (int) (47 * (fs.amount / capacity));
				yy -= fluidHeight;
				ClientUtils.drawRepeatedFluidSprite(fs, guiLeft + 112, yy, 16, fluidHeight);
			}
		}

		//		if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
		//		{
		//			int h = (int)(47*(tile.tank.getFluid().amount/(float)tile.tank.getCapacity()));
		//			ClientUtils.drawRepeatedFluidIcon(tile.tank.getFluid().getFluid(), guiLeft+98,guiTop+21+47-h, 16, h);
		//			ClientUtils.bindTexture("immersiveengineering:textures/gui/fluidProducer.png");
		//		}
		//		this.drawTexturedModalRect(guiLeft+96,guiTop+19, 177,31, 20,51);
	}
}