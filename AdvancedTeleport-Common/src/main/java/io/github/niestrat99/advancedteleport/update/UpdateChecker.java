package io.github.niestrat99.advancedteleport.update;

public interface UpdateChecker {

    AvailableUpdate getLatestVersion();

    String getDownloadLink();
}
