package com.koopa.lifestealcore.managers;

import com.koopa.lifestealcore.LifeStealCore;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.*;
import org.bukkit.Sound;
import com.koopa.lifestealcore.utils.MessageUtils;
import org.bukkit.OfflinePlayer;

public class BanManager {
    private final LifeStealCore plugin;
    private final Map<UUID, Boolean> bannedPlayers = new HashMap<>();
    private final Map<UUID, Location> revivalLocations = new HashMap<>();
    private final File banFile;
    private YamlConfiguration banData;

    public BanManager(LifeStealCore plugin) {
        this.plugin = plugin;
        this.banFile = new File(plugin.getDataFolder(), "bans.yml");
        loadBanData();
    }

    private void loadBanData() {
        if (!banFile.exists()) {
            try {
                banFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        banData = YamlConfiguration.loadConfiguration(banFile);
    }

    public void saveBanData() {
        try {
            banData.save(banFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void banPlayer(Player player) {
        banPlayer(player, false);
    }

    public void banPlayer(Player player, boolean debugMode) {
        bannedPlayers.put(player.getUniqueId(), true);
        
        // Save to ban data
        banData.set(player.getUniqueId().toString() + ".name", player.getName());
        banData.set(player.getUniqueId().toString() + ".banTime", System.currentTimeMillis());
        saveBanData();

        if (!debugMode) {
            // Ban the player
            Bukkit.getBanList(BanList.Type.NAME).addBan(
                player.getName(),
                "§c§lYou died! §7Get someone to revive you with a Revival Beacon!",
                null,
                "LifeSteal System"
            );
            player.kickPlayer("§c§lYou died! §7Get someone to revive you with a Revival Beacon!");
        }
    }

    public void revivePlayer(String playerName, Location beaconLoc) {
        UUID uuid = null;
        for (String key : banData.getKeys(false)) {
            if (banData.getString(key + ".name").equalsIgnoreCase(playerName)) {
                uuid = UUID.fromString(key);
                break;
            }
        }
        
        if (uuid != null) {
            bannedPlayers.remove(uuid);
            revivalLocations.put(uuid, beaconLoc);
            banData.set(uuid.toString(), null);
            saveBanData();
        }
        
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);
    }

    public void revivePlayer(String playerName) {
        revivePlayer(playerName, null);
    }

    public List<String> getBannedPlayers() {
        List<String> players = new ArrayList<>();
        for (String key : banData.getKeys(false)) {
            players.add(banData.getString(key + ".name"));
        }
        return players;
    }

    public Location getRevivalLocation(UUID uuid) {
        Location loc = revivalLocations.get(uuid);
        revivalLocations.remove(uuid);
        return loc;
    }

    public boolean isPlayerBanned(UUID uuid) {
        return bannedPlayers.getOrDefault(uuid, false);
    }

    public void banPlayer(Player player, Player killer) {
        // Play dramatic sound to all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
        }

        // Dramatic ban message
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MessageUtils.color("&8&l[&c&l!!!&8&l] &c&lA PLAYER HAS FALLEN &8&l[&c&l!!!&8&l]"));
        Bukkit.broadcastMessage(MessageUtils.color("&7" + player.getName() + " &8has lost their final heart..."));
        if (killer != null) {
            Bukkit.broadcastMessage(MessageUtils.color("&7Slain by the hands of &c" + killer.getName()));
        }
        Bukkit.broadcastMessage(MessageUtils.color("&c&lBANISHED TO THE SHADOW REALM"));
        Bukkit.broadcastMessage("");

        // Ban the player
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.kickPlayer(MessageUtils.color(
                "&c&lYOU HAVE BEEN BANISHED!\n\n" +
                "&7You have lost all your hearts...\n" +
                "&7Find a Revival Beacon to return!"
            ));
            plugin.getConfig().set("banned-players." + player.getUniqueId(), true);
            plugin.saveConfig();
        }, 2L); // Small delay for dramatic effect
    }

    public void unbanPlayer(Player revivedBy, String playerName) {
        // Get the banned player's UUID
        UUID bannedUUID = null;
        String bannedName = playerName;
        
        for (String uuidStr : plugin.getConfig().getConfigurationSection("banned-players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                bannedUUID = uuid;
                bannedName = offlinePlayer.getName();
                break;
            }
        }

        if (bannedUUID != null) {
            // Play dramatic revival sound
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
            }

            // Dramatic revival message
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(MessageUtils.color("&8&l[&e&l!!!&8&l] &e&lA SOUL HAS BEEN REVIVED &8&l[&e&l!!!&8&l]"));
            Bukkit.broadcastMessage(MessageUtils.color("&7Through the power of the Revival Beacon..."));
            Bukkit.broadcastMessage(MessageUtils.color("&e" + bannedName + " &7has been given another chance!"));
            Bukkit.broadcastMessage(MessageUtils.color("&7Restored to life by &e" + revivedBy.getName()));
            Bukkit.broadcastMessage("");

            // Unban the player
            plugin.getConfig().set("banned-players." + bannedUUID, null);
            plugin.saveConfig();

            // Reset their hearts to default
            plugin.getHeartManager().setHearts(bannedUUID, 
                plugin.getConfig().getInt("settings.default-hearts", 10));
        }
    }
} 