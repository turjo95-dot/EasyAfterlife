package dev.turjo.easyafterlife.managers;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import dev.turjo.easyafterlife.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GraveManager {
    
    private final EasyAfterlifePlugin plugin;
    private final Map<UUID, List<Grave>> playerGraves;
    private final Map<Location, Grave> locationGraves;
    private final Map<UUID, Grave> graveIndex;
    
    public GraveManager(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
        this.playerGraves = new ConcurrentHashMap<>();
        this.locationGraves = new ConcurrentHashMap<>();
        this.graveIndex = new ConcurrentHashMap<>();
        
        // Load graves from database
        loadGravesFromDatabase();
    }
    
    public Grave createGrave(Player player, Location deathLocation) {
        UUID graveId = UUID.randomUUID();
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();
        
        // Get safe location
        Location graveLocation = plugin.getConfigManager().isSafeRelocationEnabled() 
            ? LocationUtils.findSafeLocation(deathLocation, plugin.getConfigManager().getSafeRadius())
            : deathLocation;
        
        // Store inventory
        ItemStack[] inventory = player.getInventory().getContents().clone();
        ItemStack[] armor = player.getInventory().getArmorContents().clone();
        ItemStack offhand = player.getInventory().getItemInOffHand().clone();
        
        // Store experience
        int experience = plugin.getConfigManager().shouldStoreXP() ? player.getTotalExperience() : 0;
        
        // Calculate expiration time
        long creationTime = System.currentTimeMillis();
        long expirationTime = creationTime + (plugin.getConfigManager().getGraveLifetime() * 1000L);
        
        // Create grave object
        Grave grave = new Grave(graveId, playerId, playerName, graveLocation, 
                               inventory, armor, offhand, experience, creationTime, expirationTime);
        
        // Handle overwrite rule
        if (plugin.getConfigManager().isOverwriteEnabled()) {
            removePlayerGraves(playerId);
        }
        
        // Check max graves per player
        List<Grave> existingGraves = playerGraves.getOrDefault(playerId, new ArrayList<>());
        if (existingGraves.size() >= plugin.getConfigManager().getMaxGravesPerPlayer()) {
            removeOldestGrave(playerId);
        }
        
        // Store grave
        playerGraves.computeIfAbsent(playerId, k -> new ArrayList<>()).add(grave);
        locationGraves.put(graveLocation, grave);
        graveIndex.put(graveId, grave);
        
        // Place physical grave
        placeGraveChest(graveLocation);
        
        // Store items in the chest
        storeItemsInChest(graveLocation, grave);
        
        // Create hologram
        if (plugin.getConfigManager().areHologramsEnabled()) {
            plugin.getHologramManager().createGraveHologram(grave);
        }
        
        // Play sound
        if (plugin.getConfigManager().areSoundsEnabled()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getGraveSpawnSound(), 1.0f, 1.0f);
        }
        
        // Clear player inventory
        player.getInventory().clear();
        if (plugin.getConfigManager().shouldStoreXP()) {
            player.setTotalExperience(0);
            player.setLevel(0);
            player.setExp(0);
        }
        
        // Save to database
        plugin.getDatabaseManager().saveGrave(grave);
        
        // Start particle effects
        if (plugin.getConfigManager().areParticlesEnabled()) {
            startParticleEffects(graveLocation);
        }
        
        return grave;
    }
    
    public boolean unlockGrave(UUID graveId, Player player) {
        Grave grave = graveIndex.get(graveId);
        
        if (grave == null) {
            return false;
        }
        
        // Check if player owns the grave (unless raiding is allowed)
        if (!plugin.getConfigManager().isRaidingAllowed() && !grave.getPlayerId().equals(player.getUniqueId())) {
            return false;
        }
        
        // Restore inventory
        restorePlayerInventory(player, grave);
        
        // Remove grave
        removeGrave(grave);
        
        // Play sound
        if (plugin.getConfigManager().areSoundsEnabled()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getGraveUnlockSound(), 1.0f, 1.0f);
        }
        
        return true;
    }
    
    public void removeGraveAfterAccess(Grave grave) {
        removeGrave(grave);
    }
    
    private void restorePlayerInventory(Player player, Grave grave) {
        // Clear current inventory
        player.getInventory().clear();
        
        // Restore items
        player.getInventory().setContents(grave.getInventory());
        player.getInventory().setArmorContents(grave.getArmor());
        player.getInventory().setItemInOffHand(grave.getOffhand());
        
        // Restore experience
        if (plugin.getConfigManager().shouldStoreXP() && grave.getExperience() > 0) {
            player.setTotalExperience(grave.getExperience());
        }
    }
    
    private void placeGraveChest(Location location) {
        Block block = location.getBlock();
        block.setType(plugin.getConfigManager().getChestType());
        
        // Set chest inventory to empty but locked
        if (block.getState() instanceof Chest chest) {
            chest.setCustomName("§8[§6⚰§8] §7Grave Chest");
            chest.update();
        }
    }
    
    private void storeItemsInChest(Location location, Grave grave) {
        Block block = location.getBlock();
        if (!(block.getState() instanceof Chest chest)) {
            return;
        }
        
        Inventory chestInventory = chest.getInventory();
        chestInventory.clear();
        
        // Store main inventory items
        ItemStack[] graveInventory = grave.getInventory();
        for (int i = 0; i < graveInventory.length && i < chestInventory.getSize(); i++) {
            if (graveInventory[i] != null) {
                chestInventory.setItem(i, graveInventory[i]);
            }
        }
        
        // Store armor in remaining slots
        ItemStack[] armor = grave.getArmor();
        int slot = graveInventory.length;
        for (ItemStack armorPiece : armor) {
            if (armorPiece != null && slot < chestInventory.getSize()) {
                chestInventory.setItem(slot++, armorPiece);
            }
        }
        
        // Store offhand
        if (grave.getOffhand() != null && slot < chestInventory.getSize()) {
            chestInventory.setItem(slot, grave.getOffhand());
        }
        
        chest.update();
    }
    
    private void removeGrave(Grave grave) {
        UUID playerId = grave.getPlayerId();
        
        // Remove from maps
        List<Grave> graves = playerGraves.get(playerId);
        if (graves != null) {
            graves.remove(grave);
            if (graves.isEmpty()) {
                playerGraves.remove(playerId);
            }
        }
        
        locationGraves.remove(grave.getLocation());
        graveIndex.remove(grave.getGraveId());
        
        // Remove physical chest
        grave.getLocation().getBlock().setType(Material.AIR);
        
        // Remove hologram
        plugin.getHologramManager().removeGraveHologram(grave.getGraveId());
        
        // Remove from database
        plugin.getDatabaseManager().removeGrave(grave.getGraveId());
    }
    
    private void removePlayerGraves(UUID playerId) {
        List<Grave> graves = playerGraves.get(playerId);
        if (graves != null) {
            for (Grave grave : new ArrayList<>(graves)) {
                removeGrave(grave);
            }
        }
    }
    
    private void removeOldestGrave(UUID playerId) {
        List<Grave> graves = playerGraves.get(playerId);
        if (graves != null && !graves.isEmpty()) {
            Grave oldest = graves.stream()
                .min(Comparator.comparing(Grave::getCreationTime))
                .orElse(null);
            
            if (oldest != null) {
                removeGrave(oldest);
            }
        }
    }
    
    public void cleanupExpiredGraves() {
        List<Grave> expiredGraves = new ArrayList<>();
        
        for (Grave grave : graveIndex.values()) {
            if (grave.isExpired()) {
                expiredGraves.add(grave);
            }
        }
        
        for (Grave grave : expiredGraves) {
            removeGrave(grave);
            
            // Notify player if online
            Player player = plugin.getServer().getPlayer(grave.getPlayerId());
            if (player != null && player.isOnline()) {
                player.sendMessage(plugin.getConfigManager().getMessage("grave-expired"));
            }
        }
    }
    
    public List<Grave> getPlayerGraves(UUID playerId) {
        return playerGraves.getOrDefault(playerId, new ArrayList<>());
    }
    
    public Grave getGraveAt(Location location) {
        return locationGraves.get(location);
    }
    
    public Grave getGrave(UUID graveId) {
        return graveIndex.get(graveId);
    }
    
    public Grave getLatestGrave(UUID playerId) {
        List<Grave> graves = getPlayerGraves(playerId);
        
        return graves.stream()
            .max(Comparator.comparing(Grave::getCreationTime))
            .orElse(null);
    }
    
    private void startParticleEffects(Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Grave grave = locationGraves.get(location);
                if (grave == null || grave.isExpired()) {
                    cancel();
                    return;
                }
                
                try {
                    Particle particle = Particle.valueOf(plugin.getConfigManager().getParticleType());
                    location.getWorld().spawnParticle(particle, location.clone().add(0.5, 1.5, 0.5), 5, 0.3, 0.3, 0.3, 0.02);
                } catch (IllegalArgumentException e) {
                    location.getWorld().spawnParticle(Particle.SOUL, location.clone().add(0.5, 1.5, 0.5), 5, 0.3, 0.3, 0.3, 0.02);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Every second
    }
    
    private void loadGravesFromDatabase() {
        // Load graves from database on startup
        plugin.getDatabaseManager().loadGraves().forEach(grave -> {
            UUID playerId = grave.getPlayerId();
            
            playerGraves.computeIfAbsent(playerId, k -> new ArrayList<>()).add(grave);
            locationGraves.put(grave.getLocation(), grave);
            graveIndex.put(grave.getGraveId(), grave);
            
            // Place physical grave
            placeGraveChest(grave.getLocation());
            
            // Store items in the chest
            storeItemsInChest(grave.getLocation(), grave);
            
            // Create hologram
            if (plugin.getConfigManager().areHologramsEnabled()) {
                plugin.getHologramManager().createGraveHologram(grave);
            }
            
            // Start particle effects
            if (plugin.getConfigManager().areParticlesEnabled()) {
                startParticleEffects(grave.getLocation());
            }
        });
    }
    
    public int getTotalActiveGraves() {
        return graveIndex.size();
    }
    
    public int getPlayerActiveGraves(UUID playerId) {
        return getPlayerGraves(playerId).size();
    }
}