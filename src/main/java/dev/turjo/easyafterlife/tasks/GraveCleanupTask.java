package dev.turjo.easyafterlife.tasks;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GraveCleanupTask extends BukkitRunnable {
    
    private final EasyAfterlifePlugin plugin;
    
    public GraveCleanupTask(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        try {
            plugin.getGraveManager().cleanupExpiredGraves();
        } catch (Exception e) {
            plugin.getLogger().severe("Error during grave cleanup: " + e.getMessage());
        }
    }
}