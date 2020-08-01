package flaxbeard.immersivepetroleum.common.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.network.IMessage;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/** As a result of manual changes. Altering an already existing page is no longer possible. ~TwistedGate */
@Deprecated
public class MessageReservoirListSync implements IMessage{
	Map<ReservoirType, Integer> map = new HashMap<ReservoirType, Integer>();
	
	public MessageReservoirListSync(HashMap<ReservoirType, Integer> map){
		this.map = map;
	}
	public MessageReservoirListSync(PacketBuffer buf){
		int size = buf.readInt();
		for(int i = 0;i < size;i++){
			int weight=buf.readInt();
			ReservoirType type = new ReservoirType(buf.readCompoundTag());
			
			map.put(type, weight);
		}
	}
	
	@Override
	public void toBytes(PacketBuffer buf){
		buf.writeInt(map.size());
		for(Map.Entry<ReservoirType, Integer> e:map.entrySet()){
			CompoundNBT tag = e.getKey().writeToNBT();
			
			buf.writeInt(e.getValue()); // Weight
			buf.writeCompoundTag(tag);
		}
	}

	@Override
	public void process(Supplier<Context> context){
		context.get().enqueueWork(()->{
			Context con=context.get();
			
			if(con.getDirection().getReceptionSide()==LogicalSide.CLIENT){
				PumpjackHandler.reservoirList.clear();
				for(ReservoirType min:this.map.keySet()){
					PumpjackHandler.reservoirList.put(min, this.map.get(min));
				}
				
				//ClientProxy.handleReservoirManual();
			}
		});
	}
	
	/*
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
	*/
}