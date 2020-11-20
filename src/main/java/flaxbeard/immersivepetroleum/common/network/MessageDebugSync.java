package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Supplier;

import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageDebugSync implements INetMessage{
	
	CompoundNBT nbt;
	public MessageDebugSync(CompoundNBT nbt){
		this.nbt = nbt;
	}
	
	public MessageDebugSync(PacketBuffer buf){
		this.nbt = buf.readCompoundTag();
	}
	
	@Override
	public void toBytes(PacketBuffer buf){
		buf.writeCompoundTag(this.nbt);
	}
	
	@Override
	public void process(Supplier<Context> context){
		context.get().enqueueWork(() -> {
			Context con = context.get();
			
			if(con.getDirection().getReceptionSide() == LogicalSide.SERVER && con.getSender() != null){
				PlayerEntity player = con.getSender();
				ItemStack mainItem = player.getHeldItemMainhand();
				ItemStack secondItem = player.getHeldItemOffhand();
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.debugItem;
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.debugItem;
				
				if(main || off){
					ItemStack target = main ? mainItem : secondItem;
					
					CompoundNBT targetNBT = target.getOrCreateChildTag("settings");
					targetNBT.merge(this.nbt);
				}
			}
		});
	}
}
