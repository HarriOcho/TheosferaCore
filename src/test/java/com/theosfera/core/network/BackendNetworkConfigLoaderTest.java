package com.theosfera.core.network;

import com.theosfera.protocol.message.payload.BackendType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BackendNetworkConfigLoaderTest {

    private JavaPlugin plugin;
    private FileConfiguration fileConfiguration;
    private BackendNetworkConfigLoader loader;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        fileConfiguration = mock(FileConfiguration.class);

        when(plugin.getConfig())
                .thenReturn(fileConfiguration);

        when(plugin.getLogger())
                .thenReturn(mock(Logger.class));

        loader = new BackendNetworkConfigLoader(plugin);
    }

    @Test
    void loadsDisabledConfiguration() {
        when(fileConfiguration.getBoolean(
                "network.enabled",
                false
        )).thenReturn(false);

        BackendNetworkConfig result =
                loader.load();

        assertFalse(result.enabled());
    }

    @Test
    void loadsLobbyConfigurationCaseInsensitively() {
        when(fileConfiguration.getBoolean(
                "network.enabled",
                false
        )).thenReturn(true);

        when(fileConfiguration.getString(
                "network.backend-name",
                ""
        )).thenReturn("lobby-1");

        when(fileConfiguration.getString(
                "network.backend-type",
                ""
        )).thenReturn("lobby");

        BackendNetworkConfig result =
                loader.load();

        assertTrue(result.enabled());
        assertEquals("lobby-1", result.backendName());

        assertEquals(
                BackendType.LOBBY,
                result.backendType()
        );

        assertFalse(result.isAuthenticationBackend());
        assertTrue(result.isPlayableBackend());
    }

    @Test
    void loadsSkyblockConfiguration() {
        when(fileConfiguration.getBoolean(
                "network.enabled",
                false
        )).thenReturn(true);

        when(fileConfiguration.getString(
                "network.backend-name",
                ""
        )).thenReturn("skyblock-1");

        when(fileConfiguration.getString(
                "network.backend-type",
                ""
        )).thenReturn("SKYBLOCK");

        BackendNetworkConfig result =
                loader.load();

        assertTrue(result.enabled());

        assertEquals(
                BackendType.SKYBLOCK,
                result.backendType()
        );

        assertFalse(result.isAuthenticationBackend());
        assertTrue(result.isPlayableBackend());
    }

    @Test
    void disablesUnknownBackendType() {
        when(fileConfiguration.getBoolean(
                "network.enabled",
                false
        )).thenReturn(true);

        when(fileConfiguration.getString(
                "network.backend-name",
                ""
        )).thenReturn("skywars-1");

        when(fileConfiguration.getString(
                "network.backend-type",
                ""
        )).thenReturn("SKYWARS");

        BackendNetworkConfig result =
                loader.load();

        assertFalse(result.enabled());
    }

    @Test
    void loadsAuthConfiguration() {
        when(fileConfiguration.getBoolean(
                "network.enabled",
                false
        )).thenReturn(true);

        when(fileConfiguration.getString(
                "network.backend-name",
                ""
        )).thenReturn("auth-1");

        when(fileConfiguration.getString(
                "network.backend-type",
                ""
        )).thenReturn("AUTH");

        BackendNetworkConfig result =
                loader.load();

        assertTrue(result.enabled());
        assertEquals("auth-1", result.backendName());

        assertEquals(
                BackendType.AUTH,
                result.backendType()
        );

        assertTrue(result.isAuthenticationBackend());
        assertFalse(result.isPlayableBackend());
    }

    @Test
    void disablesBlankBackendName() {
        when(fileConfiguration.getBoolean(
                "network.enabled",
                false
        )).thenReturn(true);

        when(fileConfiguration.getString(
                "network.backend-name",
                ""
        )).thenReturn("   ");

        when(fileConfiguration.getString(
                "network.backend-type",
                ""
        )).thenReturn("LOBBY");

        BackendNetworkConfig result =
                loader.load();

        assertFalse(result.enabled());
    }
}
