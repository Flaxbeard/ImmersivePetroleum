package flaxbeard.immersivepetroleum.api.crafting;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityAutoLubricator;

public class LubricatedHandler
{
	public static interface ILubricationHandler<E extends TileEntity>
	{
		public TileEntity isPlacedCorrectly(World world, TileEntityAutoLubricator tile, EnumFacing facing);
		public boolean isMachineEnabled(World world, TileEntityAutoLubricator tile, EnumFacing facing, E master);
		public void lubricate(World world, TileEntityAutoLubricator tile, EnumFacing facing, int ticks, E master);
		public void spawnLubricantParticles(World world, TileEntityAutoLubricator tile, EnumFacing facing, E master);
		@SideOnly(Side.CLIENT)
		public void renderPipes(World world, TileEntityAutoLubricator tile, EnumFacing facing, E master);
		
		public Tuple<BlockPos, EnumFacing> getGhostBlockPosition(World world, E tile);
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
	
}
