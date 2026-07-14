package com.theosfera.core.network.auth;

import org.bukkit.entity.Player;

public interface PlayerAuthenticationPublisher {

    PlayerAuthenticationRequest publishAuthenticatedPlayer(
            Player player,
            long authenticatedAt
    );
}
