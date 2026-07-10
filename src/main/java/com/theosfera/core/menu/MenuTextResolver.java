package com.theosfera.core.menu;

import java.util.List;
import java.util.Map;

public final class MenuTextResolver {

    public String resolve(
            final String text,
            final Map<String, String> placeholders
    ) {
        if (text == null) {
            return "";
        }

        String resolved = text;

        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolved = resolved.replace(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        return resolved;
    }

    public List<String> resolve(
            final List<String> lines,
            final Map<String, String> placeholders
    ) {
        return lines.stream()
                .map(line -> resolve(line, placeholders))
                .toList();
    }
}