package dev.turjo.easyafterlife.listeners;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import dev.turjo.easyafterlife.models.GraveKey;
import dev.turjo.easyafterlife.gui.GraveGUI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerInteractListener implements Listener {
    
    private final EasyAfterlifePlugin plugin;
    
    public PlayerInteractListener(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if player is using a grave key
        if (GraveKey.isGraveKey(item)) {
            event.setCancelled(true);
            handleGraveKeyUsage(player, item);
            return;
        }
        
        // Check if player is interacting with a grave chest
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && isGraveChest(block)) {
                event.setCancelled(true);
                handleGraveChestInteraction(player, block);
            }
        }
    }
    
    private void handleGraveKeyUsage(Player player, ItemStack key) {
        UUID graveId = GraveKey.getGraveIdFromKey(key);
        
        if (graveId == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-key"));
            return;
        }
        
        Grave grave = plugin.getGraveManager().getGrave(graveId);
        if (grave == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-grave"));
            return;
        }
        
        // Check if player owns the grave (unless raiding is allowed)
        if (!plugin.getConfigManager().isRaidingAllowed() && 
            !grave.getPlayerId().equals(player.getUniqueId())) {
            player.sendMessage("§cThis grave doesn't belong to you!");
            return;
        }
        
        // Check if grave is expired
        if (grave.isExpired()) {
            player.sendMessage(plugin.getConfigManager().getMessage("grave-expired"));
            return;
        }
        
        // Open grave GUI
        new GraveGUI(plugin, player, grave).open();
    }
    
    private void handleGraveChestInteraction(Player player, Block block) {
        Grave grave = plugin.getGraveManager().getGraveAt(block.getLocation());
        
        if (grave == null) {
            return;
        }
        
        // Check if player has the key
        if (!plugin.getKeyManager().hasGraveKey(player, grave.getGraveId())) {
            player.sendMessage("§cYou need the grave key to access this grave!");
            return;
        }
        
        // Check if player owns the grave (unless raiding is allowed)
        if (!plugin.getConfigManager().isRaidingAllowed() && 
            !grave.getPlayerId().equals(player.getUniqueId())) {
            player.sendMessage("§cThis grave doesn't belong to you!");
            return;
        }
        
        // Open grave GUI
        new GraveGUI(plugin, player, grave).open();
    }
    
    private boolean isGraveChest(Block block) {
        Material chestType = plugin.getConfigManager().getChestType();
        return block.getType() == chestType && 
               plugin.getGraveManager().getGraveAt(block.getLocation()) != null;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Grave grave = plugin.getGraveManager().getGraveAt(block.getLocation());
        
        if (grave != null) {
            Player player = event.getPlayer();
            
            // Check if grave breaking is allowed
            if (!plugin.getConfigManager().isGraveBreakingAllowed()) {
                event.setCancelled(true);
                player.sendMessage("§cGraves are protected and cannot be broken!");
                return;
            }
            
            // Check if player owns the grave
            if (!grave.getPlayerId().equals(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot break someone else's grave!");
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> 
            plugin.getGraveManager().getGraveAt(block.getLocation()) != null);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> 
            plugin.getGraveManager().getGraveAt(block.getLocation()) != null);
    }
}