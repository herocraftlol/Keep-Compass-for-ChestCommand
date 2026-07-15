package com.compasshotbar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CompassCommand implements CommandExecutor, TabCompleter {

    private final CompassHotbar plugin;

    public CompassCommand(CompassHotbar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        
        if (args.length == 0) {
            if (!sender.hasPermission("compasshotbar.command")) {
                sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }

            boolean newState = !plugin.isPluginEnabled();
            plugin.setPluginEnabled(newState);

            String stateMessage = newState ? "§aactivé" : "§cdésactivé";
            sender.sendMessage("§6[CompassHotbar] §aLe plugin est maintenant " + stateMessage + "§a!");
            
            if (newState) {
                sender.sendMessage("§6[CompassHotbar] §7Tous les joueurs vont recevoir les items configurés.");
            } else {
                sender.sendMessage("§6[CompassHotbar] §7Les items configurés vont être retirés.");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("compasshotbar.command")) {
                    sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                    return true;
                }
                plugin.reloadHotbarConfig();
                plugin.setPluginEnabled(plugin.getConfig().getBoolean("enabled", true));
                sender.sendMessage("§6[CompassHotbar] §aConfiguration rechargée!");
                break;

            case "give":
                if (!sender.hasPermission("compasshotbar.command")) {
                    sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                    return true;
                }
                if (plugin.isPluginEnabled()) {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        plugin.giveAllItems(player);
                    }
                    sender.sendMessage("§6[CompassHotbar] §aLes items ont été donnés à tous les joueurs!");
                } else {
                    sender.sendMessage("§6[CompassHotbar] §cLe plugin est désactivé!");
                }
                break;

            case "toggle":
                if (!sender.hasPermission("compasshotbar.command")) {
                    sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                    return true;
                }
                boolean newStateToggle = !plugin.isPluginEnabled();
                plugin.setPluginEnabled(newStateToggle);
                String stateToggle = newStateToggle ? "§aactivé" : "§cdésactivé";
                sender.sendMessage("§6[CompassHotbar] §aLe plugin est maintenant " + stateToggle + "§a!");
                break;

            case "status":
                String status = plugin.isPluginEnabled() ? "§aactivé" : "§cdésactivé";
                sender.sendMessage("§6[CompassHotbar] §7Statut du plugin: " + status);
                break;

            default:
                sender.sendMessage("§6[CompassHotbar] §cUsage: /compasshotbar [reload|give|toggle|status]");
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("compasshotbar.command")) {
                completions.add("reload");
                completions.add("give");
                completions.add("toggle");
                completions.add("status");
            }
        }

        return completions;
    }
}