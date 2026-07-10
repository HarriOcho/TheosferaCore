package com.theosfera.core.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MenuConfigLoader {

    private final Plugin plugin;

    public MenuConfigLoader(final Plugin plugin) {
        this.plugin = plugin;
    }

    public MenuConfig load(final File file) {
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        final String id = configuration.getString("id", file.getName().replace(".yml", ""));
        final String title = configuration.getString("title", id);
        final int size = normalizeSize(configuration.getInt("size", 54));

        final List<MenuItemConfig> items = loadItems(
                id,
                configuration.getConfigurationSection("items"),
                size
        );

        return new MenuConfig(
                id,
                title,
                size,
                List.copyOf(items)
        );
    }

    private List<MenuItemConfig> loadItems(
            final String menuId,
            final ConfigurationSection itemsSection,
            final int inventorySize
    ) {
        final List<MenuItemConfig> items = new ArrayList<>();

        if (itemsSection == null) {
            return items;
        }

        for (final String itemId : itemsSection.getKeys(false)) {
            final ConfigurationSection itemSection =
                    itemsSection.getConfigurationSection(itemId);

            if (itemSection == null) {
                continue;
            }

            final MenuItemConfig item = loadItem(
                    menuId,
                    itemId,
                    itemSection,
                    inventorySize
            );

            if (item.slots().isEmpty()) {
                continue;
            }

            items.add(item);
        }

        return items;
    }

    private MenuItemConfig loadItem(
            final String menuId,
            final String itemId,
            final ConfigurationSection section,
            final int inventorySize
    ) {
        final String material = section.getString("material", "STONE");
        final String name = section.getString("name", itemId);
        final List<String> lore = section.getStringList("lore");
        final List<String> actions = section.getStringList("actions");

        final Object rawSlots;

        if (section.contains("slots")) {
            rawSlots = section.getList("slots");
        } else {
            rawSlots = section.get("slot");
        }

        final List<Integer> slots = SlotParser.parseSingleValue(
                rawSlots,
                inventorySize
        );

        if (slots.isEmpty()) {
            plugin.getLogger().warning(
                    "[Menu] Menú '" + menuId
                            + "', item '" + itemId
                            + "': no tiene slots válidos. Valor recibido: "
                            + rawSlots
                            + ". Tamaño del inventario: "
                            + inventorySize
                            + "."
            );
        }

        return new MenuItemConfig(
                itemId,
                material,
                name,
                List.copyOf(lore),
                List.copyOf(slots),
                List.copyOf(actions)
        );
    }

    private int normalizeSize(final int requestedSize) {
        if (requestedSize < 9) {
            return 9;
        }

        if (requestedSize > 54) {
            return 54;
        }

        final int remainder = requestedSize % 9;

        if (remainder == 0) {
            return requestedSize;
        }

        return requestedSize + (9 - remainder);
    }
}