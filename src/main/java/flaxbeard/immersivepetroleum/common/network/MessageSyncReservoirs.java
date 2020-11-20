package flaxbeard.immersivepetroleum.common.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.pumpjack.PumpjackHandler.ReservoirType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSyncReservoirs implements INetMessage{
	final Map<ResourceLocation, ReservoirType> map = new HashMap<>();
	
	public MessageSyncReservoirs(HashMap<ResourceLocation, ReservoirType> map){
		for(Map.Entry<ResourceLocation, ReservoirType> e:map.entrySet()){
			this.map.put(e.getKey(), e.getValue());
		}
	}
	
	public MessageSyncReservoirs(PacketBuffer buf){
		int size = buf.readInt();
		for(int i = 0;i < size;i++){
			ResourceLocation loc = new ResourceLocation(buf.readString());
			ReservoirType type = new ReservoirType(buf.readCompoundTag());
			
			this.map.put(loc, type);
		}
	}
	
	@Override
	public void toBytes(PacketBuffer buf){
		buf.writeInt(this.map.size());
		for(Map.Entry<ResourceLocation, ReservoirType> e:this.map.entrySet()){
			buf.writeString(e.getKey().toString());
			buf.writeCompoundTag(e.getValue().writeToNBT());
		}
	}
	
	@Override
	public void process(Supplier<Context> context){
		context.get().enqueueWork(() -> {
			Context con = context.get();
			
			if(con.getDirection().getReceptionSide() == LogicalSide.CLIENT){
				ImmersivePetroleum.log.info("Received SyncReservoirs Message.");
				PumpjackHandler.reservoirs.clear();
				for(Map.Entry<ResourceLocation, ReservoirType> e:this.map.entrySet()){
					PumpjackHandler.reservoirs.put(e.getKey(), e.getValue());
				}
				PumpjackHandler.recalculateChances(true);
				ImmersivePetroleum.log.info("Reservoirs Synced.");
				// ClientProxy.handleReservoirManual();
			}
		});
	}
}
