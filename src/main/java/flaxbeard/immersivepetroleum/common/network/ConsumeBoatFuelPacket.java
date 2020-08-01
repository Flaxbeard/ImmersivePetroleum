package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.network.IMessage;
import flaxbeard.immersivepetroleum.common.entity.EntitySpeedboat;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ConsumeBoatFuelPacket implements IMessage{
	public int amount;
	
	public ConsumeBoatFuelPacket(int amount){
		this.amount = amount;
	}
	
	public ConsumeBoatFuelPacket(PacketBuffer buf){
		amount = buf.readInt();
	}
	
	@Override
	public void toBytes(PacketBuffer buf){
		buf.writeInt(amount);
	}
	
	@Override
	public void process(Supplier<Context> context){
		context.get().enqueueWork(()->{
			Context con=context.get();
			
			if(con.getDirection().getReceptionSide()==LogicalSide.SERVER && con.getSender()!=null){
				Entity entity = con.getSender().getRidingEntity();
				
				if(entity instanceof EntitySpeedboat){
					EntitySpeedboat boat = (EntitySpeedboat) entity;
					FluidStack fluid = boat.getContainedFluid();
					if(fluid != null)
						fluid.setAmount(Math.max(0, fluid.getAmount() - amount));
					
					boat.setContainedFluid(fluid);
				}
			}
		});
	}
/*
	public static class Handler implements IMessageHandler<ConsumeBoatFuelPacket, IMessage>{
		
		@Override
		public IMessage onMessage(ConsumeBoatFuelPacket message, MessageContext ctx){
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(new DoSync(player, message.amount));
			
			return null;
		}
	}
	
	private static class DoSync implements Runnable{
		private EntityPlayer p;
		private int amount;
		
		public DoSync(EntityPlayer p, int amount){
			this.p = p;
			this.amount = amount;
		}
		
		@Override
		public void run(){
			if(p != null){
				Entity entity = p.getRidingEntity();
				
				if(entity instanceof EntitySpeedboat){
					EntitySpeedboat boat = (EntitySpeedboat) entity;
					FluidStack fluid = boat.getContainedFluid();
					if(fluid != null) fluid.amount = Math.max(0, fluid.amount - amount);
					boat.setContainedFluid(fluid);
				}
			}
		}
	}
	*/
}
