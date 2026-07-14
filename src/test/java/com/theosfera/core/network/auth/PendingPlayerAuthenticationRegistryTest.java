package com.theosfera.core.network.auth;

import com.theosfera.protocol.message.payload.PlayerAuthenticatedAckPayload;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PendingPlayerAuthenticationRegistryTest {

    private static final UUID REQUEST_ID =
            UUID.fromString(
                    "11111111-2222-3333-4444-555555555555"
            );

    private static final UUID PLAYER_ID =
            UUID.fromString(
                    "417e98b4-74a1-467e-b453-a15be3af8996"
            );

    private final PendingPlayerAuthenticationRegistry registry =
            new PendingPlayerAuthenticationRegistry();

    @Test
    void registersAuthenticationInBothIndexes() {
        PendingPlayerAuthentication authentication =
                createAuthentication(
                        REQUEST_ID,
                        PLAYER_ID
                );

        assertEquals(
                PlayerAuthenticationRegistrationResult
                        .REGISTERED,
                registry.register(authentication)
        );

        assertSame(
                authentication,
                registry.findByRequest(
                        REQUEST_ID
                ).orElseThrow()
        );

        assertSame(
                authentication,
                registry.findByPlayer(
                        PLAYER_ID
                ).orElseThrow()
        );

        assertEquals(1, registry.size());
    }

    @Test
    void rejectsSecondAuthenticationForSamePlayer() {
        assertEquals(
                PlayerAuthenticationRegistrationResult
                        .REGISTERED,
                registry.register(
                        createAuthentication(
                                REQUEST_ID,
                                PLAYER_ID
                        )
                )
        );

        assertEquals(
                PlayerAuthenticationRegistrationResult
                        .PLAYER_ALREADY_PENDING,
                registry.register(
                        createAuthentication(
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
                PlayerAuthenticationRegistrationResult
                        .REGISTERED,
                registry.register(
                        createAuthentication(
                                REQUEST_ID,
                                PLAYER_ID
                        )
                )
        );

        assertEquals(
                PlayerAuthenticationRegistrationResult
                        .REQUEST_ID_CONFLICT,
                registry.register(
                        createAuthentication(
                                REQUEST_ID,
                                UUID.randomUUID()
                        )
                )
        );

        assertEquals(1, registry.size());
    }

    @Test
    void removesAuthenticationFromBothIndexesByRequest() {
        PendingPlayerAuthentication authentication =
                createAuthentication(
                        REQUEST_ID,
                        PLAYER_ID
                );

        registry.register(authentication);

        assertSame(
                authentication,
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
    void removesAuthenticationFromBothIndexesByPlayer() {
        PendingPlayerAuthentication authentication =
                createAuthentication(
                        REQUEST_ID,
                        PLAYER_ID
                );

        registry.register(authentication);

        assertSame(
                authentication,
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
    void drainsAllAuthenticationsAndIndexes() {
        PendingPlayerAuthentication first =
                createAuthentication(
                        REQUEST_ID,
                        PLAYER_ID
                );

        PendingPlayerAuthentication second =
                createAuthentication(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        registry.register(first);
        registry.register(second);

        assertEquals(
                2,
                registry.drain().size()
        );

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

    private PendingPlayerAuthentication createAuthentication(
            UUID requestId,
            UUID playerId
    ) {
        return new PendingPlayerAuthentication(
                requestId,
                playerId,
                1_750_000_000_000L,
                new CompletableFuture<
                        PlayerAuthenticatedAckPayload
                        >()
        );
    }
}
