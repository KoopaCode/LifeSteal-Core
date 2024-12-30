package com.koopa.lifestealcore.utils;

import com.koopa.lifestealcore.LifeStealCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.RecipeChoice;

import java.util.Arrays;

public class RecipeManager {
    private final LifeStealCore plugin;
    private static final NamespacedKey REVIVAL_BEACON_KEY = new NamespacedKey(LifeStealCore.getInstance(), "revival_beacon");

    public RecipeManager(LifeStealCore plugin) {
        this.plugin = plugin;
    }

    public void registerRecipes() {
        String difficulty = plugin.getConfig().getString("settings.difficulty", "MEDIUM");
        String configPath = "settings.recipe-materials." + difficulty;

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "revival_beacon"), new ItemStack(Material.BEACON));
        recipe.shape("CCC", "CSC", "BBB");

        Material cornerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".top_corners"));
        Material centerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".center_row"));
        Material skullMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".skull"));
        Material bottomMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".bottom"));

        recipe.setIngredient('C', cornerMaterial);
        recipe.setIngredient('S', skullMaterial);
        recipe.setIngredient('B', bottomMaterial);

        Bukkit.addRecipe(recipe);
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
} 