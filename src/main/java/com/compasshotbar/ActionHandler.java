package com.compasshotbar;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Exécute l'action (URL ou commande) associée à un HotbarItem lorsqu'un
 * joueur clique-droit dessus.
 *
 * NB: Minecraft (édition Java) ne permet pas à un serveur d'ouvrir
 * directement le navigateur d'un joueur : c'est une protection du client
 * lui-même, pas une limite du plugin. La seule approche possible est un
 * message de chat cliquable (ClickEvent.OPEN_URL) : le lien s'ouvre dans le
 * navigateur par défaut du joueur dès qu'il clique dessus. On maximise ici
 * la visibilité de ce message (encadré, actionbar, son) pour que le joueur
 * ne le rate pas.
 */
public class ActionHandler {

    private static final String DEFAULT_SOUND_URL = "ENTITY_EXPERIENCE_ORB_PICKUP";
    private static final String DEFAULT_SOUND_COMMAND = "UI_BUTTON_CLICK";

    public static void executeFor(Player player, HotbarItem item) {
        switch (item.getActionType()) {
            case URL -> sendClickableLink(player, item);
            case COMMAND -> runCommand(player, item);
            case NONE -> { /* rien à faire, ex: la boussole */ }
        }
    }

    private static void sendClickableLink(Player player, HotbarItem item) {
        String url = item.getActionValue();
        if (url == null || url.isEmpty()) return;

        String message = item.getClickMessage() != null
                ? item.getClickMessage()
                : "&a&l▶▶▶ &f&lCLIQUE ICI &f&lpour ouvrir : &e&n" + url + "&r";

        String border = "&8&m                                                  ";

        // Bordure + message + bordure : ça détache bien le lien du reste du chat.
        player.sendMessage(HotbarManager.colorize(border));

        TextComponent component = new TextComponent(HotbarManager.colorize(message));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(new ComponentBuilder(HotbarManager.colorize("&7Cliquez pour ouvrir : &f" + url)).create())));
        player.spigot().sendMessage(component);

        player.sendMessage(HotbarManager.colorize(border));

        // Rappel dans l'actionbar, bien visible au-dessus de la hotbar.
        String actionbar = item.getActionbarMessage() != null
                ? item.getActionbarMessage()
                : "&e➤ &fUn lien cliquable vient d'apparaître dans le chat !";
        sendActionbar(player, actionbar);

        playSound(player, item.getSoundName() != null ? item.getSoundName() : DEFAULT_SOUND_URL);
    }

    private static void runCommand(Player player, HotbarItem item) {
        String command = item.getActionValue();
        if (command == null || command.isEmpty()) return;

        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        player.performCommand(command);

        if (item.getActionbarMessage() != null) {
            sendActionbar(player, item.getActionbarMessage());
        }

        playSound(player, item.getSoundName() != null ? item.getSoundName() : DEFAULT_SOUND_COMMAND);
    }

    private static void sendActionbar(Player player, String message) {
        TextComponent component = new TextComponent(HotbarManager.colorize(message));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    private static void playSound(Player player, String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (IllegalArgumentException ex) {
            CompassHotbar.getInstance().getLogger().log(Level.WARNING,
                    "Son invalide '" + soundName + "' dans la config, ignoré.");
        }
    }
}
