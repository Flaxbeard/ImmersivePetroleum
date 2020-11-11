package flaxbeard.immersivepetroleum.common.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBeltTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.event.SchematicPlaceBlockEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicPlaceBlockPostEvent;
import flaxbeard.immersivepetroleum.api.event.SchematicRenderBlockEvent;
import flaxbeard.immersivepetroleum.client.ClientProxy;
import flaxbeard.immersivepetroleum.client.ShaderUtil;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Items;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.MessageRotateSchematic;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
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
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID)
public class ProjectorItem extends IPItemBase{
	public ProjectorItem(String name){
		super(name, new Item.Properties().maxStackSize(1));
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
	private static int processMultiblock(IMultiblock multiblock, Rotation rotation, boolean flip, Predicate<BlockProcessInfo> consumer){
		if(multiblock==null)
			return 0;
		
		Vector3i size=multiblock.getSize();
		int mWidth = size.getX();
		int mDepth = size.getZ();
		
		// Determine if the dimensions are even (true) or odd (false)
		boolean evenWidth=((mWidth/2F)-(mWidth/2))==0F; // Divide with float, Divide with int then subtract both and check for 0
		boolean evenDepth=((mDepth/2F)-(mDepth/2))==0F;
		
		// Take even/odd-ness of multiblocks into consideration for rotation
		int xa=evenWidth?1:0;
		int za=evenDepth?1:0;
		
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
					offset=offset.add(xa, 0, za);
					break;
				}
				case CLOCKWISE_180:{
					offset=offset.add(0, 0, za);
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
		
		
		List<Template.BlockInfo> blocks=multiblock.getStructure().stream().sorted((a,b)->a.pos.compareTo(b.pos)).collect(Collectors.toList());
		for(int i=0;i<blocks.size();i++){
			Template.BlockInfo info=blocks.get(i);
			BlockPos transformedPos=Template.transformedBlockPos(setting, info.pos).subtract(offset);
			
			if(consumer.test(new BlockProcessInfo(setting, info, multiblock, transformedPos)))
				break;
		}
		
		return blocks.size();
	}
	
	private static BlockPos alignHit(BlockPos hit, PlayerEntity playerIn, Rotation rotation, Vector3i multiblockSize, boolean flip){
		int xd = (rotation.ordinal() % 2 == 0) ? multiblockSize.getX() : multiblockSize.getZ();
		int zd = (rotation.ordinal() % 2 == 0) ? multiblockSize.getZ() : multiblockSize.getX();
		
		Direction look = playerIn.getHorizontalFacing();
		
		if(multiblockSize.getZ() > 1 && (look == Direction.NORTH || look == Direction.SOUTH)){
			int a=zd/2;
			if(look == Direction.NORTH){
				a+=1;
			}
			hit = hit.add(0, 0, a);
		}else if(multiblockSize.getX() > 1 && (look == Direction.EAST || look == Direction.WEST)){
			int a=xd/2;
			if(look == Direction.WEST){
				a+=1;
			}
			hit = hit.add(a, 0, 0);
		}
		
		if(multiblockSize.getZ() > 1 && look == Direction.NORTH){
			hit = hit.add(0, 0, -zd);
		}else if(multiblockSize.getX() > 1 && look == Direction.WEST){
			hit = hit.add(-xd, 0, 0);
		}
		
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
				
				Vector3i size = mb.getSize();
				tooltip.add(new StringTextComponent(size.getX() + " x " + size.getY() + " x " + size.getZ()).mergeStyle(TextFormatting.DARK_GRAY));
				
				if(ItemNBTHelper.hasKey(stack, "pos")){
					CompoundNBT pos = ItemNBTHelper.getTagCompound(stack, "pos");
					int x = pos.getInt("x");
					int y = pos.getInt("y");
					int z = pos.getInt("z");
					tooltip.add(new TranslationTextComponent("chat.immersivepetroleum.info.schematic.center", x, y, z).mergeStyle(TextFormatting.DARK_GRAY));
				}
				
				String rotation=I18n.format("chat.immersivepetroleum.info.projector.rotated."+Direction.byHorizontalIndex(ProjectorItem.getRotation(stack).ordinal()));
				String flipped=I18n.format("chat.immersivepetroleum.info.projector.flipped."+(ProjectorItem.getFlipped(stack)?"yes":"no"));
				
				tooltip.add(new StringTextComponent(rotation).mergeStyle(TextFormatting.DARK_GRAY));
				tooltip.add(new StringTextComponent(I18n.format("chat.immersivepetroleum.info.projector.flipped", flipped)).mergeStyle(TextFormatting.DARK_GRAY));
				
				ITextComponent ctrl0=new TranslationTextComponent("chat.immersivepetroleum.info.schematic.controls1")
						.mergeStyle(TextFormatting.DARK_GRAY);
				
				ITextComponent ctrl1=new TranslationTextComponent("chat.immersivepetroleum.info.schematic.controls2",
						I18n.format(ClientProxy.keybind_preview_flip.getTranslationKey()))
						.mergeStyle(TextFormatting.DARK_GRAY);
				
				tooltip.add(ctrl0);
				tooltip.add(ctrl1);
				
				return;
			}
		}
		tooltip.add(new StringTextComponent(TextFormatting.DARK_GRAY + I18n.format("chat.immersivepetroleum.info.schematic.noMultiblock")));
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
			
