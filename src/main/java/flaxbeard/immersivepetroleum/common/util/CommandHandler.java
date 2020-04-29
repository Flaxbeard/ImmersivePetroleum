package flaxbeard.immersivepetroleum.common.util;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CommandHandler extends CommandBase
{
	ArrayList<IPSubCommand> commands = new ArrayList<>();
	final String name;

	public CommandHandler()
	{
		commands.add(new CommandHelp());
		commands.add(new CommandReservoir());
		name = "ip";

	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

//	/**
//	 * Check if the given ICommandSender has permission to execute this command
//	 */
//	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
//	{
//	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		ArrayList<String> list = new ArrayList<String>();
		if (args.length > 0)
			for (IPSubCommand sub : commands)
			{
				if (args.length == 1)
				{
					if (args[0].isEmpty() || sub.getIdent().startsWith(args[0].toLowerCase(Locale.ENGLISH)))
						list.add(sub.getIdent());
				}
				else if (sub.getIdent().equalsIgnoreCase(args[0]))
				{
					String[] redArgs = new String[args.length - 1];
					System.arraycopy(args, 1, redArgs, 0, redArgs.length);
					ArrayList<String> subCommands = sub.getSubCommands(this, server, sender, redArgs);
					if (subCommands != null)
						list.addAll(subCommands);
				}
			}
		return list;
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		String sub = "";
		int i = 0;
		for (IPSubCommand com : commands)
		{
			sub += ((i++) > 0 ? "|" : "") + com.getIdent();
		}
		return "/" + name + " <" + sub + ">";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
	{
		if (args.length > 0)
			for (IPSubCommand com : commands)
			{
				if (com.getIdent().equalsIgnoreCase(args[0]))
				{
					if (!sender.canUseCommand(com.getPermissionLevel(), this.getName()))
					{
						TextComponentTranslation msg = new TextComponentTranslation("commands.generic.permission");
						msg.getStyle().setColor(TextFormatting.RED);
						sender.sendMessage(msg);
					}
					else
						com.perform(this, server, sender, args);
				}
			}
		else
		{
			String sub = "";
			int i = 0;
			for (IPSubCommand com : commands)
			{
				sub += ((i++) > 0 ? ", " : "") + com.getIdent();
			}
			sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND + "available", sub));
		}
	}

	public abstract static class IPSubCommand
	{
		public abstract String getIdent();

		public abstract void perform(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args);

		public String getHelp(String subIdent)
		{
			return "chat.immersivepetroleum.command." + getIdent() + subIdent + ".help";
		}

		public abstract ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args);

		public abstract int getPermissionLevel();
	}

}