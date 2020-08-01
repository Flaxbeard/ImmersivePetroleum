package flaxbeard.immersivepetroleum.common.util.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandHandler{
	public static void registerServer(CommandDispatcher<CommandSource> dispatcher){
		LiteralArgumentBuilder<CommandSource> lab=Commands.literal("ip");
		
		lab.then(ReservoirCommand.create());
		
		dispatcher.register(lab);
	}
}
