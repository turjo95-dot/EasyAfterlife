package dev.turjo.easyafterlife.database;

import dev.turjo.easyafterlife.EasyAfterlifePlugin;
import dev.turjo.easyafterlife.models.Grave;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {
    
    private final EasyAfterlifePlugin plugin;
    private Connection connection;
    
    public DatabaseManager(EasyAfterlifePlugin plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            String dbType = plugin.getConfigManager().getDatabaseType();
            
            if (dbType.equalsIgnoreCase("MYSQL")) {
                setupMySQL();
            } else {
                setupSQLite();
            }
            
            createTables();
            plugin.getLogger().info("✅ Database initialized successfully!");
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database!", e);
        }
    }
    
    private void setupSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/graves.db";
        connection = DriverManager.getConnection(url);
    }
    
    private void setupMySQL() throws SQLException {
        String host = plugin.getConfigManager().getMySQLHost();
        int port = plugin.getConfigManager().getMySQLPort();
        String database = plugin.getConfigManager().getMySQLDatabase();
        String username = plugin.getConfigManager().getMySQLUsername();
        String password = plugin.getConfigManager().getMySQLPassword();
        
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true", 
            host, port, database);
        
        connection = DriverManager.getConnection(url, username, password);
    }
    
    private void createTables() throws SQLException {
        String createGravesTable = """
            CREATE TABLE IF NOT EXISTS graves (
                grave_id VARCHAR(36) PRIMARY KEY,
                player_id VARCHAR(36) NOT NULL,
                player_name VARCHAR(16) NOT NULL,
                world VARCHAR(50) NOT NULL,
                x DOUBLE NOT NULL,
                y DOUBLE NOT NULL,
                z DOUBLE NOT NULL,
                yaw FLOAT DEFAULT 0,
                pitch FLOAT DEFAULT 0,
                inventory TEXT,
                armor TEXT,
                offhand TEXT,
                experience INTEGER DEFAULT 0,
                creation_time BIGINT NOT NULL,
                expiration_time BIGINT NOT NULL
            )
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(createGravesTable)) {
            stmt.execute();
        }
    }
    
    public void saveGrave(Grave grave) {
        String sql = """
            INSERT OR REPLACE INTO graves 
            (grave_id, player_id, player_name, world, x, y, z, yaw, pitch, 
             inventory, armor, offhand, experience, creation_time, expiration_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Location loc = grave.getLocation();
            
            stmt.setString(1, grave.getGraveId().toString());
            stmt.setString(2, grave.getPlayerId().toString());
            stmt.setString(3, grave.getPlayerName());
            stmt.setString(4, loc.getWorld().getName());
            stmt.setDouble(5, loc.getX());
            stmt.setDouble(6, loc.getY());
            stmt.setDouble(7, loc.getZ());
            stmt.setFloat(8, loc.getYaw());
            stmt.setFloat(9, loc.getPitch());
            stmt.setString(10, ItemStackSerializer.serialize(grave.getInventory()));
            stmt.setString(11, ItemStackSerializer.serialize(grave.getArmor()));
            stmt.setString(12, ItemStackSerializer.serialize(new ItemStack[]{grave.getOffhand()}));
            stmt.setInt(13, grave.getExperience());
            stmt.setLong(14, grave.getCreationTime());
            stmt.setLong(15, grave.getExpirationTime());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save grave to database!", e);
        }
    }
    
    public void removeGrave(UUID graveId) {
        String sql = "DELETE FROM graves WHERE grave_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, graveId.toString());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to remove grave from database!", e);
        }
    }
    
    public List<Grave> loadGraves() {
        List<Grave> graves = new ArrayList<>();
        String sql = "SELECT * FROM graves WHERE expiration_time > ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    graves.add(createGraveFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load graves from database!", e);
        }
        
        return graves;
    }
    
    private Grave createGraveFromResultSet(ResultSet rs) throws SQLException {
        UUID graveId = UUID.fromString(rs.getString("grave_id"));
        UUID playerId = UUID.fromString(rs.getString("player_id"));
        String playerName = rs.getString("player_name");
        
        // Reconstruct location
        World world = Bukkit.getWorld(rs.getString("world"));
        if (world == null) {
            throw new SQLException("World not found: " + rs.getString("world"));
        }
        
        Location location = new Location(world, 
            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
            rs.getFloat("yaw"), rs.getFloat("pitch"));
        
        // Deserialize items
        ItemStack[] inventory = ItemStackSerializer.deserialize(rs.getString("inventory"));
        ItemStack[] armor = ItemStackSerializer.deserialize(rs.getString("armor"));
        ItemStack[] offhandArray = ItemStackSerializer.deserialize(rs.getString("offhand"));
        ItemStack offhand = (offhandArray.length > 0) ? offhandArray[0] : null;
        
        int experience = rs.getInt("experience");
        long creationTime = rs.getLong("creation_time");
        long expirationTime = rs.getLong("expiration_time");
        
        return new Grave(graveId, playerId, playerName, location, inventory, armor, offhand, 
                        experience, creationTime, expirationTime);
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close database connection!", e);
            }
        }
    }
}