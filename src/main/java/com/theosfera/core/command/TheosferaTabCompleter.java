package com.theosfera.core.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TheosferaTabCompleter implements TabCompleter {

    private static final String ADMIN_PERMISSION = "theosfera.admin";

    private static final List<String> ROOT_COMMANDS = List.of(
            "help",
            "variables",
            "reload"
    );

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return filter(ROOT_COMMANDS, args[0]);
        }

        return List.of();
    }

    private List<String> filter(List<String> suggestions, String input) {
        String normalizedInput = input.toLowerCase();

        return suggestions.stream()
                .filter(suggestion ->
                        suggestion.toLowerCase().startsWith(normalizedInput)
                )
                .toList();
    }
}