package io.papermc.paper.util;

import com.sun.security.auth.module.NTSystem;
import com.sun.security.auth.module.UnixSystem;
import org.apache.commons.lang.SystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ServerEnvironment {
    private static final boolean RUNNING_AS_ROOT_OR_ADMIN;
    private static final String WINDOWS_HIGH_INTEGRITY_LEVEL = "S-1-16-12288";

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            RUNNING_AS_ROOT_OR_ADMIN = Set.of(new NTSystem().getGroupIDs()).contains(WINDOWS_HIGH_INTEGRITY_LEVEL);
        } else {
            boolean isRunningAsRoot = false;
            if (new UnixSystem().getUid() == 0) {
                // Due to an OpenJDK bug (https://bugs.openjdk.java.net/browse/JDK-8274721), UnixSystem#getUid incorrectly
                // returns 0 when the user doesn't have a username. Because of this, we'll have to double-check if the user ID is
                // actually 0 by running the id -u command.
                try {
                    Process process = new ProcessBuilder("id", "-u").start();
                    process.waitFor();
                    InputStream inputStream = process.getInputStream();
                    isRunningAsRoot = new String(inputStream.readAllBytes()).trim().equals("0");
                } catch (InterruptedException | IOException ignored) {
                    isRunningAsRoot = false;
                }
            }
            RUNNING_AS_ROOT_OR_ADMIN = isRunningAsRoot;
        }
    }

    public static boolean userIsRootOrAdmin() {
        return RUNNING_AS_ROOT_OR_ADMIN;
    }

    public static boolean isMissingAWTDependency() {
        try {
            new java.awt.Color(0);
        } catch (UnsatisfiedLinkError e) {
            return true;
        }

        return false;
    }
}
