package flaxbeard.immersivepetroleum.client.gui;

import static flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity.CHAMBER_A;
import static flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity.CHAMBER_B;
import static flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity.TANK_INPUT;
import static flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity.TANK_OUTPUT;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity.CokingChamber;
import flaxbeard.immersivepetroleum.common.gui.CokerUnitContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class CokerUnitScreen extends IEContainerScreen<CokerUnitContainer>{
	static final ResourceLocation GUI_TEXTURE = new ResourceLocation("immersivepetroleum", "textures/gui/coker.png");
	
	CokerUnitTileEntity tile;
	public CokerUnitScreen(CokerUnitContainer inventorySlotsIn, PlayerInventory inv, ITextComponent title){
		super(inventorySlotsIn, inv, title);
		this.tile = container.tile;
		
		this.xSize = 200;
		this.ySize = 187;
	}
	
	@Override
	public void render(MatrixStack matrix, int mx, int my, float partialTicks){
		this.renderBackground(matrix);
		super.render(matrix, mx, my, partialTicks);
		this.renderHoveredTooltip(matrix, mx, my);
		
		List<ITextComponent> tooltip = new ArrayList<>();
		
		// Buffer tank displays
		GuiHelper.handleGuiTank(matrix, tile.bufferTanks[TANK_INPUT], guiLeft + 32, guiTop + 14, 16, 47, 0, 0, 0, 0, mx, my, GUI_TEXTURE, tooltip);
		GuiHelper.handleGuiTank(matrix, tile.bufferTanks[TANK_OUTPUT], guiLeft + 152, guiTop + 14, 16, 47, 0, 0, 0, 0, mx, my, GUI_TEXTURE, tooltip);
		
		// Chamber Stats
		chamberDisplay(matrix, guiLeft + 74, guiTop + 24, 6, 38, CHAMBER_A, mx, my, partialTicks, tooltip);
		chamberDisplay(matrix, guiLeft + 120, guiTop + 24, 6, 38, CHAMBER_B, mx, my, partialTicks, tooltip);
		
		// Power Stored
		if(mx > guiLeft + 167 && mx < guiLeft + 175 && my > guiTop + 66 && my < guiTop + 88){
			tooltip.add(new StringTextComponent(tile.energyStorage.getEnergyStored() + "/" + tile.energyStorage.getMaxEnergyStored() + " RF"));
		}
		
		if(!tooltip.isEmpty()){
			GuiUtils.drawHoveringText(matrix, tooltip, mx, my, width, height, -1, font);
		}
	}
	
	private void chamberDisplay(MatrixStack matrix, int x, int y, int w, int h, int chamberId, int mx, int my, float partialTicks, List<ITextComponent> tooltip){
		CokingChamber chamber = tile.chambers[chamberId];
		
		// Vertical Bar for Content amount.
		ClientUtils.bindTexture(GUI_TEXTURE);
		int scale = 38;
		int off = (int) (chamber.getTotalAmount() / (float) chamber.getCapacity() * scale);
		this.blit(matrix, x, y + scale - off, 200, 51, 6, off);
		
		// Vertical Overlay to visualize progress
		off = (int)(chamber.getTotalAmount() > 0 ? scale * (chamber.getOutputAmount() / (float)chamber.getCapacity()) : 0);
		this.blit(matrix, x, y + scale - off, 206, 51 + (scale - off), 6, off);
		
		// Chamber Tank
		GuiHelper.handleGuiTank(matrix, chamber.getTank(), x, y, 6, 38, 0, 0, 0, 0, mx, my, GUI_TEXTURE, null);
		
		// Debugging Tooltip
		/*if((mx >= x && mx < x + w) && (my >= y && my < y + h)){
			float completed = chamber.getTotalAmount() > 0 ? 100 * (chamber.getOutputAmount() / (float)chamber.getTotalAmount()) : 0;
			
			tooltip.add(new StringTextComponent("State: " + chamber.getState().toString()));
			tooltip.add(new StringTextComponent("Content: " + chamber.getTotalAmount() + " / " + chamber.getCapacity()));
			tooltip.add(new StringTextComponent("Input: ").appendString(chamber.getInputItem().getDisplayName().getString()));
			tooltip.add(new StringTextComponent("Output: ").appendString(chamber.getOutputItem().getDisplayName().getString()));
			tooltip.add(new StringTextComponent(MathHelper.floor(completed) + "% Completed. (Raw: " + completed + ")"));
			
			tooltip.add(new StringTextComponent("-------------"));
			ClientUtils.handleGuiTank(matrix, chamber.getTank(), x, y, w, x, 0, 0, 0, 0, mx, my, GUI_TEXTURE, tooltip);
		}
		//*/
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mx, int my){
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture(GUI_TEXTURE);
		this.blit(matrix, guiLeft, guiTop, 0, 0, xSize, ySize);
		
		GuiHelper.handleGuiTank(matrix, tile.bufferTanks[TANK_INPUT], guiLeft + 32, guiTop + 14, 16, 47, 202, 2, 16, 47, mx, my, GUI_TEXTURE, null);
		GuiHelper.handleGuiTank(matrix, tile.bufferTanks[TANK_OUTPUT], guiLeft + 152, guiTop + 14, 16, 47, 202, 2, 16, 47, mx, my, GUI_TEXTURE, null);
		
		int x = guiLeft + 168;
		int y = guiTop + 67;
		int stored = (int) (tile.energyStorage.getEnergyStored() / (float) tile.energyStorage.getMaxEnergyStored() * 21);
		fillGradient(matrix, x, y + 21 - stored, x + 7, y + 21, 0xffb51500, 0xff600b00);
	}
}
