package com.theosfera.core.keybind;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class KeybindStorage {

    private final JavaPlugin plugin;
    private final File file;

    public KeybindStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "keybinds.yml");
    }

    public void createIfMissing() {
        if (!file.exists()) {
            plugin.saveResource("keybinds.yml", false);
        }
    }

    public Map<Integer, KeybindEntry> loadAll() {
        createIfMissing();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("keybinds");

        Map<Integer, KeybindEntry> keybinds = new LinkedHashMap<>();

        if (section == null) {
            return keybinds;
        }

        for (String key : section.getKeys(false)) {
            int id;

            try {
                id = Integer.parseInt(key);
            } catch (NumberFormatException exception) {
                plugin.getLogger().warning("ID inválido en keybinds.yml: " + key);
                continue;
            }

            String path = "keybinds." + id;
            String name = config.getString(path + ".name", "");
            String keyboardKey = config.getString(path + ".key", "");
            String description = config.getString(path + ".description", "Sin descripción.");

            List<KeybindAction> actions = new ArrayList<>();
            ConfigurationSection actionsSection = config.getConfigurationSection(path + ".actions");

            if (actionsSection != null) {
                for (String actionKey : actionsSection.getKeys(false)) {
                    String actionPath = path + ".actions." + actionKey;

                    String typeName = config.getString(actionPath + ".type", "");
                    String value = config.getString(actionPath + ".value", "");

                    KeybindActionType type = KeybindActionType.fromName(typeName);

                    if (type == null) {
                        plugin.getLogger().warning("Tipo de acción inválido en keybinds.yml: " + typeName);
                        continue;
                    }

                    actions.add(new KeybindAction(type, value));
                }
            }

            keybinds.put(id, new KeybindEntry(id, name, description, keyboardKey, actions));
        }

        return keybinds;
    }

    public void saveAll(Map<Integer, KeybindEntry> keybinds) {
        YamlConfiguration config = new YamlConfiguration();
        config.createSection("keybinds");

        for (KeybindEntry entry : keybinds.values()) {
            String path = "keybinds." + entry.id();

            config.set(path + ".name", entry.name());
            config.set(path + ".description", entry.description());
            config.set(path + ".key", entry.key());

            for (int i = 0; i < entry.actions().size(); i++) {
                KeybindAction action = entry.actions().get(i);
                String actionPath = path + ".actions." + i;

                config.set(actionPath + ".type", action.getType().yamlName());
                config.set(actionPath + ".value", action.getValue());
            }
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("No se pudo guardar keybinds.yml: " + exception.getMessage());
        }
    }
}