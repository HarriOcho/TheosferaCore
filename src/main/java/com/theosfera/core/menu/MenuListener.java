package com.theosfera.core.menu;

import com.theosfera.core.menu.action.MenuActionContext;
import com.theosfera.core.menu.action.MenuActionExecutor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import com.theosfera.core.variable.VariableService;
import com.theosfera.core.menu.action.MenuActionCodec;
import org.bukkit.event.inventory.InventoryDragEvent;
import com.theosfera.core.ui.MessageService;
import com.theosfera.core.keybind.KeybindManager;
import com.theosfera.core.menu.input.MenuChatInputService;
import org.bukkit.event.inventory.ClickType;

public final class MenuListener implements Listener {

    private final Plugin plugin;
    private final MenuService menuService;
    private final MenuActionExecutor actionExecutor;
    private final VariableService variableService;
    private final MenuActionCodec actionCodec;
    private final MessageService messageService;
    private final KeybindManager keybindManager;
    private final MenuChatInputService inputService;

    public MenuListener(
            final Plugin plugin,
            final MenuService menuService,
            final VariableService variableService,
            final MessageService messageService,
            final KeybindManager keybindManager,
            final MenuChatInputService inputService
    ) {
        this.plugin = plugin;
        this.menuService = menuService;
        this.variableService = variableService;
        this.messageService = messageService;
        this.keybindManager = keybindManager;
        this.inputService = inputService;
        this.actionExecutor = new MenuActionExecutor();
        this.actionCodec = new MenuActionCodec();
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        final int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        final ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        final PersistentDataContainer data =
                clickedItem.getItemMeta().getPersistentDataContainer();

        final String rawActions = data.get(
                new NamespacedKey(plugin, MenuItemTag.ACTIONS),
                PersistentDataType.STRING
        );

        if (rawActions == null || rawActions.isBlank()) {
            return;
        }

        final ClickType clickType = event.getClick();

        final MenuActionContext context = new MenuActionContext(
                plugin,
                player,
                holder,
                data,
                menuService,
                variableService,
                messageService,
                keybindManager,
                inputService,
                clickType
        );

        for (final String action : actionCodec.decode(rawActions)) {
            actionExecutor.execute(action, context);
        }
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder)) {
            return;
        }

        for (final int slot : event.getRawSlots()) {
            if (slot >= 0 && slot < event.getInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }
}