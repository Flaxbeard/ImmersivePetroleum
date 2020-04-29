package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class LubricatedHandler
{
	public interface ILubricationHandler<E extends TileEntity>
	{
		TileEntity isPlacedCorrectly(World world, TileEntityAutoLubricator tile, EnumFacing facing);

		boolean isMachineEnabled(World world, E master);

		void lubricate(World world, int ticks, E master);

		void spawnLubricantParticles(World world, TileEntityAutoLubricator tile, EnumFacing facing, E master);

		@SideOnly(Side.CLIENT)
		void renderPipes(World world, TileEntityAutoLubricator tile, EnumFacing facing, E master);

		Tuple<BlockPos, EnumFacing> getGhostBlockPosition(World world, E tile);

		int[] getStructureDimensions();
	}

	static final HashMap<Class<? extends TileEntity>, ILubricationHandler> lubricationHandlers = new HashMap<Class<? extends TileEntity>, ILubricationHandler>();

	public static void registerLubricatedTile(Class<? extends TileEntity> tileClass, ILubricationHandler handler)
	{
		lubricationHandlers.put(tileClass, handler);
	}

	public static ILubricationHandler getHandlerForTile(TileEntity tile)
	{
		if (tile == null) return null;

		for (Entry<Class<? extends TileEntity>, ILubricationHandler> e : lubricationHandlers.entrySet())
		{
			if (e.getKey().isInstance(tile))
			{
				return e.getValue();
			}
		}
		return null;
	}

	public static class LubricatedTileInfo
	{
		public BlockPos pos;
		public int ticks;
		public int world;

		public LubricatedTileInfo(int world, BlockPos pos, int ticks)
		{
			this.world = world;
			this.pos = pos;
			this.ticks = ticks;
		}

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("ticks", ticks);
			tag.setInteger("x", pos.getX());
			tag.setInteger("y", pos.getY());
			tag.setInteger("z", pos.getZ());
			tag.setInteger("world", world);

			return tag;
		}

		public static LubricatedTileInfo readFromNBT(NBTTagCompound tag)
		{
			int ticks = tag.getInteger("ticks");
			int x = tag.getInteger("x");
			int y = tag.getInteger("y");
			int z = tag.getInteger("z");
			int world = tag.getInteger("world");

			return new LubricatedTileInfo(world, new BlockPos(x, y, z), ticks);
		}
	}

	public static List<LubricatedTileInfo> lubricatedTiles = new ArrayList<LubricatedTileInfo>();

	public static boolean lubricateTile(TileEntity tile, int ticks)
	{
		return lubricateTile(tile, ticks, false, -1);
	}

	public static boolean lubricateTile(TileEntity tile, int ticks, boolean additive, int cap)
	{
		if (tile instanceof TileEntityMultiblockPart)
		{
			tile = ((TileEntityMultiblockPart) tile).master();
		}
		if (getHandlerForTile(tile) != null)
		{
			BlockPos pos = tile.getPos();
			for (int i = 0; i < lubricatedTiles.size(); i++)
			{
				LubricatedTileInfo info = lubricatedTiles.get(i);
				if (info.pos.equals(pos) && info.world == tile.getWorld().provider.getDimension())
				{
					if (info.ticks >= ticks)
					{
						if (additive)
						{
							if (cap == -1)
							{
								info.ticks += ticks;
							}
							else
							{
								info.ticks = Math.min(cap, info.ticks + ticks);
							}
							return true;
						}
						else
						{
							return false;
						}
					}

					info.ticks = ticks;
					return true;
				}
			}
			LubricatedTileInfo lti = new LubricatedTileInfo(tile.getWorld().provider.getDimension(), tile.getPos(), ticks);
			lubricatedTiles.add(lti);
			return true;
		}
		return false;
	}


	public static class LubricantEffect extends ChemthrowerEffect
	{
		@Override
		public void applyToEntity(EntityLivingBase target, EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			if (target instanceof EntityIronGolem)
			{
				if (LubricantHandler.isValidLube(fluid))
				{
					int amount = (Math.max(1, IEConfig.Tools.chemthrower_consumption / LubricantHandler.getLubeAmount(fluid)) * 4) / 3;

					PotionEffect activeSpeed = target.getActivePotionEffect(MobEffects.SPEED);
					int ticksSpeed = amount;
					if (activeSpeed != null && activeSpeed.getAmplifier() <= 1)
					{
						ticksSpeed = Math.min(activeSpeed.getDuration() + amount, 60 * 20);
					}

					PotionEffect activeStrength = target.getActivePotionEffect(MobEffects.STRENGTH);
					int ticksStrength = amount;
					if (activeStrength != null && activeStrength.getAmplifier() <= 1)
					{
						ticksStrength = Math.min(activeStrength.getDuration() + amount, 60 * 20);
					}

					target.addPotionEffect(new PotionEffect(MobEffects.SPEED, ticksSpeed, 1));
					target.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, ticksStrength, 1));
				}
			}

		}

		@Override
		public void applyToBlock(World worldObj, RayTraceResult mop, EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			if (LubricantHandler.isValidLube(fluid))
			{
				int amount = (Math.max(1, IEConfig.Tools.chemthrower_consumption / LubricantHandler.getLubeAmount(fluid)) * 2) / 3;
				LubricatedHandler.lubricateTile(worldObj.getTileEntity(mop.getBlockPos()), amount, true, 20 * 60);
			}
		}

	}
}
