package com.theosfera.core.network;

import com.theosfera.protocol.codec.ProtocolJsonCodec;
import com.theosfera.protocol.message.ProtocolEnvelope;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProtocolMessageSenderTest {

    private JavaPlugin plugin;
    private ProtocolJsonCodec codec;
    private Player player;
    private ProtocolEnvelope<?> envelope;
    private ProtocolMessageSender sender;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        codec = mock(ProtocolJsonCodec.class);
        player = mock(Player.class);
        envelope = mock(ProtocolEnvelope.class);

        sender = new ProtocolMessageSender(
                plugin,
                codec
        );
    }

    @Test
    void rejectsCarrierBeforeChannelRegistration() {
        when(player.isOnline()).thenReturn(true);
        when(player.getListeningPluginChannels())
                .thenReturn(Set.of());

        assertFalse(
                sender.send(player, envelope)
        );

        verify(codec, never()).encode(any());
        verify(player, never()).sendPluginMessage(
                any(),
                any(),
                any()
        );
    }

    @Test
    void sendsWhenCarrierListensToProtocolChannel() {
        byte[] encoded = new byte[]{1, 2, 3};

        when(player.isOnline()).thenReturn(true);
        when(player.getListeningPluginChannels())
                .thenReturn(
                        Set.of(ProtocolChannel.NAME)
                );
        when(codec.encode(envelope))
                .thenReturn(encoded);

        assertTrue(
                sender.send(player, envelope)
        );

        verify(player).sendPluginMessage(
                plugin,
                ProtocolChannel.NAME,
                encoded
        );
    }
}
