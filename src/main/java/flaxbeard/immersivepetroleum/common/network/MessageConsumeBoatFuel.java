package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Supplier;

import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageConsumeBoatFuel implements INetMessage{
	public int amount;
	
	public MessageConsumeBoatFuel(int amount){
		this.amount = amount;
	}
	
	public MessageConsumeBoatFuel(PacketBuffer buf){
		this.amount = buf.readInt();
	}
	
	@Override
	public void toBytes(PacketBuffer buf){
		buf.writeInt(amount);
	}
	
	@Override
	public void process(Supplier<Context> context){
		context.get().enqueueWork(() -> {
			Context con = context.get();
			
			if(con.getDirection().getReceptionSide() == LogicalSide.SERVER && con.getSender() != null){
				Entity entity = con.getSender().getRidingEntity();
				
				if(entity instanceof MotorboatEntity){
					MotorboatEntity boat = (MotorboatEntity) entity;
					FluidStack fluid = boat.getContainedFluid();
					
					if(fluid != null && fluid != FluidStack.EMPTY)
						fluid.setAmount(Math.max(0, fluid.getAmount() - amount));
					
					boat.setContainedFluid(fluid);
				}
			}
		});
	}
}
