package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.gui.GuiDistillationTower;
import flaxbeard.immersivepetroleum.common.blocks.metal.TileEntityDistillationTower;
import flaxbeard.immersivepetroleum.common.gui.ContainerDistillationTower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nonnull;


public class CommonProxy implements IGuiHandler
{
	public void preInit()
	{
	}

	public void preInitEnd()
	{
	}

	public void init()
	{
	}

	public void postInit()
	{
	}

	public static <T extends TileEntity & IGuiTile> void openGuiForTile(@Nonnull EntityPlayer player, @Nonnull T tile)
	{
		player.openGui(ImmersivePetroleum.INSTANCE, tile.getGuiID(), tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if (te instanceof IGuiTile)
		{
			Object gui = null;
			if (ID == 0 && te instanceof TileEntityDistillationTower)
			{
				gui = new ContainerDistillationTower(player.inventory, (TileEntityDistillationTower) te);
			}

			if (gui != null)
				((IGuiTile) te).onGuiOpened(player, false);
			return gui;
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if (te instanceof IGuiTile)
		{
			Object gui = null;
			if (ID == 0 && te instanceof TileEntityDistillationTower)
			{
				gui = new GuiDistillationTower(player.inventory, (TileEntityDistillationTower) te);
			}

			return gui;
		}
		return null;
	}

	public void renderTile(TileEntity te)
	{
	}

	public void handleEntitySound(SoundEvent soundEvent, Entity e, boolean active, float volume, float pitch)
	{
	}

	public void drawUpperHalfSlab(ItemStack stack)
	{
	}
}