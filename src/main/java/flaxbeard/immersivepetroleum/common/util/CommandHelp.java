package flaxbeard.immersivepetroleum.common.util;

@Deprecated
public class CommandHelp{}
/*
public class CommandHelp extends IPSubCommand{
	@Override
	public String getIdent(){
		return "help";
	}
	
	@Override
	public void perform(CommandHandler handler, MinecraftServer server, ICommandSender sender, String[] args){
		if(args.length > 1){
			String sub = "";
			for(int i = 2;i < args.length;i++){
				sub += "." + args[i];
			}
			for(IPSubCommand com:handler.commands){
				if(com.getIdent().equalsIgnoreCase(args[1])){
					String h = I18n.format(com.getHelp(sub));
					for(String s:h.split("<br>")){
						sender.sendMessage(new StringTextComponent(s));
					}
				}
			}
		}else{
			String h = I18n.format(getHelp(""));
			for(String s:h.split("<br>")){
				sender.sendMessage(new StringTextComponent(s));
			}
			String sub = "";
			int i = 0;
			for(IPSubCommand com:handler.commands){
				sub += ((i++) > 0 ? ", " : "") + com.getIdent();
			}
			sender.sendMessage(new TranslationTextComponent(Lib.CHAT_COMMAND + "available", sub));
		}
	}
	
	@Override
	public ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args){
		ArrayList<String> list = new ArrayList<>();
		for(IPSubCommand sub:h.commands){
			if(sub != this && sender.canUseCommand(sub.getPermissionLevel(), h.getName())){
				if(args.length == 1){
					if(args[0].isEmpty() || sub.getIdent().startsWith(args[0].toLowerCase(Locale.ENGLISH))) list.add(sub.getIdent());
				}else if(sub.getIdent().equalsIgnoreCase(args[0])){
					String[] redArgs = new String[args.length - 1];
					System.arraycopy(args, 1, redArgs, 0, redArgs.length);
					ArrayList<String> subCommands = sub.getSubCommands(h, server, sender, redArgs);
					if(subCommands != null) list.addAll(subCommands);
				}
			}
		}
		return list;
	}
	
	@Override
	public int getPermissionLevel(){
		return 0;
	}
}
*/
