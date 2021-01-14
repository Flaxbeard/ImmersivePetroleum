package flaxbeard.immersivepetroleum.common.network;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public interface INetMessage{
	void toBytes(PacketBuffer buf);
	void process(Supplier<Context> context);
}
