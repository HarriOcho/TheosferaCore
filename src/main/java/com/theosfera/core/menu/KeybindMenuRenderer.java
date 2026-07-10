package com.theosfera.core.menu;

import com.theosfera.core.keybind.KeybindEntry;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public final class KeybindMenuRenderer {

    private static final String KEYBIND_ENTRY_ITEM_ID = "keybind-entry";
    private static final String KEYBIND_INFO_ITEM_ID = "keybind-info";
    private static final String ACTION_ENTRY_ITEM_ID = "action-entry";

    private final MenuItemFactory itemFactory;

    public KeybindMenuRenderer(final Plugin plugin) {
        this.itemFactory = new MenuItemFactory(plugin);
    }

    public void renderKeybindEntries(
            final Inventory inventory,
            final MenuConfig config,
            final List<KeybindEntry> keybinds,
            final int page
    ) {
        final MenuItemConfig keybindTemplate = findItem(config, KEYBIND_ENTRY_ITEM_ID);

        if (keybindTemplate == null || keybindTemplate.slots().isEmpty()) {
            return;
        }

        final int normalizedPage = Math.max(1, page);
        final int entriesPerPage = keybindTemplate.slots().size();
        final int startIndex = (normalizedPage - 1) * entriesPerPage;
        final int endIndex = Math.min(startIndex + entriesPerPage, keybinds.size());

        if (startIndex >= keybinds.size()) {
            return;
        }

        int slotIndex = 0;

        for (int index = startIndex; index < endIndex; index++) {
            final KeybindEntry entry = keybinds.get(index);
            final int slot = keybindTemplate.slots().get(slotIndex);

            final ItemStack item = itemFactory.create(
                    keybindTemplate,
                    createKeybindPlaceholders(entry),
                    OptionalInt.of(entry.id())
            );

            inventory.setItem(slot, item);
            slotIndex++;
        }
    }

    public void renderKeybindActions(
            final Inventory inventory,
            final MenuConfig config,
            final KeybindEntry entry
    ) {
        final MenuItemConfig actionTemplate =
                findItem(config, ACTION_ENTRY_ITEM_ID);

        if (actionTemplate == null || actionTemplate.slots().isEmpty()) {
            return;
        }

        final int actionsToRender = Math.min(
                entry.actions().size(),
                actionTemplate.slots().size()
        );

        for (int index = 0; index < actionsToRender; index++) {
            final int actionNumber = index + 1;
            final int slot = actionTemplate.slots().get(index);

            final Map<String, String> placeholders = Map.of(
                    "%action_number%", String.valueOf(actionNumber),
                    "%action_type%", entry.actions().get(index).getType().name(),
                    "%action_value%", entry.actions().get(index).getValue()
            );

            final ItemStack item = itemFactory.create(
                    actionTemplate,
                    placeholders,
                    OptionalInt.of(entry.id()),
                    OptionalInt.of(index)
            );

            inventory.setItem(slot, item);
        }
    }

    public boolean renderKeybindInfo(
            final Inventory inventory,
            final MenuItemConfig itemConfig,
            final KeybindEntry entry
    ) {
        if (!KEYBIND_INFO_ITEM_ID.equalsIgnoreCase(itemConfig.id())) {
            return false;
        }

        final ItemStack item = itemFactory.create(
                itemConfig,
                createKeybindPlaceholders(entry),
                OptionalInt.of(entry.id())
        );

        for (final int slot : itemConfig.slots()) {
            inventory.setItem(slot, item.clone());
        }

        return true;
    }

    public boolean isKeybindEntryItem(final MenuItemConfig itemConfig) {
        return KEYBIND_ENTRY_ITEM_ID.equalsIgnoreCase(itemConfig.id());
    }

    private MenuItemConfig findItem(
            final MenuConfig config,
            final String itemId
    ) {
        for (final MenuItemConfig item : config.items()) {
            if (itemId.equalsIgnoreCase(item.id())) {
                return item;
            }
        }

        return null;
    }

    public boolean isActionEntryItem(
            final MenuItemConfig itemConfig
    ) {
        return ACTION_ENTRY_ITEM_ID.equalsIgnoreCase(itemConfig.id());
    }

    private Map<String, String> createKeybindPlaceholders(
            final KeybindEntry entry
    ) {
        final String actionsCount = String.valueOf(entry.actions().size());

        return Map.of(
                "%keybind_id%", String.valueOf(entry.id()),
                "%keybind_name%", entry.name(),
                "%keybind_description%", entry.description(),
                "%keybind_key%", entry.key(),
                "%keybind_actions%", actionsCount,
                "%keybind_actions_count%", actionsCount
        );
    }
}