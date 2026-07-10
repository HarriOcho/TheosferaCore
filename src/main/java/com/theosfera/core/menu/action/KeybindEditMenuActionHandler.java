package com.theosfera.core.menu.action;

import com.theosfera.core.keybind.KeybindEntry;
import com.theosfera.core.keybind.KeybindValidator;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class KeybindEditMenuActionHandler implements MenuActionHandler {

    public void register(final MenuActionRegistry registry) {
        registry.register("keybind_edit", this);
    }

    @Override
    public void execute(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        final OptionalIntWrapper keybindId = getKeybindId(context);

        if (keybindId.isEmpty()) {
            context.messageService().sendErrorKey(
                    context.player(),
                    "keybind.not-found",
                    "%id%", "?"
            );
            return;
        }

        final int id = keybindId.value();

        if (context.keybindManager().getById(id).isEmpty()) {
            context.messageService().sendErrorKey(
                    context.player(),
                    "keybind.not-found",
                    "%id%", id
            );
            return;
        }

        switch (action.value().trim().toLowerCase()) {
            case "name" -> requestName(context, id);
            case "description" -> requestDescription(context, id);
            case "key" -> requestKey(context, id);
            default -> context.plugin().getLogger().warning(
                    "[Menu] Campo keybind_edit desconocido: " + action.value()
            );
        }
    }

    private void requestName(
            final MenuActionContext context,
            final int id
    ) {
        final Player player = context.player();

        player.closeInventory();

        context.messageService().sendLine(
                player,
                "&bEscribe el nuevo nombre de la keybind o &ccancelar&b."
        );

        context.inputService().requestInput(player, input -> {
            if (isCancelled(input)) {
                context.messageService().sendWarningKey(player, "general.cancelled");
                context.menuService().openKeybindEdit(player, id);
                return;
            }

            final String newName = input.trim();

            if (!KeybindValidator.isValidName(newName)) {
                context.messageService().sendErrorKey(player, "validation.invalid-name");
                context.menuService().openKeybindEdit(player, id);
                return;
            }

            context.keybindManager().editName(id, newName);

            context.messageService().sendSuccessKey(
                    player,
                    "keybind.name-updated",
                    "%id%", id,
                    "%name%", newName
            );

            context.menuService().openKeybindEdit(player, id);
        });
    }

    private void requestDescription(
            final MenuActionContext context,
            final int id
    ) {
        final Player player = context.player();

        player.closeInventory();

        context.messageService().sendLine(
                player,
                "&bEscribe la nueva descripción o &ccancelar&b."
        );

        context.inputService().requestInput(player, input -> {
            if (isCancelled(input)) {
                context.messageService().sendWarningKey(player, "general.cancelled");
                context.menuService().openKeybindEdit(player, id);
                return;
            }

            final String newDescription = input.trim();

            if (!KeybindValidator.isValidDescription(newDescription)) {
                context.messageService().sendErrorKey(player, "validation.empty-description");
                context.menuService().openKeybindEdit(player, id);
                return;
            }

            context.keybindManager().editDescription(id, newDescription);

            context.messageService().sendSuccessKey(
                    player,
                    "keybind.description-updated",
                    "%id%", id
            );

            context.menuService().openKeybindEdit(player, id);
        });
    }

    private void requestKey(
            final MenuActionContext context,
            final int id
    ) {
        final Player player = context.player();

        player.closeInventory();

        context.messageService().sendLine(
                player,
                "&bEscribe la nueva tecla o &ccancelar&b. &7Ejemplo: A, 1, F1-F12"
        );

        context.inputService().requestInput(player, input -> {
            if (isCancelled(input)) {
                context.messageService().sendWarningKey(player, "general.cancelled");
                context.menuService().openKeybindEdit(player, id);
                return;
            }

            final String newKey = input.trim().toUpperCase();

            if (!KeybindValidator.isValidKey(newKey)) {
                context.messageService().sendErrorKey(player, "validation.invalid-key");
                context.menuService().openKeybindEdit(player, id);
                return;
            }

            final Optional<KeybindEntry> updated =
                    context.keybindManager().editKey(id, newKey);

            if (updated.isEmpty()) {
                context.messageService().sendErrorKey(
                        player,
                        "keybind.duplicate-key",
                        "%key%", newKey
                );
                context.menuService().openKeybindEdit(player, id);
                return;
            }

            context.messageService().sendSuccessKey(
                    player,
                    "keybind.key-updated",
                    "%id%", id,
                    "%key%", newKey
            );

            context.menuService().openKeybindEdit(player, id);
        });
    }

    private boolean isCancelled(final String input) {
        return input != null && input.trim().equalsIgnoreCase("cancelar");
    }

    private OptionalIntWrapper getKeybindId(final MenuActionContext context) {
        if (context.holder().getKeybindId().isEmpty()) {
            return OptionalIntWrapper.empty();
        }

        return OptionalIntWrapper.of(context.holder().getKeybindId().getAsInt());
    }

    private record OptionalIntWrapper(boolean present, int value) {

        static OptionalIntWrapper of(final int value) {
            return new OptionalIntWrapper(true, value);
        }

        static OptionalIntWrapper empty() {
            return new OptionalIntWrapper(false, -1);
        }

        boolean isEmpty() {
            return !present;
        }
    }
}