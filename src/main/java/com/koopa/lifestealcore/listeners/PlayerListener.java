package com.koopa.lifestealcore.listeners;

import com.koopa.lifestealcore.LifeStealCore;
import com.koopa.lifestealcore.utils.ItemManager;
import com.koopa.lifestealcore.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {
    private final LifeStealCore plugin;

    public PlayerListener(LifeStealCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getHeartManager().updatePlayerMaxHealth(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && killer != victim) {
            int victimHearts = plugin.getHeartManager().getPlayerHearts(victim);
            int killerHearts = plugin.getHeartManager().getPlayerHearts(killer);
            
            if (victimHearts > plugin.getConfig().getInt("settings.min-hearts")) {
                plugin.getHeartManager().setPlayerHearts(victim, victimHearts - 1);
                plugin.getHeartManager().setPlayerHearts(killer, killerHearts + 1);
                killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            }
        }

        // Ban the player
        plugin.getBanManager().banPlayer(victim);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if right-clicking with a heart item
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) 
                && ItemManager.isHeartItem(item)) {
            event.setCancelled(true);
            
            int currentHearts = plugin.getHeartManager().getPlayerHearts(player);
            int maxHearts = plugin.getConfig().getInt("settings.max-hearts");

            if (currentHearts >= maxHearts) {
                player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.maximum-hearts-reached")
                        .replace("%max%", String.valueOf(maxHearts))));
                return;
            }

            // Remove one heart item and add to player's health
            item.setAmount(item.getAmount() - 1);
            plugin.getHeartManager().setPlayerHearts(player, currentHearts + 1);
            player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.hearts-deposited")
                    .replace("%amount%", "1")));
        }
    }

    @EventHandler
    public void onHeartChange(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Give regeneration effect when hearts change
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1)); // 5 seconds of Regen II
    }
} 