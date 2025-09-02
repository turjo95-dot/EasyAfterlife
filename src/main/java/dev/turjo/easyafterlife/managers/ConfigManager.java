package dev.turjo.easyafterlife.managers;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {
    
    private final EasyAfterlifePlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    // Grave Settings
    public int getGraveLifetime() {
        return config.getInt("grave.lifetime", 1800);
    }
    
    public boolean isSafeRelocationEnabled() {
        return config.getBoolean("grave.safeRelocation", true);
    }
    
    public boolean isOverwriteEnabled() {
        return config.getBoolean("grave.overwrite", true);
    }
    
    public boolean shouldStoreXP() {
        return config.getBoolean("grave.store-xp", true);
    }
    
    public int getSafeRadius() {
        return config.getInt("grave.safeRadius", 10);
    }
    
    public int getMaxGravesPerPlayer() {
        return config.getInt("grave.maxGravesPerPlayer", 3);
    }
    
    // Key Settings
    public boolean areKeysEnabled() {
        return config.getBoolean("keys.enabled", true);
    }
    
    public boolean isLostKeyRetrieveEnabled() {
        return config.getBoolean("keys.lostKeyRetrieve", true);
    }
    
    public int getRetrieveCooldown() {
        return config.getInt("keys.retrieveCooldown", 300);
    }
    
    public double getRetrieveCost() {
        return config.getDouble("keys.retrieveCost", 50.0);
    }
    
    public boolean shouldGiveKeyOnDeath() {
        return config.getBoolean("keys.giveOnDeath", true);
    }
    
    // PvP Settings
    public boolean isRaidingAllowed() {
        return config.getBoolean("pvp.allowRaiding", false);
    }
    
    public int getCorpseLootChance() {
        return config.getInt("pvp.corpseLootChance", 0);
    }
    
    public boolean isGraveBreakingAllowed() {
        return config.getBoolean("pvp.allowGraveBreaking", false);
    }
    
    // Visual Settings
    public boolean areHologramsEnabled() {
        return config.getBoolean("visuals.hologram", true);
    }
    
    public String getHologramText() {
        return config.getString("visuals.hologram-text", "☠ Here lies %player%'s grave");
    }
    
    public Material getChestType() {
        String type = config.getString("visuals.chest-type", "CHEST");
        try {
            return Material.valueOf(type);
        } catch (IllegalArgumentException e) {
            return Material.CHEST;
        }
    }
    
    public boolean areParticlesEnabled() {
        return config.getBoolean("visuals.particles", true);
    }
    
    public String getParticleType() {
        return config.getString("visuals.particleType", "SOUL");
    }
    
    // Sound Settings
    public boolean areSoundsEnabled() {
        return config.getBoolean("sounds.enabled", true);
    }
    
    public Sound getGraveSpawnSound() {
        try {
            return Sound.valueOf(config.getString("sounds.graveSpawn", "BLOCK_BELL_USE"));
        } catch (IllegalArgumentException e) {
            return Sound.BLOCK_BELL_USE;
        }
    }
    
    public Sound getGraveUnlockSound() {
        try {
            return Sound.valueOf(config.getString("sounds.graveUnlock", "BLOCK_CHEST_OPEN"));
        } catch (IllegalArgumentException e) {
            return Sound.BLOCK_CHEST_OPEN;
        }
    }
    
    public Sound getKeyUseSound() {
        try {
            return Sound.valueOf(config.getString("sounds.keyUse", "ITEM_TOTEM_USE"));
        } catch (IllegalArgumentException e) {
            return Sound.ITEM_TOTEM_USE;
        }
    }
    
    // World Settings
    public List<String> getDisabledWorlds() {
        return config.getStringList("worlds.disabled-worlds");
    }
    
    public List<String> getEnabledWorlds() {
        return config.getStringList("worlds.enabled-worlds");
    }
    
    public boolean isWorldEnabled(String worldName) {
        List<String> disabledWorlds = getDisabledWorlds();
        List<String> enabledWorlds = getEnabledWorlds();
        
        if (disabledWorlds.contains(worldName)) {
            return false;
        }
        
        if (!enabledWorlds.isEmpty()) {
            return enabledWorlds.contains(worldName);
        }
        
        return true;
    }
    
    // Database Settings
    public String getDatabaseType() {
        return config.getString("database.type", "SQLITE");
    }
    
    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }
    
    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }
    
    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "easyafterlife");
    }
    
    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }
    
    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "password");
    }
    
    // Messages
    public String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', 
            config.getString("messages." + key, "&cMessage not found: " + key));
    }
    
    public String getPrefix() {
        return getMessage("prefix");
    }
}