package com.theosfera.core.menu.action;

public record ParticleActionConfig(
        String particleName,
        int count,
        double offsetX,
        double offsetY,
        double offsetZ,
        double extra
) {
}