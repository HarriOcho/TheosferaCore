package com.theosfera.core.network;

import com.theosfera.protocol.codec.ProtocolJsonCodec;
import com.theosfera.protocol.message.ProtocolEnvelope;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class ProtocolMessageSender {

    private static final int MAX_MESSAGE_SIZE = 32_766;

    private final JavaPlugin plugin;
    private final ProtocolJsonCodec codec;

    public ProtocolMessageSender(
            JavaPlugin plugin,
            ProtocolJsonCodec codec
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );
        this.codec = Objects.requireNonNull(
                codec,
                "codec cannot be null"
        );
    }

    public boolean send(
            Player player,
            ProtocolEnvelope<?> envelope
    ) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );
        Objects.requireNonNull(
                envelope,
                "envelope cannot be null"
        );

        if (!player.isOnline()) {
            return false;
        }

        if (!player.getListeningPluginChannels().contains(
                ProtocolChannel.NAME
        )) {
            return false;
        }

        final byte[] encoded;

        try {
            encoded = codec.encode(envelope);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "No se pudo codificar el mensaje de protocolo "
                            + envelope.type()
                            + " (requestId: "
                            + envelope.requestId()
                            + ").",
                    exception
            );
            return false;
        }

        if (encoded.length > MAX_MESSAGE_SIZE) {
            plugin.getLogger().warning(
                    "Se rechazó un mensaje de protocolo sobredimensionado "
                            + envelope.type()
                            + " (requestId: "
                            + envelope.requestId()
                            + ", bytes: "
                            + encoded.length
                            + ")."
            );
            return false;
        }

        try {
            player.sendPluginMessage(
                    plugin,
                    ProtocolChannel.NAME,
                    encoded
            );
            return true;
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "No se pudo enviar el mensaje de protocolo "
                            + envelope.type()
                            + " (requestId: "
                            + envelope.requestId()
                            + ").",
                    exception
            );
            return false;
        }
    }
}
