@file:JvmName("ExCast")

package io.github.niestrat99.advancedteleport.extensions

fun <T> Any.cast(): T = this as T