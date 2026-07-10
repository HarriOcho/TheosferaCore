package com.theosfera.core.command;

import com.theosfera.core.keybind.KeybindEntry;
import com.theosfera.core.keybind.KeybindActionExecutor;
import com.theosfera.core.keybind.KeybindManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class KeyCommand implements CommandExecutor {

    private final KeybindManager keybindManager;
    private final KeybindActionExecutor actionExecutor;

    public KeyCommand(KeybindManager keybindManager, KeybindActionExecutor actionExecutor) {
        this.keybindManager = keybindManager;
        this.actionExecutor = actionExecutor;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cUso correcto: /key <tecla>");
            return true;
        }

        String key = args[0].toUpperCase();
        KeybindEntry keybind = keybindManager.findByKey(key);

        if (keybind == null) {
            player.sendMessage("§cNo hay ninguna acción asignada a la tecla §f" + key + "§c.");
            return true;
        }

        actionExecutor.execute(player, keybind);
        return true;
    }
}