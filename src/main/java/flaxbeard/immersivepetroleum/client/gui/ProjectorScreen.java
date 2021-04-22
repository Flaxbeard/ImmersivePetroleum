package flaxbeard.immersivepetroleum.client.gui;

import static flaxbeard.immersivepetroleum.ImmersivePetroleum.MODID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.utils.TemplateWorldCreator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.UnionMultiblock;
import flaxbeard.immersivepetroleum.client.gui.elements.GuiReactiveList;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.common.items.ProjectorItem;
import flaxbeard.immersivepetroleum.common.util.projector.Settings;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.Lazy;

public class ProjectorScreen extends Screen{
	static final ResourceLocation GUI_TEXTURE = new ResourceLocation(MODID, "textures/gui/projector.png");
	
	static final ITextComponent GUI_CONFIRM = translation("gui.immersivepetroleum.projector.button.confirm");
	static final ITextComponent GUI_CANCEL = translation("gui.immersivepetroleum.projector.button.cancel");
	static final ITextComponent GUI_MIRROR = translation("gui.immersivepetroleum.projector.button.mirror");
	static final ITextComponent GUI_ROTATE_CW = translation("gui.immersivepetroleum.projector.button.rcw");
	static final ITextComponent GUI_ROTATE_CCW = translation("gui.immersivepetroleum.projector.button.rccw");
	static final ITextComponent GUI_UP = translation("gui.immersivepetroleum.projector.button.up");
	static final ITextComponent GUI_DOWN = translation("gui.immersivepetroleum.projector.button.down");
	static final ITextComponent GUI_SEARCH = translation("gui.immersivepetroleum.projector.search");
	
	private int xSize = 256;
	private int ySize = 166;
	private int guiLeft;
	private int guiTop;
	
	private Lazy<List<IMultiblock>> multiblocks;
	private World templateWorld;
	private IMultiblock multiblock;
	private GuiReactiveList list;
	private String[] listEntries;
	
	private SearchField searchField;
	
	Settings settings;
	Hand hand;
	
	float rotation = 0.0F, move = 0.0F;
	public ProjectorScreen(Hand hand, ItemStack projector){
		super(new StringTextComponent("projector"));
		this.settings = new Settings(projector);
		this.hand = hand;
		this.multiblocks = Lazy.of(() -> MultiblockHandler.getMultiblocks());
		
		if(this.settings.getMultiblock() != null){
			this.move = 20F;
		}
	}
	
	@Override
	protected void init(){
		this.width = Minecraft.getInstance().getMainWindow().getScaledWidth();
		this.height = Minecraft.getInstance().getMainWindow().getScaledHeight();
		
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		
		this.searchField = addButton(new SearchField(this.font, this.guiLeft + 25, this.guiTop + 13));
		
		addButton(new ConfirmButton(this.guiLeft + 115, this.guiTop + 10, but -> {
			ItemStack held = Minecraft.getInstance().player.getHeldItem(this.hand);
			this.settings.applyTo(held);
			this.settings.sendPacketToServer(this.hand);
			Minecraft.getInstance().currentScreen.closeScreen();
		}));
		addButton(new CancelButton(this.guiLeft + 115, this.guiTop + 34, but -> {
			Minecraft.getInstance().currentScreen.closeScreen();
		}));
		addButton(new MirrorButton(this.guiLeft + 115, this.guiTop + 58, this.settings, but -> {
			this.settings.flip();
		}));
		addButton(new RotateLeftButton(this.guiLeft + 115, this.guiTop + 106, but -> {
			this.settings.rotateCCW();
		}));
		addButton(new RotateRightButton(this.guiLeft + 115, this.guiTop + 130, but -> {
			this.settings.rotateCW();
		}));
		
		updatelist();
	}
	
	private void listaction(Button button){
		GuiReactiveList l = (GuiReactiveList) button;
		if(l.selectedOption >= 0 && l.selectedOption < listEntries.length){
			String str = this.listEntries[l.selectedOption];
			IMultiblock mb = this.multiblocks.get().get(Integer.valueOf(str));
			this.settings.setMultiblock(mb);
		}
	}
	
