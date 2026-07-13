package com.theosfera.core.network.transfer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public final class PlayerTransferDisconnectListener
        implements Listener {

    private final PlayerTransferService transferService;

    public PlayerTransferDisconnectListener(
            PlayerTransferService transferService
    ) {
        this.transferService = Objects.requireNonNull(
                transferService,
                "transferService cannot be null"
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        transferService.handleDisconnect(
                event.getPlayer().getUniqueId()
        );
    }
}
