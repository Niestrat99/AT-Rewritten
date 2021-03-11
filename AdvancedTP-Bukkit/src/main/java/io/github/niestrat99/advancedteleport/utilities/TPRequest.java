package io.github.niestrat99.advancedteleport.utilities;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TPRequest {

    private static List<TPRequest> requestList = new ArrayList<>();
    private Player requester; // The player sending the request.
    private Player responder; // The player receiving it.
    private BukkitRunnable timer;
    private TeleportType type;

    public TPRequest(Player requester, Player responder, BukkitRunnable timer, TeleportType type) {
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

    public TeleportType getType() {
        return type;
    }

    public enum TeleportType {
        TPAHERE,
        TPA
    }

    public static List<TPRequest> getRequests(Player responder) {
        List<TPRequest> requests = new ArrayList<>();
        for (TPRequest request : requestList) {
            if (request.responder == responder) {
                requests.add(request);
            }
        }
        return requests;
    }

    public static List<TPRequest> getRequestsByRequester(Player requester) {
        List<TPRequest> requests = new ArrayList<>(); // Requests that the requester has pending
        for (TPRequest request : requestList) {
            if (request.getRequester() == requester) {
                requests.add(request);
            }
        }
        return requests;
    }

    public static TPRequest getRequestByReqAndResponder(Player responder, Player requester) {
        for (TPRequest request : requestList) {
            if (request.getRequester() == requester && request.getResponder() == responder) {
                return request;
            }
        }
        return null;
    }

    public static void addRequest(TPRequest request) {
        requestList.add(request);
    }

    public static void removeRequest(TPRequest request) {
        requestList.remove(request);
    }

    public void destroy() {
        timer.cancel();
        removeRequest(this);
    }

}
