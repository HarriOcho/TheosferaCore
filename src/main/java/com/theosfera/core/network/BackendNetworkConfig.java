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

        if (enabled && backendType == BackendType.AUTH) {
            throw new IllegalArgumentException(
                    "TheosferaCore player transfers cannot run on AUTH"
            );
        }
    }

    public static BackendNetworkConfig disabled() {
        return new BackendNetworkConfig(
                false,
                "",
                BackendType.LOBBY
        );
    }
}
