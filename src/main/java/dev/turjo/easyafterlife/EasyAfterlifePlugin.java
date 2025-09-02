package dev.turjo.easyafterlife;

import dev.turjo.easyafterlife.commands.GraveAdminCommand;
import dev.turjo.easyafterlife.commands.GraveCommand;
import dev.turjo.easyafterlife.database.DatabaseManager;
import dev.turjo.easyafterlife.listeners.PlayerDeathListener;
import dev.turjo.easyafterlife.listeners.PlayerInteractListener;
import dev.turjo.easyafterlife.managers.ConfigManager;
import dev.turjo.easyafterlife.managers.GraveManager;
import dev.turjo.easyafterlife.managers.HologramManager;
import dev.turjo.easyafterlife.managers.KeyManager;
import dev.turjo.easyafterlife.tasks.GraveCleanupTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class EasyAfterlifePlugin extends JavaPlugin {
    
    private static EasyAfterlifePlugin instance;
    
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private GraveManager graveManager;
    private KeyManager keyManager;
    private HologramManager hologramManager;
    private Economy economy;
    
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("⚰️ EasyAfterlife - Secure Death, Smarter Recovery");
        getLogger().info("Initializing plugin...");
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.graveManager = new GraveManager(this);
        this.keyManager = new KeyManager(this);
        this.hologramManager = new HologramManager(this);
        
        // Setup economy
        setupEconomy();
        
        // Register events
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        
        // Register commands
        getCommand("grave").setExecutor(new GraveCommand(this));
        getCommand("graveadmin").setExecutor(new GraveAdminCommand(this));
        
        // Start cleanup task
        new GraveCleanupTask(this).runTaskTimer(this, 20L * 60L, 20L * 60L); // Every minute
        
        getLogger().info("✅ EasyAfterlife enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        if (hologramManager != null) {
            hologramManager.cleanup();
        }
        
        getLogger().info("⚰️ EasyAfterlife disabled successfully!");
    }
    
    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        
        economy = rsp.getProvider();
        getLogger().info("✅ Vault economy integration enabled!");
    }
    
    public static EasyAfterlifePlugin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public GraveManager getGraveManager() {
        return graveManager;
    }
    
    public KeyManager getKeyManager() {
        return keyManager;
    }
    
    public HologramManager getHologramManager() {
        return hologramManager;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public boolean hasEconomy() {
        return economy != null;
    }
}