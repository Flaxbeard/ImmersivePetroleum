package flaxbeard.immersivepetroleum.common.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.network.CloseBookPacket.CloseBookPacketHandler;
import flaxbeard.immersivepetroleum.common.network.RotateSchematicPacket.RotateSchematicPacketHandler;

public class IPPacketHandler
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ImmersivePetroleum.MODID);
	
	public static void preInit()
	{	
		INSTANCE.registerMessage(CloseBookPacketHandler.class, CloseBookPacket.class, 0, Side.SERVER);
		INSTANCE.registerMessage(RotateSchematicPacketHandler.class, RotateSchematicPacket.class, 1, Side.SERVER);
	}
}
