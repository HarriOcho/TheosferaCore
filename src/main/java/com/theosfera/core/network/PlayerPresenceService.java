package com.theosfera.core.network;

import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.PlayerServerReadyPayload;
import org.bukkit.entity.Player;

import java.time.Clock;
import java.util.Objects;

public final class PlayerPresenceService {

    private final BackendNetworkConfig config;
    private final BackendHandshakeService handshakeService;
    private final ProtocolMessageSender messageSender;
    private final Clock clock;

    public PlayerPresenceService(
            BackendNetworkConfig config,
            BackendHandshakeService handshakeService,
            ProtocolMessageSender messageSender
    ) {
        this(
                config,
                handshakeService,
                messageSender,
                Clock.systemUTC()
        );
    }

    PlayerPresenceService(
            BackendNetworkConfig config,
            BackendHandshakeService handshakeService,
            ProtocolMessageSender messageSender,
            Clock clock
    ) {
        this.config = Objects.requireNonNull(
                config,
                "config cannot be null"
        );
        this.handshakeService = Objects.requireNonNull(
                handshakeService,
                "handshakeService cannot be null"
        );
        this.messageSender = Objects.requireNonNull(
                messageSender,
                "messageSender cannot be null"
        );
        this.clock = Objects.requireNonNull(
                clock,
                "clock cannot be null"
        );
    }

    public boolean announceReady(Player player) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        if (!handshakeService.isAuthorized()
                || !player.isOnline()) {
            return false;
        }

        long eventTimestamp = clock.millis();

        PlayerServerReadyPayload payload =
                new PlayerServerReadyPayload(
                        player.getUniqueId(),
                        config.backendName(),
                        eventTimestamp
                );

        ProtocolEnvelope<PlayerServerReadyPayload> envelope =
                ProtocolEnvelope.create(
                        ProtocolMessageType.PLAYER_SERVER_READY,
                        payload
                );

        return messageSender.send(
                player,
                envelope
        );
    }
}
