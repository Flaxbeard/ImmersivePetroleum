package flaxbeard.immersivepetroleum.common.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.event.ProjectorEvent;
import flaxbeard.immersivepetroleum.client.ClientProxy;
import flaxbeard.immersivepetroleum.client.ShaderUtil;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Items;
import flaxbeard.immersivepetroleum.common.util.projector.MultiblockProjection;
import flaxbeard.immersivepetroleum.common.util.projector.Settings;
import flaxbeard.immersivepetroleum.common.util.projector.Settings.Mode;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

public class ProjectorItem extends IPItemBase{
	public ProjectorItem(String name){
		super(name, new Item.Properties().maxStackSize(1));
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack){
		String selfKey = getTranslationKey(stack);
		if(stack.hasTag()){
			Settings settings = getSettings(stack);
			if(settings.getMultiblock() != null){
				TranslationTextComponent name = new TranslationTextComponent("desc.immersiveengineering.info.multiblock.IE:" + getActualMBName(settings.getMultiblock()));
				
				return new TranslationTextComponent(selfKey + ".specific", name).mergeStyle(TextFormatting.GOLD);
			}
		}
		return new TranslationTextComponent(selfKey).mergeStyle(TextFormatting.GOLD);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		Settings settings = getSettings(stack);
		if(settings.getMultiblock() != null){
			Vector3i size = settings.getMultiblock().getSize(worldIn);
			
			String name = getActualMBName(settings.getMultiblock());
			tooltip.add(new TranslationTextComponent("desc.immersivepetroleum.info.projector.build0"));
			tooltip.add(new TranslationTextComponent("desc.immersivepetroleum.info.projector.build1", new TranslationTextComponent("desc.immersiveengineering.info.multiblock.IE:" + name)));
			
			if(isPressing(GLFW.GLFW_KEY_LEFT_SHIFT) || isPressing(GLFW.GLFW_KEY_RIGHT_SHIFT)){
				ITextComponent mbSize = new TranslationTextComponent("desc.immersivepetroleum.info.projector.size", size.getX(), size.getY(), size.getZ()).mergeStyle(TextFormatting.DARK_GRAY);
				tooltip.add(mbSize);
				
				Direction dir = Direction.byHorizontalIndex(settings.getRotation().ordinal());
				ITextComponent rotation = new TranslationTextComponent("desc.immersivepetroleum.info.projector.rotated." + dir).mergeStyle(TextFormatting.DARK_GRAY);
				
				ITextComponent flip;
				if(settings.isMirrored()){
					flip = new TranslationTextComponent("desc.immersivepetroleum.info.projector.flipped.true").mergeStyle(TextFormatting.DARK_GRAY);
				}else{
					flip = new TranslationTextComponent("desc.immersivepetroleum.info.projector.flipped.false").mergeStyle(TextFormatting.DARK_GRAY);
				}
				
				if(settings.getPos() != null){
					int x = settings.getPos().getX();
					int y = settings.getPos().getY();
					int z = settings.getPos().getZ();
					
					tooltip.add(new TranslationTextComponent("desc.immersivepetroleum.info.projector.center", x, y, z).mergeStyle(TextFormatting.DARK_GRAY));
				}
				
				tooltip.add(rotation);
				tooltip.add(flip);
			}else{
				ITextComponent text = new StringTextComponent("[")
						.appendSibling(new TranslationTextComponent("desc.immersivepetroleum.info.projector.holdshift"))
						.appendString("] ")
						.appendSibling(new TranslationTextComponent("desc.immersivepetroleum.info.projector.holdshift.text"))
						.mergeStyle(TextFormatting.DARK_AQUA);
				tooltip.add(text);
			}
			
			if(isPressing(GLFW.GLFW_KEY_LEFT_CONTROL) || isPressing(GLFW.GLFW_KEY_RIGHT_CONTROL)){
				ITextComponent ctrl0 = new TranslationTextComponent("desc.immersivepetroleum.info.projector.control1").mergeStyle(TextFormatting.DARK_GRAY);
				ITextComponent ctrl1 = new TranslationTextComponent("desc.immersivepetroleum.info.projector.control2", ClientProxy.keybind_preview_flip.func_238171_j_()).mergeStyle(TextFormatting.DARK_GRAY);
				ITextComponent ctrl2 = new TranslationTextComponent("desc.immersivepetroleum.info.projector.control3").mergeStyle(TextFormatting.DARK_GRAY);
				
				tooltip.add(ctrl0);
				tooltip.add(ctrl1);
				tooltip.add(ctrl2);
			}else{
				ITextComponent text = new StringTextComponent("[")
						.appendSibling(new TranslationTextComponent("desc.immersivepetroleum.info.projector.holdctrl"))
						.appendString("] ")
						.appendSibling(new TranslationTextComponent("desc.immersivepetroleum.info.projector.holdctrl.text"))
						.mergeStyle(TextFormatting.DARK_PURPLE);
				tooltip.add(text);
			}
		}else{
			tooltip.add(new TranslationTextComponent("desc.immersivepetroleum.info.projector.noMultiblock"));
		}
	}
	
