package com.theosfera.core.menu;

import com.theosfera.core.menu.action.MenuActionCodec;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public final class MenuItemFactory {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private final MenuTextResolver textResolver;
    private final Plugin plugin;
    private final MenuActionCodec actionCodec;

    public MenuItemFactory(final Plugin plugin) {
        this.plugin = plugin;
        this.textResolver = new MenuTextResolver();
        this.actionCodec = new MenuActionCodec();
    }

    public ItemStack create(final MenuItemConfig config) {
        return create(config, Map.of(), OptionalInt.empty(), OptionalInt.empty());
    }

    public ItemStack create(
            final MenuItemConfig config,
            final Map<String, String> placeholders
    ) {
        return create(config, placeholders, OptionalInt.empty(), OptionalInt.empty());
    }

    public ItemStack create(
            final MenuItemConfig config,
            final Map<String, String> placeholders,
            final OptionalInt keybindId
    ) {
        return create(config, placeholders, keybindId, OptionalInt.empty());
    }

    public ItemStack create(
            final MenuItemConfig config,
            final Map<String, String> placeholders,
            final OptionalInt keybindId,
            final OptionalInt actionIndex
    ) {
        final Material material = parseMaterial(config.material(), config.id());
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            plugin.getLogger().warning(
                    "Menu item '" + config.id()
                            + "' usa un material sin ItemMeta: " + material.name()
                            + ". Se reemplazará por STONE."
            );

            return create(
                    new MenuItemConfig(
                            config.id(),
                            Material.STONE.name(),
                            config.name(),
                            config.lore(),
                            config.slots(),
                            config.actions()
                    ),
                    placeholders,
                    keybindId,
                    actionIndex
            );
        }

        meta.displayName(
                deserialize(
                        textResolver.resolve(config.name(), placeholders)
                )
        );

        meta.lore(
                createLore(
                        textResolver.resolve(config.lore(), placeholders)
                )
        );

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, MenuItemTag.ITEM_ID),
                PersistentDataType.STRING,
                config.id()
        );

        if (!config.actions().isEmpty()) {
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, MenuItemTag.ACTIONS),
                    PersistentDataType.STRING,
                    actionCodec.encode(config.actions())
            );
        }

        if (keybindId.isPresent()) {
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, MenuItemTag.KEYBIND_ID),
                    PersistentDataType.INTEGER,
                    keybindId.getAsInt()
            );
        }

        if (actionIndex.isPresent()) {
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, MenuItemTag.ACTION_INDEX),
                    PersistentDataType.INTEGER,
                    actionIndex.getAsInt()
            );
        }

        item.setItemMeta(meta);

        return item;
    }

    private Material parseMaterial(final String materialName, final String itemId) {
        if (materialName == null || materialName.isBlank()) {
            plugin.getLogger().warning(
                    "Menu item '" + itemId
                            + "' no tiene material definido. Se usará STONE."
            );
            return Material.STONE;
        }

        final String normalizedMaterial = materialName.trim().toUpperCase();
        final Material material = Material.matchMaterial(normalizedMaterial);

        if (material == null || material == Material.AIR || !material.isItem()) {
            plugin.getLogger().warning(
                    "Menu item '" + itemId
                            + "' tiene material inválido/no usable: "
                            + materialName
                            + ". Se usará STONE."
            );
            return Material.STONE;
        }

        return material;
    }

    private List<Component> createLore(final List<String> rawLore) {
        final List<Component> lore = new ArrayList<>();

        for (final String line : rawLore) {
            lore.add(deserialize(line));
        }

        return lore;
    }

    private Component deserialize(final String text) {
        if (text == null) {
            return Component.empty();
        }

        return LEGACY_SERIALIZER.deserialize(text);
    }
}