package com.theosfera.core.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

public final class MenuHolder implements InventoryHolder {

    private final String menuId;
    private final int page;
    private final int maxPage;
    private final OptionalInt keybindId;
    private Inventory inventory;

    public MenuHolder(
            final String menuId,
            final int page,
            final int maxPage
    ) {
        this(menuId, page, maxPage, OptionalInt.empty());
    }

    public MenuHolder(
            final String menuId,
            final int page,
            final OptionalInt keybindId
    ) {
        this(menuId, page, 1, keybindId);
    }

    public MenuHolder(
            final String menuId,
            final int page,
            final int maxPage,
            final OptionalInt keybindId
    ) {
        this.menuId = menuId;
        this.page = page;
        this.maxPage = Math.max(1, maxPage);
        this.keybindId = keybindId;
    }

    public String getMenuId() {
        return menuId;
    }

    public int getPage() {
        return page;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public OptionalInt getKeybindId() {
        return keybindId;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(final Inventory inventory) {
        this.inventory = inventory;
    }
}