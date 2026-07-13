package com.theosfera.core.network;

import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.TransferResultPayload;
import com.theosfera.protocol.message.payload.TransferResultStatus;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ProtocolMessageDispatcherTest {

    private static final UUID PLAYER_ID =
            UUID.fromString(
                    "417e98b4-74a1-467e-b453-a15be3af8996"
            );

    @Test
    void dispatchesRegisteredPayload() {
        ProtocolMessageDispatcher dispatcher =
                new ProtocolMessageDispatcher();

        AtomicReference<TransferResultPayload> received =
                new AtomicReference<>();

        dispatcher.register(
                ProtocolMessageType.TRANSFER_RESULT,
                TransferResultPayload.class,
                (carrier, envelope) ->
                        received.set(envelope.payload())
        );

        TransferResultPayload payload =
                new TransferResultPayload(
                        PLAYER_ID,
                        TransferResultStatus.SUCCESS,
                        "Transfer completed"
                );

        boolean dispatched = dispatcher.dispatch(
                mock(Player.class),
                ProtocolEnvelope.create(
                        ProtocolMessageType.TRANSFER_RESULT,
                        payload
                )
        );

        assertTrue(dispatched);
        assertEquals(payload, received.get());
    }

    @Test
    void reportsUnregisteredMessageType() {
        ProtocolMessageDispatcher dispatcher =
                new ProtocolMessageDispatcher();

        TransferResultPayload payload =
                new TransferResultPayload(
                        PLAYER_ID,
                        TransferResultStatus.SUCCESS,
                        "Transfer completed"
                );

        assertFalse(
                dispatcher.dispatch(
                        mock(Player.class),
                        ProtocolEnvelope.create(
                                ProtocolMessageType.TRANSFER_RESULT,
                                payload
                        )
                )
        );
    }

    @Test
    void rejectsDuplicateHandlerRegistration() {
        ProtocolMessageDispatcher dispatcher =
                new ProtocolMessageDispatcher();

        dispatcher.register(
                ProtocolMessageType.TRANSFER_RESULT,
                TransferResultPayload.class,
                (carrier, envelope) -> {
                }
        );

        assertThrows(
                IllegalStateException.class,
                () -> dispatcher.register(
                        ProtocolMessageType.TRANSFER_RESULT,
                        TransferResultPayload.class,
                        (carrier, envelope) -> {
                        }
                )
        );
    }

    @Test
    void rejectsPayloadThatDoesNotMatchHandlerType() {
        ProtocolMessageDispatcher dispatcher =
                new ProtocolMessageDispatcher();

        dispatcher.register(
                ProtocolMessageType.TRANSFER_RESULT,
                String.class,
                (carrier, envelope) -> {
                }
        );

        TransferResultPayload payload =
                new TransferResultPayload(
                        PLAYER_ID,
                        TransferResultStatus.SUCCESS,
                        "Transfer completed"
                );

        assertFalse(
                dispatcher.dispatch(
                        mock(Player.class),
                        ProtocolEnvelope.create(
                                ProtocolMessageType.TRANSFER_RESULT,
                                payload
                        )
                )
        );
    }

    @Test
    void rejectsNullArguments() {
        ProtocolMessageDispatcher dispatcher =
                new ProtocolMessageDispatcher();

        assertThrows(
                NullPointerException.class,
                () -> dispatcher.register(
                        null,
                        TransferResultPayload.class,
                        (carrier, envelope) -> {
                        }
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> dispatcher.dispatch(
                        null,
                        ProtocolEnvelope.create(
                                ProtocolMessageType.TRANSFER_RESULT,
                                new TransferResultPayload(
                                        PLAYER_ID,
                                        TransferResultStatus.FAILED,
                                        "Transfer failed"
                                )
                        )
                )
        );
    }
}
