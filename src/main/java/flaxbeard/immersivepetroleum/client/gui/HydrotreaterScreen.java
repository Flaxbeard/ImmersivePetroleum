package flaxbeard.immersivepetroleum.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.HydrotreaterTileEntity;
import flaxbeard.immersivepetroleum.common.gui.HydrotreaterContainer;
import flaxbeard.immersivepetroleum.common.util.MCUtil;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class HydrotreaterScreen extends IEContainerScreen<HydrotreaterContainer>{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/hydrotreater.png");
	
	HydrotreaterTileEntity tile;
	public HydrotreaterScreen(HydrotreaterContainer inventorySlotsIn, PlayerInventory inv, ITextComponent title){
		super(inventorySlotsIn, inv, title);
		this.tile = this.container.tile;
		
		this.xSize = 140;
		this.ySize = 69;
	}
	
	@Override
	public void render(MatrixStack matrix, int mx, int my, float partialTicks){
		this.renderBackground(matrix);
		super.render(matrix, mx, my, partialTicks);
//		this.renderHoveredTooltip(matrix, mx, my); // Not needed
		
		List<ITextComponent> tooltip = new ArrayList<>();
		
		// Tank displays
		GuiHelper.handleGuiTank(matrix, this.tile.tanks[HydrotreaterTileEntity.TANK_INPUT_A], this.guiLeft + 34, this.guiTop + 11, 16, 47, 0, 0, 0, 0, mx, my, GUI_TEXTURE, tooltip);
		GuiHelper.handleGuiTank(matrix, this.tile.tanks[HydrotreaterTileEntity.TANK_INPUT_B], this.guiLeft + 11, this.guiTop + 11, 16, 47, 0, 0, 0, 0, mx, my, GUI_TEXTURE, tooltip);
		GuiHelper.handleGuiTank(matrix, this.tile.tanks[HydrotreaterTileEntity.TANK_OUTPUT], this.guiLeft + 92, this.guiTop + 11, 16, 47, 0, 0, 0, 0, mx, my, GUI_TEXTURE, tooltip);
		
		// Power Stored
		if(mx > this.guiLeft + 121 && mx < this.guiLeft + 129 && my > this.guiTop + 11 && my < this.guiTop + 58){
			tooltip.add(new StringTextComponent(this.tile.energyStorage.getEnergyStored() + "/" + this.tile.energyStorage.getMaxEnergyStored() + " IF"));
		}
		
		if(!tooltip.isEmpty()){
			GuiUtils.drawHoveringText(matrix, tooltip, mx, my, this.width, this.height, -1, this.font);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y){
		// Not needed
		//super.drawGuiContainerForegroundLayer(matrixStack, x, y);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mx, int my){
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		MCUtil.bindTexture(GUI_TEXTURE);
		this.blit(matrix, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		
		GuiHelper.handleGuiTank(matrix, this.tile.tanks[HydrotreaterTileEntity.TANK_INPUT_A], this.guiLeft + 34, this.guiTop + 11, 16, 47, 140, 0, 20, 51, mx, my, GUI_TEXTURE, null);
		GuiHelper.handleGuiTank(matrix, this.tile.tanks[HydrotreaterTileEntity.TANK_INPUT_B], this.guiLeft + 11, this.guiTop + 11, 16, 47, 140, 0, 20, 51, mx, my, GUI_TEXTURE, null);
		GuiHelper.handleGuiTank(matrix, this.tile.tanks[HydrotreaterTileEntity.TANK_OUTPUT], this.guiLeft + 92, this.guiTop + 11, 16, 47, 140, 0, 20, 51, mx, my, GUI_TEXTURE, null);
		
		int stored = (int) (46 * (tile.getEnergyStored(null) / (float) tile.getMaxEnergyStored(null)));
		fillGradient(matrix, guiLeft + 122, guiTop + 12 + (46 - stored), guiLeft + 129, guiTop + 58, 0xffb51500, 0xff600b00);
	}
}
