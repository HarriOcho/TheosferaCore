package com.theosfera.core.network.transfer;

import com.theosfera.core.network.ProtocolMessageSender;
import com.theosfera.protocol.ProtocolVersion;
import com.theosfera.protocol.message.ProtocolEnvelope;
import com.theosfera.protocol.message.ProtocolMessageType;
import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.TransferResultPayload;
import com.theosfera.protocol.message.payload.TransferResultStatus;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.logging.Logger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerTransferServiceTest {

    private static final UUID PLAYER_ID =
            UUID.fromString(
                    "417e98b4-74a1-467e-b453-a15be3af8996"
            );

    private JavaPlugin plugin;
    private ProtocolMessageSender messageSender;
    private PendingPlayerTransferRegistry registry;
    private PlayerTransferService service;
    private Player player;
    private BukkitScheduler scheduler;
    private BukkitTask timeoutTask;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);

        when(plugin.getLogger())
                .thenReturn(mock(Logger.class));

        messageSender = mock(ProtocolMessageSender.class);
        registry = new PendingPlayerTransferRegistry();
        player = mock(Player.class);
        scheduler = mock(BukkitScheduler.class);
        timeoutTask = mock(BukkitTask.class);

        Server server = mock(Server.class);

        when(plugin.getServer()).thenReturn(server);
        when(server.isPrimaryThread()).thenReturn(true);
        when(server.getScheduler()).thenReturn(scheduler);

        when(scheduler.runTaskLater(
                eq(plugin),
                any(Runnable.class),
                eq(240L)
        )).thenReturn(timeoutTask);

        when(player.getUniqueId()).thenReturn(PLAYER_ID);
        when(player.isOnline()).thenReturn(true);

        when(messageSender.send(
                eq(player),
                any()
        )).thenReturn(true);

        service = new PlayerTransferService(
                plugin,
                messageSender,
                registry
        );
    }

    @Test
    void submitsAndCompletesCorrelatedTransfer() {
        PlayerTransferRequest request =
                service.requestTransfer(
                        player,
                        BackendType.SKYBLOCK
                );

        assertEquals(
                PlayerTransferRequestStatus.SUBMITTED,
                request.status()
        );

        assertEquals(1, service.pendingCount());

        TransferResultPayload payload =
                new TransferResultPayload(
                        PLAYER_ID,
                        TransferResultStatus.SUCCESS,
                        "Transfer completed"
                );

        service.handleResult(
                player,
                resultEnvelope(
                        request.optionalRequestId()
                                .orElseThrow(),
                        payload
                )
        );

        assertEquals(
                payload,
                request.optionalCompletion()
                        .orElseThrow()
                        .join()
        );

        assertEquals(0, service.pendingCount());
        verify(timeoutTask).cancel();
    }

    @Test
    void rejectsSecondTransferForSamePlayer() {
        PlayerTransferRequest first =
                service.requestTransfer(
                        player,
                        BackendType.SKYBLOCK
                );

        PlayerTransferRequest second =
                service.requestTransfer(
                        player,
                        BackendType.LOBBY
                );

        assertEquals(
                PlayerTransferRequestStatus.SUBMITTED,
                first.status()
        );

        assertEquals(
                PlayerTransferRequestStatus.ALREADY_PENDING,
                second.status()
        );

        assertEquals(1, service.pendingCount());
    }

    @Test
    void rejectsResultCarriedByDifferentPlayer() {
        PlayerTransferRequest request =
                service.requestTransfer(
                        player,
                        BackendType.SKYBLOCK
                );

        Player differentCarrier =
                mock(Player.class);

        when(differentCarrier.getUniqueId())
                .thenReturn(UUID.randomUUID());

        TransferResultPayload payload =
                new TransferResultPayload(
                        PLAYER_ID,
                        TransferResultStatus.SUCCESS,
                        "Transfer completed"
                );

        service.handleResult(
                differentCarrier,
                resultEnvelope(
                        request.optionalRequestId()
                                .orElseThrow(),
                        payload
                )
        );

        assertFalse(
                request.optionalCompletion()
                        .orElseThrow()
                        .isDone()
        );

        assertEquals(1, service.pendingCount());
    }

    @Test
    void ignoresUnknownRequestId() {
        PlayerTransferRequest request =
                service.requestTransfer(
                        player,
                        BackendType.SKYBLOCK
                );

        TransferResultPayload payload =
                new TransferResultPayload(
                        PLAYER_ID,
                        TransferResultStatus.SUCCESS,
                        "Transfer completed"
                );

        service.handleResult(
                player,
                resultEnvelope(
                        UUID.randomUUID(),
                        payload
                )
        );

        assertFalse(
                request.optionalCompletion()
                        .orElseThrow()
                        .isDone()
        );

        assertEquals(1, service.pendingCount());
    }

    @Test
    void expiresPendingTransfer() {
        ArgumentCaptor<Runnable> timeoutCaptor =
                ArgumentCaptor.forClass(Runnable.class);

        PlayerTransferRequest request =
                service.requestTransfer(
                        player,
                        BackendType.SKYBLOCK
                );

        verify(scheduler).runTaskLater(
                eq(plugin),
                timeoutCaptor.capture(),
                eq(240L)
        );

        timeoutCaptor.getValue().run();

        TransferResultPayload result =
                request.optionalCompletion()
                        .orElseThrow()
                        .join();

        assertEquals(
                TransferResultStatus.TIMED_OUT,
                result.status()
        );

        assertEquals(0, service.pendingCount());
    }

    @Test
    void removesPendingStateWhenTransportFails() {
        when(messageSender.send(
                eq(player),
                any()
        )).thenReturn(false);

        PlayerTransferRequest request =
                service.requestTransfer(
                        player,
                        BackendType.SKYBLOCK
                );

        assertEquals(
                PlayerTransferRequestStatus.TRANSPORT_UNAVAILABLE,
                request.status()
        );

        assertEquals(0, service.pendingCount());
        verify(timeoutTask).cancel();
    }

    @Test
    void closesAndCompletesPendingTransfers() {
        PlayerTransferRequest request =
                service.requestTransfer(
                        player,
                        BackendType.SKYBLOCK
                );

        service.close();

        TransferResultPayload result =
                request.optionalCompletion()
                        .orElseThrow()
                        .join();

        assertEquals(
                TransferResultStatus.FAILED,
                result.status()
        );

        assertEquals(0, service.pendingCount());
        verify(timeoutTask).cancel();

        assertEquals(
                PlayerTransferRequestStatus.SERVICE_CLOSED,
                service.requestTransfer(
                        player,
                        BackendType.LOBBY
                ).status()
        );
    }

    @Test
    void rejectsRequestsOutsidePrimaryThread() {
        when(plugin.getServer().isPrimaryThread())
                .thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestTransfer(
                        player,
                        BackendType.SKYBLOCK
                )
        );
    }

    @Test
    void rejectsOfflinePlayer() {
        when(player.isOnline()).thenReturn(false);

        PlayerTransferRequest request =
                service.requestTransfer(
                        player,
                        BackendType.SKYBLOCK
                );

        assertEquals(
                PlayerTransferRequestStatus.TRANSPORT_UNAVAILABLE,
                request.status()
        );

        assertEquals(0, service.pendingCount());
    }

    private ProtocolEnvelope<TransferResultPayload>
    resultEnvelope(
            UUID requestId,
            TransferResultPayload payload
    ) {
        return new ProtocolEnvelope<>(
                ProtocolVersion.CURRENT,
                ProtocolMessageType.TRANSFER_RESULT,
                requestId,
                1_750_000_000_000L,
                payload
        );
    }
}
