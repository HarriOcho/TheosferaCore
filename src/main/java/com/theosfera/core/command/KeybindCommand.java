package com.theosfera.core.command;

import com.theosfera.core.keybind.KeybindAction;
import com.theosfera.core.keybind.KeybindActionType;
import com.theosfera.core.keybind.KeybindEntry;
import com.theosfera.core.keybind.KeybindManager;
import com.theosfera.core.menu.MenuService;
import com.theosfera.core.ui.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.theosfera.core.keybind.KeybindValidator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class KeybindCommand implements CommandExecutor {

    private static final int KEYBINDS_PER_PAGE = 10;
    private static final String ADMIN_PERMISSION = "theosfera.admin";

    private final MessageService messages;
    private final KeybindManager keybindManager;
    private final MenuService menuService;

    public KeybindCommand(
            MessageService messages,
            KeybindManager keybindManager,
            MenuService menuService
    ) {
        this.messages = messages;
        this.keybindManager = keybindManager;
        this.menuService = menuService;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            messages.sendErrorKey(sender, "general.no-permission");
            return true;
        }

        if (args.length == 0) {
            sendKeybindUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> sendKeybindUsage(sender);
            case "list" -> handleKeybindList(sender, args);
            case "menu" -> handleKeybindMenu(sender, args);
            case "get" -> handleKeybindGet(sender, args);
            case "edit" -> handleKeybindEdit(sender, args);
            case "add" -> handleKeybindAdd(sender, args);
            case "remove" -> handleKeybindRemove(sender, args);
            case "action" -> handleKeybindAction(sender, args);
            default -> sendKeybindUsage(sender);
        }

        return true;
    }

    private void handleKeybindList(CommandSender sender, String[] args) {
        int page = 1;

        if (args.length >= 2) {
            Optional<Integer> parsedPage = parsePositiveInteger(args[1]);

            if (parsedPage.isEmpty()) {
                messages.sendErrorKey(sender, "validation.positive-page");
                return;
            }

            page = parsedPage.get();
        }

        List<KeybindEntry> keybinds = keybindManager.getAllSorted();

        if (keybinds.isEmpty()) {
            messages.sendWarningKey(sender, "keybind.no-keybinds");
            return;
        }

        int totalPages = Math.max(
                1,
                (int) Math.ceil((double) keybinds.size() / KEYBINDS_PER_PAGE)
        );

        if (page > totalPages) {
            messages.sendErrorKey(
                    sender,
                    "keybind.page-not-found",
                    "%page%", page,
                    "%total_pages%", totalPages
            );
            return;
        }

        int startIndex = (page - 1) * KEYBINDS_PER_PAGE;
        int endIndex = Math.min(startIndex + KEYBINDS_PER_PAGE, keybinds.size());

        messages.sendTitle(
                sender,
                messages.getMessage(sender, "title.keybinds"),
                messages.getMessage(
                        sender,
                        "subtitle.keybind-page",
                        "%page%", page,
                        "%total_pages%", totalPages
                )
        );

        for (int index = startIndex; index < endIndex; index++) {
            KeybindEntry entry = keybinds.get(index);

            messages.sendHoverLine(
                    sender,
                    messages.getMessage(
                            sender,
                            "display.keybind-list-line",
                            "%id%", entry.id(),
                            "%name%", entry.name()
                    ),
                    messages.getMessage(
                            sender,
                            "display.keybind-list-hover",
                            "%id%", entry.id(),
                            "%name%", entry.name(),
                            "%description%", entry.description(),
                            "%key%", entry.key(),
                            "%actions%", entry.actions().size()
                    )
            );
        }

        messages.sendEmpty(sender);
    }

    private void handleKeybindMenu(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.sendErrorKey(sender, "general.only-player");
            return;
        }

        int page = 1;

        if (args.length >= 2) {
            Optional<Integer> parsedPage = parsePositiveInteger(args[1]);

            if (parsedPage.isEmpty()) {
                messages.sendErrorKey(sender, "validation.positive-page");
                return;
            }

            page = parsedPage.get();
        }

        if (!menuService.openKeybindList(player, page)) {
            messages.sendErrorKey(sender, "menu.not-found");
        }
    }

    private void handleKeybindGet(CommandSender sender, String[] args) {
        if (args.length != 3) {
            messages.sendErrorKey(sender, "usage.keybind-get");
            return;
        }

        Optional<KeybindEntry> optionalEntry = getKeybindFromArgument(sender, args[1]);

        if (optionalEntry.isEmpty()) {
            return;
        }

        KeybindEntry entry = optionalEntry.get();

        switch (args[2].toLowerCase()) {
            case "name" -> messages.sendLineKey(
                    sender,
                    "display.keybind-name",
                    "%id%", entry.id(),
                    "%name%", entry.name()
            );

            case "description" -> messages.sendLineKey(
                    sender,
                    "display.keybind-description",
                    "%id%", entry.id(),
                    "%description%", entry.description()
            );

            case "key" -> messages.sendLineKey(
                    sender,
                    "display.keybind-key",
                    "%id%", entry.id(),
                    "%key%", entry.key()
            );

            case "actions" -> sendKeybindActions(sender, entry);

            default -> messages.sendErrorKey(sender, "field.keybind-get");
        }
    }

    private void handleKeybindEdit(CommandSender sender, String[] args) {
        if (args.length < 4) {
            messages.sendErrorKey(sender, "usage.keybind-edit");
            return;
        }

        Optional<Integer> parsedId = parsePositiveInteger(args[1]);

        if (parsedId.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-id");
            return;
        }

        int id = parsedId.get();

        if (keybindManager.getById(id).isEmpty()) {
            messages.sendErrorKey(sender, "keybind.not-found", "%id%", id);
            return;
        }

        switch (args[2].toLowerCase()) {
            case "name" -> editKeybindName(sender, id, args);
            case "description" -> editKeybindDescription(sender, id, args);
            case "key" -> editKeybindKey(sender, id, args);
            default -> messages.sendErrorKey(sender, "field.keybind-edit");
        }
    }

    private void editKeybindName(CommandSender sender, int id, String[] args) {
        String newName = args[3];

        if (!isValidName(newName)) {
            messages.sendErrorKey(sender, "validation.invalid-name");
            return;
        }

        keybindManager.editName(id, newName);

        messages.sendSuccessKey(sender, "keybind.name-updated", "%id%", id, "%name%", newName);
    }

    private void editKeybindDescription(CommandSender sender, int id, String[] args) {
        String newDescription = joinArguments(args, 3).trim();

        if (newDescription.isEmpty()) {
            messages.sendErrorKey(sender, "validation.empty-description");
            return;
        }

        keybindManager.editDescription(id, newDescription);

        messages.sendSuccessKey(sender, "keybind.description-updated", "%id%", id);
    }

    private void editKeybindKey(CommandSender sender, int id, String[] args) {
        String newKey = args[3].toUpperCase();

        if (!isValidKey(newKey)) {
            messages.sendErrorKey(sender, "validation.invalid-key");
            return;
        }

        Optional<KeybindEntry> updated = keybindManager.editKey(id, newKey);

        if (updated.isEmpty()) {
            messages.sendErrorKey(sender, "keybind.duplicate-key", "%key%", newKey);
            return;
        }

        messages.sendSuccessKey(sender, "keybind.key-updated", "%id%", id, "%key%", newKey);
    }

    private void handleKeybindAdd(CommandSender sender, String[] args) {
        if (args.length != 3) {
            messages.sendErrorKey(sender, "usage.keybind-add");
            return;
        }

        String name = args[1];

        if (!isValidName(name)) {
            messages.sendErrorKey(sender, "validation.invalid-name");
            return;
        }

        String key = args[2].toUpperCase();
        String description = messages.getMessage(sender, "keybind.default-description");

        if (!isValidKey(key)) {
            messages.sendErrorKey(sender, "validation.invalid-key");
            return;
        }

        Optional<KeybindEntry> created = keybindManager.add(name, key, description);

        if (created.isEmpty()) {
            messages.sendErrorKey(sender, "keybind.duplicate-key", "%key%", key);
            return;
        }

        messages.sendSuccessKey(sender, "keybind.added", "%id%", created.get().id());
    }

    private void handleKeybindRemove(CommandSender sender, String[] args) {
        if (args.length != 2) {
            messages.sendErrorKey(sender, "usage.keybind-remove");
            return;
        }

        Optional<Integer> parsedId = parsePositiveInteger(args[1]);

        if (parsedId.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-id");
            return;
        }

        int id = parsedId.get();

        if (!keybindManager.remove(id)) {
            messages.sendErrorKey(sender, "keybind.not-found", "%id%", id);
            return;
        }

        messages.sendSuccessKey(sender, "keybind.removed", "%id%", id);
    }

    private void handleKeybindAction(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendKeybindActionUsage(sender);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list" -> handleKeybindActionList(sender, args);
            case "add" -> handleKeybindActionAdd(sender, args);
            case "edit" -> handleKeybindActionEdit(sender, args);
            case "move" -> handleKeybindActionMove(sender, args);
            case "remove" -> handleKeybindActionRemove(sender, args);
            default -> sendKeybindActionUsage(sender);
        }
    }

    private void handleKeybindActionList(CommandSender sender, String[] args) {
        if (args.length != 3) {
            messages.sendErrorKey(sender, "usage.action-list");
            return;
        }

        Optional<KeybindEntry> entry = getKeybindFromArgument(sender, args[2]);

        entry.ifPresent(value -> sendKeybindActions(sender, value));
    }

    private void handleKeybindActionAdd(CommandSender sender, String[] args) {
        if (args.length < 5) {
            messages.sendErrorKey(sender, "usage.action-add");
            return;
        }

        Optional<Integer> parsedId = parsePositiveInteger(args[2]);

        if (parsedId.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-id");
            return;
        }

        int id = parsedId.get();

        if (keybindManager.getById(id).isEmpty()) {
            messages.sendErrorKey(sender, "keybind.not-found", "%id%", id);
            return;
        }

        KeybindActionType type = KeybindActionType.fromName(args[3]);

        if (type == null) {
            messages.sendErrorKey(sender, "validation.invalid-action-type");
            return;
        }

        String value = joinArguments(args, 4).trim();

        if (value.isEmpty()) {
            messages.sendErrorKey(sender, "validation.empty-action-value");
            return;
        }

        keybindManager.addAction(id, new KeybindAction(type, value));

        messages.sendSuccessKey(sender, "action.added", "%id%", id);
    }

    private void handleKeybindActionEdit(CommandSender sender, String[] args) {
        if (args.length < 6) {
            messages.sendErrorKey(sender, "usage.action-edit");
            return;
        }

        Optional<Integer> parsedId = parsePositiveInteger(args[2]);
        Optional<Integer> parsedAction = parsePositiveInteger(args[3]);

        if (parsedId.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-id");
            return;
        }

        if (parsedAction.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-action");
            return;
        }

        int id = parsedId.get();
        int actionNumber = parsedAction.get();

        if (keybindManager.getById(id).isEmpty()) {
            messages.sendErrorKey(sender, "keybind.not-found", "%id%", id);
            return;
        }

        KeybindActionType type = KeybindActionType.fromName(args[4]);

        if (type == null) {
            messages.sendErrorKey(sender, "validation.invalid-action-type");
            return;
        }

        String value = joinArguments(args, 5).trim();

        if (value.isEmpty()) {
            messages.sendErrorKey(sender, "validation.empty-action-value");
            return;
        }

        Optional<KeybindEntry> updated = keybindManager.editAction(
                id,
                actionNumber - 1,
                new KeybindAction(type, value)
        );

        if (updated.isEmpty()) {
            messages.sendErrorKey(sender, "action.not-found", "%action%", actionNumber, "%id%", id);
            return;
        }

        messages.sendSuccessKey(sender, "action.updated", "%action%", actionNumber, "%id%", id);
    }

    private void handleKeybindActionMove(CommandSender sender, String[] args) {
        if (args.length != 5) {
            messages.sendErrorKey(sender, "usage.action-move");
            return;
        }

        Optional<Integer> parsedId = parsePositiveInteger(args[2]);
        Optional<Integer> parsedAction = parsePositiveInteger(args[3]);
        Optional<Integer> parsedPosition = parsePositiveInteger(args[4]);

        if (parsedId.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-id");
            return;
        }

        if (parsedAction.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-action");
            return;
        }

        if (parsedPosition.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-position");
            return;
        }

        int id = parsedId.get();
        int actionNumber = parsedAction.get();
        int position = parsedPosition.get();

        if (keybindManager.getById(id).isEmpty()) {
            messages.sendErrorKey(sender, "keybind.not-found", "%id%", id);
            return;
        }

        Optional<KeybindEntry> updated = keybindManager.moveAction(
                id,
                actionNumber - 1,
                position - 1
        );

        if (updated.isEmpty()) {
            messages.sendErrorKey(sender, "action.move-failed");
            return;
        }

        messages.sendSuccessKey(
                sender,
                "action.moved",
                "%action%", actionNumber,
                "%position%", position,
                "%id%", id
        );
    }

    private void handleKeybindActionRemove(CommandSender sender, String[] args) {
        if (args.length != 4) {
            messages.sendErrorKey(sender, "usage.action-remove");
            return;
        }

        Optional<Integer> parsedId = parsePositiveInteger(args[2]);
        Optional<Integer> parsedAction = parsePositiveInteger(args[3]);

        if (parsedId.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-id");
            return;
        }

        if (parsedAction.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-action");
            return;
        }

        int id = parsedId.get();
        int actionNumber = parsedAction.get();

        if (keybindManager.getById(id).isEmpty()) {
            messages.sendErrorKey(sender, "keybind.not-found", "%id%", id);
            return;
        }

        Optional<KeybindEntry> updated = keybindManager.removeAction(id, actionNumber - 1);

        if (updated.isEmpty()) {
            messages.sendErrorKey(sender, "action.not-found", "%action%", actionNumber, "%id%", id);
            return;
        }

        messages.sendSuccessKey(sender, "action.removed", "%action%", actionNumber, "%id%", id);
    }

    private Optional<KeybindEntry> getKeybindFromArgument(
            CommandSender sender,
            String argument
    ) {
        Optional<Integer> parsedId = parsePositiveInteger(argument);

        if (parsedId.isEmpty()) {
            messages.sendErrorKey(sender, "validation.positive-id");
            return Optional.empty();
        }

        int id = parsedId.get();
        Optional<KeybindEntry> entry = keybindManager.getById(id);

        if (entry.isEmpty()) {
            messages.sendErrorKey(sender, "keybind.not-found", "%id%", id);
        }

        return entry;
    }

    private void sendKeybindActions(
            CommandSender sender,
            KeybindEntry entry
    ) {
        messages.sendTitle(
                sender,
                messages.getMessage(sender, "title.keybinds"),
                messages.getMessage(sender, "subtitle.keybind-actions-id", "%id%", entry.id())
        );

        if (entry.actions().isEmpty()) {
            messages.sendWarningKey(sender, "action.no-actions");
            return;
        }

        for (int index = 0; index < entry.actions().size(); index++) {
            KeybindAction action = entry.actions().get(index);

            messages.sendLineKey(
                    sender,
                    "display.action-list-line",
                    "%action%", index + 1,
                    "%type%", action.getType().name(),
                    "%value%", action.getValue()
            );
        }

        messages.sendEmpty(sender);
    }

    private String joinArguments(String[] args, int startIndex) {
        return String.join(
                " ",
                Arrays.copyOfRange(args, startIndex, args.length)
        );
    }

    private Optional<Integer> parsePositiveInteger(String value) {
        try {
            int number = Integer.parseInt(value);

            if (number <= 0) {
                return Optional.empty();
            }

            return Optional.of(number);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private boolean isValidName(String name) {
        return KeybindValidator.isValidName(name);
    }

    private boolean isValidKey(String key) {
        return KeybindValidator.isValidKey(key);
    }

    private void sendKeybindUsage(CommandSender sender) {
        messages.sendTitle(
                sender,
                messages.getMessage(sender, "title.keybinds"),
                messages.getMessage(sender, "subtitle.keybind-help")
        );

        messages.sendLineKey(sender, "help.keybind-help");
        messages.sendLineKey(sender, "help.keybind-list");
        messages.sendLineKey(sender, "help.keybind-get");
        messages.sendLineKey(sender, "help.keybind-edit");
        messages.sendLineKey(sender, "help.keybind-add");
        messages.sendLineKey(sender, "help.keybind-remove");
        messages.sendLineKey(sender, "help.keybind-action");
        messages.sendLineKey(sender, "help.keybind-menu");

        messages.sendEmpty(sender);
    }

    private void sendKeybindActionUsage(CommandSender sender) {
        messages.sendTitle(
                sender,
                messages.getMessage(sender, "title.keybinds"),
                messages.getMessage(sender, "subtitle.keybind-actions")
        );

        messages.sendLineKey(sender, "help.action-list");
        messages.sendLineKey(sender, "help.action-add");
        messages.sendLineKey(sender, "help.action-edit");
        messages.sendLineKey(sender, "help.action-move");
        messages.sendLineKey(sender, "help.action-remove");

        messages.sendEmpty(sender);
    }
}