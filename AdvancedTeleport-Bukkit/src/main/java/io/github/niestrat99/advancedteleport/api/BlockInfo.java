package io.github.niestrat99.advancedteleport.api;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Represents
 */
public class BlockInfo {

    private final @NotNull UUID receiverUUID;
    private final @NotNull UUID blockedUUID;
    private final @NotNull String formattedTime;
    private final long time;
    private @Nullable String reason;

    public BlockInfo(@NotNull UUID receiver, @NotNull UUID blocked, @Nullable String reason, long time) {
        receiverUUID = receiver;
        blockedUUID = blocked;
        this.reason = reason;
        this.time = time;

        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        formattedTime = format.format(date);
    }

    @NotNull
    public OfflinePlayer getReceivingPlayer() {
        return Bukkit.getOfflinePlayer(receiverUUID);
    }

    @NotNull
    public OfflinePlayer getBlockedPlayer() {
        return Bukkit.getOfflinePlayer(blockedUUID);
    }

    @NotNull
    public UUID getReceiverUUID() {
        return receiverUUID;
    }

    public long getTime() {
        return time;
    }

    @NotNull
    public UUID getBlockedUUID() {
        return blockedUUID;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    public void setReason(@Nullable String reason) {
        this.reason = reason;
    }

    @NotNull
    public String getFormattedTime() {
        return formattedTime;
    }
}
