package com.theosfera.core.network.auth;

import com.theosfera.core.network.BackendHandshakeService;
import com.theosfera.core.network.BackendNetworkConfig;
import com.theosfera.core.network.ProtocolMessageSender;
import com.theosfera.protocol.ProtocolVersion;
import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.PlayerAuthenticatedAckPayload;
import com.theosfera.protocol.message.payload.PlayerAuthenticatedPayload;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerAuthenticationServiceTest {

    private static final UUID PLAYER_ID =
            UUID.fromString(
                    "417e98b4-74a1-467e-b453-a15be3af8996"
            );

    private static final long AUTHENTICATED_AT =
            1_750_000_000_000L;

    private JavaPlugin plugin;
    private BackendHandshakeService handshakeService;
    private ProtocolMessageSender messageSender;
    private PendingPlayerAuthenticationRegistry registry;
    private PlayerAuthenticationService service;
    private Player player;
    private BukkitScheduler scheduler;
    private BukkitTask timeoutTask;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);

        when(plugin.getLogger())
                .thenReturn(mock(Logger.class));

        handshakeService =
                mock(BackendHandshakeService.class);

        messageSender =
                mock(ProtocolMessageSender.class);

        registry =
                new PendingPlayerAuthenticationRegistry();

        player = mock(Player.class);
        scheduler = mock(BukkitScheduler.class);
        timeoutTask = mock(BukkitTask.class);

        Server server = mock(Server.class);

        when(plugin.getServer()).thenReturn(server);
        when(server.isPrimaryThread()).thenReturn(true);
        when(server.getScheduler()).thenReturn(scheduler);

        when(scheduler.runTaskLater(
                eq(plugin),
                any(Runnable.class),
                eq(240L)
        )).thenReturn(timeoutTask);

        when(handshakeService.isAuthorized())
                .thenReturn(true);

        when(player.getUniqueId())
                .thenReturn(PLAYER_ID);

        when(player.getName())
                .thenReturn("HarriOcho");

        when(player.isOnline())
                .thenReturn(true);

        when(messageSender.send(
                eq(player),
                any()
        )).thenReturn(true);

        service = createService(
                new BackendNetworkConfig(
                        true,
                        "auth-1",
                        BackendType.AUTH
                )
        );
    }

    @Test
    void submitsAndCompletesCorrelatedAuthentication() {
        ArgumentCaptor<ProtocolEnvelope<?>> envelopeCaptor =
                ArgumentCaptor.forClass(
                        ProtocolEnvelope.class
                );

        PlayerAuthenticationRequest request =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        assertEquals(
                PlayerAuthenticationRequestStatus.SUBMITTED,
                request.status()
        );

        assertEquals(1, service.pendingCount());

        verify(messageSender).send(
                eq(player),
                envelopeCaptor.capture()
        );

        ProtocolEnvelope<?> sentEnvelope =
                envelopeCaptor.getValue();

        assertEquals(
                ProtocolMessageType.PLAYER_AUTHENTICATED,
                sentEnvelope.type()
        );

        assertEquals(
                request.optionalRequestId().orElseThrow(),
                sentEnvelope.requestId()
        );

        PlayerAuthenticatedPayload sentPayload =
                (PlayerAuthenticatedPayload)
                        sentEnvelope.payload();

        assertEquals(
                PLAYER_ID,
                sentPayload.playerId()
        );

        assertEquals(
                "HarriOcho",
                sentPayload.playerName()
        );

        assertEquals(
                AUTHENTICATED_AT,
                sentPayload.authenticatedAt()
        );

        PlayerAuthenticatedAckPayload acknowledgement =
                new PlayerAuthenticatedAckPayload(
                        PLAYER_ID,
                        true,
                        "Player session registered"
                );

        service.handleAcknowledgement(
                player,
                acknowledgementEnvelope(
                        request.optionalRequestId()
                                .orElseThrow(),
                        acknowledgement
                )
        );

        assertEquals(
                acknowledgement,
                request.optionalCompletion()
                        .orElseThrow()
                        .join()
        );

        assertEquals(0, service.pendingCount());
        verify(timeoutTask).cancel();
    }

    @Test
    void rejectsSecondAuthenticationForSamePlayer() {
        PlayerAuthenticationRequest first =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        PlayerAuthenticationRequest second =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT + 1L
                );

        assertEquals(
                PlayerAuthenticationRequestStatus.SUBMITTED,
                first.status()
        );

        assertEquals(
                PlayerAuthenticationRequestStatus
                        .ALREADY_PENDING,
                second.status()
        );

        assertEquals(1, service.pendingCount());
    }

    @Test
    void rejectsAcknowledgementCarriedByDifferentPlayer() {
        PlayerAuthenticationRequest request =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        Player differentCarrier =
                mock(Player.class);

        when(differentCarrier.getUniqueId())
                .thenReturn(UUID.randomUUID());

        service.handleAcknowledgement(
                differentCarrier,
                acknowledgementEnvelope(
                        request.optionalRequestId()
                                .orElseThrow(),
                        new PlayerAuthenticatedAckPayload(
                                PLAYER_ID,
                                true,
                                "Player session registered"
                        )
                )
        );

        assertFalse(
                request.optionalCompletion()
                        .orElseThrow()
                        .isDone()
        );

        assertEquals(1, service.pendingCount());
    }

    @Test
    void ignoresUnknownRequestId() {
        PlayerAuthenticationRequest request =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        service.handleAcknowledgement(
                player,
                acknowledgementEnvelope(
                        UUID.randomUUID(),
                        new PlayerAuthenticatedAckPayload(
                                PLAYER_ID,
                                true,
                                "Player session registered"
                        )
                )
        );

        assertFalse(
                request.optionalCompletion()
                        .orElseThrow()
                        .isDone()
        );

        assertEquals(1, service.pendingCount());
    }

    @Test
    void expiresPendingAuthentication() {
        ArgumentCaptor<Runnable> timeoutCaptor =
                ArgumentCaptor.forClass(Runnable.class);

        PlayerAuthenticationRequest request =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        verify(scheduler).runTaskLater(
                eq(plugin),
                timeoutCaptor.capture(),
                eq(240L)
        );

        timeoutCaptor.getValue().run();

        PlayerAuthenticatedAckPayload result =
                request.optionalCompletion()
                        .orElseThrow()
                        .join();

        assertFalse(result.accepted());

        assertEquals(
                "Authentication acknowledgement timed out",
                result.message()
        );

        assertEquals(0, service.pendingCount());
    }

    @Test
    void removesPendingStateWhenTransportFails() {
        when(messageSender.send(
                eq(player),
                any()
        )).thenReturn(false);

        PlayerAuthenticationRequest request =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        assertEquals(
                PlayerAuthenticationRequestStatus
                        .TRANSPORT_UNAVAILABLE,
                request.status()
        );

        assertEquals(0, service.pendingCount());
        verify(timeoutTask).cancel();
    }

    @Test
    void completesPendingAuthenticationOnDisconnect() {
        PlayerAuthenticationRequest request =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        service.handleDisconnect(PLAYER_ID);

        PlayerAuthenticatedAckPayload result =
                request.optionalCompletion()
                        .orElseThrow()
                        .join();

        assertFalse(result.accepted());

        assertEquals(
                "Player disconnected before "
                        + "authentication acknowledgement",
                result.message()
        );

        assertEquals(0, service.pendingCount());
        verify(timeoutTask).cancel();
    }

    @Test
    void closesAndCompletesPendingAuthentications() {
        PlayerAuthenticationRequest request =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        service.close();

        PlayerAuthenticatedAckPayload result =
                request.optionalCompletion()
                        .orElseThrow()
                        .join();

        assertFalse(result.accepted());

        assertEquals(
                "Authentication service closed",
                result.message()
        );

        assertEquals(0, service.pendingCount());
        verify(timeoutTask).cancel();

        assertEquals(
                PlayerAuthenticationRequestStatus
                        .SERVICE_CLOSED,
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                ).status()
        );
    }

    @Test
    void rejectsRequestsOutsidePrimaryThread() {
        when(plugin.getServer().isPrimaryThread())
                .thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                )
        );
    }

    @Test
    void rejectsOfflinePlayer() {
        when(player.isOnline()).thenReturn(false);

        PlayerAuthenticationRequest request =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        assertEquals(
                PlayerAuthenticationRequestStatus
                        .TRANSPORT_UNAVAILABLE,
                request.status()
        );

        assertEquals(0, service.pendingCount());
    }

    @Test
    void rejectsUnavailableHandshake() {
        when(handshakeService.isAuthorized())
                .thenReturn(false);

        PlayerAuthenticationRequest request =
                service.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        assertEquals(
                PlayerAuthenticationRequestStatus
                        .TRANSPORT_UNAVAILABLE,
                request.status()
        );

        assertEquals(0, service.pendingCount());
    }

    @Test
    void rejectsPublicationFromPlayableBackend() {
        PlayerAuthenticationService playableService =
                createService(
                        new BackendNetworkConfig(
                                true,
                                "lobby-1",
                                BackendType.LOBBY
                        )
                );

        PlayerAuthenticationRequest request =
                playableService.requestAuthentication(
                        player,
                        AUTHENTICATED_AT
                );

        assertEquals(
                PlayerAuthenticationRequestStatus
                        .NOT_AUTHENTICATION_BACKEND,
                request.status()
        );

        assertEquals(0, playableService.pendingCount());
    }

    @Test
    void rejectsInvalidAuthenticationTimestamp() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestAuthentication(
                        player,
                        0L
                )
        );
    }

    private PlayerAuthenticationService createService(
            BackendNetworkConfig config
    ) {
        return new PlayerAuthenticationService(
                plugin,
                config,
                handshakeService,
                messageSender,
                registry
        );
    }

    private ProtocolEnvelope<PlayerAuthenticatedAckPayload>
    acknowledgementEnvelope(
            UUID requestId,
            PlayerAuthenticatedAckPayload payload
    ) {
        return new ProtocolEnvelope<>(
                ProtocolVersion.CURRENT,
                ProtocolMessageType
                        .PLAYER_AUTHENTICATED_ACK,
                requestId,
                AUTHENTICATED_AT + 1L,
                payload
        );
    }
}
