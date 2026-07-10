package com.theosfera.core.command;

import com.theosfera.core.keybind.KeybindActionType;
import com.theosfera.core.keybind.KeybindEntry;
import com.theosfera.core.keybind.KeybindManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class KeybindTabCompleter implements TabCompleter {

    private static final String ADMIN_PERMISSION = "theosfera.admin";

    private static final List<String> KEYBIND_COMMANDS = List.of(
            "help",
            "list",
            "get",
            "edit",
            "add",
            "remove",
            "action",
            "menu"
    );

    private static final List<String> KEYBIND_FIELDS = List.of(
            "name",
            "description",
            "key",
            "actions"
    );

    private static final List<String> EDITABLE_KEYBIND_FIELDS = List.of(
            "name",
            "description",
            "key"
    );

    private static final List<String> ACTION_COMMANDS = List.of(
            "list",
            "add",
            "edit",
            "move",
            "remove"
    );

    private static final List<String> ACTION_TYPES = List.of(
            KeybindActionType.PLAYER_COMMAND.name(),
            KeybindActionType.CONSOLE_COMMAND.name(),
            KeybindActionType.MESSAGE.name()
    );

    private final KeybindManager keybindManager;

    public KeybindTabCompleter(KeybindManager keybindManager) {
        this.keybindManager = keybindManager;
    }

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
            return filter(KEYBIND_COMMANDS, args[0]);
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("action")) {
            return handleActionTab(args);
        }

        if (args.length == 2) {
            return switch (subCommand) {
                case "get", "edit", "remove" -> filter(getKeybindIds(), args[1]);
                case "list", "menu" -> filter(getPages(), args[1]);
                default -> List.of();
            };
        }

        if (args.length == 3) {
            return switch (subCommand) {
                case "get" -> filter(KEYBIND_FIELDS, args[2]);
                case "edit" -> filter(EDITABLE_KEYBIND_FIELDS, args[2]);
                default -> List.of();
            };
        }

        return List.of();
    }

    private List<String> handleActionTab(String[] args) {
        if (args.length == 2) {
            return filter(ACTION_COMMANDS, args[1]);
        }

        String actionSubCommand = args[1].toLowerCase();

        if (args.length == 3) {
            return switch (actionSubCommand) {
                case "list", "add", "edit", "move", "remove" ->
                        filter(getKeybindIds(), args[2]);

                default -> List.of();
            };
        }

        if (args.length == 4) {
            return switch (actionSubCommand) {
                case "add" -> filter(ACTION_TYPES, args[3]);

                case "edit", "move", "remove" ->
                        filter(getActionNumbers(args[2]), args[3]);

                default -> List.of();
            };
        }

        if (args.length == 5) {
            return switch (actionSubCommand) {
                case "edit" -> filter(ACTION_TYPES, args[4]);

                case "move" ->
                        filter(getActionNumbers(args[2]), args[4]);

                default -> List.of();
            };
        }

        return List.of();
    }

    private List<String> getKeybindIds() {
        return keybindManager.getAllSorted()
                .stream()
                .map(KeybindEntry::id)
                .map(String::valueOf)
                .toList();
    }

    private List<String> getPages() {
        int keybindCount = keybindManager.getAllSorted().size();
        int totalPages = Math.max(1, (keybindCount + 9) / 10);

        return java.util.stream.IntStream
                .rangeClosed(1, totalPages)
                .mapToObj(String::valueOf)
                .toList();
    }

    private List<String> getActionNumbers(String idText) {
        try {
            int id = Integer.parseInt(idText);

            return keybindManager.getById(id)
                    .map(entry -> java.util.stream.IntStream
                            .rangeClosed(1, entry.actions().size())
                            .mapToObj(String::valueOf)
                            .toList()
                    )
                    .orElse(List.of());
        } catch (NumberFormatException exception) {
            return List.of();
        }
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