package com.theosfera.core.keybind;

import com.theosfera.core.variable.VariableService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class KeybindActionExecutor {

    private final JavaPlugin plugin;
    private final VariableService variableService;

    public KeybindActionExecutor(
            JavaPlugin plugin,
            VariableService variableService
    ) {
        this.plugin = plugin;
        this.variableService = variableService;
    }

    public void execute(Player player, KeybindEntry keybind) {
        for (KeybindAction action : keybind.actions()) {
            String value = variableService.applyKeybindContext(
                    action.getValue(),
                    player,
                    keybind
            );

            switch (action.getType()) {
                case PLAYER_COMMAND -> player.performCommand(value);

                case CONSOLE_COMMAND -> Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        value
                );

                case MESSAGE -> player.sendMessage(value);

                default -> plugin.getLogger().warning(
                        "Acción desconocida en keybind '" + keybind.name() + "'."
                );
            }
        }
    }
}