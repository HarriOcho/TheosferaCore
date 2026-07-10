package com.theosfera.core.menu.action;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.util.Locale;
import java.util.Optional;

public final class EffectMenuActionHandler {

    private final ParticleActionParser particleActionParser;

    public EffectMenuActionHandler() {
        this.particleActionParser = new ParticleActionParser();
    }

    public void register(final MenuActionRegistry registry) {
        registry.register("sound", this::playSound);
        registry.register("particles", this::spawnParticles);
    }

    private void playSound(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        final String[] parts = action.value().split(";");

        if (parts.length == 0 || parts[0].isBlank()) {
            return;
        }

        final String soundName = parts[0]
                .trim()
                .toLowerCase(Locale.ROOT);

        final NamespacedKey soundKey = NamespacedKey.minecraft(soundName);
        final Sound sound = Registry.SOUNDS.get(soundKey);

        if (sound == null) {
            context.plugin().getLogger().warning(
                    "[Menu] Sonido inválido en acción sound: '"
                            + action.value()
                            + "'."
            );
            return;
        }

        try {
            final float volume = parts.length >= 2
                    ? Float.parseFloat(parts[1].trim())
                    : 1.0F;

            final float pitch = parts.length >= 3
                    ? Float.parseFloat(parts[2].trim())
                    : 1.0F;

            context.player().playSound(
                    context.player().getLocation(),
                    sound,
                    volume,
                    pitch
            );
        } catch (final NumberFormatException exception) {
            context.plugin().getLogger().warning(
                    "[Menu] Acción sound inválida: '"
                            + action.value()
                            + "'. Revisa volumen o pitch."
            );
        }
    }

    private void spawnParticles(
            final ParsedMenuAction action,
            final MenuActionContext context
    ) {
        final Optional<ParticleActionConfig> optionalConfig =
                particleActionParser.parse(action.value());

        if (optionalConfig.isEmpty()) {
            context.plugin().getLogger().warning(
                    "[Menu] Acción particles inválida: '"
                            + action.value()
                            + "'."
            );
            return;
        }

        final ParticleActionConfig config = optionalConfig.get();

        try {
            final Particle particle = Particle.valueOf(
                    config.particleName()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

            context.player().spawnParticle(
                    particle,
                    context.player().getLocation(),
                    config.count(),
                    config.offsetX(),
                    config.offsetY(),
                    config.offsetZ(),
                    config.extra()
            );
        } catch (final IllegalArgumentException exception) {
            context.plugin().getLogger().warning(
                    "[Menu] Partícula inválida en acción particles: '"
                            + action.value()
                            + "'."
            );
        }
    }
}