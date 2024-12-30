package com.koopa.lifestealcore.gui;

import com.koopa.lifestealcore.LifeStealCore;
import com.koopa.lifestealcore.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.Arrays;
import java.util.List;

public class RevivalGUI {
    private static final String GUI_TITLE = "§c§lRevive Banned Player";
    private final Location beaconLocation;

    public RevivalGUI(Location beaconLocation) {
        this.beaconLocation = beaconLocation;
    }

    public void openGUI(Player player) {
        List<String> bannedPlayers = LifeStealCore.getInstance().getBanManager().getBannedPlayers();
        
        // Always create at least a 9-slot inventory
        int size = bannedPlayers.isEmpty() ? 9 : Math.min(((bannedPlayers.size() + 8) / 9) * 9, 54);
        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        if (bannedPlayers.isEmpty()) {
            // Add an item to show there are no banned players
            ItemStack noPlayers = new ItemStack(Material.BARRIER);
            ItemMeta meta = noPlayers.getItemMeta();
            meta.setDisplayName(MessageUtils.color("&cNo banned players!"));
            meta.setLore(Arrays.asList(
                MessageUtils.color("&7There are currently no"),
                MessageUtils.color("&7banned players to revive.")
            ));
            noPlayers.setItemMeta(meta);
            gui.setItem(4, noPlayers);
        } else {
            // Add banned player heads
            for (String playerName : bannedPlayers) {
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setDisplayName(MessageUtils.color("&c" + playerName));
                meta.setLore(Arrays.asList(
                    MessageUtils.color("&7Click to revive this player"),
                    MessageUtils.color("&7They will spawn at this beacon")
                ));
                meta.setOwner(playerName);
                skull.setItemMeta(meta);
                gui.addItem(skull);
            }
        }

        player.openInventory(gui);
    }
} 