package io.github.niestrat99.advancedteleport.api;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents
 */
public final class BlockInfo {

    @NotNull private final UUID receiverUUID;
    @NotNull private final UUID blockedUUID;
    @Nullable private String reason;
    private final long time;
    @NotNull private final String formattedTime;

    @Contract(pure = true)
    public BlockInfo(
        @NotNull final UUID receiver,
        @NotNull final UUID blocked,
        @Nullable final String reason,
        final long time
    ) {
        receiverUUID = receiver;
        blockedUUID = blocked;
        this.reason = reason;
        this.time = time;

        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        formattedTime = format.format(date);
    }

    @Contract(pure = true)
    public @NotNull OfflinePlayer getReceivingPlayer() {
        return Bukkit.getOfflinePlayer(receiverUUID);
    }

    @Contract(pure = true)
    public @NotNull OfflinePlayer getBlockedPlayer() {
        return Bukkit.getOfflinePlayer(blockedUUID);
    }

    @Contract(pure = true)
    public @NotNull UUID getReceiverUUID() {
        return receiverUUID;
    }

    @Contract(pure = true)
    public long getTime() {
        return time;
    }

    @Contract(pure = true)
    public @NotNull UUID getBlockedUUID() {
        return blockedUUID;
    }

    @Contract(pure = true)
    public @Nullable String getReason() {
        return reason;
    }

    @Contract(pure = true)
    public void setReason(@Nullable String reason) {
        this.reason = reason;
    }

    @Contract(pure = true)
    public @NotNull String getFormattedTime() {
        return formattedTime;
    }
}
