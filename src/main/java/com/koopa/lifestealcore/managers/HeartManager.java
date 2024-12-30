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
        return playerHearts.getOrDefault(player.getUniqueId(), 
            plugin.getConfig().getInt("settings.default-hearts"));
    }

    public void setPlayerHearts(Player player, int hearts) {
        playerHearts.put(player.getUniqueId(), hearts);
        updatePlayerMaxHealth(player);
    }

    public void updatePlayerMaxHealth(Player player) {
        int hearts = getPlayerHearts(player);
        player.setMaxHealth(hearts * 2);
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