package com.theosfera.core.network;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackendConnectionListenerTest {

    private JavaPlugin plugin;
    private BackendHandshakeService handshakeService;
    private PlayerPresenceService presenceService;
    private Player player;
    private BackendConnectionListener listener;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        handshakeService =
                mock(BackendHandshakeService.class);
        presenceService =
                mock(PlayerPresenceService.class);
        player = mock(Player.class);

        Server server = mock(Server.class);
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
}
