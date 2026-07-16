package com.theosfera.core.network;

import com.theosfera.protocol.message.payload.BackendType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackendNetworkConfigTest {

    @Test
    void allowsAuthenticationBackendToTransferOnlyToLobby() {
        BackendNetworkConfig config =
                config(
                        "auth-1",
                        BackendType.AUTH
                );

        assertTrue(
                config.allowsTransferTo(
                        BackendType.LOBBY
                )
        );

        assertFalse(
                config.allowsTransferTo(
                        BackendType.SKYBLOCK
                )
        );

        assertFalse(
                config.allowsTransferTo(
                        BackendType.AUTH
                )
        );
    }

    @Test
    void allowsPlayableBackendsToTransferToPlayableTargets() {
        for (BackendType sourceType : new BackendType[]{
                BackendType.LOBBY,
                BackendType.SKYBLOCK
        }) {
            BackendNetworkConfig config =
                    config(
                            sourceType.name()
                                    .toLowerCase()
                                    + "-1",
                            sourceType
                    );

            assertTrue(
                    config.allowsTransferTo(
                            BackendType.LOBBY
                    )
            );

            assertTrue(
                    config.allowsTransferTo(
                            BackendType.SKYBLOCK
                    )
            );

            assertFalse(
                    config.allowsTransferTo(
                            BackendType.AUTH
                    )
            );
        }
    }

    @Test
    void rejectsNullTransferTarget() {
        BackendNetworkConfig config =
                config(
                        "auth-1",
                        BackendType.AUTH
                );

        assertThrows(
                NullPointerException.class,
                () -> config.allowsTransferTo(null)
        );
    }

    private BackendNetworkConfig config(
            String backendName,
            BackendType backendType
    ) {
        return new BackendNetworkConfig(
                true,
                backendName,
                backendType
        );
    }
}
