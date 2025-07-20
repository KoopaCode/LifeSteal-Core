package com.koopa.lifestealcore.gui;

import com.koopa.lifestealcore.LifeStealCore;
import com.koopa.lifestealcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigGUI {
    private final LifeStealCore plugin;
    private static final String GUI_TITLE = "§8LifeSteal Configuration";
    private static final String RECIPE_GUI_TITLE = "§8Revival Beacon Recipe";
    private static final String HEART_RECIPE_GUI_TITLE = "§8Heart Crafting Recipe";

    public ConfigGUI(LifeStealCore plugin) {
        this.plugin = plugin;
    }

    public void openConfigGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);

        // Fill with gray stained glass panes
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, createConfigItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        // Default Hearts Setting (Heart of the Sea)
        gui.setItem(10, createConfigItem(Material.HEART_OF_THE_SEA,
                "&c&lDefault Hearts",
                "&7Current value: &f" + plugin.getConfig().getInt("settings.default-hearts"),
                "&7",
                "&7Click to change the default",
                "&7hearts for new players"));

        // Min Hearts Setting (Redstone)
        gui.setItem(12, createConfigItem(Material.REDSTONE,
                "&c&lMinimum Hearts",
                "&7Current value: &f" + plugin.getConfig().getInt("settings.min-hearts"),
                "&7",
                "&7Click to change the minimum",
                "&7hearts a player can have"));

        // Max Hearts Setting (Diamond)
        gui.setItem(14, createConfigItem(Material.DIAMOND,
                "&c&lMaximum Hearts",
                "&7Current value: &f" + plugin.getConfig().getInt("settings.max-hearts"),
                "&7",
                "&7Click to change the maximum",
                "&7hearts a player can have"));

        // Heart Item Preview (Nether Star)
        gui.setItem(16, createConfigItem(Material.NETHER_STAR,
                plugin.getConfig().getString("settings.heart-item-name"),
                "&7",
                "&7Current heart item settings:",
                "&7" + String.join("&7", plugin.getConfig().getStringList("settings.heart-item-lore"))));

        // Heart Recipe Viewer (Crafting Table)
        gui.setItem(28, createConfigItem(Material.CRAFTING_TABLE,
                "&c&lHeart Crafting Recipe",
                "&7",
                "&7Click to view the current",
                "&7heart crafting recipe"));

        // Difficulty Selector (Netherite Ingot)
        ItemStack difficultyItem = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta diffMeta = difficultyItem.getItemMeta();
        String currentDifficulty = plugin.getConfig().getString("settings.difficulty", "MEDIUM");
        diffMeta.setDisplayName(MessageUtils.color("&c&lCrafting Difficulty"));
        diffMeta.setLore(Arrays.asList(
            MessageUtils.color("&7Current: &f" + currentDifficulty),
            MessageUtils.color("&7"),
            MessageUtils.color("&7Left-Click to change"),
            MessageUtils.color("&7Right-Click to view beacon recipe")
        ));
        difficultyItem.setItemMeta(diffMeta);
        gui.setItem(30, difficultyItem);

        // Save & Reload button (Emerald)
        gui.setItem(32, createConfigItem(Material.EMERALD,
                "&a&lSave & Reload",
                "&7",
                "&7Click to save changes",
                "&7and reload the config"));

        // Reload Recipes button (Anvil)
        gui.setItem(34, createConfigItem(Material.ANVIL,
                "&e&lReload Recipes",
                "&7",
                "&7Click to reload all",
                "&7crafting recipes"));

        player.openInventory(gui);
    }

    public void openHeartRecipeGUI(Player player, String difficulty) {
        Inventory gui = Bukkit.createInventory(null, 27, HEART_RECIPE_GUI_TITLE);
        String configPath = "settings.heart-recipe-materials." + difficulty;

        // Fill with gray stained glass panes first
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, createConfigItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        // Get recipe materials
        Material cornerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".corners"));
        Material centerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".center"));
        Material coreMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".core"));

        // Set recipe items
        ItemStack corner = createConfigItem(cornerMaterial, "&f" + cornerMaterial.name(), "&7Required: 4");
        ItemStack center = createConfigItem(centerMaterial, "&f" + centerMaterial.name(), "&7Required: 4");
        ItemStack core = createConfigItem(coreMaterial, "&f" + coreMaterial.name(), "&7Required: 1");

        // Place items in crafting grid pattern for heart recipe
        gui.setItem(3, corner);  // Top left
        gui.setItem(4, center);  // Top middle
        gui.setItem(5, corner);  // Top right
        gui.setItem(12, center); // Middle left
        gui.setItem(13, core);   // Center
        gui.setItem(14, center); // Middle right
        gui.setItem(21, corner); // Bottom left
        gui.setItem(22, center); // Bottom middle
        gui.setItem(23, corner); // Bottom right

        // Result item
        ItemStack result = createConfigItem(Material.NETHER_STAR, "&c❤ Heart", 
            "&7The result of the recipe",
            "&7in " + difficulty + " mode");
        gui.setItem(16, result);

        // Back button
        gui.setItem(18, createConfigItem(Material.ARROW, "&c&lBack to Settings",
            "&7Click to return to the settings menu"));

        player.openInventory(gui);
    }

    public void openRecipeGUI(Player player, String difficulty) {
        Inventory gui = Bukkit.createInventory(null, 27, RECIPE_GUI_TITLE);
        String configPath = "settings.recipe-materials." + difficulty;

        // Fill with gray stained glass panes first
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, createConfigItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        // Get recipe materials
        Material cornerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".top_corners"));
        Material centerMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".center_row"));
        Material skullMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".skull"));
        Material bottomMaterial = Material.valueOf(plugin.getConfig().getString(configPath + ".bottom"));

        // Set recipe items
        ItemStack corner = createConfigItem(cornerMaterial, "&f" + cornerMaterial.name(), "&7Required: 2");
        ItemStack center = createConfigItem(centerMaterial, "&f" + centerMaterial.name(), "&7Required: 3");
        ItemStack skull = createConfigItem(skullMaterial, "&f" + skullMaterial.name(), "&7Required: 1");
        ItemStack bottom = createConfigItem(bottomMaterial, "&f" + bottomMaterial.name(), "&7Required: 3");

        // Place items in crafting grid pattern (original layout)
        gui.setItem(3, corner);  // Top left
        gui.setItem(4, center);  // Top middle
        gui.setItem(5, corner);  // Top right
        gui.setItem(12, center); // Middle left
        gui.setItem(13, skull);  // Center
        gui.setItem(14, center); // Middle right
        gui.setItem(21, bottom); // Bottom left
        gui.setItem(22, bottom); // Bottom middle
        gui.setItem(23, bottom); // Bottom right

        // Result item
        ItemStack result = createConfigItem(Material.BEACON, "&c&lRevival Beacon", 
            "&7The result of the recipe",
            "&7in " + difficulty + " mode");
        gui.setItem(16, result);

        // Back button
        gui.setItem(18, createConfigItem(Material.ARROW, "&c&lBack to Settings",
            "&7Click to return to the settings menu"));

        player.openInventory(gui);
    }

    public void openCustomRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8Custom Recipe Editor");

        // Fill with gray stained glass panes
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, createConfigItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        // Create empty crafting grid
        int[] craftingSlots = {11, 12, 13, 20, 21, 22, 29, 30, 31};
        for (int slot : craftingSlots) {
            gui.setItem(slot, null);  // Empty slots for items
        }

        // Result item (Beacon)
        gui.setItem(24, createConfigItem(Material.BEACON, "&c&lRevival Beacon", 
            "&7The crafting result"));

        // Save button
        gui.setItem(49, createConfigItem(Material.EMERALD, "&a&lSave Recipe",
            "&7Click to save your custom recipe"));

        // Back button
        gui.setItem(45, createConfigItem(Material.ARROW, "&c&lBack",
            "&7Return to settings"));

        player.openInventory(gui);
    }

    private boolean isInCraftingGrid(int slot) {
        int[] craftingSlots = {11, 12, 13, 20, 21, 22, 29, 30, 31};
        for (int craftingSlot : craftingSlots) {
            if (slot == craftingSlot) return true;
        }
        return false;
    }

    private ItemStack createConfigItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color(name));
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(MessageUtils.color(line));
        }
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        return item;
    }
} 