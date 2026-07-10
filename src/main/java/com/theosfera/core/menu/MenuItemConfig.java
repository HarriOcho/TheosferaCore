package com.theosfera.core.menu;

import java.util.List;

public record MenuItemConfig(
        String id,
        String material,
        String name,
        List<String> lore,
        List<Integer> slots,
        List<String> actions
) {
}