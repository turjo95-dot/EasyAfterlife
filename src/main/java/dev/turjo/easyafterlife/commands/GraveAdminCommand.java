package dev.turjo.easyafterlife.commands;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraveAdminCommand implements CommandExecutor, TabCompleter {
    
    private final EasyAfterlifePlugin plugin;
    
    public GraveAdminCommand(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("easyafterlife.admin.use")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "givekey" -> handleGiveKeyCommand(sender, args);
            case "reload" -> handleReloadCommand(sender);
            case "stats" -> handleStatsCommand(sender);
            default -> sendHelpMessage(sender);
        }
        
        return true;
    }
    
    private void handleGiveKeyCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("easyafterlife.admin.givekey")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /graveadmin givekey <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer '" + args[1] + "' not found or offline!");
            return;
        }
        
        Grave latestGrave = plugin.getGraveManager().getLatestGrave(target.getUniqueId());
        if (latestGrave == null) {
            sender.sendMessage("§cPlayer '" + target.getName() + "' has no active graves!");
            return;
        }
        
        plugin.getKeyManager().giveGraveKey(target, latestGrave);
        
        sender.sendMessage("§aSuccessfully gave grave key to " + target.getName() + "!");
        target.sendMessage("§aAn admin gave you a grave key!");
    }
    
    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("easyafterlife.admin.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return;
        }
        
        try {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(plugin.getConfigManager().getMessage("reload-success"));
        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload configuration: " + e.getMessage());
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
        }
    }
    
    private void handleStatsCommand(CommandSender sender) {
        int totalGraves = plugin.getGraveManager().getTotalActiveGraves();
        
        sender.sendMessage("§6⚰ EasyAfterlife Statistics:");
        sender.sendMessage("§8" + "━".repeat(30));
        sender.sendMessage("§eTotal Active Graves: §7" + totalGraves);
        sender.sendMessage("§ePlugin Version: §7" + plugin.getDescription().getVersion());
        sender.sendMessage("§eUptime: §7" + getUptime());
        sender.sendMessage("§8" + "━".repeat(30));
    }
    
    private String getUptime() {
        long uptime = System.currentTimeMillis() - plugin.getServer().getWorlds().get(0).getFullTime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        return String.format("%dh %dm %ds", hours % 24, minutes % 60, seconds % 60);
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6⚰ EasyAfterlife Admin Commands:");
        sender.sendMessage("§8" + "━".repeat(40));
        sender.sendMessage("§e/graveadmin givekey <player> §7- Give a grave key to a player");
        sender.sendMessage("§e/graveadmin reload §7- Reload plugin configuration");
        sender.sendMessage("§e/graveadmin stats §7- Show plugin statistics");
        sender.sendMessage("§8" + "━".repeat(40));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("givekey", "reload", "stats");
            String partial = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(partial)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("givekey")) {
            String partial = args[1].toLowerCase();
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partial)) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}