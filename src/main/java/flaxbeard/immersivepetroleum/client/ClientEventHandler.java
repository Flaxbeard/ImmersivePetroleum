package flaxbeard.immersivepetroleum.client;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ItemOverlayUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleTileEntity;
import blusunrize.immersiveengineering.common.items.BuzzsawItem;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.SpeedloaderItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import flaxbeard.immersivepetroleum.common.util.MCUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

public class ClientEventHandler{
	
	@SubscribeEvent
	public void renderLast(RenderWorldLastEvent event){
		MatrixStack transform=event.getMatrixStack();
		Minecraft mc = Minecraft.getInstance();
		
		/*
		if(IPClientConfig.MISCELLANEOUS.sample_displayBorder.get() && mc.player != null){
			PlayerEntity player = mc.player;
			
			transform.push();
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
						//renderChunkBorder(transform, pos.x >> 4 << 4, pos.z >> 4 << 4);
					}
				}
			}
			transform.pop();
		}
		*/
		
		transform.push();
		{
			if(mc.player != null){
				ItemStack mainItem = mc.player.getHeldItemMainhand();
				ItemStack secondItem = mc.player.getHeldItemOffhand();
				
				boolean main = (mainItem != null && !mainItem.isEmpty()) && mainItem.getItem() == IPContent.Blocks.auto_lubricator.asItem();
				boolean off = (secondItem != null && !secondItem.isEmpty()) && secondItem.getItem() == IPContent.Blocks.auto_lubricator.asItem();
				
				if(main || off){
					BlockRendererDispatcher blockDispatcher = MCUtil.getBlockRenderer();
					IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
					
					// Anti-Jiggle when moving
					Vector3d renderView = MCUtil.getGameRenderer().getActiveRenderInfo().getProjectedView();
					transform.translate(-renderView.x, -renderView.y, -renderView.z);
					
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
											BlockState targetState = mc.player.world.getBlockState(targetPos);
											BlockState targetStateUp = mc.player.world.getBlockState(targetPos.up());
											if(targetState.getMaterial().isReplaceable() && targetStateUp.getMaterial().isReplaceable()){
												IVertexBuilder vBuilder = buffer.getBuffer(RenderType.getTranslucent());
												transform.push();
												{
													transform.translate(targetPos.getX(), targetPos.getY() - 1, targetPos.getZ());
													
													BlockState state = IPContent.Blocks.auto_lubricator.getDefaultState().with(AutoLubricatorBlock.FACING, targetFacing);
													IBakedModel model = blockDispatcher.getModelForState(state);
													blockDispatcher.getBlockModelRenderer().renderModel(transform.getLast(), vBuilder, null, model, 1.0F, 1.0F, 1.0F, 0xF000F0, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
													
												}
												transform.pop();
												
												ShaderUtil.alpha_static(0.5f, mc.player.ticksExisted);
												buffer.finish();
												ShaderUtil.releaseShader();
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
		transform.pop();
	}
	
	public void renderChunkBorder(MatrixStack transform, int chunkX, int chunkZ){
		PlayerEntity player = MCUtil.getPlayer();
		
		double px = player.getPosX();
		double py = player.getPosY();
		double pz = player.getPosZ();
		int y = Math.min((int) py - 2, player.getEntityWorld().getChunk(chunkX, chunkZ).getHeight());
		float h = (float) Math.max(32, py - y + 4);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		
		float r = Lib.COLOUR_F_ImmersiveOrange[0];
		float g = Lib.COLOUR_F_ImmersiveOrange[1];
		float b = Lib.COLOUR_F_ImmersiveOrange[2];
		transform.translate(chunkX - px, y + 2 - py, chunkZ - pz);
		// transform.lineWidth(5f);
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
	}
	
	static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handleItemTooltip(ItemTooltipEvent event){
		ItemStack stack = event.getItemStack();
		if(stack.getItem() instanceof CoresampleItem){
			if(ItemNBTHelper.hasKey(stack, "resAmount")){
				String resName = ItemNBTHelper.hasKey(stack, "resType") ? ItemNBTHelper.getString(stack, "resType") : null;
				if(ItemNBTHelper.hasKey(stack, "resAmount") && resName == null){
					resName = "";
				}
				
				ReservoirType res = null;
				for(ReservoirType type:PumpjackHandler.reservoirs.values()){
					if(resName.equalsIgnoreCase(type.name)){
						res = type;
					}
				}
				
				List<ITextComponent> tooltip = event.getToolTip();
				int amnt = ItemNBTHelper.getInt(stack, "resAmount");
				int tipPos = Math.max(0, tooltip.size() - 5);
				
				if(res != null && amnt > 0){
					int est = (amnt / 1000) * 1000;
					ITextComponent fluidName = new FluidStack(res.getFluid(), 1).getDisplayName();
					
					ITextComponent header = new TranslationTextComponent("chat.immersivepetroleum.info.coresample.oil", fluidName).mergeStyle(TextFormatting.GRAY);
					
					ITextComponent info = new StringTextComponent("  " + FORMATTER.format(est) + " mB").mergeStyle(TextFormatting.DARK_GRAY);
					
					tooltip.add(tipPos, header);
					tooltip.add(tipPos + 1, info);
				}else{
					if(res != null && res.replenishRate > 0){
						String fluidName = new FluidStack(res.getFluid(), 1).getDisplayName().getUnformattedComponentText();
						
						ITextComponent header = new TranslationTextComponent("chat.immersivepetroleum.info.coresample.oil", fluidName).mergeStyle(TextFormatting.GRAY);
						
						ITextComponent info = new StringTextComponent("  " + I18n.format("chat.immersivepetroleum.info.coresample.oilRep", res.replenishRate, fluidName)).mergeStyle(TextFormatting.GRAY);
						
						tooltip.add(tipPos, header);
						tooltip.add(tipPos + 1, info);
					}else{
						tooltip.add(tipPos, new StringTextComponent(I18n.format("chat.immersivepetroleum.info.coresample.noOil")).mergeStyle(TextFormatting.GRAY));
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void renderInfoOverlays(RenderGameOverlayEvent.Post event){
		if(MCUtil.getPlayer() != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			PlayerEntity player = MCUtil.getPlayer();
			
			if(MCUtil.getHitResult() != null){
				boolean hammer = player.getHeldItem(Hand.MAIN_HAND) != null && Utils.isHammer(player.getHeldItem(Hand.MAIN_HAND));
				RayTraceResult mop = MCUtil.getHitResult();
				
				if(mop != null){
					switch(mop.getType()){
						case BLOCK:{
							if(mop instanceof BlockRayTraceResult){
								TileEntity tileEntity = player.world.getTileEntity(((BlockRayTraceResult) mop).getPos());
								
								if(tileEntity instanceof CoresampleTileEntity){
									IBlockOverlayText overlayBlock = (IBlockOverlayText) tileEntity;
									ITextComponent[] text = overlayBlock.getOverlayText(player, mop, hammer);
									ItemStack coresample = ((CoresampleTileEntity) tileEntity).coresample;
									
									if(ItemNBTHelper.hasKey(coresample, "resAmount") && text != null && text.length > 0){
										String resName = ItemNBTHelper.hasKey(coresample, "resType") ? ItemNBTHelper.getString(coresample, "resType") : "";
										int amnt = ItemNBTHelper.getInt(coresample, "resAmount");
										int i = text.length;
										
										ReservoirType res = null;
										for(ReservoirType type:PumpjackHandler.reservoirs.values()){
											if(resName.equals(type.name)){
												res = type;
											}
										}
										
										// LanguageMap.getInstance().func_241870_a(fluidName)
										
										ITextComponent display = new TranslationTextComponent("chat.immersivepetroleum.info.coresample.noOil");
										
										if(res != null && amnt > 0){
											ITextComponent fluidName = new FluidStack(res.getFluid(), 1).getDisplayName();
											display = new TranslationTextComponent("chat.immersivepetroleum.info.coresample.oil", fluidName);
										}else if(res != null && res.replenishRate > 0){
											ITextComponent fluidName = new FluidStack(res.getFluid(), 1).getDisplayName();
											display = new TranslationTextComponent("chat.immersivepetroleum.info.coresample.oilRep", res.replenishRate, fluidName);
										}
										
										int fx = event.getWindow().getScaledWidth() / 2 + 8;
										int fy = event.getWindow().getScaledHeight() / 2 + 8 + i * MCUtil.getFont().FONT_HEIGHT;
										
										IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
										MCUtil.getFont().drawEntityText(LanguageMap.getInstance().func_241870_a(display), fx, fy, 0xFFFFFFFF, true, event.getMatrixStack().getLast().getMatrix(), buffer, false, 0, 0xF000F0);
										buffer.finish();
									}
								}
							}
							break;
						}
						case ENTITY:{
							EntityRayTraceResult rtr = (EntityRayTraceResult) mop;
							if(rtr.getEntity() instanceof MotorboatEntity){
								String[] text = ((MotorboatEntity) rtr.getEntity()).getOverlayText(player, mop);
								
								if(text != null && text.length > 0){
									FontRenderer font = MCUtil.getFont();
									int col = 0xffffff;
									for(int i = 0;i < text.length;i++){
										if(text[i] != null){
											int fx = event.getWindow().getScaledWidth() / 2 + 8;
											int fy = event.getWindow().getScaledHeight() / 2 + 8 + i * font.FONT_HEIGHT;
											font.drawStringWithShadow(event.getMatrixStack(), text[i], fx, fy, col);
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
	
	@SubscribeEvent
	public void onRenderOverlayPost(RenderGameOverlayEvent.Post event){
		if(MCUtil.getPlayer() != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			PlayerEntity player = MCUtil.getPlayer();
			MatrixStack matrix = event.getMatrixStack();
			
			if(player.getRidingEntity() instanceof MotorboatEntity){
				int offset = 0;
				boolean holdingDebugItem = false;
				for(Hand hand:Hand.values()){
					if(!player.getHeldItem(hand).isEmpty()){
						ItemStack equipped = player.getHeldItem(hand);
						if((equipped.getItem() instanceof DrillItem) || (equipped.getItem() instanceof ChemthrowerItem) || (equipped.getItem() instanceof BuzzsawItem)){
							offset -= 85;
						}else if((equipped.getItem() instanceof RevolverItem) || (equipped.getItem() instanceof SpeedloaderItem)){
							offset -= 65;
						}else if(equipped.getItem() instanceof RailgunItem){
							offset -= 50;
						}else if(equipped.getItem() instanceof IEShieldItem){
							offset -= 40;
						}
						
						if(equipped.getItem() instanceof DebugItem){
							holdingDebugItem = true;
						}
					}
				}
				
				matrix.push();
				{
					int scaledWidth = MCUtil.getWindow().getScaledWidth();
					int scaledHeight = MCUtil.getWindow().getScaledHeight();
					
					MotorboatEntity boat = (MotorboatEntity) player.getRidingEntity();
					IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
					IVertexBuilder builder = ItemOverlayUtils.getHudElementsBuilder(buffer);
					
					int rightOffset = 0;
					if(MCUtil.getOptions().showSubtitles)
						rightOffset += 100;
					float dx = scaledWidth - rightOffset - 16;
					float dy = scaledHeight + offset;
					matrix.push();
					{
						matrix.translate(dx, dy, 0);
						GuiHelper.drawTexturedRect(builder, matrix, -24, -68, 31, 62, 256f, 179, 210, 9, 71);
						
						matrix.translate(-23, -37, 0);
						int capacity = boat.getMaxFuel();
						if(capacity > 0){
							FluidStack fuel = boat.getContainedFluid();
							int amount = fuel.getAmount();
							float cap = (float) capacity;
							float angle = 83 - (166 * amount / cap);
							matrix.push();
							matrix.rotate(new Quaternion(0, 0, angle, true));
							GuiHelper.drawTexturedRect(builder, matrix, 6, -2, 24, 4, 256f, 91, 123, 80, 87);
							matrix.pop();
							matrix.translate(23, 37, 0);
							
							GuiHelper.drawTexturedRect(builder, matrix, -41, -73, 53, 72, 256f, 8, 61, 4, 76);
						}
					}
					matrix.pop();
					
					buffer.finish();
					
					if(holdingDebugItem && Minecraft.getInstance().fontRenderer != null){
						matrix.push();
						{
							matrix.translate(dx, dy, 0);
							FontRenderer font = MCUtil.getFont();
							
							int capacity = boat.getMaxFuel();
							FluidStack fs = boat.getContainedFluid();
							int amount = fs == FluidStack.EMPTY || fs.getFluid() == null ? 0 : fs.getAmount();
							
							Vector3d vec = boat.getMotion();
							
							float rot = boat.propellerYRotation;
							
							float speed = MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z);
							
							String out0 = String.format(Locale.US, "Fuel: %s/%sMB", amount, capacity);
							String out1 = String.format(Locale.US, "Speed: %.2f", speed);
							String out2 = String.format(Locale.US, "Rot: %s", rot);
							font.drawStringWithShadow(matrix, out0, -90, -104, 0xFFFFFFFF);
							font.drawStringWithShadow(matrix, out1, -90, -95, 0xFFFFFFFF);
							font.drawStringWithShadow(matrix, out2, -90, -86, 0xFFFFFFFF);
						}
						matrix.pop();
					}
				}
				matrix.pop();
			}
		}
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(RenderBlockOverlayEvent event){
		PlayerEntity entity = event.getPlayer();
		if(event.getOverlayType() == OverlayType.FIRE && entity.isBurning() && entity.getRidingEntity() instanceof MotorboatEntity){
			MotorboatEntity boat = (MotorboatEntity) entity.getRidingEntity();
			if(boat.isFireproof){
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void handleFireRender(RenderPlayerEvent.Pre event){
		PlayerEntity entity = event.getPlayer();
		if(entity.isBurning() && entity.getRidingEntity() instanceof MotorboatEntity){
			MotorboatEntity boat = (MotorboatEntity) entity.getRidingEntity();
			if(boat.isFireproof){
				entity.extinguish();
			}
		}
	}
	
	@SubscribeEvent
	public void handleLubricatingMachinesClient(ClientTickEvent event){
		if(event.phase == Phase.END && Minecraft.getInstance().world != null){
			CommonEventHandler.handleLubricatingMachines(MCUtil.getWorld());
		}
	}
	
}
