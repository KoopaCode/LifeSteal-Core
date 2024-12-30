package com.koopa.lifestealcore.commands;

import com.koopa.lifestealcore.LifeStealCore;
import com.koopa.lifestealcore.utils.ItemManager;
import com.koopa.lifestealcore.utils.MessageUtils;
import com.koopa.lifestealcore.gui.ConfigGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.UUID;

public class LifeStealCommand implements CommandExecutor {
    private final LifeStealCore plugin;
    private final HashMap<UUID, Boolean> resetConfirmMap = new HashMap<>();

    public LifeStealCommand(LifeStealCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> showHelp(player);
            case "withdraw" -> handleWithdraw(player, args);
            case "deposit" -> handleDeposit(player);
            case "reset" -> handleReset(player);
            case "giveheart" -> handleGiveHeart(player, args);
            case "recipe" -> handleRecipe(player);
            case "config" -> handleConfig(player);
            case "debug" -> handleDebugCommand(player, args);
            default -> player.sendMessage(MessageUtils.color("&cUnknown command! Use /lifesteal help"));
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(MessageUtils.color("&8&l=== &c&lLifeSteal Commands &8&l==="));
        player.sendMessage(MessageUtils.color("&c/lifesteal help &8- &7Show this help menu"));
        player.sendMessage(MessageUtils.color("&c/lifesteal withdraw <amount> &8- &7Convert hearts to items"));
        player.sendMessage(MessageUtils.color("&c/lifesteal deposit &8- &7Deposit heart items"));
        player.sendMessage(MessageUtils.color("&c/lifesteal reset &8- &7Reset all players' hearts"));
        player.sendMessage(MessageUtils.color("&c/lifesteal giveheart <amount> &8- &7Give yourself heart items"));
        player.sendMessage(MessageUtils.color("&c/lifesteal recipe &8- &7View the revival beacon recipe"));
        if (player.hasPermission("lifesteal.admin.config")) {
            player.sendMessage(MessageUtils.color("&c/lifesteal config &8- &7Open configuration GUI"));
        }
        if (player.hasPermission("lifesteal.admin.debug")) {
            player.sendMessage(MessageUtils.color("&c/lifesteal debug <ban|unban> <player> &8- &7Test ban/unban messages"));
        }
    }

    private void handleWithdraw(Player player, String[] args) {
        if (!player.hasPermission("lifesteal.withdraw")) {
            player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        if (args.length != 2) {
            player.sendMessage(MessageUtils.color("&cUsage: /lifesteal withdraw <amount>"));
            return;
        }

        try {
            int amount = Integer.parseInt(args[1]);
            int currentHearts = plugin.getHeartManager().getPlayerHearts(player);
            int minHearts = plugin.getConfig().getInt("settings.min-hearts");

            if (currentHearts - amount < minHearts) {
                player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.minimum-hearts-reached")
                        .replace("%min%", String.valueOf(minHearts))));
                return;
            }

            plugin.getHeartManager().setPlayerHearts(player, currentHearts - amount);
            ItemStack heartItem = ItemManager.createHeartItem(amount);
            player.getInventory().addItem(heartItem);
            player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.hearts-withdrawn")
                    .replace("%amount%", String.valueOf(amount))));

        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.color("&cPlease enter a valid number!"));
        }
    }

    private void handleDeposit(Player player) {
        if (!player.hasPermission("lifesteal.deposit")) {
            player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        int deposited = 0;
        int maxHearts = plugin.getConfig().getInt("settings.max-hearts");
        int currentHearts = plugin.getHeartManager().getPlayerHearts(player);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && ItemManager.isHeartItem(item)) {
                if (currentHearts + deposited + item.getAmount() <= maxHearts) {
                    deposited += item.getAmount();
                    item.setAmount(0);
                } else {
                    int canDeposit = maxHearts - (currentHearts + deposited);
                    if (canDeposit > 0) {
                        deposited += canDeposit;
                        item.setAmount(item.getAmount() - canDeposit);
                    }
                    break;
                }
            }
        }

        if (deposited > 0) {
            plugin.getHeartManager().setPlayerHearts(player, currentHearts + deposited);
            player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.hearts-deposited")
                    .replace("%amount%", String.valueOf(deposited))));
        } else {
            player.sendMessage(MessageUtils.color("&cNo hearts found in your inventory!"));
        }
    }

    private void handleReset(Player player) {
        if (!player.hasPermission("lifesteal.admin.reset")) {
            player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        int defaultHearts = plugin.getConfig().getInt("settings.default-hearts");
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            plugin.getHeartManager().setPlayerHearts(onlinePlayer, defaultHearts);
        }
        player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.hearts-reset")));
    }

    private void handleGiveHeart(Player player, String[] args) {
        if (!player.hasPermission("lifesteal.admin.give")) {
            player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        if (args.length != 2) {
            player.sendMessage(MessageUtils.color("&cUsage: /lifesteal giveheart <amount>"));
            return;
        }

        try {
            int amount = Integer.parseInt(args[1]);
            ItemStack heartItem = ItemManager.createHeartItem(amount);
            player.getInventory().addItem(heartItem);
            player.sendMessage(MessageUtils.color(plugin.getConfig().getString("messages.hearts-given")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%player%", player.getName())));
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.color("&cPlease enter a valid number!"));
        }
    }

    private void handleRecipe(Player player) {
        new ConfigGUI(plugin).openRecipeGUI(player, plugin.getConfig().getString("settings.difficulty", "MEDIUM"));
    }

    private void handleConfig(Player player) {
        if (!player.hasPermission("lifesteal.admin.config")) {
            player.sendMessage(MessageUtils.color("&cYou don't have permission to use this command!"));
            return;
        }
        new ConfigGUI(plugin).openConfigGUI(player);
    }

    private void handleDebugCommand(Player player, String[] args) {
        if (!player.hasPermission("lifesteal.admin.debug")) {
            player.sendMessage(MessageUtils.color("&cYou don't have permission to use debug commands!"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(MessageUtils.color("&cUsage: /lifesteal debug <ban|unban> <player>"));
            return;
        }

        String action = args[1].toLowerCase();
        String targetName = args[2];
        Player target = Bukkit.getPlayer(targetName);

        switch (action) {
            case "ban" -> {
                if (target == null) {
                    player.sendMessage(MessageUtils.color("&cPlayer not found!"));
                    return;
                }
                // Test ban message with both killer and victim
                plugin.getBanManager().banPlayer(target, player);
                player.sendMessage(MessageUtils.color("&aDebug: Tested ban message for " + target.getName() + 
                    " (killed by " + player.getName() + ")"));
            }
            case "unban" -> {
                // Test unban/revival message
                plugin.getBanManager().unbanPlayer(player, targetName);
                player.sendMessage(MessageUtils.color("&aDebug: Tested unban message for " + targetName + 
                    " (revived by " + player.getName() + ")"));
            }
            default -> player.sendMessage(MessageUtils.color("&cInvalid debug action! Use 'ban' or 'unban'"));
        }
    }
} 