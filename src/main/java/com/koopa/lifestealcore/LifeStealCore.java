package com.koopa.lifestealcore;

import org.bukkit.plugin.java.JavaPlugin;
import com.koopa.lifestealcore.commands.LifeStealCommand;
import com.koopa.lifestealcore.listeners.PlayerListener;
import com.koopa.lifestealcore.managers.HeartManager;
import com.koopa.lifestealcore.listeners.GUIListener;
import com.koopa.lifestealcore.managers.BanManager;
import com.koopa.lifestealcore.utils.RecipeManager;
import com.koopa.lifestealcore.listeners.BeaconListener;
import com.koopa.lifestealcore.utils.VersionChecker;
import com.koopa.lifestealcore.utils.VersionSupport;
import org.bukkit.Bukkit;

public class LifeStealCore extends JavaPlugin {
    private HeartManager heartManager;
    private BanManager banManager;
    private static LifeStealCore instance;

    @Override
    public void onEnable() {
        instance = this;

        // Check version compatibility
        if (!VersionSupport.isSupported()) {
            getLogger().severe("§8§l[§c§lLifeSteal§8§l] §cThis plugin requires Minecraft 1.13 or newer!");
            getLogger().severe("§8§l[§c§lLifeSteal§8§l] §cDisabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Log server version info
        getLogger().info("§8§l[§c§lLifeSteal§8§l] §aDetected server version: " + VersionSupport.getServerVersion());
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        heartManager = new HeartManager(this);
        banManager = new BanManager(this);
        
        // Register recipes
        new RecipeManager(this).registerRecipes();
        
        // Register commands
        getCommand("lifesteal").setExecutor(new LifeStealCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new BeaconListener(this), this);
        
        // Add version checker
        new VersionChecker(this).checkVersion();
        
        getLogger().info("LifeStealCore has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all player data
        heartManager.saveAllData();
        getLogger().info("LifeStealCore has been disabled!");
    }

    public HeartManager getHeartManager() {
        return heartManager;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public static LifeStealCore getInstance() {
        return instance;
    }
} 