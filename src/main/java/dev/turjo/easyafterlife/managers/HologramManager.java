package dev.turjo.easyafterlife.managers;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramManager {
    
    private final EasyAfterlifePlugin plugin;
    private final Map<UUID, Hologram> graveHolograms;
    private boolean decentHologramsAvailable;
    
    public HologramManager(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
        this.graveHolograms = new HashMap<>();
        
        // Check if DecentHolograms is available
        this.decentHologramsAvailable = plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null;
        
        if (decentHologramsAvailable) {
            plugin.getLogger().info("✅ DecentHolograms integration enabled!");
        }
    }
    
    public void createGraveHologram(Grave grave) {
        if (!decentHologramsAvailable || !plugin.getConfigManager().areHologramsEnabled()) {
            return;
        }
        
        Location hologramLocation = grave.getLocation().clone().add(0.5, 2.0, 0.5);
        String hologramName = "grave_" + grave.getGraveId().toString().replace("-", "");
        
        // Get hologram text and replace placeholders
        String text = plugin.getConfigManager().getHologramText()
            .replace("%player%", grave.getPlayerName())
            .replace("%time%", grave.getFormattedTimeLeft());
        
        // Create hologram with multiple lines
        Hologram hologram = DHAPI.createHologram(hologramName, hologramLocation);
        DHAPI.addHologramLine(hologram, "§6⚰ §8[§bEasyAfterlife§8]");
        DHAPI.addHologramLine(hologram, "§7" + text);
        DHAPI.addHologramLine(hologram, "§8Time left: §c" + grave.getFormattedTimeLeft());
        
        graveHolograms.put(grave.getGraveId(), hologram);
        
        // Start hologram update task
        startHologramUpdateTask(grave.getGraveId());
    }
    
    public void removeGraveHologram(UUID graveId) {
        Hologram hologram = graveHolograms.remove(graveId);
        if (hologram != null) {
            DHAPI.removeHologram(hologram.getName());
        }
    }
    
    public void updateGraveHologram(UUID graveId) {
        Hologram hologram = graveHolograms.get(graveId);
        Grave grave = plugin.getGraveManager().getGrave(graveId);
        
        if (hologram == null || grave == null) {
            return;
        }
        
        // Update time left line
        DHAPI.setHologramLine(hologram, 2, "§8Time left: §c" + grave.getFormattedTimeLeft());
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
        if (decentHologramsAvailable) {
            for (Hologram hologram : graveHolograms.values()) {
                DHAPI.removeHologram(hologram.getName());
            }
            graveHolograms.clear();
        }
    }
}