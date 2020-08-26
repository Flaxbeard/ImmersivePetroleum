package flaxbeard.immersivepetroleum.common;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.ManualElementMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
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
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricatedTileInfo;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.OilWorldInfo;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.client.ShaderUtil;
import flaxbeard.immersivepetroleum.common.IPContent.Fluids;
import flaxbeard.immersivepetroleum.common.blocks.BlockNapalm;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.MessageCloseBook;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID)
public class EventHandler{
	
	@SubscribeEvent
	public static void onSave(WorldEvent.Save event){
		IPSaveData.setDirty();
	}
	
	@SubscribeEvent
	public static void onUnload(WorldEvent.Unload event){
		IPSaveData.setDirty();
	}
	
	@OnlyIn(Dist.CLIENT)
	private static Object lastGui = null;
	@OnlyIn(Dist.CLIENT)
	private static Field FIELD_PAGES;
	@OnlyIn(Dist.CLIENT)
	private static Field FIELD_SPECIAL;
	@OnlyIn(Dist.CLIENT)
	private static Field FIELD_MULTIBLOCK;
	@OnlyIn(Dist.CLIENT)
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
	
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void renderLast(RenderWorldLastEvent event){
		Minecraft mc = Minecraft.getInstance();
		
		if(IPConfig.MISCELLANEOUS.sample_displayBorder.get() && mc.player != null){
			PlayerEntity player = mc.player;
			
			GlStateManager.pushMatrix();
			{
				ItemStack mainItem = player.getHeldItemMainhand();
				ItemStack secondItem = player.getHeldItemOffhand();
				
				boolean main = !mainItem.isEmpty() && mainItem.getItem() instanceof CoresampleItem && ItemNBTHelper.hasKey(mainItem, "coords");
				boolean off = !secondItem.isEmpty() && secondItem.getItem() instanceof CoresampleItem && ItemNBTHelper.hasKey(secondItem, "coords");
				
				boolean chunkBorders = false;
				for(Hand hand:Hand.values()){
					if(player.getHeldItem(hand).getItem().equals(IEBlocks.MetalDevices.sampleDrill.asItem())){
						chunkBorders = true;
						break;
					}
				}
				
				if(!chunkBorders && ClientUtils.mc().objectMouseOver != null && ClientUtils.mc().objectMouseOver.getType() == Type.BLOCK && mc.world.getTileEntity(new BlockPos(mc.objectMouseOver.getHitVec())) instanceof SampleDrillTileEntity)
					chunkBorders = true;
				
				ItemStack target = main ? mainItem : secondItem;
				
				if(!chunkBorders && (main || off)){
					CompoundNBT nbt=ItemNBTHelper.getTagCompound(target, "coords");
					
					int x=nbt.getInt("x");
					int z=nbt.getInt("z");
					
					renderChunkBorder(x << 4, z << 4);
				}
			}
			GlStateManager.popMatrix();
		}
		
		GlStateManager.pushMatrix();
		{
			if(mc.player != null){
				ItemStack mainItem = mc.player.getHeldItemMainhand();
				ItemStack secondItem = mc.player.getHeldItemOffhand();
				
				boolean main = (mainItem != null && !mainItem.isEmpty()) && mainItem.getItem() == IPContent.Blocks.blockAutolubricator.asItem();
				boolean off = (secondItem != null && !secondItem.isEmpty()) && secondItem.getItem() == IPContent.Blocks.blockAutolubricator.asItem();
				
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
													
													ItemStack toRender = new ItemStack(IPContent.Blocks.blockAutolubricator);
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
	
	@OnlyIn(Dist.CLIENT)
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
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void handlePickupItem(RightClickBlock event){
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		if(!event.getWorld().isRemote && state.getBlock() == IEBlocks.MetalDevices.sampleDrill){
			TileEntity te = event.getWorld().getTileEntity(pos);
			if(te instanceof SampleDrillTileEntity){
				SampleDrillTileEntity drill = (SampleDrillTileEntity) te;
				if(drill.isDummy()){
					drill = (SampleDrillTileEntity)drill.master();
				}
				
				if(!drill.sample.isEmpty()){
					if(ItemNBTHelper.hasKey(drill.sample, "coords")){
						try{
							World world = event.getWorld();
							DimensionChunkCoords coords=DimensionChunkCoords.readFromNBT(ItemNBTHelper.getTagCompound(drill.sample, "coords"));
							
							OilWorldInfo info = PumpjackHandler.getOilWorldInfo(world, coords.x, coords.z);
							if(info.getType() != null){
								ItemNBTHelper.putString(drill.sample, "resType", info.getType().name);
								ItemNBTHelper.putInt(drill.sample, "oil", info.current);
								
								if(event.getPlayer()!=null){
									int cap=info.capacity;
									int cur=info.current;
									ReservoirType type=info.type;
									ReservoirType override=info.overrideType;
									
									String out=String.format("%.3f/%.3fmB of %s%s", cur/1000D, cap/1000D, (override!=null?override.name:type.name), (override!=null?" [OVERRIDDEN]":""));
									event.getPlayer().sendStatusMessage(new StringTextComponent(out), true);
								}
								
							}else{
								ItemNBTHelper.putInt(drill.sample, "oil", 0);
								
								if(event.getPlayer()!=null)
									event.getPlayer().sendStatusMessage(new StringTextComponent("Found nothing."), true);
							}
						}catch(Exception e){
							ImmersivePetroleum.log.warn(e);
						}
					}
				}
			}
		}
	}
	
	static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	@OnlyIn(Dist.CLIENT)
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
				
				int amnt = ItemNBTHelper.getInt(stack, "oil");
				List<ITextComponent> tooltip = event.getToolTip();
				if(res != null && amnt > 0){
					int est = (amnt / 1000) * 1000; // ?!
					String fluidName = new FluidStack(res.getFluid(), 1).getDisplayName().getUnformattedComponentText();
					
					tooltip.add(2, new StringTextComponent(I18n.format("chat.immersivepetroleum.info.coresample.oil", FORMATTER.format(est), fluidName)));
				}else{
					if(res != null && res.replenishRate > 0){
						String fluidName = new FluidStack(res.getFluid(), 1).getDisplayName().getUnformattedComponentText();
						tooltip.add(2, new StringTextComponent(I18n.format("chat.immersivepetroleum.info.coresample.oilRep", res.replenishRate, fluidName)));
					}else{
						tooltip.add(2, new StringTextComponent(I18n.format("chat.immersivepetroleum.info.coresample.noOil")));
					}
				}
			}
		}
	}
	
//	@SubscribeEvent(priority = EventPriority.HIGH)
//	public static void onLogin(PlayerLoggedInEvent event){
		// ExcavatorHandler.allowPacketsToPlayer = true;
//		if(!event.getPlayer().world.isRemote){
//			HashMap<ReservoirType, Integer> packetMap = new HashMap<ReservoirType, Integer>();
//			for(Entry<ReservoirType, Integer> e:PumpjackHandler.reservoirList.entrySet()){
//				if(e.getKey() != null && e.getValue() != null)
//					packetMap.put(e.getKey(), e.getValue());
//			}
//			
//			//IPPacketHandler.sendToPlayer(event.getPlayer(), new MessageReservoirListSync(packetMap));
//		}
//	}
	
