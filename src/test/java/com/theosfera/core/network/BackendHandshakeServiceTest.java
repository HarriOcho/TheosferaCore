package com.theosfera.core.network;

import com.theosfera.protocol.ProtocolVersion;
import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.BackendHelloAckPayload;
import com.theosfera.protocol.message.payload.BackendType;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackendHandshakeServiceTest {

    private static final UUID CARRIER_ID =
            UUID.fromString(
                    "417e98b4-74a1-467e-b453-a15be3af8996"
            );

    private JavaPlugin plugin;
    private ProtocolMessageSender messageSender;
    private BackendHandshakeService service;
    private Player carrier;
    private BukkitScheduler scheduler;
    private BukkitTask timeoutTask;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        messageSender = mock(ProtocolMessageSender.class);
        carrier = mock(Player.class);
        scheduler = mock(BukkitScheduler.class);
        timeoutTask = mock(BukkitTask.class);

        Server server = mock(Server.class);

        when(plugin.getLogger())
                .thenReturn(mock(Logger.class));

        when(plugin.getServer()).thenReturn(server);
        when(server.isPrimaryThread()).thenReturn(true);
        when(server.getScheduler()).thenReturn(scheduler);

        when(carrier.getUniqueId()).thenReturn(CARRIER_ID);
        when(carrier.isOnline()).thenReturn(true);

        when(messageSender.send(
                eq(carrier),
                any()
        )).thenReturn(true);

        when(scheduler.runTaskLater(
                eq(plugin),
                any(Runnable.class),
                eq(200L)
        )).thenReturn(timeoutTask);

        service = new BackendHandshakeService(
                plugin,
                new BackendNetworkConfig(
                        true,
                        "lobby-1",
                        BackendType.LOBBY
                ),
                messageSender
        );
    }

    @Test
    void authorizesCorrelatedHandshake() {
        ProtocolEnvelope<?> hello =
                beginAndCaptureHello();

        assertEquals(
                BackendHandshakeStatus.HELLO_PENDING,
                service.status()
        );

        boolean authorized = service.handleAck(
                carrier,
                ackEnvelope(
                        hello.requestId(),
                        true
                )
        );

        assertTrue(authorized);
        assertTrue(service.isAuthorized());

        assertEquals(
                BackendHandshakeStatus.AUTHORIZED,
                service.status()
        );

        verify(timeoutTask).cancel();
    }

    @Test
    void rejectsUncorrelatedAck() {
        beginAndCaptureHello();

        assertFalse(
                service.handleAck(
                        carrier,
                        ackEnvelope(
                                UUID.randomUUID(),
                                true
                        )
                )
        );

        assertFalse(service.isAuthorized());

        assertEquals(
                BackendHandshakeStatus.HELLO_PENDING,
                service.status()
        );
    }

    @Test
    void rejectsAckFromDifferentCarrier() {
        ProtocolEnvelope<?> hello =
                beginAndCaptureHello();

        Player differentCarrier =
                mock(Player.class);

        when(differentCarrier.getUniqueId())
                .thenReturn(UUID.randomUUID());

        assertFalse(
                service.handleAck(
                        differentCarrier,
                        ackEnvelope(
                                hello.requestId(),
                                true
                        )
                )
        );

        assertFalse(service.isAuthorized());
    }

    @Test
    void preservesProxyRejection() {
        ProtocolEnvelope<?> hello =
                beginAndCaptureHello();

        assertFalse(
                service.handleAck(
                        carrier,
                        ackEnvelope(
                                hello.requestId(),
                                false
                        )
                )
        );

        assertEquals(
                BackendHandshakeStatus.REJECTED,
                service.status()
        );

        assertFalse(service.isAuthorized());
    }

    @Test
    void clearsPendingHandshakeWhenCarrierDisconnects() {
        beginAndCaptureHello();

        service.handleCarrierDisconnect(CARRIER_ID);

        assertEquals(
                BackendHandshakeStatus.WAITING_FOR_CARRIER,
                service.status()
        );

        verify(timeoutTask).cancel();
    }

    @Test
    void returnsToWaitingStateAfterTimeout() {
        ArgumentCaptor<Runnable> timeoutCaptor =
                ArgumentCaptor.forClass(Runnable.class);

        service.begin(carrier);

        verify(scheduler).runTaskLater(
                eq(plugin),
                timeoutCaptor.capture(),
                eq(200L)
        );

        timeoutCaptor.getValue().run();

        assertEquals(
                BackendHandshakeStatus.WAITING_FOR_CARRIER,
                service.status()
        );

        assertFalse(service.isAuthorized());
    }

    @Test
    void closesIdempotently() {
        beginAndCaptureHello();

        service.close();
        service.close();

        assertEquals(
                BackendHandshakeStatus.CLOSED,
                service.status()
        );

        assertFalse(service.begin(carrier));
        verify(timeoutTask).cancel();
    }

    private ProtocolEnvelope<?> beginAndCaptureHello() {
        ArgumentCaptor<ProtocolEnvelope<?>> captor =
                ArgumentCaptor.forClass(
                        ProtocolEnvelope.class
                );

        assertTrue(service.begin(carrier));

        verify(messageSender).send(
                eq(carrier),
                captor.capture()
        );

        ProtocolEnvelope<?> envelope =
                captor.getValue();

        assertEquals(
                ProtocolMessageType.BACKEND_HELLO,
                envelope.type()
        );

        return envelope;
    }

    private ProtocolEnvelope<BackendHelloAckPayload>
    ackEnvelope(
            UUID requestId,
            boolean accepted
    ) {
        return new ProtocolEnvelope<>(
                ProtocolVersion.CURRENT,
                ProtocolMessageType.BACKEND_HELLO_ACK,
                requestId,
                1_750_000_000_000L,
                new BackendHelloAckPayload(
                        accepted,
                        accepted
                                ? "Backend registered"
                                : "Backend rejected"
                )
        );
    }
}
