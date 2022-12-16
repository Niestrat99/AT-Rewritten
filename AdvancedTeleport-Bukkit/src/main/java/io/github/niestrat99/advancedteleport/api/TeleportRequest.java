package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param requester The player sending the request.
 * @param responder The player receiving it.
 */
public record TeleportRequest(
    @NotNull Player requester,
    @NotNull Player responder,
    @NotNull BukkitRunnable timer,
    @NotNull TeleportRequestType type
) {

    private static final List<TeleportRequest> requestList = new ArrayList<>();

    public enum TeleportType {
        TPAHERE,
        TPA
    }

    @Contract(pure = true)
    public static @NotNull List<TeleportRequest> getRequests(@NotNull final Player responder) {
        return requestList.stream()
            .filter(request -> request.responder().equals(responder))
            .toList();
    }

    @Contract(pure = true)
    public static @NotNull List<TeleportRequest> getRequestsByRequester(@NotNull final Player requester) {
        return requestList.stream()
            .filter(request -> request.requester().equals(requester))
            .toList();
    }

    @Contract(pure = true)
    public static @Nullable TeleportRequest getRequestByReqAndResponder(
        @NotNull final Player responder,
        @NotNull final Player requester
    ) {
        return requestList.stream()
            .filter(request -> request.responder().equals(responder))
            .filter(request -> request.requester().equals(requester))
            .findFirst()
            .orElse(null);
    }

    @Contract(pure = true)
    public static void addRequest(@NotNull final TeleportRequest request) {
        if (NewConfig.get().USE_MULTIPLE_REQUESTS.get()) {
            requestList.add(request);
            return;
        }

        final var shouldNotify = NewConfig.get().NOTIFY_ON_EXPIRE.get();
        getRequests(request.responder()).forEach(otherRequest -> {
            otherRequest.destroy();
            if (!shouldNotify) return;
            CustomMessages.sendMessage(
                otherRequest.requester(),
                "Error.requestDisplaced",
                "{player}", otherRequest.responder().getName()
            );
        });
    }

    @Contract(pure = true)
    public static void removeRequest(@NotNull final TeleportRequest request) {
        requestList.remove(request);
    }

    @Contract(pure = true)
    public void destroy() {
        timer.cancel();
        removeRequest(this);
    }

}
