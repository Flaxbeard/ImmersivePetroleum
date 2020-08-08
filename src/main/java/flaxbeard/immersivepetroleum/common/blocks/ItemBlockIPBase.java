package flaxbeard.immersivepetroleum.common.blocks;

import java.util.List;
import java.util.Locale;

import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

public class ItemBlockIPBase extends BlockItem
{
	public ItemBlockIPBase(Block b)
	{
		super(b);
		if (((BlockIPBase) b).enumValues.length > 1)
			setHasSubtypes(true);
	}


	@Override
	public int getMetadata(int damageValue)
	{
		return damageValue;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> itemList)
	{
		if (this.isInCreativeTab(tab))
			this.block.getSubBlocks(tab, itemList);
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return ((BlockIPBase) this.block).getUnlocalizedName(stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		if(((BlockIPBase) block).hasFlavour(stack)){
			String subName = ((BlockIPBase) this.block).getStateFromMeta(stack.getItemDamage()).getValue(((BlockIPBase) this.block).property).toString().toLowerCase(Locale.US);
			String flavourKey = "desc." + ImmersivePetroleum.MODID + ".flavor." + ((BlockIPBase) this.block).name + "." + subName;
			tooltip.add(new StringTextComponent(TextFormatting.GRAY.toString() + I18n.format(flavourKey)));
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			tooltip.add(new TranslationTextComponent("desc.immersiveengineering.info.energyStored", ItemNBTHelper.getInt(stack, "energyStorage")));
		
		if(ItemNBTHelper.hasKey(stack, "tank")){
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs != null)
				tooltip.add(new StringTextComponent(fs.getDisplayName() + ": " + fs.getAmount() + "mB"));
		}
	}
	
	@Override
	protected boolean placeBlock(BlockItemUseContext context, BlockState state){
		
//		if (!((BlockIPBase) this.block).canIEBlockBePlaced(world, pos, newState, side, hitX, hitY, hitZ, player, stack))
//			return false;
//		boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
//		if (ret)
//		{
//			((BlockIPBase) this.block).onIEBlockPlacedBy(world, pos, newState, side, hitX, hitY, hitZ, player, stack);
//		}
//		return ret;
		
		return super.placeBlock(context, state);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		PlayerEntity player=context.getPlayer();
		Hand hand=context.getHand();
		World world=context.getWorld();
		BlockPos pos=context.getPos();
		Direction side=context.getFace();
		Vec3d hit=context.getHitVec();
		
		
		ItemStack stack = player.getHeldItem(hand);
		BlockState iblockstate = world.getBlockState(pos);
		Block block = iblockstate.getBlock();
		if (!block.isReplaceable(world, pos))
			pos = pos.offset(side);
		if (stack.getCount() > 0 && player.canPlayerEdit(pos, side, stack) && canBlockBePlaced(world, pos, side, stack))
		{
			int i = this.getMetadata(stack.getMetadata());
			BlockState iblockstate1 = this.block.getStateForPlacement(world, pos, side, hit.getX(), hit.getY(), hit.getZ(), i, player);
			if (placeBlockAt(stack, player, world, pos, side, hit.getX(), hit.getY(), hit.getZ(), iblockstate1))
			{
				SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
				world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				if (!player.capabilities.isCreativeMode)
					stack.shrink(1);
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.FAIL;
	}

	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, Direction side, PlayerEntity player, ItemStack stack)
	{
		Block block = worldIn.getBlockState(pos).getBlock();

		if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos))
		{
			side = Direction.UP;
		}
		else if (!block.isReplaceable(worldIn, pos))
		{
			pos = pos.offset(side);
		}

		return canBlockBePlaced(worldIn, pos, side, stack);
	}

	private boolean canBlockBePlaced(World w, BlockPos pos, Direction side, ItemStack stack)
	{
		BlockIPBase blockIn = (BlockIPBase) this.block;
		Block block = w.getBlockState(pos).getBlock();
		AxisAlignedBB axisalignedbb = blockIn.getCollisionBoundingBox(blockIn.getStateFromMeta(stack.getItemDamage()), w, pos);
		if (axisalignedbb != null && !w.checkNoEntityCollision(axisalignedbb.offset(pos), null)) return false;
		return block.isReplaceable(w, pos) && blockIn.canPlaceBlockOnSide(w, pos, side);
	}
}