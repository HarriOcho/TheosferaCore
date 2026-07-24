package com.theosfera.core.network;

import com.theosfera.core.network.auth.PendingPlayerAuthenticationRegistry;
import com.theosfera.core.network.auth.PlayerAuthenticationDisconnectListener;
import com.theosfera.core.network.auth.PlayerAuthenticationPublisher;
import com.theosfera.core.network.auth.PlayerAuthenticationRequest;
import com.theosfera.core.network.auth.PlayerAuthenticationRequestStatus;
import com.theosfera.core.network.auth.PlayerAuthenticationService;
import com.theosfera.core.network.transfer.PendingPlayerTransferRegistry;
import com.theosfera.core.network.transfer.PlayerTransferDisconnectListener;
import com.theosfera.core.network.transfer.PlayerTransferPublisher;
import com.theosfera.core.network.transfer.PlayerTransferRequest;
import com.theosfera.core.network.transfer.PlayerTransferRequestStatus;
import com.theosfera.core.network.transfer.PlayerTransferService;
import com.theosfera.protocol.codec.ProtocolJsonCodec;
import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.BackendHelloAckPayload;
import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.PlayerAuthenticatedAckPayload;
import com.theosfera.protocol.message.payload.PingPayload;
import com.theosfera.protocol.message.payload.TransferResultPayload;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;

import java.util.Objects;

public final class TheosferaNetworkModule
        implements AutoCloseable,
        PlayerAuthenticationPublisher,
        PlayerTransferPublisher {

    private final JavaPlugin plugin;
    private final BackendNetworkConfig config;
    private final BackendHandshakeService handshakeService;
    private final PlayerPresenceService presenceService;
    private final PlayerAuthenticationService
            authenticationService;
    private final PlayerTransferService transferService;
    private final BackendConnectionListener connectionListener;
    private final PlayerAuthenticationDisconnectListener
            authenticationDisconnectListener;
    private final PlayerTransferDisconnectListener
            transferDisconnectListener;
    private final ProtocolChannelRegistration channelRegistration;

    private boolean initialized;
    private boolean closed;
    private boolean authenticationPublisherRegistered;
    private boolean transferPublisherRegistered;

    public TheosferaNetworkModule(
            JavaPlugin plugin,
            BackendNetworkConfig config
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );

        this.config = Objects.requireNonNull(
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

        BackendHeartbeatService heartbeatService =
                new BackendHeartbeatService(
                        handshakeService,
                        messageSender
                );

        presenceService =
                new PlayerPresenceService(
                        config,
                        handshakeService,
                        messageSender
                );

        authenticationService =
                new PlayerAuthenticationService(
                        plugin,
                        config,
                        handshakeService,
                        messageSender,
                        new PendingPlayerAuthenticationRegistry()
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

        authenticationDisconnectListener =
                new PlayerAuthenticationDisconnectListener(
                        authenticationService
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
                ProtocolMessageType.PING,
                PingPayload.class,
                heartbeatService::handlePing
        );

        dispatcher.register(
                ProtocolMessageType.PLAYER_AUTHENTICATED_ACK,
                PlayerAuthenticatedAckPayload.class,
                authenticationService::handleAcknowledgement
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

        if (config.isAuthenticationBackend()) {
            plugin.getServer()
                    .getPluginManager()
                    .registerEvents(
                            authenticationDisconnectListener,
                            plugin
                    );
        }

        if (config.isPlayableBackend()
                || config.isAuthenticationBackend()) {
            plugin.getServer()
                    .getPluginManager()
                    .registerEvents(
                            transferDisconnectListener,
                            plugin
                    );
        }

        initialized = true;

        if (config.isAuthenticationBackend()) {
            plugin.getServer()
                    .getServicesManager()
                    .register(
                            PlayerAuthenticationPublisher.class,
                            this,
                            plugin,
                            ServicePriority.Normal
                    );

            authenticationPublisherRegistered = true;
        }

        plugin.getServer()
                .getServicesManager()
                .register(
                        PlayerTransferPublisher.class,
                        this,
                        plugin,
                        ServicePriority.Normal
                );

        transferPublisherRegistered = true;

        plugin.getLogger().info(
                "Integración Core–Proxy habilitada en "
                        + ProtocolChannel.NAME
                        + "."
        );

        plugin.getServer()
                .getOnlinePlayers()
                .stream()
                .findFirst()
                .ifPresent(handshakeService::begin);
    }

    @Override
    public PlayerAuthenticationRequest publishAuthenticatedPlayer(
            Player player,
            long authenticatedAt
    ) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        if (!config.isAuthenticationBackend()) {
            return PlayerAuthenticationRequest.rejected(
                    PlayerAuthenticationRequestStatus
                            .NOT_AUTHENTICATION_BACKEND
            );
        }

        if (!initialized
                || closed
                || !handshakeService.isAuthorized()) {
            return PlayerAuthenticationRequest.rejected(
                    PlayerAuthenticationRequestStatus
                            .TRANSPORT_UNAVAILABLE
            );
        }

        return authenticationService.requestAuthentication(
                player,
                authenticatedAt
        );
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

        boolean transferAllowed =
                config.allowsTransferTo(
                        targetBackendType
                );

        if (!transferAllowed
                || !initialized
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

    public boolean isAuthenticationBackend() {
        return config.isAuthenticationBackend();
    }

    public int pendingAuthenticationCount() {
        return authenticationService.pendingCount();
    }

    public int pendingTransferCount() {
        return transferService.pendingCount();
    }

    private void handleHandshakeAck(
            Player carrier,
            ProtocolEnvelope<BackendHelloAckPayload> envelope
    ) {
        if (!handshakeService.handleAck(
                carrier,
                envelope
        )) {
            return;
        }

        if (!config.isPlayableBackend()) {
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

        if (authenticationPublisherRegistered) {
            plugin.getServer()
                    .getServicesManager()
                    .unregister(
                            PlayerAuthenticationPublisher.class,
                            this
                    );

            authenticationPublisherRegistered = false;
        }

        if (transferPublisherRegistered) {
            plugin.getServer()
                    .getServicesManager()
                    .unregister(
                            PlayerTransferPublisher.class,
                            this
                    );

            transferPublisherRegistered = false;
        }

        HandlerList.unregisterAll(connectionListener);

        HandlerList.unregisterAll(
                authenticationDisconnectListener
        );

        HandlerList.unregisterAll(
                transferDisconnectListener
        );

        authenticationService.close();
        transferService.close();
        handshakeService.close();
        channelRegistration.close();

        initialized = false;

        plugin.getLogger().info(
                "Integración Core–Proxy desactivada correctamente."
        );
    }
}
