package com.theosfera.core.network;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProtocolChannelRegistrationTest {

    private JavaPlugin plugin;
    private ProtocolMessageListener listener;
    private Messenger messenger;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        listener = mock(ProtocolMessageListener.class);
        messenger = mock(Messenger.class);

        Server server = mock(Server.class);

        when(plugin.getServer()).thenReturn(server);
        when(server.getMessenger()).thenReturn(messenger);
    }

    @Test
    void registersAndClosesChannelOnce() {
        ProtocolChannelRegistration registration =
                new ProtocolChannelRegistration(
                        plugin,
                        listener
                );

        registration.register();
        registration.register();

        assertTrue(registration.isRegistered());

        verify(
                messenger,
                times(1)
        ).registerOutgoingPluginChannel(
                plugin,
                ProtocolChannel.NAME
        );

        verify(
                messenger,
                times(1)
        ).registerIncomingPluginChannel(
                plugin,
                ProtocolChannel.NAME,
                listener
        );

        registration.close();
        registration.close();

        assertFalse(registration.isRegistered());

        verify(
                messenger,
                times(1)
        ).unregisterIncomingPluginChannel(
                plugin,
                ProtocolChannel.NAME,
                listener
        );

        verify(
                messenger,
                times(1)
        ).unregisterOutgoingPluginChannel(
                plugin,
                ProtocolChannel.NAME
        );
    }

    @Test
    void rollsBackOutgoingChannelWhenIncomingFails() {
        ProtocolChannelRegistration registration =
                new ProtocolChannelRegistration(
                        plugin,
                        listener
                );

        doThrow(new IllegalStateException("registration failed"))
                .when(messenger)
                .registerIncomingPluginChannel(
                        plugin,
                        ProtocolChannel.NAME,
                        listener
                );

        assertThrows(
                IllegalStateException.class,
                registration::register
        );

        assertFalse(registration.isRegistered());

        verify(messenger).unregisterOutgoingPluginChannel(
                plugin,
                ProtocolChannel.NAME
        );
    }

    @Test
    void rejectsNullDependencies() {
        assertThrows(
                NullPointerException.class,
                () -> new ProtocolChannelRegistration(
                        null,
                        listener
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> new ProtocolChannelRegistration(
                        plugin,
                        null
                )
        );
    }
}
