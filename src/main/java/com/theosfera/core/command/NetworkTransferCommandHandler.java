package com.theosfera.core.command;

import com.theosfera.core.network.TheosferaNetworkModule;
import com.theosfera.core.network.transfer.PlayerTransferRequest;
import com.theosfera.core.network.transfer.PlayerTransferRequestStatus;
import com.theosfera.core.ui.MessageService;
import com.theosfera.protocol.message.payload.BackendType;
import com.theosfera.protocol.message.payload.TransferResultPayload;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class NetworkTransferCommandHandler {

    private final MessageService messages;
    private final Supplier<Optional<TheosferaNetworkModule>>
            networkModuleSupplier;

    public NetworkTransferCommandHandler(
            MessageService messages,
            Supplier<Optional<TheosferaNetworkModule>>
                    networkModuleSupplier
    ) {
        this.messages = Objects.requireNonNull(
                messages,
                "messages cannot be null"
        );

        this.networkModuleSupplier =
                Objects.requireNonNull(
                        networkModuleSupplier,
                        "networkModuleSupplier cannot be null"
                );
    }

    public void handle(
            CommandSender sender,
            String[] args
    ) {
        Objects.requireNonNull(
                sender,
                "sender cannot be null"
        );
        Objects.requireNonNull(
                args,
                "args cannot be null"
        );

        if (!(sender instanceof Player player)) {
            messages.sendErrorKey(
                    sender,
                    "general.only-player"
            );
            return;
        }

        if (args.length != 2) {
            messages.sendErrorKey(
                    sender,
                    "network.transfer-usage"
            );
            return;
        }

        Optional<BackendType> target =
                parseTarget(args[1]);

        if (target.isEmpty()) {
            messages.sendErrorKey(
                    sender,
                    "network.invalid-target"
            );
            return;
        }

        Optional<TheosferaNetworkModule> networkModule =
                networkModuleSupplier.get();

        if (networkModule.isEmpty()) {
            messages.sendErrorKey(
                    sender,
                    "network.unavailable"
            );
            return;
        }

        PlayerTransferRequest request =
                networkModule.orElseThrow()
                        .requestTransfer(
                                player,
                                target.orElseThrow()
                        );

        handleSubmission(
                player,
                target.orElseThrow(),
                request
        );
    }

    private Optional<BackendType> parseTarget(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        final BackendType backendType;

        try {
            backendType = BackendType.valueOf(
                    value.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        if (backendType == BackendType.AUTH) {
            return Optional.empty();
        }

        return Optional.of(backendType);
    }

    private void handleSubmission(
            Player player,
            BackendType target,
            PlayerTransferRequest request
    ) {
        PlayerTransferRequestStatus status =
                request.status();

        switch (status) {
            case SUBMITTED -> {
                messages.sendSuccessKey(
                        player,
                        "network.transfer-submitted",
                        "%target%", target.name(),
                        "%request_id%",
                        request.optionalRequestId()
                                .orElseThrow()
                                .toString()
                );

                request.optionalCompletion()
                        .orElseThrow()
                        .thenAccept(result ->
                                handleCompletion(
                                        player,
                                        result
                                )
                        );
            }
            case ALREADY_PENDING ->
                    messages.sendErrorKey(
                            player,
                            "network.already-pending"
                    );
            case TRANSPORT_UNAVAILABLE ->
                    messages.sendErrorKey(
                            player,
                            "network.transport-unavailable"
                    );
            case SERVICE_CLOSED ->
                    messages.sendErrorKey(
                            player,
                            "network.service-closed"
                    );
        }
    }

    private void handleCompletion(
            Player player,
            TransferResultPayload result
    ) {
        if (!player.isOnline()) {
            return;
        }

        switch (result.status()) {
            case SUCCESS ->
                    messages.sendSuccessKey(
                            player,
                            "network.transfer-success"
                    );
            case REJECTED ->
                    messages.sendErrorKey(
                            player,
                            "network.transfer-rejected"
                    );
            case FAILED ->
                    messages.sendErrorKey(
                            player,
                            "network.transfer-failed"
                    );
            case TIMED_OUT ->
                    messages.sendErrorKey(
                            player,
                            "network.transfer-timed-out"
                    );
        }
    }
}