			final Mutable hit=new Mutable(pos.getX(), pos.getY(), pos.getZ());
			if(!state.getMaterial().isReplaceable() && facing == Direction.UP){
				hit.setAndOffset(hit, 0, 1, 0);
			}
			
			Vector3i size=multiblock.getSize();
			int mHeight = size.getY();
			int mWidth = size.getX();
			int mDepth = size.getZ();
			
			Rotation rotation = getRotation(stack);
			boolean flip = getFlipped(stack);
			
			hit.setPos(alignHit(hit, playerIn, rotation, size, flip));
			
			if(playerIn.isSneaking() && playerIn.isCreative()){
				if(multiblock.getUniqueName().getPath().contains("excavator_demo") || multiblock.getUniqueName().getPath().contains("bucket_wheel")){
					hit.setAndOffset(hit, 0, -2, 0);
				}
				
				processMultiblock(multiblock, rotation, flip, con->{
					SchematicPlaceBlockEvent event=new SchematicPlaceBlockEvent(multiblock, world, con.tPos, con.getInfoPos(), con.getInfoState(), con.getInfoNBT(), rotation);
					if(!MinecraftForge.EVENT_BUS.post(event)){
						world.setBlockState(con.tPos.add(hit), event.getState());
						
						SchematicPlaceBlockPostEvent postevent=new SchematicPlaceBlockPostEvent(multiblock, world, con.tPos, con.getInfoPos(), event.getState(), con.getInfoNBT(), rotation);
						MinecraftForge.EVENT_BUS.post(postevent);
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
			
			if(index<0 || index>=Rotation.values().length)
				index=0; // Pure safety precaution
			
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
	
	public static void rotateClient(ItemStack stack, int direction){
		int newRotate = (getRotation(stack).ordinal() + direction) % 4;
		boolean flip = getFlipped(stack);
		
		while(newRotate < 0)
			newRotate += 4;
		
		setRotate(stack, newRotate);
		IPPacketHandler.sendToServer(new MessageRotateSchematic(newRotate, flip));
	}
	
	public static void flipClient(ItemStack stack){
		int newRotate = getRotation(stack).ordinal();
		boolean flip = !getFlipped(stack);
		setFlipped(stack, flip);
		IPPacketHandler.sendToServer(new MessageRotateSchematic(newRotate, flip));
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
			return ActionResult.resultSuccess(stack);
		}
		return ActionResult.resultSuccess(stack);
	}
	
	@SubscribeEvent
	public static void handleConveyorPlace(SchematicPlaceBlockPostEvent event){
		IMultiblock mb = event.getMultiblock();
		BlockState state = event.getState();
		String mbName = mb.getUniqueName().getPath();
		
		if(mbName.equals("multiblocks/auto_workbench") || mbName.equals("multiblocks/bottling_machine") || mbName.equals("multiblocks/assembler") || mbName.equals("multiblocks/metal_press")){
			if(state.getBlock() == IEBlocks.MetalDevices.CONVEYORS.get(BasicConveyor.NAME)){
				TileEntity te = event.getWorld().getTileEntity(event.getWorldPos());
				if(te instanceof ConveyorBeltTileEntity){
					
				}
			}
		}
	}
	
	// STATIC SUPPORT CLASSES
	
	/** Client Rendering Stuff */
	@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value=Dist.CLIENT)
	public static class ClientRenderHandler{
		@SubscribeEvent
		public static void renderLast(RenderWorldLastEvent event){
			Minecraft mc = ClientUtils.mc();
			
			if(mc.player != null){
				MatrixStack matrix = event.getMatrixStack();
				matrix.push();
				{
					ItemStack secondItem = mc.player.getHeldItemOffhand();
					boolean off = !secondItem.isEmpty() && secondItem.getItem() == Items.projector && ItemNBTHelper.hasKey(secondItem, "multiblock");
					
					// Anti-Jiggle when moving
					Vector3d renderView = ClientUtils.mc().gameRenderer.getActiveRenderInfo().getProjectedView();
					matrix.translate(-renderView.x, -renderView.y, -renderView.z);
					
					for(int i = 0;i <= 10;i++){
						ItemStack stack = (i == 10 ? secondItem : mc.player.inventory.getStackInSlot(i));
						if(!stack.isEmpty() && stack.getItem() == Items.projector && ItemNBTHelper.hasKey(stack, "multiblock")){
							matrix.push();
							{
								renderSchematic(matrix, stack, mc.player, mc.player.world, event.getPartialTicks(), i == mc.player.inventory.currentItem || (i == 10 && off));
							}
							matrix.pop();
						}
					}
				}
				matrix.pop();
			}
		}
		
		static final Mutable FULL_MAX=new Mutable(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		public static void renderSchematic(MatrixStack matrix, ItemStack target, PlayerEntity player, World world, float partialTicks, boolean shouldRenderMoving){
			IMultiblock multiblock = ProjectorItem.getMultiblock(target);
			if(multiblock==null)
				return;
			
			final Mutable hit = new Mutable(FULL_MAX.getX(), FULL_MAX.getY(), FULL_MAX.getZ());
			Vector3i size = multiblock.getSize();
			Rotation rotation = ProjectorItem.getRotation(target);
			boolean flip = ProjectorItem.getFlipped(target);
			boolean isPlaced = false;
			
			if(ItemNBTHelper.hasKey(target, "pos")){
				CompoundNBT pos = ItemNBTHelper.getTagCompound(target, "pos");
				int x = pos.getInt("x");
				int y = pos.getInt("y");
				int z = pos.getInt("z");
				hit.setPos(x, y, z);
				isPlaced = true;
			}else if(shouldRenderMoving && ClientUtils.mc().objectMouseOver != null && ClientUtils.mc().objectMouseOver.getType() == Type.BLOCK){
				BlockRayTraceResult blockRTResult=(BlockRayTraceResult)ClientUtils.mc().objectMouseOver;
				
				BlockPos pos = (BlockPos)blockRTResult.getPos();
				
				BlockState state = world.getBlockState(pos);
				if(state.getMaterial().isReplaceable() || blockRTResult.getFace() != Direction.UP){
					hit.setPos(pos);
				}else{
					hit.setAndOffset(pos, 0, 1, 0);
				}
				
				hit.setPos(alignHit(hit, ClientUtils.mc().player, rotation, size, flip));
			}
			
			if(!hit.equals(FULL_MAX)){
				if(multiblock.getUniqueName().getPath().contains("excavator_demo") || multiblock.getUniqueName().getPath().contains("bucket_wheel")){
					hit.setAndOffset(hit, 0, -2, 0);
				}
	
				final boolean placedCopy=isPlaced;
				final List<RenderInfo> toRender=new ArrayList<>();
				final MutableInt currentSlice=new MutableInt();
				final MutableInt badBlocks=new MutableInt();
				final MutableInt goodBlocks=new MutableInt();
				int blockListSize=processMultiblock(multiblock, rotation, flip, con->{
					// Slice handling
					if(badBlocks.getValue()==0 && con.getInfoPos().getY()>currentSlice.getValue()){
						currentSlice.setValue(con.getInfoPos().getY());
					}else if(con.getInfoPos().getY()!=currentSlice.getValue()){
						return true; // breaks the internal loop
					}
					
					if(placedCopy){ // Render only slices when placed
						if(con.getInfoPos().getY()==currentSlice.getValue()){
							boolean skip=false;
							BlockState toCompare=world.getBlockState(con.tPos.add(hit));
							if(con.getInfoState().getBlock()==toCompare.getBlock()){
								toRender.add(new RenderInfo(RenderInfo.Layer.PERFECT, con.info, con.setting, con.tPos));
								goodBlocks.increment();
								skip=true;
							}else{
								// Making it this far only needs an air check, the other already proved to be false.
								if(toCompare.getBlock()!=Blocks.AIR){
									toRender.add(new RenderInfo(RenderInfo.Layer.BAD, con.info, con.setting, con.tPos));
									skip=true;
								}
								badBlocks.increment();
							}
							
							if(!skip){
								toRender.add(new RenderInfo(RenderInfo.Layer.ALL, con.info, con.setting, con.tPos));
							}
						}
					}else{ // Render all when not placed
						toRender.add(new RenderInfo(RenderInfo.Layer.ALL, con.info, con.setting, con.tPos));
					}
					
					return false;
				});
				
				boolean perfect=(goodBlocks.getValue()==blockListSize);
				
				Mutable min=new Mutable(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
				Mutable max=new Mutable(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
				float flicker = (world.rand.nextInt(10) == 0) ? 0.75F : (world.rand.nextInt(20) == 0 ? 0.5F : 1F);
				matrix.translate(hit.getX(), hit.getY(), hit.getZ());
				

				toRender.sort((a,b)->{
					if(a.layer.ordinal()>b.layer.ordinal()){
						return 1;
					}else if(a.layer.ordinal()<b.layer.ordinal()){
						return -1;
					}
					return 0;
				});
				
				//ClientUtils.bindAtlas();
				ItemStack heldStack = player.getHeldItemMainhand();
				for(RenderInfo rInfo:toRender){
					switch(rInfo.layer){
						case ALL:{ // All / Slice
							Template.BlockInfo info=rInfo.blockInfo;
							float alpha = heldStack.getItem()==info.state.getBlock().asItem() ? 1.0F : .5F;
							
							matrix.push();
							{
								renderPhantom(matrix, multiblock, world, info, rInfo.worldPos, flicker, alpha, partialTicks, flip, rInfo.settings.getRotation());
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
							min.setPos(
									(rInfo.worldPos.getX() < min.getX() ? rInfo.worldPos.getX() : min.getX()),
									(rInfo.worldPos.getY() < min.getY() ? rInfo.worldPos.getY() : min.getY()),
									(rInfo.worldPos.getZ() < min.getZ() ? rInfo.worldPos.getZ() : min.getZ()));
							
							max.setPos(
									(rInfo.worldPos.getX() > max.getX() ? rInfo.worldPos.getX() : max.getX()),
									(rInfo.worldPos.getY() > max.getY() ? rInfo.worldPos.getY() : max.getY()),
									(rInfo.worldPos.getZ() > max.getZ() ? rInfo.worldPos.getZ() : max.getZ()));
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
					if(!player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem()==IPContent.debugItem){
						matrix.push();
						{
							// Min (Magenta/Purple)
							matrix.translate(min.getX(), min.getY(), min.getZ());
							renderCenteredOutlineBox(matrix, 0xFF0000, flicker);
						}
						matrix.pop();
						
						matrix.push();
						{
							// Max (Yellow)
							matrix.translate(max.getX(), max.getY(), max.getZ());
							renderCenteredOutlineBox(matrix, 0x00FF00, flicker);
						}
						matrix.pop();
						
						matrix.push();
						{
							// Center (White)
							BlockPos center=min.toImmutable().add(max);
							matrix.translate(center.getX()/2, center.getY()/2, center.getZ()/2);
							
							renderCenteredOutlineBox(matrix, 0x0000FF, flicker);
						}
						matrix.pop();
					}
				}
			}
		}
		
		private static void renderPhantom(MatrixStack matrix, IMultiblock multiblock, World world, Template.BlockInfo info, BlockPos wPos, float flicker, float alpha, float partialTicks, boolean flipXZ, Rotation rotation){
			BlockRendererDispatcher dispatcher=ClientUtils.mc().getBlockRendererDispatcher();
			BlockModelRenderer blockRenderer=dispatcher.getBlockModelRenderer();
			BlockColors blockColors=ClientUtils.mc().getBlockColors();
			
			matrix.translate(wPos.getX(), wPos.getY(), wPos.getZ()); // Centers the preview block
			IRenderTypeBuffer.Impl buffer=IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			
			SchematicRenderBlockEvent renderEvent = new SchematicRenderBlockEvent(multiblock, world, wPos, info.pos, info.state, info.nbt, rotation);
			if(!MinecraftForge.EVENT_BUS.post(renderEvent)){
				BlockState state=renderEvent.getState();
				
				//dispatcher.renderBlock(renderEvent.getState(), matrix, buffer, 0xF000F0, 0, EmptyModelData.INSTANCE);
				
				BlockRenderType blockrendertype = state.getRenderType();
				if(blockrendertype != BlockRenderType.INVISIBLE){
					if(blockrendertype==BlockRenderType.MODEL){
						IBakedModel ibakedmodel = dispatcher.getModelForState(state);
						int i = blockColors.getColor(state, (IBlockDisplayReader) null, (BlockPos) null, 0);
						float f = (float) (i >> 16 & 255) / 255.0F;
						float f1 = (float) (i >> 8 & 255) / 255.0F;
						float f2 = (float) (i & 255) / 255.0F;
						blockRenderer.renderModel(matrix.getLast(), buffer.getBuffer(RenderType.getTranslucent()), state, ibakedmodel, f, f1, f2, 0xF000F0, 0, EmptyModelData.INSTANCE);
//						blockRenderer.renderModel(matrix.getLast(), buffer.getBuffer(RenderTypeLookup.func_239220_a_(state, false)), state, ibakedmodel, f, f1, f2, 0xF000F0, 0, EmptyModelData.INSTANCE);
						
					}else if(blockrendertype==BlockRenderType.ENTITYBLOCK_ANIMATED){
						ItemStack stack = new ItemStack(state.getBlock());
						stack.getItem().getItemStackTileEntityRenderer().func_239207_a_(stack, ItemCameraTransforms.TransformType.NONE, matrix, buffer, 0xF000F0, 0);
					}
				}
			}
			
			ShaderUtil.alpha_static(flicker * alpha, ClientUtils.mc().player.ticksExisted + partialTicks);
			buffer.finish();
			ShaderUtil.releaseShader();
		}
		
		private static void renderOutlineBox(MatrixStack matrix, Vector3i min, Vector3i max, int rgb, float flicker){
			IRenderTypeBuffer.Impl buffer=IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			IVertexBuilder builder=buffer.getBuffer(IPRenderTypes.TRANSLUCENT_LINES);
			
			float alpha = 0.25F+(0.5F*flicker);
			
			float xMax=Math.abs(max.getX()-min.getX())+1;
			float yMax=Math.abs(max.getY()-min.getY())+1;
			float zMax=Math.abs(max.getZ()-min.getZ())+1;
			
			float r=((rgb>>16)&0xFF)/255.0F;
			float g=((rgb>>8)&0xFF)/255.0F;
			float b=((rgb>>0)&0xFF)/255.0F;
			
			matrix.translate(-1, 0, -1);
			matrix.scale(xMax, yMax, zMax);
			Matrix4f mat=matrix.getLast().getMatrix();
			
			builder.pos(mat, 0.0F, 1.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 1.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 1.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 1.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 1.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 0.0F, 1.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 0.0F, 1.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 0.0F, 1.0F, 0.0F).color(r, g, b, alpha).endVertex();
			
			builder.pos(mat, 0.0F, 1.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 0.0F, 0.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 1.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 0.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 0.0F, 1.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 0.0F, 0.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 1.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 0.0F, 1.0F).color(r, g, b, alpha).endVertex();
			
			builder.pos(mat, 0.0F, 0.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 0.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 0.0F, 0.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 0.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 1.0F, 0.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 0.0F, 0.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 0.0F, 0.0F, 1.0F).color(r, g, b, alpha).endVertex();
			builder.pos(mat, 0.0F, 0.0F, 0.0F).color(r, g, b, alpha).endVertex();
			
			buffer.finish();
		}
		
		private static void renderCenteredOutlineBox(MatrixStack matrix, int rgb, float flicker){
			IRenderTypeBuffer.Impl buffer=IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			IVertexBuilder builder=buffer.getBuffer(IPRenderTypes.TRANSLUCENT_LINES);
			
			matrix.translate(0.5, 0.5, 0.5);
			matrix.scale(1.01F, 1.01F, 1.01F);
			Matrix4f mat=matrix.getLast().getMatrix();
			
			float r=((rgb>>16)&0xFF)/255.0F;
			float g=((rgb>>8)&0xFF)/255.0F;
			float b=((rgb>>0)&0xFF)/255.0F;
			float alpha = .375F * flicker;
			float s=0.5F;
			
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
		
		@SubscribeEvent
		public static void handleConveyorsAndPipes(SchematicRenderBlockEvent event){
			BlockState state = event.getState();
			
			if(state.getBlock()==IEBlocks.MetalDevices.fluidPipe){
				event.setState(IPContent.Blocks.dummyPipe.getDefaultState());
			}else if(state.getBlock()==IEBlocks.MetalDevices.CONVEYORS.get(BasicConveyor.NAME)){
				//event.setState(IPContent.Blocks.dummyConveyor.getDefaultState());
			}
		}
	}
	
	/** Client Input Stuff */
	@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT)
	public static class ClientInputHandler{
		static boolean shiftHeld = false;
		@SubscribeEvent
		public static void handleScroll(InputEvent.MouseScrollEvent event){
			double delta = event.getScrollDelta();
			
			if(shiftHeld && delta != 0.0){
				PlayerEntity player = ClientUtils.mc().player;
				ItemStack mainItem = player.getHeldItemMainhand();
				ItemStack secondItem = player.getHeldItemOffhand();
				
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == Items.projector && ItemNBTHelper.hasKey(mainItem, "multiblock");
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == Items.projector && ItemNBTHelper.hasKey(secondItem, "multiblock");
				
				if(main || off){
					ItemStack target = main ? mainItem : secondItem;
					
					ProjectorItem.rotateClient(target, (int) delta);
					Rotation rot = ProjectorItem.getRotation(target);
					Direction facing=Direction.byHorizontalIndex(rot.ordinal());
					player.sendStatusMessage(new TranslationTextComponent("chat.immersivepetroleum.info.projector.rotated."+facing), true);
					event.setCanceled(true);
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
			
			if(shiftHeld){
				if(ClientProxy.keybind_preview_flip.isPressed()){
					doAFlip();
				}
			}
		}
		
		@SubscribeEvent
		public static void handleMouseInput(InputEvent.MouseInputEvent event){
			if(ClientProxy.keybind_preview_flip.isPressed()){
				doAFlip();
			}
		}
		
		private static void doAFlip(){
			PlayerEntity player = ClientUtils.mc().player;
			ItemStack mainItem = player.getHeldItemMainhand();
			ItemStack secondItem = player.getHeldItemOffhand();
			
			boolean main = !mainItem.isEmpty() && mainItem.getItem() == Items.projector && ItemNBTHelper.hasKey(mainItem, "multiblock");
			boolean off = !secondItem.isEmpty() && secondItem.getItem() == Items.projector && ItemNBTHelper.hasKey(secondItem, "multiblock");
			ItemStack target = main ? mainItem : secondItem;
			
			if(main || off){
				ProjectorItem.flipClient(target);
				
				boolean flipped = ProjectorItem.getFlipped(target);
				String yesno = flipped ? I18n.format("chat.immersivepetroleum.info.projector.flipped.yes") : I18n.format("chat.immersivepetroleum.info.projector.flipped.no");
				player.sendStatusMessage(new TranslationTextComponent("chat.immersivepetroleum.info.projector.flipped", yesno), true);
			}
		}
	}

	/** This is pretty much a carrier class */
	protected static class BlockProcessInfo{
		/** raw information from the template */
		public final Template.BlockInfo info;
		
		/** Transformed Position */
		public final BlockPos tPos;
		
		/** Currently applied template transformation (tPos) */
		public final PlacementSettings setting;
		
		/** the multiblock in question */
		public final IMultiblock multiblock;
		
		public BlockProcessInfo(PlacementSettings setting, Template.BlockInfo info, IMultiblock multiblock, BlockPos transformedPos){
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
		
		/**
		 * The raw, unaltered blockstate
		 * @return
		 */
		public BlockState getInfoState(){
			return this.info.state;
		}
		
		/**
		 * The state with rotations in mind
		 * @return
		 */
		public BlockState getState(){
			@SuppressWarnings("deprecation")
			BlockState rotated=this.info.state.rotate(getRotation());
			return rotated;
		}
	}
	
	private static class RenderInfo{
		public final Layer layer;
		public final Template.BlockInfo blockInfo;
		public final BlockPos worldPos;
		public final PlacementSettings settings;
		public RenderInfo(Layer layer, Template.BlockInfo blockInfo, PlacementSettings settings, BlockPos worldPos){
			this.layer=layer;
			this.blockInfo=blockInfo;
			this.worldPos=worldPos;
			this.settings=settings;
		}
		
		public static enum Layer{
			ALL,BAD,PERFECT;
		}
	}
}
