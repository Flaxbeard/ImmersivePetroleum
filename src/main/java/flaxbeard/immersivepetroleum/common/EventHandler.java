package flaxbeard.immersivepetroleum.common;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.items.ItemCoresample;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import blusunrize.lib.manual.gui.GuiManual;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.client.ShaderUtil;
import flaxbeard.immersivepetroleum.common.Config.IPConfig;
import flaxbeard.immersivepetroleum.common.items.ItemSchematic;
import flaxbeard.immersivepetroleum.common.network.CloseBookPacket;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;

public class EventHandler
{
	@SubscribeEvent
	public void onSave(WorldEvent.Save event)
	{
		IPSaveData.setDirty(0);
	}
	
	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event)
	{
		IPSaveData.setDirty(0);
	}
	
	private Object lastGui = null;
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void guiOpen(GuiOpenEvent event)
	{
		if (event.getGui() == null && lastGui instanceof GuiManual)
		{
			GuiManual gui = (GuiManual) lastGui;
			String name = null;

			ManualInstance inst = gui.getManual();
			if (inst != null)
			{
				ManualEntry entry = inst.getEntry(gui.getSelectedEntry());
				if (entry != null)
				{
					IManualPage[] pages = entry.getPages();
					for (int i = 0; i < pages.length; i++)
					{
						IManualPage page = pages[i];
						if (page instanceof ManualPageMultiblock)
						{
							ManualPageMultiblock mbPage = (ManualPageMultiblock) page;
							IMultiblock mb = ReflectionHelper.getPrivateValue(ManualPageMultiblock.class, mbPage, 0);
							if (mb != null)
							{
								if (name == null || i == gui.page)
								{
									name = mb.getUniqueName();
								}
							}
						}
					}
				}
			}
			EntityPlayer p = ClientUtils.mc().thePlayer;
			
			ItemStack mainItem = p.getHeldItemMainhand();
			ItemStack offItem = p.getHeldItemOffhand();

			boolean main = mainItem != null && mainItem.getItem() == IEContent.itemTool && mainItem.getItemDamage() == 3;
			boolean off = offItem != null && offItem.getItem() == IEContent.itemTool && offItem.getItemDamage() == 3;
			ItemStack target = main ? mainItem : offItem;
			
			if (main || off)
			{
				IPPacketHandler.INSTANCE.sendToServer(new CloseBookPacket(name));

				if (name == null && ItemNBTHelper.hasKey(target, "lastMultiblock"))
				{
					ItemNBTHelper.remove(target, "lastMultiblock");
				}
				else if (name != null)
				{
					ItemNBTHelper.setString(target, "lastMultiblock", name);
				}
			}
		}

		lastGui = event.getGui();
	}
	
	
	
	boolean lastDown = false;
	private int tempCode = 99999;
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void handleKeypress(ClientTickEvent event)
	{
		Minecraft mc = ClientUtils.mc();
		if (event.phase == Phase.START && mc.thePlayer != null)
		{
			//
			ItemStack mainItem = mc.thePlayer.getHeldItemMainhand();
			ItemStack secondItem = mc.thePlayer.getHeldItemOffhand();
			
			boolean main = mainItem != null && mainItem.getItem() == IPContent.itemSchematic && ItemNBTHelper.hasKey(mainItem, "multiblock");
			boolean off = secondItem != null && secondItem.getItem() == IPContent.itemSchematic && ItemNBTHelper.hasKey(secondItem, "multiblock");
			ItemStack target = main ? mainItem : secondItem;
			World world = mc.theWorld;
			
			if (main || off)
			{
				int code = mc.gameSettings.keyBindPickBlock.getKeyCode();
				if (code != 99999 && !ItemNBTHelper.hasKey(target, "pos"))
				{
					if (code < 0)
					{
						code += 100;
						if (Mouse.isButtonDown(code))
						{
							if (!lastDown)
							{
								if (mc.thePlayer.isSneaking())
								{
									ItemSchematic.flipClient(target);
								}
								else
								{
									ItemSchematic.rotateClient(target);
								}
							}
							tempCode = code - 100;
							mc.gameSettings.keyBindPickBlock.setKeyCode(99999);
						}
					}
					else
					{
						if (Keyboard.isKeyDown(code))
						{
							if (!lastDown)
							{
								if (mc.thePlayer.isSneaking())
								{
									ItemSchematic.flipClient(target);
								}
								else
								{
									ItemSchematic.rotateClient(target);
								}
							}
							tempCode = code;
							mc.gameSettings.keyBindPickBlock.setKeyCode(99999);
						}
					}
				}
			}
		}
		else if (event.phase == Phase.END && tempCode != 99999)
		{
			mc.gameSettings.keyBindPickBlock.setKeyCode(tempCode);
			tempCode = 99999;
			lastDown = true;
		}
		else if (tempCode == 99999)
		{
			lastDown = false;
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderLast(RenderWorldLastEvent event)
	{
		GlStateManager.pushMatrix();
		Minecraft mc = Minecraft.getMinecraft();
		if (IPConfig.sample_displayBorder && mc.thePlayer != null)
		{
			ItemStack mainItem = mc.thePlayer.getHeldItemMainhand();
			ItemStack secondItem = mc.thePlayer.getHeldItemOffhand();
			
			boolean chunkBorders = false;
			for(EnumHand hand : EnumHand.values())
				if(OreDictionary.itemMatches(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta()), ClientUtils.mc().thePlayer.getHeldItem(hand),true))
				{
					chunkBorders = true;
					break;
				}
			if(!chunkBorders && ClientUtils.mc().objectMouseOver!=null && ClientUtils.mc().objectMouseOver.typeOfHit==Type.BLOCK && ClientUtils.mc().theWorld.getTileEntity(ClientUtils.mc().objectMouseOver.getBlockPos()) instanceof TileEntitySampleDrill)
				chunkBorders = true;
			
			boolean main = mainItem != null && mainItem.getItem() instanceof ItemCoresample && ItemNBTHelper.hasKey(mainItem, "coords");
			boolean off = secondItem != null && secondItem.getItem() instanceof ItemCoresample && ItemNBTHelper.hasKey(secondItem, "coords");
			ItemStack target = main ? mainItem : secondItem;
			
			if (!chunkBorders && (main || off))
			{
			
				int[] coords = ItemNBTHelper.getIntArray(target, "coords");
				//World world = DimensionManager.getWorld(coords[0]);
				//if (world.provider.getDimension() == mc.thePlayer.worldObj.provider.getDimension())
				//{
					EntityPlayer player = mc.thePlayer;

					renderChunkBorder(coords[1] << 4, coords[2] << 4);
				//}
			}
			else if (chunkBorders)
			{
				EntityPlayer player = mc.thePlayer;
				int chunkX = (int)player.posX>>4<<4;
				int chunkZ = (int)player.posZ>>4<<4;
				renderChunkBorder(chunkX, chunkZ);
			}
		}
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		
		if (IPConfig.sample_displayBorder && mc.thePlayer != null)
		{
			ItemStack mainItem = mc.thePlayer.getHeldItemMainhand();
			ItemStack secondItem = mc.thePlayer.getHeldItemOffhand();
			
			boolean main = mainItem != null && mainItem.getItem() == IPContent.itemSchematic && ItemNBTHelper.hasKey(mainItem, "multiblock");
			boolean off = secondItem != null && secondItem.getItem() == IPContent.itemSchematic && ItemNBTHelper.hasKey(secondItem, "multiblock");
			ItemStack target = main ? mainItem : secondItem;
			World world = mc.theWorld;
			
			if (main || off)
			{
				IMultiblock mb = ItemSchematic.getMultiblock(ItemNBTHelper.getString(target, "multiblock"));
				
				
				if (mb != null)
				{
					int mh = mb.getStructureManual().length;
					int ml = mb.getStructureManual()[0].length;
					int mw = mb.getStructureManual()[0][0].length;
					
					int rotate = ItemSchematic.getRotation(target);
					boolean flip = ItemSchematic.getFlipped(target);
					
					int xd = (rotate % 2 == 0) ? ml :  mw;
					int zd = (rotate % 2 == 0) ? mw :  ml;

					BlockPos hit = null;
					if (ItemNBTHelper.hasKey(target, "pos"))
					{
						NBTTagCompound pos = ItemNBTHelper.getTagCompound(target, "pos");
						int x = pos.getInteger("x");
						int y = pos.getInteger("y");
						int z = pos.getInteger("z");
						hit = new BlockPos(x, y, z);
					}
					else if (ClientUtils.mc().objectMouseOver!=null && ClientUtils.mc().objectMouseOver.typeOfHit==Type.BLOCK)
					{
						BlockPos pos = ClientUtils.mc().objectMouseOver.getBlockPos();
						
						
						IBlockState state = world.getBlockState(pos);
						if (state.getBlock().isReplaceable(world, pos) || ClientUtils.mc().objectMouseOver.sideHit != EnumFacing.UP)
						{
							hit = pos;
						}
						else
						{
							hit = pos.add(0, 1, 0);
						}
						
						
						Vec3d vec = mc.thePlayer.getLookVec();
						EnumFacing facing = (Math.abs(vec.zCoord) > Math.abs(vec.xCoord)) ? (vec.zCoord > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH) : (vec.xCoord > 0 ? EnumFacing.EAST : EnumFacing.WEST);
						
						//rotate = (facing == EnumFacing.NORTH ? 0 : (facing == EnumFacing.SOUTH ? 2: (facing == EnumFacing.EAST ? 1 : 3)));
						
						if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH)
						{
							hit = hit.add(-xd / 2, 0, 0);
						}
						else if (facing == EnumFacing.EAST || facing == EnumFacing.WEST)
						{
							hit = hit.add(0, 0, -zd / 2);
						}
						
						if (facing == EnumFacing.NORTH)
						{
							hit = hit.add(0, 0, -zd + 1);
						}
						else if (facing == EnumFacing.WEST)
						{
							hit = hit.add(-xd + 1, 0, 0);
						}
					}
					if (hit != null)
					{
						if (mb.getUniqueName().equals("IE:ExcavatorDemo"))
						{
							hit = hit.add(0, -2, 0);
						}
						double px = TileEntityRendererDispatcher.staticPlayerX;
						double py = TileEntityRendererDispatcher.staticPlayerY;
						double pz = TileEntityRendererDispatcher.staticPlayerZ;
						
						final BlockRendererDispatcher blockRender = Minecraft.getMinecraft().getBlockRendererDispatcher();
	
						
						GlStateManager.translate(hit.getX()-px, hit.getY()-py, hit.getZ()-pz);
	
						GlStateManager.disableLighting();
	
						if(Minecraft.isAmbientOcclusionEnabled())
							GlStateManager.shadeModel(GL11.GL_SMOOTH);
						else
							GlStateManager.shadeModel(GL11.GL_FLAT);
	
						ClientUtils.mc().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
						
						Tessellator tessellator = Tessellator.getInstance();
						VertexBuffer buffer = tessellator.getBuffer();
						boolean perfect = true;
						
						int idx = 0;
						for(int h = 0; h < mh; h++)
							for(int l = 0; l < ml; l++)
								for(int w = 0; w < mw; w++)
								{
									BlockPos pos = new BlockPos(l, h, w);
	
									GlStateManager.pushMatrix();
									
									if (mb.getStructureManual()[h][l][w] != null)
									{
										BlockPos blockPos = new BlockPos(0, 0, 0);
										
										int xo = l;
										int zo = w;
										
										switch (rotate)
										{
											case 1:
												zo = l;
												xo = (mw - w - 1);
												break;
											case 2:
												xo = (ml - l - 1);
												zo = (mw - w - 1);
												break;
											case 3:
												zo = (ml - l - 1);
												xo = w;
												break;
										}
										if (rotate % 2 == 1)
										{
											xo = flip ? xo : (mw - xo - 1);
										}
										else
										{
											zo = flip ? zo : (mw - zo - 1);
										}
										
										BlockPos actualPos = hit.add(xo, h, zo);
										
										ItemStack heldStack = main ? secondItem : mainItem;
										IBlockState otherState = null;
										if (heldStack != null && heldStack.getItem() instanceof ItemBlock)
										{
											otherState = ((ItemBlock)heldStack.getItem()).getBlock().getStateFromMeta(heldStack.getItemDamage());
										}
										ItemStack stack = mb.getStructureManual()[h][l][w];
										IBlockState state = mb.getBlockstateFromStack(idx, stack);
										IBlockState actualState = world.getBlockState(actualPos);
										boolean stateEqual = actualState.equals(state);
										boolean otherStateEqual = otherState == null ? false : otherState.equals(state);
										
										int[] ids = OreDictionary.getOreIDs(stack);
										for (int id : ids)
										{
											String idS = OreDictionary.getOreName(id);
											if (Utils.isOreBlockAt(world, actualPos, idS))
											{
												stateEqual = true;
											}
										}
										
										
										if (heldStack != null && otherState != null)
										{
											int[] ids2 = OreDictionary.getOreIDs(heldStack);
											for (int id2 : ids2)
											{
												for (int id : ids)
												{
													if (id == id2)
													{
														otherStateEqual = true;
													}
												}
											}
										}
										
										if (!stateEqual && !(mb.getUniqueName().equals("IE:BottlingMachine") && h == 2))
										{
											perfect = false;
											ShaderUtil.alpha(otherStateEqual ? .75F : .5F);
											GlStateManager.translate(xo, h, zo);
	
											GlStateManager.translate(.5, .5, .5);
											
											if (state.getBlock() == IEContent.blockConveyor)
											{
												switch (rotate)
												{
													case 0:
														GlStateManager.rotate(180, 0, 1, 0);
														break;
													case 1:
														GlStateManager.rotate(90, 0, 1, 0);
														break;
													case 3:
														GlStateManager.rotate(270, 0, 1, 0);
														break;
												}
												if ((mb.getUniqueName().equals("IE:AutoWorkbench") && (w != 1 || l != 2))
														|| mb.getUniqueName().equals("IE:BottlingMachine"))
												{
													GlStateManager.rotate(270, 0, 1, 0);
												}
												GlStateManager.scale(16F/20.1F, 16F/20.1F, 16F/20.1F);
												GlStateManager.translate(6.25F/16F, 0, 0);
												GlStateManager.rotate(90, 0, 1, 0);
											}
											else if (state.getBlock() == IEContent.blockMetalDevice0 && BlockTypes_MetalDevice0.values()[state.getBlock().getMetaFromState(state)]==BlockTypes_MetalDevice0.FLUID_PUMP)
											{
												GlStateManager.translate(0, .45F, 0);
												GlStateManager.scale(.9F, .9F, .9F);
											}
											else if (state.getBlock() == Blocks.PISTON)
											{
												GlStateManager.rotate(180, 1, 0, 0);
											}
											
											
											GlStateManager.scale(2.01, 2.01, 2.01);
											//GlStateManager.translate(-.5F, -.5F, -.5F);
									
											ClientUtils.mc().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
											//ImmersiveEngineering.proxy.drawSpecificFluidPipe("001002");
											//BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();																						
											//GlStateManager.translate(-.5F, -.5F, .5F);
											//brd.renderBlockBrightness(state, 1.0F);
											
											ShaderUtil.releaseShader();
	
										}
										
	
	
									}
									GlStateManager.popMatrix();
	
									idx++;
								}
						
						idx = 0;
						for(int h = 0; h < mh; h++)
							for(int l = 0; l < ml; l++)
								for(int w = 0; w < mw; w++)
								{
									BlockPos pos = new BlockPos(l, h, w);
					
									GlStateManager.pushMatrix();
									GlStateManager.disableDepth();
	
									if (mb.getStructureManual()[h][l][w] != null)
									{
										BlockPos blockPos = new BlockPos(0, 0, 0);
										
										int xo = l;
										int zo = w;
										
										switch (rotate)
										{
											case 1:
												zo = l;
												xo = (mw - w - 1);
												break;
											case 2:
												xo = (ml - l - 1);
												zo = (mw - w - 1);
												break;
											case 3:
												zo = (ml - l - 1);
												xo = w;
												break;
										}
										if (rotate % 2 == 1)
										{
											xo = flip ? xo : (mw - xo - 1);
										}
										else
										{
											zo = flip ? zo : (mw - zo - 1);
										}
										
										BlockPos actualPos = hit.add(xo, h, zo);
										
										ItemStack heldStack = main ? secondItem : mainItem;
										IBlockState otherState = null;
										if (heldStack != null && heldStack.getItem() instanceof ItemBlock)
										{
											otherState = ((ItemBlock)heldStack.getItem()).getBlock().getStateFromMeta(heldStack.getItemDamage());
										}
										ItemStack stack = mb.getStructureManual()[h][l][w];
										IBlockState state = mb.getBlockstateFromStack(idx, stack);
										IBlockState actualState = world.getBlockState(actualPos);
										boolean stateEqual = actualState.equals(state);
										boolean otherStateEqual = otherState == null ? false : otherState.equals(state);
										
										int[] ids = OreDictionary.getOreIDs(stack);
										for (int id : ids)
										{
											String idS = OreDictionary.getOreName(id);
											if (Utils.isOreBlockAt(world, actualPos, idS))
											{
												stateEqual = true;
											}
										}
										
										
										if (heldStack != null && otherState != null)
										{
											int[] ids2 = OreDictionary.getOreIDs(heldStack);
											for (int id2 : ids2)
											{
												for (int id : ids)
												{
													if (id == id2)
													{
														otherStateEqual = true;
													}
												}
											}
										}
										
										
										if (!stateEqual)
										{
											boolean isEmpty = world.getBlockState(actualPos).getBlock().isReplaceable(world, actualPos);
											if (!isEmpty || otherStateEqual)
											{
												GlStateManager.pushMatrix();
												GlStateManager.disableTexture2D();
												GlStateManager.enableBlend();
												GlStateManager.disableCull();
												GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
												GlStateManager.shadeModel(GL11.GL_SMOOTH);
												float r = 1;
												float g = !isEmpty ? 0 : 1;
												float b = !isEmpty ? 0 : 1;
												GlStateManager.translate(xo + .5, h + .5, zo + .5);
												GlStateManager.scale(1.01, 1.01, 1.01);
												//buffer.setTranslation(l, h, w);
												GlStateManager.glLineWidth(2f);
												buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
												buffer.pos(-.5F, .5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, .5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, .5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, .5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, .5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(-.5F, .5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(-.5F, .5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(-.5F, .5F, -.5F).color(r,g,b,.375f).endVertex();
												
												buffer.pos(-.5F, .5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(-.5F, -.5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, .5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, -.5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(-.5F, .5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(-.5F, -.5F, .5F).color(r,g,b,.375f).endVertex();	
												buffer.pos(.5F, .5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, -.5F, .5F).color(r,g,b,.375f).endVertex();
												
												buffer.pos(-.5F, -.5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, -.5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, -.5F, -.5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, -.5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(.5F, -.5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(-.5F, -.5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(-.5F, -.5F, .5F).color(r,g,b,.375f).endVertex();
												buffer.pos(-.5F, -.5F, -.5F).color(r,g,b,.375f).endVertex();
												
												tessellator.draw();
												buffer.setTranslation(0, 0, 0);
												GlStateManager.shadeModel(GL11.GL_FLAT);
												GlStateManager.enableCull();
												GlStateManager.disableBlend();
												GlStateManager.enableTexture2D();
												GlStateManager.popMatrix();
											}
											
										}
	
									}
									GlStateManager.popMatrix();
	
									idx++;
								}
	
						if (perfect)
						{
							
							GlStateManager.pushMatrix();
							GlStateManager.disableTexture2D();
							GlStateManager.enableBlend();
							GlStateManager.disableCull();
							GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
							GlStateManager.shadeModel(GL11.GL_SMOOTH);
							float r = 0;
							float g = 1;
							float b = 0;
							GlStateManager.translate(0, 0, 0);
							GlStateManager.scale(xd, mh, zd);
							//buffer.setTranslation(l, h, w);
							GlStateManager.glLineWidth(2f);
							buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
							buffer.pos(0, 1, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 1, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 1, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 1, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 1, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(0, 1, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(0, 1, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(0, 1, 0).color(r,g,b,.375f).endVertex();
							
							buffer.pos(0, 1, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(0, 0, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 1, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 0, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(0, 1, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(0, 0, 1).color(r,g,b,.375f).endVertex();	
							buffer.pos(1, 1, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 0, 1).color(r,g,b,.375f).endVertex();
							
							buffer.pos(0, 0, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 0, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 0, 0).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 0, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(1, 0, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(0, 0, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(0, 0, 1).color(r,g,b,.375f).endVertex();
							buffer.pos(0, 0, 0).color(r,g,b,.375f).endVertex();
							
							tessellator.draw();
							buffer.setTranslation(0, 0, 0);
							GlStateManager.shadeModel(GL11.GL_FLAT);
							GlStateManager.enableCull();
							GlStateManager.disableBlend();
							GlStateManager.enableTexture2D();
							GlStateManager.popMatrix();
						}
	
						RenderHelper.disableStandardItemLighting();
						GlStateManager.disableRescaleNormal();
	
						GlStateManager.enableBlend();
						RenderHelper.disableStandardItemLighting();
					}
				}
			}
		}
		
		GlStateManager.popMatrix();

	}
	
	@SideOnly(Side.CLIENT)
	public void renderChunkBorder(int chunkX, int chunkZ)
	{
		EntityPlayer player = ClientUtils.mc().thePlayer;

		double px = TileEntityRendererDispatcher.staticPlayerX;
		double py = TileEntityRendererDispatcher.staticPlayerY;
		double pz = TileEntityRendererDispatcher.staticPlayerZ;
		int y = Math.min((int)player.posY-2,player.getEntityWorld().getChunkFromBlockCoords(new BlockPos(chunkX, 0, chunkZ)).getLowestHeight());
		float h = (float)Math.max(32, player.posY-y+4);
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		float r = Lib.COLOUR_F_ImmersiveOrange[0];
		float g = Lib.COLOUR_F_ImmersiveOrange[1];
		float b = Lib.COLOUR_F_ImmersiveOrange[2];
		vertexbuffer.setTranslation(chunkX-px, y+2-py, chunkZ-pz);
		GlStateManager.glLineWidth(5f);
		vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		vertexbuffer.pos( 0,0, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,h, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,0, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,h, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,0,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,h,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,0,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,h,16).color(r,g,b,.375f).endVertex();

		vertexbuffer.pos( 0,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,2,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,2,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2,16).color(r,g,b,.375f).endVertex();
		tessellator.draw();
		vertexbuffer.setTranslation(0, 0, 0);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handlePickupItem(RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		IBlockState state = event.getWorld().getBlockState(pos);
		if (!event.getWorld().isRemote && state.getBlock() == IEContent.blockMetalDevice1)
		{
			TileEntity te = event.getWorld().getTileEntity(pos);
			if (te instanceof TileEntitySampleDrill)
			{
				TileEntitySampleDrill drill = (TileEntitySampleDrill) te;
				
				if (drill.dummy != 0)
				{
					te = event.getWorld().getTileEntity(pos.add(0, - drill.dummy, 0));
					if (te instanceof TileEntitySampleDrill)
					{
						drill = (TileEntitySampleDrill) te;
					}
				}
				if (drill.sample != null)
				{
					if(ItemNBTHelper.hasKey(drill.sample, "coords"))
					{
						int[] coords = ItemNBTHelper.getIntArray(drill.sample, "coords");
						World world = DimensionManager.getWorld(coords[0]);
						
						int amnt = PumpjackHandler.getOilAmount(world, coords[1], coords[2]);
						ItemNBTHelper.setInt(drill.sample, "oil", amnt);
					}
				}
			}
		}

	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	@SideOnly(Side.CLIENT)
	public void handleItemTooltip(ItemTooltipEvent event)
	{
		ItemStack stack = event.getItemStack();
		if (stack.getItem() instanceof ItemCoresample)
		{
			if(ItemNBTHelper.hasKey(stack, "oil"))
			{
				int amnt = ItemNBTHelper.getInt(stack, "oil");
				List<String> tooltip = event.getToolTip();
				if (amnt > 0)
				{
					int est = (amnt / 1000) * 1000;
					String test = new DecimalFormat("#,###.##").format(est);
					tooltip.add(2, I18n.format("chat.immersivepetroleum.info.coresample.oil", test));
				}
				else
				{
					tooltip.add(2, I18n.format("chat.immersivepetroleum.info.coresample.noOil"));
				}
			}
		}
	}

}