	/** Find the key that is being pressed while minecraft is in focus */
	@OnlyIn(Dist.CLIENT)
	private boolean isPressing(int key){
		long window = Minecraft.getInstance().getMainWindow().getHandle();
		return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
	}
	
	/** Name cache for {@link ProjectorItem#getActualMBName(IMultiblock)} */
	static final Map<Class<? extends IMultiblock>, String> nameCache = new HashMap<>();
	/** Gets the name of the class */
	public static String getActualMBName(IMultiblock multiblock){
		if(!nameCache.containsKey(multiblock.getClass())){
			String name = multiblock.getClass().getSimpleName();
			name = name.substring(0, name.indexOf("Multiblock"));
			
			switch(name){
				case "LightningRod": name="Lightningrod"; break;
				case "ImprovedBlastfurnace": name="BlastFurnaceAdvanced"; break;
			}
			
			nameCache.put(multiblock.getClass(), name);
		}
		
		return nameCache.get(multiblock.getClass());
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items){
		if(this.isInGroup(group)){
			items.add(new ItemStack(this, 1));
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
		ItemStack held = playerIn.getHeldItem(handIn);
		
		boolean changeMode = false;
		Settings settings = getSettings(held);
		switch(settings.getMode()){
			case PROJECTION:{
				if(worldIn.isRemote){
					if(playerIn.isSneaking()){
						if(settings.getPos() != null){
							settings.setPos(null);
							settings.sendPacketToServer(handIn);
						}else{
							changeMode = true;
						}
					}
				}
				break;
			}
			case MULTIBLOCK_SELECTION:{
				if(worldIn.isRemote){
					if(!playerIn.isSneaking()){
						ImmersivePetroleum.proxy.openProjectorGui(handIn, held);
					}else{
						changeMode = true;
					}
				}
				break;
			}
			default:break;
		}
		
		if(worldIn.isRemote && changeMode){
			int modeId = settings.getMode().ordinal() + 1;
			settings.setMode(Mode.values()[modeId >= Mode.values().length ? 0 : modeId]);
			settings.applyTo(held);
			settings.sendPacketToServer(handIn);
			playerIn.sendStatusMessage(settings.getMode().getTranslated(), true);
		}
		
		return ActionResult.resultSuccess(held);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		World world = context.getWorld();
		PlayerEntity playerIn = context.getPlayer();
		Hand hand = context.getHand();
		BlockPos pos = context.getPos();
		Direction facing = context.getFace();
		
		ItemStack stack = playerIn.getHeldItem(hand);
		final Settings settings = ProjectorItem.getSettings(stack);
		if(playerIn.isSneaking() && settings.getPos() != null){
			if(world.isRemote){
				settings.setPos(null);
				settings.applyTo(stack);
				settings.sendPacketToServer(hand);
			}
			
			return ActionResultType.SUCCESS;
		}
		
		if(settings.getMode() == Mode.PROJECTION && settings.getPos() == null && settings.getMultiblock() != null){
			BlockState state = world.getBlockState(pos);
			
			final Mutable hit = pos.toMutable();
			if(!state.getMaterial().isReplaceable() && facing == Direction.UP){
				hit.setAndOffset(hit, 0, 1, 0);
			}
			
			Vector3i size = settings.getMultiblock().getSize(world);
			alignHit(hit, playerIn, size, settings.getRotation(), settings.isMirrored());
			
			if(playerIn.isSneaking() && playerIn.isCreative()){
				if(!world.isRemote){
					if(settings.getMultiblock().getUniqueName().getPath().contains("excavator_demo") || settings.getMultiblock().getUniqueName().getPath().contains("bucket_wheel")){
						hit.setAndOffset(hit, 0, -2, 0);
					}
					
					Predicate<MultiblockProjection.Info> pred = layer -> {
						BlockState tstate = layer.templateWorld.getBlockState(layer.templatePos);
						tstate = tstate.rotate(world, pos, settings.getRotation());
						
						ProjectorEvent.PlaceBlock event = new ProjectorEvent.PlaceBlock(layer.multiblock, layer.templateWorld, layer.templatePos, world, layer.tPos, tstate, settings.getRotation());
						if(!MinecraftForge.EVENT_BUS.post(event)){
							tstate = event.getState();
							
							world.setBlockState(layer.tPos.add(hit), tstate);
							
							ProjectorEvent.PlaceBlockPost postEvent = new ProjectorEvent.PlaceBlockPost(layer.multiblock, layer.templateWorld, event.getTemplatePos(), world, layer.tPos, event.getState(), settings.getRotation());
							MinecraftForge.EVENT_BUS.post(postEvent);
						}
						
						return false; // Don't ever skip a step.
					};
					
					MultiblockProjection projection = new MultiblockProjection(world, settings.getMultiblock());
					projection.setFlip(settings.isMirrored());
					projection.setRotation(settings.getRotation());
					for(int i = 0;i < projection.getLayerCount();i++){
						projection.process(i, pred);
					}
				}
				
				return ActionResultType.SUCCESS;
				
			}else{
				if(world.isRemote){
					settings.setPos(hit);
					settings.applyTo(stack);
					settings.sendPacketToServer(hand);
				}
				
				return ActionResultType.SUCCESS;
			}
		}
		
		return ActionResultType.PASS;
	}
	
	// STATIC METHODS
	
	public static Settings getSettings(@Nullable ItemStack stack){
		return new Settings(stack);
	}
	
	private static void alignHit(Mutable hit, PlayerEntity playerIn, Vector3i size, Rotation rotation, boolean mirror){
		int x = ((rotation.ordinal() % 2 == 0) ? size.getX() : size.getZ()) / 2;
		int z = ((rotation.ordinal() % 2 == 0) ? size.getZ() : size.getX()) / 2;
		Direction facing = playerIn.getHorizontalFacing();
		
		switch(facing){
			case NORTH:	hit.setAndOffset(hit, 0, 0, -z);break;
			case SOUTH:	hit.setAndOffset(hit, 0, 0, z);break;
			case EAST:	hit.setAndOffset(hit, x, 0, 0);break;
			case WEST:	hit.setAndOffset(hit, -x, 0, 0);break;
			default:break;
		}
	}
	
	// STATIC SUPPORT CLASSES
	
	/** Client Rendering Stuff */
	@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT)
	public static class ClientRenderHandler{
		@SubscribeEvent
		public static void renderLast(RenderWorldLastEvent event){
			Minecraft mc = ClientUtils.mc();
			
			if(mc.player != null){
				MatrixStack matrix = event.getMatrixStack();
				matrix.push();
				{
					// Anti-Jiggle when moving
					Vector3d renderView = ClientUtils.mc().gameRenderer.getActiveRenderInfo().getProjectedView();
					matrix.translate(-renderView.x, -renderView.y, -renderView.z);
					
					ItemStack secondItem = mc.player.getHeldItemOffhand();
					boolean off = !secondItem.isEmpty() && secondItem.getItem() == Items.projector && ItemNBTHelper.hasKey(secondItem, "settings");
					
					for(int i = 0;i <= 10;i++){
						ItemStack stack = (i == 10 ? secondItem : mc.player.inventory.getStackInSlot(i));
						if(!stack.isEmpty() && stack.getItem() == Items.projector && ItemNBTHelper.hasKey(stack, "settings")){
							Settings settings = getSettings(stack);
							matrix.push();
							{
								boolean renderMoving = i == mc.player.inventory.currentItem || (i == 10 && off);
								renderSchematic(matrix, settings, mc.player, mc.player.world, event.getPartialTicks(), renderMoving);
							}
							matrix.pop();
						}
					}
				}
				matrix.pop();
			}
		}
		
