package com.compasshotbar;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassHotbar extends JavaPlugin {

    private static CompassHotbar instance;
    private boolean enabled = true;
    private static final int COMPASS_SLOT = 8;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        enabled = getConfig().getBoolean("enabled", true);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getCommand("compasshotbar").setExecutor(new CompassCommand(this));
        getCommand("compasshotbar").setTabCompleter(new CompassTabCompleter());

        for (Player player : getServer().getOnlinePlayers()) {
            giveCompass(player);
        }

        getLogger().info("CompassHotbar has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CompassHotbar has been disabled!");
    }

    public static CompassHotbar getInstance() {
        return instance;
    }

    public boolean isPluginEnabled() {
        return enabled;
    }

    public void setPluginEnabled(boolean enabled) {
        this.enabled = enabled;
        getConfig().set("enabled", enabled);
        saveConfig();

        if (!enabled) {
            for (Player player : getServer().getOnlinePlayers()) {
                removeCompass(player);
            }
        } else {
            for (Player player : getServer().getOnlinePlayers()) {
                giveCompass(player);
            }
        }
    }

    public void giveCompass(Player player) {
        if (!enabled) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        PlayerInventory inventory = player.getInventory();
        ItemStack compass = new ItemStack(Material.COMPASS);
        inventory.setItem(COMPASS_SLOT, compass);
    }

    public void removeCompass(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack currentItem = inventory.getItem(COMPASS_SLOT);

        if (currentItem != null && currentItem.getType() == Material.COMPASS) {
            inventory.setItem(COMPASS_SLOT, null);
        }
    }

    public void ensureCompassInSlot(Player player) {
        if (!enabled) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        PlayerInventory inventory = player.getInventory();
        ItemStack currentItem = inventory.getItem(COMPASS_SLOT);

        if (currentItem == null || currentItem.getType() != Material.COMPASS) {
            giveCompass(player);
            player.sendMessage("§6[CompassHotbar] §aVotre boussole a été restaurée dans votre hotbar!");
        }
    }
}