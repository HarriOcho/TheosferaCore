package com.theosfera.core.network;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class BackendConnectionListener
        implements Listener {

    private final JavaPlugin plugin;
    private final BackendHandshakeService handshakeService;
    private final PlayerPresenceService presenceService;

    public BackendConnectionListener(
            JavaPlugin plugin,
            BackendHandshakeService handshakeService,
            PlayerPresenceService presenceService
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );
        this.handshakeService = Objects.requireNonNull(
                handshakeService,
                "handshakeService cannot be null"
        );
        this.presenceService = Objects.requireNonNull(
                presenceService,
                "presenceService cannot be null"
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        scheduleReadyPlayer(
                event.getPlayer()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRegisterChannel(
            PlayerRegisterChannelEvent event
    ) {
        if (!ProtocolChannel.NAME.equals(
                event.getChannel()
        )) {
            return;
        }

        scheduleReadyPlayer(
                event.getPlayer()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        handshakeService.handleCarrierDisconnect(
                event.getPlayer().getUniqueId()
        );
    }

    private void scheduleReadyPlayer(Player player) {
        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> handleReadyPlayer(player)
        );
    }

    private void handleReadyPlayer(Player player) {
        if (!player.isOnline()) {
            return;
        }

        if (handshakeService.isAuthorized()) {
            presenceService.announceReady(player);
            return;
        }

        handshakeService.begin(player);
    }
}
