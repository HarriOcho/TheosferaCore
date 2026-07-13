package com.theosfera.core.network.transfer;

import com.theosfera.protocol.message.payload.TransferResultPayload;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record PlayerTransferRequest(
        PlayerTransferRequestStatus status,
        UUID requestId,
        CompletableFuture<TransferResultPayload> completion
) {

    public PlayerTransferRequest {
        Objects.requireNonNull(
                status,
                "status cannot be null"
        );

        if (status == PlayerTransferRequestStatus.SUBMITTED) {
            Objects.requireNonNull(
                    requestId,
                    "requestId cannot be null for a submitted request"
            );
            Objects.requireNonNull(
                    completion,
                    "completion cannot be null for a submitted request"
            );
        } else if (requestId != null || completion != null) {
            throw new IllegalArgumentException(
                    "A rejected request cannot contain pending state"
            );
        }
    }

    public static PlayerTransferRequest submitted(
            UUID requestId,
            CompletableFuture<TransferResultPayload> completion
    ) {
        return new PlayerTransferRequest(
                PlayerTransferRequestStatus.SUBMITTED,
                requestId,
                completion
        );
    }

    public static PlayerTransferRequest rejected(
            PlayerTransferRequestStatus status
    ) {
        if (status == PlayerTransferRequestStatus.SUBMITTED) {
            throw new IllegalArgumentException(
                    "SUBMITTED cannot be used as a rejection status"
            );
        }

        return new PlayerTransferRequest(
                status,
                null,
                null
        );
    }

    public Optional<UUID> optionalRequestId() {
        return Optional.ofNullable(requestId);
    }

    public Optional<CompletableFuture<TransferResultPayload>>
    optionalCompletion() {
        return Optional.ofNullable(completion);
    }
}
