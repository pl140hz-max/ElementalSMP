package com.elementalsmp.gems.managers;

import java.util.*;

public class TrustManager {

    // Map storing Player UUID -> Set of Trusted Player UUIDs
    private final Map<UUID, Set<UUID>> trustedPlayers = new HashMap<>();

    /**
     * Trusts a target player.
     */
    public void trustPlayer(UUID player, UUID target) {
        trustedPlayers.computeIfAbsent(player, k -> new HashSet<>()).add(target);
    }

    /**
     * Untrusts a target player.
     */
    public void untrustPlayer(UUID player, UUID target) {
        Set<UUID> trusted = trustedPlayers.get(player);
        if (trusted != null) {
            trusted.remove(target);
        }
    }

    /**
     * Checks if a target player is trusted by the main player.
     */
    public boolean isTrusted(UUID player, UUID target) {
        Set<UUID> trusted = trustedPlayers.get(player);
        return trusted != null && trusted.contains(target);
    }

    /**
     * Returns the set of UUIDs trusted by the player.
     * Required for /trustlist in TrustCommand.
     */
    public Set<UUID> getTrusted(UUID player) {
        return trustedPlayers.getOrDefault(player, Collections.emptySet());
    }
}
