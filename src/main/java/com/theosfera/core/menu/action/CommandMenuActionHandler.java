package com.theosfera.core.menu.action;

import org.bukkit.Bukkit;

public final class CommandMenuActionHandler {

    public void register(final MenuActionRegistry registry) {
        registry.register("player_command", this::executePlayerCommand);
        registry.register("console_command", this::executeConsoleCommand);
    }

    private void executePlayerCommand(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        final String command = normalizeCommand(
                resolvePlayerVariables(action.value(), context)
        );

        if (command.isBlank()) {
            return;
        }

        context.player().performCommand(command);
    }

    private void executeConsoleCommand(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        final String command = normalizeCommand(
                resolvePlayerVariables(action.value(), context)
        );

        if (command.isBlank()) {
            return;
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private String resolvePlayerVariables(
            final String value,
            final MenuActionContext context
    ) {
        return context.variableService().applyPlayerVariables(
                value,
                context.player()
        );
    }

    private String normalizeCommand(final String command) {
        final String trimmed = command.trim();

        if (trimmed.startsWith("/")) {
            return trimmed.substring(1);
        }

        return trimmed;
    }
}