package io.github.niestrat99.advancedteleport.data

import io.github.niestrat99.advancedteleport.extensions.cleanDeserialize
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.*

/**
 * A partial component is a wrapper around the MiniMessage format,
 * which allows for the initial parsing of placeholders such as
 * prefixes and other static elements, which is then cached and can be
 * retrieved as is or with the addition of more placeholder resolutions.
 *
 * **Based on** [link](https://github.com/DaRacci/Minix/blob/1cba4a243bd227f96203a107f37b8b61ca66f7aa/minix-modules/module-common/src/main/kotlin/dev/racci/minix/api/utils/adventure/PartialComponent.kt#L14)
 *
 * @author Racci
 */
class PartialComponent private constructor(private var raw: String) {
    private var _value = raw
    private var dirty = true
    private var cache: Component? = null

    val value: Component
        get() {
            if (dirty) {
                cache = MiniMessage.miniMessage().cleanDeserialize(_value)
                dirty = false
            }
            return cache!!
        }

    operator fun get(vararg placeholders: TagResolver): Component = if (placeholders.isEmpty()) {
        value
    } else MiniMessage.miniMessage().cleanDeserialize(_value, *placeholders)

    fun formatRaw(placeholders: SortedSet<String>) {
        fun prefix(index: Int): String = buildString {
            append("<prefix:")
            append(index)
            append('>')
        }

        var tmp = raw
        val actualPlaceholders = placeholders.withIndex().map { (index, entry) ->
            if (index == 0) { // Assume this is the master prefix for <prefix>
                return@map "<prefix>" to entry
            } else prefix(index) to entry
        }

        for ((placeholder, prefix) in actualPlaceholders) {
            tmp = tmp.replace(placeholder, prefix)

            dirty = true
            cache = null
        }

        _value = tmp
    }

    override fun toString(): String {
        return "PartialComponent(raw='$raw', _value='$_value', dirty=$dirty, cache=$cache)"
    }

    companion object {

        @JvmStatic
        fun of(raw: String): PartialComponent {
            return PartialComponent(raw)
        }
    }
}
