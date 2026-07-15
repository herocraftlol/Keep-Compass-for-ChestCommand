package com.compasshotbar;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassHotbar extends JavaPlugin {

    private static CompassHotbar instance;
    private boolean enabled = true;
    private HotbarManager hotbarManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        enabled = getConfig().getBoolean("enabled", true);

        hotbarManager = new HotbarManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getCommand("compasshotbar").setExecutor(new CompassCommand(this));
        getCommand("compasshotbar").setTabCompleter(new CompassTabCompleter());

        for (Player player : getServer().getOnlinePlayers()) {
            hotbarManager.giveAll(player);
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

    public HotbarManager getHotbarManager() {
        return hotbarManager;
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
                hotbarManager.removeAll(player);
            }
        } else {
            for (Player player : getServer().getOnlinePlayers()) {
                hotbarManager.giveAll(player);
            }
        }
    }

    /**
     * Donne tous les items configurés (boussole, boutique, site, discord, amis, ...)
     * à un joueur.
     */
    public void giveAllItems(Player player) {
        hotbarManager.giveAll(player);
    }

    /**
     * Retire tous les items gérés par le plugin de l'inventaire du joueur.
     */
    public void removeAllItems(Player player) {
        hotbarManager.removeAll(player);
    }

    /**
     * Vérifie que tous les items sont bien présents dans leurs slots et les
     * restaure si besoin (avec message d'information).
     */
    public void ensureAllItems(Player player) {
        hotbarManager.ensureAll(player);
    }

    public void reloadHotbarConfig() {
        reloadConfig();
        hotbarManager.reload();
    }
}
