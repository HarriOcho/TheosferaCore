package com.theosfera.core.network;

import com.theosfera.protocol.codec.ProtocolJsonCodec;
import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.TransferResultPayload;
import com.theosfera.protocol.message.payload.TransferResultStatus;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProtocolMessageListenerTest {

    private static final UUID PLAYER_ID =
            UUID.fromString(
                    "417e98b4-74a1-467e-b453-a15be3af8996"
            );

    private JavaPlugin plugin;
    private ProtocolJsonCodec codec;
    private ProtocolMessageDispatcher dispatcher;
    private ProtocolMessageListener listener;
    private Player carrier;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        codec = new ProtocolJsonCodec();
        dispatcher = mock(ProtocolMessageDispatcher.class);
        carrier = mock(Player.class);

        when(plugin.getLogger())
                .thenReturn(mock(Logger.class));

        when(carrier.isOnline()).thenReturn(true);
        when(carrier.getUniqueId()).thenReturn(PLAYER_ID);

        listener = new ProtocolMessageListener(
                plugin,
                codec,
                dispatcher
        );
    }

    @Test
    void decodesAndDispatchesRegisteredMessage() {
        TransferResultPayload payload =
                new TransferResultPayload(
                        PLAYER_ID,
                        TransferResultStatus.SUCCESS,
                        "Transfer completed"
                );

        ProtocolEnvelope<TransferResultPayload> envelope =
                ProtocolEnvelope.create(
                        ProtocolMessageType.TRANSFER_RESULT,
                        payload
                );

        byte[] encoded = codec.encode(envelope);

        when(dispatcher.dispatch(
                eq(carrier),
                any()
        )).thenReturn(true);

        listener.onPluginMessageReceived(
                ProtocolChannel.NAME,
                carrier,
                encoded
        );

        ArgumentCaptor<ProtocolEnvelope<?>> captor =
                ArgumentCaptor.forClass(
                        ProtocolEnvelope.class
                );

        verify(dispatcher).dispatch(
                eq(carrier),
                captor.capture()
        );

        ProtocolEnvelope<?> received =
                captor.getValue();

        assertEquals(envelope, received);
        assertEquals(
                TransferResultPayload.class,
                received.payload().getClass()
        );
    }

    @Test
    void ignoresDifferentChannel() {
        listener.onPluginMessageReceived(
                "example:other",
                carrier,
                new byte[]{1}
        );

        verify(
                dispatcher,
                never()
        ).dispatch(any(), any());
    }

    @Test
    void ignoresOfflineCarrier() {
        when(carrier.isOnline()).thenReturn(false);

        listener.onPluginMessageReceived(
                ProtocolChannel.NAME,
                carrier,
                new byte[]{1}
        );

        verify(
                dispatcher,
                never()
        ).dispatch(any(), any());
    }

    @Test
    void rejectsEmptyMessage() {
        listener.onPluginMessageReceived(
                ProtocolChannel.NAME,
                carrier,
                new byte[0]
        );

        verify(
                dispatcher,
                never()
        ).dispatch(any(), any());
    }

    @Test
    void rejectsMalformedMessage() {
        listener.onPluginMessageReceived(
                ProtocolChannel.NAME,
                carrier,
                "{invalid-json".getBytes(
                        StandardCharsets.UTF_8
                )
        );

        verify(
                dispatcher,
                never()
        ).dispatch(any(), any());
    }
}
