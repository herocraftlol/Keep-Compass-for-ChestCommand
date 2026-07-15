package com.compasshotbar;

import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CompassTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull org.bukkit.command.CommandSender sender,
                                                @NotNull org.bukkit.command.Command command,
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