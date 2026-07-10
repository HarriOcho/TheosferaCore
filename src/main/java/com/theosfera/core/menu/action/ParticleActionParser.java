package com.theosfera.core.menu.action;

import java.util.Optional;

public final class ParticleActionParser {

    private static final int DEFAULT_COUNT = 1;
    private static final double DEFAULT_OFFSET = 0.0D;
    private static final double DEFAULT_EXTRA = 0.0D;

    public Optional<ParticleActionConfig> parse(final String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }

        final String[] parts = rawValue.split(";", -1);
        final String particleName = parts[0].trim();

        if (particleName.isEmpty()) {
            return Optional.empty();
        }

        final int count = parseInteger(
                parts,
                1,
                DEFAULT_COUNT
        );

        final double offsetX = parseDouble(
                parts,
                2,
                DEFAULT_OFFSET
        );

        final double offsetY = parseDouble(
                parts,
                3,
                DEFAULT_OFFSET
        );

        final double offsetZ = parseDouble(
                parts,
                4,
                DEFAULT_OFFSET
        );

        final double extra = parseDouble(
                parts,
                5,
                DEFAULT_EXTRA
        );

        return Optional.of(
                new ParticleActionConfig(
                        particleName,
                        Math.max(0, count),
                        offsetX,
                        offsetY,
                        offsetZ,
                        extra
                )
        );
    }

    private int parseInteger(
            final String[] parts,
            final int index,
            final int defaultValue
    ) {
        if (index >= parts.length || parts[index].isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(parts[index].trim());
        } catch (final NumberFormatException exception) {
            return defaultValue;
        }
    }

    private double parseDouble(
            final String[] parts,
            final int index,
            final double defaultValue
    ) {
        if (index >= parts.length || parts[index].isBlank()) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(parts[index].trim());
        } catch (final NumberFormatException exception) {
            return defaultValue;
        }
    }
}