package io.github.niestrat99.advancedteleport.sql;

public class AuditLogManager extends SQLManager {


    @Override
    public void createTable() {

    }

    @Override
    public void transferOldData() {

    }

    public enum Action {
        BACK,
        BLOCK,
        HOME_CREATE,
        HOME_TELEPORT,
        HOME_MOVE,
        HOME_DELETE,
        OFFLINE_TP,
        OFFLINE_TP_HERE,
        SPAWN_SET,
        SPAWN_TELEPORT,
        TP_ALL,
        TP_CANCEL,
        TP_LOC,
        TP_REQUEST,
        TP_REQUEST_HERE,
        UNBLOCK,
        WARP_CREATE,
        WARP_TELEPORT,
        WARP_MOVE,
        WARP_DELETE,
    }
}
