package com.theosfera.core.network;

import com.theosfera.protocol.codec.ProtocolJsonCodec;
import com.theosfera.protocol.message.ProtocolEnvelope;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Objects;
import java.util.logging.Level;

public final class ProtocolMessageListener
        implements PluginMessageListener {

    private final JavaPlugin plugin;
    private final ProtocolJsonCodec codec;
    private final ProtocolMessageDispatcher dispatcher;

    public ProtocolMessageListener(
            JavaPlugin plugin,
            ProtocolJsonCodec codec,
            ProtocolMessageDispatcher dispatcher
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );
        this.codec = Objects.requireNonNull(
                codec,
                "codec cannot be null"
        );
        this.dispatcher = Objects.requireNonNull(
                dispatcher,
                "dispatcher cannot be null"
        );
    }

    @Override
    public void onPluginMessageReceived(
            String channel,
            Player carrier,
            byte[] message
    ) {
        if (!ProtocolChannel.NAME.equals(channel)) {
            return;
        }

        if (carrier == null || !carrier.isOnline()) {
            return;
        }

        if (message == null || message.length == 0) {
            plugin.getLogger().warning(
                    "Se rechazó un mensaje vacío recibido en "
                            + ProtocolChannel.NAME
                            + "."
            );
            return;
        }

        final ProtocolEnvelope<?> envelope;

        try {
            envelope = codec.decodeRegistered(message);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Se rechazó un mensaje de protocolo inválido "
                            + "recibido mediante "
                            + carrier.getUniqueId()
                            + ".",
                    exception
            );
            return;
        }

        try {
            if (!dispatcher.dispatch(carrier, envelope)) {
                plugin.getLogger().warning(
                        "No existe un handler compatible para "
                                + envelope.type()
                                + " (requestId: "
                                + envelope.requestId()
                                + ")."
                );
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Falló el procesamiento del mensaje "
                            + envelope.type()
                            + " (requestId: "
                            + envelope.requestId()
                            + ").",
                    exception
            );
        }
    }
}
