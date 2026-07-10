package com.theosfera.core;

import com.theosfera.core.command.KeyCommand;
import com.theosfera.core.command.KeybindCommand;
import com.theosfera.core.command.KeybindTabCompleter;
import com.theosfera.core.command.TheosferaCommand;
import com.theosfera.core.command.TheosferaTabCompleter;
import com.theosfera.core.keybind.KeybindActionExecutor;
import com.theosfera.core.keybind.KeybindManager;
import com.theosfera.core.keybind.KeybindStorage;
import com.theosfera.core.menu.MenuListener;
import com.theosfera.core.menu.MenuManager;
import com.theosfera.core.menu.MenuService;
import com.theosfera.core.menu.input.MenuChatInputListener;
import com.theosfera.core.menu.input.MenuChatInputService;
import com.theosfera.core.ui.MessageService;
import com.theosfera.core.variable.VariableService;
import com.theosfera.core.variable.placeholder.ExternalPlaceholderService;
import com.theosfera.core.variable.placeholder.NoOpExternalPlaceholderService;
import com.theosfera.core.variable.placeholder.PlaceholderApiExternalPlaceholderService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public final class TheosferaCore extends JavaPlugin {

    private MessageService messageService;
    private KeybindManager keybindManager;
    private VariableService variableService;
    private MenuManager menuManager;
    private MenuService menuService;
    private MenuChatInputService menuChatInputService;

    @Override
    public void onEnable() {
        messageService = new MessageService(this);
        menuManager = new MenuManager(this);
        menuManager.reload();
        variableService = new VariableService(
                createExternalPlaceholderService()
        );
        menuChatInputService = new MenuChatInputService();

        KeybindStorage keybindStorage = new KeybindStorage(this);
        keybindManager = new KeybindManager(keybindStorage);
        keybindManager.load();

        menuService = new MenuService(
                this,
                menuManager,
                keybindManager
        );

        getServer().getPluginManager().registerEvents(
                new MenuListener(
                        this,
                        menuService,
                        variableService,
                        messageService,
                        keybindManager,
                        menuChatInputService
                ),
                this
        );

        getServer().getPluginManager().registerEvents(
                new MenuChatInputListener(this, menuChatInputService),
                this
        );

        for (String warning : keybindManager.findDuplicateKeyWarnings()) {
            getLogger().warning(warning);
        }

        registerTheosferaCommand();
        registerKeybindCommand();
        registerKeyCommand();

        getLogger().info("TheosferaCore iniciado correctamente.");
    }

    private void registerTheosferaCommand() {
        PluginCommand theosferaCommand = getCommand("theosfera");

        if (theosferaCommand == null) {
            getLogger().severe("No se pudo registrar el comando /theosfera.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        theosferaCommand.setExecutor(
                new TheosferaCommand(
                        messageService,
                        keybindManager,
                        variableService,
                        menuManager
                )
        );

        theosferaCommand.setTabCompleter(new TheosferaTabCompleter());
    }

    private void registerKeybindCommand() {
        PluginCommand keybindCommand = getCommand("keybind");

        if (keybindCommand == null) {
            getLogger().severe("No se pudo registrar el comando /keybind.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        keybindCommand.setExecutor(
                new KeybindCommand(
                        messageService,
                        keybindManager,
                        menuService
                )
        );

        keybindCommand.setTabCompleter(
                new KeybindTabCompleter(keybindManager)
        );
    }

    private void registerKeyCommand() {
        KeybindActionExecutor keybindActionExecutor = new KeybindActionExecutor(
                this,
                variableService
        );

        PluginCommand keyCommand = getCommand("key");

        if (keyCommand == null) {
            getLogger().severe("No se pudo registrar el comando /key.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        keyCommand.setExecutor(
                new KeyCommand(keybindManager, keybindActionExecutor)
        );
    }

    private ExternalPlaceholderService createExternalPlaceholderService() {
        if (!getServer().getPluginManager()
                .isPluginEnabled("PlaceholderAPI")) {

            getLogger().info(
                    "PlaceholderAPI no está disponible. "
                            + "Los placeholders externos permanecerán sin resolver."
            );

            return new NoOpExternalPlaceholderService();
        }

        try {
            final ExternalPlaceholderService service =
                    new PlaceholderApiExternalPlaceholderService(getLogger());

            getLogger().info(
                    "Integración con PlaceholderAPI habilitada correctamente."
            );

            return service;
        } catch (final RuntimeException | LinkageError exception) {
            getLogger().log(
                    Level.SEVERE,
                    "No se pudo inicializar la integración con PlaceholderAPI.",
                    exception
            );

            getLogger().severe(
                    "============================================================"
            );
            getLogger().severe("                     THEOSFERA ALERT");
            getLogger().severe(
                    "============================================================"
            );
            getLogger().severe("Dependencia: PlaceholderAPI");
            getLogger().severe("Estado: INICIALIZACIÓN FALLIDA");
            getLogger().severe("Módulo afectado: External Placeholders");
            getLogger().severe(
                    "Acción: Los placeholders externos fueron desactivados."
            );
            getLogger().severe(
                    "============================================================"
            );

            return new NoOpExternalPlaceholderService();
        }
    }

    @Override
    public void onDisable() {
        if (keybindManager != null) {
            keybindManager.save();
        }

        getLogger().info("TheosferaCore apagado correctamente.");
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    public VariableService getVariableService() {
        return variableService;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public MenuService getMenuService() {
        return menuService;
    }

    public MenuChatInputService getMenuChatInputService() {
        return menuChatInputService;
    }
}