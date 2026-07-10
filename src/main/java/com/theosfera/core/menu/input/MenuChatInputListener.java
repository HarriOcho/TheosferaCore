package com.theosfera.core.menu.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.function.Consumer;

public final class MenuChatInputListener implements Listener {

    private final Plugin plugin;
    private final MenuChatInputService inputService;

    public MenuChatInputListener(
            final Plugin plugin,
            final MenuChatInputService inputService
    ) {
        this.plugin = plugin;
        this.inputService = inputService;
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        if (!inputService.hasPendingInput(player)) {
            return;
        }

        event.setCancelled(true);

        final String input = event.getMessage();

        Bukkit.getScheduler().runTask(
                plugin,
                () -> {
                    final Optional<Consumer<String>> optionalConsumer =
                            inputService.consumeInput(player);

                    optionalConsumer.ifPresent(
                            consumer -> consumer.accept(input)
                    );
                }
        );
    }
}