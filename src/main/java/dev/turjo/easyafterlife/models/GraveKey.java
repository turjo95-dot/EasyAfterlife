package dev.turjo.easyafterlife.models;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GraveKey {
    
    public static final String KEY_NAME_PREFIX = "✦ Grave Key of ";
    public static final String KEY_NAME_SUFFIX = " ✦";
    public static final String NBT_KEY = "EASYAFTERLIFE_GRAVE_KEY";
    
    private final UUID graveId;
    private final UUID playerId;
    private final String playerName;
    
    public GraveKey(UUID graveId, UUID playerId, String playerName) {
        this.graveId = graveId;
        this.playerId = playerId;
        this.playerName = playerName;
    }
    
    public UUID getGraveId() {
        return graveId;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public ItemStack createKeyItem() {
        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = key.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6" + KEY_NAME_PREFIX + playerName + KEY_NAME_SUFFIX);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Use this to reclaim your belongings from your grave.");
            lore.add("§8");
            lore.add("§eGrave ID: §7" + graveId.toString().substring(0, 8) + "...");
            lore.add("§eOwner: §7" + playerName);
            lore.add("§8");
            lore.add("§c§lSoulbound - Cannot be traded");
            
            meta.setLore(lore);
            
            // Add custom model data for resource pack support
            meta.setCustomModelData(12345);
            
            key.setItemMeta(meta);
        }
        
        return key;
    }
    
    public static boolean isGraveKey(ItemStack item) {
        if (item == null || item.getType() != Material.TRIPWIRE_HOOK) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        String displayName = meta.getDisplayName();
        return displayName.contains(KEY_NAME_PREFIX) && displayName.contains(KEY_NAME_SUFFIX);
    }
    
    public static UUID getGraveIdFromKey(ItemStack key) {
        if (!isGraveKey(key)) {
            return null;
        }
        
        ItemMeta meta = key.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return null;
        }
        
        for (String line : meta.getLore()) {
            if (line.contains("Grave ID:")) {
                String idPart = line.split("§7")[1].split("\\.")[0];
                // This is a simplified approach - in a real implementation,
                // you'd store the full UUID in NBT data
                return null; // Placeholder for proper NBT implementation
            }
        }
        
        return null;
    }
    
    public static String getOwnerFromKey(ItemStack key) {
        if (!isGraveKey(key)) {
            return null;
        }
        
        String displayName = key.getItemMeta().getDisplayName();
        String cleanName = ChatColor.stripColor(displayName);
        
        int startIndex = cleanName.indexOf(KEY_NAME_PREFIX) + KEY_NAME_PREFIX.length();
        int endIndex = cleanName.indexOf(KEY_NAME_SUFFIX);
        
        if (startIndex > 0 && endIndex > startIndex) {
            return cleanName.substring(startIndex, endIndex);
        }
        
        return null;
    }
}