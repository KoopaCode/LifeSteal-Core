package com.koopa.lifestealcore.listeners;

import com.koopa.lifestealcore.LifeStealCore;
import com.koopa.lifestealcore.gui.RevivalGUI;
import com.koopa.lifestealcore.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class BeaconListener implements Listener {
    private final LifeStealCore plugin;
    private Location lastBeaconLocation;

    public BeaconListener(LifeStealCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBeaconPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.BEACON || !item.hasItemMeta() || 
            !item.getItemMeta().getDisplayName().equals(MessageUtils.color("&c&lRevival Beacon"))) {
            return;
        }

        lastBeaconLocation = event.getBlock().getLocation();
        new RevivalGUI(lastBeaconLocation).openGUI(event.getPlayer());
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§c§lRevive Banned Player")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        // If it's the "no players" barrier, just return
        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Handle player head clicks
        if (clicked.getType() == Material.PLAYER_HEAD) {
            String playerName = clicked.getItemMeta().getDisplayName().substring(2); // Remove color code
            
            // Remove the beacon block
            if (lastBeaconLocation != null) {
                Block beacon = lastBeaconLocation.getBlock();
                if (beacon.getType() == Material.BEACON) {
                    beacon.setType(Material.AIR);
                }
            }

            plugin.getBanManager().revivePlayer(playerName, lastBeaconLocation);
            player.sendMessage(MessageUtils.color("&aPlayer has been revived!"));
            player.closeInventory();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location revivalLoc = plugin.getBanManager().getRevivalLocation(player.getUniqueId());
        
        if (revivalLoc != null) {
            player.teleport(revivalLoc);
            plugin.getHeartManager().setPlayerHearts(player, 5);
            player.sendMessage(MessageUtils.color("&aYou have been revived! You have 5 hearts."));
        }
    }

    // Cancel right-clicking the beacon to prevent the vanilla beacon GUI
    @EventHandler
    public void onBeaconInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.BEACON) return;

        // Check if it's our revival beacon by location
        if (lastBeaconLocation != null && lastBeaconLocation.equals(event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
} 