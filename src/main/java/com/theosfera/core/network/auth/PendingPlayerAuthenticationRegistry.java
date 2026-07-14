package com.theosfera.core.network.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PendingPlayerAuthenticationRegistry {

    private final Map<UUID, PendingPlayerAuthentication>
            byRequest = new HashMap<>();

    private final Map<UUID, PendingPlayerAuthentication>
            byPlayer = new HashMap<>();

    public synchronized PlayerAuthenticationRegistrationResult
    register(
            PendingPlayerAuthentication authentication
    ) {
        Objects.requireNonNull(
                authentication,
                "authentication cannot be null"
        );

        if (byPlayer.containsKey(
                authentication.playerId()
        )) {
            return PlayerAuthenticationRegistrationResult
                    .PLAYER_ALREADY_PENDING;
        }

        if (byRequest.containsKey(
                authentication.requestId()
        )) {
            return PlayerAuthenticationRegistrationResult
                    .REQUEST_ID_CONFLICT;
        }

        byRequest.put(
                authentication.requestId(),
                authentication
        );

        byPlayer.put(
                authentication.playerId(),
                authentication
        );

        return PlayerAuthenticationRegistrationResult
                .REGISTERED;
    }

    public synchronized Optional<PendingPlayerAuthentication>
    findByRequest(
            UUID requestId
    ) {
        Objects.requireNonNull(
                requestId,
                "requestId cannot be null"
        );

        return Optional.ofNullable(
                byRequest.get(requestId)
        );
    }

    public synchronized Optional<PendingPlayerAuthentication>
    findByPlayer(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        return Optional.ofNullable(
                byPlayer.get(playerId)
        );
    }

    public synchronized Optional<PendingPlayerAuthentication>
    remove(
            UUID requestId
    ) {
        Objects.requireNonNull(
                requestId,
                "requestId cannot be null"
        );

        PendingPlayerAuthentication removed =
                byRequest.remove(requestId);

        if (removed == null) {
            return Optional.empty();
        }

        byPlayer.remove(
                removed.playerId(),
                removed
        );

        return Optional.of(removed);
    }

    public synchronized Optional<PendingPlayerAuthentication>
    removeByPlayer(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        PendingPlayerAuthentication removed =
                byPlayer.remove(playerId);

        if (removed == null) {
            return Optional.empty();
        }

        byRequest.remove(
                removed.requestId(),
                removed
        );

        return Optional.of(removed);
    }

    public synchronized List<PendingPlayerAuthentication>
    drain() {
        List<PendingPlayerAuthentication> authentications =
                new ArrayList<>(byRequest.values());

        byRequest.clear();
        byPlayer.clear();

        return List.copyOf(authentications);
    }

    public synchronized int size() {
        return byRequest.size();
    }
}
