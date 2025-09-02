package dev.turjo.easyafterlife.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class LocationUtils {
    
    public static Location findSafeLocation(Location originalLocation, int radius) {
        World world = originalLocation.getWorld();
        int centerX = originalLocation.getBlockX();
        int centerY = originalLocation.getBlockY();
        int centerZ = originalLocation.getBlockZ();
        
        // First, try the original location if it's safe
        if (isSafeLocation(originalLocation)) {
            return originalLocation;
        }
        
        // Search in expanding circles
        for (int r = 1; r <= radius; r++) {
            for (int x = centerX - r; x <= centerX + r; x++) {
                for (int z = centerZ - r; z <= centerZ + r; z++) {
                    // Only check border of the current radius
                    if (Math.abs(x - centerX) != r && Math.abs(z - centerZ) != r) {
                        continue;
                    }
                    
                    // Find safe Y level
                    for (int y = Math.max(centerY - 5, world.getMinHeight()); 
                         y <= Math.min(centerY + 10, world.getMaxHeight() - 2); y++) {
                        
                        Location testLocation = new Location(world, x, y, z);
                        if (isSafeLocation(testLocation)) {
                            return testLocation;
                        }
                    }
                }
            }
        }
        
        // If no safe location found, return original (will be handled by grave placement logic)
        return originalLocation;
    }
    
    public static boolean isSafeLocation(Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        
        Block block = location.getBlock();
        Block above = block.getRelative(0, 1, 0);
        Block below = block.getRelative(0, -1, 0);
        
        // Check if location is in void
        if (location.getY() < location.getWorld().getMinHeight()) {
            return false;
        }
        
        // Check if location is above build height
        if (location.getY() >= location.getWorld().getMaxHeight()) {
            return false;
        }
        
        // Check if current block and above are passable
        if (!isPassable(block) || !isPassable(above)) {
            return false;
        }
        
        // Check if block below is solid (or at least not dangerous)
        if (!isSolid(below) && !isDangerous(below)) {
            return false;
        }
        
        // Check for dangerous blocks nearby
        if (isDangerous(block) || isDangerous(above) || isDangerous(below)) {
            return false;
        }
        
        return true;
    }
    
    private static boolean isPassable(Block block) {
        Material type = block.getType();
        return type.isAir() || 
               type == Material.WATER || 
               type == Material.TALL_GRASS || 
               type == Material.GRASS ||
               !type.isSolid();
    }
    
    private static boolean isSolid(Block block) {
        Material type = block.getType();
        return type.isSolid() && 
               type != Material.LAVA && 
               type != Material.FIRE && 
               type != Material.MAGMA_BLOCK;
    }
    
    private static boolean isDangerous(Block block) {
        Material type = block.getType();
        return type == Material.LAVA || 
               type == Material.FIRE || 
               type == Material.MAGMA_BLOCK || 
               type == Material.CACTUS ||
               type == Material.SWEET_BERRY_BUSH ||
               type == Material.WITHER_ROSE;
    }
    
    public static String locationToString(Location location) {
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f", 
            location.getWorld().getName(),
            location.getX(),
            location.getY(), 
            location.getZ(),
            location.getYaw(),
            location.getPitch());
    }
    
    public static Location locationFromString(String locationString) {
        String[] parts = locationString.split(",");
        if (parts.length != 6) {
            return null;
        }
        
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}