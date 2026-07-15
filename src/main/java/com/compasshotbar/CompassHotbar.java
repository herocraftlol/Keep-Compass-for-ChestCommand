package com.compasshotbar;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassHotbar extends JavaPlugin {

    private static CompassHotbar instance;
    private boolean enabled = true;
    private HotbarManager hotbarManager;
    private ZoneManager zoneManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        enabled = getConfig().getBoolean("enabled", true);

        hotbarManager = new HotbarManager(this);
        zoneManager = new ZoneManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getCommand("compasshotbar").setExecutor(new CompassCommand(this));
        getCommand("compasshotbar").setTabCompleter(new CompassTabCompleter());

        for (Player player : getServer().getOnlinePlayers()) {
            syncZoneState(player);
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

    public ZoneManager getZoneManager() {
        return zoneManager;
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
                syncZoneState(player);
            }
        }
    }

    /**
     * Donne ou retire les items d'un joueur selon l'état du plugin et,
     * si elle est activée, selon sa position par rapport à la zone
     * définie par /compasshotbar pos1 et pos2 (zone "lobby").
     */
    public void syncZoneState(Player player) {
        if (!enabled) {
            hotbarManager.removeAll(player);
            return;
        }

        if (zoneManager.isEnabled() && zoneManager.isConfigured()) {
            if (zoneManager.contains(player.getLocation())) {
                hotbarManager.giveAll(player);
            } else {
                hotbarManager.removeAll(player);
            }
        } else {
            hotbarManager.giveAll(player);
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
        zoneManager.load();

        for (Player player : getServer().getOnlinePlayers()) {
            syncZoneState(player);
        }
    }
}
