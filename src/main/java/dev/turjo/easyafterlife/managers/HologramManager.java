package dev.turjo.easyafterlife.managers;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramManager {
    
    private final EasyAfterlifePlugin plugin;
    private final Map<UUID, Hologram> graveHolograms;
    private HolographicDisplaysAPI hologramAPI;
    
    public HologramManager(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
        this.graveHolograms = new HashMap<>();
        
        // Initialize HolographicDisplays API if available
        if (plugin.getServer().getPluginManager().getPlugin("HolographicDisplays") != null) {
            this.hologramAPI = HolographicDisplaysAPI.get(plugin);
        }
    }
    
    public void createGraveHologram(Grave grave) {
        if (hologramAPI == null || !plugin.getConfigManager().areHologramsEnabled()) {
            return;
        }
        
        Location hologramLocation = grave.getLocation().clone().add(0.5, 2.0, 0.5);
        
        Hologram hologram = hologramAPI.createHologram(hologramLocation);
        
        // Get hologram text and replace placeholders
        String text = plugin.getConfigManager().getHologramText()
            .replace("%player%", grave.getPlayerName())
            .replace("%time%", grave.getFormattedTimeLeft());
        
        hologram.getLines().appendText("§6⚰ §8[§bEasyAfterlife§8]");
        hologram.getLines().appendText("§7" + text);
        hologram.getLines().appendText("§8Time left: §c" + grave.getFormattedTimeLeft());
        
        graveHolograms.put(grave.getGraveId(), hologram);
        
        // Start hologram update task
        startHologramUpdateTask(grave.getGraveId());
    }
    
    public void removeGraveHologram(UUID graveId) {
        Hologram hologram = graveHolograms.remove(graveId);
        if (hologram != null) {
            hologram.delete();
        }
    }
    
    public void updateGraveHologram(UUID graveId) {
        Hologram hologram = graveHolograms.get(graveId);
        Grave grave = plugin.getGraveManager().getGrave(graveId);
        
        if (hologram == null || grave == null) {
            return;
        }
        
        // Update time left
        if (hologram.getLines().size() >= 3) {
            hologram.getLines().remove(2);
            hologram.getLines().insertText(2, "§8Time left: §c" + grave.getFormattedTimeLeft());
        }
    }
    
    private void startHologramUpdateTask(UUID graveId) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            Grave grave = plugin.getGraveManager().getGrave(graveId);
            if (grave == null || grave.isExpired()) {
                removeGraveHologram(graveId);
                return;
            }
            
            updateGraveHologram(graveId);
        }, 20L, 20L); // Update every second
    }
    
    public void cleanup() {
        if (hologramAPI != null) {
            for (Hologram hologram : graveHolograms.values()) {
                hologram.delete();
            }
            graveHolograms.clear();
        }
    }
}