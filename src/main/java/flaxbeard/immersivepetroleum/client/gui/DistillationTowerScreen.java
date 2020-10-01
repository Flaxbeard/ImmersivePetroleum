package flaxbeard.immersivepetroleum.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.gui.DistillationTowerContainer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class DistillationTowerScreen extends IEContainerScreen<DistillationTowerContainer>{
	DistillationTowerTileEntity tile;
	
	public DistillationTowerScreen(DistillationTowerContainer container, PlayerInventory playerInventory, ITextComponent title){
		super(container, playerInventory, title);
		this.tile = container.tile;
	}
	
	@Override
	public void render(MatrixStack transform, int mx, int my, float partialTicks){
		this.renderBackground(transform);
		super.render(transform, mx, my, partialTicks);
		this.renderHoveredTooltip(transform, mx, my);
		
		List<ITextComponent> tooltip = new ArrayList<>();
		ClientUtils.handleGuiTank(transform, tile.tanks[0], guiLeft + 62, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivepetroleum:textures/gui/distillation.png", tooltip);
		
//		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft + 112, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivepetroleum:textures/gui/distillation.png", tooltip);
		if(mx >= guiLeft + 112 && mx <= guiLeft + 112 + 16 && my >= guiTop + 21 && my <= guiTop + 21 + 47){
			float capacity = tile.tanks[1].getCapacity();
			int yy = guiTop + 21 + 47;
			if(tile.tanks[1].getFluidTypes() == 0){
				tooltip.add(new TranslationTextComponent("gui.immersiveengineering.empty"));
			}else{
				for(int i = tile.tanks[1].getFluidTypes() - 1;i >= 0;i--){
					FluidStack fs = tile.tanks[1].fluids.get(i);
					if(fs != null && fs.getFluid() != null){
						int fluidHeight = (int) (47 * (fs.getAmount() / capacity));
						yy -= fluidHeight;
						if(my >= yy && my < yy + fluidHeight) ClientUtils.addFluidTooltip(fs, tooltip, (int) capacity);
					}
				}
			}
		}
		
		if(mx > guiLeft + 157 && mx < guiLeft + 164 && my > guiTop + 21 && my < guiTop + 67) tooltip.add(new StringTextComponent(tile.getEnergyStored(null) + "/" + tile.getMaxEnergyStored(null) + " RF"));
		
		if(!tooltip.isEmpty()){
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float f, int mx, int my){
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersivepetroleum:textures/gui/distillation.png");
		this.blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
//		if(tile.tick > 0){
//			int h = (int) (18 * (tile.tick / 80f));
//			ClientUtils.drawGradientRect(guiLeft + 83, guiTop + 34 + h, guiLeft + 90, guiTop + 52, 0xffd4d2ab, 0xffc4c29e);
//		}
		
		int stored = (int) (46 * (tile.getEnergyStored(null) / (float) tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft + 158, guiTop + 22 + (46 - stored), guiLeft + 165, guiTop + 68, 0xffb51500, 0xff600b00);
		
		ClientUtils.handleGuiTank(matrixStack, tile.tanks[0], guiLeft + 62, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivepetroleum:textures/gui/distillation.png", null);
		
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		float capacity = tile.tanks[1].getCapacity();
		int yy = guiTop + 21 + 47;
		for(int i = tile.tanks[1].getFluidTypes() - 1;i >= 0;i--){
			FluidStack fs = tile.tanks[1].fluids.get(i);
			if(fs != null && fs.getFluid() != null){
				int fluidHeight = (int) (47 * (fs.getAmount() / capacity));
				yy -= fluidHeight;
				ClientUtils.drawRepeatedFluidSpriteGui(buffers, matrixStack, fs, guiLeft + 112, yy, 16, fluidHeight);
			}
		}
		buffers.finish();
		
//		if(tile.tank.getFluid() != null && tile.tank.getFluid().getFluid() != null){
//			int h = (int) (47 * (tile.tank.getFluid().amount / (float) tile.tank.getCapacity()));
//			ClientUtils.drawRepeatedFluidIcon(tile.tank.getFluid().getFluid(), guiLeft + 98, guiTop + 21 + 47 - h, 16, h);
//			ClientUtils.bindTexture("immersiveengineering:textures/gui/fluidProducer.png");
//		}
//		this.drawTexturedModalRect(guiLeft + 96, guiTop + 19, 177, 31, 20, 51);
	}
}