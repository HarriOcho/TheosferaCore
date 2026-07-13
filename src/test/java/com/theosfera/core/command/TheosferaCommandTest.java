package com.theosfera.core.command;

import com.theosfera.core.keybind.KeybindManager;
import com.theosfera.core.menu.MenuManager;
import com.theosfera.core.ui.MessageService;
import com.theosfera.core.variable.VariableService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TheosferaCommandTest {

    private MessageService messages;
    private KeybindManager keybindManager;
    private MenuManager menuManager;
    private Runnable networkReloader;
    private CommandSender sender;
    private Command command;
    private TheosferaCommand theosferaCommand;

    @BeforeEach
    void setUp() {
        messages = mock(MessageService.class);
        keybindManager = mock(KeybindManager.class);
        menuManager = mock(MenuManager.class);
        networkReloader = mock(Runnable.class);
        sender = mock(CommandSender.class);
        command = mock(Command.class);

        theosferaCommand = new TheosferaCommand(
                messages,
                keybindManager,
                mock(VariableService.class),
                menuManager,
                mock(NetworkTransferCommandHandler.class),
                networkReloader
        );
    }

    @Test
    void reloadsNetworkAfterExistingServices() {
        when(sender.hasPermission("theosfera.admin"))
                .thenReturn(true);

        assertTrue(
                theosferaCommand.onCommand(
                        sender,
                        command,
                        "theosfera",
                        new String[]{"reload"}
                )
        );

        InOrder reloadOrder = inOrder(
                messages,
                keybindManager,
                menuManager,
                networkReloader
        );

        reloadOrder.verify(messages).load();
        reloadOrder.verify(keybindManager).load();
        reloadOrder.verify(menuManager).reload();
        reloadOrder.verify(networkReloader).run();

        verify(messages).sendSuccessKey(
                sender,
                "general.reloaded"
        );
    }

    @Test
    void doesNotReloadNetworkWithoutPermission() {
        when(sender.hasPermission("theosfera.admin"))
                .thenReturn(false);

        assertTrue(
                theosferaCommand.onCommand(
                        sender,
                        command,
                        "theosfera",
                        new String[]{"reload"}
                )
        );

        verify(networkReloader, never()).run();
        verify(messages, never()).load();
        verify(keybindManager, never()).load();
        verify(menuManager, never()).reload();

        verify(messages).sendErrorKey(
                sender,
                "general.no-permission"
        );
    }
}
