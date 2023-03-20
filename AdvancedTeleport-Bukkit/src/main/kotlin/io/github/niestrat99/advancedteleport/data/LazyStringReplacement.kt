package io.github.niestrat99.advancedteleport.data

import net.kyori.adventure.text.minimessage.tag.PreProcess
import net.kyori.adventure.text.minimessage.tag.Tag

/**
 * Based on [link](https://github.com/DaRacci/Minix/blob/1cba4a243bd227f96203a107f37b8b61ca66f7aa/minix-modules/module-common/src/main/kotlin/dev/racci/minix/api/utils/adventure/LazyStringReplacement.kt#L6)
 *
 * @author Racci
 */
@JvmInline
value class LazyStringReplacement(val value: () -> String) : Tag, PreProcess {
    override fun value() = value.invoke()
}
