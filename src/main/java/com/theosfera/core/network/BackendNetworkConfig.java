package com.theosfera.core.network;

import com.theosfera.protocol.message.payload.BackendType;

import java.util.Objects;

public record BackendNetworkConfig(
        boolean enabled,
        String backendName,
        BackendType backendType
) {

    public BackendNetworkConfig {
        backendName = Objects.requireNonNull(
                backendName,
                "backendName cannot be null"
        ).trim();

        Objects.requireNonNull(
                backendType,
                "backendType cannot be null"
        );

        if (enabled && backendName.isEmpty()) {
            throw new IllegalArgumentException(
                    "backendName cannot be blank when networking is enabled"
            );
        }
    }

    public boolean isAuthenticationBackend() {
        return backendType == BackendType.AUTH;
    }

    public boolean allowsTransferTo(
            BackendType targetBackendType
    ) {
        Objects.requireNonNull(
                targetBackendType,
                "targetBackendType cannot be null"
        );

        return targetBackendType != BackendType.AUTH
                && (isPlayableBackend()
                || isAuthenticationBackend()
                && targetBackendType == BackendType.LOBBY);
    }

    public boolean isPlayableBackend() {
        return backendType == BackendType.LOBBY
                || backendType == BackendType.SKYBLOCK;
    }

    public static BackendNetworkConfig disabled() {
        return new BackendNetworkConfig(
                false,
                "",
                BackendType.LOBBY
        );
    }
}
