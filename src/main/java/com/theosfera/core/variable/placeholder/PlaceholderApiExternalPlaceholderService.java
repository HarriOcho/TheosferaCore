package com.theosfera.core.variable.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public final class PlaceholderApiExternalPlaceholderService
        implements ExternalPlaceholderService {

    @Override
    public String apply(final Player player, final String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}