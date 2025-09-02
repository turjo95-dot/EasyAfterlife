package dev.turjo.easyafterlife.managers;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import dev.turjo.easyafterlife.models.GraveKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KeyManager {
    
    private final EasyAfterlifePlugin plugin;
    private final Map<UUID, Long> retrieveCooldowns;
    
    public KeyManager(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
        this.retrieveCooldowns = new HashMap<>();
    }
    
    public void giveGraveKey(Player player, Grave grave) {
        if (!plugin.getConfigManager().areKeysEnabled()) {
            return;
        }
        
        GraveKey graveKey = new GraveKey(grave.getGraveId(), grave.getPlayerId(), grave.getPlayerName());
        ItemStack keyItem = graveKey.createKeyItem();
        
        // Try to add to inventory, drop if full
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(keyItem);
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        
        // Play sound
        if (plugin.getConfigManager().areSoundsEnabled()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getKeyUseSound(), 1.0f, 1.2f);
        }
        
        player.sendMessage(plugin.getConfigManager().getMessage("key-given"));
    }
    
    public boolean retrieveLostKey(Player player) {
        if (!plugin.getConfigManager().isLostKeyRetrieveEnabled()) {
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Check cooldown
        if (isOnRetrieveCooldown(playerId)) {
            long timeLeft = getCooldownTimeLeft(playerId);
            String message = plugin.getConfigManager().getMessage("key-cooldown")
                .replace("%time%", String.valueOf(timeLeft));
            player.sendMessage(message);
            return false;
        }
        
        // Check economy cost
        if (plugin.hasEconomy() && plugin.getConfigManager().getRetrieveCost() > 0) {
            double cost = plugin.getConfigManager().getRetrieveCost();
            
            if (!plugin.getEconomy().has(player, cost)) {
                String message = plugin.getConfigManager().getMessage("insufficient-funds")
                    .replace("%cost%", String.valueOf(cost));
                player.sendMessage(message);
                return false;
            }
            
            plugin.getEconomy().withdrawPlayer(player, cost);
        }
        
        // Get latest grave
        Grave latestGrave = plugin.getGraveManager().getLatestGrave(playerId);
        if (latestGrave == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-grave"));
            return false;
        }
        
        // Give key
        giveGraveKey(player, latestGrave);
        
        // Set cooldown
        setCooldown(playerId);
        
        return true;
    }
    
    public boolean hasGraveKey(Player player, UUID graveId) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (GraveKey.isGraveKey(item)) {
                UUID keyGraveId = GraveKey.getGraveIdFromKey(item);
                if (graveId.equals(keyGraveId)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public ItemStack findGraveKey(Player player, UUID graveId) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (GraveKey.isGraveKey(item)) {
                UUID keyGraveId = GraveKey.getGraveIdFromKey(item);
                if (graveId.equals(keyGraveId)) {
                    return item;
                }
            }
        }
        return null;
    }
    
    public void removeGraveKey(Player player, UUID graveId) {
        Inventory inventory = player.getInventory();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (GraveKey.isGraveKey(item)) {
                UUID keyGraveId = GraveKey.getGraveIdFromKey(item);
                if (graveId.equals(keyGraveId)) {
                    inventory.setItem(i, null);
                    return;
                }
            }
        }
    }
    
    public int countGraveKeys(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (GraveKey.isGraveKey(item)) {
                count++;
            }
        }
        return count;
    }
    
    private boolean isOnRetrieveCooldown(UUID playerId) {
        Long lastRetrieve = retrieveCooldowns.get(playerId);
        if (lastRetrieve == null) {
            return false;
        }
        
        long cooldownEnd = lastRetrieve + (plugin.getConfigManager().getRetrieveCooldown() * 1000L);
        return System.currentTimeMillis() < cooldownEnd;
    }
    
    private long getCooldownTimeLeft(UUID playerId) {
        Long lastRetrieve = retrieveCooldowns.get(playerId);
        if (lastRetrieve == null) {
            return 0;
        }
        
        long cooldownEnd = lastRetrieve + (plugin.getConfigManager().getRetrieveCooldown() * 1000L);
        return Math.max(0, (cooldownEnd - System.currentTimeMillis()) / 1000);
    }
    
    private void setCooldown(UUID playerId) {
        retrieveCooldowns.put(playerId, System.currentTimeMillis());
    }
}