package com.theosfera.core.network.auth;

import com.theosfera.protocol.message.payload.PlayerAuthenticatedAckPayload;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record PlayerAuthenticationRequest(
        PlayerAuthenticationRequestStatus status,
        UUID requestId,
        CompletableFuture<PlayerAuthenticatedAckPayload> completion
) {

    public PlayerAuthenticationRequest {
        Objects.requireNonNull(
                status,
                "status cannot be null"
        );

        if (status
                == PlayerAuthenticationRequestStatus.SUBMITTED) {
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

    public static PlayerAuthenticationRequest submitted(
            UUID requestId,
            CompletableFuture<PlayerAuthenticatedAckPayload> completion
    ) {
        return new PlayerAuthenticationRequest(
                PlayerAuthenticationRequestStatus.SUBMITTED,
                requestId,
                completion
        );
    }

    public static PlayerAuthenticationRequest rejected(
            PlayerAuthenticationRequestStatus status
    ) {
        if (status
                == PlayerAuthenticationRequestStatus.SUBMITTED) {
            throw new IllegalArgumentException(
                    "SUBMITTED cannot be used as a rejection status"
            );
        }

        return new PlayerAuthenticationRequest(
                status,
                null,
                null
        );
    }

    public Optional<UUID> optionalRequestId() {
        return Optional.ofNullable(requestId);
    }

    public Optional<CompletableFuture<PlayerAuthenticatedAckPayload>>
    optionalCompletion() {
        return Optional.ofNullable(completion);
    }
}
