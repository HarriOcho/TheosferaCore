package com.theosfera.core.keybind;

public enum KeybindActionType {

    PLAYER_COMMAND("player_command"),
    CONSOLE_COMMAND("console_command"),
    MESSAGE("message");

    private final String yamlName;

    KeybindActionType(String yamlName) {
        this.yamlName = yamlName;
    }

    public String yamlName() {
        return yamlName;
    }

    public static KeybindActionType fromName(String name) {
        for (KeybindActionType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.yamlName.equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}