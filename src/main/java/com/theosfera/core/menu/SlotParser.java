package com.theosfera.core.menu;

import java.util.ArrayList;
import java.util.List;

public final class SlotParser {

    private SlotParser() {
    }

    public static List<Integer> parse(final List<String> rawSlots, final int inventorySize) {
        final List<Integer> slots = new ArrayList<>();

        for (final String rawSlot : rawSlots) {
            if (rawSlot == null || rawSlot.isBlank()) {
                continue;
            }

            final String value = rawSlot.trim();

            if (isRange(value)) {
                parseRange(value, inventorySize, slots);
                continue;
            }

            parseSingle(value, inventorySize, slots);
        }

        return slots;
    }

    public static List<Integer> parseSingleValue(final Object rawValue, final int inventorySize) {
        final List<String> rawSlots = new ArrayList<>();

        if (rawValue instanceof final List<?> list) {
            for (final Object value : list) {
                rawSlots.add(String.valueOf(value));
            }
        } else if (rawValue != null) {
            rawSlots.add(String.valueOf(rawValue));
        }

        return parse(rawSlots, inventorySize);
    }

    private static boolean isRange(final String value) {
        return value.matches("\\d+\\s*-\\s*\\d+");
    }

    private static void parseRange(
            final String value,
            final int inventorySize,
            final List<Integer> slots
    ) {
        final String[] parts = value.split("-", 2);

        if (parts.length != 2) {
            return;
        }

        try {
            final int start = Integer.parseInt(parts[0].trim());
            final int end = Integer.parseInt(parts[1].trim());

            final int min = Math.min(start, end);
            final int max = Math.max(start, end);

            for (int slot = min; slot <= max; slot++) {
                addValidSlot(slot, inventorySize, slots);
            }
        } catch (final NumberFormatException ignored) {
            // Invalid slot range.
        }
    }

    private static void parseSingle(
            final String value,
            final int inventorySize,
            final List<Integer> slots
    ) {
        try {
            addValidSlot(Integer.parseInt(value), inventorySize, slots);
        } catch (final NumberFormatException ignored) {
            // Invalid slot.
        }
    }

    private static void addValidSlot(
            final int slot,
            final int inventorySize,
            final List<Integer> slots
    ) {
        if (slot < 0 || slot >= inventorySize || slots.contains(slot)) {
            return;
        }

        slots.add(slot);
    }
}