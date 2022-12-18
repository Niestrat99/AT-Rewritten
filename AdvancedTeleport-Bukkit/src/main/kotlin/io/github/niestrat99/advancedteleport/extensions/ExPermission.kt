@file:JvmName("ExPermission")

package io.github.niestrat99.advancedteleport.extensions

import org.bukkit.permissions.Permissible

/**
 * [**Based on**](https://github.com/DaRacci/Minix/blob/72bdcd377a66808c6cf79ac647fbd3b886fd909f/Minix-API/src/main/kotlin/dev/racci/minix/api/extensions/ExPermission.kt#L7)
 * @author Racci
 */
fun Permissible.anyPermission(
    vararg permissions: String
) = permissions.any(::hasPermission)

/**
 * [**Based on**](https://github.com/DaRacci/Minix/blob/72bdcd377a66808c6cf79ac647fbd3b886fd909f/Minix-API/src/main/kotlin/dev/racci/minix/api/extensions/ExPermission.kt#L11)
 * @author Racci
 */
fun Permissible.allPermission(
    vararg permissions: String
) = permissions.all(::hasPermission)

/**
 * [**Based on**](https://github.com/DaRacci/Minix/blob/72bdcd377a66808c6cf79ac647fbd3b886fd909f/Minix-API/src/main/kotlin/dev/racci/minix/api/extensions/ExPermission.kt#L15)
 * @author Racci
 */
fun Permissible.hasPermissionOrStar(
    permission: String
) = hasPermission(permission) || hasPermission(permission.replaceAfterLast('.', "*"))
