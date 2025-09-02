package dev.turjo.easyafterlife.listeners;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Random;

public class PlayerDeathListener implements Listener {
    
    private final EasyAfterlifePlugin plugin;
    private final Random random;
    
    public PlayerDeathListener(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if world is enabled
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }
        
        // Check corpse loot chance for PvP
        if (plugin.getConfigManager().getCorpseLootChance() > 0) {
            int chance = random.nextInt(100);
            if (chance < plugin.getConfigManager().getCorpseLootChance()) {
                // Allow normal item drops
                return;
            }
        }
        
        // Check if player has any items to store
        if (isInventoryEmpty(player)) {
            return;
        }
        
        // Clear drops to prevent duplication
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        // Create grave
        Grave grave = plugin.getGraveManager().createGrave(player, player.getLocation());
        
        // Give grave key
        if (plugin.getConfigManager().shouldGiveKeyOnDeath()) {
            plugin.getKeyManager().giveGraveKey(player, grave);
        }
        
        // Send message
        player.sendMessage(plugin.getConfigManager().getMessage("grave-created"));
        
        // Log grave creation
        plugin.getLogger().info(String.format("Created grave for player %s at %s", 
            player.getName(), locationToString(grave.getLocation())));
    }
    
    private boolean isInventoryEmpty(Player player) {
        // Check main inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().isItem()) {
                return false;
            }
        }
        
        // Check armor
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType().isItem()) {
                return false;
            }
        }
        
        // Check offhand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType().isItem()) {
            return false;
        }
        
        // Check experience
        if (plugin.getConfigManager().shouldStoreXP() && player.getTotalExperience() > 0) {
            return false;
        }
        
        return true;
    }
    
    private String locationToString(org.bukkit.Location location) {
        return String.format("(%d, %d, %d) in %s", 
            location.getBlockX(), 
            location.getBlockY(), 
            location.getBlockZ(), 
            location.getWorld().getName());
    }
}