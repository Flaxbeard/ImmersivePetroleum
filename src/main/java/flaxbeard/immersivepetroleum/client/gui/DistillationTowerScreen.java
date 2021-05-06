package flaxbeard.immersivepetroleum.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.gui.DistillationTowerContainer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class DistillationTowerScreen extends IEContainerScreen<DistillationTowerContainer>{
	static final ResourceLocation GUI_TEXTURE = new ResourceLocation("immersivepetroleum", "textures/gui/distillation.png");
	
	DistillationTowerTileEntity tile;
	
	public DistillationTowerScreen(DistillationTowerContainer container, PlayerInventory playerInventory, ITextComponent title){
		super(container, playerInventory, title);
		this.tile = container.tile;
	}
	
	@Override
	public void render(MatrixStack matrix, int mx, int my, float partialTicks){
		this.renderBackground(matrix);
		super.render(matrix, mx, my, partialTicks);
		this.renderHoveredTooltip(matrix, mx, my);
		
		List<ITextComponent> tooltip = new ArrayList<>();
		GuiHelper.handleGuiTank(matrix, tile.tanks[0], guiLeft + 62, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, GUI_TEXTURE, tooltip);
		
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
						if(my >= yy && my < yy + fluidHeight)
							GuiHelper.addFluidTooltip(fs, tooltip, (int) capacity);
					}
				}
			}
		}
		
		if(mx > guiLeft + 157 && mx < guiLeft + 164 && my > guiTop + 21 && my < guiTop + 67)
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null) + "/" + tile.getMaxEnergyStored(null) + " RF"));
		
		if(!tooltip.isEmpty()){
			GuiUtils.drawHoveringText(matrix, tooltip, mx, my, width, height, -1, font);
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float f, int mx, int my){
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture(GUI_TEXTURE);
		this.blit(matrix, guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int stored = (int) (46 * (tile.getEnergyStored(null) / (float) tile.getMaxEnergyStored(null)));
		fillGradient(matrix, guiLeft + 158, guiTop + 22 + (46 - stored), guiLeft + 165, guiTop + 68, 0xffb51500, 0xff600b00);
		
		GuiHelper.handleGuiTank(matrix, tile.tanks[0], guiLeft + 62, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, GUI_TEXTURE, null);
		
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		float capacity = tile.tanks[1].getCapacity();
		int yy = guiTop + 21 + 47;
		for(int i = tile.tanks[1].getFluidTypes() - 1;i >= 0;i--){
			FluidStack fs = tile.tanks[1].fluids.get(i);
			if(fs != null && fs.getFluid() != null){
				int fluidHeight = (int) (47 * (fs.getAmount() / capacity));
				yy -= fluidHeight;
				GuiHelper.drawRepeatedFluidSpriteGui(buffers, matrix, fs, guiLeft + 112, yy, 16, fluidHeight);
			}
		}
		buffers.finish();
	}
}
