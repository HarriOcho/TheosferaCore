package com.theosfera.core.network.transfer;

import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.TransferResultPayload;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record PendingPlayerTransfer(
        UUID requestId,
        UUID playerId,
        BackendType targetBackendType,
        long createdAt,
        CompletableFuture<TransferResultPayload> completion
) {

    public PendingPlayerTransfer {
        Objects.requireNonNull(
                requestId,
                "requestId cannot be null"
        );
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );
        Objects.requireNonNull(
                targetBackendType,
                "targetBackendType cannot be null"
        );
        Objects.requireNonNull(
                completion,
                "completion cannot be null"
        );

        if (targetBackendType == BackendType.AUTH) {
            throw new IllegalArgumentException(
                    "AUTH cannot be used as a transfer target"
            );
        }

        if (createdAt < 0L) {
            throw new IllegalArgumentException(
                    "createdAt cannot be negative"
            );
        }
    }
}
