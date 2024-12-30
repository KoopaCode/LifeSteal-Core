package com.koopa.lifestealcore.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.koopa.lifestealcore.LifeStealCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemManager {
    private static final NamespacedKey HEART_KEY = new NamespacedKey(LifeStealCore.getInstance(), "lifesteal_heart");

    public static ItemStack createHeartItem(int amount) {
        ItemStack item = new ItemStack(Material.NETHER_STAR, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&c❤ Heart"));
        meta.setLore(Arrays.asList(
            MessageUtils.color("&7Right-click to consume"),
            MessageUtils.color("&7and gain an extra heart")
        ));
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHeartItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.hasDisplayName() && 
               meta.getDisplayName().equals(MessageUtils.color("&c❤ Heart"));
    }
} 