		static final Mutable FULL_MAX = new Mutable(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		public static void renderSchematic(MatrixStack matrix, Settings settings, PlayerEntity player, World world, float partialTicks, boolean renderMoving){
			if(settings.getMultiblock() == null)
				return;
			
			Vector3i size = settings.getMultiblock().getSize(world);
			final Mutable hit = new Mutable(FULL_MAX.getX(), FULL_MAX.getY(), FULL_MAX.getZ());
			final MutableBoolean isPlaced = new MutableBoolean(false);
			if(settings.getPos() != null){
				hit.setPos(settings.getPos());
				isPlaced.setTrue();
				
			}else if(renderMoving && ClientUtils.mc().objectMouseOver != null && ClientUtils.mc().objectMouseOver.getType() == Type.BLOCK){
				BlockRayTraceResult blockRTResult = (BlockRayTraceResult) ClientUtils.mc().objectMouseOver;
				
				BlockPos pos = (BlockPos) blockRTResult.getPos();
				
				BlockState state = world.getBlockState(pos);
				if(state.getMaterial().isReplaceable() || blockRTResult.getFace() != Direction.UP){
					hit.setPos(pos);
				}else{
					hit.setAndOffset(pos, 0, 1, 0);
				}
				
				alignHit(hit, player, size, settings.getRotation(), settings.isMirrored());
			}
			
			if(!hit.equals(FULL_MAX)){
				ResourceLocation name = settings.getMultiblock().getUniqueName();
				if(name.getPath().contains("excavator_demo") || name.getPath().contains("bucket_wheel")){
					hit.setAndOffset(hit, 0, -2, 0);
				}
				
				MultiblockProjection projection = new MultiblockProjection(world, settings.getMultiblock());
				projection.setRotation(settings.getRotation());
				projection.setFlip(settings.isMirrored());
				
				final List<RenderInfo> toRender = new ArrayList<>();
				final MutableInt currentLayer = new MutableInt();
				final MutableInt badBlocks = new MutableInt();
				final MutableInt goodBlocks = new MutableInt();
				BiPredicate<Integer, MultiblockProjection.Info> bipred = (layer, info) -> {
					// Slice handling
					if(badBlocks.getValue() == 0 && layer > currentLayer.getValue()){
						currentLayer.setValue(layer);
					}else if(layer != currentLayer.getValue()){
						return true; // breaks the internal loop
					}
					
					if(isPlaced.booleanValue()){ // Render only slices when placed
						if(layer == currentLayer.getValue()){
							boolean skip = false;
							BlockState toCompare = world.getBlockState(info.tPos.add(hit));
							BlockState tState = info.templateWorld.getBlockState(info.templatePos).rotate(world, info.tPos.add(hit), info.settings.getRotation());
							if(tState == toCompare){
								toRender.add(new RenderInfo(RenderInfo.Layer.PERFECT, info));
								goodBlocks.increment();
								skip = true;
							}else{
								// Making it this far only needs an air check,
								// the other already proved to be false.
								if(!toCompare.getBlockState().getBlock().isAir(toCompare.getBlockState(), info.templateWorld, info.tPos.add(hit))){
									toRender.add(new RenderInfo(RenderInfo.Layer.BAD, info));
									skip = true;
								}else{
									badBlocks.increment();
								}
							}
							
							if(!skip){
								toRender.add(new RenderInfo(RenderInfo.Layer.ALL, info));
							}
						}
					}else{ // Render all when not placed
						toRender.add(new RenderInfo(RenderInfo.Layer.ALL, info));
					}
					
					return false;
				};
				projection.processAll(bipred);
				
				boolean perfect = (goodBlocks.getValue() == projection.getBlockCount());
				
				Mutable min = new Mutable(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
				Mutable max = new Mutable(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
				float flicker = (world.rand.nextInt(10) == 0) ? 0.75F : (world.rand.nextInt(20) == 0 ? 0.5F : 1F);
				matrix.translate(hit.getX(), hit.getY(), hit.getZ());
				
				toRender.sort((a, b) -> {
					if(a.layer.ordinal() > b.layer.ordinal()){
						return 1;
					}else if(a.layer.ordinal() < b.layer.ordinal()){
						return -1;
					}
					return 0;
				});
				
				ItemStack heldStack = player.getHeldItemMainhand();
				for(RenderInfo rInfo:toRender){
					switch(rInfo.layer){
						case ALL:{ // All / Slice
							boolean held = heldStack.getItem() == rInfo.getState().getBlock().asItem();
							float alpha = held ? 0.55F : 0.25F;
							
							matrix.push();
							{
								renderPhantom(matrix, world, rInfo, settings.isMirrored(), flicker, alpha, partialTicks);
								
								if(held){
									renderCenteredOutlineBox(matrix, 0xAFAFAF, flicker);
								}
							}
							matrix.pop();
							break;
						}
						case BAD:{ // Bad block
							matrix.push();
							{
								matrix.translate(rInfo.worldPos.getX(), rInfo.worldPos.getY(), rInfo.worldPos.getZ());
								
								renderCenteredOutlineBox(matrix, 0xFF0000, flicker);
							}
							matrix.pop();
							break;
						}
						case PERFECT:{
							int x = rInfo.worldPos.getX();
							int y = rInfo.worldPos.getY();
							int z = rInfo.worldPos.getZ();
							
							min.setPos(
									(x < min.getX() ? x : min.getX()),
									(y < min.getY() ? y : min.getY()),
									(z < min.getZ() ? z : min.getZ()));
							
							max.setPos(
									(x > max.getX() ? x : max.getX()),
									(y > max.getY() ? y : max.getY()),
									(z > max.getZ() ? z : max.getZ()));
							break;
						}
					}
				}
				
				if(perfect){
					// Multiblock Correctly Built
					matrix.push();
					{
						renderOutlineBox(matrix, min, max, 0x00BF00, flicker);
					}
					matrix.pop();
					
					// Debugging Stuff
					if(!player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem() == IPContent.debugItem){
						matrix.push();
						{
							// Min (Red)
							matrix.translate(min.getX(), min.getY(), min.getZ());
							renderCenteredOutlineBox(matrix, 0xFF0000, flicker);
						}
						matrix.pop();
						
						matrix.push();
						{
							// Max (Greem)
							matrix.translate(max.getX(), max.getY(), max.getZ());
							renderCenteredOutlineBox(matrix, 0x00FF00, flicker);
						}
						matrix.pop();
						
						matrix.push();
						{
							// Center (Blue)
							BlockPos center = min.toImmutable().add(max);
							matrix.translate(center.getX() / 2, center.getY() / 2, center.getZ() / 2);
							
							renderCenteredOutlineBox(matrix, 0x0000FF, flicker);
						}
						matrix.pop();
					}
				}
			}
		}
		
		private static void renderPhantom(MatrixStack matrix, World world, RenderInfo rInfo, boolean mirror, float flicker, float alpha, float partialTicks){
			renderPhantom(matrix, rInfo.multiblock, rInfo.templateWorld, world, rInfo.templatePos, rInfo.worldPos, rInfo.settings.getRotation(), mirror, flicker, alpha, partialTicks);
		}
		
		private static void renderPhantom(MatrixStack matrix, IMultiblock multiblock, World templateWorld, World world, BlockPos templatePos, BlockPos worldPos, Rotation rotation, boolean mirror, float flicker, float alpha, float partialTicks){
			BlockRendererDispatcher dispatcher = ClientUtils.mc().getBlockRendererDispatcher();
			BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
			BlockColors blockColors = ClientUtils.mc().getBlockColors();
			
			// Centers the preview block
			matrix.translate(worldPos.getX(), worldPos.getY(), worldPos.getZ());
			
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			
			BlockState state = templateWorld.getBlockState(templatePos);
			state = state.rotate(world, worldPos, rotation);
			
			ProjectorEvent.RenderBlock renderEvent = new ProjectorEvent.RenderBlock(multiblock, templateWorld, templatePos, world, worldPos, state, rotation);
			if(!MinecraftForge.EVENT_BUS.post(renderEvent)){
				state = renderEvent.getState();
				
				IModelData modelData = EmptyModelData.INSTANCE;
				TileEntity te = templateWorld.getTileEntity(templatePos);
				if(te != null){
					te.cachedBlockState = state;
					modelData = te.getModelData();
				}
				
				BlockRenderType blockrendertype = state.getRenderType();
				if(blockrendertype != BlockRenderType.INVISIBLE){
					if(blockrendertype == BlockRenderType.MODEL){
						IBakedModel ibakedmodel = dispatcher.getModelForState(state);
						int i = blockColors.getColor(state, null, null, 0);
						float red = (i >> 16 & 0xFF) / 255F;
						float green = (i >> 8 & 0xFF) / 255F;
						float blue = (i & 0xFF) / 255F;
						blockRenderer.renderModel(matrix.getLast(), buffer.getBuffer(RenderType.getTranslucent()), state, ibakedmodel, red, green, blue, 0xF000F0, OverlayTexture.NO_OVERLAY, modelData);
						
					}else if(blockrendertype == BlockRenderType.ENTITYBLOCK_ANIMATED){
						ItemStack stack = new ItemStack(state.getBlock());
						stack.getItem().getItemStackTileEntityRenderer().func_239207_a_(stack, ItemCameraTransforms.TransformType.NONE, matrix, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY);
					}
				}
			}
			
			ShaderUtil.alpha_static(flicker * alpha, ClientUtils.mc().player.ticksExisted + partialTicks);
			buffer.finish();
			ShaderUtil.releaseShader();
		}
		
		private static void renderOutlineBox(MatrixStack matrix, Vector3i min, Vector3i max, int rgb, float flicker){
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			IVertexBuilder builder = buffer.getBuffer(IPRenderTypes.TRANSLUCENT_LINES);
			
			float alpha = 0.25F + (0.5F * flicker);
			
			float xMin = min.getX();
			float yMin = min.getY();
			float zMin = min.getZ();
			
			float xMax = max.getX() + 1F;
			float yMax = max.getY() + 1F;
			float zMax = max.getZ() + 1F;
			
			float r = ((rgb >> 16) & 0xFF) / 255F;
			float g = ((rgb >> 8) & 0xFF) / 255F;
			float b = ((rgb >> 0) & 0xFF) / 255F;
			
			// matrix.scale(xScale, yScale, zScale);
			Matrix4f mat = matrix.getLast().getMatrix();
			
			builder.pos(mat, xMin, yMax, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMax, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMax, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMax, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMax, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMin, yMax, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMin, yMax, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMin, yMax, zMin).color(r, g, b, alpha).endVertex();
			
			builder.pos(mat, xMin, yMax, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMin, yMin, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMax, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMin, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMin, yMax, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMin, yMin, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMax, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMin, zMax).color(r, g, b, alpha).endVertex();
			
			builder.pos(mat, xMin, yMin, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMin, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMin, zMin).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMin, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMax, yMin, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMin, yMin, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMin, yMin, zMax).color(r, g, b, alpha).endVertex();
			builder.pos(mat, xMin, yMin, zMin).color(r, g, b, alpha).endVertex();
			
			buffer.finish();
		}
		
		private static void renderCenteredOutlineBox(MatrixStack matrix, int rgb, float flicker){
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			IVertexBuilder builder = buffer.getBuffer(IPRenderTypes.TRANSLUCENT_LINES);
			
			matrix.translate(0.5, 0.5, 0.5);
			matrix.scale(1.01F, 1.01F, 1.01F);
			Matrix4f mat = matrix.getLast().getMatrix();
			
			float r = ((rgb >> 16) & 0xFF) / 255.0F;
			float g = ((rgb >> 8) & 0xFF) / 255.0F;
			float b = ((rgb >> 0) & 0xFF) / 255.0F;
			float alpha = .375F * flicker;
			float s = 0.5F;
			
			builder.pos(mat, -s, s, -s)	.color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, s, -s)	.color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, s, -s)	.color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, s,  s)	.color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, s,  s)	.color(r, g, b, alpha).endVertex();
			builder.pos(mat, -s, s,  s)	.color(r, g, b, alpha).endVertex();
			builder.pos(mat, -s, s,  s)	.color(r, g, b, alpha).endVertex();
			builder.pos(mat, -s, s, -s)	.color(r, g, b, alpha).endVertex();
			
