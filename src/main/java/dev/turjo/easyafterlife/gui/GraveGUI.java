package dev.turjo.easyafterlife.gui;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GraveGUI implements Listener {
    
    private final EasyAfterlifePlugin plugin;
    private final Player player;
    private final Grave grave;
    private final Inventory inventory;
    
    public GraveGUI(EasyAfterlifePlugin plugin, Player player, Grave grave) {
        this.plugin = plugin;
        this.player = player;
        this.grave = grave;
        this.inventory = Bukkit.createInventory(null, 54, "§8⚰ " + grave.getPlayerName() + "'s Grave");
        
        setupInventory();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    private void setupInventory() {
        // Add border items
        ItemStack border = createBorderItem();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(45 + i, border);
        }
        for (int i = 0; i < 54; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
        
        // Add grave info
        inventory.setItem(4, createGraveInfoItem());
        
        // Add restore button
        inventory.setItem(49, createRestoreButton());
        
        // Add player's items
        ItemStack[] graveInventory = grave.getInventory();
        ItemStack[] graveArmor = grave.getArmor();
        ItemStack graveOffhand = grave.getOffhand();
        
        // Main inventory (slots 10-43, excluding borders)
        int slot = 10;
        for (int i = 0; i < graveInventory.length && slot < 44; i++) {
            if (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
                i--;
                continue;
            }
            
            if (graveInventory[i] != null) {
                inventory.setItem(slot, graveInventory[i]);
            }
            slot++;
        }
        
        // Armor display (slots 36-39)
        if (graveArmor[3] != null) inventory.setItem(36, graveArmor[3]); // Helmet
        if (graveArmor[2] != null) inventory.setItem(37, graveArmor[2]); // Chestplate
        if (graveArmor[1] != null) inventory.setItem(38, graveArmor[1]); // Leggings
        if (graveArmor[0] != null) inventory.setItem(39, graveArmor[0]); // Boots
        
        // Offhand
        if (graveOffhand != null) {
            inventory.setItem(40, graveOffhand);
        }
    }
    
    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§0");
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createGraveInfoItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6⚰ Grave Information");
            meta.setLore(Arrays.asList(
                "§7Owner: §e" + grave.getPlayerName(),
                "§7Location: §b" + grave.getLocation().getBlockX() + ", " + 
                                   grave.getLocation().getBlockY() + ", " + 
                                   grave.getLocation().getBlockZ(),
                "§7World: §a" + grave.getLocation().getWorld().getName(),
                "§7Time Left: §c" + grave.getFormattedTimeLeft(),
                "§7Experience: §d" + grave.getExperience() + " XP",
                "§8",
                "§aClick the restore button below to reclaim your items!"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createRestoreButton() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§l✓ RESTORE ITEMS");
            meta.setLore(Arrays.asList(
                "§7Click to restore all your items",
                "§7and close this grave permanently.",
                "§8",
                "§c§lWARNING: This action cannot be undone!"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker)) {
            return;
        }
        
        if (!clicker.equals(player)) {
            return;
        }
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        
        // Handle restore button click
        if (clickedItem.getType() == Material.EMERALD && 
            clickedItem.hasItemMeta() && 
            clickedItem.getItemMeta().getDisplayName().contains("RESTORE")) {
            
            // Remove the grave key from player's inventory
            plugin.getKeyManager().removeGraveKey(player, grave.getGraveId());
            
            // Unlock the grave
            boolean success = plugin.getGraveManager().unlockGrave(grave.getGraveId(), player);
            
            if (success) {
                player.sendMessage(plugin.getConfigManager().getMessage("grave-unlocked"));
                player.closeInventory();
            } else {
                player.sendMessage("§cFailed to unlock grave!");
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}