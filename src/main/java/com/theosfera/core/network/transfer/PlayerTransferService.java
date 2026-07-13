package com.theosfera.core.network.transfer;

import com.theosfera.core.network.ProtocolMessageSender;
import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.TransferRequestPayload;
import com.theosfera.protocol.message.payload.TransferResultPayload;
import com.theosfera.protocol.message.payload.TransferResultStatus;
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

public final class PlayerTransferService
        implements AutoCloseable {

    private static final long TIMEOUT_TICKS = 20L * 12L;

    private final JavaPlugin plugin;
    private final ProtocolMessageSender messageSender;
    private final PendingPlayerTransferRegistry registry;
    private final Map<UUID, BukkitTask> timeoutTasks =
            new HashMap<>();

    private boolean closed;

    public PlayerTransferService(
            JavaPlugin plugin,
            ProtocolMessageSender messageSender,
            PendingPlayerTransferRegistry registry
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
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

        if (!plugin.getServer().isPrimaryThread()) {
            throw new IllegalStateException(
                    "Transfers must be requested from the primary thread"
            );
        }

        if (closed) {
            return PlayerTransferRequest.rejected(
                    PlayerTransferRequestStatus.SERVICE_CLOSED
            );
        }

        if (!player.isOnline()) {
            return PlayerTransferRequest.rejected(
                    PlayerTransferRequestStatus.TRANSPORT_UNAVAILABLE
            );
        }

        TransferRequestPayload payload =
                new TransferRequestPayload(
                        player.getUniqueId(),
                        targetBackendType
                );

        ProtocolEnvelope<TransferRequestPayload> envelope =
                ProtocolEnvelope.create(
                        ProtocolMessageType.TRANSFER_REQUEST,
                        payload
                );

        CompletableFuture<TransferResultPayload> completion =
                new CompletableFuture<>();

        PendingPlayerTransfer pending =
                new PendingPlayerTransfer(
                        envelope.requestId(),
                        player.getUniqueId(),
                        targetBackendType,
                        envelope.timestamp(),
                        completion
                );

        PlayerTransferRegistrationResult registration =
                registry.register(pending);

        if (registration
                == PlayerTransferRegistrationResult
                .PLAYER_ALREADY_PENDING) {
            return PlayerTransferRequest.rejected(
                    PlayerTransferRequestStatus.ALREADY_PENDING
            );
        }

        if (registration
                == PlayerTransferRegistrationResult
                .REQUEST_ID_CONFLICT) {
            plugin.getLogger().severe(
                    "Se detectó un requestId duplicado al registrar "
                            + "una transferencia: "
                            + envelope.requestId()
                            + "."
            );

            return PlayerTransferRequest.rejected(
                    PlayerTransferRequestStatus.TRANSPORT_UNAVAILABLE
            );
        }

        BukkitTask timeoutTask =
                plugin.getServer().getScheduler().runTaskLater(
                        plugin,
                        () -> expire(envelope.requestId()),
                        TIMEOUT_TICKS
                );

        timeoutTasks.put(
                envelope.requestId(),
                timeoutTask
        );

        if (!messageSender.send(player, envelope)) {
            removePending(envelope.requestId());

            return PlayerTransferRequest.rejected(
                    PlayerTransferRequestStatus.TRANSPORT_UNAVAILABLE
            );
        }

        return PlayerTransferRequest.submitted(
                envelope.requestId(),
                completion
        );
    }

    public void handleResult(
            Player carrier,
            ProtocolEnvelope<TransferResultPayload> envelope
    ) {
        Objects.requireNonNull(
                carrier,
                "carrier cannot be null"
        );
        Objects.requireNonNull(
                envelope,
                "envelope cannot be null"
        );

        TransferResultPayload payload =
                envelope.payload();

        if (!carrier.getUniqueId().equals(payload.playerId())) {
            plugin.getLogger().warning(
                    "Se rechazó TRANSFER_RESULT porque el portador "
                            + carrier.getUniqueId()
                            + " no coincide con el jugador "
                            + payload.playerId()
                            + " (requestId: "
                            + envelope.requestId()
                            + ")."
            );
            return;
        }

        Optional<PendingPlayerTransfer> pendingOptional =
                registry.findByRequest(envelope.requestId());

        if (pendingOptional.isEmpty()) {
            plugin.getLogger().warning(
                    "Se ignoró TRANSFER_RESULT sin solicitud pendiente "
                            + "(requestId: "
                            + envelope.requestId()
                            + ", playerId: "
                            + payload.playerId()
                            + ")."
            );
            return;
        }

        PendingPlayerTransfer pending =
                pendingOptional.get();

        if (!pending.playerId().equals(payload.playerId())) {
            plugin.getLogger().warning(
                    "Se rechazó TRANSFER_RESULT por UUID inconsistente "
                            + "(requestId: "
                            + envelope.requestId()
                            + ")."
            );
            return;
        }

        Optional<PendingPlayerTransfer> removed =
                removePending(envelope.requestId());

        if (removed.isEmpty()) {
            return;
        }

        removed.get().completion().complete(payload);
    }

    public void handleDisconnect(UUID playerId) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        Optional<PendingPlayerTransfer> removed =
                registry.removeByPlayer(playerId);

        if (removed.isEmpty()) {
            return;
        }

        cancelTimeout(removed.get().requestId());

        removed.get().completion().complete(
                new TransferResultPayload(
                        playerId,
                        TransferResultStatus.FAILED,
                        "Player disconnected before transfer completion"
                )
        );
    }

    private void expire(UUID requestId) {
        timeoutTasks.remove(requestId);

        Optional<PendingPlayerTransfer> removed =
                registry.remove(requestId);

        if (removed.isEmpty()) {
            return;
        }

        PendingPlayerTransfer pending =
                removed.get();

        pending.completion().complete(
                new TransferResultPayload(
                        pending.playerId(),
                        TransferResultStatus.TIMED_OUT,
                        "Transfer request timed out"
                )
        );
    }

    private Optional<PendingPlayerTransfer> removePending(
            UUID requestId
    ) {
        Optional<PendingPlayerTransfer> removed =
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
                    "No se pudo cancelar el timeout de la solicitud "
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
                                + "durante el apagado.",
                        exception
                );
            }
        }

        timeoutTasks.clear();

        for (PendingPlayerTransfer pending
                : registry.drain()) {
            pending.completion().complete(
                    new TransferResultPayload(
                            pending.playerId(),
                            TransferResultStatus.FAILED,
                            "Transfer service closed"
                    )
            );
        }
    }
}
