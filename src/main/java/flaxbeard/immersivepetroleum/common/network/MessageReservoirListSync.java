package flaxbeard.immersivepetroleum.common.network;

import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.client.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class MessageReservoirListSync implements IMessage
{
	Map<ReservoirType, Integer> map = new HashMap<ReservoirType, Integer>();

	public MessageReservoirListSync(HashMap<ReservoirType, Integer> map)
	{
		this.map = map;
	}

	public MessageReservoirListSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		int size = buf.readInt();
		for (int i = 0; i < size; i++)
		{
			NBTTagCompound tag = ByteBufUtils.readTag(buf);
			ReservoirType mix = ReservoirType.readFromNBT(tag);
			if (mix != null)
				map.put(mix, tag.getInteger("weight"));
		}

	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(map.size());
		for (Map.Entry<ReservoirType, Integer> e : map.entrySet())
		{
			NBTTagCompound tag = e.getKey().writeToNBT();
			tag.setInteger("weight", e.getValue());
			ByteBufUtils.writeTag(buf, tag);
		}
	}

	public static class Handler implements IMessageHandler<MessageReservoirListSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageReservoirListSync message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> onMessageMain(message));
			return null;
		}

		private void onMessageMain(MessageReservoirListSync message)
		{
			PumpjackHandler.reservoirList.clear();
			for (ReservoirType min : message.map.keySet())
			{
				PumpjackHandler.reservoirList.put(min, message.map.get(min));
			}
			ClientProxy.handleReservoirManual();
		}
	}
}