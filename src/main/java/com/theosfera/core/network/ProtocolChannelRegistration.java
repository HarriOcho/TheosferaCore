package com.theosfera.core.network;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import java.util.Objects;

public final class ProtocolChannelRegistration
        implements AutoCloseable {

    private final JavaPlugin plugin;
    private final ProtocolMessageListener listener;

    private boolean registered;

    public ProtocolChannelRegistration(
            JavaPlugin plugin,
            ProtocolMessageListener listener
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );
        this.listener = Objects.requireNonNull(
                listener,
                "listener cannot be null"
        );
    }

    public void register() {
        if (registered) {
            return;
        }

        Messenger messenger =
                plugin.getServer().getMessenger();

        messenger.registerOutgoingPluginChannel(
                plugin,
                ProtocolChannel.NAME
        );

        try {
            messenger.registerIncomingPluginChannel(
                    plugin,
                    ProtocolChannel.NAME,
                    listener
            );
            registered = true;
        } catch (RuntimeException exception) {
            messenger.unregisterOutgoingPluginChannel(
                    plugin,
                    ProtocolChannel.NAME
            );
            throw exception;
        }
    }

    public boolean isRegistered() {
        return registered;
    }

    @Override
    public void close() {
        if (!registered) {
            return;
        }

        Messenger messenger =
                plugin.getServer().getMessenger();

        messenger.unregisterIncomingPluginChannel(
                plugin,
                ProtocolChannel.NAME,
                listener
        );

        messenger.unregisterOutgoingPluginChannel(
                plugin,
                ProtocolChannel.NAME
        );

        registered = false;
    }
}
