package com.theosfera.core.keybind;

public final class KeybindValidator {

    private KeybindValidator() {
    }

    public static boolean isValidName(final String name) {
        return name != null && name.matches("[A-Za-z0-9_-]+");
    }

    public static boolean isValidDescription(final String description) {
        return description != null && !description.trim().isEmpty();
    }

    public static boolean isValidKey(final String key) {
        return key != null
                && (
                key.matches("[A-Z0-9]")
                        || key.matches("F([1-9]|1[0-2])")
        );
    }
}