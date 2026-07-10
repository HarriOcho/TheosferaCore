package com.theosfera.core.keybind;

public class KeybindAction {

    private final KeybindActionType type;
    private final String value;

    public KeybindAction(KeybindActionType type, String value) {
        this.type = type;
        this.value = value;
    }

    public KeybindActionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}