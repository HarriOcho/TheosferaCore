package com.theosfera.core.variable.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlaceholderApiExternalPlaceholderService
        implements ExternalPlaceholderService {

    private final Logger logger;
    private boolean failureLogged;

    public PlaceholderApiExternalPlaceholderService(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public String apply(final Player player, final String text) {
        try {
            return PlaceholderAPI.setPlaceholders(player, text);
        } catch (final RuntimeException | LinkageError exception) {
            if (!failureLogged) {
                logger.log(
                        Level.SEVERE,
                        "PlaceholderAPI falló al resolver placeholders. "
                                + "Se conservará el texto original.",
                        exception
                );

                failureLogged = true;
            }

            return text;
        }
    }
}