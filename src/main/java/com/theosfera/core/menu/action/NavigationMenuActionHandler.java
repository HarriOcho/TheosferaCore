package com.theosfera.core.menu.action;

import com.theosfera.core.menu.MenuItemTag;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.OptionalInt;

public final class NavigationMenuActionHandler {

    public void register(final MenuActionRegistry registry) {
        registry.register("close", this::close);
        registry.register("back", this::back);
        registry.register("previous_page", this::previousPage);
        registry.register("next_page", this::nextPage);
        registry.register("keybind_details", this::openKeybindDetails);
        registry.register("open_menu", this::openMenu);
    }

    private void close(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        context.player().closeInventory();
    }

    private void back(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        context.menuService().openKeybindList(
                context.player(),
                context.holder().getPage()
        );
    }

    private void previousPage(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        if (context.holder().getPage() <= 1) {
            return;
        }

        context.menuService().openKeybindList(
                context.player(),
                context.holder().getPage() - 1
        );
    }

    private void nextPage(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        if (context.holder().getPage() >= context.holder().getMaxPage()) {
            return;
        }

        context.menuService().openKeybindList(
                context.player(),
                context.holder().getPage() + 1
        );
    }

    private void openKeybindDetails(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        final Integer keybindId = context.data().get(
                new NamespacedKey(context.plugin(), MenuItemTag.KEYBIND_ID),
                PersistentDataType.INTEGER
        );

        if (keybindId == null) {
            return;
        }

        context.menuService().openKeybindDetails(
                context.player(),
                keybindId,
                context.holder().getPage()
        );
    }

    private void openMenu(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        final String menuId = action.value().trim();

        if (menuId.isBlank()) {
            return;
        }

        if ("keybind-list".equalsIgnoreCase(menuId)) {
            context.menuService().openKeybindList(
                    context.player(),
                    context.holder().getPage()
            );
            return;
        }

        final int page = context.holder().getPage();

        final OptionalInt holderKeybindId = context.holder().getKeybindId();

        final int resolvedKeybindId = holderKeybindId.isPresent()
                ? holderKeybindId.getAsInt()
                : -1;

        if ("keybind-details".equalsIgnoreCase(menuId)) {
            if (resolvedKeybindId <= 0) {
                return;
            }

            context.menuService().openKeybindDetails(
                    context.player(),
                    resolvedKeybindId,
                    page
            );
            return;
        }

        if ("keybind-edit".equalsIgnoreCase(menuId)) {
            if (resolvedKeybindId <= 0) {
                return;
            }

            context.menuService().openKeybindEdit(
                    context.player(),
                    resolvedKeybindId,
                    page
            );
        }

        if ("keybind-actions".equalsIgnoreCase(menuId)) {
            if (resolvedKeybindId <= 0) {
                return;
            }

            context.menuService().openKeybindActions(
                    context.player(),
                    resolvedKeybindId,
                    page
            );
        }
    }
}