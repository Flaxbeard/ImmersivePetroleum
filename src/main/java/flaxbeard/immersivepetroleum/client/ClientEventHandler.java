package flaxbeard.immersivepetroleum.client;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.ManualElementMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleTileEntity;
import blusunrize.immersiveengineering.common.items.BuzzsawItem;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.SpecialManualElement;
import blusunrize.lib.manual.gui.ManualScreen;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.IPConfig;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.MessageCloseBook;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

public class ClientEventHandler{
	private static Object lastGui = null;
	private static Field FIELD_PAGES;
	private static Field FIELD_SPECIAL;
	private static Field FIELD_MULTIBLOCK;
	@SubscribeEvent
	public static void guiOpen(GuiOpenEvent event){
		if(event.getGui() == null && lastGui instanceof ManualScreen){
			ManualScreen gui = (ManualScreen) lastGui;
			ResourceLocation name = null;
			
			ManualEntry entry = gui.getCurrentPage();
			if(entry != null){
				try{
					if(FIELD_PAGES==null){
						FIELD_PAGES = ManualEntry.class.getDeclaredField("pages");
						FIELD_PAGES.setAccessible(true);
					}
					
					List<?> pages=(List<?>)FIELD_PAGES.get(entry);
					
					Object page = pages.get(gui.page);
					if(page != null && page.getClass().toString().contains("blusunrize.lib.manual.ManualEntry$ManualPage")){
						if(FIELD_SPECIAL==null){
							FIELD_SPECIAL = page.getClass().getDeclaredField("special");
							FIELD_SPECIAL.setAccessible(true);
						}
						
						if(FIELD_SPECIAL.get(page)!=null){
							SpecialManualElement special = (SpecialManualElement) FIELD_SPECIAL.get(page);
							if(special != null && special instanceof ManualElementMultiblock){
								if(FIELD_MULTIBLOCK==null){
									FIELD_MULTIBLOCK = ManualElementMultiblock.class.getDeclaredField("multiblock");
									FIELD_MULTIBLOCK.setAccessible(true);
								}
								
								IMultiblock mb = (IMultiblock) FIELD_MULTIBLOCK.get(special);
								name = mb.getUniqueName();
							}
						}
					}
				}catch(Exception e){
					ImmersivePetroleum.log.warn("This shouldnt happen.", e);
				}
			}
			
			PlayerEntity p = ClientUtils.mc().player;
			
			ItemStack mainItem = p.getHeldItemMainhand();
			ItemStack offItem = p.getHeldItemOffhand();
			
			boolean main = !mainItem.isEmpty() && mainItem.getItem() == IEItems.Tools.manual;
			boolean off = !offItem.isEmpty() && offItem.getItem() == IEItems.Tools.manual;
			ItemStack target = main ? mainItem : offItem;
			
			if(main || off){
				IPPacketHandler.INSTANCE.sendToServer(new MessageCloseBook(name));
				
				if(name == null && ItemNBTHelper.hasKey(target, "lastMultiblock")){
					ItemNBTHelper.remove(target, "lastMultiblock");
					ImmersivePetroleum.log.debug("Removed Multiblock-NBT from {}", target.getDisplayName().getUnformattedComponentText());
				}else if(name != null){
					ItemNBTHelper.putString(target, "lastMultiblock", name.toString());
					ImmersivePetroleum.log.debug("Added Multiblock-NBT to {} -> {}", target.getDisplayName().getUnformattedComponentText(), name.toString());
				}
			}
		}
		
		lastGui = event.getGui();
	}
	
	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public static void renderLast(RenderWorldLastEvent event){
		Minecraft mc = Minecraft.getInstance();
		
		if(IPConfig.MISCELLANEOUS.sample_displayBorder.get() && mc.player != null){
			PlayerEntity player = mc.player;
			
			GlStateManager.pushMatrix();
			{
				boolean chunkBorders = false;
				for(Hand hand:Hand.values()){
					if(player.getHeldItem(hand).getItem().equals(IEBlocks.MetalDevices.sampleDrill.asItem())){
						chunkBorders = true;
						break;
					}
				}
				
				if(!chunkBorders && ClientUtils.mc().objectMouseOver != null && ClientUtils.mc().objectMouseOver.getType() == Type.BLOCK && mc.world.getTileEntity(new BlockPos(mc.objectMouseOver.getHitVec())) instanceof SampleDrillTileEntity)
					chunkBorders = true;
				
				ItemStack mainItem = player.getHeldItemMainhand();
				ItemStack secondItem = player.getHeldItemOffhand();
				boolean main = !mainItem.isEmpty() && mainItem.getItem() instanceof CoresampleItem;
				boolean off = !secondItem.isEmpty() && secondItem.getItem() instanceof CoresampleItem;
				
				ItemStack target = main ? mainItem : secondItem;
				if(!chunkBorders && (main || off)){
					ColumnPos pos=CoresampleItem.getCoords(target);
					if(pos!=null){
						renderChunkBorder(pos.x >> 4 << 4, pos.z >> 4 << 4);
					}
				}
			}
			GlStateManager.popMatrix();
		}
		
		GlStateManager.pushMatrix();
		{
			if(mc.player != null){
				ItemStack mainItem = mc.player.getHeldItemMainhand();
				ItemStack secondItem = mc.player.getHeldItemOffhand();
				
				boolean main = (mainItem != null && !mainItem.isEmpty()) && mainItem.getItem() == IPContent.Blocks.auto_lubricator.asItem();
				boolean off = (secondItem != null && !secondItem.isEmpty()) && secondItem.getItem() == IPContent.Blocks.auto_lubricator.asItem();
				
				if(main || off){
					ItemRenderer itemRenderer=ClientUtils.mc().getItemRenderer();
					
					BlockPos base = mc.player.getPosition();
					for(int x = -16;x <= 16;x++){
						for(int z = -16;z <= 16;z++){
							for(int y = -16;y <= 16;y++){
								BlockPos pos = base.add(x, y, z);
								TileEntity te = mc.player.world.getTileEntity(pos);
								
								if(te != null){
									ILubricationHandler<TileEntity> handler = (ILubricationHandler<TileEntity>) LubricatedHandler.getHandlerForTile(te);
									if(handler != null){
										Tuple<BlockPos, Direction> target = handler.getGhostBlockPosition(mc.player.world, te);
										if(target != null){
											BlockPos targetPos = target.getA();
											Direction targetFacing = target.getB();
											BlockState targetState=mc.player.world.getBlockState(targetPos);
											BlockState targetStateUp=mc.player.world.getBlockState(targetPos.up());
											if(targetState.getMaterial().isReplaceable() && targetStateUp.getMaterial().isReplaceable()){
												GlStateManager.pushMatrix();
												{
													float alpha = .5f;
													ShaderUtil.alpha_static(alpha, mc.player.ticksExisted);
													double px = TileEntityRendererDispatcher.staticPlayerX;
													double py = TileEntityRendererDispatcher.staticPlayerY;
													double pz = TileEntityRendererDispatcher.staticPlayerZ;
													
													GlStateManager.translated(targetPos.getX() - px, targetPos.getY() - py, targetPos.getZ() - pz);
													GlStateManager.translated(0.5, -.5, .5);
													
													switch(targetFacing){
														case SOUTH:
															GlStateManager.rotated(270, 0, 1, 0);
															break;
														case NORTH:
															GlStateManager.rotated(90, 0, 1, 0);
															break;
														case WEST:
															GlStateManager.rotated(180, 0, 1, 0);
															break;
														case EAST:
															break;
														default:
													}
													GlStateManager.translated(0.02, 0, .019);
													
													GlStateManager.scaled(1, 1, 1);
													//GlStateManager.scaled(2, 2, 2);
													
													ItemStack toRender = new ItemStack(IPContent.Blocks.auto_lubricator);
													itemRenderer.renderItem(toRender, itemRenderer.getModelWithOverrides(toRender));
													
													ShaderUtil.releaseShader();
												}
												GlStateManager.popMatrix();
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		GlStateManager.popMatrix();
	}
	
	public static void renderChunkBorder(int chunkX, int chunkZ){
		PlayerEntity player = ClientUtils.mc().player;
		
		double px = TileEntityRendererDispatcher.staticPlayerX;
		double py = TileEntityRendererDispatcher.staticPlayerY;
		double pz = TileEntityRendererDispatcher.staticPlayerZ;
		int y = Math.min((int) player.posY - 2, player.getEntityWorld().getChunk(chunkX, chunkZ).getHeight());
		float h = (float) Math.max(32, player.posY - y + 4);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		float r = Lib.COLOUR_F_ImmersiveOrange[0];
		float g = Lib.COLOUR_F_ImmersiveOrange[1];
		float b = Lib.COLOUR_F_ImmersiveOrange[2];
		vertexbuffer.setTranslation(chunkX - px, y + 2 - py, chunkZ - pz);
		GlStateManager.lineWidth(5f);
		vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		vertexbuffer.pos(0, 0, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(0, h, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(16, 0, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(16, h, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(16, 0, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(16, h, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(0, 0, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(0, h, 16).color(r, g, b, .375f).endVertex();
		
		vertexbuffer.pos(0, 2, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(16, 2, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(0, 2, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(0, 2, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(0, 2, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(16, 2, 16).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(16, 2, 0).color(r, g, b, .375f).endVertex();
		vertexbuffer.pos(16, 2, 16).color(r, g, b, .375f).endVertex();
		tessellator.draw();
		vertexbuffer.setTranslation(0, 0, 0);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
	}
	
	static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void handleItemTooltip(ItemTooltipEvent event){
		ItemStack stack = event.getItemStack();
		if(stack.getItem() instanceof CoresampleItem){
			if(ItemNBTHelper.hasKey(stack, "oil")){
				String resName = ItemNBTHelper.hasKey(stack, "resType") ? ItemNBTHelper.getString(stack, "resType") : null;
				if(ItemNBTHelper.hasKey(stack, "oil") && resName == null){
					resName = "";
				}
				
				ReservoirType res = null;
				for(ReservoirType type:PumpjackHandler.reservoirs.values()){
					if(resName.equalsIgnoreCase(type.name)){
						res = type;
					}
				}
				
				List<ITextComponent> tooltip = event.getToolTip();
				int amnt = ItemNBTHelper.getInt(stack, "oil");
				int tipPos = Math.max(0, tooltip.size() - 5);
				
				if(res != null && amnt > 0){
					int est = (amnt / 1000) * 1000;
					String fluidName = new FluidStack(res.getFluid(), 1).getDisplayName().getUnformattedComponentText();
					
					ITextComponent header=new TranslationTextComponent("chat.immersivepetroleum.info.coresample.oil", fluidName)
							.applyTextStyle(TextFormatting.GRAY);
					
					ITextComponent info=new StringTextComponent("  "+FORMATTER.format(est)+" mB")
							.applyTextStyle(TextFormatting.DARK_GRAY);
					
					tooltip.add(tipPos, header);
					tooltip.add(tipPos+1, info);
				}else{
					if(res != null && res.replenishRate > 0){
						String fluidName = new FluidStack(res.getFluid(), 1).getDisplayName().getUnformattedComponentText();
						
						ITextComponent header=new TranslationTextComponent("chat.immersivepetroleum.info.coresample.oil", fluidName)
								.applyTextStyle(TextFormatting.GRAY);
						
						ITextComponent info=new StringTextComponent("  "+I18n.format("chat.immersivepetroleum.info.coresample.oilRep", res.replenishRate, fluidName))
								.applyTextStyle(TextFormatting.GRAY);
						
						tooltip.add(tipPos, header);
						tooltip.add(tipPos+1, info);
					}else{
						tooltip.add(tipPos, new StringTextComponent(I18n.format("chat.immersivepetroleum.info.coresample.noOil")).applyTextStyle(TextFormatting.GRAY));
					}
				}
			}
		}
	}
	
	@SubscribeEvent()
	public static void renderInfoOverlays(RenderGameOverlayEvent.Post event){
		if(ClientUtils.mc().player != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			PlayerEntity player = ClientUtils.mc().player;
			
			if(ClientUtils.mc().objectMouseOver != null){
				boolean hammer = player.getHeldItem(Hand.MAIN_HAND) != null && Utils.isHammer(player.getHeldItem(Hand.MAIN_HAND));
				RayTraceResult mop = ClientUtils.mc().objectMouseOver;
				
				if(mop != null){
					switch(mop.getType()){
						case BLOCK:{
							if(mop.getHitVec() != null){
								TileEntity tileEntity = player.world.getTileEntity(((BlockRayTraceResult)mop).getPos());
								
								if(tileEntity instanceof CoresampleTileEntity){
									IBlockOverlayText overlayBlock = (IBlockOverlayText) tileEntity;
									String[] text = overlayBlock.getOverlayText(player, mop, hammer);
									ItemStack coresample = ((CoresampleTileEntity) tileEntity).coresample;
									
									if(ItemNBTHelper.hasKey(coresample, "oil") && text != null && text.length > 0){
										String resName = ItemNBTHelper.hasKey(coresample, "resType") ? ItemNBTHelper.getString(coresample, "resType") : "";
										int amnt = ItemNBTHelper.getInt(coresample, "oil");
										int i = text.length;
										
										ReservoirType res = null;
										for(ReservoirType type:PumpjackHandler.reservoirs.values()){
											if(resName.equals(type.name)){
												res = type;
											}
										}
										
										String s = I18n.format("chat.immersivepetroleum.info.coresample.noOil");
										if(res != null && amnt > 0){
											String fluidName = new FluidStack(res.getFluid(), 1).getDisplayName().getUnformattedComponentText();
											
											s = I18n.format("chat.immersivepetroleum.info.coresample.oil", fluidName);
										}else if(res != null && res.replenishRate > 0){
											String fluidName = new FluidStack(res.getFluid(), 1).getDisplayName().getUnformattedComponentText();
											s = I18n.format("chat.immersivepetroleum.info.coresample.oilRep", res.replenishRate, fluidName);
										}
										
										int fx = event.getWindow().getScaledWidth() / 2 + 8;
										int fy = event.getWindow().getScaledHeight() / 2 + 8 + i * ClientUtils.font().FONT_HEIGHT;
										ClientUtils.font().drawStringWithShadow(s, fx, fy, 0xffffff);
									}
								}
							}
							break;
						}
						case ENTITY:{
							EntityRayTraceResult rtr=(EntityRayTraceResult)mop;
							if(rtr.getEntity() instanceof SpeedboatEntity){
								String[] text = ((SpeedboatEntity) rtr.getEntity()).getOverlayText(player, mop);
								
								if(text != null && text.length > 0){
									FontRenderer font = ClientUtils.font();
									int col = 0xffffff;
									for(int i = 0;i < text.length; i++){
										if(text[i] != null){
											int fx = event.getWindow().getScaledWidth() / 2 + 8;
											int fy = event.getWindow().getScaledHeight() / 2 + 8 + i * font.FONT_HEIGHT;
											font.drawStringWithShadow(text[i], fx, fy, col);
										}
									}
								}
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
	
	@SubscribeEvent()
	public static void onRenderOverlayPost(RenderGameOverlayEvent.Post event){
		if(ClientUtils.mc().player != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			PlayerEntity player = ClientUtils.mc().player;
			
			if(player.getRidingEntity() instanceof SpeedboatEntity){
				int offset = 0;
				for(Hand hand:Hand.values()){
					if(!player.getHeldItem(hand).isEmpty()){
						ItemStack equipped = player.getHeldItem(hand);
						if((equipped.getItem() instanceof DrillItem) || equipped.getItem() instanceof ChemthrowerItem || equipped.getItem() instanceof BuzzsawItem){
							offset -= 85;
						}
					}
				}
				
				GlStateManager.pushMatrix();
				{
					float dx = event.getWindow().getScaledWidth()-16;
					float dy = event.getWindow().getScaledHeight();
					int w = 31;
					int h = 62;
					
					double uMin = 179 / 256f;
					double uMax = 210 / 256f;
					double vMin = 9 / 256f;
					double vMax = 71 / 256f;
					
					ClientUtils.bindTexture("immersivepetroleum:textures/gui/hud_elements.png");
					GlStateManager.color4f(1, 1, 1, 1);
					GlStateManager.translated(dx, dy + offset, 0);
					ClientUtils.drawTexturedRect(-24, -68, w, h, uMin, uMax, vMin, vMax);
					
					GL11.glTranslated(-23, -37, 0);
					SpeedboatEntity boat = (SpeedboatEntity) player.getRidingEntity();
					int capacity = boat.getMaxFuel();
					
					FluidStack fs = boat.getContainedFluid();
					int amount = fs == FluidStack.EMPTY || fs.getFluid() == null ? 0 : fs.getAmount();
					
					float cap = (float) capacity;
					float angle = 83 - (166 * amount / cap);
					GlStateManager.rotatef(angle, 0, 0, 1);
					ClientUtils.drawTexturedRect(6, -2, 24, 4, 91 / 256f, 123 / 256f, 80 / 256f, 87 / 256f);
					GlStateManager.rotatef(-angle, 0, 0, 1);
					
					GlStateManager.translated(23, 37, 0);
					ClientUtils.drawTexturedRect(-41, -73, 53, 72, 8 / 256f, 61 / 256f, 4 / 256f, 76 / 256f);
					
					// TODO DEBUG: Remove later.
					GlStateManager.pushMatrix();
					{
						FontRenderer font=Minecraft.getInstance().fontRenderer;
						if(font!=null){
							Vec3d vec=boat.getMotion();
							
							float speed=(float)(Math.sqrt(vec.x*vec.x + vec.z*vec.z));
							
							String out0=String.format(Locale.US, "Fuel: %s/%sMB", amount,capacity);
							String out1=String.format(Locale.US, "Speed: %.2f", speed);
							font.drawStringWithShadow(out0, -65, -94, 0xFFFFFFFF);
							font.drawStringWithShadow(out1, -65, -85, 0xFFFFFFFF);
						}
					}
					GlStateManager.popMatrix();
				}
				GlStateManager.popMatrix();
			}
		}
	}
	
	@SubscribeEvent
	public static void handleBoatImmunity(RenderBlockOverlayEvent event){
		PlayerEntity entity = event.getPlayer();
		if(event.getOverlayType() == OverlayType.FIRE && entity.isBurning() && entity.getRidingEntity() instanceof SpeedboatEntity){
			SpeedboatEntity boat = (SpeedboatEntity) entity.getRidingEntity();
			if(boat.isFireproof){
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void handleFireRender(RenderPlayerEvent.Pre event){
		PlayerEntity entity = event.getPlayer();
		if(entity.isBurning() && entity.getRidingEntity() instanceof SpeedboatEntity){
			SpeedboatEntity boat = (SpeedboatEntity) entity.getRidingEntity();
			if(boat.isFireproof){
				entity.extinguish();
			}
		}
	}
	
	@SubscribeEvent
	public static void handleLubricatingMachinesClient(ClientTickEvent event){
		if(event.phase == Phase.END && Minecraft.getInstance().world != null){
			CommonEventHandler.handleLubricatingMachines(Minecraft.getInstance().world);
		}
	}
	
}
