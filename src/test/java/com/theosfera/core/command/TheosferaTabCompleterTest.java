package com.theosfera.core.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TheosferaTabCompleterTest {

    private TheosferaTabCompleter completer;
    private CommandSender sender;
    private Command command;

    @BeforeEach
    void setUp() {
        completer = new TheosferaTabCompleter();
        sender = mock(CommandSender.class);
        command = mock(Command.class);
    }

    @Test
    void hidesSuggestionsWithoutAdminPermission() {
        when(sender.hasPermission("theosfera.admin"))
                .thenReturn(false);

        assertTrue(
                completer.onTabComplete(
                        sender,
                        command,
                        "theosfera",
                        new String[]{""}
                ).isEmpty()
        );
    }

    @Test
    void suggestsTransferAtRoot() {
        when(sender.hasPermission("theosfera.admin"))
                .thenReturn(true);

        assertEquals(
                List.of("transfer"),
                completer.onTabComplete(
                        sender,
                        command,
                        "theosfera",
                        new String[]{"tr"}
                )
        );
    }

    @Test
    void suggestsTransferTargetsContextually() {
        when(sender.hasPermission("theosfera.admin"))
                .thenReturn(true);

        assertEquals(
                List.of(
                        "lobby",
                        "skyblock"
                ),
                completer.onTabComplete(
                        sender,
                        command,
                        "theosfera",
                        new String[]{
                                "transfer",
                                ""
                        }
                )
        );
    }

    @Test
    void filtersTransferTarget() {
        when(sender.hasPermission("theosfera.admin"))
                .thenReturn(true);

        assertEquals(
                List.of("skyblock"),
                completer.onTabComplete(
                        sender,
                        command,
                        "theosfera",
                        new String[]{
                                "transfer",
                                "sky"
                        }
                )
        );
    }

    @Test
    void doesNotSuggestExtraArguments() {
        when(sender.hasPermission("theosfera.admin"))
                .thenReturn(true);

        assertTrue(
                completer.onTabComplete(
                        sender,
                        command,
                        "theosfera",
                        new String[]{
                                "transfer",
                                "skyblock",
                                ""
                        }
                ).isEmpty()
        );
    }
}
