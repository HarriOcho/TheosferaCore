package com.theosfera.core.menu.action;

import com.theosfera.core.menu.MenuHolder;
import com.theosfera.core.menu.MenuService;
import com.theosfera.core.variable.VariableService;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import com.theosfera.core.ui.MessageService;
import com.theosfera.core.keybind.KeybindManager;
import com.theosfera.core.menu.input.MenuChatInputService;
import org.bukkit.event.inventory.ClickType;


public record MenuActionContext(
        Plugin plugin,
        Player player,
        MenuHolder holder,
        PersistentDataContainer data,
        MenuService menuService,
        VariableService variableService,
        MessageService messageService,
        KeybindManager keybindManager,
        MenuChatInputService inputService,
        ClickType clickType
) {
}