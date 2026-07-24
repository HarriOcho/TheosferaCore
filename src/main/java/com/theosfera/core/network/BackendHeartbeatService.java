package com.theosfera.core.network;

import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.PingPayload;
import com.theosfera.protocol.message.payload.PongPayload;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.LongSupplier;

final class BackendHeartbeatService {

    private final BackendHandshakeService handshakeService;
    private final ProtocolMessageSender messageSender;
    private final LongSupplier currentTimeMillis;

    BackendHeartbeatService(
            BackendHandshakeService handshakeService,
            ProtocolMessageSender messageSender
    ) {
        this(
                handshakeService,
                messageSender,
                System::currentTimeMillis
        );
    }

    BackendHeartbeatService(
            BackendHandshakeService handshakeService,
            ProtocolMessageSender messageSender,
            LongSupplier currentTimeMillis
    ) {
        this.handshakeService = Objects.requireNonNull(
                handshakeService,
                "handshakeService cannot be null"
        );

        this.messageSender = Objects.requireNonNull(
                messageSender,
                "messageSender cannot be null"
        );

        this.currentTimeMillis = Objects.requireNonNull(
                currentTimeMillis,
                "currentTimeMillis cannot be null"
        );
    }

    void handlePing(
            Player carrier,
            ProtocolEnvelope<PingPayload> envelope
    ) {
        Objects.requireNonNull(
                carrier,
                "carrier cannot be null"
        );

        Objects.requireNonNull(
                envelope,
                "envelope cannot be null"
        );

        if (!handshakeService.isAuthorized()) {
            return;
        }

        long respondedAt = Math.max(
                currentTimeMillis.getAsLong(),
                envelope.payload().sentAt()
        );

        PongPayload payload = new PongPayload(
                envelope.payload().sentAt(),
                respondedAt
        );

        ProtocolEnvelope<PongPayload> response =
                new ProtocolEnvelope<>(
                        envelope.version(),
                        ProtocolMessageType.PONG,
                        envelope.requestId(),
                        respondedAt,
                        payload
                );

        messageSender.send(
                carrier,
                response
        );
    }
}
