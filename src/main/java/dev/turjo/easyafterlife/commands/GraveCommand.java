package dev.turjo.easyafterlife.commands;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GraveCommand implements CommandExecutor, TabCompleter {
    
    private final EasyAfterlifePlugin plugin;
    
    public GraveCommand(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "locate" -> handleLocateCommand(player);
            case "list" -> handleListCommand(player);
            case "retrieve" -> handleRetrieveCommand(player);
            default -> sendHelpMessage(player);
        }
        
        return true;
    }
    
    private void handleLocateCommand(Player player) {
        if (!player.hasPermission("easyafterlife.player.locate")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return;
        }
        
        UUID playerId = player.getUniqueId();
        Grave latestGrave = plugin.getGraveManager().getLatestGrave(playerId);
        
        if (latestGrave == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-grave"));
            return;
        }
        
        Location loc = latestGrave.getLocation();
        String message = plugin.getConfigManager().getMessage("grave-located")
            .replace("%x%", String.valueOf(loc.getBlockX()))
            .replace("%y%", String.valueOf(loc.getBlockY()))
            .replace("%z%", String.valueOf(loc.getBlockZ()))
            .replace("%world%", loc.getWorld().getName());
        
        player.sendMessage(message);
    }
    
    private void handleListCommand(Player player) {
        if (!player.hasPermission("easyafterlife.player.list")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return;
        }
        
        UUID playerId = player.getUniqueId();
        List<Grave> graves = plugin.getGraveManager().getPlayerGraves(playerId);
        
        if (graves.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-grave"));
            return;
        }
        
        player.sendMessage("§6⚰ Your Active Graves:");
        player.sendMessage("§8" + "━".repeat(30));
        
        for (int i = 0; i < graves.size(); i++) {
            Grave grave = graves.get(i);
            Location loc = grave.getLocation();
            
            player.sendMessage(String.format("§7%d. §e%s §7at §b(%d, %d, %d) §7in §a%s", 
                i + 1,
                grave.getFormattedTimeLeft(),
                loc.getBlockX(),
                loc.getBlockY(), 
                loc.getBlockZ(),
                loc.getWorld().getName()));
        }
        
        player.sendMessage("§8" + "━".repeat(30));
    }
    
    private void handleRetrieveCommand(Player player) {
        if (!player.hasPermission("easyafterlife.player.retrieve")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return;
        }
        
        boolean success = plugin.getKeyManager().retrieveLostKey(player);
        
        if (success) {
            player.sendMessage("§aSuccessfully retrieved a new grave key!");
        }
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage("§6⚰ EasyAfterlife Commands:");
        player.sendMessage("§8" + "━".repeat(35));
        player.sendMessage("§e/grave locate §7- Show coordinates of your last grave");
        player.sendMessage("§e/grave list §7- List all your active graves");
        player.sendMessage("§e/grave retrieve §7- Retrieve a lost grave key");
        player.sendMessage("§8" + "━".repeat(35));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("locate", "list", "retrieve");
            String partial = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(partial)) {
                    completions.add(subCommand);
                }
            }
        }
        
        return completions;
    }
}