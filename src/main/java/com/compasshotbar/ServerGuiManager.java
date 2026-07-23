package com.compasshotbar;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Charge la section "servers-gui" de la config.yml et construit le menu
 * (inventaire) affiché quand un joueur clique-droit sur la boussole.
 *
 * Chaque icône représente un serveur/mini-jeu distinct. Son état
 * (ouvert/fermé, nombre de joueurs) vient du ProxyBridge, alimenté par le
 * plugin Velocity CompassHotbarVelocity. Tant qu'aucune donnée n'a été
 * reçue pour un serveur, l'icône affiche un lore "chargement" neutre
 * plutôt que des chiffres inventés.
 */
public class ServerGuiManager {

    private final CompassHotbar plugin;
    private final NamespacedKey guiItemIdKey;

    private boolean enabled;
    private String title;
    private int rows;
    private long syncMaxAgeMillis;
    private int fallbackMaxPlayers;
    private boolean fillEmptySlots;
    private Material fillerMaterial;
    private List<String> noDataLore;
    private List<String> openLoreTemplate;
    private List<String> closedLoreTemplate;

    private final Map<String, ServerGuiItem> items = new LinkedHashMap<>();

    public ServerGuiManager(CompassHotbar plugin) {
        this.plugin = plugin;
        this.guiItemIdKey = new NamespacedKey(plugin, "server_gui_item_id");
        reload();
    }

    public NamespacedKey getGuiItemIdKey() {
        return guiItemIdKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ServerGuiItem getItem(String id) {
        return items.get(id);
    }

    public void reload() {
        items.clear();

        ConfigurationSection root = plugin.getConfig().getConfigurationSection("servers-gui");
        if (root == null) {
            enabled = false;
            return;
        }

        enabled = root.getBoolean("enabled", true);
        title = HotbarManager.colorize(root.getString("title", "&8&lServeurs & Mini-jeux"));
        rows = clampRows(root.getInt("rows", 3));
        syncMaxAgeMillis = root.getInt("sync-max-age-seconds", 15) * 1000L;
        fallbackMaxPlayers = root.getInt("fallback-max-players", 100);
        fillEmptySlots = root.getBoolean("fill-empty-slots", true);

        try {
            fillerMaterial = Material.valueOf(root.getString("filler-material", "GRAY_STAINED_GLASS_PANE").toUpperCase());
        } catch (IllegalArgumentException ex) {
            fillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
        }

        noDataLore = root.getStringList("no-data-lore");
        openLoreTemplate = root.getStringList("open-lore-template");
        closedLoreTemplate = root.getStringList("closed-lore-template");

        ConfigurationSection itemsSection = root.getConfigurationSection("items");
        if (itemsSection == null) return;

        for (String id : itemsSection.getKeys(false)) {
            ConfigurationSection section = itemsSection.getConfigurationSection(id);
            if (section == null) continue;

            int slot = section.getInt("slot", -1);
            if (slot < 0 || slot >= rows * 9) {
                plugin.getLogger().warning("Slot invalide pour l'item de menu '" + id + "' (" + slot + "), item ignoré.");
                continue;
            }

            Material material;
            try {
                material = Material.valueOf(section.getString("material", "PAPER").toUpperCase());
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Matériau invalide pour l'item de menu '" + id + "', utilisation de PAPER par défaut.");
                material = Material.PAPER;
            }

            String displayName = section.getString("name", "&f" + id);
            List<String> extraLore = section.getStringList("extra-lore");
            String server = section.getString("server", id);
            boolean glow = section.getBoolean("glow-when-open", false);

            items.put(id, new ServerGuiItem(id, slot, material, displayName, extraLore, server, glow));
        }
    }

    private int clampRows(int rows) {
        if (rows < 1) return 1;
        if (rows > 6) return 6;
        return rows;
    }

    /**
     * Construit un inventaire à jour à partir des statuts actuellement
     * connus (ProxyBridge) et l'ouvre pour le joueur.
     */
    public void open(Player player) {
        Inventory inventory = build();
        player.openInventory(inventory);
    }

    /**
     * Reconstruit le contenu (les ItemStack) d'un inventaire déjà ouvert,
     * sans le refermer, pour un rafraîchissement en direct des compteurs
     * de joueurs / statuts.
     */
    public void refresh(Inventory inventory) {
        for (ServerGuiItem item : items.values()) {
            inventory.setItem(item.getSlot(), buildItemStack(item));
        }
    }

    private Inventory build() {
        ServerGuiHolder holder = new ServerGuiHolder();
        Inventory inventory = Bukkit.createInventory(holder, rows * 9, title);
        holder.setInventory(inventory);

        if (fillEmptySlots) {
            ItemStack filler = new ItemStack(fillerMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(" ");
                filler.setItemMeta(fillerMeta);
            }
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        for (ServerGuiItem item : items.values()) {
            inventory.setItem(item.getSlot(), buildItemStack(item));
        }

        return inventory;
    }

    private ItemStack buildItemStack(ServerGuiItem item) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        meta.setDisplayName(HotbarManager.colorize(item.getDisplayName()));

        ServerStatus status = plugin.getProxyBridge().getStatus(item.getTargetServer());
        boolean hasFreshData = status != null && status.isFresh(syncMaxAgeMillis);

        List<String> lore = new ArrayList<>();
        if (!hasFreshData) {
            lore.addAll(colorizeAll(noDataLore));
        } else if (status.isOpen()) {
            lore.addAll(colorizeAll(applyPlaceholders(openLoreTemplate, status)));
        } else {
            lore.addAll(colorizeAll(applyPlaceholders(closedLoreTemplate, status)));
        }

        if (!item.getExtraLore().isEmpty()) {
            lore.add("");
            lore.addAll(colorizeAll(item.getExtraLore()));
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.values());
        meta.getPersistentDataContainer().set(guiItemIdKey, PersistentDataType.STRING, item.getId());

        if (item.isGlowWhenOpen() && hasFreshData && status.isOpen()) {
            // Astuce classique pour un effet "brillant" (glint) sur l'icône :
            // un enchantement inoffensif + ItemFlag.HIDE_ENCHANTS (déjà
            // ajouté ci-dessus) pour ne pas afficher son nom dans le lore.
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        }

        stack.setItemMeta(meta);
        return stack;
    }

    private List<String> applyPlaceholders(List<String> template, ServerStatus status) {
        int max = status.getMax() > 0 ? status.getMax() : fallbackMaxPlayers;
        List<String> result = new ArrayList<>();
        for (String line : template) {
            result.add(line
                    .replace("{online}", String.valueOf(status.getOnline()))
                    .replace("{max}", String.valueOf(max)));
        }
        return result;
    }

    private List<String> colorizeAll(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(HotbarManager.colorize(line));
        }
        return result;
    }

    public String getItemIdOf(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(guiItemIdKey, PersistentDataType.STRING);
    }
}
