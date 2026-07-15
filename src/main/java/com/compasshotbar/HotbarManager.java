package com.compasshotbar;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Charge les items de la hotbar depuis la config.yml, les construit et
 * gère leur affectation / protection dans l'inventaire des joueurs.
 */
public class HotbarManager {

    private final CompassHotbar plugin;
    private final NamespacedKey itemIdKey;
    private final Map<String, HotbarItem> items = new LinkedHashMap<>();

    public HotbarManager(CompassHotbar plugin) {
        this.plugin = plugin;
        this.itemIdKey = new NamespacedKey(plugin, "hotbar_item_id");
        reload();
    }

    public NamespacedKey getItemIdKey() {
        return itemIdKey;
    }

    public Map<String, HotbarItem> getItems() {
        return items;
    }

    public HotbarItem getItem(String id) {
        return items.get(id);
    }

    /**
     * (Re)lit la section "hotbar-items" de la config.yml.
     */
    public void reload() {
        items.clear();

        ConfigurationSection root = plugin.getConfig().getConfigurationSection("hotbar-items");
        if (root == null) {
            plugin.getLogger().warning("Aucune section 'hotbar-items' trouvée dans config.yml !");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) continue;

            boolean enabled = section.getBoolean("enabled", true);
            int slot = section.getInt("slot", -1);

            Material material;
            try {
                material = Material.valueOf(section.getString("material", "STONE").toUpperCase());
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Matériau invalide pour l'item '" + id + "', utilisation de STONE par défaut.");
                material = Material.STONE;
            }

            String displayName = section.getString("name", null);
            List<String> lore = section.getStringList("lore");

            HotbarItem.ActionType actionType = HotbarItem.ActionType.NONE;
            String actionValue = null;

            if (section.contains("url") && section.getString("url") != null && !section.getString("url").isEmpty()) {
                actionType = HotbarItem.ActionType.URL;
                actionValue = section.getString("url");
            } else if (section.contains("command") && section.getString("command") != null && !section.getString("command").isEmpty()) {
                actionType = HotbarItem.ActionType.COMMAND;
                actionValue = section.getString("command");
            }

            String clickMessage = section.getString("click-message", null);
            String actionbarMessage = section.getString("actionbar-message", null);
            String soundName = section.getString("sound", null);

            if (slot < 0 || slot > 8) {
                plugin.getLogger().warning("Slot invalide pour l'item '" + id + "' (" + slot + "), item ignoré.");
                continue;
            }

            items.put(id, new HotbarItem(id, enabled, slot, material, displayName, lore, actionType, actionValue,
                    clickMessage, actionbarMessage, soundName));
        }
    }

    /**
     * Construit l'ItemStack correspondant à un HotbarItem, avec le tag NBT
     * permettant de le reconnaître (protection / clic droit).
     */
    public ItemStack buildItemStack(HotbarItem item) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();

        if (meta != null) {
            if (item.getDisplayName() != null) {
                meta.setDisplayName(colorize(item.getDisplayName()));
            }

            if (!item.getLore().isEmpty()) {
                List<String> colored = new ArrayList<>();
                for (String line : item.getLore()) {
                    colored.add(colorize(line));
                }
                meta.setLore(colored);
            }

            meta.addItemFlags(ItemFlag.values());
            meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, item.getId());
            stack.setItemMeta(meta);
        }

        return stack;
    }

    /**
     * Retourne l'id de l'item plugin présent dans cet ItemStack, ou null si
     * ce n'est pas un item géré par le plugin.
     */
    public String getItemId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
    }

    public void giveAll(Player player) {
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        ZoneManager zone = plugin.getZoneManager();
        if (zone.isEnabled() && zone.isConfigured() && !zone.contains(player.getLocation())) {
            // Le joueur est en dehors de la zone définie : on n'affiche rien.
            return;
        }

        PlayerInventory inventory = player.getInventory();
        for (HotbarItem item : items.values()) {
            if (!item.isEnabled()) continue;
            inventory.setItem(item.getSlot(), buildItemStack(item));
        }
    }

    public void removeAll(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (HotbarItem item : items.values()) {
            ItemStack current = inventory.getItem(item.getSlot());
            if (item.getId().equals(getItemId(current))) {
                inventory.setItem(item.getSlot(), null);
            }
        }
    }

    /**
     * Vérifie que chaque item activé est bien présent dans son slot et le
     * restaure si besoin.
     */
    public void ensureAll(Player player) {
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        ZoneManager zone = plugin.getZoneManager();
        if (zone.isEnabled() && zone.isConfigured() && !zone.contains(player.getLocation())) {
            // Hors zone : on s'assure qu'aucun item du plugin ne traîne dans l'inventaire.
            removeAll(player);
            return;
        }

        PlayerInventory inventory = player.getInventory();
        boolean restoredAny = false;

        for (HotbarItem item : items.values()) {
            if (!item.isEnabled()) continue;

            ItemStack current = inventory.getItem(item.getSlot());
            if (!item.getId().equals(getItemId(current))) {
                inventory.setItem(item.getSlot(), buildItemStack(item));
                restoredAny = true;
            }
        }

        if (restoredAny) {
            String message = plugin.getConfig().getString("restored-message",
                    "&6[CompassHotbar] &aVos items ont été restaurés dans votre hotbar!");
            player.sendMessage(colorize(message));
        }
    }

    public static String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
