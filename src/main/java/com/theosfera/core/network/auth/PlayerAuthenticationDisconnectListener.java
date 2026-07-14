package com.theosfera.core.network.auth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public final class PlayerAuthenticationDisconnectListener
        implements Listener {

    private final PlayerAuthenticationService
            authenticationService;

    public PlayerAuthenticationDisconnectListener(
            PlayerAuthenticationService authenticationService
    ) {
        this.authenticationService =
                Objects.requireNonNull(
                        authenticationService,
                        "authenticationService cannot be null"
                );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        authenticationService.handleDisconnect(
                event.getPlayer().getUniqueId()
        );
    }
}
