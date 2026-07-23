package com.compasshotbar;

/**
 * Dernier statut connu d'un serveur backend, tel que rapporté par le
 * plugin Velocity (CompassHotbarVelocity) via le canal "compasshotbar:sync".
 */
public class ServerStatus {

    private final int online;
    private final int max;
    private final boolean open;
    private final long receivedAt;

    public ServerStatus(int online, int max, boolean open, long receivedAt) {
        this.online = online;
        this.max = max;
        this.open = open;
        this.receivedAt = receivedAt;
    }

    public int getOnline() {
        return online;
    }

    public int getMax() {
        return max;
    }

    public boolean isOpen() {
        return open;
    }

    /**
     * True si cette donnée a été reçue il y a moins de "maxAgeMillis" ms.
     * Permet de considérer une donnée trop vieille (proxy coupé, etc.)
     * comme "pas de donnée" plutôt que d'afficher un statut périmé.
     */
    public boolean isFresh(long maxAgeMillis) {
        return System.currentTimeMillis() - receivedAt <= maxAgeMillis;
    }
}
