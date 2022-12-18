package io.github.niestrat99.advancedteleport.data

import net.kyori.adventure.text.minimessage.tag.PreProcess
import net.kyori.adventure.text.minimessage.tag.Tag

/**
 * Based on [link](https://github.com/DaRacci/Minix/blob/master/Minix-API/src/main/kotlin/dev/racci/minix/api/utils/adventure/LazyStringReplacement.kt#L6)
 *
 * @author Racci
 */
@JvmInline
value class LazyStringReplacement(val value: () -> String) : Tag, PreProcess {
    override fun value() = value.invoke()
}
