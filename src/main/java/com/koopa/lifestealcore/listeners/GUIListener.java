package com.koopa.lifestealcore.listeners;

import com.koopa.lifestealcore.LifeStealCore;
import com.koopa.lifestealcore.gui.ConfigGUI;
import com.koopa.lifestealcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class GUIListener implements Listener {
    private final LifeStealCore plugin;
    private static final String GUI_TITLE = "§8LifeSteal Configuration";
    private final HashMap<UUID, String> editingPlayers = new HashMap<>();
    private final HashMap<UUID, String> materialEditing = new HashMap<>();

    public GUIListener(LifeStealCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        // Handle Config GUI
        if (title.equals("§8LifeSteal Configuration")) {
            event.setCancelled(true);
            handleConfigGUI(event);
            return;
        }
        
        // Handle Recipe View GUI
        if (title.equals("§8Revival Beacon Recipe")) {
            event.setCancelled(true);
            if (event.getSlot() == 18) { // Back button
                new ConfigGUI(plugin).openConfigGUI((Player) event.getWhoClicked());
            }
            return;
        }

        // Handle Custom Recipe Editor
        if (title.equals("§8Custom Recipe Editor")) {
            handleCustomRecipeEditor(event);
            return;
        }
    }

    private void handleCustomRecipeEditor(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.hasPermission("lifesteal.admin.config")) return;

        int slot = event.getSlot();
        int[] craftingSlots = {11, 12, 13, 20, 21, 22, 29, 30, 31};

        // Allow clicking in crafting grid
        if (isInArray(slot, craftingSlots)) {
            event.setCancelled(false);
            return;
        }

        // Handle save button
        if (slot == 49) {
            event.setCancelled(true);
            saveCustomRecipe(event.getInventory());
            player.sendMessage(MessageUtils.color("&aCustom recipe saved!"));
            new ConfigGUI(plugin).openConfigGUI(player);
            return;
        }

        // Handle back button
        if (slot == 45) {
            event.setCancelled(true);
            new ConfigGUI(plugin).openConfigGUI(player);
            return;
        }

        // Cancel all other clicks in the GUI
        if (event.getClickedInventory() != null && 
            event.getView().getTitle().equals("§8Custom Recipe Editor")) {
            event.setCancelled(true);
        }
    }

    private boolean isInArray(int value, int[] array) {
        for (int i : array) {
            if (i == value) return true;
        }
        return false;
    }

    private ItemStack createEmptySlot() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&7Click to set item"));
        item.setItemMeta(meta);
        return item;
    }

    private void handleConfigGUI(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.hasPermission("lifesteal.admin.config")) return;

        switch (event.getSlot()) {
            case 11 -> promptForValue(player, "default-hearts", "Enter new default hearts value:");
            case 13 -> promptForValue(player, "min-hearts", "Enter new minimum hearts value:");
            case 15 -> promptForValue(player, "max-hearts", "Enter new maximum hearts value:");
            case 23 -> handleDifficultyClick(event);
            case 31 -> {
                plugin.reloadConfig();
                player.sendMessage(MessageUtils.color("&aConfiguration reloaded!"));
                player.closeInventory();
            }
        }
    }

    private void handleDifficultyClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.isRightClick()) {
            if (plugin.getConfig().getString("settings.difficulty").equals("CUSTOM")) {
                new ConfigGUI(plugin).openCustomRecipeGUI(player);
            } else {
                String currentDiff = plugin.getConfig().getString("settings.difficulty", "MEDIUM");
                new ConfigGUI(plugin).openRecipeGUI(player, currentDiff);
            }
        } else if (event.isLeftClick()) {
            String currentDiff = plugin.getConfig().getString("settings.difficulty", "MEDIUM");
            String newDiff = switch (currentDiff) {
                case "EASY" -> "MEDIUM";
                case "MEDIUM" -> "HARD";
                case "HARD" -> "CUSTOM";
                case "CUSTOM" -> "EASY";
                default -> "MEDIUM";
            };
            plugin.getConfig().set("settings.difficulty", newDiff);
            plugin.saveConfig();
            new ConfigGUI(plugin).openConfigGUI(player);
            player.sendMessage(MessageUtils.color("&aSet crafting difficulty to: " + newDiff));
        }
    }

    private void saveCustomRecipe(Inventory gui) {
        String configPath = "settings.recipe-materials.CUSTOM";
        
        // Get materials from crafting grid
        ItemStack cornerItem = gui.getItem(11);
        ItemStack centerItem = gui.getItem(12);
        ItemStack skullItem = gui.getItem(21);
        ItemStack bottomItem = gui.getItem(29);

        // Save to config (checking for null items)
        plugin.getConfig().set(configPath + ".top_corners", 
            cornerItem != null ? cornerItem.getType().name() : "GLASS");
        plugin.getConfig().set(configPath + ".center_row", 
            centerItem != null ? centerItem.getType().name() : "DIAMOND");
        plugin.getConfig().set(configPath + ".skull", 
            skullItem != null ? skullItem.getType().name() : "WITHER_SKELETON_SKULL");
        plugin.getConfig().set(configPath + ".bottom", 
            bottomItem != null ? bottomItem.getType().name() : "OBSIDIAN");
        plugin.saveConfig();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("§8Custom Recipe Editor")) {
            event.getPlayer().setItemOnCursor(null);
        }
    }

    private void promptForValue(Player player, String setting, String prompt) {
        player.closeInventory();
        player.sendMessage(MessageUtils.color(prompt));
        editingPlayers.put(player.getUniqueId(), "settings." + setting);
    }

    private void promptForMaterial(Player player, String part, String prompt) {
        player.closeInventory();
        player.sendMessage(MessageUtils.color(prompt));
        materialEditing.put(player.getUniqueId(), "settings.recipe-materials.CUSTOM." + part);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String settingValue = editingPlayers.get(player.getUniqueId());
        String settingMaterial = materialEditing.get(player.getUniqueId());
        
        if (settingValue != null) {
            event.setCancelled(true);
            try {
                int value = Integer.parseInt(event.getMessage());
                plugin.getConfig().set(settingValue, value);
                plugin.saveConfig();
                player.sendMessage(MessageUtils.color("&aValue updated!"));
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    new ConfigGUI(plugin).openConfigGUI(player);
                });
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.color("&cPlease enter a valid number!"));
            }
            editingPlayers.remove(player.getUniqueId());
        }
        else if (settingMaterial != null) {
            event.setCancelled(true);
            if (event.getMessage().equalsIgnoreCase("cancel")) {
                player.sendMessage(MessageUtils.color("&cCancelled material selection."));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    new ConfigGUI(plugin).openCustomRecipeGUI(player);
                });
            } else {
                try {
                    Material material = Material.valueOf(event.getMessage().toUpperCase());
                    plugin.getConfig().set(settingMaterial, material.name());
                    plugin.saveConfig();
                    player.sendMessage(MessageUtils.color("&aMaterial updated!"));
                    
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        new ConfigGUI(plugin).openCustomRecipeGUI(player);
                    });
                } catch (IllegalArgumentException e) {
                    player.sendMessage(MessageUtils.color("&cInvalid material! Try again or type 'cancel'"));
                }
            }
            materialEditing.remove(player.getUniqueId());
        }
    }

    private ItemStack createConfigItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color(name));
        item.setItemMeta(meta);
        return item;
    }
} 