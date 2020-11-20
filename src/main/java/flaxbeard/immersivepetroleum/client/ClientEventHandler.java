package flaxbeard.immersivepetroleum.client;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.ManualElementMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleTileEntity;
import blusunrize.immersiveengineering.common.items.BuzzsawItem;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.SpecialManualElement;
import blusunrize.lib.manual.gui.ManualScreen;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
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
	@Deprecated private static Object lastGui = null;
	@Deprecated private static Field FIELD_PAGES;
	@Deprecated private static Field FIELD_SPECIAL;
	@Deprecated private static Field FIELD_MULTIBLOCK;
	@Deprecated
	public void guiOpen(GuiOpenEvent event){
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
				//IPPacketHandler.sendToServer(new MessageCloseBook(name));
				
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
	public void renderLast(RenderWorldLastEvent event){
		MatrixStack transform=event.getMatrixStack();
		Minecraft mc = Minecraft.getInstance();
		
		/*
		if(IPConfig.MISCELLANEOUS.sample_displayBorder.get() && mc.player != null){
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
					BlockRendererDispatcher blockDispatcher=ClientUtils.mc().getBlockRendererDispatcher();
					IRenderTypeBuffer.Impl buffer=IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
					
					// Anti-Jiggle when moving
					Vector3d renderView = ClientUtils.mc().gameRenderer.getActiveRenderInfo().getProjectedView();
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
											BlockState targetState=mc.player.world.getBlockState(targetPos);
											BlockState targetStateUp=mc.player.world.getBlockState(targetPos.up());
											if(targetState.getMaterial().isReplaceable() && targetStateUp.getMaterial().isReplaceable()){
												IVertexBuilder vBuilder=buffer.getBuffer(RenderType.getTranslucent());
												transform.push();
												{
													transform.translate(targetPos.getX(), targetPos.getY()-1, targetPos.getZ());
													
													BlockState state=IPContent.Blocks.auto_lubricator.getDefaultState()
															.with(AutoLubricatorBlock.FACING, targetFacing);
													IBakedModel model=blockDispatcher.getModelForState(state);
													blockDispatcher.getBlockModelRenderer()
														.renderModel(transform.getLast(), vBuilder, null, model, 1.0F, 1.0F, 1.0F, 0xF000F0, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
													
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
		PlayerEntity player = ClientUtils.mc().player;
		
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
		//transform.lineWidth(5f);
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
					
					ITextComponent header=new TranslationTextComponent("chat.immersivepetroleum.info.coresample.oil", fluidName)
							.mergeStyle(TextFormatting.GRAY);
					
					ITextComponent info=new StringTextComponent("  "+FORMATTER.format(est)+" mB")
							.mergeStyle(TextFormatting.DARK_GRAY);
					
					tooltip.add(tipPos, header);
					tooltip.add(tipPos+1, info);
				}else{
					if(res != null && res.replenishRate > 0){
						String fluidName = new FluidStack(res.getFluid(), 1).getDisplayName().getUnformattedComponentText();
						
						ITextComponent header=new TranslationTextComponent("chat.immersivepetroleum.info.coresample.oil", fluidName)
								.mergeStyle(TextFormatting.GRAY);
						
						ITextComponent info=new StringTextComponent("  "+I18n.format("chat.immersivepetroleum.info.coresample.oilRep", res.replenishRate, fluidName))
								.mergeStyle(TextFormatting.GRAY);
						
						tooltip.add(tipPos, header);
						tooltip.add(tipPos+1, info);
					}else{
						tooltip.add(tipPos, new StringTextComponent(I18n.format("chat.immersivepetroleum.info.coresample.noOil")).mergeStyle(TextFormatting.GRAY));
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void renderDebuggingOverlay(RenderGameOverlayEvent.Post event){
		if(ClientUtils.mc().player != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			PlayerEntity player = ClientUtils.mc().player;
			
			ItemStack main=player.getHeldItem(Hand.MAIN_HAND);
			ItemStack off=player.getHeldItem(Hand.OFF_HAND);
			
			if((main!=ItemStack.EMPTY && main.getItem()==IPContent.debugItem) || (off!=ItemStack.EMPTY && off.getItem()==IPContent.debugItem)){
				if(ClientUtils.mc().objectMouseOver != null){
					RayTraceResult rt = ClientUtils.mc().objectMouseOver;
					
					if(rt.getType()==RayTraceResult.Type.BLOCK && rt instanceof BlockRayTraceResult){
						BlockRayTraceResult result=(BlockRayTraceResult)rt;
						
						World world=player.world;
						
						TileEntity te=world.getTileEntity(result.getPos());
						if(te instanceof DistillationTowerTileEntity){
							DistillationTowerTileEntity tower=(DistillationTowerTileEntity)te;
							if(!tower.offsetToMaster.equals(BlockPos.ZERO)){
								tower=tower.master();
							}
							
							List<String> strings=new ArrayList<>();
							strings.add("Distillation Tower");
							strings.add(tower.energyStorage.getEnergyStored()+"/"+tower.energyStorage.getMaxEnergyStored()+"RF");
							
							{
								MultiFluidTank tank=tower.tanks[DistillationTowerTileEntity.TANK_INPUT];
								if(tank.fluids.size()>0){
									strings.add("Input");
									for(int i=0;i<tank.fluids.size();i++){
										FluidStack fstack=tank.fluids.get(i);
										strings.add("  "+fstack.getDisplayName()+" "+fstack.getAmount()+"mB");
									}
								}
							}
							
							{
								MultiFluidTank tank=tower.tanks[DistillationTowerTileEntity.TANK_OUTPUT];
								if(tank.fluids.size()>0){
									strings.add("Output");
									for(int i=0;i<tank.fluids.size();i++){
										FluidStack fstack=tank.fluids.get(i);
										strings.add("  "+fstack.getDisplayName()+" "+fstack.getAmount()+"mB");
									}
								}
							}
							
							MatrixStack matrix=event.getMatrixStack();
							matrix.push();
							for(int i=0;i<strings.size();i++){
								ClientUtils.mc().fontRenderer.drawStringWithShadow(matrix, strings.get(i), 1, 1+(i * ClientUtils.font().FONT_HEIGHT), 0xffffff);
							}
							matrix.pop();
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void renderInfoOverlays(RenderGameOverlayEvent.Post event){
		if(ClientUtils.mc().player != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			PlayerEntity player = ClientUtils.mc().player;
			
			if(ClientUtils.mc().objectMouseOver != null){
				boolean hammer = player.getHeldItem(Hand.MAIN_HAND) != null && Utils.isHammer(player.getHeldItem(Hand.MAIN_HAND));
				RayTraceResult mop = ClientUtils.mc().objectMouseOver;
				
				if(mop != null){
					switch(mop.getType()){
						case BLOCK:{
							if(mop instanceof BlockRayTraceResult){
								TileEntity tileEntity = player.world.getTileEntity(((BlockRayTraceResult)mop).getPos());
								
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
										int fy = event.getWindow().getScaledHeight() / 2 + 8 + i * ClientUtils.font().FONT_HEIGHT;
										
										IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
										ClientUtils.font().func_238416_a_(LanguageMap.getInstance().func_241870_a(display),
												fx, fy, 0xFFFFFFFF, true,
												event.getMatrixStack().getLast().getMatrix(), buffer,
												false, 0, 0xF000F0);
										buffer.finish();
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
	
	@SuppressWarnings("unused")
	@SubscribeEvent
	public void onRenderOverlayPost(RenderGameOverlayEvent.Post event){
		if(ClientUtils.mc().player != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			PlayerEntity player = ClientUtils.mc().player;
			MatrixStack transform=event.getMatrixStack();
			
			if(player.getRidingEntity() instanceof SpeedboatEntity){
				int offset = 0;
				boolean holdingDebugItem=false;
				for(Hand hand:Hand.values()){
					if(!player.getHeldItem(hand).isEmpty()){
						ItemStack equipped = player.getHeldItem(hand);
						if((equipped.getItem() instanceof DrillItem) || (equipped.getItem() instanceof ChemthrowerItem) || (equipped.getItem() instanceof BuzzsawItem)){
							offset -= 85;
						}
						if(equipped.getItem() instanceof DebugItem){
							holdingDebugItem=true;
						}
					}
				}
				
				transform.push();
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
					//transform.color4f(1, 1, 1, 1);
					transform.translate(dx, dy + offset, 0);
					//ClientUtils.drawTexturedRect(-24, -68, w, h, uMin, uMax, vMin, vMax); // FIXME
					
					GL11.glTranslated(-23, -37, 0);
					SpeedboatEntity boat = (SpeedboatEntity) player.getRidingEntity();
					int capacity = boat.getMaxFuel();
					
					FluidStack fs = boat.getContainedFluid();
					int amount = fs == FluidStack.EMPTY || fs.getFluid() == null ? 0 : fs.getAmount();
					
					float cap = (float) capacity;
					float angle = 83 - (166 * amount / cap);
					transform.rotate(new Quaternion(0, 0, angle, true));
					//ClientUtils.drawTexturedRect(6, -2, 24, 4, 91 / 256f, 123 / 256f, 80 / 256f, 87 / 256f); // FIXME
					transform.rotate(new Quaternion(0, 0, -angle, true));
					
					transform.translate(23, 37, 0);
					//ClientUtils.drawTexturedRect(-41, -73, 53, 72, 8 / 256f, 61 / 256f, 4 / 256f, 76 / 256f); // FIXME
					
					if(holdingDebugItem){
						transform.push();
						{
							FontRenderer font=Minecraft.getInstance().fontRenderer;
							if(font!=null){
								Vector3d vec=boat.getMotion();
								
								float speed=(float)(Math.sqrt(vec.x*vec.x + vec.z*vec.z));
								
								String out0=String.format(Locale.US, "Fuel: %s/%sMB", amount,capacity);
								String out1=String.format(Locale.US, "Speed: %.2f", speed);
								font.drawStringWithShadow(transform, out0, -65, -94, 0xFFFFFFFF);
								font.drawStringWithShadow(transform, out1, -65, -85, 0xFFFFFFFF);
							}
						}
						transform.pop();
					}
				}
				transform.pop();
			}
		}
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(RenderBlockOverlayEvent event){
		PlayerEntity entity = event.getPlayer();
		if(event.getOverlayType() == OverlayType.FIRE && entity.isBurning() && entity.getRidingEntity() instanceof SpeedboatEntity){
			SpeedboatEntity boat = (SpeedboatEntity) entity.getRidingEntity();
			if(boat.isFireproof){
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void handleFireRender(RenderPlayerEvent.Pre event){
		PlayerEntity entity = event.getPlayer();
		if(entity.isBurning() && entity.getRidingEntity() instanceof SpeedboatEntity){
			SpeedboatEntity boat = (SpeedboatEntity) entity.getRidingEntity();
			if(boat.isFireproof){
				entity.extinguish();
			}
		}
	}
	
	@SubscribeEvent
	public void handleLubricatingMachinesClient(ClientTickEvent event){
		if(event.phase == Phase.END && Minecraft.getInstance().world != null){
			CommonEventHandler.handleLubricatingMachines(Minecraft.getInstance().world);
		}
	}
	
}
