package com.theosfera.core.menu.action;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class MenuActionCodec {

    private static final String SEPARATOR = ".";

    public String encode(final List<String> actions) {
        if (actions == null || actions.isEmpty()) {
            return "";
        }

        final Base64.Encoder encoder = Base64.getUrlEncoder()
                .withoutPadding();

        return actions.stream()
                .map(action -> action == null ? "" : action)
                .map(action -> encoder.encodeToString(
                        action.getBytes(StandardCharsets.UTF_8)
                ))
                .reduce(
                        (first, second) -> first + SEPARATOR + second
                )
                .orElse("");
    }

    public List<String> decode(final String encodedActions) {
        if (encodedActions == null || encodedActions.isBlank()) {
            return List.of();
        }

        final Base64.Decoder decoder = Base64.getUrlDecoder();
        final List<String> actions = new ArrayList<>();

        for (final String encodedAction : encodedActions.split("\\.", -1)) {
            if (encodedAction.isEmpty()) {
                actions.add("");
                continue;
            }

            try {
                actions.add(
                        new String(
                                decoder.decode(encodedAction),
                                StandardCharsets.UTF_8
                        )
                );
            } catch (final IllegalArgumentException exception) {
                // Invalid encoded action.
            }
        }

        return List.copyOf(actions);
    }
}