	private void updatelist(){
		boolean exists = this.buttons.contains(this.list);
		
		List<String> list = new ArrayList<>();
		for(int i = 0;i < this.multiblocks.get().size();i++){
			String name = this.multiblocks.get().get(i).getUniqueName().toString();
			if(!name.contains("feedthrough")){
				list.add(Integer.toString(i));
			}
		}
		
		// Lazy search based on content
		list.removeIf(str -> {
			IMultiblock mb = this.multiblocks.get().get(Integer.valueOf(str));
			String name;
			if(mb instanceof UnionMultiblock && mb.getUniqueName().getPath().contains("excavator_demo")){
				name = I18n.format("desc.immersiveengineering.info.multiblock.IE:Excavator") + "2";
			}else{
				name = I18n.format("desc.immersiveengineering.info.multiblock.IE:" + ProjectorItem.getActualMBName(mb));
			}
			
			return !name.toLowerCase().contains(this.searchField.getText().toLowerCase());
		});
		
		this.listEntries = list.toArray(new String[0]);
		GuiReactiveList guilist = new GuiReactiveList(this, this.guiLeft + 15, this.guiTop + 29, 89, 127, button -> listaction(button), this.listEntries);
		guilist.setPadding(1, 1, 1, 1);
		guilist.setTextColor(0);
		guilist.setTextHoverColor(0x7F7FFF);
		guilist.setTranslationFunc(str -> {
			IMultiblock mb = this.multiblocks.get().get(Integer.valueOf(str));
			if(mb instanceof UnionMultiblock && mb.getUniqueName().getPath().contains("excavator_demo")){
				return I18n.format("desc.immersiveengineering.info.multiblock.IE:Excavator") + "2";
			}
			return I18n.format("desc.immersiveengineering.info.multiblock.IE:" + ProjectorItem.getActualMBName(mb));
		});
		
		if(!exists){
			this.list = addButton(guilist);
			return;
		}
		
		int a = this.buttons.indexOf(this.list);
		int b = this.children.indexOf(this.list);
		this.list = guilist;
		if(a != -1) this.buttons.set(a, this.list);
		if(b != -1) this.children.set(b, this.list);
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		// Over-GUI Text
		if(this.settings.getMultiblock() != null){
			IMultiblock mb = this.settings.getMultiblock();
			int x = this.guiLeft + 28;
			int y = this.guiTop - (int) (15 * (this.move / 20F));
			
			if(this.move < 20){
				this.move += 1.5 * partialTicks;
			}
			
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
			blit(matrix, x, y, 0, 166, 200, 13);
			
			ITextComponent text;
			if(mb instanceof UnionMultiblock && this.settings.getMultiblock().getUniqueName().getPath().contains("excavator_demo")){
				text = new TranslationTextComponent("desc.immersiveengineering.info.multiblock.IE:Excavator").appendString("2");
			}else{
				text = new TranslationTextComponent("desc.immersiveengineering.info.multiblock.IE:" + ProjectorItem.getActualMBName(mb));
			}
			
			x += 100;
			y += 3;
			IReorderingProcessor re = text.func_241878_f();
			this.font.func_238422_b_(matrix, re, (x - this.font.func_243245_a(re) / 2), y, 0x3F3F3F);
		}
		background(matrix, mouseX, mouseY, partialTicks);
		super.render(matrix, mouseX, mouseY, partialTicks);
		this.searchField.render(matrix, mouseX, mouseY, partialTicks);
		
		for(Widget widget:this.buttons){
			if(widget.isHovered()){
				widget.renderToolTip(matrix, mouseX, mouseY);
				break;
			}
		}
		
		{ // Direction Display (N-S-E-W)
			int x = this.guiLeft + 115;
			int y = this.guiTop + 82;
			
			Direction dir = Direction.byHorizontalIndex(this.settings.getRotation().ordinal());
			StringTextComponent dirText = new StringTextComponent(dir.toString().toUpperCase().substring(0, 1));
			drawCenteredString(matrix, this.font, dirText, x + 5, y + 1, -1);
			
			if(mouseX > x && mouseX < x + 10 && mouseY > y && mouseY < y + 10){
				ITextComponent rotText = new TranslationTextComponent("desc.immersivepetroleum.info.projector.rotated." + dir);
				renderTooltip(matrix, rotText, mouseX, mouseY);
			}
		}
		
		if(this.settings.getMultiblock() != null){
			IMultiblock mb = this.settings.getMultiblock();
			
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			try{
				
				this.rotation += 1.5F * partialTicks;
				
				Vector3i size = mb.getSize(null);
				matrix.push();
				{
					matrix.translate(this.guiLeft + 190, this.guiTop + 80, 64);
					matrix.scale(mb.getManualScale(), -mb.getManualScale(), 1);
					matrix.rotate(new Quaternion(25, 0, 0, true));
					matrix.rotate(new Quaternion(0, (int) (45 - this.rotation), 0, true));
					matrix.translate(size.getX() / -2F, size.getY() / -2F, size.getZ() / -2F);
					
					boolean tempDisable = true;
					if(tempDisable && mb.canRenderFormedStructure()){
						matrix.push();
						{
							mb.renderFormedStructure(matrix, IPRenderTypes.disableLighting(buffer));
						}
						matrix.pop();
					}else{
						if(this.templateWorld == null || (!this.multiblock.getUniqueName().equals(mb.getUniqueName()))){
							this.templateWorld = TemplateWorldCreator.CREATOR.getValue().makeWorld(mb.getStructure(null), pos -> true);
							this.multiblock = mb;
						}
						
						final BlockRendererDispatcher blockRender = Minecraft.getInstance().getBlockRendererDispatcher();
						int it = 0;
						List<Template.BlockInfo> infos = mb.getStructure(null);
						for(Template.BlockInfo info:infos){
							if(info.state.getMaterial() != Material.AIR && !mb.overwriteBlockRender(info.state, it++)){
								matrix.push();
								{
									matrix.translate(info.pos.getX(), info.pos.getY(), info.pos.getZ());
									int overlay = OverlayTexture.NO_OVERLAY;
									IModelData modelData = EmptyModelData.INSTANCE;
									TileEntity te = this.templateWorld.getTileEntity(info.pos);
									if(te != null){
										modelData = te.getModelData();
									}
									blockRender.renderBlock(info.state, matrix, IPRenderTypes.disableLighting(buffer), 0xF000F0, overlay, modelData);
								}
								matrix.pop();
							}
						}
					}
				}
				matrix.pop();
			}catch(Exception e){
				e.printStackTrace();
			}
			buffer.finish();
		}
	}
	
