package com.theosfera.core.keybind;

import com.theosfera.core.variable.VariableService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.theosfera.core.ui.MessageService;

public final class KeybindActionExecutor {

    private final JavaPlugin plugin;
    private final VariableService variableService;
    private final MessageService messageService;

    public KeybindActionExecutor(
            final JavaPlugin plugin,
            final VariableService variableService,
            final MessageService messageService
    ) {
        this.plugin = plugin;
        this.variableService = variableService;
        this.messageService = messageService;
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

                case MESSAGE -> messageService.sendRaw(player, value);

                default -> plugin.getLogger().warning(
                        "Acción desconocida en keybind '" + keybind.name() + "'."
                );
            }
        }
    }
}