package com.compasshotbar;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère la zone (pos1/pos2) dans laquelle les items de la hotbar doivent
 * s'afficher. En dehors de cette zone (si elle est activée), tous les
 * items du plugin sont retirés de l'inventaire du joueur — un peu comme
 * une zone "lobby".
 */
public class ZoneManager {

    private final CompassHotbar plugin;

    private boolean enabled;
    private String worldName;
    private Integer x1, y1, z1;
    private Integer x2, y2, z2;

    /**
     * Mode "global" : quand activé, la restriction de zone est totalement
     * ignorée et la hotbar s'affiche partout sur le serveur, même si une
     * zone est configurée et activée. Ne modifie pas la configuration de la
     * zone (pos1/pos2/enabled) : c'est un simple interrupteur prioritaire,
     * changeable à tout moment via une commande admin
     * (/compasshotbar global on|off).
     */
    private boolean globalMode;

    public ZoneManager(CompassHotbar plugin) {
        this.plugin = plugin;
        load();
    }

    /**
     * (Re)lit la section "zone" de la config.yml.
     */
    public void load() {
        globalMode = plugin.getConfig().getBoolean("global-mode", false);

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("zone");

        if (section == null) {
            enabled = false;
            worldName = null;
            x1 = y1 = z1 = x2 = y2 = z2 = null;
            return;
        }

        enabled = section.getBoolean("enabled", false);
        worldName = section.getString("world", null);

        ConfigurationSection pos1 = section.getConfigurationSection("pos1");
        ConfigurationSection pos2 = section.getConfigurationSection("pos2");

        if (worldName != null && pos1 != null && pos2 != null) {
            x1 = pos1.getInt("x");
            y1 = pos1.getInt("y");
            z1 = pos1.getInt("z");
            x2 = pos2.getInt("x");
            y2 = pos2.getInt("y");
            z2 = pos2.getInt("z");
        } else {
            x1 = y1 = z1 = x2 = y2 = z2 = null;
        }
    }

    /**
     * Sauvegarde l'état actuel de la zone dans la config.yml.
     */
    public void save() {
        plugin.getConfig().set("global-mode", globalMode);
        plugin.getConfig().set("zone.enabled", enabled);
        plugin.getConfig().set("zone.world", worldName);

        if (x1 != null) {
            plugin.getConfig().set("zone.pos1.x", x1);
            plugin.getConfig().set("zone.pos1.y", y1);
            plugin.getConfig().set("zone.pos1.z", z1);
        }
        if (x2 != null) {
            plugin.getConfig().set("zone.pos2.x", x2);
            plugin.getConfig().set("zone.pos2.y", y2);
            plugin.getConfig().set("zone.pos2.z", z2);
        }

        plugin.saveConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public boolean isGlobalMode() {
        return globalMode;
    }

    public void setGlobalMode(boolean globalMode) {
        this.globalMode = globalMode;
        save();
    }

    /**
     * True si la restriction de zone doit réellement s'appliquer, c'est-à-dire
     * si la zone est activée et configurée ET que le mode global (qui a
     * priorité sur tout) n'est pas actif.
     */
    public boolean isRestrictionActive() {
        return !globalMode && enabled && isConfigured();
    }

    /**
     * True si pos1 ET pos2 ont bien été définies (même monde).
     */
    public boolean isConfigured() {
        return worldName != null && x1 != null && x2 != null;
    }

    public void setPos1(Location loc) {
        this.worldName = loc.getWorld().getName();
        this.x1 = loc.getBlockX();
        this.y1 = loc.getBlockY();
        this.z1 = loc.getBlockZ();
        save();
    }

    public void setPos2(Location loc) {
        this.worldName = loc.getWorld().getName();
        this.x2 = loc.getBlockX();
        this.y2 = loc.getBlockY();
        this.z2 = loc.getBlockZ();
        save();
    }

    /**
     * True si la Location donnée est comprise dans la zone (bornes incluses).
     * Retourne toujours false si la zone n'est pas entièrement configurée.
     */
    public boolean contains(Location loc) {
        if (!isConfigured()) return false;
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equals(worldName)) return false;

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        int bx = loc.getBlockX();
        int by = loc.getBlockY();
        int bz = loc.getBlockZ();

        return bx >= minX && bx <= maxX
                && by >= minY && by <= maxY
                && bz >= minZ && bz <= maxZ;
    }

    /**
     * Petites lignes d'info (déjà colorées) pour /compasshotbar zone info.
     */
    public List<String> getInfoLines() {
        List<String> lines = new ArrayList<>();

        lines.add(HotbarManager.colorize("&6[CompassHotbar] &7Mode global : "
                + (globalMode ? "&aactivé &7(hotbar partout, zone ignorée)" : "&cdésactivé")));

        if (!isConfigured()) {
            lines.add(HotbarManager.colorize("&cZone non configurée."));
            lines.add(HotbarManager.colorize("&7Utilisez &f/compasshotbar pos1 &7et &f/compasshotbar pos2"));
            lines.add(HotbarManager.colorize("&7en regardant les blocs de coin de votre zone."));
            return lines;
        }

        lines.add(HotbarManager.colorize("&6[CompassHotbar] &7Zone :"));
        lines.add(HotbarManager.colorize("&7Monde : &f" + worldName));
        lines.add(HotbarManager.colorize("&7Pos1 : &f" + x1 + ", " + y1 + ", " + z1));
        lines.add(HotbarManager.colorize("&7Pos2 : &f" + x2 + ", " + y2 + ", " + z2));
        lines.add(HotbarManager.colorize("&7Activée : " + (enabled ? "&aoui" : "&cnon")));
        return lines;
    }
}
