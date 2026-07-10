package com.theosfera.core.command;

import com.theosfera.core.keybind.KeybindManager;
import com.theosfera.core.menu.MenuManager;
import com.theosfera.core.ui.MessageService;
import com.theosfera.core.variable.VariableService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class TheosferaCommand implements CommandExecutor {

    private final MessageService messages;
    private final KeybindManager keybindManager;
    private final VariableService variableService;
    private final MenuManager menuManager;

    public TheosferaCommand(
            MessageService messages,
            KeybindManager keybindManager,
            VariableService variableService,
            MenuManager menuManager
    ) {
        this.messages = messages;
        this.keybindManager = keybindManager;
        this.variableService = variableService;
        this.menuManager = menuManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!hasAdminPermission(sender)) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            sendMainMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(sender);
            case "variables" -> sendVariables(sender);
            case "reload" -> handleReload(sender);
            default -> sendUnknownCommand(sender);
        }

        return true;
    }

    private void sendVariables(CommandSender sender) {
        messages.sendTitle(
                sender,
                messages.getMessage(sender, "title.variables"),
                messages.getMessage(sender, "subtitle.variables")
        );

        messages.sendLineKey(sender, "display.variables-player");

        for (String variable : variableService.getPlayerVariables()) {
            messages.sendLineKey(
                    sender,
                    "display.variable-line",
                    "%variable%", variable
            );
        }

        messages.sendEmpty(sender);

        messages.sendLineKey(sender, "display.variables-keybind");

        for (String variable : variableService.getKeybindVariables()) {
            messages.sendLineKey(
                    sender,
                    "display.variable-line",
                    "%variable%", variable
            );
        }

        messages.sendEmpty(sender);
    }

    private void sendMainMessage(CommandSender sender) {
        messages.sendTitle(
                sender,
                messages.getMessage(sender, "title.core"),
                messages.getMessage(sender, "subtitle.version")
        );

        messages.sendLineKey(sender, "display.main-help-hint");
        messages.sendEmpty(sender);
    }

    private void sendHelp(CommandSender sender) {
        messages.sendTitle(
                sender,
                messages.getMessage(sender, "title.core"),
                messages.getMessage(sender, "subtitle.general-help")
        );

        messages.sendLineKey(sender, "help.root");
        messages.sendLineKey(sender, "help.help");
        messages.sendLineKey(sender, "help.keybind");
        messages.sendLineKey(sender, "help.variables");
        messages.sendLineKey(sender, "help.reload");

        messages.sendEmpty(sender);
    }

    private void handleReload(CommandSender sender) {
        messages.load();
        keybindManager.load();
        menuManager.reload();

        messages.sendSuccessKey(sender, "general.reloaded");
    }

    private void sendUnknownCommand(CommandSender sender) {
        messages.sendErrorKey(sender, "general.unknown-command");
        messages.sendLineKey(sender, "general.help-hint");
    }

    private boolean hasAdminPermission(CommandSender sender) {
        return sender.hasPermission("theosfera.admin");
    }

    private void sendNoPermission(CommandSender sender) {
        messages.sendErrorKey(sender, "general.no-permission");
    }
}