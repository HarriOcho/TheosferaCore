package com.theosfera.core.menu;

import java.util.List;

public record MenuConfig(
        String id,
        String title,
        int size,
        List<MenuItemConfig> items
) {
}