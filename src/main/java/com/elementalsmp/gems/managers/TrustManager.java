package com.elementalsmp.gems.managers;

import java.util.*;

public class TrustManager {

    // Key: Player UUID, Value: Set of Trusted Player UUIDs
    private final Map<UUID, Set<UUID>> trustedPlayers = new HashMap<>();

    public boolean trustPlayer(UUID owner, UUID target) {
        trustedPlayers.putIfAbsent(owner, new HashSet<>());
        return trustedPlayers.get(owner).add(target);
    }

    public boolean untrustPlayer(UUID owner, UUID target) {
        if (!trustedPlayers.containsKey(owner)) return false;
        return trustedPlayers.get(owner).remove(target);
    }

    public boolean isTrusted(UUID owner, UUID target) {
        return trustedPlayers.getOrDefault(owner, Collections.emptySet()).contains(target);
    }

    public Set<UUID> getTrustedPlayers(UUID owner) {
        return trustedPlayers.getOrDefault(owner, Collections.emptySet());
    }
}