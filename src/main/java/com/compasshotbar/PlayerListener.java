package com.compasshotbar;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerListener implements Listener {

    private final CompassHotbar plugin;
    private static final int COMPASS_SLOT = 8;

    public PlayerListener(CompassHotbar plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPluginEnabled()) {
            plugin.giveCompass(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();;
        if (plugin.isPluginEnabled()) {
            plugin.giveCompass(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.isPluginEnabled()) return;

        PlayerInventory inventory = player.getInventory();
        ItemStack compass = inventory.getItem(COMPASS_SLOT);

        if (compass != null && compass.getType() == Material.COMPASS) {
            event.getDrops().remove(compass);
            event.setKeepInventory(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        ItemStack droppedItem = event.getItemDrop().getItemStack();

        if (droppedItem.getType() == Material.COMPASS) {
            event.setCancelled(true);
            player.sendMessage("§6[CompassHotbar] §cVous ne pouvez pas lâcher votre boussole!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        // On ne protège la boussole QUE si le clic se fait directement
        // dans l'inventaire du joueur (pas dans un coffre, une table de craft, etc.)
        // et précisément sur le slot 8.
        if (event.getClickedInventory() instanceof PlayerInventory playerInv
                && playerInv == player.getInventory()
                && event.getSlot() == COMPASS_SLOT) {

            ItemStack currentItem = event.getCurrentItem();

            if (currentItem != null && currentItem.getType() == Material.COMPASS) {
                // Bloquer les déplacements (shift-clic, move_to_other_inventory…)
                if (event.isShiftClick() || event.getAction().name().contains("MOVE")) {
                    event.setCancelled(true);
                    return;
                }

                // Si le joueur essaie d'échanger un item avec la boussole via le curseur,
                // on annule sans dropper l'item : l'item reste sur le curseur.
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
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

        ItemStack compass = player.getInventory().getItem(COMPASS_SLOT);
        if (compass == null || compass.getType() != Material.COMPASS) return;

        // Annuler le glissé uniquement s'il touche le slot 8 de l'inventaire du joueur.
        for (int rawSlot : event.getRawSlots()) {
            if (event.getView().getInventory(rawSlot) == player.getInventory()
                    && event.getView().convertSlot(rawSlot) == COMPASS_SLOT) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.COMPASS) {
            if (event.getAction().name().contains("RIGHT")) {
                plugin.ensureCompassInSlot(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!plugin.isPluginEnabled()) return;
        if (!player.hasPermission("compasshotbar.use")) return;

        plugin.ensureCompassInSlot(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(ServerLoadEvent event) {
        if (plugin.isPluginEnabled()) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                plugin.giveCompass(player);
            }
        }
    }
}
