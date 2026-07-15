package com.compasshotbar;

import org.bukkit.block.Block;
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

            case "pos1":
                if (!sender.hasPermission("compasshotbar.command")) {
                    sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                    return true;
                }
                if (!(sender instanceof Player player1)) {
                    sender.sendMessage("§cCette commande ne peut être utilisée que par un joueur.");
                    return true;
                }
                Block target1 = player1.getTargetBlockExact(10);
                if (target1 == null) {
                    sender.sendMessage("§6[CompassHotbar] §cAucun bloc visé dans la portée (10 blocs).");
                    return true;
                }
                plugin.getZoneManager().setPos1(target1.getLocation());
                sender.sendMessage("§6[CompassHotbar] §aPos1 définie : §f" + target1.getX() + ", "
                        + target1.getY() + ", " + target1.getZ() + " §7(" + target1.getWorld().getName() + ")");
                break;

            case "pos2":
                if (!sender.hasPermission("compasshotbar.command")) {
                    sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                    return true;
                }
                if (!(sender instanceof Player player2)) {
                    sender.sendMessage("§cCette commande ne peut être utilisée que par un joueur.");
                    return true;
                }
                Block target2 = player2.getTargetBlockExact(10);
                if (target2 == null) {
                    sender.sendMessage("§6[CompassHotbar] §cAucun bloc visé dans la portée (10 blocs).");
                    return true;
                }
                plugin.getZoneManager().setPos2(target2.getLocation());
                sender.sendMessage("§6[CompassHotbar] §aPos2 définie : §f" + target2.getX() + ", "
                        + target2.getY() + ", " + target2.getZ() + " §7(" + target2.getWorld().getName() + ")");
                break;

            case "zone":
                if (!sender.hasPermission("compasshotbar.command")) {
                    sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§6[CompassHotbar] §cUsage: /compasshotbar zone <enable|disable|info>");
                    return true;
                }

                switch (args[1].toLowerCase()) {
                    case "enable":
                        if (!plugin.getZoneManager().isConfigured()) {
                            sender.sendMessage("§6[CompassHotbar] §cDéfinissez d'abord pos1 et pos2 avant d'activer la zone.");
                        } else {
                            plugin.getZoneManager().setEnabled(true);
                            sender.sendMessage("§6[CompassHotbar] §aRestriction de zone activée ! La hotbar ne s'affichera que dans la zone définie.");
                            for (Player p : plugin.getServer().getOnlinePlayers()) {
                                plugin.syncZoneState(p);
                            }
                        }
                        break;

                    case "disable":
                        plugin.getZoneManager().setEnabled(false);
                        sender.sendMessage("§6[CompassHotbar] §7Restriction de zone désactivée. La hotbar s'affiche partout.");
                        for (Player p : plugin.getServer().getOnlinePlayers()) {
                            plugin.syncZoneState(p);
                        }
                        break;

                    case "info":
                        for (String line : plugin.getZoneManager().getInfoLines()) {
                            sender.sendMessage(line);
                        }
                        break;

                    default:
                        sender.sendMessage("§6[CompassHotbar] §cUsage: /compasshotbar zone <enable|disable|info>");
                        break;
                }
                break;

            case "global":
                if (!sender.hasPermission("compasshotbar.command")) {
                    sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                    return true;
                }
                if (args.length < 2) {
                    String currentGlobal = plugin.getZoneManager().isGlobalMode() ? "§aactivé" : "§cdésactivé";
                    sender.sendMessage("§6[CompassHotbar] §cUsage: /compasshotbar global <on|off|status>");
                    sender.sendMessage("§6[CompassHotbar] §7Mode global actuel: " + currentGlobal);
                    return true;
                }

                switch (args[1].toLowerCase()) {
                    case "on":
                    case "enable":
                        plugin.getZoneManager().setGlobalMode(true);
                        sender.sendMessage("§6[CompassHotbar] §aMode global activé ! La hotbar s'affiche maintenant partout sur le serveur, quelle que soit la zone configurée.");
                        for (Player p : plugin.getServer().getOnlinePlayers()) {
                            plugin.syncZoneState(p);
                        }
                        break;

                    case "off":
                    case "disable":
                        plugin.getZoneManager().setGlobalMode(false);
                        sender.sendMessage("§6[CompassHotbar] §7Mode global désactivé. La restriction de zone (si activée) s'applique de nouveau.");
                        for (Player p : plugin.getServer().getOnlinePlayers()) {
                            plugin.syncZoneState(p);
                        }
                        break;

                    case "status":
                        String globalStatus = plugin.getZoneManager().isGlobalMode() ? "§aactivé" : "§cdésactivé";
                        sender.sendMessage("§6[CompassHotbar] §7Mode global: " + globalStatus);
                        break;

                    default:
                        sender.sendMessage("§6[CompassHotbar] §cUsage: /compasshotbar global <on|off|status>");
                        break;
                }
                break;

            default:
                sender.sendMessage("§6[CompassHotbar] §cUsage: /compasshotbar [reload|give|toggle|status|pos1|pos2|zone|global]");
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
                completions.add("pos1");
                completions.add("pos2");
                completions.add("zone");
                completions.add("global");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("zone")) {
            if (sender.hasPermission("compasshotbar.command")) {
                completions.add("enable");
                completions.add("disable");
                completions.add("info");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("global")) {
            if (sender.hasPermission("compasshotbar.command")) {
                completions.add("on");
                completions.add("off");
                completions.add("status");
            }
        }

        return completions;
    }
}