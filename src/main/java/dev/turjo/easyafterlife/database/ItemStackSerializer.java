package dev.turjo.easyafterlife.database;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ItemStackSerializer {
    
    public static String serialize(ItemStack[] items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            
            dataOutput.writeInt(items.length);
            
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize ItemStack array", e);
        }
    }
    
    public static ItemStack[] deserialize(String data) {
        if (data == null || data.isEmpty()) {
            return new ItemStack[0];
        }
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            
            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];
            
            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            
            return items;
            
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize ItemStack array", e);
        }
    }
}