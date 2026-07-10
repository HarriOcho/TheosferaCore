package com.theosfera.core.menu;

import com.theosfera.core.keybind.KeybindManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import com.theosfera.core.keybind.KeybindEntry;
import java.util.List;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public final class MenuService {

    private static final String KEYBIND_LIST_MENU_ID = "keybind-list";
    private static final String KEYBIND_DETAILS_MENU_ID = "keybind-details";
    private static final String KEYBIND_EDIT_MENU_ID = "keybind-edit";
    private static final String KEYBIND_ACTIONS_MENU_ID = "keybind-actions";

    private final MenuManager menuManager;
    private final KeybindManager keybindManager;
    private final MenuRenderer menuRenderer;

    public MenuService(
            final Plugin plugin,
            final MenuManager menuManager,
            final KeybindManager keybindManager
    ) {
        this.menuManager = menuManager;
        this.keybindManager = keybindManager;
        this.menuRenderer = new MenuRenderer(plugin);
    }

    public boolean openKeybindList(
            final Player player,
            final int page
    ) {
        final Optional<MenuConfig> optionalMenu =
                menuManager.getMenu(KEYBIND_LIST_MENU_ID);

        if (optionalMenu.isEmpty()) {
            return false;
        }

        final MenuConfig menu = optionalMenu.get();
        final List<KeybindEntry> keybinds = keybindManager.getAllSorted();
        final int maxPage = getKeybindListMaxPage(menu, keybinds);
        final int safePage = Math.max(1, Math.min(page, maxPage));

        final Inventory inventory = menuRenderer.renderKeybindList(
                menu,
                keybinds,
                safePage,
                maxPage
        );

        player.openInventory(inventory);

        return true;
    }

    private int getKeybindListMaxPage(
            final MenuConfig menu,
            final List<KeybindEntry> keybinds
    ) {
        int entriesPerPage = 1;

        for (final MenuItemConfig item : menu.items()) {
            if ("keybind-entry".equalsIgnoreCase(item.id())) {
                entriesPerPage = Math.max(1, item.slots().size());
                break;
            }
        }

        return Math.max(
                1,
                (int) Math.ceil((double) keybinds.size() / entriesPerPage)
        );
    }

    public boolean openKeybindDetails(
            final Player player,
            final int keybindId,
            final int sourcePage
    ) {
        final Optional<MenuConfig> optionalMenu =
                menuManager.getMenu(KEYBIND_DETAILS_MENU_ID);

        if (optionalMenu.isEmpty()) {
            return false;
        }

        final Optional<KeybindEntry> optionalEntry =
                keybindManager.getById(keybindId);

        if (optionalEntry.isEmpty()) {
            return false;
        }

        final Inventory inventory = menuRenderer.renderKeybindDetails(
                optionalMenu.get(),
                optionalEntry.get(),
                sourcePage
        );

        player.openInventory(inventory);

        return true;
    }

    public boolean openKeybindEdit(
            final Player player,
            final int keybindId,
            final int sourcePage
    ) {
        final Optional<MenuConfig> optionalMenu =
                menuManager.getMenu(KEYBIND_EDIT_MENU_ID);

        if (optionalMenu.isEmpty()) {
            return false;
        }

        final Optional<KeybindEntry> optionalEntry =
                keybindManager.getById(keybindId);

        if (optionalEntry.isEmpty()) {
            return false;
        }

        final Inventory inventory = menuRenderer.renderKeybindDetails(
                optionalMenu.get(),
                optionalEntry.get(),
                sourcePage
        );

        player.openInventory(inventory);

        return true;
    }

    public boolean openKeybindEdit(
            final Player player,
            final int keybindId
    ) {
        return openKeybindEdit(player, keybindId, 1);
    }

    public boolean openKeybindActions(
            final Player player,
            final int keybindId,
            final int sourcePage
    ) {
        final Optional<MenuConfig> optionalMenu =
                menuManager.getMenu(KEYBIND_ACTIONS_MENU_ID);

        if (optionalMenu.isEmpty()) {
            return false;
        }

        final Optional<KeybindEntry> optionalEntry =
                keybindManager.getById(keybindId);

        if (optionalEntry.isEmpty()) {
            return false;
        }

        final Inventory inventory = menuRenderer.renderKeybindDetails(
                optionalMenu.get(),
                optionalEntry.get(),
                sourcePage
        );

        player.openInventory(inventory);

        return true;
    }
}