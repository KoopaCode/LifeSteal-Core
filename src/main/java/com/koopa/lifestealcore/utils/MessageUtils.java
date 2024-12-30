package com.koopa.lifestealcore.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.Map;

public class MessageUtils {
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendHelpMenu(CommandSender sender, ConfigurationSection config) {
        // Send header
        sender.sendMessage(color(config.getString("messages.help.header")));

        // Send command list
        String format = config.getString("messages.help.format");
        List<Map<?, ?>> commands = config.getMapList("messages.help.commands");

        for (Map<?, ?> cmdMap : commands) {
            String command = (String) cmdMap.get("command");
            String description = (String) cmdMap.get("description");
            String permission = (String) cmdMap.get("permission");

            // Only show commands the player has permission for
            if (permission == null || sender.hasPermission(permission)) {
                String helpLine = format
                    .replace("%command%", command)
                    .replace("%description%", description);
                sender.sendMessage(color(helpLine));
            }
        }

        // Send footer
        sender.sendMessage(color(config.getString("messages.help.footer")));
    }
} 