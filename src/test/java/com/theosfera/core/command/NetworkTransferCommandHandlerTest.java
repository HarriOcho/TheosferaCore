package com.theosfera.core.command;

import com.theosfera.core.network.TheosferaNetworkModule;
import com.theosfera.core.network.transfer.PlayerTransferRequest;
import com.theosfera.core.network.transfer.PlayerTransferRequestStatus;
import com.theosfera.core.ui.MessageService;
import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.TransferResultPayload;
import com.theosfera.protocol.message.payload.TransferResultStatus;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NetworkTransferCommandHandlerTest {

    private MessageService messages;
    private TheosferaNetworkModule networkModule;
    private Player player;

    @BeforeEach
    void setUp() {
        messages = mock(MessageService.class);
        networkModule = mock(TheosferaNetworkModule.class);
        player = mock(Player.class);

        when(player.getUniqueId()).thenReturn(
                UUID.fromString(
                        "417e98b4-74a1-467e-b453-a15be3af8996"
                )
        );
    }

    @Test
    void rejectsConsoleSender() {
        CommandSender console =
                mock(CommandSender.class);

        NetworkTransferCommandHandler handler =
                handlerWithModule();

        handler.handle(
                console,
                new String[]{
                        "transfer",
                        "skyblock"
                }
        );

        verify(messages).sendErrorKey(
                console,
                "general.only-player"
        );

        verify(
                networkModule,
                never()
        ).requestTransfer(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void rejectsInvalidUsage() {
        NetworkTransferCommandHandler handler =
                handlerWithModule();

        handler.handle(
                player,
                new String[]{"transfer"}
        );

        verify(messages).sendErrorKey(
                player,
                "network.transfer-usage"
        );
    }

    @Test
    void rejectsAuthTarget() {
        NetworkTransferCommandHandler handler =
                handlerWithModule();

        handler.handle(
                player,
                new String[]{
                        "transfer",
                        "auth"
                }
        );

        verify(messages).sendErrorKey(
                player,
                "network.invalid-target"
        );

        verify(
                networkModule,
                never()
        ).requestTransfer(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void reportsUnavailableModule() {
        NetworkTransferCommandHandler handler =
                new NetworkTransferCommandHandler(
                        messages,
                        Optional::empty
                );

        handler.handle(
                player,
                new String[]{
                        "transfer",
                        "skyblock"
                }
        );

        verify(messages).sendErrorKey(
                player,
                "network.unavailable"
        );
    }

    @Test
    void submitsTransferAndReportsSuccessfulCompletion() {
        UUID requestId =
                UUID.fromString(
                        "11111111-2222-3333-4444-555555555555"
                );

        CompletableFuture<TransferResultPayload> completion =
                new CompletableFuture<>();

        when(networkModule.requestTransfer(
                player,
                BackendType.SKYBLOCK
        )).thenReturn(
                PlayerTransferRequest.submitted(
                        requestId,
                        completion
                )
        );

        when(player.isOnline()).thenReturn(true);

        NetworkTransferCommandHandler handler =
                handlerWithModule();

        handler.handle(
                player,
                new String[]{
                        "transfer",
                        "skyblock"
                }
        );

        verify(networkModule).requestTransfer(
                player,
                BackendType.SKYBLOCK
        );

        verify(messages).sendSuccessKey(
                player,
                "network.transfer-submitted",
                "%target%",
                "SKYBLOCK",
                "%request_id%",
                requestId.toString()
        );

        completion.complete(
                new TransferResultPayload(
                        player.getUniqueId(),
                        TransferResultStatus.SUCCESS,
                        "Transfer completed"
                )
        );

        verify(messages).sendSuccessKey(
                player,
                "network.transfer-success"
        );
    }

    @Test
    void reportsAlreadyPendingRequest() {
        when(networkModule.requestTransfer(
                player,
                BackendType.LOBBY
        )).thenReturn(
                PlayerTransferRequest.rejected(
                        PlayerTransferRequestStatus
                                .ALREADY_PENDING
                )
        );

        NetworkTransferCommandHandler handler =
                handlerWithModule();

        handler.handle(
                player,
                new String[]{
                        "transfer",
                        "lobby"
                }
        );

        verify(messages).sendErrorKey(
                player,
                "network.already-pending"
        );
    }

    private NetworkTransferCommandHandler
    handlerWithModule() {
        return new NetworkTransferCommandHandler(
                messages,
                () -> Optional.of(networkModule)
        );
    }
}
