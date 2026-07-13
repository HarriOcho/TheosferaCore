package com.theosfera.core.network;

import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.BackendHelloAckPayload;
import com.theosfera.protocol.message.payload.BackendHelloPayload;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public final class BackendHandshakeService
        implements AutoCloseable {

    private static final long HANDSHAKE_TIMEOUT_TICKS =
            20L * 10L;

    private final JavaPlugin plugin;
    private final BackendNetworkConfig config;
    private final ProtocolMessageSender messageSender;

    private BackendHandshakeStatus status =
            BackendHandshakeStatus.WAITING_FOR_CARRIER;

    private UUID pendingRequestId;
    private UUID pendingCarrierId;
    private BukkitTask timeoutTask;

    public BackendHandshakeService(
            JavaPlugin plugin,
            BackendNetworkConfig config,
            ProtocolMessageSender messageSender
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );
        this.config = Objects.requireNonNull(
                config,
                "config cannot be null"
        );
        this.messageSender = Objects.requireNonNull(
                messageSender,
                "messageSender cannot be null"
        );

        if (!config.enabled()) {
            throw new IllegalArgumentException(
                    "Networking must be enabled"
            );
        }
    }

    public boolean begin(Player carrier) {
        Objects.requireNonNull(
                carrier,
                "carrier cannot be null"
        );

        if (!plugin.getServer().isPrimaryThread()) {
            throw new IllegalStateException(
                    "Handshake must run on the primary thread"
            );
        }

        if (status == BackendHandshakeStatus.CLOSED
                || status == BackendHandshakeStatus.REJECTED) {
            return false;
        }

        if (status == BackendHandshakeStatus.AUTHORIZED) {
            return true;
        }

        if (status == BackendHandshakeStatus.HELLO_PENDING
                || !carrier.isOnline()) {
            return false;
        }

        ProtocolEnvelope<BackendHelloPayload> envelope =
                ProtocolEnvelope.create(
                        ProtocolMessageType.BACKEND_HELLO,
                        new BackendHelloPayload(
                                config.backendName(),
                                config.backendType()
                        )
                );

        pendingRequestId = envelope.requestId();
        pendingCarrierId = carrier.getUniqueId();
        status = BackendHandshakeStatus.HELLO_PENDING;

        if (!messageSender.send(carrier, envelope)) {
            clearPending();
            status = BackendHandshakeStatus
                    .WAITING_FOR_CARRIER;
            return false;
        }

        timeoutTask =
                plugin.getServer().getScheduler().runTaskLater(
                        plugin,
                        this::expire,
                        HANDSHAKE_TIMEOUT_TICKS
                );

        return true;
    }

    public boolean handleAck(
            Player carrier,
            ProtocolEnvelope<BackendHelloAckPayload> envelope
    ) {
        Objects.requireNonNull(
                carrier,
                "carrier cannot be null"
        );
        Objects.requireNonNull(
                envelope,
                "envelope cannot be null"
        );

        if (status != BackendHandshakeStatus.HELLO_PENDING) {
            plugin.getLogger().warning(
                    "Se ignoró BACKEND_HELLO_ACK sin handshake pendiente "
                            + "(requestId: "
                            + envelope.requestId()
                            + ")."
            );
            return false;
        }

        if (!envelope.requestId().equals(pendingRequestId)) {
            plugin.getLogger().warning(
                    "Se rechazó BACKEND_HELLO_ACK con requestId "
                            + "no correlacionado: "
                            + envelope.requestId()
                            + "."
            );
            return false;
        }

        if (!carrier.getUniqueId().equals(pendingCarrierId)) {
            plugin.getLogger().warning(
                    "Se rechazó BACKEND_HELLO_ACK recibido mediante "
                            + "un portador diferente (requestId: "
                            + envelope.requestId()
                            + ")."
            );
            return false;
        }

        cancelTimeout();
        clearPending();

        BackendHelloAckPayload payload =
                envelope.payload();

        if (!payload.accepted()) {
            status = BackendHandshakeStatus.REJECTED;

            plugin.getLogger().severe(
                    "El Proxy rechazó la identidad del backend "
                            + config.backendName()
                            + ": "
                            + payload.message()
            );
            return false;
        }

        status = BackendHandshakeStatus.AUTHORIZED;

        plugin.getLogger().info(
                "Backend autorizado por TheosferaProxy: "
                        + config.backendName()
                        + " ("
                        + config.backendType()
                        + ")."
        );

        return true;
    }

    public void handleCarrierDisconnect(UUID playerId) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        if (status != BackendHandshakeStatus.HELLO_PENDING
                || !playerId.equals(pendingCarrierId)) {
            return;
        }

        cancelTimeout();
        clearPending();
        status = BackendHandshakeStatus.WAITING_FOR_CARRIER;
    }

    public boolean isAuthorized() {
        return status == BackendHandshakeStatus.AUTHORIZED;
    }

    public BackendHandshakeStatus status() {
        return status;
    }

    private void expire() {
        timeoutTask = null;

        if (status != BackendHandshakeStatus.HELLO_PENDING) {
            return;
        }

        plugin.getLogger().warning(
                "El handshake con TheosferaProxy expiró para "
                        + config.backendName()
                        + ". Se intentará nuevamente cuando exista "
                        + "otro portador disponible."
        );

        clearPending();
        status = BackendHandshakeStatus.WAITING_FOR_CARRIER;
    }

    private void cancelTimeout() {
        if (timeoutTask == null) {
            return;
        }

        try {
            timeoutTask.cancel();
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "No se pudo cancelar el timeout del handshake.",
                    exception
            );
        } finally {
            timeoutTask = null;
        }
    }

    private void clearPending() {
        pendingRequestId = null;
        pendingCarrierId = null;
    }

    @Override
    public void close() {
        if (status == BackendHandshakeStatus.CLOSED) {
            return;
        }

        cancelTimeout();
        clearPending();
        status = BackendHandshakeStatus.CLOSED;
    }
}
