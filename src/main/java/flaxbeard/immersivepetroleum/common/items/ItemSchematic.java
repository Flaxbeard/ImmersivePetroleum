package flaxbeard.immersivepetroleum.common.items;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavatorDemo;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;

import com.mojang.realmsclient.gui.ChatFormatting;

public class ItemSchematic extends ItemIPBase
{
	public ItemSchematic(String name)
	{
		super(name, 1, new String[0]);
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
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
				
				tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("chat.immersivepetroleum.info.schematic.build", I18n.format("desc.immersiveengineering.info.multiblock." + multiblock)));
				
				int h = mb.getStructureManual().length;
				int l = mb.getStructureManual()[0].length;
				int w = mb.getStructureManual()[0][0].length;

				tooltip.add(ChatFormatting.DARK_GRAY + (l + " x " + h + " x " + w));
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
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		List<IMultiblock> multiblocks = MultiblockHandler.getMultiblocks();
		for (IMultiblock multiblock : multiblocks)
		{
			String str = multiblock.getUniqueName();
			if (str.equals("IE:BucketWheel") || str.equals("IE:Excavator")) continue;
			ItemStack stack = new ItemStack(item, 1, 0);
			ItemNBTHelper.setString(stack, "multiblock", multiblock.getUniqueName());
			list.add(stack);
		}
		ItemStack stack = new ItemStack(item, 1, 0);
		ItemNBTHelper.setString(stack, "multiblock", MultiblockExcavatorDemo.instance.getUniqueName());
		list.add(stack);
	}
	
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (ItemNBTHelper.hasKey(stack, "pos") && playerIn.isSneaking())
		{
			ItemNBTHelper.remove(stack, "pos");
			return EnumActionResult.SUCCESS;
		}
		
		IMultiblock mb = ItemSchematic.getMultiblock(ItemNBTHelper.getString(stack, "multiblock"));
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
			
			int rotate = ((playerIn.ticksExisted / 10) % 4);
			
			int xd = (rotate % 2 == 0) ? ml :  mw;
			int zd = (rotate % 2 == 0) ? mw :  ml;
			
			Vec3d vec = playerIn.getLookVec();
			EnumFacing look = (Math.abs(vec.zCoord) > Math.abs(vec.xCoord)) ? (vec.zCoord > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH) : (vec.xCoord > 0 ? EnumFacing.EAST : EnumFacing.WEST);
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
			
			posTag.setInteger("x", hit.getX());
			posTag.setInteger("y", hit.getY());
			posTag.setInteger("z", hit.getZ());
			ItemNBTHelper.setTagCompound(stack, "pos", posTag);
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
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


}