	private void background(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
		blit(matrix, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		return super.keyPressed(keyCode, scanCode, modifiers) || this.searchField.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char codePoint, int modifiers){
		return super.charTyped(codePoint, modifiers) || this.searchField.charTyped(codePoint, modifiers);
	}
	
	@Override
	public boolean isPauseScreen(){
		return false;
	}
	
	// CLASSES
	
	class ConfirmButton extends ProjectorScreen.ControlButton{
		public ConfirmButton(int x, int y, Consumer<PButton> action){
			super(x, y, 10, 10, 0, 179, action, GUI_CONFIRM);
		}
	}
	
	class CancelButton extends ProjectorScreen.ControlButton{
		public CancelButton(int x, int y, Consumer<PButton> action){
			super(x, y, 10, 10, 10, 179, action, GUI_CANCEL);
		}
	}
	
	class MirrorButton extends ProjectorScreen.ControlButton{
		Settings settings;
		public MirrorButton(int x, int y, Settings settings, Consumer<PButton> action){
			super(x, y, 10, 10, 20, 179, action, GUI_MIRROR);
			this.settings = settings;
		}
		
		@Override
		public void renderWidget(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
			Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
			if(isHovered()){
				fill(matrix, this.x, this.y + 1, this.x + this.iconSize, this.y + this.iconSize - 1, 0xAF7F7FFF);
			}
			
			if(this.settings.isMirrored()){
				blit(matrix, this.x, this.y, this.xOverlay, this.yOverlay + this.iconSize, this.iconSize, this.iconSize);
			}else{
				blit(matrix, this.x, this.y, this.xOverlay, this.yOverlay, this.iconSize, this.iconSize);
			}
		}
	}
	
	class RotateLeftButton extends ProjectorScreen.ControlButton{
		public RotateLeftButton(int x, int y, Consumer<PButton> action){
			super(x, y, 10, 10, 30, 179, action, GUI_ROTATE_CCW);
		}
	}
	
	class RotateRightButton extends ProjectorScreen.ControlButton{
		public RotateRightButton(int x, int y, Consumer<PButton> action){
			super(x, y, 10, 10, 40, 179, action, GUI_ROTATE_CW);
		}
	}
	
	class ControlButton extends ProjectorScreen.PButton{
		ITextComponent hoverText;
		public ControlButton(int x, int y, int width, int height, int overlayX, int overlayY, Consumer<PButton> action, ITextComponent hoverText){
			super(x, y, width, height, overlayX, overlayY, action);
			this.hoverText = hoverText;
		}
		
		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY){
			if(this.hoverText != null){
				ProjectorScreen.this.renderTooltip(matrixStack, this.hoverText, mouseX, mouseY);
			}
		}
	}
	
