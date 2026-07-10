package com.theosfera.core.menu.action;

import com.theosfera.core.keybind.KeybindAction;
import com.theosfera.core.keybind.KeybindActionType;
import com.theosfera.core.menu.MenuItemTag;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public final class KeybindActionMenuActionHandler implements MenuActionHandler {

    @Override
    public void execute(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        switch (action.value().trim().toLowerCase()) {
            case "manage" -> manageAction(context);
            case "add" -> requestAddActionType(context);
            default -> context.plugin().getLogger().warning(
                    "[Menu] Acción keybind_action desconocida: "
                            + action.value()
            );
        }
    }

    public void register(final MenuActionRegistry registry) {
        registry.register("keybind_action", this);
    }

    private void manageAction(final MenuActionContext context) {
        final Integer actionIndex = getActionIndex(context);

        if (actionIndex == null) {
            return;
        }

        final int keybindId = getKeybindId(context);

        if (keybindId <= 0) {
            return;
        }

        if (context.clickType().isRightClick()) {
            removeAction(context, keybindId, actionIndex);
            return;
        }

        if (context.clickType().isLeftClick()) {
            requestEditActionType(context, keybindId, actionIndex);
        }
    }

    private void removeAction(
            final MenuActionContext context,
            final int keybindId,
            final int actionIndex
    ) {
        final int actionNumber = actionIndex + 1;

        if (context.keybindManager()
                .removeAction(keybindId, actionIndex)
                .isEmpty()) {

            context.messageService().sendErrorKey(
                    context.player(),
                    "action.not-found",
                    "%action%", actionNumber,
                    "%id%", keybindId
            );
            return;
        }

        context.messageService().sendSuccessKey(
                context.player(),
                "action.removed",
                "%action%", actionNumber,
                "%id%", keybindId
        );

        reopenActionsMenu(context, keybindId);
    }

    private void requestEditActionType(
            final MenuActionContext context,
            final int keybindId,
            final int actionIndex
    ) {
        final Player player = context.player();
        final int actionNumber = actionIndex + 1;

        player.closeInventory();

        context.messageService().sendLine(
                player,
                "&bEditando la acción &f#" + actionNumber + "&b."
        );

        context.messageService().sendLine(
                player,
                "&bEscribe el nuevo tipo: &fPLAYER_COMMAND&7, "
                        + "&fCONSOLE_COMMAND &7o &fMESSAGE&b. "
                        + "Escribe &ccancelar&b para salir."
        );

        context.inputService().requestInput(player, input -> {
            if (isCancelled(input)) {
                context.messageService().sendWarningKey(
                        player,
                        "general.cancelled"
                );

                reopenActionsMenu(context, keybindId);
                return;
            }

            final KeybindActionType type =
                    KeybindActionType.fromName(input.trim());

            if (type == null) {
                context.messageService().sendErrorKey(
                        player,
                        "validation.invalid-action-type"
                );

                reopenActionsMenu(context, keybindId);
                return;
            }

            requestEditActionValue(
                    context,
                    keybindId,
                    actionIndex,
                    type
            );
        });
    }

    private void requestEditActionValue(
            final MenuActionContext context,
            final int keybindId,
            final int actionIndex,
            final KeybindActionType type
    ) {
        final Player player = context.player();
        final int actionNumber = actionIndex + 1;

        context.messageService().sendLine(
                player,
                "&bEscribe el nuevo valor de la acción o &ccancelar&b."
        );

        context.inputService().requestInput(player, input -> {
            if (isCancelled(input)) {
                context.messageService().sendWarningKey(
                        player,
                        "general.cancelled"
                );

                reopenActionsMenu(context, keybindId);
                return;
            }

            final String value = input.trim();

            if (value.isEmpty()) {
                context.messageService().sendErrorKey(
                        player,
                        "validation.empty-action-value"
                );

                reopenActionsMenu(context, keybindId);
                return;
            }

            if (context.keybindManager().editAction(
                    keybindId,
                    actionIndex,
                    new KeybindAction(type, value)
            ).isEmpty()) {
                context.messageService().sendErrorKey(
                        player,
                        "action.not-found",
                        "%action%", actionNumber,
                        "%id%", keybindId
                );

                reopenActionsMenu(context, keybindId);
                return;
            }

            context.messageService().sendSuccessKey(
                    player,
                    "action.updated",
                    "%action%", actionNumber,
                    "%id%", keybindId
            );

            reopenActionsMenu(context, keybindId);
        });
    }

    private void requestAddActionType(
            final MenuActionContext context
    ) {
        final int keybindId = getKeybindId(context);

        if (keybindId <= 0) {
            return;
        }

        final Player player = context.player();

        player.closeInventory();

        context.messageService().sendLine(
                player,
                "&bEscribe el tipo de acción: &fPLAYER_COMMAND&7, "
                        + "&fCONSOLE_COMMAND &7o &fMESSAGE&b. "
                        + "Escribe &ccancelar&b para salir."
        );

        context.inputService().requestInput(player, input -> {
            if (isCancelled(input)) {
                context.messageService().sendWarningKey(
                        player,
                        "general.cancelled"
                );

                reopenActionsMenu(context, keybindId);
                return;
            }

            final KeybindActionType type =
                    KeybindActionType.fromName(input.trim());

            if (type == null) {
                context.messageService().sendErrorKey(
                        player,
                        "validation.invalid-action-type"
                );

                reopenActionsMenu(context, keybindId);
                return;
            }

            requestAddActionValue(context, keybindId, type);
        });
    }

    private void requestAddActionValue(
            final MenuActionContext context,
            final int keybindId,
            final KeybindActionType type
    ) {
        final Player player = context.player();

        context.messageService().sendLine(
                player,
                "&bEscribe el valor de la acción o &ccancelar&b."
        );

        context.inputService().requestInput(player, input -> {
            if (isCancelled(input)) {
                context.messageService().sendWarningKey(
                        player,
                        "general.cancelled"
                );

                reopenActionsMenu(context, keybindId);
                return;
            }

            final String value = input.trim();

            if (value.isEmpty()) {
                context.messageService().sendErrorKey(
                        player,
                        "validation.empty-action-value"
                );

                reopenActionsMenu(context, keybindId);
                return;
            }

            context.keybindManager().addAction(
                    keybindId,
                    new KeybindAction(type, value)
            );

            context.messageService().sendSuccessKey(
                    player,
                    "action.added",
                    "%id%", keybindId
            );

            reopenActionsMenu(context, keybindId);
        });
    }

    private Integer getActionIndex(
            final MenuActionContext context
    ) {
        return context.data().get(
                new NamespacedKey(
                        context.plugin(),
                        MenuItemTag.ACTION_INDEX
                ),
                PersistentDataType.INTEGER
        );
    }

    private int getKeybindId(
            final MenuActionContext context
    ) {
        return context.holder().getKeybindId().isPresent()
                ? context.holder().getKeybindId().getAsInt()
                : -1;
    }

    private void reopenActionsMenu(
            final MenuActionContext context,
            final int keybindId
    ) {
        context.menuService().openKeybindActions(
                context.player(),
                keybindId,
                context.holder().getPage()
        );
    }

    private boolean isCancelled(final String input) {
        return input != null
                && input.trim().equalsIgnoreCase("cancelar");
    }
}