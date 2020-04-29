package flaxbeard.immersivepetroleum.common.network;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CloseBookPacket implements IMessage
{
	public CloseBookPacket()
	{
	}

	public String name;

	public CloseBookPacket(String name)
	{
		this.name = name;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeByte(name == null ? 0 : 1);
		if (name != null)
			ByteBufUtils.writeUTF8String(buf, name);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		boolean isStrNull = buf.readByte() == 0;
		if (!isStrNull)
		{
			this.name = ByteBufUtils.readUTF8String(buf);
		}
		else
		{
			this.name = null;
		}
	}

	public static class Handler implements IMessageHandler<CloseBookPacket, IMessage>
	{

		@Override
		public IMessage onMessage(CloseBookPacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(new DoSync(player, message.name));

			return null;
		}

	}

	private static class DoSync implements Runnable
	{
		private EntityPlayer p;
		private String name;

		public DoSync(EntityPlayer p, String string)
		{
			this.p = p;
			this.name = string;
		}


		@Override
		public void run()
		{
			if (p != null)
			{
				ItemStack mainItem = p.getHeldItemMainhand();
				ItemStack offItem = p.getHeldItemOffhand();

				boolean main = !mainItem.isEmpty() && mainItem.getItem() == IEContent.itemTool && mainItem.getItemDamage() == 3;
				boolean off = !offItem.isEmpty() && offItem.getItem() == IEContent.itemTool && offItem.getItemDamage() == 3;
				ItemStack target = main ? mainItem : offItem;

				if (main || off)
				{
					if (name == null && ItemNBTHelper.hasKey(target, "lastMultiblock"))
					{
						ItemNBTHelper.remove(target, "lastMultiblock");
					}
					else if (name != null)
					{
						ItemNBTHelper.setString(target, "lastMultiblock", name);
					}
				}
			}
		}


	}


}
