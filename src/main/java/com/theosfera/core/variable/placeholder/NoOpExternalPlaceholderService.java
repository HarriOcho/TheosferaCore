package com.theosfera.core.variable.placeholder;

import org.bukkit.entity.Player;

public final class NoOpExternalPlaceholderService
        implements ExternalPlaceholderService {

    @Override
    public String apply(final Player player, final String text) {
        return text;
    }
}