package com.theosfera.core.menu;

import com.theosfera.core.TheosferaCore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class MenuManager {

    private static final String MENUS_FOLDER = "menus";
    private static final String KEYBIND_LIST_MENU = "keybind-list.yml";
    private static final String KEYBIND_DETAILS_MENU = "keybind-details.yml";
    private static final String KEYBIND_EDIT_MENU = "keybind-edit.yml";
    private static final String KEYBIND_ACTIONS_MENU = "keybind-actions.yml";

    private final TheosferaCore plugin;
    private final MenuConfigLoader loader;
    private final Map<String, MenuConfig> menus = new HashMap<>();

    public MenuManager(final TheosferaCore plugin) {
        this.plugin = plugin;
        this.loader = new MenuConfigLoader(plugin);
    }

    public void reload() {
        menus.clear();

        final File menusFolder = new File(plugin.getDataFolder(), MENUS_FOLDER);

        if (!menusFolder.exists()) {
            if (!menusFolder.exists() && !menusFolder.mkdirs()) {
                plugin.getLogger().warning(
                        "No se pudo crear la carpeta de menús: "
                                + menusFolder.getPath()
                );
                return;
            }
        }

        saveDefaultMenu(KEYBIND_LIST_MENU);
        saveDefaultMenu(KEYBIND_DETAILS_MENU);
        saveDefaultMenu(KEYBIND_EDIT_MENU);
        saveDefaultMenu(KEYBIND_ACTIONS_MENU);

        final File[] files = menusFolder.listFiles((directory, name) ->
                name.toLowerCase().endsWith(".yml")
        );

        if (files == null) {
            return;
        }

        for (final File file : files) {
            final MenuConfig menu = loader.load(file);
            menus.put(menu.id().toLowerCase(), menu);
        }
    }

    public Optional<MenuConfig> getMenu(final String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(menus.get(id.toLowerCase()));
    }

    private void saveDefaultMenu(final String fileName) {
        final File file = new File(plugin.getDataFolder(), MENUS_FOLDER + "/" + fileName);

        if (file.exists()) {
            return;
        }

        plugin.saveResource(MENUS_FOLDER + "/" + fileName, false);
    }
}