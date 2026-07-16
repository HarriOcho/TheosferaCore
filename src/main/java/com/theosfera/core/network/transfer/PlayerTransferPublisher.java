package com.theosfera.core.network.transfer;

import com.theosfera.protocol.message.payload.BackendType;
import org.bukkit.entity.Player;

public interface PlayerTransferPublisher {

    PlayerTransferRequest requestTransfer(
            Player player,
            BackendType targetBackendType
    );
}
