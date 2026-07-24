package com.theosfera.core.network;

import com.theosfera.protocol.ProtocolVersion;
import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.PingPayload;
import com.theosfera.protocol.message.payload.PongPayload;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackendHeartbeatServiceTest {

    private BackendHandshakeService handshakeService;
    private ProtocolMessageSender messageSender;
    private Player carrier;

    @BeforeEach
    void setUp() {
        handshakeService =
                mock(BackendHandshakeService.class);

        messageSender =
                mock(ProtocolMessageSender.class);

        carrier = mock(Player.class);
    }

    @Test
    void respondsToPingWhenBackendIsAuthorized() {
        long pingSentAt = 1_000L;
        long respondedAt = 1_500L;
        UUID requestId = UUID.randomUUID();

        ProtocolEnvelope<PingPayload> ping =
                new ProtocolEnvelope<>(
                        ProtocolVersion.CURRENT,
                        ProtocolMessageType.PING,
                        requestId,
                        pingSentAt,
                        new PingPayload(pingSentAt)
                );

        when(handshakeService.isAuthorized())
                .thenReturn(true);

        BackendHeartbeatService service =
                new BackendHeartbeatService(
                        handshakeService,
                        messageSender,
                        () -> respondedAt
                );

        service.handlePing(carrier, ping);

        ArgumentCaptor<ProtocolEnvelope<?>> captor =
                ArgumentCaptor.forClass(
                        ProtocolEnvelope.class
                );

        verify(messageSender).send(
                eq(carrier),
                captor.capture()
        );

        ProtocolEnvelope<?> response =
                captor.getValue();

        assertEquals(
                ProtocolMessageType.PONG,
                response.type()
        );

        assertEquals(
                requestId,
                response.requestId()
        );

        assertEquals(
                respondedAt,
                response.timestamp()
        );

        assertEquals(
                new PongPayload(
                        pingSentAt,
                        respondedAt
                ),
                response.payload()
        );
    }

    @Test
    void ignoresPingBeforeBackendIsAuthorized() {
        ProtocolEnvelope<PingPayload> ping =
                ProtocolEnvelope.create(
                        ProtocolMessageType.PING,
                        new PingPayload(1_000L)
                );

        when(handshakeService.isAuthorized())
                .thenReturn(false);

        BackendHeartbeatService service =
                new BackendHeartbeatService(
                        handshakeService,
                        messageSender,
                        () -> 1_500L
                );

        service.handlePing(carrier, ping);

        verify(
                messageSender,
                never()
        ).send(any(), any());
    }

    @Test
    void handlesProxyClockAheadOfBackend() {
        long pingSentAt = 2_000L;
        UUID requestId = UUID.randomUUID();

        ProtocolEnvelope<PingPayload> ping =
                new ProtocolEnvelope<>(
                        ProtocolVersion.CURRENT,
                        ProtocolMessageType.PING,
                        requestId,
                        pingSentAt,
                        new PingPayload(pingSentAt)
                );

        when(handshakeService.isAuthorized())
                .thenReturn(true);

        BackendHeartbeatService service =
                new BackendHeartbeatService(
                        handshakeService,
                        messageSender,
                        () -> 1_500L
                );

        service.handlePing(carrier, ping);

        ArgumentCaptor<ProtocolEnvelope<?>> captor =
                ArgumentCaptor.forClass(
                        ProtocolEnvelope.class
                );

        verify(messageSender).send(
                eq(carrier),
                captor.capture()
        );

        ProtocolEnvelope<?> response =
                captor.getValue();

        assertEquals(
                2_000L,
                response.timestamp()
        );

        assertEquals(
                new PongPayload(
                        2_000L,
                        2_000L
                ),
                response.payload()
        );
    }
}
