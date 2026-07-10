package com.theosfera.core.ui;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class MessageService {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&?#([A-F0-9]{6})");

    private final JavaPlugin plugin;
    private final Map<Language, YamlConfiguration> languages;

    public MessageService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.languages = new EnumMap<>(Language.class);

        createIfMissing();
        load();
    }

    private void createIfMissing() {
        File folder = new File(plugin.getDataFolder(), "lang");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        for (Language language : Language.values()) {
            saveLanguageIfMissing(language);
        }
    }

    private void saveLanguageIfMissing(Language language) {
        String resourcePath = "lang/" + language.code() + ".yml";
        File languageFile = new File(plugin.getDataFolder(), resourcePath);

        if (!languageFile.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    public void load() {
        languages.clear();

        for (Language language : Language.values()) {
            YamlConfiguration defaults = loadDefaults(language);
            YamlConfiguration loaded = new YamlConfiguration();

            String resourcePath = "lang/" + language.code() + ".yml";
            File languageFile = new File(plugin.getDataFolder(), resourcePath);

            try {
                loaded.load(languageFile);

                boolean updated = addMissingDefaults(loaded, defaults);

                if (updated) {
                    loaded.save(languageFile);
                }

                languages.put(language, loaded);
            } catch (IOException | InvalidConfigurationException exception) {
                plugin.getLogger().warning(
                        "No se pudo cargar " + resourcePath
                                + ". Se usarán los mensajes internos por defecto."
                );

                plugin.getLogger().warning(
                        "Revisa el archivo plugins/TheosferaCore/" + resourcePath + "."
                );

                languages.put(language, defaults);
            }
        }
    }

    private YamlConfiguration loadDefaults(Language language) {
        YamlConfiguration defaults = new YamlConfiguration();
        String resourcePath = "lang/" + language.code() + ".yml";

        try (InputStream stream = plugin.getResource(resourcePath)) {
            if (stream == null) {
                plugin.getLogger().severe(
                        "No se pudo encontrar " + resourcePath + " dentro del plugin."
                );

                return defaults;
            }

            defaults.load(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            );
        } catch (IOException | InvalidConfigurationException exception) {
            plugin.getLogger().severe(
                    "No se pudo cargar " + resourcePath + " interno del plugin."
            );
        }

        return defaults;
    }

    private boolean addMissingDefaults(
            YamlConfiguration target,
            YamlConfiguration defaults
    ) {
        boolean updated = false;

        for (String key : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(key)) {
                continue;
            }

            if (!target.contains(key)) {
                target.set(key, defaults.get(key));
                updated = true;
            }
        }

        return updated;
    }

    public String get(String path) {
        return get(Language.SPANISH, path);
    }

    public String get(String path, Object... replacements) {
        return get(Language.SPANISH, path, replacements);
    }

    public String get(Language language, String path) {
        return color(resolveMessage(language, path));
    }

    public String get(Language language, String path, Object... replacements) {
        return color(
                applyReplacements(
                        resolveMessage(language, path),
                        replacements
                )
        );
    }

    private String resolveMessage(Language language, String path) {
        YamlConfiguration selectedLanguage = languages.get(language);

        if (selectedLanguage != null && selectedLanguage.contains(path)) {
            return selectedLanguage.getString(path, path);
        }

        YamlConfiguration spanish = languages.get(Language.SPANISH);

        if (spanish != null && spanish.contains(path)) {
            return spanish.getString(path, path);
        }

        return path;
    }

    public String getMessage(String path) {
        return getMessage(Language.SPANISH, path);
    }

    public String getMessage(String path, Object... replacements) {
        return getMessage(Language.SPANISH, path, replacements);
    }

    public String getMessage(Language language, String path) {
        return get(language, "messages." + path);
    }

    public String getMessage(
            Language language,
            String path,
            Object... replacements
    ) {
        return get(
                language,
                "messages." + path,
                replacements
        );
    }

    public Language getLanguage(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return Language.SPANISH;
        }

        return Language.fromLocale(player.locale().toLanguageTag());
    }

    public String getMessage(CommandSender sender, String path) {
        return getMessage(getLanguage(sender), path);
    }

    public String getMessage(
            CommandSender sender,
            String path,
            Object... replacements
    ) {
        return getMessage(
                getLanguage(sender),
                path,
                replacements
        );
    }

    private String applyReplacements(String text, Object... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Los reemplazos deben venir en pares: placeholder, valor.");
        }

        String result = text;

        for (int index = 0; index < replacements.length; index += 2) {
            String placeholder = String.valueOf(replacements[index]);
            String value = String.valueOf(replacements[index + 1]);

            result = result.replace(placeholder, value);
        }

        return result;
    }

    public String color(String text) {
        if (text == null) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(
                    builder,
                    ChatColor.of("#" + matcher.group(1)).toString()
            );
        }

        matcher.appendTail(builder);

        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    public void sendTitle(CommandSender sender, String title, String subtitle) {
        sender.sendMessage("");
        sender.sendMessage(
                MessageCenteringService.center(
                        TheosferaPalette.LIGHT + color(title)
                )
        );
        sender.sendMessage(
                MessageCenteringService.center(
                        TheosferaPalette.GOLD + color(subtitle)
                )
        );
        sender.sendMessage("");
    }

    public void sendLine(CommandSender sender, String message) {
        sender.sendMessage(
                TheosferaPalette.IVORY + color(message)
        );
    }

    public void sendHoverLine(CommandSender sender, String message, String hover) {
        Component line = LegacyComponentSerializer.legacySection()
                .deserialize(
                        TheosferaPalette.IVORY + color(message)
                )
                .hoverEvent(
                        HoverEvent.showText(
                                LegacyComponentSerializer.legacySection()
                                        .deserialize(color(hover))
                        )
                );

        sender.sendMessage(line);
    }

    public void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public void sendCentered(CommandSender sender, String message) {
        sender.sendMessage(
                MessageCenteringService.center(
                        color(message)
                )
        );
    }

    public void sendEmpty(CommandSender sender) {
        sender.sendMessage("");
    }

    public void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(TheosferaPalette.GRAY + "- " + TheosferaPalette.SUCCESS + color(message));
    }

    public void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(TheosferaPalette.GRAY + "- " + TheosferaPalette.WARNING + color(message));
    }

    public void sendError(CommandSender sender, String message) {
        sender.sendMessage(TheosferaPalette.GRAY + "- " + TheosferaPalette.ERROR + color(message));
    }

    public void sendLineKey(CommandSender sender, String path) {
        sendLine(sender, getMessage(sender, path));
    }

    public void sendLineKey(CommandSender sender, String path, Object... replacements) {
        sendLine(sender, getMessage(sender, path, replacements));
    }

    public void sendSuccessKey(CommandSender sender, String path) {
        sendSuccess(sender, getMessage(sender, path));
    }

    public void sendSuccessKey(CommandSender sender, String path, Object... replacements) {
        sendSuccess(sender, getMessage(sender, path, replacements));
    }

    public void sendWarningKey(CommandSender sender, String path) {
        sendWarning(sender, getMessage(sender, path));
    }

    public void sendWarningKey(CommandSender sender, String path, Object... replacements) {
        sendWarning(sender, getMessage(sender, path, replacements));
    }

    public void sendErrorKey(CommandSender sender, String path) {
        sendError(sender, getMessage(sender, path));
    }

    public void sendErrorKey(CommandSender sender, String path, Object... replacements) {
        sendError(sender, getMessage(sender, path, replacements));
    }

    public void sendFeatureDisabled(CommandSender sender) {
        sendError(sender, getMessage("general.feature-disabled"));
        sendLine(sender, getMessage("general.feature-disabled-extra"));
    }
}