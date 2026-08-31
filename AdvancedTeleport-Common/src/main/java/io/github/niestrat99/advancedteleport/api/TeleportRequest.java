package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @param requester The player sending the request.
 * @param responder The player receiving it.
 */
public record TeleportRequest(
        @NotNull Player requester,
        @NotNull Player responder,
        @NotNull BukkitRunnable timer,
        @NotNull TeleportRequestType type) {

    private static final List<TeleportRequest> requestList = new ArrayList<>();

    @Contract(pure = true)
    public static @NotNull List<TeleportRequest> getRequestsByRequester(
            @NotNull final Player requester) {
        return requestList.stream()
                .filter(request -> request.requester().equals(requester))
                .toList();
    }

    @Contract(pure = true)
    public static @Nullable TeleportRequest getRequestByReqAndResponder(
            @NotNull final Player responder, @NotNull final Player requester) {
        return requestList.stream()
                .filter(request -> request.responder().equals(responder))
                .filter(request -> request.requester().equals(requester))
                .findFirst()
                .orElse(null);
    }

    @Contract(pure = true)
    public static void addRequest(@NotNull final TeleportRequest request) {

        // If multiple requests are allowed, then just add the request
        if (MainConfig.get().USE_MULTIPLE_REQUESTS.get()) {
            requestList.add(request);
            return;
        }

        // Find the other request and displace it
        final var shouldNotify = MainConfig.get().NOTIFY_ON_EXPIRE.get();
        getRequests(request.responder())
                .forEach(
                        otherRequest -> {
                            otherRequest.destroy();
                            if (!shouldNotify) return;
                            CustomMessages.sendMessage(
                                    otherRequest.requester(),
                                    "Error.requestDisplaced",
                                    Placeholder.unparsed(
                                            "player", otherRequest.responder().getName()));
                        });

        // Add the request
        requestList.add(request);
    }

    @Contract(pure = true)
    public static @NotNull List<TeleportRequest> getRequests(@NotNull final Player responder) {
        return requestList.stream()
                .filter(request -> request.responder().equals(responder))
                .toList();
    }

    @Contract(pure = true)
    public void destroy() {
        timer.cancel();
        removeRequest(this);
    }

    @Contract(pure = true)
    public static void removeRequest(@NotNull final TeleportRequest request) {
        requestList.remove(request);
    }
}
