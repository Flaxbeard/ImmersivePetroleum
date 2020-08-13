package flaxbeard.immersivepetroleum.common.items;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBeltTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.event.SchematicPlaceBlockEvent;
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
	
	/** Name cache for {@link ItemProjector#getActualMBName(IMultiblock)} */
	static final Map<Class<? extends IMultiblock>, String> nameCache=new HashMap<>();
	/** Gets the name of the class */
	private static String getActualMBName(IMultiblock multiblock){
		if(!nameCache.containsKey(multiblock.getClass())){
			String name=multiblock.getClass().getSimpleName();
			name=name.substring(0, name.indexOf("Multiblock"));
			
			switch(name){
				case "LightningRod": name="Lightningrod"; break;
				case "ImprovedBlastfurnace": name="BlastFurnaceAdvanced"; break;
			}
			
			nameCache.put(multiblock.getClass(), name);
			//System.out.println(multiblock.getClass().getSimpleName()+" -> "+name);
		}
		
		return nameCache.get(multiblock.getClass());
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items){
		if (this.isInGroup(group)){
			items.add(new ItemStack(this, 1));
			
			List<IMultiblock> multiblocks = MultiblockHandler.getMultiblocks();
			for(IMultiblock multiblock:multiblocks){
				ResourceLocation str = multiblock.getUniqueName();
				if(str.getPath().equals("excavator_demo") || str.getPath().contains("feedthrough"))
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
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	private BlockPos adjustHit(BlockPos hit, PlayerEntity playerIn, int rotate, int width, int depth){
		int xd = (rotate % 2 == 0) ? width : depth;
		int zd = (rotate % 2 == 0) ? depth : width;
		
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
		
		return hit;
	}
	
	@SuppressWarnings("unused")
	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		World world=context.getWorld();
		PlayerEntity playerIn=context.getPlayer();
		Hand hand=context.getHand();
		BlockPos pos=context.getPos();
		Direction facing=context.getFace();
		
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
			int mHeight = size.getY();
			int mWidth = size.getX();
			int mDepth = size.getZ();
			
			int rotate = getRotation(stack);
			
			hit=adjustHit(hit, playerIn, rotate, mWidth, mDepth);
			
			if(playerIn.isSneaking() && playerIn.isCreative()){
				if(multiblock.getUniqueName().getPath().equals("excavator_demo")){
					hit = hit.add(0, -2, 0);
				}
				boolean flip = getFlipped(stack);
				
				PlacementSettings setting=new PlacementSettings();
				setting.setMirror(flip?Mirror.LEFT_RIGHT:Mirror.NONE);
				setting.setRotation(Rotation.values()[rotate]);
				
				if(flip){
					setting.setCenterOffset(new BlockPos(mWidth/2, 0, -mDepth/2));
				}else{
					setting.setCenterOffset(new BlockPos(mWidth/2, 0, mDepth/2));
				}
					
				Template multiblockTemplate=getMultiblockTemplate(multiblock);
				List<Template.BlockInfo> blocks=multiblockTemplate.blocks.get(0);
				for(int i=0;i<blocks.size();i++){
					Template.BlockInfo info=blocks.get(i);
					BlockPos wPos=info.pos;
					BlockState tState=info.state;
					
					wPos=Template.transformedBlockPos(setting, wPos);
					if(flip)
						wPos=wPos.offset(Direction.SOUTH, mDepth/2+1);
					
					// TODO Re-add the event stuff
					SchematicPlaceBlockEvent event=new SchematicPlaceBlockEvent(multiblock, world, wPos, info.pos, tState, info.nbt, Rotation.values()[rotate]);
					if(!MinecraftForge.EVENT_BUS.post(event)){
						world.setBlockState(hit.add(wPos), tState);
						
						SchematicPlaceBlockPostEvent postevent=new SchematicPlaceBlockPostEvent(multiblock, world, wPos, tState, Rotation.values()[rotate]);
					}
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
					player.sendStatusMessage(new StringTextComponent("Rotated "+Rotation.values()[rot]), true);
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void renderLast(RenderWorldLastEvent event){
		boolean preview=true;
		boolean autolubeRender=false;
		
		Minecraft mc = ClientUtils.mc();
		
		GlStateManager.pushMatrix();
		{
			if(preview && mc.player != null){ // TODO BookMark: renderSchematic
				ItemStack secondItem = mc.player.getHeldItemOffhand();
				
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");
				
				for(int i = 0;i <= 10;i++){
					ItemStack stack = (i == 10 ? secondItem : mc.player.inventory.getStackInSlot(i));
					if(!stack.isEmpty() && stack.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(stack, "multiblock")){
						GlStateManager.pushMatrix();
						{
							renderSchematic(stack, mc.player, mc.player.world, event.getPartialTicks(), i == mc.player.inventory.currentItem || (i == 10 && off));
						}
						GlStateManager.popMatrix();
					}
				}
			}
		}
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		{
			if(autolubeRender && mc.player != null){ // Not yet ready for rendering anything!
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
	
	@OnlyIn(Dist.CLIENT)
	private void renderRedOutline(Tessellator tessellator, BufferBuilder buffer, BlockPos wPos, float flicker){
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		float r = 1;
		float g = 0;
		float b = 0;
		float alpha = .375F * flicker;
		GlStateManager.translated(wPos.getX()+0.50, wPos.getY()+0.50, wPos.getZ()+0.50);
		GlStateManager.scaled(1.005, 1.005, 1.005);
		GlStateManager.lineWidth(2.5f);
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
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
	}
	
	@OnlyIn(Dist.CLIENT)
	private void renderPhantom(IMultiblock multiblock, World world, Template.BlockInfo info, BlockPos wPos, float flicker, float alpha, float partialTicks, boolean flipXZ, Rotation rotation){
		ItemRenderer itemRenderer=ClientUtils.mc().getItemRenderer();
		
		GlStateManager.scaled(1.0, 1.0, 1.0);
		
		GlStateManager.translated(wPos.getX()+.5, wPos.getY()+.5, wPos.getZ()+.5); // Finally centers the preview block
		
		ShaderUtil.alpha_static(flicker * alpha, ClientUtils.mc().player.ticksExisted + partialTicks);
		SchematicRenderBlockEvent renderEvent = new SchematicRenderBlockEvent(multiblock, world, wPos, info.pos, info.state, info.nbt, rotation);
		if(!MinecraftForge.EVENT_BUS.post(renderEvent)){
			ItemStack toRender = new ItemStack(renderEvent.getState().getBlock());
			itemRenderer.renderItem(toRender, itemRenderer.getModelWithOverrides(toRender));
		}
		ShaderUtil.releaseShader();
	}
	
	@SuppressWarnings("unused")
	@OnlyIn(Dist.CLIENT)
	public void renderSchematic(ItemStack target, PlayerEntity player, World world, float partialTicks, boolean shouldRenderMoving){
		Minecraft mc = ClientUtils.mc();
		IMultiblock multiblock = ItemProjector.getMultiblock(target);
		if(multiblock != null){
			ItemStack heldStack = player.getHeldItemMainhand();
			
			Vec3i size=multiblock.getSize();
			
			int mWidth = size.getX();
			int mHeight = size.getY();
			int mDepth = size.getZ();
			
			int rotate = ItemProjector.getRotation(target);
			boolean flip = ItemProjector.getFlipped(target);
			
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
				
				hit=adjustHit(hit, mc.player, rotate, mWidth, mDepth);
			}
			
			if(hit != null){
				if(multiblock.getUniqueName().getPath().contains("excavator_demo")){
					hit = hit.add(0, -2, 0);
				}
				
				//final BlockRendererDispatcher blockRender = Minecraft.getInstance().getBlockRendererDispatcher();
				
				boolean perfect = false;
				
				// Determine if the dimensions are even (true) or odd (false)
				boolean evenWidth=(mWidth/2F-(mWidth/2))==0F; // Divide with float, Divide with int then subtract both and check for 0
				boolean evenDepth=(mDepth/2F-(mDepth/2))==0F;
				
				PlacementSettings setting=new PlacementSettings();
				setting.setMirror(flip?Mirror.FRONT_BACK:Mirror.NONE);
				setting.setRotation(Rotation.values()[rotate]);
				
				if(!flip){
					setting.setCenterOffset(new BlockPos(mWidth/2, 0, mDepth/2));
				}else{
					setting.setCenterOffset(new BlockPos(-mWidth/2, 0, mDepth/2));
				}
				
				// Take even/odd-ness of multiblocks into consideration for rotation
				int xa=evenWidth?-1:0;
				int za=evenDepth?-1:0;
				BlockPos rotationOffset=BlockPos.ZERO;
				if(!flip){
					switch(setting.getRotation()){
						case CLOCKWISE_90:
							rotationOffset=new BlockPos(xa, 0, 0);
							break;
						case CLOCKWISE_180:
							rotationOffset=new BlockPos(xa, 0, za);
							break;
						case COUNTERCLOCKWISE_90:
							rotationOffset=new BlockPos(0, 0, za);
							break;
						default:
							break;
					}
				}else{
					switch(setting.getRotation()){
						case CLOCKWISE_90:
							rotationOffset=new BlockPos(0, 0, za);
							break;
						case CLOCKWISE_180:
							rotationOffset=new BlockPos(-xa, 0, za);
							break;
						case COUNTERCLOCKWISE_90:
							rotationOffset=new BlockPos(-xa, 0, 0);
							break;
						default:
							break;
					}
				}
				
				// TODO Bookmark: Slicing
				
				Template multiblockTemplate=getMultiblockTemplate(multiblock);
				List<Template.BlockInfo> blocks=multiblockTemplate.blocks.get(0);
				List<RenderInfo> toRender=new ArrayList<>();
				
				int currentSlice=0;
				int badBlocks=0;
				int goodBlocks=0;
				for(int i=0;i<blocks.size();i++){
					Template.BlockInfo info=blocks.get(i);
					BlockState tState=info.state;
					BlockPos tPos=Template.transformedBlockPos(setting, info.pos) // Transformed Position
							.add(rotationOffset);
					
					if(flip){
//						if(setting.getRotation()==Rotation.CLOCKWISE_90 || setting.getRotation()==Rotation.COUNTERCLOCKWISE_90){
//							tPos=tPos.offset(Direction.EAST, mDepth/2+1);
//						}else{
							int a=2;
							if(mWidth!=3) a=mWidth/2;
							if(evenWidth) a+=1;
							tPos=tPos.offset(Direction.EAST, a);
//						}
					}
					
					// Slice handling
					if(badBlocks==0 && info.pos.getY()>currentSlice){
						currentSlice=info.pos.getY();
					}else if(info.pos.getY()!=currentSlice){
						break;
					}
					
					if(isPlaced){ // Render only slices when placed
						if(info.pos.getY()==currentSlice){
							boolean skip=false;
							BlockState toCompare=world.getBlockState(hit.add(tPos));
							if(tState.getBlock()==toCompare.getBlock()){
								goodBlocks++;
								continue;
							}else{
								// Making it this far only needs an air check, the other already proved to be false.
								if(toCompare!=Blocks.AIR.getDefaultState()){
									toRender.add(new RenderInfo(1, info, setting, tPos));
									skip=true;
								}
								badBlocks++;
							}
							
							if(!skip){
								toRender.add(new RenderInfo(0, info, setting, tPos));
							}
						}
					}else{ // Render all when not placed
						toRender.add(new RenderInfo(0, info, setting, tPos));
					}
				}
				
				perfect=(goodBlocks==blocks.size());
				
				toRender.sort((a,b)->{
					if(a.layer>b.layer){
						return 1;
					}else if(a.layer<b.layer){
						return -1;
					}
					return 0;
				});
				
				// TODO Bookmark: Preview Rendering
				
				double px = TileEntityRendererDispatcher.staticPlayerX;
				double py = TileEntityRendererDispatcher.staticPlayerY;
				double pz = TileEntityRendererDispatcher.staticPlayerZ;
				
				GlStateManager.translated(hit.getX() - px, hit.getY() - py, hit.getZ() - pz);
				GlStateManager.disableLighting();
				GlStateManager.shadeModel(Minecraft.isAmbientOcclusionEnabled() ? GL11.GL_SMOOTH : GL11.GL_FLAT);
				GlStateManager.enableBlend();
				ClientUtils.bindAtlas();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();
				final float flicker = (world.rand.nextInt(10) == 0) ? 0.75F : (world.rand.nextInt(20) == 0 ? 0.5F : 1F);
				toRender.forEach(rInfo->{
					Template.BlockInfo info=rInfo.blockInfo;
					float alpha = heldStack.getItem()==info.state.getBlock().asItem() ? 1.0F : .5F;
					switch(rInfo.layer){
						case 0:{ // All / Slice
							GlStateManager.pushMatrix();
							{
								renderPhantom(multiblock, world, info, rInfo.worldPos, flicker, alpha, partialTicks, flip, rInfo.settings.getRotation());
							}
							GlStateManager.popMatrix();
							break;
						}
						case 1:{ // Bad block
							GlStateManager.pushMatrix();
							{
								GlStateManager.disableDepthTest();
								renderRedOutline(tessellator, buffer, rInfo.worldPos, flicker);
								GlStateManager.enableDepthTest();
							}
							GlStateManager.popMatrix();
							break;
						}
					}
				});
				
				if(perfect){
					GlStateManager.pushMatrix();
					{
						GlStateManager.disableDepthTest();
						int xd = (rotate % 2 == 0) ? mWidth : mDepth;
						int zd = (rotate % 2 == 0) ? mDepth : mWidth;
						
						GlStateManager.disableTexture();
						GlStateManager.enableBlend();
						GlStateManager.disableCull();
						GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
						GlStateManager.shadeModel(GL11.GL_SMOOTH);
						float r = 0;
						float g = 1;
						float b = 0;
						GlStateManager.translated(0, 0, 0);
						GlStateManager.scaled(xd, mHeight, zd);
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
						GlStateManager.shadeModel(GL11.GL_FLAT);
						GlStateManager.enableCull();
						GlStateManager.disableBlend();
						GlStateManager.enableTexture();
						GlStateManager.enableDepthTest();
					}
					GlStateManager.popMatrix();
				}
				
				/*
				int idx = 0;
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
				}
				
				idx = 0;
				
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
			}
		}
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
	public void handleConveyorPlace(SchematicPlaceBlockPostEvent event){
		IMultiblock mb = event.getMultiblock();
		BlockState state = event.getBlockState();
		String mbName = mb.getUniqueName().getPath();
		
		if(mbName.equals("multiblocks/auto_workbench") || mbName.equals("multiblocks/bottling_machine") || mbName.equals("multiblocks/assembler") || mbName.equals("multiblocks/metal_press")){
			if(state.getBlock() == IEBlocks.MetalDevices.CONVEYORS.get(null)){
				TileEntity te = event.getWorld().getTileEntity(event.getPos());
				if(te instanceof ConveyorBeltTileEntity){
					ConveyorBeltTileEntity conveyor = ((ConveyorBeltTileEntity) te);
					ResourceLocation rl = new ResourceLocation(ImmersiveEngineering.MODID, "conveyor");
					IConveyorBelt subType = ConveyorHandler.getConveyor(rl, conveyor);
					conveyor.setConveyorSubtype(subType);
					
					Direction facing = ((ConveyorBeltTileEntity) te).getFacing();
					
					if((mbName.equals("multiblocks/auto_workbench") && (event.getMultiblock().getSize().getX() != 1 || event.getMultiblock().getSize().getZ() != 2)) || mbName.equals("multiblocks/bottling_machine")){
						conveyor.setFacing(event.getRotate().rotateY());
					}else if(mbName.equals("multiblocks/auto_workbench") && (event.getRotate() == Direction.WEST || event.getRotate() == Direction.EAST)){
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
		public void handleSlabPlace(SchematicPlaceBlockPostEvent event){ // TODO Is this even nessesary anymore?
	//		IMultiblock mb = event.getMultiblock();
	//		
	//		BlockState state = event.getBlockState();
	//		if(mb.getUniqueName().getPath().equals("multiblocks/distillationtower")){
	//			TileEntity te = event.getWorld().getTileEntity(event.getPos());
	//			if(te instanceof TileEntityIESlab){
	//				((TileEntityIESlab) te).slabType = 1;
	//			}
	//		}
		}

	@SubscribeEvent
	public void handleConveyorTest(SchematicTestEvent event){ // TODO
		IMultiblock mb = event.getMultiblock();
		
		BlockState state = event.getMultiblock().getBlockstateFromStack(event.getIndex(), event.getItemStack());
		
		String mbName=mb.getUniqueName().getPath();
		
		if(mbName.equals("multiblocks/auto_workbench") || mbName.equals("multiblocks/bottling_machine") || mbName.equals("multiblocks/assembler") || mbName.equals("multiblocks/metal_press")){
			if(state.getBlock() == IEContent.blockConveyor){
				TileEntity te = event.getWorld().getTileEntity(event.getPos());
				if(te instanceof TileEntityConveyorBelt){
					IConveyorBelt subtype = ((TileEntityConveyorBelt) te).getConveyorSubtype();
					if(subtype != null && subtype.getConveyorDirection() != ConveyorDirection.HORIZONTAL){
						event.setIsEqual(false);
						return;
					}
					
					Direction facing = ((TileEntityConveyorBelt) te).facing;
					
					if(event.isEqual() && (mbName.equals("multiblocks/auto_workbench") && (event.getW() != 1 || event.getL() != 2)) || mbName.equals("multiblocks/bottling_machine")){
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
		
		if(mbName.equals("multiblocks/distillationtower")){
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
	
	@SubscribeEvent // TODO Bookmark: SchematicRenderBlockEvent usage
	public void handleConveyorsAndPipes(SchematicRenderBlockEvent event){
		String mbName=event.getMultiblock().getUniqueName().getPath();
		
		if((mbName.equals("multiblocks/bottling") && event.getTemplatePos().getY() == 2)){
			event.setCanceled(true);
			return;
		}
		
		BlockState state = event.getState();
		Rotation rotate = event.getRotate();
		
		if(state.getBlock() == MetalDevices.CONVEYORS.get(BasicConveyor.NAME)){
			switch(rotate){
				case CLOCKWISE_180:
					GlStateManager.rotated(180, 0, 1, 0);
					break;
				case CLOCKWISE_90:
					GlStateManager.rotated(90, 0, 1, 0);
					break;
				case COUNTERCLOCKWISE_90:
					GlStateManager.rotated(270, 0, 1, 0);
					break;
				default: // East
					break;
			}
			if((mbName.equals("multiblocks/auto_workbench") && (event.getTemplatePos().getX() != 1 || event.getTemplatePos().getZ() != 2)) || mbName.equals("multiblocks/bottling_machine")){
				GlStateManager.rotated(270, 0, 1, 0);
			}else if(mbName.equals("multiblocks/auto_workbench") && (rotate == Rotation.CLOCKWISE_180 || rotate == Rotation.NONE)){
				GlStateManager.rotated(180, 0, 1, 0);
			}
			GlStateManager.rotated(90, 0, 1, 0);
		}else if(state.getBlock() == IEBlocks.MetalDevices.fluidPump){
			GlStateManager.translated(0, .225F, 0);
			GlStateManager.scaled(.9F, .9F, .9F);
		}else if(state.getBlock() == Blocks.PISTON){
			GlStateManager.rotated(180, 1, 0, 0);
		}
		
		if(mbName.equals("multiblocks/distillationtower")){
			if(state.getBlock() == IEBlocks.toSlab.get(IEBlocks.MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))){
				GlStateManager.translated(0, .25F, 0);
			}
		}
	}
	
	private static class RenderInfo{
		public final int layer;
		public final Template.BlockInfo blockInfo;
		public final BlockPos worldPos;
		public final PlacementSettings settings;
		public RenderInfo(int layer, Template.BlockInfo blockInfo, PlacementSettings settings, BlockPos worldPos){
			this.layer=layer;
			this.blockInfo=blockInfo;
			this.worldPos=worldPos;
			this.settings=settings;
		}
	}
}
