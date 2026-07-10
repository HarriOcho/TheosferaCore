package com.theosfera.core.variable;

import com.theosfera.core.keybind.KeybindEntry;
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

    public String applyPlayerVariables(String text, Player player) {
        return text
                .replace("%player%", player.getName())
                .replace("%player_name%", player.getName())
                .replace("%player_uuid%", player.getUniqueId().toString())
                .replace("%player_world%", player.getWorld().getName())
                .replace("%player_x%", formatCoordinate(player.getLocation().getX()))
                .replace("%player_y%", formatCoordinate(player.getLocation().getY()))
                .replace("%player_z%", formatCoordinate(player.getLocation().getZ()));
    }

    public String applyKeybindVariables(String text, KeybindEntry keybind) {
        return text
                .replace("%keybind%", keybind.name())
                .replace("%key%", keybind.key());
    }

    public String applyKeybindContext(
            String text,
            Player player,
            KeybindEntry keybind
    ) {
        String result = applyPlayerVariables(text, player);
        return applyKeybindVariables(result, keybind);
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

    private String formatCoordinate(double coordinate) {
        return String.format(java.util.Locale.ROOT, "%.2f", coordinate);
    }
}