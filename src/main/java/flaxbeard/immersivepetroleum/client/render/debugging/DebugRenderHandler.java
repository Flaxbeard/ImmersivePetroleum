package flaxbeard.immersivepetroleum.client.render.debugging;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricatedTileInfo;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity.CokingChamber;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.HydrotreaterTileEntity;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class DebugRenderHandler{
	public DebugRenderHandler(){
	}
	
	private boolean isHoldingDebugItem(PlayerEntity player){
		ItemStack main = player.getHeldItem(Hand.MAIN_HAND);
		ItemStack off = player.getHeldItem(Hand.OFF_HAND);
		
		return (main != ItemStack.EMPTY && main.getItem() == IPContent.debugItem) || (off != ItemStack.EMPTY && off.getItem() == IPContent.debugItem);
	}
	
	@SubscribeEvent
	public void renderDebuggingOverlay(RenderGameOverlayEvent.Post event){
		Minecraft mc = Minecraft.getInstance();
		
		if(mc.player != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			PlayerEntity player = mc.player;
			
			if(isHoldingDebugItem(player)){
				RayTraceResult rt = mc.objectMouseOver;
				if(rt != null){
					switch(rt.getType()){
						case BLOCK:{
							BlockRayTraceResult result = (BlockRayTraceResult) rt;
							World world = player.world;
							
							List<ITextComponent> debugOut = new ArrayList<>();
							
							TileEntity te = world.getTileEntity(result.getPos());
							boolean isMBPart = te instanceof MultiblockPartTileEntity;
							if(isMBPart){
								MultiblockPartTileEntity<?> multiblock = (MultiblockPartTileEntity<?>) te;
								
								if(!multiblock.offsetToMaster.equals(BlockPos.ZERO)){
									multiblock = multiblock.master();
								}
								
								if(te instanceof DistillationTowerTileEntity){
									distillationtower(debugOut, (DistillationTowerTileEntity) multiblock);
									
								}else if(te instanceof CokerUnitTileEntity){
									cokerunit(debugOut, (CokerUnitTileEntity) multiblock);
									
								}else if(te instanceof HydrotreaterTileEntity){
									hydrotreater(debugOut, (HydrotreaterTileEntity) multiblock);
								}
							}
							
							if(!debugOut.isEmpty() || isMBPart){
								if(isMBPart){
									MultiblockPartTileEntity<?> generic = (MultiblockPartTileEntity<?>) te;
									BlockPos tPos = generic.posInMultiblock;
									
									if(!generic.offsetToMaster.equals(BlockPos.ZERO)){
										generic = generic.master();
									}
									
									BlockPos hit = result.getPos();
									Block block = generic.getBlockState().getBlock();
									
									debugOut.add(0, toText("World XYZ: " + hit.getX() + ", " + hit.getY() + ", " + hit.getZ()));
									debugOut.add(1, toText("Template XYZ: " + tPos.getX() + ", " + tPos.getY() + ", " + tPos.getZ()));
									
									IFormattableTextComponent name = toTranslation(block.getTranslationKey()).mergeStyle(TextFormatting.GOLD);
									
									try{
										name.appendSibling(toText(generic.isRSDisabled() ? " (Redstoned)" : "").mergeStyle(TextFormatting.RED));
									}catch(UnsupportedOperationException e){
										// Don't care, skip if this is thrown
									}
									
									if(generic instanceof PoweredMultiblockTileEntity<?, ?>){
										PoweredMultiblockTileEntity<?, ?> poweredGeneric = (PoweredMultiblockTileEntity<?, ?>) generic;
										
										name.appendSibling(toText(poweredGeneric.shouldRenderAsActive() ? " (Active)" : "").mergeStyle(TextFormatting.GREEN));
										
										debugOut.add(2, toText(poweredGeneric.energyStorage.getEnergyStored() + "/" + poweredGeneric.energyStorage.getMaxEnergyStored() + "RF"));
									}
									
									synchronized(LubricatedHandler.lubricatedTiles){
										for(LubricatedTileInfo info:LubricatedHandler.lubricatedTiles){
											if(info.pos.equals(generic.getPos())){
												name.appendSibling(toText(" (Lubricated " + info.ticks + ")").mergeStyle(TextFormatting.YELLOW));
											}
										}
									}
									
									debugOut.add(2, name);
								}
								
								renderOverlay(event.getMatrixStack(), debugOut);
							}
							break;
						}
						case ENTITY:{
							EntityRayTraceResult result = (EntityRayTraceResult) rt;
							
							if(result.getEntity() instanceof MotorboatEntity){
								MotorboatEntity boat = (MotorboatEntity) result.getEntity();
								
								List<ITextComponent> debugOut = new ArrayList<>();
								
								debugOut.add(toText("").appendSibling(boat.getDisplayName()).mergeStyle(TextFormatting.GOLD));
								
								FluidStack fluid = boat.getContainedFluid();
								if(fluid == FluidStack.EMPTY){
									debugOut.add(toText("Tank: Empty"));
								}else{
									debugOut.add(toText("Tank: " + fluid.getAmount() + "/" + boat.getMaxFuel() + "mB of ").appendSibling(fluid.getDisplayName()));
								}
								
								NonNullList<ItemStack> upgrades = boat.getUpgrades();
								int i = 0;
								for(ItemStack upgrade:upgrades){
									if(upgrade == null || upgrade == ItemStack.EMPTY){
										debugOut.add(toText("Upgrade " + (++i) + ": Empty"));
									}else{
										debugOut.add(toText("Upgrade " + (++i) + ": ").appendSibling(upgrade.getDisplayName()));
									}
								}
								
								renderOverlay(event.getMatrixStack(), debugOut);
							}
							break;
						}
						default:
							break;
					}
				}
			}
		}
	}
	
	private static void renderOverlay(MatrixStack matrix, List<ITextComponent> debugOut){
		Minecraft mc = Minecraft.getInstance();
		
		matrix.push();
		{
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			for(int i = 0;i < debugOut.size();i++){
				int w = mc.fontRenderer.getStringWidth(debugOut.get(i).getString());
				int yOff = i * (mc.fontRenderer.FONT_HEIGHT + 2);
				
				matrix.push();
				{
					matrix.translate(0, 0, 1);
					GuiHelper.drawColouredRect(1, 1 + yOff, w + 1, 10, 0xAF_000000, buffer, matrix);
					buffer.finish();
					// Draw string without shadow
					mc.fontRenderer.drawText(matrix, debugOut.get(i), 2, 2 + yOff, -1);
				}
				matrix.pop();
			}
		}
		matrix.pop();
	}
	
	private static void distillationtower(List<ITextComponent> text, DistillationTowerTileEntity tower){
		for(int i = 0;i < tower.tanks.length;i++){
			text.add(toText("Tank " + (i + 1)).mergeStyle(TextFormatting.UNDERLINE));
			
			MultiFluidTank tank = tower.tanks[i];
			if(tank.fluids.size() > 0){
				for(int j = 0;j < tank.fluids.size();j++){
					FluidStack fstack = tank.fluids.get(j);
					text.add(toText("  " + fstack.getDisplayName().getString() + " (" + fstack.getAmount() + "mB)"));
				}
			}else{
				text.add(toText("  Empty"));
			}
		}
	}
	
	private static void cokerunit(List<ITextComponent> text, CokerUnitTileEntity coker){
		{
			FluidTank tank = coker.bufferTanks[CokerUnitTileEntity.TANK_INPUT];
			FluidStack fs = tank.getFluid();
			text.add(toText("In Buffer: " + (fs.getAmount() + "/" + tank.getCapacity() + "mB " + (fs.isEmpty() ? "" : "(" + fs.getDisplayName().getString() + ")"))));
		}
		
		{
			FluidTank tank = coker.bufferTanks[CokerUnitTileEntity.TANK_OUTPUT];
			FluidStack fs = tank.getFluid();
			text.add(toText("Out Buffer: " + (fs.getAmount() + "/" + tank.getCapacity() + "mB " + (fs.isEmpty() ? "" : "(" + fs.getDisplayName().getString() + ")"))));
		}
		
		for(int i = 0;i < coker.chambers.length;i++){
			CokingChamber chamber = coker.chambers[i];
			FluidTank tank = chamber.getTank();
			FluidStack fs = tank.getFluid();
			
			float completed = chamber.getTotalAmount() > 0 ? 100 * (chamber.getOutputAmount() / (float) chamber.getTotalAmount()) : 0;
			
			text.add(toText("Chamber " + i).mergeStyle(TextFormatting.UNDERLINE, TextFormatting.AQUA));
			text.add(toText("State: " + chamber.getState().toString()));
			text.add(toText("  Tank: " + (fs.getAmount() + "/" + tank.getCapacity() + "mB " + (fs.isEmpty() ? "" : "(" + fs.getDisplayName().getString() + ")"))));
			text.add(toText("  Content: " + chamber.getTotalAmount() + " / " + chamber.getCapacity()).appendString(" (" + chamber.getInputItem().getDisplayName().getString() + ")"));
			text.add(toText("  Out: " + chamber.getOutputItem().getDisplayName().getString()));
			text.add(toText("  " + MathHelper.floor(completed) + "% Completed. (Raw: " + completed + ")"));
		}
	}
	
	private static void hydrotreater(List<ITextComponent> text, HydrotreaterTileEntity treater){
		IFluidTank[] tanks = treater.getInternalTanks();
		if(tanks != null && tanks.length > 0){
			for(int i = 0;i < tanks.length;i++){
				FluidStack fs = tanks[i].getFluid();
				text.add(toText("Tank " + i + ": " + (fs.getAmount() + "/" + tanks[i].getCapacity() + "mB " + (fs.isEmpty() ? "" : "(" + fs.getDisplayName().getString() + ")"))));
			}
		}
	}
	
	static IFormattableTextComponent toText(String string){
		return new StringTextComponent(string);
	}
	
	static IFormattableTextComponent toTranslation(String translationKey, Object... args){
		return new TranslationTextComponent(translationKey, args);
	}
}
