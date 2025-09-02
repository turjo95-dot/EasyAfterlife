package dev.turjo.easyafterlife.placeholders;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EasyAfterlifePlaceholders extends PlaceholderExpansion {
    
    private final EasyAfterlifePlugin plugin;
    
    public EasyAfterlifePlaceholders(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "easyafterlife";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        
        switch (params.toLowerCase()) {
            case "graves_active":
                return String.valueOf(plugin.getGraveManager().getPlayerActiveGraves(player.getUniqueId()));
                
            case "key_count":
                return String.valueOf(plugin.getKeyManager().countGraveKeys(player));
                
            case "total_graves":
                return String.valueOf(plugin.getGraveManager().getTotalActiveGraves());
                
            case "has_grave":
                return plugin.getGraveManager().getLatestGrave(player.getUniqueId()) != null ? "true" : "false";
                
            default:
                return null;
        }
    }
}