			builder.pos(mat, -s,  s, -s).color(r, g, b, alpha).endVertex();
			builder.pos(mat, -s, -s, -s).color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s,  s, -s).color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, -s, -s).color(r, g, b, alpha).endVertex();
			builder.pos(mat, -s,  s,  s).color(r, g, b, alpha).endVertex();
			builder.pos(mat, -s, -s,  s).color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s,  s,  s).color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, -s,  s).color(r, g, b, alpha).endVertex();
			
			builder.pos(mat, -s, -s, -s).color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, -s, -s).color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, -s, -s).color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, -s,  s).color(r, g, b, alpha).endVertex();
			builder.pos(mat,  s, -s,  s).color(r, g, b, alpha).endVertex();
			builder.pos(mat, -s, -s,  s).color(r, g, b, alpha).endVertex();
			builder.pos(mat, -s, -s,  s).color(r, g, b, alpha).endVertex();
			builder.pos(mat, -s, -s, -s).color(r, g, b, alpha).endVertex();
			
			buffer.finish();
		}
	}
	
	/** Client Input Stuff */
	@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT)
	public static class ClientInputHandler{
		static boolean shiftHeld = false;
		
		@SubscribeEvent
		public static void onPlayerTick(TickEvent.PlayerTickEvent event){
			if(event.side == LogicalSide.CLIENT && event.player != null && event.player == ClientUtils.mc().getRenderViewEntity()){
				if(event.phase == Phase.END){
					if(!ClientProxy.keybind_preview_flip.isInvalid() && ClientProxy.keybind_preview_flip.isPressed()){
						doAFlip();
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void handleScroll(InputEvent.MouseScrollEvent event){
			double delta = event.getScrollDelta();
			
			if(shiftHeld && delta != 0.0){
				PlayerEntity player = ClientUtils.mc().player;
				ItemStack mainItem = player.getHeldItemMainhand();
				ItemStack secondItem = player.getHeldItemOffhand();
				
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == Items.projector && ItemNBTHelper.hasKey(mainItem, "settings", NBT.TAG_COMPOUND);
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == Items.projector && ItemNBTHelper.hasKey(secondItem, "settings", NBT.TAG_COMPOUND);
				
				if(main || off){
					ItemStack target = main ? mainItem : secondItem;
					
					if(shiftHeld){
						Settings settings = getSettings(target);
						
						if(delta > 0){
							settings.rotateCCW();
						}else{
							settings.rotateCW();
						}
						
						settings.applyTo(target);
						settings.sendPacketToServer(main ? Hand.MAIN_HAND : Hand.OFF_HAND);
						
						Direction facing = Direction.byHorizontalIndex(settings.getRotation().ordinal());
						player.sendStatusMessage(new TranslationTextComponent("desc.immersivepetroleum.info.projector.rotated." + facing), true);
						
						event.setCanceled(true);
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void handleKey(InputEvent.KeyInputEvent event){
			if(event.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT || event.getKey() == GLFW.GLFW_KEY_LEFT_SHIFT){
				switch(event.getAction()){
					case GLFW.GLFW_PRESS:{
						shiftHeld = true;
						return;
					}
					case GLFW.GLFW_RELEASE:{
						shiftHeld = false;
						return;
					}
				}
			}
		}
		
		private static void doAFlip(){
			PlayerEntity player = ClientUtils.mc().player;
			ItemStack mainItem = player.getHeldItemMainhand();
			ItemStack secondItem = player.getHeldItemOffhand();
			
			boolean main = !mainItem.isEmpty() && mainItem.getItem() == Items.projector && ItemNBTHelper.hasKey(mainItem, "settings", NBT.TAG_COMPOUND);
			boolean off = !secondItem.isEmpty() && secondItem.getItem() == Items.projector && ItemNBTHelper.hasKey(secondItem, "settings", NBT.TAG_COMPOUND);
			ItemStack target = main ? mainItem : secondItem;
			
			if(main || off){
				Settings settings = ProjectorItem.getSettings(target);
				
				settings.flip();
				settings.applyTo(target);
				settings.sendPacketToServer(main ? Hand.MAIN_HAND : Hand.OFF_HAND);
				
				ITextComponent flip;
				if(settings.isMirrored()){
					flip = new TranslationTextComponent("desc.immersivepetroleum.info.projector.flipped.true");
				}else{
					flip = new TranslationTextComponent("desc.immersivepetroleum.info.projector.flipped.false");
				}
				player.sendStatusMessage(flip, true);
			}
		}
	}
	
	private static class RenderInfo{
		public final Layer layer;
		public final IMultiblock multiblock;
		public final World templateWorld;
		public final BlockPos templatePos;
		public final BlockPos worldPos;
		public final PlacementSettings settings;
		
		public RenderInfo(Layer layer, MultiblockProjection.Info info){
			this.layer = layer;
			this.multiblock = info.multiblock;
			this.templateWorld = info.templateWorld;
			this.templatePos = info.templatePos;
			this.settings = info.settings;
			this.worldPos = info.tPos;
		}
		
		public BlockState getState(){
			return this.templateWorld.getBlockState(this.templatePos);
		}
		
		public static enum Layer{
			ALL, BAD, PERFECT;
		}
	}
}
