package com.theosfera.core.network;

import com.theosfera.core.network.transfer.PendingPlayerTransferRegistry;
import com.theosfera.core.network.transfer.PlayerTransferDisconnectListener;
import com.theosfera.core.network.transfer.PlayerTransferRequest;
import com.theosfera.core.network.transfer.PlayerTransferRequestStatus;
import com.theosfera.core.network.transfer.PlayerTransferService;
import com.theosfera.protocol.codec.ProtocolJsonCodec;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.BackendHelloAckPayload;
import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.TransferResultPayload;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TheosferaNetworkModule
        implements AutoCloseable {

    private final JavaPlugin plugin;
    private final BackendHandshakeService handshakeService;
    private final PlayerPresenceService presenceService;
    private final PlayerTransferService transferService;
    private final BackendConnectionListener connectionListener;
    private final PlayerTransferDisconnectListener
            transferDisconnectListener;
    private final ProtocolChannelRegistration channelRegistration;

    private boolean initialized;
    private boolean closed;

    public TheosferaNetworkModule(
            JavaPlugin plugin,
            BackendNetworkConfig config
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );
        Objects.requireNonNull(
                config,
                "config cannot be null"
        );

        if (!config.enabled()) {
            throw new IllegalArgumentException(
                    "Networking must be enabled"
            );
        }

        ProtocolJsonCodec codec =
                new ProtocolJsonCodec();

        ProtocolMessageSender messageSender =
                new ProtocolMessageSender(
                        plugin,
                        codec
                );

        ProtocolMessageDispatcher dispatcher =
                new ProtocolMessageDispatcher();

        handshakeService =
                new BackendHandshakeService(
                        plugin,
                        config,
                        messageSender
                );

        presenceService =
                new PlayerPresenceService(
                        config,
                        handshakeService,
                        messageSender
                );

        transferService =
                new PlayerTransferService(
                        plugin,
                        messageSender,
                        new PendingPlayerTransferRegistry()
                );

        connectionListener =
                new BackendConnectionListener(
                        plugin,
                        handshakeService,
                        presenceService
                );

        transferDisconnectListener =
                new PlayerTransferDisconnectListener(
                        transferService
                );

        ProtocolMessageListener messageListener =
                new ProtocolMessageListener(
                        plugin,
                        codec,
                        dispatcher
                );

        channelRegistration =
                new ProtocolChannelRegistration(
                        plugin,
                        messageListener
                );

        dispatcher.register(
                ProtocolMessageType.BACKEND_HELLO_ACK,
                BackendHelloAckPayload.class,
                this::handleHandshakeAck
        );

        dispatcher.register(
                ProtocolMessageType.TRANSFER_RESULT,
                TransferResultPayload.class,
                transferService::handleResult
        );
    }

    public void initialize() {
        if (closed) {
            throw new IllegalStateException(
                    "The network module is closed"
            );
        }

        if (initialized) {
            return;
        }

        channelRegistration.register();

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        connectionListener,
                        plugin
                );

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        transferDisconnectListener,
                        plugin
                );

        initialized = true;

        plugin.getLogger().info(
                "Integración Core–Proxy habilitada en "
                        + ProtocolChannel.NAME
                        + "."
        );

        plugin.getServer().getOnlinePlayers()
                .stream()
                .findFirst()
                .ifPresent(handshakeService::begin);
    }

    public PlayerTransferRequest requestTransfer(
            Player player,
            BackendType targetBackendType
    ) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );
        Objects.requireNonNull(
                targetBackendType,
                "targetBackendType cannot be null"
        );

        if (!initialized
                || closed
                || !handshakeService.isAuthorized()) {
            return PlayerTransferRequest.rejected(
                    PlayerTransferRequestStatus
                            .TRANSPORT_UNAVAILABLE
            );
        }

        return transferService.requestTransfer(
                player,
                targetBackendType
        );
    }

    public boolean isAuthorized() {
        return initialized
                && !closed
                && handshakeService.isAuthorized();
    }

    public int pendingTransferCount() {
        return transferService.pendingCount();
    }

    private void handleHandshakeAck(
            Player carrier,
            com.theosfera.protocol.message.ProtocolEnvelope<
                    BackendHelloAckPayload
                    > envelope
    ) {
        if (!handshakeService.handleAck(
                carrier,
                envelope
        )) {
            return;
        }

        for (Player player
                : plugin.getServer().getOnlinePlayers()) {
            presenceService.announceReady(player);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        HandlerList.unregisterAll(connectionListener);
        HandlerList.unregisterAll(transferDisconnectListener);

        transferService.close();
        handshakeService.close();
        channelRegistration.close();

        initialized = false;

        plugin.getLogger().info(
                "Integración Core–Proxy desactivada correctamente."
        );
    }
}
