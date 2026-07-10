package com.theosfera.core.menu.action;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import java.time.Duration;

public final class MessageMenuActionHandler {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    public void register(final MenuActionRegistry registry) {
        registry.register("message", this::sendMessage);
        registry.register("centered_message", this::sendCenteredMessage);
        registry.register("actionbar", this::sendActionBar);
        registry.register("title", this::sendTitle);
    }

    private void sendMessage(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        if (action.value().isBlank()) {
            return;
        }

        context.player().sendMessage(
                LEGACY_SERIALIZER.deserialize(
                        resolvePlayerVariables(action.value(), context)
                )
        );
    }

    private void sendCenteredMessage(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        if (action.value().isBlank()) {
            return;
        }

        context.messageService().sendCentered(
                context.player(),
                resolvePlayerVariables(action.value(), context)
        );
    }

    private void sendActionBar(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        if (action.value().isBlank()) {
            return;
        }

        context.player().sendActionBar(
                LEGACY_SERIALIZER.deserialize(
                        resolvePlayerVariables(action.value(), context)
                )
        );
    }

    private void sendTitle(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        if (action.value().isBlank()) {
            return;
        }

        final String[] parts = action.value().split(";", -1);

        final String rawTitle = parts.length >= 1
                ? resolvePlayerVariables(parts[0], context)
                : "";

        final String rawSubtitle = parts.length >= 2
                ? resolvePlayerVariables(parts[1], context)
                : "";

        final int fadeIn = parseInteger(parts, 2, 10);
        final int stay = parseInteger(parts, 3, 40);
        final int fadeOut = parseInteger(parts, 4, 10);

        context.player().showTitle(
                Title.title(
                        LEGACY_SERIALIZER.deserialize(rawTitle),
                        LEGACY_SERIALIZER.deserialize(rawSubtitle),
                        Title.Times.times(
                                Duration.ofMillis(fadeIn * 50L),
                                Duration.ofMillis(stay * 50L),
                                Duration.ofMillis(fadeOut * 50L)
                        )
                )
        );
    }

    private String resolvePlayerVariables(
            final String value,
            final MenuActionContext context
    ) {
        return context.variableService().applyPlayerVariables(
                value,
                context.player()
        );
    }

    private int parseInteger(
            final String[] parts,
            final int index,
            final int defaultValue
    ) {
        if (index >= parts.length) {
            return defaultValue;
        }

        try {
            return Math.max(
                    0,
                    Integer.parseInt(parts[index].trim())
            );
        } catch (final NumberFormatException exception) {
            return defaultValue;
        }
    }
}