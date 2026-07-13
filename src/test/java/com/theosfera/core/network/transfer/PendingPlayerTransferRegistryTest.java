package com.theosfera.core.network.transfer;

import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.TransferResultPayload;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PendingPlayerTransferRegistryTest {

    private static final UUID REQUEST_ID =
            UUID.fromString(
                    "11111111-2222-3333-4444-555555555555"
            );

    private static final UUID PLAYER_ID =
            UUID.fromString(
                    "417e98b4-74a1-467e-b453-a15be3af8996"
            );

    private final PendingPlayerTransferRegistry registry =
            new PendingPlayerTransferRegistry();

    @Test
    void registersTransferInBothIndexes() {
        PendingPlayerTransfer transfer =
                createTransfer(
                        REQUEST_ID,
                        PLAYER_ID
                );

        assertEquals(
                PlayerTransferRegistrationResult.REGISTERED,
                registry.register(transfer)
        );

        assertSame(
                transfer,
                registry.findByRequest(
                        REQUEST_ID
                ).orElseThrow()
        );

        assertSame(
                transfer,
                registry.findByPlayer(
                        PLAYER_ID
                ).orElseThrow()
        );

        assertEquals(1, registry.size());
    }

    @Test
    void rejectsSecondTransferForSamePlayer() {
        assertEquals(
                PlayerTransferRegistrationResult.REGISTERED,
                registry.register(
                        createTransfer(
                                REQUEST_ID,
                                PLAYER_ID
                        )
                )
        );

        assertEquals(
                PlayerTransferRegistrationResult
                        .PLAYER_ALREADY_PENDING,
                registry.register(
                        createTransfer(
                                UUID.randomUUID(),
                                PLAYER_ID
                        )
                )
        );

        assertEquals(1, registry.size());
    }

    @Test
    void rejectsDuplicateRequestId() {
        assertEquals(
                PlayerTransferRegistrationResult.REGISTERED,
                registry.register(
                        createTransfer(
                                REQUEST_ID,
                                PLAYER_ID
                        )
                )
        );

        assertEquals(
                PlayerTransferRegistrationResult
                        .REQUEST_ID_CONFLICT,
                registry.register(
                        createTransfer(
                                REQUEST_ID,
                                UUID.randomUUID()
                        )
                )
        );

        assertEquals(1, registry.size());
    }

    @Test
    void removesTransferFromBothIndexesByRequest() {
        PendingPlayerTransfer transfer =
                createTransfer(
                        REQUEST_ID,
                        PLAYER_ID
                );

        registry.register(transfer);

        assertSame(
                transfer,
                registry.remove(
                        REQUEST_ID
                ).orElseThrow()
        );

        assertTrue(
                registry.findByRequest(
                        REQUEST_ID
                ).isEmpty()
        );

        assertTrue(
                registry.findByPlayer(
                        PLAYER_ID
                ).isEmpty()
        );
    }

    @Test
    void removesTransferFromBothIndexesByPlayer() {
        PendingPlayerTransfer transfer =
                createTransfer(
                        REQUEST_ID,
                        PLAYER_ID
                );

        registry.register(transfer);

        assertSame(
                transfer,
                registry.removeByPlayer(
                        PLAYER_ID
                ).orElseThrow()
        );

        assertTrue(
                registry.findByRequest(
                        REQUEST_ID
                ).isEmpty()
        );

        assertTrue(
                registry.findByPlayer(
                        PLAYER_ID
                ).isEmpty()
        );
    }

    @Test
    void drainsAllTransfersAndIndexes() {
        PendingPlayerTransfer first =
                createTransfer(
                        REQUEST_ID,
                        PLAYER_ID
                );

        PendingPlayerTransfer second =
                createTransfer(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        registry.register(first);
        registry.register(second);

        assertEquals(2, registry.drain().size());
        assertEquals(0, registry.size());

        assertTrue(
                registry.findByRequest(
                        first.requestId()
                ).isEmpty()
        );

        assertTrue(
                registry.findByPlayer(
                        second.playerId()
                ).isEmpty()
        );
    }

    @Test
    void rejectsNullInputs() {
        assertThrows(
                NullPointerException.class,
                () -> registry.register(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> registry.findByRequest(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> registry.findByPlayer(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> registry.remove(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> registry.removeByPlayer(null)
        );
    }

    private PendingPlayerTransfer createTransfer(
            UUID requestId,
            UUID playerId
    ) {
        return new PendingPlayerTransfer(
                requestId,
                playerId,
                BackendType.SKYBLOCK,
                1_750_000_000_000L,
                new CompletableFuture<
                        TransferResultPayload
                        >()
        );
    }
}
