package com.koopa.lifestealcore.utils;

import com.koopa.lifestealcore.LifeStealCore;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class VersionChecker {
    private final LifeStealCore plugin;
    private static final String VERSION_URL = "https://raw.githubusercontent.com/KoopaCode/LifeSteal-Core/refs/heads/main/version/versioncheck";
    private static final String SPIGOT_URL = "https://www.spigotmc.org/resources/lifesteal-core-1-13-1-20-4.121599/";
    private static final String MODRINTH_URL = "https://modrinth.com/plugin/lifestealcore";
    private static final String GITHUB_URL = "https://github.com/KoopaCode/LifeSteal-Core/releases";

    public VersionChecker(LifeStealCore plugin) {
        this.plugin = plugin;
    }

    public void checkVersion() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(VERSION_URL);
                java.net.URLConnection conn = url.openConnection();
                conn.setUseCaches(false);
                conn.addRequestProperty("User-Agent", "Mozilla/5.0");
                conn.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                conn.addRequestProperty("Pragma", "no-cache");
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String latestVersion = reader.readLine().trim();
                reader.close();

                String currentVersion = plugin.getDescription().getVersion();
                int comparison = compareVersions(currentVersion, latestVersion);
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (comparison > 0) {
                        // Running beta/development version (current > latest)
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §e§lYou are running a beta version!");
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §eCurrent version: §f" + currentVersion);
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §eLatest stable: §f" + latestVersion);
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §c⚠ Bugs may occur in this development build!");
                    } else if (comparison < 0) {
                        // Running outdated version (current < latest)
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §c§lA new version is available!");
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §cCurrent version: §f" + currentVersion);
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §cLatest version: §f" + latestVersion);
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §eDownload from:");
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §bSpigot: §f" + SPIGOT_URL);
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §bModrinth: §f" + MODRINTH_URL);
                        plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §bGitHub: §f" + GITHUB_URL);
                    } else {
                        // Versions are equal - running latest stable
                        plugin.getLogger().info("§8§l[§c§lLifeSteal§8§l] §a§lYou are running the latest stable version!");
                        plugin.getLogger().info("§8§l[§c§lLifeSteal§8§l] §aCurrent version: §f" + currentVersion);
                    }
                });
            } catch (Exception e) {
                plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §cFailed to check for updates: " + e.getMessage());
            }
        });
    }

    private int compareVersions(String version1, String version2) {
        try {
            String[] v1Parts = version1.split("\\.");
            String[] v2Parts = version2.split("\\.");
            
            // Convert to integers for proper comparison
            int major1 = Integer.parseInt(v1Parts[0]);
            int minor1 = v1Parts.length > 1 ? Integer.parseInt(v1Parts[1]) : 0;
            int patch1 = v1Parts.length > 2 ? Integer.parseInt(v1Parts[2]) : 0;
            
            int major2 = Integer.parseInt(v2Parts[0]);
            int minor2 = v2Parts.length > 1 ? Integer.parseInt(v2Parts[1]) : 0;
            int patch2 = v2Parts.length > 2 ? Integer.parseInt(v2Parts[2]) : 0;
            
            // Compare major version first
            if (major1 != major2) {
                return major1 - major2;
            }
            
            // If major versions are equal, compare minor versions
            if (minor1 != minor2) {
                return minor1 - minor2;
            }
            
            // If minor versions are equal, compare patch versions
            return patch1 - patch2;
            
        } catch (Exception e) {
            plugin.getLogger().warning("§8§l[§c§lLifeSteal§8§l] §cError parsing version numbers!");
            return 0;
        }
    }
} 