package com.theosfera.core.network;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackendConnectionListenerTest {

    private JavaPlugin plugin;
    private BackendHandshakeService handshakeService;
    private PlayerPresenceService presenceService;
    private Player player;
    private Server server;
    private BackendConnectionListener listener;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        handshakeService =
                mock(BackendHandshakeService.class);
        presenceService =
                mock(PlayerPresenceService.class);
        player = mock(Player.class);

        server = mock(Server.class);
        BukkitScheduler scheduler =
                mock(BukkitScheduler.class);

        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);

        when(
                scheduler.runTask(
                        any(JavaPlugin.class),
                        any(Runnable.class)
                )
        ).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(
                    1,
                    Runnable.class
            );
            task.run();
            return mock(BukkitTask.class);
        });

        listener = new BackendConnectionListener(
                plugin,
                handshakeService,
                presenceService
        );
    }

    @Test
    void beginsHandshakeWhenProtocolChannelRegisters() {
        PlayerRegisterChannelEvent event =
                mock(PlayerRegisterChannelEvent.class);

        when(event.getChannel())
                .thenReturn(ProtocolChannel.NAME);
        when(event.getPlayer()).thenReturn(player);
        when(player.isOnline()).thenReturn(true);
        when(handshakeService.isAuthorized())
                .thenReturn(false);

        listener.onPlayerRegisterChannel(event);

        verify(handshakeService).begin(player);
        verify(presenceService, never())
                .announceReady(any());
    }

    @Test
    void ignoresUnrelatedRegisteredChannel() {
        PlayerRegisterChannelEvent event =
                mock(PlayerRegisterChannelEvent.class);

        when(event.getChannel())
                .thenReturn("example:unrelated");

        listener.onPlayerRegisterChannel(event);

        verify(handshakeService, never())
                .begin(any());
        verify(presenceService, never())
                .announceReady(any());
    }

    @Test
    void announcesPresenceWhenAlreadyAuthorized() {
        PlayerRegisterChannelEvent event =
                mock(PlayerRegisterChannelEvent.class);

        when(event.getChannel())
                .thenReturn(ProtocolChannel.NAME);
        when(event.getPlayer()).thenReturn(player);
        when(player.isOnline()).thenReturn(true);
        when(handshakeService.isAuthorized())
                .thenReturn(true);

        listener.onPlayerRegisterChannel(event);

        verify(presenceService).announceReady(player);
        verify(handshakeService, never())
                .begin(any());
    }

    @Test
    void notifiesBackendEmptyWhenLastQuittingPlayerIsStillOnline() {
        UUID playerId =
                UUID.fromString(
                        "2f3262d1-8497-4078-9c3a-85d8f7c2ab54"
                );
        PlayerQuitEvent event =
                mock(PlayerQuitEvent.class);

        when(event.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        doReturn(List.of(player))
                .when(server)
                .getOnlinePlayers();

        listener.onPlayerQuit(event);

        verify(handshakeService).handleBackendEmpty();
        verify(handshakeService, never())
                .handleCarrierDisconnect(any());
    }

    @Test
    void notifiesBackendEmptyWhenLastQuittingPlayerWasAlreadyRemoved() {
        UUID playerId =
                UUID.fromString(
                        "2f3262d1-8497-4078-9c3a-85d8f7c2ab54"
                );
        PlayerQuitEvent event =
                mock(PlayerQuitEvent.class);

        when(event.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        doReturn(List.of())
                .when(server)
                .getOnlinePlayers();

        listener.onPlayerQuit(event);

        verify(handshakeService).handleBackendEmpty();
        verify(handshakeService, never())
                .handleCarrierDisconnect(any());
    }

    @Test
    void doesNotNotifyBackendEmptyWhenAnotherPlayerRemains() {
        UUID leavingPlayerId =
                UUID.fromString(
                        "2f3262d1-8497-4078-9c3a-85d8f7c2ab54"
                );
        Player remainingPlayer =
                mock(Player.class);
        PlayerQuitEvent event =
                mock(PlayerQuitEvent.class);

        when(event.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(leavingPlayerId);
        when(remainingPlayer.getUniqueId())
                .thenReturn(UUID.randomUUID());
        doReturn(List.of(player, remainingPlayer))
                .when(server)
                .getOnlinePlayers();

        listener.onPlayerQuit(event);

        verify(handshakeService, never())
                .handleBackendEmpty();
    }

    @Test
    void preservesCarrierDisconnectWhenAnotherPlayerRemains() {
        UUID leavingPlayerId =
                UUID.fromString(
                        "2f3262d1-8497-4078-9c3a-85d8f7c2ab54"
                );
        Player remainingPlayer =
                mock(Player.class);
        PlayerQuitEvent event =
                mock(PlayerQuitEvent.class);

        when(event.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(leavingPlayerId);
        when(remainingPlayer.getUniqueId())
                .thenReturn(UUID.randomUUID());
        doReturn(List.of(remainingPlayer))
                .when(server)
                .getOnlinePlayers();

        listener.onPlayerQuit(event);

        verify(handshakeService).handleCarrierDisconnect(
                leavingPlayerId
        );
        verify(handshakeService, never())
                .handleBackendEmpty();
    }
}
