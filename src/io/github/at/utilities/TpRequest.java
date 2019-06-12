package io.github.at.utilities;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TpRequest {

    private static List<TpRequest> requestList = new ArrayList<>();
    private Player requester; // The player sending the request.
    private Player responder; // The player receiving it.
    private BukkitRunnable timer;
    private TeleportType type;

    public TpRequest(Player requester, Player responder, BukkitRunnable timer, TeleportType type) {
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
        TPA_HERE,
        TPA_NORMAL
    }

    public static List<TpRequest> getRequests(Player responder) {
        List<TpRequest> requests = new ArrayList<>();
        for (TpRequest request : requestList) {
            if (request.responder == responder) {
                requests.add(request);
            }
        }
        return requests;
    }

    public static List<TpRequest> getRequestsByRequester(Player requester) {
        List<TpRequest> requests = new ArrayList<>(); // Requests that the requester has pending
        for (TpRequest request : requestList) {
            if (request.getRequester() == requester) {
                requests.add(request);
            }
        }
        return requests;
    }

    public static TpRequest getRequestByReqAndResponder(Player responder, Player requester) {
        for (TpRequest request : requestList) {
            if (request.getRequester() == requester && request.getResponder() == responder) {
                return request;
            }
        }
        return null;
    }

    public static void addRequest(TpRequest request) {
        requestList.add(request);
    }

    public static void removeRequest(TpRequest request) {
        requestList.remove(request);
    }

    public void destroy() {
        timer.cancel();
        removeRequest(this);
    }

}
