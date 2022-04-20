package io.github.niestrat99.advancedteleport.api;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TeleportRequest {

    private static final List<TeleportRequest> requestList = new ArrayList<>();
    private final Player requester; // The player sending the request.
    private final Player responder; // The player receiving it.
    private final BukkitRunnable timer;
    private final TeleportRequestType type;

    public TeleportRequest(Player requester, Player responder, BukkitRunnable timer, TeleportRequestType type) {
        this.requester = requester;
        this.responder = responder;
        this.timer = timer;
        this.type = type;
    }

    public BukkitRunnable getTimer() {
        return timer;
    }

    public Player getRequester() {
        return requester;
    }

    public Player getResponder() {
        return responder;
    }

    public TeleportRequestType getType() {
        return type;
    }

    public enum TeleportType {
        TPAHERE,
        TPA
    }

    public static List<TeleportRequest> getRequests(Player responder) {
        List<TeleportRequest> requests = new ArrayList<>();
        for (TeleportRequest request : requestList) {
            if (request.responder == responder) {
                requests.add(request);
            }
        }
        return requests;
    }

    public static List<TeleportRequest> getRequestsByRequester(Player requester) {
        List<TeleportRequest> requests = new ArrayList<>(); // Requests that the requester has pending
        for (TeleportRequest request : requestList) {
            if (request.getRequester() == requester) {
                requests.add(request);
            }
        }
        return requests;
    }

    public static TeleportRequest getRequestByReqAndResponder(Player responder, Player requester) {
        for (TeleportRequest request : requestList) {
            if (request.getRequester() == requester && request.getResponder() == responder) {
                return request;
            }
        }
        return null;
    }

    public static void addRequest(TeleportRequest request) {
        if (!NewConfig.get().USE_MULTIPLE_REQUESTS.get()) {
            for (TeleportRequest otherRequest : getRequests(request.responder)) {
                if (NewConfig.get().NOTIFY_ON_EXPIRE.get()) {
                    CustomMessages.sendMessage(otherRequest.requester, "Info.requestDisplaced", "{player}", request.responder.getName());
                }
                otherRequest.destroy();
            }
        }
        requestList.add(request);
    }

    public static void removeRequest(TeleportRequest request) {
        requestList.remove(request);
    }

    public void destroy() {
        timer.cancel();
        removeRequest(this);
    }

}
