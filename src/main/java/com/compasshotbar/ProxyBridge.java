package com.compasshotbar;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Gère les échanges avec le proxy Velocity :
 *
 *  - Canal "compasshotbar:sync" (entrant) : le plugin Velocity
 *    (CompassHotbarVelocity) pousse régulièrement un petit texte listant
 *    tous les serveurs connus, leur nombre de joueurs et s'ils répondent
 *    au ping ("ouvert"/"fermé"). Format d'une ligne :
 *        nom|joueurs|maxJoueurs|ouvert(0 ou 1)
 *    plusieurs lignes séparées par "\n".
 *
 *  - Canal "bungeecord:main" (sortant) : canal legacy nativement supporté
 *    par Velocity (aucun plugin nécessaire côté proxy) pour envoyer un
 *    joueur vers un autre serveur via la sous-commande "Connect".
 *
 * Si le plugin Velocity n'est pas installé, le premier canal ne recevra
 * simplement jamais rien : le menu affichera alors "Chargement..." pour
 * tous les serveurs (voir ServerGuiManager).
 */
public class ProxyBridge implements PluginMessageListener {

    public static final String SYNC_CHANNEL = "compasshotbar:sync";
    public static final String BUNGEE_CHANNEL = "bungeecord:main";

    private final CompassHotbar plugin;
    private final Map<String, ServerStatus> statuses = new ConcurrentHashMap<>();

    public ProxyBridge(CompassHotbar plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(SYNC_CHANNEL)) return;

        String payload = new String(message, StandardCharsets.UTF_8);
        long now = System.currentTimeMillis();

        for (String line : payload.split("\n")) {
            if (line.isBlank()) continue;
            String[] parts = line.split("\\|", -1);
            if (parts.length < 4) continue;

            try {
                String name = parts[0];
                int online = Integer.parseInt(parts[1].trim());
                int max = Integer.parseInt(parts[2].trim());
                boolean open = parts[3].trim().equals("1");
                statuses.put(name, new ServerStatus(online, max, open, now));
            } catch (NumberFormatException ex) {
                plugin.getLogger().log(Level.FINE, "Ligne de statut illisible reçue de Velocity : " + line);
            }
        }
    }

    /**
     * Statut connu d'un serveur, ou null si aucune donnée n'a encore été
     * reçue pour ce nom (soit le plugin Velocity vient de démarrer, soit
     * il n'est pas installé, soit le nom ne correspond à aucun serveur
     * déclaré côté proxy).
     */
    public ServerStatus getStatus(String serverName) {
        return statuses.get(serverName);
    }

    /**
     * Envoie un joueur vers un autre serveur backend via le proxy
     * (fonctionne avec Velocity ET BungeeCord, aucune configuration
     * supplémentaire nécessaire côté proxy).
     */
    public void connect(Player player, String targetServer) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArray);
            out.writeUTF("Connect");
            out.writeUTF(targetServer);
            player.sendPluginMessage(plugin, BUNGEE_CHANNEL, byteArray.toByteArray());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Impossible d'envoyer la demande de connexion vers " + targetServer, ex);
        }
    }
}
