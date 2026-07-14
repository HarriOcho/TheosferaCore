package com.theosfera.core.network.auth;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerAuthenticationDisconnectListenerTest {

    private static final UUID PLAYER_ID =
            UUID.fromString(
                    "417e98b4-74a1-467e-b453-a15be3af8996"
            );

    @Test
    void removesPendingAuthenticationOnPlayerQuit() {
        PlayerAuthenticationService authenticationService =
                mock(PlayerAuthenticationService.class);

        PlayerAuthenticationDisconnectListener listener =
                new PlayerAuthenticationDisconnectListener(
                        authenticationService
                );

        Player player = mock(Player.class);

        PlayerQuitEvent event =
                mock(PlayerQuitEvent.class);

        when(player.getUniqueId())
                .thenReturn(PLAYER_ID);

        when(event.getPlayer())
                .thenReturn(player);

        listener.onPlayerQuit(event);

        verify(authenticationService)
                .handleDisconnect(PLAYER_ID);
    }

    @Test
    void rejectsNullService() {
        assertThrows(
                NullPointerException.class,
                () -> new PlayerAuthenticationDisconnectListener(
                        null
                )
        );
    }
}
