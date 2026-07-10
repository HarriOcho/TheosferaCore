package com.theosfera.core.menu.action;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class MenuActionRegistry {

    private final Map<String, MenuActionHandler> handlers =
            new HashMap<>();

    public void register(
            final String type,
            final MenuActionHandler handler
    ) {
        if (type == null || type.isBlank() || handler == null) {
            return;
        }

        handlers.put(
                normalizeType(type),
                handler
        );
    }

    public Optional<MenuActionHandler> get(final String type) {
        if (type == null || type.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                handlers.get(normalizeType(type))
        );
    }

    private String normalizeType(final String type) {
        return type.trim().toLowerCase(Locale.ROOT);
    }
}