package com.compasshotbar;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * InventoryHolder "marqueur" : permet de détecter de façon fiable (sans
 * dépendre du titre, qui peut être personnalisé) qu'un inventaire cliqué
 * est bien le menu Serveurs/Mini-jeux du plugin.
 */
public class ServerGuiHolder implements InventoryHolder {

    private Inventory inventory;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
