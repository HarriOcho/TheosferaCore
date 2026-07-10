package com.theosfera.core.menu.action;

import java.util.Locale;
import java.util.Optional;

public final class MenuActionParser {

    public Optional<ParsedMenuAction> parse(final String rawAction) {
        if (rawAction == null || rawAction.isBlank()) {
            return Optional.empty();
        }

        final String value = rawAction.trim();

        if (!value.startsWith("[")) {
            return Optional.empty();
        }

        final int closingBracket = value.indexOf(']');

        if (closingBracket <= 1) {
            return Optional.empty();
        }

        final String type = value.substring(1, closingBracket)
                .trim()
                .toLowerCase(Locale.ROOT);

        if (type.isEmpty()) {
            return Optional.empty();
        }

        final String actionValue = value.substring(closingBracket + 1)
                .trim();

        return Optional.of(
                new ParsedMenuAction(type, actionValue)
        );
    }
}