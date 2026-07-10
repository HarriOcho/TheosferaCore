package com.theosfera.core.menu;

import com.theosfera.core.keybind.KeybindEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.OptionalInt;

public final class MenuRenderer {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private final MenuItemFactory itemFactory;
    private final KeybindMenuRenderer keybindMenuRenderer;

    public MenuRenderer(final Plugin plugin) {
        this.itemFactory = new MenuItemFactory(plugin);
        this.keybindMenuRenderer = new KeybindMenuRenderer(plugin);
    }

    public Inventory renderKeybindList(
            final MenuConfig config,
            final List<KeybindEntry> keybinds,
            final int page,
            final int maxPage
    ) {
        final int normalizedPage = Math.max(1, page);
        final MenuHolder holder = new MenuHolder(
                config.id(),
                normalizedPage,
                maxPage
        );
        final Inventory inventory = createInventory(config, holder);

        for (final MenuItemConfig itemConfig : config.items()) {
            if (keybindMenuRenderer.isKeybindEntryItem(itemConfig)) {
                continue;
            }

            placeStaticItem(inventory, itemConfig);
        }

        keybindMenuRenderer.renderKeybindEntries(
                inventory,
                config,
                keybinds,
                normalizedPage
        );

        return inventory;
    }

    public Inventory renderKeybindDetails(
            final MenuConfig config,
            final KeybindEntry entry,
            final int sourcePage
    ) {
        final MenuHolder holder = new MenuHolder(
                config.id(),
                Math.max(1, sourcePage),
                OptionalInt.of(entry.id())
        );

        final Inventory inventory = createInventory(config, holder);

        for (final MenuItemConfig itemConfig : config.items()) {
            if (keybindMenuRenderer.renderKeybindInfo(inventory, itemConfig, entry)) {
                continue;
            }

            if (keybindMenuRenderer.isActionEntryItem(itemConfig)) {
                continue;
            }

            placeStaticItem(inventory, itemConfig);
        }

        keybindMenuRenderer.renderKeybindActions(
                inventory,
                config,
                entry
        );

        return inventory;
    }

    private Inventory createInventory(
            final MenuConfig config,
            final MenuHolder holder
    ) {
        final Component title = LEGACY_SERIALIZER.deserialize(config.title());

        final Inventory inventory = Bukkit.createInventory(
                holder,
                config.size(),
                title
        );

        holder.setInventory(inventory);

        return inventory;
    }

    private void placeStaticItem(
            final Inventory inventory,
            final MenuItemConfig itemConfig
    ) {
        final ItemStack item = itemFactory.create(itemConfig);

        for (final int slot : itemConfig.slots()) {
            inventory.setItem(slot, item.clone());
        }
    }
}