package flaxbeard.immersivepetroleum.common.items;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableInt;
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
import blusunrize.immersiveengineering.common.blocks.metal.FluidPumpTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.api.event.SchematicPlaceBlockEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicPlaceBlockPostEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicRenderBlockEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicTestEvent;
import flaxbeard.immersivepetroleum.client.ShaderUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Items;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.MessageRotateSchematic;
import net.minecraft.block.Block;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
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

public class ProjectorItem extends IPItemBase{
	public ProjectorItem(String name){
		super(name, new Item.Properties().maxStackSize(1));
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	/** Like {@link ProjectorItem#getMultiblock(ResourceLocation)} but using the ItemStack directly */
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
	 * @return the stack
	 */
	protected static ItemStack putMultiblockIdentifier(ItemStack stack, IMultiblock multiblock){
		ItemNBTHelper.putString(stack, "multiblock", 
				multiblock
				.getUniqueName()
				.toString());
		return stack; // For convenience
	}
	
	/**
	 * 
	 * @param multiblock the current multiblock to be worked on
	 * @param rotation the rotation to apply to the multiblocks template
	 * @param flip wether or not to mirror the given multiblocks template
	 * @param consumer what to do with a template location. (Returning true inside this predicate causes the internal loop to stop)
	 * @return amount of blocks in the template
	 */
	private static int processMultiblock(IMultiblock multiblock, Rotation rotation, boolean flip, Predicate<TestClass> consumer){
		if(multiblock==null)
			return 0;
		
		Vec3i size=multiblock.getSize();
		int mWidth = size.getX();
		int mDepth = size.getZ();
		
		// Determine if the dimensions are even (true) or odd (false)
		boolean evenWidth=((mWidth/2F)-(mWidth/2))==0F; // Divide with float, Divide with int then subtract both and check for 0
		boolean evenDepth=((mDepth/2F)-(mDepth/2))==0F;
		
		// Take even/odd-ness of multiblocks into consideration for rotation
		int xa=evenWidth?1:0;
		int za=evenDepth?1:0;
		int xai=evenWidth?0:1; // xa-"Inverted"
		
		PlacementSettings setting=new PlacementSettings();
		setting.setMirror(flip?Mirror.FRONT_BACK:Mirror.NONE);
		setting.setRotation(rotation);
		
		BlockPos offset;
		if(flip){
			offset=new BlockPos(-mWidth/2, 0, mDepth/2);
			setting.setCenterOffset(offset);
			
			switch(rotation){
				case NONE:{
					offset=offset.add(xa, 0, 0);
					break;
				}
				case CLOCKWISE_90:{
					offset=offset.add(xa, 0, za-xai);
					break;
				}
				case CLOCKWISE_180:{
					offset=offset.add(0, 0, za);
					break;
				}
				case COUNTERCLOCKWISE_90:{
					offset=offset.add(0, 0, xai);
					break;
				}
				default: break;
			}
		}else{
			offset=new BlockPos(mWidth/2, 0, mDepth/2);
			setting.setCenterOffset(offset);
			
			switch(rotation){
				case CLOCKWISE_90:{
					offset=offset.add(xa, 0, 0);
					break;
				}
				case CLOCKWISE_180:{
					offset=offset.add(xa, 0, za);
					break;
				}
				case COUNTERCLOCKWISE_90:{
					offset=offset.add(0, 0, za);
					break;
				}
				default: break;
			}
		}
		
		Template multiblockTemplate=getMultiblockTemplate(multiblock);
		List<Template.BlockInfo> blocks=multiblockTemplate.blocks.get(0);
		for(int i=0;i<blocks.size();i++){
			Template.BlockInfo info=blocks.get(i);
			BlockPos transformedPos=Template.transformedBlockPos(setting, info.pos).subtract(offset);
			
			if(consumer.test(new TestClass(setting, info, multiblock, transformedPos)))
				break;
		}
		
		return blocks.size();
	}
	
	private static BlockPos alignHit(BlockPos hit, PlayerEntity playerIn, Rotation rotation, Vec3i multiblockSize, boolean flip){
		int xd = (rotation.ordinal() % 2 == 0) ? multiblockSize.getX() : multiblockSize.getZ();
		int zd = (rotation.ordinal() % 2 == 0) ? multiblockSize.getZ() : multiblockSize.getX();
		
		Direction look = playerIn.getHorizontalFacing();
		
//		if(look == Direction.NORTH || look == Direction.SOUTH){
//			hit = hit.add(-xd / 2, 0, 0);
//		}else if(look == Direction.EAST || look == Direction.WEST){
//			hit = hit.add(0, 0, -zd / 2);
//		}
//		
//		if(look == Direction.NORTH){
//			hit = hit.add(0, 0, -zd + 1);
//		}else if(look == Direction.WEST){
//			hit = hit.add(-xd + 1, 0, 0);
//		}
		
		return hit;
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
	
	/** Name cache for {@link ProjectorItem#getActualMBName(IMultiblock)} */
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
	private static Template getMultiblockTemplate(IMultiblock multiblock){
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
		
		IMultiblock multiblock = ProjectorItem.getMultiblock(stack);
		
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
			
			Rotation rotation = getRotation(stack);
			boolean flip = getFlipped(stack);
			
			hit=alignHit(hit, playerIn, rotation, size, flip);
			
			if(playerIn.isSneaking() && playerIn.isCreative()){
				if(multiblock.getUniqueName().getPath().contains("excavator_demo")){
					hit = hit.add(0, -2, 0);
				}
				
				final BlockPos hitCopy=new BlockPos(hit);
				processMultiblock(multiblock, rotation, flip, test->{
					// TODO Re-add the event stuff
					SchematicPlaceBlockEvent event=new SchematicPlaceBlockEvent(multiblock, world, test.tPos, test.getInfoPos(), test.getInfoState(), test.getInfoNBT(), rotation);
					if(!MinecraftForge.EVENT_BUS.post(event)){
						world.setBlockState(hitCopy.add(test.tPos), test.getInfoState());
						
						SchematicPlaceBlockPostEvent postevent=new SchematicPlaceBlockPostEvent(multiblock, world, test.tPos, test.getInfoPos(), test.getInfoState(), test.getInfoNBT(), rotation);
						//MinecraftForge.EVENT_BUS.post(postevent);
					}
					
					return false; // Don't ever skip a step.
				});
				
				return ActionResultType.SUCCESS;
				
			}else{
				CompoundNBT posTag = new CompoundNBT();
				posTag.putInt("x", hit.getX());
				posTag.putInt("y", hit.getY());
				posTag.putInt("z", hit.getZ());
				ItemNBTHelper.setTagCompound(stack, "pos", posTag);
				
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.PASS;
	}
	
	public static Rotation getRotation(ItemStack stack){
		if(ItemNBTHelper.hasKey(stack, "rotate")){
			int index = ItemNBTHelper.getInt(stack, "rotate") % 4;
			return Rotation.values()[index];
		}
		return Rotation.NONE;
	}
	
	public static boolean getFlipped(ItemStack stack){
		if(ItemNBTHelper.hasKey(stack, "flip")){
			return ItemNBTHelper.getBoolean(stack, "flip");
		}
		return false;
	}
	
	public static void rotateClient(ItemStack stack){
		int newRotate = (getRotation(stack).ordinal() + 1) % 4;
		boolean flip = getFlipped(stack);
		setRotate(stack, newRotate);
		IPPacketHandler.INSTANCE.sendToServer(new MessageRotateSchematic(newRotate, flip));
	}
	
	public static void flipClient(ItemStack stack){
		int newRotate = getRotation(stack).ordinal();
		boolean flip = !getFlipped(stack);
		setFlipped(stack, flip);
		IPPacketHandler.INSTANCE.sendToServer(new MessageRotateSchematic(newRotate, flip));
	}
	
	public static void setRotate(ItemStack stack, Rotation rotation){
		ItemNBTHelper.putInt(stack, "rotate", rotation.ordinal());
	}
	
	public static void setRotate(ItemStack stack, int rotation){
		ItemNBTHelper.putInt(stack, "rotate", rotation);
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
			
			boolean main = !mainItem.isEmpty() && mainItem.getItem() == Items.itemProjector && ItemNBTHelper.hasKey(mainItem, "multiblock");
			boolean off = !secondItem.isEmpty() && secondItem.getItem() == Items.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");
			ItemStack target = main ? mainItem : secondItem;
			
			if(main || off){
				if(delta<0){
					ProjectorItem.flipClient(target);
					
					boolean flipped=ProjectorItem.getFlipped(target);
					String yesno=flipped?I18n.format("chat.immersivepetroleum.info.projector.flipped.yes"):I18n.format("chat.immersivepetroleum.info.projector.flipped.no");
					player.sendStatusMessage(new TranslationTextComponent("chat.immersivepetroleum.info.projector.flipped", yesno), true);
				}else if(delta>0){
					ProjectorItem.rotateClient(target);
					Rotation rot=ProjectorItem.getRotation(target);
					player.sendStatusMessage(new StringTextComponent("Rotated "+rot), true); // TODO Translation
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
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void renderLast(RenderWorldLastEvent event){
		Minecraft mc = ClientUtils.mc();
		
		GlStateManager.pushMatrix();
		{
			if(mc.player != null){ // TODO Bookmark: renderSchematic
				ItemStack secondItem = mc.player.getHeldItemOffhand();
				
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == Items.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");
				
				for(int i = 0;i <= 10;i++){
					ItemStack stack = (i == 10 ? secondItem : mc.player.inventory.getStackInSlot(i));
					if(!stack.isEmpty() && stack.getItem() == Items.itemProjector && ItemNBTHelper.hasKey(stack, "multiblock")){
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
		
		GlStateManager.translated(wPos.getX()+.5, wPos.getY()+.5, wPos.getZ()+.5); // Centers the preview block
		
		ShaderUtil.alpha_static(flicker * alpha, ClientUtils.mc().player.ticksExisted + partialTicks);
		SchematicRenderBlockEvent renderEvent = new SchematicRenderBlockEvent(multiblock, world, wPos, info.pos, info.state, info.nbt, rotation);
		if(!MinecraftForge.EVENT_BUS.post(renderEvent)){
			ItemStack toRender = new ItemStack(renderEvent.getState().getBlock());
			itemRenderer.renderItem(toRender, itemRenderer.getModelWithOverrides(toRender));
		}
		ShaderUtil.releaseShader();
	}
	
	@OnlyIn(Dist.CLIENT)
	private void renderPerfectBox(Vec3i mSize, Rotation rotation){
		int mWidth = mSize.getX();
		int mHeight = mSize.getY();
		int mDepth = mSize.getZ();
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		int xd = (rotation.ordinal() % 2 == 0) ? mWidth : mDepth;
		int zd = (rotation.ordinal() % 2 == 0) ? mDepth : mWidth;
		
		GlStateManager.disableDepthTest();
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
	
	@OnlyIn(Dist.CLIENT)
	public void renderSchematic(ItemStack target, PlayerEntity player, World world, float partialTicks, boolean shouldRenderMoving){
		Minecraft mc = ClientUtils.mc();
		IMultiblock multiblock = ProjectorItem.getMultiblock(target);
		if(multiblock==null)
			return;
		
		BlockPos hit = null;
		Vec3i size=multiblock.getSize();
		Rotation rotation = ProjectorItem.getRotation(target);
		boolean flip = ProjectorItem.getFlipped(target);
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
			
			hit=alignHit(hit, mc.player, rotation, size, flip);
		}
		
		if(hit != null){
			if(multiblock.getUniqueName().getPath().contains("excavator_demo")){
				hit = hit.add(0, -2, 0);
			}

			final boolean placedCopy=isPlaced;
			final List<RenderInfo> toRender=new ArrayList<>();
			final MutableInt currentSlice=new MutableInt();
			final MutableInt badBlocks=new MutableInt();
			final MutableInt goodBlocks=new MutableInt();
			final BlockPos hitCopy=new BlockPos(hit);
			int blockListSize=processMultiblock(multiblock, rotation, flip, test->{
				// Slice handling
				if(badBlocks.getValue()==0 && test.getInfoPos().getY()>currentSlice.getValue()){
					currentSlice.setValue(test.getInfoPos().getY());
				}else if(test.getInfoPos().getY()!=currentSlice.getValue()){
					return true; // breaks the internal loop
				}
				
				if(placedCopy){ // Render only slices when placed
					if(test.getInfoPos().getY()==currentSlice.getValue()){
						boolean skip=false;
						BlockState toCompare=world.getBlockState(hitCopy.add(test.tPos));
						if(test.getInfoState().getBlock()==toCompare.getBlock()){
							goodBlocks.increment();
							skip=true;
						}else{
							// Making it this far only needs an air check, the other already proved to be false.
							if(toCompare!=Blocks.AIR.getDefaultState()){
								toRender.add(new RenderInfo(1, test.info, test.setting, test.tPos));
								skip=true;
							}
							badBlocks.increment();
						}
						
						if(!skip){
							toRender.add(new RenderInfo(0, test.info, test.setting, test.tPos));
						}
					}
				}else{ // Render all when not placed
					toRender.add(new RenderInfo(0, test.info, test.setting, test.tPos));
				}
				
				return false;
			});
			
			boolean perfect=(goodBlocks.getValue()==blockListSize);
			
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
			ItemStack heldStack = player.getHeldItemMainhand();
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
					renderPerfectBox(size, rotation);
				}
				GlStateManager.popMatrix();
			}
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void handlePumpPlace(SchematicPlaceBlockPostEvent event){
		IMultiblock mb = event.getMultiblock();
		
		BlockState state = event.getState();
		
		if(state.getBlock() == IEBlocks.MetalDevices.fluidPump){
			FluidPumpTileEntity pump = (FluidPumpTileEntity) event.getWorld().getTileEntity(event.getWorldPos());
			//pump.placeDummies(event.getWorldPos(), state, Direction.UP, 0, 0, 0); // TODO place pump dummy
			event.getWorld().setBlockState(event.getWorldPos(), event.getState());
		}
	}

	@SubscribeEvent
	public void handleConveyorPlace(SchematicPlaceBlockPostEvent event){
		IMultiblock mb = event.getMultiblock();
		BlockState state = event.getState();
		String mbName = mb.getUniqueName().getPath();
		
		if(mbName.equals("multiblocks/auto_workbench") || mbName.equals("multiblocks/bottling_machine") || mbName.equals("multiblocks/assembler") || mbName.equals("multiblocks/metal_press")){
			if(state.getBlock() == IEBlocks.MetalDevices.CONVEYORS.get(null)){
				TileEntity te = event.getWorld().getTileEntity(event.getWorldPos());
				if(te instanceof ConveyorBeltTileEntity){
					ConveyorBeltTileEntity conveyor = ((ConveyorBeltTileEntity) te);
					ResourceLocation rl = new ResourceLocation(ImmersiveEngineering.MODID, "conveyor");
					IConveyorBelt subType = ConveyorHandler.getConveyor(rl, conveyor);
					
					Direction facing = ((ConveyorBeltTileEntity) te).getFacing();
					
					if((mbName.equals("multiblocks/auto_workbench") && (event.getMultiblock().getSize().getX() != 1 || event.getMultiblock().getSize().getZ() != 2)) || mbName.equals("multiblocks/bottling_machine")){
						conveyor.setFacing(event.getRotate().rotateY());
					}else if(mbName.equals("multiblocks/auto_workbench") && (event.getRotate() == Direction.WEST || event.getRotate() == Direction.EAST)){
						conveyor.setFacing(event.getRotate().getOpposite());
					}else{
						conveyor.setFacing(event.getRotate());
					}
					
					event.getWorld().setBlockState(event.getWorldPos(), event.getState());
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
	public void handleConveyorTest(SchematicTestEvent event){
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
	
	@Deprecated // Not used anywhere
	public boolean doesIntersect(PlayerEntity player, ItemStack target, BlockPos check){
		IMultiblock mb = ProjectorItem.getMultiblock(target);
		
		if(mb != null){
			int mh = mb.getSize().getY();
			int ml = mb.getSize().getX();
			int mw = mb.getSize().getZ();
			
			Rotation rotation = ProjectorItem.getRotation(target);
			boolean flip = ProjectorItem.getFlipped(target);
			
			int xd = (rotation.ordinal() % 2 == 0) ? ml : mw;
			int zd = (rotation.ordinal() % 2 == 0) ? mw : ml;
			
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
		
		Block block=event.getBlock();
		Rotation rotate = event.getRotate();
		if(block == IEBlocks.MetalDevices.fluidPipe){
			event.setState(IPContent.Blocks.dummyBlockPipe.getDefaultState());
			
		}else if(block == MetalDevices.CONVEYORS.get(BasicConveyor.NAME)){
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
			
		}else if(block == IEBlocks.MetalDevices.fluidPump){
			GlStateManager.translated(0, .225F, 0);
			GlStateManager.scaled(.9F, .9F, .9F);
			
		}else if(block == Blocks.PISTON){
			GlStateManager.rotated(180, 1, 0, 0);
		}
		
		if(mbName.equals("multiblocks/distillationtower")){
			if(block == IEBlocks.toSlab.get(IEBlocks.MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))){
				GlStateManager.translated(0, .25F, 0);
			}
		}
	}
	
	
	/** This is pretty much a carrier class */
	protected static class TestClass{
		/** raw information from the template */
		public final Template.BlockInfo info;
		
		/** Transformed Position */
		public final BlockPos tPos;
		
		/** Currently applied template transformation (tPos) */
		public final PlacementSettings setting;
		
		/** the multiblock in question */
		public final IMultiblock multiblock;
		
		public TestClass(PlacementSettings setting, Template.BlockInfo info, IMultiblock multiblock, BlockPos transformedPos){
			this.info=info;
			this.tPos=transformedPos;
			this.setting=setting;
			this.multiblock=multiblock;
		}
		
		public Rotation getRotation(){
			return this.setting.getRotation();
		}
		
		public CompoundNBT getInfoNBT(){
			return this.info.nbt;
		}
		
		public BlockPos getInfoPos(){
			return this.info.pos;
		}
		
		public BlockState getInfoState(){
			return this.info.state;
		}
	}
	
	private static class RenderInfo{
		/** 0 = All, 1 = Bad-Block */
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
