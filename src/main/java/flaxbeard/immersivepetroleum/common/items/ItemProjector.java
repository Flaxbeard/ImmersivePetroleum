package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityIESlab;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavatorDemo;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.realmsclient.gui.ChatFormatting;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.event.SchematicPlaceBlockEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicPlaceBlockPostEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicRenderBlockEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicTestEvent;
import flaxbeard.immersivepetroleum.client.ShaderUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.BlockTypes_Dummy;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.RotateSchematicPacket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ItemProjector extends ItemIPBase
{
	public ItemProjector(String name)
	{
		super(name, 1, new String[0]);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		if (ItemNBTHelper.hasKey(stack, "multiblock"))
		{
			String multiblock = ItemNBTHelper.getString(stack, "multiblock");
			IMultiblock mb = getMultiblock(multiblock);
			if (mb != null)
			{
				if ("IE:ExcavatorDemo".equals(multiblock))
				{
					multiblock = "IE:Excavator";
				}
				tooltip.add(I18n.format("chat.immersivepetroleum.info.schematic.build0"));
				tooltip.add(I18n.format("chat.immersivepetroleum.info.schematic.build1", I18n.format("desc.immersiveengineering.info.multiblock." + multiblock)));

				int h = mb.getStructureManual().length;
				int l = mb.getStructureManual()[0].length;
				int w = mb.getStructureManual()[0][0].length;

				tooltip.add(ChatFormatting.DARK_GRAY + (l + " x " + h + " x " + w));

				if (ItemNBTHelper.hasKey(stack, "pos"))
				{
					NBTTagCompound pos = ItemNBTHelper.getTagCompound(stack, "pos");
					int x = pos.getInteger("x");
					int y = pos.getInteger("y");
					int z = pos.getInteger("z");
					tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("chat.immersivepetroleum.info.schematic.center", x, y, z));
				}
				else
				{
					tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("chat.immersivepetroleum.info.schematic.controls",
							Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getDisplayName(),
							Minecraft.getMinecraft().gameSettings.keyBindSneak.getDisplayName(),
							Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getDisplayName()));
				}
				return;

			}
		}
		tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("chat.immersivepetroleum.info.schematic.noMultiblock"));
	}

	public static IMultiblock getMultiblock(String identifier)
	{
		List<IMultiblock> multiblocks = MultiblockHandler.getMultiblocks();
		for (IMultiblock multiblock : multiblocks)
		{
			if (multiblock.getUniqueName().equals(identifier))
			{
				return multiblock;
			}
		}
		if ("IE:ExcavatorDemo".equals(identifier))
		{
			return MultiblockExcavatorDemo.instance;
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			NBTTagCompound comp = stack.getTagCompound();
			if (ItemNBTHelper.hasKey(stack, "multiblock"))
			{
				String multiblock = ItemNBTHelper.getString(stack, "multiblock");
				IMultiblock mb = getMultiblock(multiblock);
				if (mb != null)
				{
					if ("IE:ExcavatorDemo".equals(multiblock))
					{
						multiblock = "IE:Excavator";
					}
					return I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".name", I18n.format("desc.immersiveengineering.info.multiblock." + multiblock)).trim();
				}
			}
		}
		return ("" + I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".name", "")).trim();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		if (this.isInCreativeTab(tab))
		{
			list.add(new ItemStack(this, 1, 0));
			List<IMultiblock> multiblocks = MultiblockHandler.getMultiblocks();
			for (IMultiblock multiblock : multiblocks)
			{
				String str = multiblock.getUniqueName();
				if (str.equals("IE:BucketWheel") || str.equals("IE:Excavator")) continue;
				ItemStack stack = new ItemStack(this, 1, 0);
				ItemNBTHelper.setString(stack, "multiblock", multiblock.getUniqueName());
				list.add(stack);
			}
			ItemStack stack = new ItemStack(this, 1, 0);
			ItemNBTHelper.setString(stack, "multiblock", MultiblockExcavatorDemo.instance.getUniqueName());
			setFlipped(stack, true);
			list.add(stack);
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = playerIn.getHeldItem(hand);
		if (ItemNBTHelper.hasKey(stack, "pos") && playerIn.isSneaking())
		{
			ItemNBTHelper.remove(stack, "pos");
			return EnumActionResult.SUCCESS;
		}

		IMultiblock mb = ItemProjector.getMultiblock(ItemNBTHelper.getString(stack, "multiblock"));
		if (!ItemNBTHelper.hasKey(stack, "pos") && mb != null)
		{
			NBTTagCompound posTag = new NBTTagCompound();

			IBlockState state = world.getBlockState(pos);

			BlockPos hit = pos;
			if (!state.getBlock().isReplaceable(world, pos) && facing == EnumFacing.UP)
			{
				hit = hit.add(0, 1, 0);
			}


			int mh = mb.getStructureManual().length;
			int ml = mb.getStructureManual()[0].length;
			int mw = mb.getStructureManual()[0][0].length;

			int rotate = getRotation(stack);

			int xd = (rotate % 2 == 0) ? ml : mw;
			int zd = (rotate % 2 == 0) ? mw : ml;

			Vec3d vec = playerIn.getLookVec();
			EnumFacing look = (Math.abs(vec.z) > Math.abs(vec.x)) ? (vec.z > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH) : (vec.x > 0 ? EnumFacing.EAST : EnumFacing.WEST);
			if (look == EnumFacing.NORTH || look == EnumFacing.SOUTH)
			{
				hit = hit.add(-xd / 2, 0, 0);
			}
			else if (look == EnumFacing.EAST || look == EnumFacing.WEST)
			{
				hit = hit.add(0, 0, -zd / 2);
			}

			if (look == EnumFacing.NORTH)
			{
				hit = hit.add(0, 0, -zd + 1);
			}
			else if (look == EnumFacing.WEST)
			{
				hit = hit.add(-xd + 1, 0, 0);
			}

			if (playerIn.isSneaking() && playerIn.isCreative())
			{
				if (mb.getUniqueName().equals("IE:ExcavatorDemo"))
				{
					hit = hit.add(0, -2, 0);
				}

				boolean flip = getFlipped(stack);

				int idx = 0;
				for (int h = 0; h < mh; h++)
				{
					for (int l = 0; l < ml; l++)
					{
						for (int w = 0; w < mw; w++)
						{
							if (mb.getStructureManual()[h][l][w] != null && !mb.getStructureManual()[h][l][w].isEmpty())
							{
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


								ItemStack toPlace = mb.getStructureManual()[h][l][w];
								IBlockState stt = mb.getBlockstateFromStack(idx, toPlace);
								SchematicPlaceBlockEvent placeEvent = new SchematicPlaceBlockEvent(mb, idx, stt, world, rotate, l, h, w);
								if (!MinecraftForge.EVENT_BUS.post(placeEvent))
								{
									world.setBlockState(actualPos, placeEvent.getBlockState());

									SchematicPlaceBlockPostEvent postEvent = new SchematicPlaceBlockPostEvent(mb, idx, stt, actualPos, world, rotate, l, h, w);
									MinecraftForge.EVENT_BUS.post(postEvent);
								}

							}
						}
					}
				}

				return EnumActionResult.SUCCESS;
			}

			posTag.setInteger("x", hit.getX());
			posTag.setInteger("y", hit.getY());
			posTag.setInteger("z", hit.getZ());
			ItemNBTHelper.setTagCompound(stack, "pos", posTag);
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	public static int getRotation(ItemStack stack)
	{
		if (ItemNBTHelper.hasKey(stack, "rotate"))
		{
			return ItemNBTHelper.getInt(stack, "rotate");
		}
		return 0;
	}

	public static boolean getFlipped(ItemStack stack)
	{
		if (ItemNBTHelper.hasKey(stack, "flip"))
		{
			return ItemNBTHelper.getBoolean(stack, "flip");
		}
		return false;
	}

	public static void rotateClient(ItemStack stack)
	{
		int newRotate = (getRotation(stack) + 1) % 4;
		boolean flip = getFlipped(stack);
		setRotate(stack, newRotate);
		IPPacketHandler.INSTANCE.sendToServer(new RotateSchematicPacket(newRotate, flip));
	}

	public static void flipClient(ItemStack stack)
	{
		int newRotate = getRotation(stack);
		boolean flip = !getFlipped(stack);
		setFlipped(stack, flip);
		IPPacketHandler.INSTANCE.sendToServer(new RotateSchematicPacket(newRotate, flip));
	}

	public static void setRotate(ItemStack stack, int rotate)
	{
		ItemNBTHelper.setInt(stack, "rotate", rotate);
	}

	public static void setFlipped(ItemStack stack, boolean flip)
	{
		ItemNBTHelper.setBoolean(stack, "flip", flip);
	}

	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand)
	{
		if (ItemNBTHelper.hasKey(stack, "pos") && playerIn.isSneaking())
		{
			ItemNBTHelper.remove(stack, "pos");
			return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult(EnumActionResult.PASS, stack);
	}


	boolean lastDown = false;
	private int tempCode = 99999;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void handleKeypress(ClientTickEvent event)
	{
		Minecraft mc = ClientUtils.mc();
		if (event.phase == Phase.START && mc.player != null)
		{
			//
			ItemStack mainItem = mc.player.getHeldItemMainhand();
			ItemStack secondItem = mc.player.getHeldItemOffhand();

			boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(mainItem, "multiblock");
			boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");
			ItemStack target = main ? mainItem : secondItem;
			World world = mc.world;

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
								if (mc.player.isSneaking())
								{
									ItemProjector.flipClient(target);
								}
								else
								{
									ItemProjector.rotateClient(target);
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
								if (mc.player.isSneaking())
								{
									ItemProjector.flipClient(target);
								}
								else
								{
									ItemProjector.rotateClient(target);
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

		Minecraft mc = ClientUtils.mc();

		GlStateManager.pushMatrix();

		if (mc.player != null)
		{
			ItemStack mainItem = mc.player.getHeldItemMainhand();
			ItemStack secondItem = mc.player.getHeldItemOffhand();

			boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(mainItem, "multiblock");
			boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");

			for (int i = 0; i < 11; i++)
			{
				GlStateManager.pushMatrix();
				ItemStack stack = (i == 10 ? secondItem : mc.player.inventory.getStackInSlot(i));
				if (!stack.isEmpty() && stack.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(stack, "multiblock"))
				{
					renderSchematic(stack, mc.player, mc.player.world, event.getPartialTicks(), i == mc.player.inventory.currentItem || (i == 10 && off));
				}
				GlStateManager.popMatrix();
			}
		}

		if (mc.player != null)
		{
			ItemStack mainItem = mc.player.getHeldItemMainhand();
			ItemStack secondItem = mc.player.getHeldItemOffhand();

			boolean main = mainItem != null && mainItem.getItem() == Item.getItemFromBlock(IPContent.blockMetalDevice);
			boolean off = secondItem != null && secondItem.getItem() == Item.getItemFromBlock(IPContent.blockMetalDevice);

			if (main || off)
			{
				BlockPos base = mc.player.getPosition();
				for (int x = -16; x <= 16; x++)
				{
					for (int z = -16; z <= 16; z++)
					{
						for (int y = -16; y <= 16; y++)
						{
							BlockPos pos = base.add(x, y, z);
							TileEntity te = mc.player.world.getTileEntity(pos);

							if (te != null)
							{
								ILubricationHandler handler = LubricatedHandler.getHandlerForTile(te);
								if (handler != null)
								{
									Tuple<BlockPos, EnumFacing> target = handler.getGhostBlockPosition(mc.player.world, te);
									if (target != null)
									{
										BlockPos targetPos = target.getFirst();
										EnumFacing targetFacing = target.getSecond();
										if (mc.player.world.getBlockState(targetPos).getBlock().isReplaceable(mc.player.world, targetPos)
												&& mc.player.world.getBlockState(targetPos.up()).getBlock().isReplaceable(mc.player.world, targetPos.up()))
										{
											GlStateManager.pushMatrix();
											float alpha = .5f;
											ShaderUtil.alpha_static(alpha, mc.player.ticksExisted);
											double px = TileEntityRendererDispatcher.staticPlayerX;
											double py = TileEntityRendererDispatcher.staticPlayerY;
											double pz = TileEntityRendererDispatcher.staticPlayerZ;


											GlStateManager.translate(targetPos.getX() - px, targetPos.getY() - py, targetPos.getZ() - pz);
											GlStateManager.translate(0.5, -.13, .5);

											switch (targetFacing)
											{
												case SOUTH:
													GlStateManager.rotate(270, 0, 1, 0);
													break;
												case NORTH:
													GlStateManager.rotate(90, 0, 1, 0);
													break;
												case WEST:
													GlStateManager.rotate(180, 0, 1, 0);
													break;
												case EAST:
													break;
												default:
											}
											GlStateManager.translate(0.02, 0, .019);

											GlStateManager.scale(1 / 0.65F, 1 / 0.65F, 1 / 0.65F);
											GlStateManager.scale(2, 2, 2);


											ItemStack toRender = new ItemStack(Item.getItemFromBlock(IPContent.blockMetalDevice));

											ClientUtils.mc().getRenderItem().renderItem(toRender, ItemCameraTransforms.TransformType.FIXED);

											ShaderUtil.releaseShader();
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

		GlStateManager.popMatrix();

	}

	public void renderSchematic(ItemStack target, EntityPlayer player, World world, float partialTicks, boolean shouldRenderMoving)
	{
		Minecraft mc = ClientUtils.mc();

		IMultiblock mb = ItemProjector.getMultiblock(ItemNBTHelper.getString(target, "multiblock"));


		if (mb != null)
		{
			ItemStack heldStack = player.getHeldItemMainhand();

			int mh = mb.getStructureManual().length;
			int ml = mb.getStructureManual()[0].length;
			int mw = mb.getStructureManual()[0][0].length;

			int rotate = ItemProjector.getRotation(target);
			boolean flip = ItemProjector.getFlipped(target);

			int xd = (rotate % 2 == 0) ? ml : mw;
			int zd = (rotate % 2 == 0) ? mw : ml;

			BlockPos hit = null;

			boolean isPlaced = false;

			if (ItemNBTHelper.hasKey(target, "pos"))
			{
				NBTTagCompound pos = ItemNBTHelper.getTagCompound(target, "pos");
				int x = pos.getInteger("x");
				int y = pos.getInteger("y");
				int z = pos.getInteger("z");
				hit = new BlockPos(x, y, z);
				isPlaced = true;
			}
			else if (shouldRenderMoving && ClientUtils.mc().objectMouseOver != null && ClientUtils.mc().objectMouseOver.typeOfHit == Type.BLOCK)
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


				Vec3d vec = mc.player.getLookVec();
				EnumFacing facing = (Math.abs(vec.z) > Math.abs(vec.x)) ? (vec.z > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH) : (vec.x > 0 ? EnumFacing.EAST : EnumFacing.WEST);


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


				GlStateManager.translate(hit.getX() - px, hit.getY() - py, hit.getZ() - pz);

				GlStateManager.disableLighting();

				if (Minecraft.isAmbientOcclusionEnabled())
					GlStateManager.shadeModel(GL11.GL_SMOOTH);
				else
					GlStateManager.shadeModel(GL11.GL_FLAT);

				ClientUtils.mc().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();

				float flicker = (world.rand.nextInt(10) == 0) ? 0.75F : (world.rand.nextInt(20) == 0 ? 0.5F : 1F);

				boolean perfect = true;

				int idx = 0;
				for (int h = 0; h < mh; h++)
				{
					boolean slicePerfect = true;
					for (int l = 0; l < ml; l++)
					{
						for (int w = 0; w < mw; w++)
						{
							BlockPos pos = new BlockPos(l, h, w);

							GlStateManager.pushMatrix();

							if (mb.getStructureManual()[h][l][w] != null && !mb.getStructureManual()[h][l][w].isEmpty())
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

								IBlockState otherState = null;
								if (!heldStack.isEmpty() && heldStack.getItem() instanceof ItemBlock)
								{
									otherState = ((ItemBlock) heldStack.getItem()).getBlock().getStateFromMeta(heldStack.getItemDamage());
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

								SchematicTestEvent testEvent = new SchematicTestEvent(stateEqual, mb, idx, stack, world, actualPos, rotate, l, h, w);
								MinecraftForge.EVENT_BUS.post(testEvent);
								stateEqual = testEvent.isEqual();


								if (!heldStack.isEmpty() && otherState != null)
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

								boolean isEmpty = world.getBlockState(actualPos).getBlock().isReplaceable(world, actualPos);
								perfect &= stateEqual;
								if (!stateEqual && isEmpty)
								{
									slicePerfect = false;
									float alpha = otherStateEqual ? .75F : .5F;
									ShaderUtil.alpha_static(flicker * alpha, mc.player.ticksExisted + partialTicks);
									GlStateManager.translate(xo, h, zo);

									GlStateManager.translate(.5, .5, .5);

									GlStateManager.scale(2.01, 2.01, 2.01);

									SchematicRenderBlockEvent renderEvent = new SchematicRenderBlockEvent(mb, idx, stack, world, rotate, l, h, w);

									if (!MinecraftForge.EVENT_BUS.post(renderEvent))
									{
										ItemStack toRender = renderEvent.getItemStack();

										ClientUtils.mc().getRenderItem().renderItem(toRender, ItemCameraTransforms.TransformType.FIXED);
									}

									ShaderUtil.releaseShader();
								}

							}
							GlStateManager.popMatrix();

							idx++;
						}
					}
					if (!slicePerfect && isPlaced) break;
				}

				idx = 0;
				GlStateManager.disableDepth();

				for (int h = 0; h < mh; h++)
				{
					boolean slicePerfect = true;
					for (int l = 0; l < ml; l++)
					{
						for (int w = 0; w < mw; w++)
						{
							BlockPos pos = new BlockPos(l, h, w);

							GlStateManager.pushMatrix();

							if (mb.getStructureManual()[h][l][w] != null && !mb.getStructureManual()[h][l][w].isEmpty())
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

								IBlockState otherState = null;
								if (!heldStack.isEmpty() && heldStack.getItem() instanceof ItemBlock)
								{
									otherState = ((ItemBlock) heldStack.getItem()).getBlock().getStateFromMeta(heldStack.getItemDamage());
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

								SchematicTestEvent testEvent = new SchematicTestEvent(stateEqual, mb, idx, stack, world, actualPos, rotate, l, h, w);
								MinecraftForge.EVENT_BUS.post(testEvent);
								stateEqual = testEvent.isEqual();

								if (!heldStack.isEmpty() && otherState != null)
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
									if (isEmpty) slicePerfect = false;
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
										float alpha = .375F * flicker;
										GlStateManager.translate(xo + .5, h + .5, zo + .5);
										GlStateManager.scale(1.01, 1.01, 1.01);
										//buffer.setTranslation(l, h, w);
										GlStateManager.glLineWidth(2f);
										buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
										buffer.pos(-.5F, .5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, .5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, .5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, .5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, .5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(-.5F, .5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(-.5F, .5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(-.5F, .5F, -.5F).color(r, g, b, alpha).endVertex();

										buffer.pos(-.5F, .5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(-.5F, -.5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, .5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, -.5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(-.5F, .5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(-.5F, -.5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, .5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, -.5F, .5F).color(r, g, b, alpha).endVertex();

										buffer.pos(-.5F, -.5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, -.5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, -.5F, -.5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, -.5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(.5F, -.5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(-.5F, -.5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(-.5F, -.5F, .5F).color(r, g, b, alpha).endVertex();
										buffer.pos(-.5F, -.5F, -.5F).color(r, g, b, alpha).endVertex();

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
					}
					if (!slicePerfect && isPlaced) break;
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
					buffer.pos(0, 1, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 1, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 1, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 1, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 1, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(0, 1, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(0, 1, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(0, 1, 0).color(r, g, b, .375f).endVertex();

					buffer.pos(0, 1, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(0, 0, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 1, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 0, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(0, 1, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(0, 0, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 1, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 0, 1).color(r, g, b, .375f).endVertex();

					buffer.pos(0, 0, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 0, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 0, 0).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 0, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(1, 0, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(0, 0, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(0, 0, 1).color(r, g, b, .375f).endVertex();
					buffer.pos(0, 0, 0).color(r, g, b, .375f).endVertex();

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
		GlStateManager.enableDepth();
	}

	@SubscribeEvent
	public void handlePumpPlace(SchematicPlaceBlockPostEvent event)
	{
		IMultiblock mb = event.getMultiblock();

		IBlockState state = event.getBlockState();
		if (event.getH() < mb.getStructureManual().length - 1)
		{
			ItemStack stack = mb.getStructureManual()[event.getH() + 1][event.getL()][event.getW()];
			if (stack != null && !stack.isEmpty())
			{
				if (state.getBlock() == IEContent.blockMetalDevice0 && state.getBlock().getMetaFromState(state) == BlockTypes_MetalDevice0.FLUID_PUMP.getMeta() &&
						!(stack.getItemDamage() != BlockTypes_MetalDevice0.FLUID_PUMP.getMeta() || stack.getItem() != Item.getItemFromBlock(IEContent.blockMetalDevice0)))
				{
					TileEntityFluidPump pump = (TileEntityFluidPump) event.getWorld().getTileEntity(event.getPos());
					pump.placeDummies(event.getPos(), state, EnumFacing.UP, 0, 0, 0);
					event.getWorld().setBlockState(event.getPos(), event.getBlockState());
				}
			}
		}
	}

	@SubscribeEvent
	public void handleConveyorPlace(SchematicPlaceBlockPostEvent event)
	{
		IMultiblock mb = event.getMultiblock();

		IBlockState state = event.getBlockState();

		if (mb.getUniqueName().equals("IE:AutoWorkbench") || mb.getUniqueName().equals("IE:BottlingMachine") || mb.getUniqueName().equals("IE:Assembler")
				|| mb.getUniqueName().equals("IE:BottlingMachine") || mb.getUniqueName().equals("IE:MetalPress"))
		{
			if (state.getBlock() == IEContent.blockConveyor)
			{
				TileEntity te = event.getWorld().getTileEntity(event.getPos());
				if (te instanceof TileEntityConveyorBelt)
				{
					TileEntityConveyorBelt conveyor = ((TileEntityConveyorBelt) te);
					ResourceLocation rl = new ResourceLocation(ImmersiveEngineering.MODID, "conveyor");
					IConveyorBelt subType = ConveyorHandler.getConveyor(rl, conveyor);
					conveyor.setConveyorSubtype(subType);

					EnumFacing facing = ((TileEntityConveyorBelt) te).facing;

					if ((mb.getUniqueName().equals("IE:AutoWorkbench") && (event.getW() != 1 || event.getL() != 2))
							|| mb.getUniqueName().equals("IE:BottlingMachine"))
					{
						conveyor.setFacing(event.getRotate().rotateY());
					}
					else if (mb.getUniqueName().equals("IE:AutoWorkbench") && (event.getRotate() == EnumFacing.WEST || event.getRotate() == EnumFacing.EAST))
					{
						conveyor.setFacing(event.getRotate().getOpposite());
					}
					else
					{
						conveyor.setFacing(event.getRotate());
					}

					event.getWorld().setBlockState(event.getPos(), event.getBlockState());
				}
			}
		}
	}

	@SubscribeEvent
	public void handleConveyorTest(SchematicTestEvent event)
	{
		IMultiblock mb = event.getMultiblock();

		IBlockState state = event.getMultiblock().getBlockstateFromStack(event.getIndex(), event.getItemStack());

		if (mb.getUniqueName().equals("IE:AutoWorkbench") || mb.getUniqueName().equals("IE:BottlingMachine") || mb.getUniqueName().equals("IE:Assembler")
				|| mb.getUniqueName().equals("IE:BottlingMachine") || mb.getUniqueName().equals("IE:MetalPress"))
		{
			if (state.getBlock() == IEContent.blockConveyor)
			{
				TileEntity te = event.getWorld().getTileEntity(event.getPos());
				if (te instanceof TileEntityConveyorBelt)
				{
					IConveyorBelt subtype = ((TileEntityConveyorBelt) te).getConveyorSubtype();
					if (subtype != null && subtype.getConveyorDirection() != ConveyorDirection.HORIZONTAL)
					{
						event.setIsEqual(false);
						return;
					}

					EnumFacing facing = ((TileEntityConveyorBelt) te).facing;

					if (event.isEqual() && (mb.getUniqueName().equals("IE:AutoWorkbench") && (event.getW() != 1 || event.getL() != 2))
							|| mb.getUniqueName().equals("IE:BottlingMachine"))
					{
						if (facing != event.getRotate().rotateY())
						{
							event.setIsEqual(false);
							return;
						}
					}
					else if (facing != event.getRotate())
					{
						event.setIsEqual(false);
						return;
					}
				}
				else
				{
					event.setIsEqual(false);
					return;
				}
			}
		}

		if (mb.getUniqueName().equals("IP:DistillationTower"))
		{
			if (state.getBlock() == IEContent.blockMetalDecorationSlabs1)
			{
				if (event.getWorld().getBlockState(event.getPos()).getBlock() == IEContent.blockMetalDecorationSlabs1)
				{
					event.setIsEqual(true);
				}
			}
		}
	}

	public boolean doesIntersect(EntityPlayer player, ItemStack target, BlockPos check)
	{
		IMultiblock mb = ItemProjector.getMultiblock(ItemNBTHelper.getString(target, "multiblock"));


		if (mb != null)
		{
			int mh = mb.getStructureManual().length;
			int ml = mb.getStructureManual()[0].length;
			int mw = mb.getStructureManual()[0][0].length;

			int rotate = ItemProjector.getRotation(target);
			boolean flip = ItemProjector.getFlipped(target);

			int xd = (rotate % 2 == 0) ? ml : mw;
			int zd = (rotate % 2 == 0) ? mw : ml;

			boolean isPlaced = false;

			if (ItemNBTHelper.hasKey(target, "pos"))
			{
				NBTTagCompound pos = ItemNBTHelper.getTagCompound(target, "pos");
				int x = pos.getInteger("x");
				int y = pos.getInteger("y");
				int z = pos.getInteger("z");

				BlockPos hit = new BlockPos(x, y, z);
				BlockPos end = hit.add(xd, mh, zd);

				return (check.getX() >= hit.getX() && check.getX() <= end.getX() &&
						check.getY() >= hit.getY() && check.getY() <= end.getY() &&
						check.getZ() >= hit.getZ() && check.getZ() <= end.getZ());
			}
		}
		return false;
	}

	@SubscribeEvent
	public void handleConveyorsAndPipes(SchematicRenderBlockEvent event)
	{
		if ((event.getMultiblock().getUniqueName().equals("IE:BottlingMachine") && event.getH() == 2))
		{
			event.setCanceled(true);
		}

		IBlockState state = event.getMultiblock().getBlockstateFromStack(event.getIndex(), event.getItemStack());
		if (state.getBlock() == IEContent.blockMetalDevice1 && BlockTypes_MetalDevice1.values()[state.getBlock().getMetaFromState(state)] == BlockTypes_MetalDevice1.FLUID_PIPE)
		{
			event.setItemStack(new ItemStack(IPContent.blockDummy, 1, BlockTypes_Dummy.PIPE.getMeta()));
		}
		else if (state.getBlock() == IEContent.blockConveyor)
		{
			event.setItemStack(new ItemStack(IPContent.blockDummy, 1, BlockTypes_Dummy.CONVEYOR.getMeta()));
		}

		EnumFacing rotate = event.getRotate();
		IMultiblock mb = event.getMultiblock();

		if (state.getBlock() == IEContent.blockConveyor)
		{
			switch (rotate)
			{
				case WEST:
					GlStateManager.rotate(180, 0, 1, 0);
					break;
				case NORTH:
					GlStateManager.rotate(90, 0, 1, 0);
					break;
				case SOUTH:
					GlStateManager.rotate(270, 0, 1, 0);
					break;
				default:
					break;
			}
			if ((mb.getUniqueName().equals("IE:AutoWorkbench") && (event.getW() != 1 || event.getL() != 2))
					|| mb.getUniqueName().equals("IE:BottlingMachine"))
			{
				GlStateManager.rotate(270, 0, 1, 0);
			}
			else if (mb.getUniqueName().equals("IE:AutoWorkbench") && (rotate == EnumFacing.WEST || rotate == EnumFacing.EAST))
			{
				GlStateManager.rotate(180, 0, 1, 0);
			}
			GlStateManager.rotate(90, 0, 1, 0);
		}
		else if (state.getBlock() == IEContent.blockMetalDevice0 && BlockTypes_MetalDevice0.values()[state.getBlock().getMetaFromState(state)] == BlockTypes_MetalDevice0.FLUID_PUMP)
		{
			GlStateManager.translate(0, .225F, 0);
			GlStateManager.scale(.9F, .9F, .9F);
		}
		else if (state.getBlock() == Blocks.PISTON)
		{
			GlStateManager.rotate(180, 1, 0, 0);
		}

		if (mb.getUniqueName().equals("IP:DistillationTower"))
		{
			if (state.getBlock() == IEContent.blockMetalDecorationSlabs1)
			{
				GlStateManager.translate(0, .25F, 0);

			}
		}
	}

	@SubscribeEvent
	public void handleSlabPlace(SchematicPlaceBlockPostEvent event)
	{
		IMultiblock mb = event.getMultiblock();

		IBlockState state = event.getBlockState();
		if (mb.getUniqueName().equals("IP:DistillationTower"))
		{
			TileEntity te = event.getWorld().getTileEntity(event.getPos());
			if (te instanceof TileEntityIESlab)
			{
				((TileEntityIESlab) te).slabType = 1;
			}
		}
	}


}
