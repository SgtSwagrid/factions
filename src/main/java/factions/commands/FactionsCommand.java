package factions.commands;

import factions.events.flags.FlagsEvent;
import factions.forgeevents.LootCrateEvent;
import factions.forgeevents.LootCrateEvent.Tier;
import factions.util.Values;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class FactionsCommand extends CommandBase {
	
	private static final String NAME = "f";

	@Override
	public String getName() { return NAME; }

	@Override
	public String getUsage(ICommandSender sender) {
		return "Usage: /f <claim/home>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender,
			String[] args) throws CommandException {
		
		TextComponentString usage = new TextComponentString
				("Usage: /f <claim/home>");
		
		if(args.length == 0) {
			sender.sendMessage(usage);
			return;
		}
		
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		
		switch(args[0].toLowerCase()) {
				
			case "purge":
				if(player.canUseCommand(2, "")) {
					purge();
				}
				break;
				
			case "drop":
				if(player.canUseCommand(2, "")) {
					
					double random = Math.random();
					Tier tier;
					if(random < 0.65F) tier = Tier.COMMON;
					else if(random < 0.9F) tier = Tier.RARE;
					else tier = Tier.LEGENDARY;
					
					LootCrateEvent.trigger(0, 0, tier);
				}
				break;
				
			case "toggle":
				if(player.canUseCommand(2, "")) {
					
					Values.setBoolean("loot_crate_enabled", !Values.getBoolean("loot_crate_enabled"));
					sender.sendMessage(new TextComponentString("Loot crates " + (Values.getBoolean("loot_crate_enabled") ? "enabled." : "disabled.")));
				}
				break;
			
			default:
				sender.sendMessage(usage);
		}
	}
	
	private void purge() {
		
		if(!Values.getBoolean("f:flags_prepared")) {
			FlagsEvent.prepare();
			
		} else if(!Values.getBoolean("f:flags_active")) {
			FlagsEvent.start();
			
		} else {
			FlagsEvent.stop();
		}
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) { return true; }
	
	@Override
	public int getRequiredPermissionLevel() { return 0; }
}