package com.compasshotbar;

import org.bukkit.Material;

import java.util.Collections;
import java.util.List;

/**
 * Représente un item fixe de la hotbar (boussole, boutique, site web, discord, amis, ...)
 * défini dans la config.yml.
 */
public class HotbarItem {

    public enum ActionType {
        NONE,
        URL,
        COMMAND
    }

    private final String id;
    private final boolean enabled;
    private final int slot;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final ActionType actionType;
    private final String actionValue;
    private final String clickMessage;
    private final String actionbarMessage;
    private final String soundName;

    public HotbarItem(String id, boolean enabled, int slot, Material material, String displayName,
                       List<String> lore, ActionType actionType, String actionValue, String clickMessage,
                       String actionbarMessage, String soundName) {
        this.id = id;
        this.enabled = enabled;
        this.slot = slot;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore == null ? Collections.emptyList() : lore;
        this.actionType = actionType;
        this.actionValue = actionValue;
        this.clickMessage = clickMessage;
        this.actionbarMessage = actionbarMessage;
        this.soundName = soundName;
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSlot() {
        return slot;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getActionValue() {
        return actionValue;
    }

    public String getClickMessage() {
        return clickMessage;
    }

    public String getActionbarMessage() {
        return actionbarMessage;
    }

    public String getSoundName() {
        return soundName;
    }
}
