package com.compasshotbar;

import org.bukkit.Material;

import java.util.Collections;
import java.util.List;

/**
 * Représente une icône du menu "Serveurs / Mini-jeux" ouvert par la
 * boussole : un slot, un matériau/nom/lore personnalisables, et le nom
 * du serveur Velocity cible (celui déclaré dans velocity.toml) vers
 * lequel envoyer le joueur au clic.
 */
public class ServerGuiItem {

    private final String id;
    private final int slot;
    private final Material material;
    private final String displayName;
    private final List<String> extraLore;
    private final String targetServer;
    private final boolean glowWhenOpen;

    public ServerGuiItem(String id, int slot, Material material, String displayName,
                          List<String> extraLore, String targetServer, boolean glowWhenOpen) {
        this.id = id;
        this.slot = slot;
        this.material = material;
        this.displayName = displayName;
        this.extraLore = extraLore == null ? Collections.emptyList() : extraLore;
        this.targetServer = targetServer;
        this.glowWhenOpen = glowWhenOpen;
    }

    public String getId() {
        return id;
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

    public List<String> getExtraLore() {
        return extraLore;
    }

    /**
     * Nom du serveur tel que déclaré côté proxy (velocity.toml [servers]).
     * C'est ce nom qui est envoyé dans le paquet "Connect" et qui doit
     * correspondre à celui utilisé par le plugin Velocity pour le ping.
     */
    public String getTargetServer() {
        return targetServer;
    }

    public boolean isGlowWhenOpen() {
        return glowWhenOpen;
    }
}
