package com.koopa.lifestealcore.managers;

import com.koopa.lifestealcore.LifeStealCore;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;

public class HeartManager {
    private final LifeStealCore plugin;
    private final Map<UUID, Integer> playerHearts;
    private final File dataFile;
    private FileConfiguration data;

    public HeartManager(LifeStealCore plugin) {
        this.plugin = plugin;
        this.playerHearts = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "hearts.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                data = new YamlConfiguration();
                data.save(dataFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Could not create hearts.yml!");
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load all saved hearts into memory
        for (String uuidString : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                int hearts = data.getInt(uuidString);
                playerHearts.put(uuid, hearts);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in hearts.yml: " + uuidString);
            }
        }
    }

    public void saveAllData() {
        if (data == null) return;
        
        for (Map.Entry<UUID, Integer> entry : playerHearts.entrySet()) {
            data.set(entry.getKey().toString(), entry.getValue());
        }
        
        try {
            data.save(dataFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save hearts data!");
            e.printStackTrace();
        }
    }

    public int getPlayerHearts(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerHearts.containsKey(uuid)) {
            // If player doesn't have hearts saved, give them default hearts
            int defaultHearts = plugin.getConfig().getInt("settings.default-hearts");
            playerHearts.put(uuid, defaultHearts);
            savePlayerHearts(uuid);
        }
        return playerHearts.get(uuid);
    }

    public void setPlayerHearts(Player player, int hearts) {
        UUID uuid = player.getUniqueId();
        // Ensure minimum of 1 health for game mechanics
        hearts = Math.max(0, hearts);
        playerHearts.put(uuid, hearts);
        savePlayerHearts(uuid);
        
        if (hearts > 0) {
            updatePlayerMaxHealth(player);
        }
    }

    private void savePlayerHearts(UUID uuid) {
        // Save to hearts.yml immediately when changed
        data.set(uuid.toString(), playerHearts.get(uuid));
        try {
            data.save(dataFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save hearts data!");
            e.printStackTrace();
        }
    }

    public void updatePlayerMaxHealth(Player player) {
        int hearts = getPlayerHearts(player);
        // Minimum of 1 health (0.5 hearts) for the game
        player.setMaxHealth(Math.max(1, hearts * 2));
    }

    public void setHearts(UUID uuid, int hearts) {
        // Ensure hearts are within configured limits
        int minHearts = plugin.getConfig().getInt("settings.min-hearts", 1);
        int maxHearts = plugin.getConfig().getInt("settings.max-hearts", 20);
        
        hearts = Math.max(minHearts, Math.min(maxHearts, hearts));
        
        // Update hearts in memory and config
        playerHearts.put(uuid, hearts);
        plugin.getConfig().set("player-hearts." + uuid, hearts);
        plugin.saveConfig();

        // Update player's max health if they're online
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.setMaxHealth(hearts * 2);
            player.setHealth(player.getMaxHealth());
        }
    }
} 