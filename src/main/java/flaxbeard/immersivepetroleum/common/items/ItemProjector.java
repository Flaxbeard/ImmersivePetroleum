package flaxbeard.immersivepetroleum.common.items;

import java.lang.reflect.Method;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.gui.ChatFormatting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.event.SchematicPlaceBlockPostEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicRenderBlockEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicTestEvent;
import flaxbeard.immersivepetroleum.client.ShaderUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.RotateSchematicPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemProjector extends IPItemBase{
	public ItemProjector(String name){
		super(name, new Item.Properties().maxStackSize(1));
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	/** Like {@link ItemProjector#getMultiblock(ResourceLocation)} but using the ItemStack directly */
	protected static IMultiblock getMultiblock(ItemStack stack){
		return getMultiblock(getMultiblockIdentifierFrom(stack));
	}
	
	/**
	 * Get's a multiblock using {@link MultiblockHandler#getByUniqueName(ResourceLocation)}
	 * 
	 * @param identifier The ResourceLocation of the Multiblock.
	 * @return The multiblock, or Null if it doesnt exist/cannot be found.
	 */
	protected static IMultiblock getMultiblock(ResourceLocation identifier){
		if(identifier == null)
			return null;
		
		return MultiblockHandler.getByUniqueName(identifier);
	}
	
	/**
	 * Attempts to retrieve a multiblocks name from a stack.
	 * 
	 * @param stack to trying retrieve the name from
	 * @return the multiblock's name, or Null if it doesnt exist/cannot be found.
	 */
	protected static ResourceLocation getMultiblockIdentifierFrom(ItemStack stack){
		if(ItemNBTHelper.hasKey(stack, "multiblock")){
			String tmp=ItemNBTHelper.getString(stack, "multiblock");
			return new ResourceLocation(tmp);
		}
		
		return null;
	}
	
	/**
	 * Stores a Multiblocks name into an ItemStack for later use.
	 * 
	 * @param stack to store the name to
	 * @param multiblock to store to the stack
	 * @return the stack for chaining
	 */
	protected static ItemStack putMultiblockIdentifier(ItemStack stack, IMultiblock multiblock){
		ItemNBTHelper.putString(stack, "multiblock", 
				multiblock
				.getUniqueName()
				.toString());
		return stack; // For convenience
	}
	
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		if(ItemNBTHelper.hasKey(stack, "multiblock")){
			IMultiblock mb = getMultiblock(stack);
			if(mb != null){
				if(mb.getUniqueName().getPath().contains("excavator_demo")){
					//multiblockLocation = "excavator";
				}
				
				String name=getActualMBName(mb);
				String build0=I18n.format("chat.immersivepetroleum.info.schematic.build0");
				String build1=I18n.format("chat.immersivepetroleum.info.schematic.build1",
							  I18n.format("desc.immersiveengineering.info.multiblock.IE:" + name));
				
				tooltip.add(new StringTextComponent(build0));
				tooltip.add(new StringTextComponent(build1));
				
				Vec3i size = mb.getSize();
				tooltip.add(new StringTextComponent(size.getX() + " x " + size.getY() + " x " + size.getZ()).applyTextStyle(TextFormatting.DARK_GRAY));
				
				if(ItemNBTHelper.hasKey(stack, "pos")){
					CompoundNBT pos = ItemNBTHelper.getTagCompound(stack, "pos");
					int x = pos.getInt("x");
					int y = pos.getInt("y");
					int z = pos.getInt("z");
					tooltip.add(new TranslationTextComponent("chat.immersivepetroleum.info.schematic.center", x, y, z).applyTextStyle(TextFormatting.DARK_GRAY));
				}else{
					tooltip.add(new TranslationTextComponent("chat.immersivepetroleum.info.schematic.controls1").applyTextStyle(TextFormatting.DARK_GRAY));
					tooltip.add(new TranslationTextComponent("chat.immersivepetroleum.info.schematic.controls2").applyTextStyle(TextFormatting.DARK_GRAY));
				}
				return;
			}
		}
		tooltip.add(new StringTextComponent(ChatFormatting.DARK_GRAY + I18n.format("chat.immersivepetroleum.info.schematic.noMultiblock")));
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack){
		String selfKey=getTranslationKey(stack);
		
		if(stack.hasTag()){
			if(ItemNBTHelper.hasKey(stack, "multiblock")){
				IMultiblock mb = getMultiblock(stack);
				if(mb != null){
					String name=getActualMBName(mb);
					
					return new TranslationTextComponent(selfKey+".specific", I18n.format("desc.immersiveengineering.info.multiblock.IE:" + name));
				}
			}
		}
		return new TranslationTextComponent(selfKey);
	}
	
	private String getActualMBName(IMultiblock multiblock){
		String className=multiblock.getClass().getSimpleName();
		className=className.substring(0, className.indexOf("Multiblock"));
		
		switch(className){
			case "LightningRod": return "Lightningrod";
			case "ImprovedBlastfurnace": return "BlastFurnaceAdvanced";
			default: return className;
		}
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items){
		if (this.isInGroup(group)){
			items.add(new ItemStack(this, 1));
			
			List<IMultiblock> multiblocks = MultiblockHandler.getMultiblocks();
			for(IMultiblock multiblock:multiblocks){
				ResourceLocation str = multiblock.getUniqueName();
				if(str.getPath().contains("union") || str.getPath().contains("feedthrough"))
					continue;
				
				items.add(putMultiblockIdentifier(new ItemStack(this, 1), multiblock));
			}
		}
	}
	
	private static Method METHOD_GETTEMPLATE;
	/** Get's the template using reflection of {@link TemplateMultiblock#getTemplate()} */
	private Template getMultiblockTemplate(IMultiblock multiblock){
		if(multiblock instanceof TemplateMultiblock){
			try{
				if(METHOD_GETTEMPLATE==null){
					METHOD_GETTEMPLATE=TemplateMultiblock.class.getDeclaredMethod("getTemplate");
					METHOD_GETTEMPLATE.setAccessible(true);
				}
				
				return (Template) METHOD_GETTEMPLATE.invoke(multiblock);
			}catch(Exception e){
				e.printStackTrace();
				METHOD_GETTEMPLATE=null; // Just incase, this might bite me in the ass later..
			}
		}
		return null;
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		World world=context.getWorld();
		PlayerEntity playerIn=context.getPlayer();
		Hand hand=context.getHand();
		BlockPos pos=context.getPos();
		Direction facing=context.getFace();
		ImmersivePetroleum.log.info("{} {} {}", pos, hand, facing);
		
		ItemStack stack = playerIn.getHeldItem(hand);
		if(ItemNBTHelper.hasKey(stack, "pos") && playerIn.isSneaking()){
			ItemNBTHelper.remove(stack, "pos");
			return ActionResultType.SUCCESS;
		}
		
		ResourceLocation multiblockId=getMultiblockIdentifierFrom(stack);
		IMultiblock multiblock = ItemProjector.getMultiblock(multiblockId);
		
		if(!ItemNBTHelper.hasKey(stack, "pos") && multiblock != null){
			BlockState state = world.getBlockState(pos);
			
			BlockPos hit = pos;
			if(!state.getMaterial().isReplaceable() && facing == Direction.UP){
				hit = hit.add(0, 1, 0);
			}
			
			Vec3i size=multiblock.getSize();
			int mh = size.getY();
			int ml = size.getX();
			int mw = size.getZ();
			
			int rotate = getRotation(stack);
			
			int xd = (rotate % 2 == 0) ? ml : mw;
			int zd = (rotate % 2 == 0) ? mw : ml;
			
			Vec3d vec = playerIn.getLookVec();
			Direction look = (Math.abs(vec.z) > Math.abs(vec.x)) ? (vec.z > 0 ? Direction.SOUTH : Direction.NORTH) : (vec.x > 0 ? Direction.EAST : Direction.WEST);
			if(look == Direction.NORTH || look == Direction.SOUTH){
				hit = hit.add(-xd / 2, 0, 0);
			}else if(look == Direction.EAST || look == Direction.WEST){
				hit = hit.add(0, 0, -zd / 2);
			}
			
			if(look == Direction.NORTH){
				hit = hit.add(0, 0, -zd + 1);
			}else if(look == Direction.WEST){
				hit = hit.add(-xd + 1, 0, 0);
			}
			
			if(playerIn.isSneaking() && playerIn.isCreative()){
				if(multiblock.getUniqueName().toString().contains("excavator_demo")){
					hit = hit.add(0, -2, 0);
				}
				boolean flip = getFlipped(stack);
				
				// TODO Redo the creative placement
				
				Rotation rot=Rotation.NONE;
				if(facing.getAxis()!=Axis.Y)
					rot=Utils.getRotationBetweenFacings(Direction.NORTH, facing);
				
				Mirror mirror=flip?Mirror.LEFT_RIGHT:Mirror.NONE;
				PlacementSettings settings=new PlacementSettings()
						.setMirror(mirror)
						.setRotation(rot);
				
				Template multiblockTemplate=getMultiblockTemplate(multiblock);
				List<Template.BlockInfo> blocks=multiblockTemplate.blocks.get(0);
				
				for(int i=0;i<blocks.size();i++){
					Template.BlockInfo info=blocks.get(i);
					
					state=info.state.mirror(mirror).rotate(rot);
					
					world.setBlockState(hit.add(Template.transformedBlockPos(settings, info.pos)), state);
				}
				
				/*
				int idx = 0;
				for(int h = 0;h < mh;h++){
					for(int l = 0;l < ml;l++){
						for(int w = 0;w < mw;w++){
							if(multiblock.getStructureManual()[h][l][w] != null && !multiblock.getStructureManual()[h][l][w].isEmpty()){
								int xo = l;
								int zo = w;
								
								switch(rotate){
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
								if(rotate % 2 == 1){
									xo = flip ? xo : (mw - xo - 1);
								}else{
									zo = flip ? zo : (mw - zo - 1);
								}
								
								BlockPos actualPos = hit.add(xo, h, zo);
								
								ItemStack toPlace = multiblock.getStructureManual()[h][l][w];
								BlockState stt = multiblock.getBlockstateFromStack(idx, toPlace);
								SchematicPlaceBlockEvent placeEvent = new SchematicPlaceBlockEvent(multiblock, idx, stt, world, rotate, l, h, w);
								if(!MinecraftForge.EVENT_BUS.post(placeEvent)){
									world.setBlockState(actualPos, placeEvent.getBlockState());
									
									SchematicPlaceBlockPostEvent postEvent = new SchematicPlaceBlockPostEvent(multiblock, idx, stt, actualPos, world, rotate, l, h, w);
									MinecraftForge.EVENT_BUS.post(postEvent);
								}
								
							}
						}
					}
				}
				*/
				
				return ActionResultType.SUCCESS;
			}
			
			CompoundNBT posTag = new CompoundNBT();
			posTag.putInt("x", hit.getX());
			posTag.putInt("y", hit.getY());
			posTag.putInt("z", hit.getZ());
			ItemNBTHelper.setTagCompound(stack, "pos", posTag);
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}
	
	public static int getRotation(ItemStack stack){
		if(ItemNBTHelper.hasKey(stack, "rotate")){
			return ItemNBTHelper.getInt(stack, "rotate");
		}
		return 0;
	}
	
	public static boolean getFlipped(ItemStack stack){
		if(ItemNBTHelper.hasKey(stack, "flip")){
			return ItemNBTHelper.getBoolean(stack, "flip");
		}
		return false;
	}
	
	public static void rotateClient(ItemStack stack){
		int newRotate = (getRotation(stack) + 1) % 4;
		boolean flip = getFlipped(stack);
		setRotate(stack, newRotate);
		IPPacketHandler.INSTANCE.sendToServer(new RotateSchematicPacket(newRotate, flip));
	}
	
	public static void flipClient(ItemStack stack){
		int newRotate = getRotation(stack);
		boolean flip = !getFlipped(stack);
		setFlipped(stack, flip);
		IPPacketHandler.INSTANCE.sendToServer(new RotateSchematicPacket(newRotate, flip));
	}
	
	public static void setRotate(ItemStack stack, int rotate){
		ItemNBTHelper.putInt(stack, "rotate", rotate);
	}
	
	public static void setFlipped(ItemStack stack, boolean flip){
		ItemNBTHelper.putBoolean(stack, "flip", flip);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
		ItemStack stack=playerIn.getHeldItem(handIn);
		if(ItemNBTHelper.hasKey(stack, "pos") && playerIn.isSneaking()){
			ItemNBTHelper.remove(stack, "pos");
			return ActionResult.newResult(ActionResultType.SUCCESS, stack);
		}
		return ActionResult.newResult(ActionResultType.SUCCESS, stack);
	}


	/** Find the key that is being pressed while minecraft is in focus. Copied from my own project. */
	@OnlyIn(Dist.CLIENT)
	private boolean isPressingKey(int key){
		long window = Minecraft.getInstance().mainWindow.getHandle();
		return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
	}
	
	/** Find the mouse button that is being pressed while minecraft is in focus. Copied from my own project. */
	@OnlyIn(Dist.CLIENT)
	private boolean isPressingMouse(int key){
		long window = Minecraft.getInstance().mainWindow.getHandle();
		return GLFW.glfwGetMouseButton(window, key) == GLFW.GLFW_PRESS;
	}
	
	private boolean shiftHeld=false;
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void handleScroll(InputEvent.MouseScrollEvent event){
		double delta=event.getScrollDelta();
		
		if(this.shiftHeld){
			PlayerEntity player=ClientUtils.mc().player;
			ItemStack mainItem = player.getHeldItemMainhand();
			ItemStack secondItem = player.getHeldItemOffhand();
			
			boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(mainItem, "multiblock");
			boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");
			ItemStack target = main ? mainItem : secondItem;
			
			if(main || off){
				if(delta<0){
					ItemProjector.flipClient(target);
					
					boolean flipped=ItemProjector.getFlipped(target);
					String yesno=flipped?I18n.format("chat.immersivepetroleum.info.projector.flipped.yes"):I18n.format("chat.immersivepetroleum.info.projector.flipped.no");
					player.sendStatusMessage(new TranslationTextComponent("chat.immersivepetroleum.info.projector.flipped", yesno), true);
				}else if(delta>0){
					ItemProjector.rotateClient(target);
					int rot=ItemProjector.getRotation(target);
					player.sendStatusMessage(new StringTextComponent("Rotated "+rot), true);
				}
				event.setCanceled(true);
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void handleKey(InputEvent.KeyInputEvent event){
		if(event.getKey()==GLFW.GLFW_KEY_RIGHT_SHIFT || event.getKey()==GLFW.GLFW_KEY_LEFT_SHIFT){
			switch(event.getAction()){
				case GLFW.GLFW_PRESS:{
					shiftHeld=true;
					return;
				}
				case GLFW.GLFW_RELEASE:{
					shiftHeld=false;
					return;
				}
			}
		}
	}
	
	boolean lastDown = false;
	int tmpCode=0;
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void handleKeypress(ClientTickEvent event){
		/*Minecraft mc = ClientUtils.mc();
		if(event.phase == Phase.START && mc.player != null){
			ItemStack mainItem = mc.player.getHeldItemMainhand();
			ItemStack secondItem = mc.player.getHeldItemOffhand();
			
			boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(mainItem, "multiblock");
			boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");
			ItemStack target = main ? mainItem : secondItem;
			
			if(main || off){
				if(isPressingMouse(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)){
					if(!lastDown){
						if(mc.player.isSneaking()){
							ItemProjector.flipClient(target);
						}else{
							ItemProjector.rotateClient(target);
						}
						lastDown=true;
					}
				}
			}
		}else if(event.phase == Phase.END && lastDown){
			lastDown=false;
		}//*/
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void renderLast(RenderWorldLastEvent event){
		boolean preview=true;
		boolean autolubeRender=false;
		
		Minecraft mc = ClientUtils.mc();
		
		GlStateManager.pushMatrix();
		{
			if(preview && mc.player != null){
				ItemStack secondItem = mc.player.getHeldItemOffhand();
				
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");
				
				for(int i = 0;i < 11;i++){
					GlStateManager.pushMatrix();
					{
						ItemStack stack = (i == 10 ? secondItem : mc.player.inventory.getStackInSlot(i));
						if(!stack.isEmpty() && stack.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(stack, "multiblock")){
							renderSchematic(stack, mc.player, mc.player.world, event.getPartialTicks(), i == mc.player.inventory.currentItem || (i == 10 && off));
						}
					}
					GlStateManager.popMatrix();
				}
			}
			
			if(autolubeRender && mc.player != null){
				ItemStack mainItem = mc.player.getHeldItemMainhand();
				ItemStack secondItem = mc.player.getHeldItemOffhand();
				
				boolean main = (mainItem != null && !mainItem.isEmpty()) && mainItem.getItem() == IPContent.blockMetalDevice.asItem();
				boolean off = (secondItem != null && !secondItem.isEmpty()) && secondItem.getItem() == IPContent.blockMetalDevice.asItem();
				
				if(main || off){
					ItemRenderer itemRenderer=ClientUtils.mc().getItemRenderer();
					
					BlockPos base = mc.player.getPosition();
					for(int x = -16;x <= 16;x++){
						for(int z = -16;z <= 16;z++){
							for(int y = -16;y <= 16;y++){
								BlockPos pos = base.add(x, y, z);
								TileEntity te = mc.player.world.getTileEntity(pos);
								
								if(te != null){
									ILubricationHandler handler = LubricatedHandler.getHandlerForTile(te);
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
													GlStateManager.translated(0.5, -.13, .5);
													
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
													
													GlStateManager.scaled(1 / 0.65F, 1 / 0.65F, 1 / 0.65F);
													GlStateManager.scaled(2, 2, 2);
													
													ItemStack toRender = new ItemStack(IPContent.blockMetalDevice);
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
	
	@SuppressWarnings("unused")
	public void renderSchematic(ItemStack target, PlayerEntity player, World world, float partialTicks, boolean shouldRenderMoving){
		Minecraft mc = ClientUtils.mc();
		IMultiblock mb = ItemProjector.getMultiblock(target);
		if(mb != null){
			ItemStack heldStack = player.getHeldItemMainhand();
			
			Vec3i size=mb.getSize();
			
			int mh = size.getY();
			int ml = size.getX();
			int mw = size.getZ();
			
			int rotate = ItemProjector.getRotation(target);
			boolean flip = ItemProjector.getFlipped(target);
			
			int xd = (rotate % 2 == 0) ? ml : mw;
			int zd = (rotate % 2 == 0) ? mw : ml;
			
			BlockPos hit = null;
			
			boolean isPlaced = false;
			
			if(ItemNBTHelper.hasKey(target, "pos")){
				CompoundNBT pos = ItemNBTHelper.getTagCompound(target, "pos");
				int x = pos.getInt("x");
				int y = pos.getInt("y");
				int z = pos.getInt("z");
				hit = new BlockPos(x, y, z);
				isPlaced = true;
			}else if(shouldRenderMoving && ClientUtils.mc().objectMouseOver != null && ClientUtils.mc().objectMouseOver.getType() == Type.BLOCK){
				BlockRayTraceResult blockRTResult=(BlockRayTraceResult)ClientUtils.mc().objectMouseOver;
				
				BlockPos pos = (BlockPos)blockRTResult.getPos();
				
				BlockState state = world.getBlockState(pos);
				if(state.getMaterial().isReplaceable() || blockRTResult.getFace() != Direction.UP){
					hit = pos;
				}else{
					hit = pos.add(0, 1, 0);
				}
				
				Vec3d vec = mc.player.getLookVec();
				Direction facing = (Math.abs(vec.z) > Math.abs(vec.x)) ? (vec.z > 0 ? Direction.SOUTH : Direction.NORTH) : (vec.x > 0 ? Direction.EAST : Direction.WEST);
				
				if(facing == Direction.NORTH || facing == Direction.SOUTH){
					hit = hit.add(-xd / 2, 0, 0);
				}else if(facing == Direction.EAST || facing == Direction.WEST){
					hit = hit.add(0, 0, -zd / 2);
				}
				
				if(facing == Direction.NORTH){
					hit = hit.add(0, 0, -zd + 1);
				}else if(facing == Direction.WEST){
					hit = hit.add(-xd + 1, 0, 0);
				}
			}
			
			if(hit != null){
				if(mb.getUniqueName().getPath().contains("excavator_demo")){
					hit = hit.add(0, -2, 0);
				}
				
				double px = TileEntityRendererDispatcher.staticPlayerX;
				double py = TileEntityRendererDispatcher.staticPlayerY;
				double pz = TileEntityRendererDispatcher.staticPlayerZ;
				
				//final BlockRendererDispatcher blockRender = Minecraft.getInstance().getBlockRendererDispatcher();
				
				GlStateManager.translated(hit.getX() - px, hit.getY() - py, hit.getZ() - pz);
				
				GlStateManager.disableLighting();
				
				if(Minecraft.isAmbientOcclusionEnabled())
					GlStateManager.shadeModel(GL11.GL_SMOOTH);
				else
					GlStateManager.shadeModel(GL11.GL_FLAT);
				
				ClientUtils.bindAtlas();
				
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();
				
				boolean perfect = false;
				
				int idx = 0;
				// TODO Redo schematic preview-rendering
				Template multiblockTemplate=getMultiblockTemplate(mb);
				List<Template.BlockInfo> blocks=multiblockTemplate.blocks.get(0);
				
				PlacementSettings setting=new PlacementSettings();
				setting.setMirror(flip?Mirror.FRONT_BACK:Mirror.NONE);
				setting.setRotation(Rotation.values()[rotate]);
				setting.setCenterOffset(hit);
				
				ItemRenderer itemRenderer=ClientUtils.mc().getItemRenderer();
				for(int i=0;i<blocks.size();i++){
					Template.BlockInfo info=blocks.get(i);
					BlockPos pos=info.pos;
					BlockState state=info.state;
					CompoundNBT nbt=info.nbt;
					
					pos=Template.transformedBlockPos(setting, pos);
					
					GlStateManager.pushMatrix();
					{
						GlStateManager.translated(pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5);
						
						
						float flicker = (world.rand.nextInt(10) == 0) ? 0.75F : (world.rand.nextInt(20) == 0 ? 0.5F : 1F);
						float alpha = false ? .75F : .5F;
						ShaderUtil.alpha_static(flicker * alpha, mc.player.ticksExisted + partialTicks);
						SchematicRenderBlockEvent renderEvent = new SchematicRenderBlockEvent(mb, world, pos, state, nbt, Direction.NORTH);
						if(!MinecraftForge.EVENT_BUS.post(renderEvent)){
							ItemStack toRender = new ItemStack(renderEvent.getState().getBlock());
							itemRenderer.renderItem(toRender, itemRenderer.getModelWithOverrides(toRender));
						}
						
						ShaderUtil.releaseShader();
					}
					GlStateManager.popMatrix();
				}
				
				/*
				for(int h = 0;h < mh;h++){
					boolean slicePerfect = true;
					for(int l = 0;l < ml;l++){
						for(int w = 0;w < mw;w++){
							BlockPos pos = new BlockPos(l, h, w);
							
							GlStateManager.pushMatrix();
							
							if(mb.getStructureManual()[h][l][w] != null && !mb.getStructureManual()[h][l][w].isEmpty()){
								BlockPos blockPos = new BlockPos(0, 0, 0);
								
								int xo = l;
								int zo = w;
								
								switch(rotate){
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
								if(rotate % 2 == 1){
									xo = flip ? xo : (mw - xo - 1);
								}else{
									zo = flip ? zo : (mw - zo - 1);
								}
								
								BlockPos actualPos = hit.add(xo, h, zo);
								
								BlockState otherState = null;
								if(!heldStack.isEmpty() && heldStack.getItem() instanceof BlockItem){
									otherState = ((BlockItem) heldStack.getItem()).getBlock().getStateFromMeta(heldStack.getItemDamage());
								}
								ItemStack stack = mb.getStructureManual()[h][l][w];
								BlockState state = mb.getBlockstateFromStack(idx, stack);
								BlockState actualState = world.getBlockState(actualPos);
								boolean stateEqual = actualState.equals(state);
								boolean otherStateEqual = otherState == null ? false : otherState.equals(state);
								
								int[] ids = OreDictionary.getOreIDs(stack);
								for(int id:ids){
									String idS = OreDictionary.getOreName(id);
									if(Utils.isOreBlockAt(world, actualPos, idS)){
										stateEqual = true;
									}
								}
								
								SchematicTestEvent testEvent = new SchematicTestEvent(stateEqual, mb, idx, stack, world, actualPos, rotate, l, h, w);
								MinecraftForge.EVENT_BUS.post(testEvent);
								stateEqual = testEvent.isEqual();
								
								if(!heldStack.isEmpty() && otherState != null){
									int[] ids2 = OreDictionary.getOreIDs(heldStack);
									for(int id2:ids2){
										for(int id:ids){
											if(id == id2){
												otherStateEqual = true;
											}
										}
									}
								}
								
								boolean isEmpty = world.getBlockState(actualPos).getMaterial().isReplaceable();
								perfect &= stateEqual;
								if(!stateEqual && isEmpty){
									slicePerfect = false;
									float alpha = otherStateEqual ? .75F : .5F;
									ShaderUtil.alpha_static(flicker * alpha, mc.player.ticksExisted + partialTicks);
									GlStateManager.translated(xo, h, zo);
									
									GlStateManager.translated(.5, .5, .5);
									
									GlStateManager.scaled(2.01, 2.01, 2.01);
									
									SchematicRenderBlockEvent renderEvent = new SchematicRenderBlockEvent(mb, idx, stack, world, rotate, l, h, w);
									
									if(!MinecraftForge.EVENT_BUS.post(renderEvent)){
										ItemStack toRender = renderEvent.getItemStack();
										
										ItemRenderer itemRenderer=ClientUtils.mc().getItemRenderer();
										itemRenderer.renderItem(toRender, itemRenderer.getModelWithOverrides(toRender));
									}
									
									ShaderUtil.releaseShader();
								}
								
							}
							GlStateManager.popMatrix();
							
							idx++;
						}
					}
					if(!slicePerfect && isPlaced) break;
				}*/
				
				idx = 0;
				GlStateManager.disableDepthTest();
				/*
				for(int h = 0;h < mh;h++){
					boolean slicePerfect = true;
					for(int l = 0;l < ml;l++){
						for(int w = 0;w < mw;w++){
							BlockPos pos = new BlockPos(l, h, w);
							
							GlStateManager.pushMatrix();
							
							if(mb.getStructureManual()[h][l][w] != null && !mb.getStructureManual()[h][l][w].isEmpty()){
								BlockPos blockPos = new BlockPos(0, 0, 0);
								
								int xo = l;
								int zo = w;
								
								switch(rotate){
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
								if(rotate % 2 == 1){
									xo = flip ? xo : (mw - xo - 1);
								}else{
									zo = flip ? zo : (mw - zo - 1);
								}
								
								BlockPos actualPos = hit.add(xo, h, zo);
								
								BlockState otherState = null;
								if(!heldStack.isEmpty() && heldStack.getItem() instanceof BlockItem){
									otherState = ((BlockItem) heldStack.getItem()).getBlock().getStateFromMeta(heldStack.getItemDamage());
								}
								ItemStack stack = mb.getStructureManual()[h][l][w];
								BlockState state = mb.getBlockstateFromStack(idx, stack);
								BlockState actualState = world.getBlockState(actualPos);
								boolean stateEqual = actualState.equals(state);
								boolean otherStateEqual = otherState == null ? false : otherState.equals(state);
								
								int[] ids = OreDictionary.getOreIDs(stack);
								for(int id:ids){
									String idS = OreDictionary.getOreName(id);
									if(Utils.isOreBlockAt(world, actualPos, idS)){
										stateEqual = true;
									}
								}
								
								SchematicTestEvent testEvent = new SchematicTestEvent(stateEqual, mb, idx, stack, world, actualPos, rotate, l, h, w);
								MinecraftForge.EVENT_BUS.post(testEvent);
								stateEqual = testEvent.isEqual();
								
								if(!heldStack.isEmpty() && otherState != null){
									int[] ids2 = OreDictionary.getOreIDs(heldStack);
									for(int id2:ids2){
										for(int id:ids){
											if(id == id2){
												otherStateEqual = true;
											}
										}
									}
								}
								
								if(!stateEqual){
									boolean isEmpty = world.getBlockState(actualPos).getMaterial().isReplaceable();
									if(isEmpty) slicePerfect = false;
									if(!isEmpty || otherStateEqual){
										GlStateManager.pushMatrix();
										{
											GlStateManager.disableTexture();
											GlStateManager.enableBlend();
											GlStateManager.disableCull();
											GlStateManager.blendFuncSeparate(770, 771, 1, 0);
											GlStateManager.shadeModel(GL11.GL_SMOOTH);
											float r = 1;
											float g = !isEmpty ? 0 : 1;
											float b = !isEmpty ? 0 : 1;
											float alpha = .375F * flicker;
											GlStateManager.translated(xo + .5, h + .5, zo + .5);
											GlStateManager.scaled(1.01, 1.01, 1.01);
											// buffer.setTranslation(l, h, w);
											GlStateManager.lineWidth(2f);
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
											GlStateManager.enableTexture();
										}
										GlStateManager.popMatrix();
									}
									
								}
								
							}
							GlStateManager.popMatrix();
							
							idx++;
						}
					}
					if(!slicePerfect && isPlaced) break;
				}*/
				
				if(perfect){
					
					GlStateManager.pushMatrix();
					{
						GlStateManager.disableTexture();
						GlStateManager.enableBlend();
						GlStateManager.disableCull();
						GlStateManager.blendFuncSeparate(770, 771, 1, 0);
						GlStateManager.shadeModel(GL11.GL_SMOOTH);
						float r = 0;
						float g = 1;
						float b = 0;
						GlStateManager.translated(0, 0, 0);
						GlStateManager.scaled(xd, mh, zd);
						// buffer.setTranslation(l, h, w);
						GlStateManager.lineWidth(2f);
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
						GlStateManager.enableTexture();
					}
					GlStateManager.popMatrix();
				}
				
				RenderHelper.disableStandardItemLighting();
				GlStateManager.disableRescaleNormal();
				
				GlStateManager.enableBlend();
				RenderHelper.disableStandardItemLighting();
			}
		}
		GlStateManager.enableDepthTest();
	}

	@SubscribeEvent
	public void handlePumpPlace(SchematicPlaceBlockPostEvent event){
		IMultiblock mb = event.getMultiblock();
		
		BlockState state = event.getBlockState();
		if(event.getH() < mb.getStructureManual().length - 1){
			ItemStack stack = mb.getStructureManual()[event.getH() + 1][event.getL()][event.getW()];
			if(stack != null && !stack.isEmpty()){
				if(state.getBlock() == IEContent.blockMetalDevice0 && state.getBlock().getMetaFromState(state) == BlockTypes_MetalDevice0.FLUID_PUMP.getMeta() && !(stack.getItemDamage() != BlockTypes_MetalDevice0.FLUID_PUMP.getMeta() || stack.getItem() != Item.getItemFromBlock(IEContent.blockMetalDevice0))){
					TileEntityFluidPump pump = (TileEntityFluidPump) event.getWorld().getTileEntity(event.getPos());
					pump.placeDummies(event.getPos(), state, Direction.UP, 0, 0, 0);
					event.getWorld().setBlockState(event.getPos(), event.getBlockState());
				}
			}
		}
	}

	@SubscribeEvent
	public void handleConveyorPlace(SchematicPlaceBlockPostEvent event)
	{
		IMultiblock mb = event.getMultiblock();
		
		BlockState state = event.getBlockState();
		
		if(mb.getUniqueName().equals("IE:AutoWorkbench") || mb.getUniqueName().equals("IE:BottlingMachine") || mb.getUniqueName().equals("IE:Assembler") || mb.getUniqueName().equals("IE:BottlingMachine") || mb.getUniqueName().equals("IE:MetalPress")){
			if(state.getBlock() == IEContent.blockConveyor){
				TileEntity te = event.getWorld().getTileEntity(event.getPos());
				if(te instanceof TileEntityConveyorBelt){
					TileEntityConveyorBelt conveyor = ((TileEntityConveyorBelt) te);
					ResourceLocation rl = new ResourceLocation(ImmersiveEngineering.MODID, "conveyor");
					IConveyorBelt subType = ConveyorHandler.getConveyor(rl, conveyor);
					conveyor.setConveyorSubtype(subType);
					
					Direction facing = ((TileEntityConveyorBelt) te).facing;
					
					if((mb.getUniqueName().equals("IE:AutoWorkbench") && (event.getW() != 1 || event.getL() != 2)) || mb.getUniqueName().equals("IE:BottlingMachine")){
						conveyor.setFacing(event.getRotate().rotateY());
					}else if(mb.getUniqueName().equals("IE:AutoWorkbench") && (event.getRotate() == Direction.WEST || event.getRotate() == Direction.EAST)){
						conveyor.setFacing(event.getRotate().getOpposite());
					}else{
						conveyor.setFacing(event.getRotate());
					}
					
					event.getWorld().setBlockState(event.getPos(), event.getBlockState());
				}
			}
		}
	}

	@SubscribeEvent
	public void handleConveyorTest(SchematicTestEvent event){ // TODO
		IMultiblock mb = event.getMultiblock();
		
		BlockState state = event.getMultiblock().getBlockstateFromStack(event.getIndex(), event.getItemStack());
		
		if(mb.getUniqueName().equals("IE:AutoWorkbench") || mb.getUniqueName().equals("IE:BottlingMachine") || mb.getUniqueName().equals("IE:Assembler") || mb.getUniqueName().equals("IE:BottlingMachine") || mb.getUniqueName().equals("IE:MetalPress")){
			if(state.getBlock() == IEContent.blockConveyor){
				TileEntity te = event.getWorld().getTileEntity(event.getPos());
				if(te instanceof TileEntityConveyorBelt){
					IConveyorBelt subtype = ((TileEntityConveyorBelt) te).getConveyorSubtype();
					if(subtype != null && subtype.getConveyorDirection() != ConveyorDirection.HORIZONTAL){
						event.setIsEqual(false);
						return;
					}
					
					Direction facing = ((TileEntityConveyorBelt) te).facing;
					
					if(event.isEqual() && (mb.getUniqueName().equals("IE:AutoWorkbench") && (event.getW() != 1 || event.getL() != 2)) || mb.getUniqueName().equals("IE:BottlingMachine")){
						if(facing != event.getRotate().rotateY()){
							event.setIsEqual(false);
							return;
						}
					}else if(facing != event.getRotate()){
						event.setIsEqual(false);
						return;
					}
				}else{
					event.setIsEqual(false);
					return;
				}
			}
		}
		
		if(mb.getUniqueName().equals("IP:DistillationTower")){
			if(state.getBlock() == IEContent.blockMetalDecorationSlabs1){
				if(event.getWorld().getBlockState(event.getPos()).getBlock() == IEContent.blockMetalDecorationSlabs1){
					event.setIsEqual(true);
				}
			}
		}
	}
	
	public boolean doesIntersect(PlayerEntity player, ItemStack target, BlockPos check){
		IMultiblock mb = ItemProjector.getMultiblock(target);
		
		if(mb != null){
			int mh = mb.getStructureManual().length;
			int ml = mb.getStructureManual()[0].length;
			int mw = mb.getStructureManual()[0][0].length;
			
			int rotate = ItemProjector.getRotation(target);
			boolean flip = ItemProjector.getFlipped(target);
			
			int xd = (rotate % 2 == 0) ? ml : mw;
			int zd = (rotate % 2 == 0) ? mw : ml;
			
			boolean isPlaced = false;
			
			if(ItemNBTHelper.hasKey(target, "pos")){
				CompoundNBT pos = ItemNBTHelper.getTagCompound(target, "pos");
				int x = pos.getInt("x");
				int y = pos.getInt("y");
				int z = pos.getInt("z");
				
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
	public void handleConveyorsAndPipes(SchematicRenderBlockEvent event){
		if((event.getMultiblock().getUniqueName().getPath().contains("bottling") && event.getPos().getY() == 2)){
			event.setCanceled(true);
		}
		
		BlockState state = event.getState();
		Direction rotate = event.getRotate();
		IMultiblock mb = event.getMultiblock();
		
		if(state.getBlock() == MetalDevices.CONVEYORS.get(BasicConveyor.NAME)){
			switch(rotate){
				case WEST:
					GlStateManager.rotated(180, 0, 1, 0);
					break;
				case NORTH:
					GlStateManager.rotated(90, 0, 1, 0);
					break;
				case SOUTH:
					GlStateManager.rotated(270, 0, 1, 0);
					break;
				default:
					break;
			}
			if((mb.getUniqueName().getPath().contains("auto_workbench") && (event.getPos().getX() != 1 || event.getPos().getZ() != 2)) || mb.getUniqueName().getPath().contains("bottling_machine")){
				GlStateManager.rotated(270, 0, 1, 0);
			}else if(mb.getUniqueName().getPath().contains("auto_workbench") && (rotate == Direction.WEST || rotate == Direction.EAST)){
				GlStateManager.rotated(180, 0, 1, 0);
			}
			GlStateManager.rotated(90, 0, 1, 0);
		}else if(state.getBlock() == IEBlocks.MetalDevices.fluidPump){
			GlStateManager.translated(0, .225F, 0);
			GlStateManager.scaled(.9F, .9F, .9F);
		}else if(state.getBlock() == Blocks.PISTON){
			GlStateManager.rotated(180, 1, 0, 0);
		}
		
		if(mb.getUniqueName().getPath().contains("distillationtower")){
			if(state.getBlock() == IEBlocks.toSlab.get(IEBlocks.MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))){
				GlStateManager.translated(0, .25F, 0);
				
			}
		}
	}
	
	@SubscribeEvent
	public void handleSlabPlace(SchematicPlaceBlockPostEvent event){ // Is this even nessesary anymore?
		IMultiblock mb = event.getMultiblock();
		
		BlockState state = event.getBlockState();
		if(mb.getUniqueName().getPath().contains("distillationtower")){
			TileEntity te = event.getWorld().getTileEntity(event.getPos());
//			if(te instanceof TileEntityIESlab){
//				((TileEntityIESlab) te).slabType = 1;
//			}
		}
	}
}