	class SearchField extends TextFieldWidget{
		public SearchField(FontRenderer font, int x, int y){
			super(font, x, y, 60, 14, GUI_SEARCH); // Font, x, y, width, height, tooltip
			setMaxStringLength(50);
			setEnableBackgroundDrawing(false);
			setVisible(true);
			setTextColor(0xFFFFFF);
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers){
			String s = getText();
			if(super.keyPressed(keyCode, scanCode, modifiers)){
				if(!Objects.equals(s, getText())){
					ProjectorScreen.this.updatelist();
				}
				
				return true;
			}else{
				return isFocused() && getVisible() && keyCode != 256 ? true : super.keyPressed(keyCode, scanCode, modifiers);
			}
		}
		
		@Override
		public boolean charTyped(char codePoint, int modifiers){
			if(!isFocused()){
				changeFocus(true);
				setFocused2(true);
			}
			
			String s = getText();
			if(super.charTyped(codePoint, modifiers)){
				if(!Objects.equals(s, getText())){
					ProjectorScreen.this.updatelist();
				}
				
				return true;
			}else{
				return false;
			}
		}
	}
	
	// STATIC METHODS
	
	static ITextComponent translation(String key){
		return new TranslationTextComponent(key);
	}
	
	// STATIC CLASSES
	
	static class PButton extends AbstractButton{
		protected boolean selected;
		protected final int xOverlay, yOverlay;
		protected int iconSize = 10;
		protected int bgStartX = 0, bgStartY = 166;
		protected Consumer<PButton> action;
		public PButton(int x, int y, int width, int height, int overlayX, int overlayY, Consumer<PButton> action){
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.action = action;
			this.xOverlay = overlayX;
			this.yOverlay = overlayY;
		}
		
		@Override
		public void renderWidget(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
			Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
			if(isHovered()){
				fill(matrix, this.x, this.y + 1, this.x + this.iconSize, this.y + this.iconSize - 1, 0xAF7F7FFF);
			}
			blit(matrix, this.x, this.y, this.xOverlay, this.yOverlay, this.iconSize, this.iconSize);
		}
		
		@Override
		public void onPress(){
			this.action.accept(this);
		}
		
		public boolean isSelected(){
			return this.selected;
		}
		
		public void setSelected(boolean isSelected){
			this.selected = isSelected;
		}
	}
}
