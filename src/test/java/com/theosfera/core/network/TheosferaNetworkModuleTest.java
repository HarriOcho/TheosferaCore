package com.theosfera.core.network;

import com.theosfera.core.network.auth.PlayerAuthenticationPublisher;
import com.theosfera.protocol.message.payload.BackendType;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TheosferaNetworkModuleTest {

    private JavaPlugin plugin;
    private PluginManager pluginManager;
    private ServicesManager servicesManager;
    private Messenger messenger;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        pluginManager = mock(PluginManager.class);
        servicesManager = mock(ServicesManager.class);
        messenger = mock(Messenger.class);

        Server server = mock(Server.class);

        when(plugin.getServer()).thenReturn(server);

        when(plugin.getLogger())
                .thenReturn(mock(Logger.class));

        when(server.getPluginManager())
                .thenReturn(pluginManager);

        when(server.getServicesManager())
                .thenReturn(servicesManager);

        when(server.getMessenger())
                .thenReturn(messenger);

        when(server.getOnlinePlayers())
                .thenReturn(List.of());
    }

    @Test
    void registersAndUnregistersAuthenticationPublisherOnAuth() {
        TheosferaNetworkModule module =
                new TheosferaNetworkModule(
                        plugin,
                        new BackendNetworkConfig(
                                true,
                                "auth-1",
                                BackendType.AUTH
                        )
                );

        module.initialize();

        verify(servicesManager).register(
                eq(PlayerAuthenticationPublisher.class),
                same(module),
                eq(plugin),
                eq(ServicePriority.Normal)
        );

        module.close();

        verify(servicesManager).unregister(
                eq(PlayerAuthenticationPublisher.class),
                same(module)
        );
    }

    @Test
    void doesNotRegisterAuthenticationPublisherOnPlayableBackend() {
        TheosferaNetworkModule module =
                new TheosferaNetworkModule(
                        plugin,
                        new BackendNetworkConfig(
                                true,
                                "lobby-1",
                                BackendType.LOBBY
                        )
                );

        module.initialize();

        verify(
                servicesManager,
                never()
        ).register(
                eq(PlayerAuthenticationPublisher.class),
                any(PlayerAuthenticationPublisher.class),
                eq(plugin),
                eq(ServicePriority.Normal)
        );

        module.close();

        verify(
                servicesManager,
                never()
        ).unregister(
                eq(PlayerAuthenticationPublisher.class),
                any(PlayerAuthenticationPublisher.class)
        );
    }
}
