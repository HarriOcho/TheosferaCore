package com.theosfera.core.network.auth;

import com.theosfera.protocol.message.payload.PlayerAuthenticatedAckPayload;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record PendingPlayerAuthentication(
        UUID requestId,
        UUID playerId,
        long createdAt,
        CompletableFuture<PlayerAuthenticatedAckPayload> completion
) {

    public PendingPlayerAuthentication {
        Objects.requireNonNull(
                requestId,
                "requestId cannot be null"
        );

        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        Objects.requireNonNull(
                completion,
                "completion cannot be null"
        );

        if (createdAt < 0L) {
            throw new IllegalArgumentException(
                    "createdAt cannot be negative"
            );
        }
    }
}
