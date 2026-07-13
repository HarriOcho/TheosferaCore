package com.theosfera.core.network;

import com.theosfera.protocol.message.ProtocolEnvelope;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ProtocolMessageDispatcher {

    private final Map<String, RegisteredHandler<?>> handlers =
            new HashMap<>();

    public <T> void register(
            String messageType,
            Class<T> payloadType,
            ProtocolMessageHandler<T> handler
    ) {
        Objects.requireNonNull(
                messageType,
                "messageType cannot be null"
        );
        Objects.requireNonNull(
                payloadType,
                "payloadType cannot be null"
        );
        Objects.requireNonNull(
                handler,
                "handler cannot be null"
        );

        if (messageType.isBlank()) {
            throw new IllegalArgumentException(
                    "messageType cannot be blank"
            );
        }

        RegisteredHandler<T> registeredHandler =
                new RegisteredHandler<>(
                        payloadType,
                        handler
                );

        if (handlers.putIfAbsent(
                messageType,
                registeredHandler
        ) != null) {
            throw new IllegalStateException(
                    "A handler is already registered for "
                            + messageType
            );
        }
    }

    public boolean dispatch(
            Player carrier,
            ProtocolEnvelope<?> envelope
    ) {
        Objects.requireNonNull(
                carrier,
                "carrier cannot be null"
        );
        Objects.requireNonNull(
                envelope,
                "envelope cannot be null"
        );

        RegisteredHandler<?> registeredHandler =
                handlers.get(envelope.type());

        if (registeredHandler == null) {
            return false;
        }

        return registeredHandler.dispatch(
                carrier,
                envelope
        );
    }

    private record RegisteredHandler<T>(
            Class<T> payloadType,
            ProtocolMessageHandler<T> handler
    ) {

        private RegisteredHandler {
            Objects.requireNonNull(
                    payloadType,
                    "payloadType cannot be null"
            );
            Objects.requireNonNull(
                    handler,
                    "handler cannot be null"
            );
        }

        private boolean dispatch(
                Player carrier,
                ProtocolEnvelope<?> envelope
        ) {
            Object payload = envelope.payload();

            if (!payloadType.isInstance(payload)) {
                return false;
            }

            handler.handle(
                    carrier,
                    castEnvelope(envelope)
            );
            return true;
        }

        @SuppressWarnings("unchecked")
        private ProtocolEnvelope<T> castEnvelope(
                ProtocolEnvelope<?> envelope
        ) {
            return (ProtocolEnvelope<T>) envelope;
        }
    }
}
