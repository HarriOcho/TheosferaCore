package com.theosfera.core.network;

import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.PlayerServerReadyPayload;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerPresenceServiceTest {

    private static final UUID PLAYER_ID =
            UUID.fromString(
                    "417e98b4-74a1-467e-b453-a15be3af8996"
            );

    private static final long TIMESTAMP =
            1_750_000_000_000L;

    private BackendHandshakeService handshakeService;
    private ProtocolMessageSender messageSender;
    private PlayerPresenceService presenceService;
    private Player player;

    @BeforeEach
    void setUp() {
        handshakeService =
                mock(BackendHandshakeService.class);

        messageSender =
                mock(ProtocolMessageSender.class);

        player = mock(Player.class);

        when(player.getUniqueId()).thenReturn(PLAYER_ID);
        when(player.isOnline()).thenReturn(true);

        Clock clock = Clock.fixed(
                Instant.ofEpochMilli(TIMESTAMP),
                ZoneOffset.UTC
        );

        presenceService = new PlayerPresenceService(
                new BackendNetworkConfig(
                        true,
                        "lobby-1",
                        BackendType.LOBBY
                ),
                handshakeService,
                messageSender,
                clock
        );
    }

    @Test
    void announcesReadyPlayerAfterAuthorization() {
        when(handshakeService.isAuthorized())
                .thenReturn(true);

        when(messageSender.send(
                eq(player),
                any()
        )).thenReturn(true);

        assertTrue(
                presenceService.announceReady(player)
        );

        ArgumentCaptor<ProtocolEnvelope<?>> captor =
                ArgumentCaptor.forClass(
                        ProtocolEnvelope.class
                );

        verify(messageSender).send(
                eq(player),
                captor.capture()
        );

        ProtocolEnvelope<?> envelope =
                captor.getValue();

        assertEquals(
                ProtocolMessageType.PLAYER_SERVER_READY,
                envelope.type()
        );

        PlayerServerReadyPayload payload =
                (PlayerServerReadyPayload) envelope.payload();

        assertEquals(PLAYER_ID, payload.playerId());
        assertEquals("lobby-1", payload.backendName());
        assertEquals(TIMESTAMP, payload.readyAt());
    }

    @Test
    void refusesPresenceBeforeAuthorization() {
        when(handshakeService.isAuthorized())
                .thenReturn(false);

        assertFalse(
                presenceService.announceReady(player)
        );

        verify(
                messageSender,
                never()
        ).send(any(), any());
    }

    @Test
    void refusesOfflinePlayer() {
        when(handshakeService.isAuthorized())
                .thenReturn(true);

        when(player.isOnline()).thenReturn(false);

        assertFalse(
                presenceService.announceReady(player)
        );

        verify(
                messageSender,
                never()
        ).send(any(), any());
    }

    @Test
    void refusesPresenceFromAuthenticationBackend() {
        when(handshakeService.isAuthorized())
                .thenReturn(true);

        PlayerPresenceService authenticationPresenceService =
                new PlayerPresenceService(
                        new BackendNetworkConfig(
                                true,
                                "auth-1",
                                BackendType.AUTH
                        ),
                        handshakeService,
                        messageSender
                );

        assertFalse(
                authenticationPresenceService
                        .announceReady(player)
        );

        verify(
                messageSender,
                never()
        ).send(any(), any());
    }

    @Test
    void reportsTransportFailure() {
        when(handshakeService.isAuthorized())
                .thenReturn(true);

        when(messageSender.send(
                eq(player),
                any()
        )).thenReturn(false);

        assertFalse(
                presenceService.announceReady(player)
        );
    }
}
