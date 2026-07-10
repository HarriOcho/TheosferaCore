package com.theosfera.core.keybind;

import java.util.List;

public record KeybindEntry(
        int id,
        String name,
        String description,
        String key,
        List<KeybindAction> actions
) {

    public KeybindEntry {
        actions = List.copyOf(actions);
    }
}