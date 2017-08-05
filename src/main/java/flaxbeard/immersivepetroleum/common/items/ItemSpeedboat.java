package flaxbeard.immersivepetroleum.common.items;



import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.common.entity.EntitySpeedboat;

public class ItemSpeedboat extends ItemIPUpgradableTool
{
	public ItemSpeedboat(String name)
	{
		super(name, 1, "BOAT", new String[0]);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		float f = 1.0F;
		float f1 = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch) * 1.0F;
		float f2 = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw) * 1.0F;
		double d0 = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX) * 1.0D;
		double d1 = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) * 1.0D + (double)playerIn.getEyeHeight();
		double d2 = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ) * 1.0D;
		Vec3d vec3d = new Vec3d(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 5.0D;
		Vec3d vec3d1 = vec3d.addVector((double)f7 * 5.0D, (double)f6 * 5.0D, (double)f8 * 5.0D);
		RayTraceResult raytraceresult = worldIn.rayTraceBlocks(vec3d, vec3d1, true);

		if (raytraceresult == null)
		{
			return new ActionResult(EnumActionResult.PASS, itemstack);
		}
		else
		{
			Vec3d vec3d2 = playerIn.getLook(1.0F);
			boolean flag = false;
			List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(playerIn, playerIn.getEntityBoundingBox().expand(vec3d2.x * 5.0D, vec3d2.y * 5.0D, vec3d2.z * 5.0D).grow(1.0D));

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity = (Entity)list.get(i);

				if (entity.canBeCollidedWith())
				{
					AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow((double)entity.getCollisionBorderSize());

					if (axisalignedbb.contains(vec3d))
					{
						flag = true;
					}
				}
			}

			if (flag)
			{
				return new ActionResult(EnumActionResult.PASS, itemstack);
			}
			else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK)
			{
				return new ActionResult(EnumActionResult.PASS, itemstack);
			}
			else
			{
				Block block = worldIn.getBlockState(raytraceresult.getBlockPos()).getBlock();
				boolean flag1 = block == Blocks.WATER || block == Blocks.FLOWING_WATER;
				EntitySpeedboat entityboat = new EntitySpeedboat(worldIn, raytraceresult.hitVec.x, flag1 ? raytraceresult.hitVec.y - 0.12D : raytraceresult.hitVec.y, raytraceresult.hitVec.z);
				entityboat.rotationYaw = playerIn.rotationYaw;
				entityboat.setUpgrades(this.getContainedItems(itemstack));
				entityboat.readTank(itemstack.getTagCompound());

				if (!worldIn.getCollisionBoxes(entityboat, entityboat.getEntityBoundingBox().grow(-0.1D)).isEmpty())
				{
					return new ActionResult(EnumActionResult.FAIL, itemstack);
				}
				else
				{
					if (!worldIn.isRemote)
					{
						worldIn.spawnEntity(entityboat);
					}

					if (!playerIn.capabilities.isCreativeMode)
					{
						itemstack.shrink(1);
					}

					playerIn.addStat(StatList.getObjectUseStats(this));
					return new ActionResult(EnumActionResult.SUCCESS, itemstack);
				}
			}
		}
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem)
	{
		return new Slot[]
				{
					new IESlot.Upgrades(container, invItem, 0,  78, 35 - 5, "BOAT", stack, true),
					new IESlot.Upgrades(container, invItem, 1,  98, 35 + 5, "BOAT", stack, true),
					new IESlot.Upgrades(container, invItem, 2, 118, 35 - 5, "BOAT", stack, true)
				};
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 4;
	}
	

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advInfo)
	{
		if (ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if (fs!=null)
				list.add(fs.getLocalizedName()+": "+fs.amount+"mB");
		}
	}

}
