package flaxbeard.immersivepetroleum.common.network;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.items.ItemProjector;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RotateSchematicPacket implements IMessage
{
	public RotateSchematicPacket()
	{
	}

	public int rotate;
	public boolean flip;

	public RotateSchematicPacket(int rotate, boolean flip)
	{
		this.rotate = rotate;
		this.flip = flip;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeByte(rotate);
		buf.writeBoolean(flip);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		rotate = buf.readByte();
		flip = buf.readBoolean();
	}

	public static class Handler implements IMessageHandler<RotateSchematicPacket, IMessage>
	{

		@Override
		public IMessage onMessage(RotateSchematicPacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(new DoSync(player, message.rotate, message.flip));

			return null;
		}

	}

	private static class DoSync implements Runnable
	{
		private EntityPlayer p;
		private boolean flip;
		private int rotate;

		public DoSync(EntityPlayer p, int rotate, boolean flip)
		{
			this.p = p;
			this.rotate = rotate;
			this.flip = flip;
		}


		@Override
		public void run()
		{
			if (p != null)
			{
				ItemStack mainItem = p.getHeldItemMainhand();
				ItemStack secondItem = p.getHeldItemOffhand();

				boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(mainItem, "multiblock");
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.itemProjector && ItemNBTHelper.hasKey(secondItem, "multiblock");

				ItemStack target = main ? mainItem : secondItem;

				if (main || off)
				{
					ItemProjector.setFlipped(target, flip);
					ItemProjector.setRotate(target, rotate);
				}
			}
		}


	}


}
