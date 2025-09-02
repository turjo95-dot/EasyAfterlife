package dev.turjo.easyafterlife.models;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Grave {
    
    private final UUID graveId;
    private final UUID playerId;
    private final String playerName;
    private final Location location;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final ItemStack offhand;
    private final int experience;
    private final long creationTime;
    private final long expirationTime;
    
    public Grave(UUID graveId, UUID playerId, String playerName, Location location, 
                 ItemStack[] inventory, ItemStack[] armor, ItemStack offhand, 
                 int experience, long creationTime, long expirationTime) {
        this.graveId = graveId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.location = location;
        this.inventory = inventory;
        this.armor = armor;
        this.offhand = offhand;
        this.experience = experience;
        this.creationTime = creationTime;
        this.expirationTime = expirationTime;
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
    
    public Location getLocation() {
        return location;
    }
    
    public ItemStack[] getInventory() {
        return inventory;
    }
    
    public ItemStack[] getArmor() {
        return armor;
    }
    
    public ItemStack getOffhand() {
        return offhand;
    }
    
    public int getExperience() {
        return experience;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public long getExpirationTime() {
        return expirationTime;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
    
    public long getTimeLeft() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }
    
    public String getFormattedTimeLeft() {
        long timeLeft = getTimeLeft() / 1000; // Convert to seconds
        
        if (timeLeft <= 0) {
            return "Expired";
        }
        
        long hours = timeLeft / 3600;
        long minutes = (timeLeft % 3600) / 60;
        long seconds = timeLeft % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}