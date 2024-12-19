package io.github.niestrat99.advancedteleport.utilities;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

/**
 * Java port based on <a href="https://github.com/DaRacci/Minix/blob/72bdcd377a66808c6cf79ac647fbd3b886fd909f/Minix-API/src/main/kotlin/dev/racci/minix/api/extensions/ExPermission.kt">Kotlin version</a>
 * @author Racci, Holly
 */
public class PermissionUtil {

    public static boolean hasPermissionOrStar(final @NotNull Permissible sender, final @NotNull String permission) {
        final String starPermission = permission.substring(0, permission.lastIndexOf('.') + 1) + "*";
        return sender.hasPermission(permission) || sender.hasPermission(starPermission);
    }

    public static boolean hasAnyPermission(final @NotNull Permissible sender, final @NotNull String... permissions) {
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) return true;
        }
        return false;
    }

    public static boolean hasAllPermissions(final @NotNull Permissible sender, final @NotNull String... permissions) {
        for (String permission : permissions) {
            if (!sender.hasPermission(permission)) return false;
        }
        return true;
    }
}
