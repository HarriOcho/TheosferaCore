package com.theosfera.core.network.auth;

import com.theosfera.core.network.BackendHandshakeService;
import com.theosfera.core.network.BackendNetworkConfig;
import com.theosfera.core.network.ProtocolMessageSender;
import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.PlayerAuthenticatedAckPayload;
import com.theosfera.protocol.message.payload.PlayerAuthenticatedPayload;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class PlayerAuthenticationService
        implements AutoCloseable {

    private static final long TIMEOUT_TICKS = 20L * 12L;

    private final JavaPlugin plugin;
    private final BackendNetworkConfig config;
    private final BackendHandshakeService handshakeService;
    private final ProtocolMessageSender messageSender;
    private final PendingPlayerAuthenticationRegistry registry;

    private final Map<UUID, BukkitTask> timeoutTasks =
            new HashMap<>();

    private boolean closed;

    public PlayerAuthenticationService(
            JavaPlugin plugin,
            BackendNetworkConfig config,
            BackendHandshakeService handshakeService,
            ProtocolMessageSender messageSender,
            PendingPlayerAuthenticationRegistry registry
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );

        this.config = Objects.requireNonNull(
                config,
                "config cannot be null"
        );

        this.handshakeService = Objects.requireNonNull(
                handshakeService,
                "handshakeService cannot be null"
        );

        this.messageSender = Objects.requireNonNull(
                messageSender,
                "messageSender cannot be null"
        );

        this.registry = Objects.requireNonNull(
                registry,
                "registry cannot be null"
        );
    }

    public PlayerAuthenticationRequest requestAuthentication(
            Player player,
            long authenticatedAt
    ) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        if (!plugin.getServer().isPrimaryThread()) {
            throw new IllegalStateException(
                    "Authentication publication must be requested "
                            + "from the primary thread"
            );
        }

        if (authenticatedAt <= 0L) {
            throw new IllegalArgumentException(
                    "authenticatedAt must be greater than zero"
            );
        }

        if (closed) {
            return PlayerAuthenticationRequest.rejected(
                    PlayerAuthenticationRequestStatus
                            .SERVICE_CLOSED
            );
        }

        if (!config.isAuthenticationBackend()) {
            return PlayerAuthenticationRequest.rejected(
                    PlayerAuthenticationRequestStatus
                            .NOT_AUTHENTICATION_BACKEND
            );
        }

        if (!handshakeService.isAuthorized()
                || !player.isOnline()) {
            return PlayerAuthenticationRequest.rejected(
                    PlayerAuthenticationRequestStatus
                            .TRANSPORT_UNAVAILABLE
            );
        }

        PlayerAuthenticatedPayload payload =
                new PlayerAuthenticatedPayload(
                        player.getUniqueId(),
                        player.getName(),
                        authenticatedAt
                );

        ProtocolEnvelope<PlayerAuthenticatedPayload> envelope =
                ProtocolEnvelope.create(
                        ProtocolMessageType.PLAYER_AUTHENTICATED,
                        payload
                );

        CompletableFuture<PlayerAuthenticatedAckPayload> completion =
                new CompletableFuture<>();

        PendingPlayerAuthentication pending =
                new PendingPlayerAuthentication(
                        envelope.requestId(),
                        player.getUniqueId(),
                        envelope.timestamp(),
                        completion
                );

        PlayerAuthenticationRegistrationResult registration =
                registry.register(pending);

        if (registration
                == PlayerAuthenticationRegistrationResult
                .PLAYER_ALREADY_PENDING) {
            return PlayerAuthenticationRequest.rejected(
                    PlayerAuthenticationRequestStatus
                            .ALREADY_PENDING
            );
        }

        if (registration
                == PlayerAuthenticationRegistrationResult
                .REQUEST_ID_CONFLICT) {
            plugin.getLogger().severe(
                    "Se detectó un requestId duplicado al "
                            + "registrar una autenticación: "
                            + envelope.requestId()
                            + "."
            );

            return PlayerAuthenticationRequest.rejected(
                    PlayerAuthenticationRequestStatus
                            .TRANSPORT_UNAVAILABLE
            );
        }

        BukkitTask timeoutTask =
                plugin.getServer()
                        .getScheduler()
                        .runTaskLater(
                                plugin,
                                () -> expire(
                                        envelope.requestId()
                                ),
                                TIMEOUT_TICKS
                        );

        timeoutTasks.put(
                envelope.requestId(),
                timeoutTask
        );

        if (!messageSender.send(player, envelope)) {
            removePending(envelope.requestId());

            return PlayerAuthenticationRequest.rejected(
                    PlayerAuthenticationRequestStatus
                            .TRANSPORT_UNAVAILABLE
            );
        }

        return PlayerAuthenticationRequest.submitted(
                envelope.requestId(),
                completion
        );
    }

    public void handleAcknowledgement(
            Player carrier,
            ProtocolEnvelope<PlayerAuthenticatedAckPayload> envelope
    ) {
        Objects.requireNonNull(
                carrier,
                "carrier cannot be null"
        );

        Objects.requireNonNull(
                envelope,
                "envelope cannot be null"
        );

        PlayerAuthenticatedAckPayload payload =
                envelope.payload();

        if (!carrier.getUniqueId().equals(
                payload.playerId()
        )) {
            plugin.getLogger().warning(
                    "Se rechazó PLAYER_AUTHENTICATED_ACK porque "
                            + "el portador "
                            + carrier.getUniqueId()
                            + " no coincide con el jugador "
                            + payload.playerId()
                            + " (requestId: "
                            + envelope.requestId()
                            + ")."
            );
            return;
        }

        Optional<PendingPlayerAuthentication> pendingOptional =
                registry.findByRequest(
                        envelope.requestId()
                );

        if (pendingOptional.isEmpty()) {
            plugin.getLogger().warning(
                    "Se ignoró PLAYER_AUTHENTICATED_ACK sin "
                            + "solicitud pendiente (requestId: "
                            + envelope.requestId()
                            + ", playerId: "
                            + payload.playerId()
                            + ")."
            );
            return;
        }

        PendingPlayerAuthentication pending =
                pendingOptional.get();

        if (!pending.playerId().equals(
                payload.playerId()
        )) {
            plugin.getLogger().warning(
                    "Se rechazó PLAYER_AUTHENTICATED_ACK por "
                            + "UUID inconsistente (requestId: "
                            + envelope.requestId()
                            + ")."
            );
            return;
        }

        Optional<PendingPlayerAuthentication> removed =
                removePending(
                        envelope.requestId()
                );

        if (removed.isEmpty()) {
            return;
        }

        removed.get()
                .completion()
                .complete(payload);
    }

    public void handleDisconnect(UUID playerId) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        Optional<PendingPlayerAuthentication> removed =
                registry.removeByPlayer(playerId);

        if (removed.isEmpty()) {
            return;
        }

        cancelTimeout(
                removed.get().requestId()
        );

        removed.get()
                .completion()
                .complete(
                        new PlayerAuthenticatedAckPayload(
                                playerId,
                                false,
                                "Player disconnected before "
                                        + "authentication acknowledgement"
                        )
                );
    }

    private void expire(UUID requestId) {
        timeoutTasks.remove(requestId);

        Optional<PendingPlayerAuthentication> removed =
                registry.remove(requestId);

        if (removed.isEmpty()) {
            return;
        }

        PendingPlayerAuthentication pending =
                removed.get();

        pending.completion().complete(
                new PlayerAuthenticatedAckPayload(
                        pending.playerId(),
                        false,
                        "Authentication acknowledgement timed out"
                )
        );
    }

    private Optional<PendingPlayerAuthentication> removePending(
            UUID requestId
    ) {
        Optional<PendingPlayerAuthentication> removed =
                registry.remove(requestId);

        cancelTimeout(requestId);

        return removed;
    }

    private void cancelTimeout(UUID requestId) {
        BukkitTask timeoutTask =
                timeoutTasks.remove(requestId);

        if (timeoutTask == null) {
            return;
        }

        try {
            timeoutTask.cancel();
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "No se pudo cancelar el timeout de la "
                            + "autenticación "
                            + requestId
                            + ".",
                    exception
            );
        }
    }

    public int pendingCount() {
        return registry.size();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        for (BukkitTask timeoutTask
                : timeoutTasks.values()) {
            try {
                timeoutTask.cancel();
            } catch (RuntimeException exception) {
                plugin.getLogger().log(
                        Level.WARNING,
                        "No se pudo cancelar una tarea de timeout "
                                + "de autenticación durante el "
                                + "apagado.",
                        exception
                );
            }
        }

        timeoutTasks.clear();

        for (PendingPlayerAuthentication pending
                : registry.drain()) {
            pending.completion().complete(
                    new PlayerAuthenticatedAckPayload(
                            pending.playerId(),
                            false,
                            "Authentication service closed"
                    )
            );
        }
    }
}
