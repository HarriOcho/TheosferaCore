package com.theosfera.core.network;

import com.theosfera.protocol.message.payload.BackendType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Objects;

public final class BackendNetworkConfigLoader {

    private final JavaPlugin plugin;

    public BackendNetworkConfigLoader(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );
    }

    public BackendNetworkConfig load() {
        FileConfiguration config =
                plugin.getConfig();

        if (!config.getBoolean("network.enabled", false)) {
            return BackendNetworkConfig.disabled();
        }

        String backendName =
                config.getString(
                        "network.backend-name",
                        ""
                );

        String backendTypeValue =
                config.getString(
                        "network.backend-type",
                        ""
                );

        if (backendTypeValue == null
                || backendTypeValue.isBlank()) {
            return disableWithAlert(
                    "network.backend-type no está configurado."
            );
        }

        final BackendType backendType;

        try {
            backendType = BackendType.valueOf(
                    backendTypeValue
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            return disableWithAlert(
                    "network.backend-type debe ser AUTH, LOBBY o SKYBLOCK."
            );
        }

        try {
            return new BackendNetworkConfig(
                    true,
                    backendName == null ? "" : backendName,
                    backendType
            );
        } catch (IllegalArgumentException exception) {
            return disableWithAlert(
                    exception.getMessage()
            );
        }
    }

    private BackendNetworkConfig disableWithAlert(
            String reason
    ) {
        plugin.getLogger().severe(
                "============================================================"
        );
        plugin.getLogger().severe(
                "                     THEOSFERA ALERT"
        );
        plugin.getLogger().severe(
                "============================================================"
        );
        plugin.getLogger().severe(
                "Integración: Core–Proxy Network"
        );
        plugin.getLogger().severe(
                "Estado: CONFIGURACIÓN INVÁLIDA"
        );
        plugin.getLogger().severe(
                "Motivo: " + reason
        );
        plugin.getLogger().severe(
                "Acción: La integración de red fue desactivada."
        );
        plugin.getLogger().severe(
                "============================================================"
        );

        return BackendNetworkConfig.disabled();
    }
}
