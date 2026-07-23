package com.compasshotbar.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * CompassHotbarVelocity
 * ---------------------
 * Petit plugin proxy, indépendant du plugin backend "CompassHotbar" (celui
 * qui gère la boussole/hotbar sur chaque serveur Spigot/Paper). Son unique
 * rôle : pinguer régulièrement chaque serveur backend déclaré dans
 * velocity.toml et pousser un résumé texte (nom, joueurs, max, ouvert ou
 * non) à tous les serveurs actuellement en ligne, via le canal plugin
 * "compasshotbar:sync".
 *
 * Le plugin CompassHotbar (Spigot) utilise ensuite ces données pour
 * afficher, dans le menu ouvert par clic-droit sur la boussole, le
 * nombre de joueurs et le statut (ouvert/fermé) de chaque mini-jeu.
 *
 * Le transfert des joueurs d'un serveur à l'autre (clic sur une icône)
 * n'a PAS besoin de ce plugin : il utilise le canal legacy
 * "bungeecord:main" ("Connect"), déjà supporté nativement par Velocity.
 */
@Plugin(
        id = "compasshotbar-velocity",
        name = "CompassHotbarVelocity",
        version = "1.0.0",
        description = "Ping les serveurs backend et pousse leur statut vers CompassHotbar",
        authors = {"Developer"}
)
public class CompassHotbarVelocity {

    private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("compasshotbar", "sync");

    // Toutes les X secondes, on repinge tous les serveurs et on republie l'état.
    private static final long INTERVAL_SECONDS = 5L;

    // Délai maximum accordé à un ping avant de considérer le serveur comme fermé.
    private static final long PING_TIMEOUT_SECONDS = 3L;

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public CompassHotbarVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(CHANNEL);

        server.getScheduler()
                .buildTask(this, this::broadcastStatus)
                .repeat(INTERVAL_SECONDS, TimeUnit.SECONDS)
                .schedule();

        logger.info("CompassHotbarVelocity activé : ping toutes les {}s, canal {} enregistré.",
                INTERVAL_SECONDS, CHANNEL.getId());
    }

    /**
     * Ping tous les serveurs connus en parallèle, construit le résumé texte
     * une fois toutes les réponses (ou timeouts) arrivées, puis l'envoie à
     * chaque serveur backend actuellement en ligne.
     */
    private void broadcastStatus() {
        Collection<RegisteredServer> servers = server.getAllServers();
        StringBuilder payload = new StringBuilder();

        CompletableFuture<?>[] futures = servers.stream()
                .map(registered -> pingOne(registered, payload))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).whenComplete((ignored, throwable) -> {
            String message = payload.toString();
            if (message.isEmpty()) return;

            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            for (RegisteredServer registered : servers) {
                for (Player player : registered.getPlayersConnected()) {
                    // On envoie via la connexion joueur->serveur : un seul
                    // joueur suffit comme "conduit" pour atteindre ce backend.
                    player.sendPluginMessage(CHANNEL, data);
                    break;
                }
            }
        });
    }

    /**
     * Ping un serveur et ajoute (en synchronisant sur le StringBuilder
     * partagé) une ligne "nom|online|max|ouvert" au payload en cours de
     * construction. Ne lève jamais d'exception : un ping raté = serveur
     * marqué fermé (0 joueur).
     */
    private CompletableFuture<Void> pingOne(RegisteredServer registered, StringBuilder payload) {
        String name = registered.getServerInfo().getName();

        return registered.ping()
                .orTimeout(PING_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .handle((ping, throwable) -> {
                    int online;
                    int max;
                    boolean open;

                    if (throwable != null || ping == null) {
                        online = 0;
                        max = 0;
                        open = false;
                    } else {
                        open = true;
                        online = ping.getPlayers().map(ServerPing.Players::getOnline).orElse(0);
                        max = ping.getPlayers().map(ServerPing.Players::getMax).orElse(0);
                    }

                    synchronized (payload) {
                        payload.append(name).append('|')
                                .append(online).append('|')
                                .append(max).append('|')
                                .append(open ? '1' : '0')
                                .append('\n');
                    }
                    return null;
                });
    }
}