	@OnlyIn(Dist.CLIENT)
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
								TileEntity tileEntity = player.world.getTileEntity(new BlockPos(mop.getHitVec()));
								
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
											int est = (amnt / 1000) * 1000; // Why?
											String fluidName = new FluidStack(res.getFluid(), 1).getDisplayName().getUnformattedComponentText();
											
											s = I18n.format("chat.immersivepetroleum.info.coresample.oil", FORMATTER.format(est), fluidName);
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
	
	@OnlyIn(Dist.CLIENT)
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
							
							float speed=(float)(100*Math.sqrt(vec.x*vec.x + vec.z*vec.z));
							
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
	public static void handleBoatImmunity(LivingAttackEvent event){
		if(event.getSource() == DamageSource.LAVA || event.getSource() == DamageSource.ON_FIRE || event.getSource() == DamageSource.IN_FIRE){
			LivingEntity entity = event.getEntityLiving();
			if(entity.getRidingEntity() instanceof SpeedboatEntity){
				SpeedboatEntity boat = (SpeedboatEntity) entity.getRidingEntity();
				if(boat.isFireproof){
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void handleBoatImmunity(PlayerTickEvent event){
		PlayerEntity entity = event.player;
		if(entity.isBurning() && entity.getRidingEntity() instanceof SpeedboatEntity){
			SpeedboatEntity boat = (SpeedboatEntity) entity.getRidingEntity();
			if(boat.isFireproof){
				entity.extinguish();
				DataParameter<Byte> FLAGS = SpeedboatEntity.getFlags();
				byte b0 = ((Byte) entity.getDataManager().get(FLAGS)).byteValue();
				
				entity.getDataManager().set(FLAGS, Byte.valueOf((byte) (b0 & ~(1 << 0))));
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
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
	
	@OnlyIn(Dist.CLIENT)
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
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void handleLubricatingMachinesClient(ClientTickEvent event){
		if(event.phase == Phase.END && Minecraft.getInstance().world != null){
			handleLubricatingMachines(Minecraft.getInstance().world);
		}
	}
	
	@SubscribeEvent
	public static void handleLubricatingMachinesServer(WorldTickEvent event){
		if(event.phase == Phase.END){
			handleLubricatingMachines(event.world);
		}
	}
	
	
	static final Random random=new Random();
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void handleLubricatingMachines(World world){
		Set<LubricatedTileInfo> toRemove = new HashSet<LubricatedTileInfo>();
		for(LubricatedTileInfo info:LubricatedHandler.lubricatedTiles){
			if(info.world == world.getDimension().getType() && world.isAreaLoaded(info.pos, 0)){
				TileEntity te = world.getTileEntity(info.pos);
				ILubricationHandler lubeHandler = LubricatedHandler.getHandlerForTile(te);
				if(lubeHandler != null){
					if(lubeHandler.isMachineEnabled(world, te)){
						lubeHandler.lubricate(world, info.ticks, te);
					}
					
					if(world.isRemote){
						if(te instanceof MultiblockPartTileEntity){
							MultiblockPartTileEntity<?> part = (MultiblockPartTileEntity<?>) te;
							BlockState n = Fluids.fluidLubricant.block.getDefaultState();
							Vec3i size=lubeHandler.getStructureDimensions();
							
							int numBlocks = (int)(size.getX()*size.getY()*size.getZ()*0.25F);
							
							for(int i = 0;i < numBlocks;i++){
								BlockPos pos = part.getBlockPosForPos(new BlockPos(size.getX()*random.nextFloat(), size.getY()*random.nextFloat(), size.getZ()*random.nextFloat()));
								if(world.getBlockState(pos)==Blocks.AIR.getDefaultState())
									continue;
								
								TileEntity te2 = world.getTileEntity(info.pos);
								if(te2 != null && te2 instanceof MultiblockPartTileEntity){
									if(((MultiblockPartTileEntity<?>) te2).master() == part.master()){
										for(Direction facing:Direction.Plane.HORIZONTAL){
											if(world.rand.nextInt(30) == 0){// && world.getBlockState(pos.offset(facing)).getBlock().isReplaceable(world, pos.offset(facing))){
												Vec3i direction = facing.getDirectionVec();
												world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n),
														pos.getX() + .5f + direction.getX() * .65f,
														pos.getY() + 1,
														pos.getZ() + .5f + direction.getZ() * .65f,
														0, 0, 0);
											}
										}
									}
								}
							}
						}
					}
					
					info.ticks--;
					if(info.ticks == 0) toRemove.add(info);
				}
			}
		}
		
		for(LubricatedTileInfo info:toRemove){
			LubricatedHandler.lubricatedTiles.remove(info);
		}
	}
	
	@SubscribeEvent
	public static void onEntityJoiningWorld(EntityJoinWorldEvent event){
		if(IPConfig.MISCELLANEOUS.autounlock_recipes.get() && event.getEntity() instanceof PlayerEntity){
			if(event.getEntity() instanceof FakePlayer){
				return;
			}
			
			List<IRecipe<?>> l = new ArrayList<IRecipe<?>>();
			Collection<IRecipe<?>> recipes=event.getWorld().getRecipeManager().getRecipes();
			recipes.forEach(recipe->{
				ResourceLocation name = recipe.getId();
				if(name.getNamespace()==ImmersivePetroleum.MODID){
					if(recipe.getRecipeOutput().getItem() != null){
						l.add(recipe);
					}
				}
			});
			
			((PlayerEntity) event.getEntity()).unlockRecipes(l);
			
		}
	}
	
	@SubscribeEvent
	public static void test(LivingEvent.LivingUpdateEvent event){
		if(event.getEntityLiving() instanceof PlayerEntity){
			// event.getEntityLiving().setFire(1);
		}
	}
	
	public static Map<Integer, List<BlockPos>> napalmPositions = new HashMap<>();
	public static Map<Integer, List<BlockPos>> toRemove = new HashMap<>();
	
	@SubscribeEvent
	public static void handleNapalm(WorldTickEvent event){
		int d = event.world.getDimension().getType().getId();
		
		if(event.phase == Phase.START){
			toRemove.put(d, new ArrayList<>());
			if(napalmPositions.get(d) != null){
				List<BlockPos> iterate = new ArrayList<>(napalmPositions.get(d));
				for(BlockPos position:iterate){
					BlockState state = event.world.getBlockState(position);
					if(state.getBlock() instanceof FlowingFluidBlock && state.getBlock() == Fluids.fluidNapalm.block){
						((BlockNapalm) Fluids.fluidNapalm).processFire(event.world, position);
					}
					toRemove.get(d).add(position);
				}
			}
		}else if(event.phase == Phase.END){
			if(toRemove.get(d) != null && napalmPositions.get(d) != null){
				for(BlockPos position:toRemove.get(d)){
					napalmPositions.get(d).remove(position);
				}
			}
		}
	}
}
