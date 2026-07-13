package com.theosfera.core.network;

import com.theosfera.protocol.message.ProtocolEnvelope;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface ProtocolMessageHandler<T> {

    void handle(
            Player carrier,
            ProtocolEnvelope<T> envelope
    );
}
