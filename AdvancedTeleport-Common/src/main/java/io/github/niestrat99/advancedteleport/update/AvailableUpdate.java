package io.github.niestrat99.advancedteleport.update;

import io.github.niestrat99.advancedteleport.CoreAdvancedTeleport;

import java.time.ZonedDateTime;

public record AvailableUpdate(String versionTag, ZonedDateTime releasedAt) {

    public boolean isNewerThanCurrent() {
        ZonedDateTime builtTimestamp = CoreAdvancedTeleport.getBuildTimestamp();
        if (builtTimestamp == null) return false;
        return releasedAt.isAfter(builtTimestamp);
    }
}
