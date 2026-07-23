package com.compasshotbar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class CompassHotbar extends JavaPlugin {

    private static CompassHotbar instance;
    private boolean enabled = true;
    private HotbarManager hotbarManager;
    private ZoneManager zoneManager;
    private ServerGuiManager serverGuiManager;
    private ProxyBridge proxyBridge;
    private BukkitTask guiRefreshTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        enabled = getConfig().getBoolean("enabled", true);

        hotbarManager = new HotbarManager(this);
        zoneManager = new ZoneManager(this);
        proxyBridge = new ProxyBridge(this);
        serverGuiManager = new ServerGuiManager(this);

        // Canal legacy natif (Velocity et BungeeCord le comprennent tous les
        // deux sans plugin supplémentaire) : utilisé pour envoyer les joueurs
        // d'un serveur à l'autre (sous-commande "Connect").
        getServer().getMessenger().registerOutgoingPluginChannel(this, ProxyBridge.BUNGEE_CHANNEL);

        // Canal custom : nécessite le petit plugin CompassHotbarVelocity côté
        // proxy pour recevoir les statuts (ouvert/fermé, joueurs) des serveurs.
        // Sans lui, le menu affichera juste "Chargement..." pour chaque icône.
        getServer().getMessenger().registerIncomingPluginChannel(this, ProxyBridge.SYNC_CHANNEL, proxyBridge);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getCommand("compasshotbar").setExecutor(new CompassCommand(this));
        getCommand("compasshotbar").setTabCompleter(new CompassTabCompleter());

        for (Player player : getServer().getOnlinePlayers()) {
            syncZoneState(player);
        }

        startGuiRefreshTask();

        getLogger().info("CompassHotbar has been enabled!");
    }

    @Override
    public void onDisable() {
        if (guiRefreshTask != null) {
            guiRefreshTask.cancel();
        }
        getLogger().info("CompassHotbar has been disabled!");
    }

    /**
     * Rafraîchit en direct (toutes les 5 secondes) le contenu du menu
     * Serveurs/Mini-jeux pour chaque joueur qui l'a actuellement ouvert,
     * sans le fermer, pour que les compteurs de joueurs restent à jour.
     */
    private void startGuiRefreshTask() {
        guiRefreshTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                Inventory top = player.getOpenInventory().getTopInventory();
                if (top.getHolder() instanceof ServerGuiHolder) {
                    serverGuiManager.refresh(top);
                }
            }
        }, 100L, 100L);
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

    public ServerGuiManager getServerGuiManager() {
        return serverGuiManager;
    }

    public ProxyBridge getProxyBridge() {
        return proxyBridge;
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
     * Donne ou retire les items d'un joueur selon l'état du plugin. La
     * logique "quel item afficher selon la zone" est gérée item par item
     * dans HotbarManager (voir HotbarItem#isShowOutsideZone).
     */
    public void syncZoneState(Player player) {
        if (!enabled) {
            hotbarManager.removeAll(player);
            return;
        }

        hotbarManager.giveAll(player);
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
        serverGuiManager.reload();

        for (Player player : getServer().getOnlinePlayers()) {
            syncZoneState(player);
        }
    }
}
