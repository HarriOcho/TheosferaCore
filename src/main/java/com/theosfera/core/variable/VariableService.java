package com.theosfera.core.variable;

import com.theosfera.core.keybind.KeybindEntry;
import com.theosfera.core.variable.placeholder.ExternalPlaceholderService;
import org.bukkit.entity.Player;

import java.util.List;

public final class VariableService {

    private static final List<String> PLAYER_VARIABLES = List.of(
            "%player%",
            "%player_name%",
            "%player_uuid%",
            "%player_world%",
            "%player_x%",
            "%player_y%",
            "%player_z%"
    );

    private static final List<String> KEYBIND_VARIABLES = List.of(
            "%keybind%",
            "%key%"
    );

    private final ExternalPlaceholderService externalPlaceholderService;

    public VariableService(
            final ExternalPlaceholderService externalPlaceholderService
    ) {
        this.externalPlaceholderService = externalPlaceholderService;
    }

    public String applyPlayerVariables(
            final String text,
            final Player player
    ) {
        final String resolved = applyInternalPlayerVariables(text, player);

        return externalPlaceholderService.apply(player, resolved);
    }

    public String applyKeybindVariables(
            final String text,
            final KeybindEntry keybind
    ) {
        return text
                .replace("%keybind%", keybind.name())
                .replace("%key%", keybind.key());
    }

    public String applyKeybindContext(
            final String text,
            final Player player,
            final KeybindEntry keybind
    ) {
        final String playerResolved =
                applyInternalPlayerVariables(text, player);

        final String keybindResolved =
                applyKeybindVariables(playerResolved, keybind);

        return externalPlaceholderService.apply(player, keybindResolved);
    }

    public List<String> getPlayerVariables() {
        return PLAYER_VARIABLES;
    }

    public List<String> getKeybindVariables() {
        return KEYBIND_VARIABLES;
    }

    public List<String> getAllVariables() {
        return java.util.stream.Stream.concat(
                PLAYER_VARIABLES.stream(),
                KEYBIND_VARIABLES.stream()
        ).toList();
    }

    private String applyInternalPlayerVariables(
            final String text,
            final Player player
    ) {
        return text
                .replace("%player%", player.getName())
                .replace("%player_name%", player.getName())
                .replace("%player_uuid%", player.getUniqueId().toString())
                .replace("%player_world%", player.getWorld().getName())
                .replace(
                        "%player_x%",
                        formatCoordinate(player.getLocation().getX())
                )
                .replace(
                        "%player_y%",
                        formatCoordinate(player.getLocation().getY())
                )
                .replace(
                        "%player_z%",
                        formatCoordinate(player.getLocation().getZ())
                );
    }

    private String formatCoordinate(final double coordinate) {
        return String.format(
                java.util.Locale.ROOT,
                "%.2f",
                coordinate
        );
    }
}