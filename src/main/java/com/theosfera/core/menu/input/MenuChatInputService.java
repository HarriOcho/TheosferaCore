package com.theosfera.core.menu.input;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class MenuChatInputService {

    private final Map<UUID, Consumer<String>> pendingInputs =
            new HashMap<>();

    public void requestInput(
            final Player player,
            final Consumer<String> consumer
    ) {
        if (player == null || consumer == null) {
            return;
        }

        pendingInputs.put(
                player.getUniqueId(),
                consumer
        );
    }

    public boolean hasPendingInput(final Player player) {
        if (player == null) {
            return false;
        }

        return pendingInputs.containsKey(player.getUniqueId());
    }

    public Optional<Consumer<String>> consumeInput(
            final Player player
    ) {
        if (player == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                pendingInputs.remove(player.getUniqueId())
        );
    }

    public void cancelInput(final Player player) {
        if (player == null) {
            return;
        }

        pendingInputs.remove(player.getUniqueId());
    }
}