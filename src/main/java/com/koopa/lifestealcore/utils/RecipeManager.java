package com.koopa.lifestealcore.utils;

import com.koopa.lifestealcore.LifeStealCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.RecipeChoice;

import java.util.Arrays;

public class RecipeManager {
    private final LifeStealCore plugin;
    private static final NamespacedKey REVIVAL_BEACON_KEY = new NamespacedKey(LifeStealCore.getInstance(), "revival_beacon");
    private static final NamespacedKey HEART_CRAFT_KEY = new NamespacedKey(LifeStealCore.getInstance(), "heart_craft");

    public RecipeManager(LifeStealCore plugin) {
        this.plugin = plugin;
    }

    public void registerRecipes() {
        // Remove existing recipes first
        removeExistingRecipes();
        
        // Register heart crafting recipe
        registerHeartRecipe();
        
        // Register revival beacon recipe
        registerRevivalBeaconRecipe();
        
        // Log successful registration
        plugin.getLogger().info("Successfully registered LifeSteal recipes!");
    }

    private void removeExistingRecipes() {
        // Remove existing recipes to prevent conflicts
        Bukkit.removeRecipe(new NamespacedKey(plugin, "revival_beacon"));
        Bukkit.removeRecipe(new NamespacedKey(plugin, "heart_craft"));
    }

    private void registerHeartRecipe() {
        String difficulty = plugin.getConfig().getString("settings.difficulty", "MEDIUM");
        String configPath = "settings.heart-recipe-materials." + difficulty;

        // Create heart crafting recipe based on difficulty
        ShapedRecipe heartRecipe = new ShapedRecipe(new NamespacedKey(plugin, "heart_craft"), createHeartItem(1));
        
        switch (difficulty) {
            case "EASY":
                heartRecipe.shape("GDG", "DRD", "GDG");
                heartRecipe.setIngredient('G', Material.GOLD_INGOT);
                heartRecipe.setIngredient('D', Material.DIAMOND);
                heartRecipe.setIngredient('R', Material.REDSTONE);
                break;
            case "MEDIUM":
                heartRecipe.shape("NEN", "ERE", "NEN");
                heartRecipe.setIngredient('N', Material.NETHERITE_INGOT);
                heartRecipe.setIngredient('E', Material.EMERALD);
                heartRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
                break;
            case "HARD":
                heartRecipe.shape("DND", "NRN", "DND");
                heartRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
                heartRecipe.setIngredient('N', Material.NETHERITE_BLOCK);
                heartRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
                break;
            case "CUSTOM":
                // Use custom materials from config
                Material cornerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".corners", "DIAMOND"));
                Material centerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".center", "EMERALD"));
                Material coreMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".core", "REDSTONE_BLOCK"));
                
                heartRecipe.shape("CEC", "ERE", "CEC");
                heartRecipe.setIngredient('C', cornerMaterial);
                heartRecipe.setIngredient('E', centerMaterial);
                heartRecipe.setIngredient('R', coreMaterial);
                break;
        }

        Bukkit.addRecipe(heartRecipe);
        plugin.getLogger().info("Registered heart crafting recipe for difficulty: " + difficulty);
    }

    private void registerRevivalBeaconRecipe() {
        String difficulty = plugin.getConfig().getString("settings.difficulty", "MEDIUM");
        String configPath = "settings.recipe-materials." + difficulty;

        // Create revival beacon with proper return item
        ItemStack revivalBeacon = createRevivalBeacon();
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "revival_beacon"), revivalBeacon);
        recipe.shape("CCC", "CSC", "BBB");

        Material cornerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".top_corners"));
        Material centerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".center_row"));
        Material skullMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".skull"));
        Material bottomMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".bottom"));

        recipe.setIngredient('C', cornerMaterial);
        recipe.setIngredient('S', skullMaterial);
        recipe.setIngredient('B', bottomMaterial);

        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("Registered revival beacon recipe for difficulty: " + difficulty);
    }

    public static ItemStack createRevivalBeacon() {
        ItemStack beacon = new ItemStack(Material.BEACON);
        ItemMeta meta = beacon.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&c&lRevival Beacon"));
        meta.setLore(Arrays.asList(
            MessageUtils.color("&7Use this beacon to revive"),
            MessageUtils.color("&7a banned player!")
        ));
        beacon.setItemMeta(meta);
        return beacon;
    }

    public static ItemStack createHeartItem(int amount) {
        return ItemManager.createHeartItem(amount);
    }
} 