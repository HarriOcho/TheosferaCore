package com.theosfera.core.network.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PendingPlayerTransferRegistry {

    private final Map<UUID, PendingPlayerTransfer> byRequest =
            new HashMap<>();

    private final Map<UUID, PendingPlayerTransfer> byPlayer =
            new HashMap<>();

    public synchronized PlayerTransferRegistrationResult register(
            PendingPlayerTransfer transfer
    ) {
        Objects.requireNonNull(
                transfer,
                "transfer cannot be null"
        );

        if (byPlayer.containsKey(transfer.playerId())) {
            return PlayerTransferRegistrationResult
                    .PLAYER_ALREADY_PENDING;
        }

        if (byRequest.containsKey(transfer.requestId())) {
            return PlayerTransferRegistrationResult
                    .REQUEST_ID_CONFLICT;
        }

        byRequest.put(
                transfer.requestId(),
                transfer
        );
        byPlayer.put(
                transfer.playerId(),
                transfer
        );

        return PlayerTransferRegistrationResult.REGISTERED;
    }

    public synchronized Optional<PendingPlayerTransfer> findByRequest(
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

    public synchronized Optional<PendingPlayerTransfer> findByPlayer(
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

    public synchronized Optional<PendingPlayerTransfer> remove(
            UUID requestId
    ) {
        Objects.requireNonNull(
                requestId,
                "requestId cannot be null"
        );

        PendingPlayerTransfer removed =
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

    public synchronized Optional<PendingPlayerTransfer> removeByPlayer(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        PendingPlayerTransfer removed =
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

    public synchronized List<PendingPlayerTransfer> drain() {
        List<PendingPlayerTransfer> transfers =
                new ArrayList<>(byRequest.values());

        byRequest.clear();
        byPlayer.clear();

        return List.copyOf(transfers);
    }

    public synchronized int size() {
        return byRequest.size();
    }
}
