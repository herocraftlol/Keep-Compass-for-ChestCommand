package com.compasshotbar;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Iterator;

public class PlayerListener implements Listener {

    private final CompassHotbar plugin;

    public PlayerListener(CompassHotbar plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.syncZoneState(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.syncZoneState(event.getPlayer());
    }

    /**
     * Détecte les entrées/sorties de la zone (pos1/pos2) pour donner ou
     * retirer les items de la hotbar en conséquence. On ne fait le calcul
     * complet que si le joueur a changé de bloc, pour ne pas surcharger le
     * serveur (PlayerMoveEvent se déclenche très souvent).
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.isPluginEnabled()) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("compasshotbar.use")) return;

        ZoneManager zone = plugin.getZoneManager();
        if (!zone.isRestrictionActive()) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getWorld().equals(to.getWorld())
                && from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return; // Pas de changement de bloc : rien à vérifier.
        }

        boolean wasIn = zone.contains(from);
        boolean isIn = zone.contains(to);
        if (wasIn == isIn) return; // Toujours dans le même état (dedans ou dehors).

        plugin.syncZoneState(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.isPluginEnabled()) return;

        HotbarManager manager = plugin.getHotbarManager();
        Iterator<ItemStack> drops = event.getDrops().iterator();
        boolean removedAny = false;

        while (drops.hasNext()) {
            ItemStack drop = drops.next();
            if (manager.getItemId(drop) != null) {
                drops.remove();
                removedAny = true;
            }
        }

        if (removedAny) {
            event.setKeepInventory(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        ItemStack droppedItem = event.getItemDrop().getItemStack();
        String itemId = plugin.getHotbarManager().getItemId(droppedItem);

        if (itemId != null) {
            event.setCancelled(true);
            String message = plugin.getConfig().getString("drop-blocked-message",
                    "&6[CompassHotbar] &cVous ne pouvez pas lâcher cet item!");
            player.sendMessage(HotbarManager.colorize(message));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        // On ne protège les items QUE si le clic se fait directement
        // dans l'inventaire du joueur (pas dans un coffre, une table de craft, etc.)
        if (event.getClickedInventory() instanceof PlayerInventory playerInv
                && playerInv == player.getInventory()) {

            ItemStack currentItem = event.getCurrentItem();
            String itemId = plugin.getHotbarManager().getItemId(currentItem);

            if (itemId != null) {
                // Bloquer les déplacements (shift-clic, move_to_other_inventory…)
                if (event.isShiftClick() || event.getAction().name().contains("MOVE")) {
                    event.setCancelled(true);
                    return;
                }

                // Si le joueur essaie d'échanger un item avec le curseur,
                // on annule sans dropper l'item : l'item reste sur le curseur.
                if (event.getCursor() != null && event.getCursor().getType() != org.bukkit.Material.AIR) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        HotbarManager manager = plugin.getHotbarManager();
        PlayerInventory inventory = player.getInventory();

        // Annuler le glissé s'il touche un slot occupé par un item du plugin.
        for (int rawSlot : event.getRawSlots()) {
            if (event.getView().getInventory(rawSlot) == inventory) {
                int slot = event.getView().convertSlot(rawSlot);
                ItemStack current = inventory.getItem(slot);
                if (manager.getItemId(current) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        // Bukkit déclenche cet évènement pour la main principale ET la main
        // secondaire : on ne traite que la main principale pour éviter un
        // double envoi du lien / une double exécution de la commande.
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        String itemId = plugin.getHotbarManager().getItemId(item);
        if (itemId == null) return;

        HotbarItem hotbarItem = plugin.getHotbarManager().getItem(itemId);
        if (hotbarItem == null) return;

        if (event.getAction().name().contains("RIGHT")) {
            // On annule l'interaction par défaut (poser un bloc comme la toile
            // d'araignée, etc.) uniquement pour les items qui ont une action
            // (URL/commande). La boussole garde son comportement normal.
            if (hotbarItem.getActionType() != HotbarItem.ActionType.NONE) {
                event.setCancelled(true);
            }
            plugin.ensureAllItems(player);
            ActionHandler.executeFor(player, hotbarItem);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        plugin.ensureAllItems(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(ServerLoadEvent event) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.syncZoneState(player);
        }
    }
}
