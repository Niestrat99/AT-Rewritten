package io.github.niestrat99.advancedteleport.data

import io.github.niestrat99.advancedteleport.extensions.lazyPlaceholder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

/**
 * A partial component is a wrapper around the MiniMessage format,
 * which allows for the initial parsing of placeholders such as prefixes and other static elements,
 * which is then cached and can be retrieved as is or with the addition of more placeholder resolutions.
 *
 * **Based on** [link](https://github.com/DaRacci/Minix/blob/master/Minix-API/src/main/kotlin/dev/racci/minix/api/utils/adventure/PartialComponent.kt#L16)
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
                cache = MiniMessage.miniMessage().deserialize(_value)
                dirty = false
            }
            return cache!!
        }

    operator fun get(vararg placeholders: Pair<String, *>): Component = if (placeholders.isEmpty()) {
        value
    } else MiniMessage.miniMessage().lazyPlaceholder(_value, *placeholders)

    fun formatRaw(placeholders: Map<String, String>) {
        var tmp = raw

        for ((placeholder, prefix) in placeholders.entries) {
            val index = tmp.indexOf(placeholder).takeIf { it != -1 } ?: continue
            tmp = tmp.replaceRange(index, index + placeholder.length, prefix)

            dirty = true
            cache = null
        }

        _value = tmp
    }

    override fun toString(): String {
        return "PartialComponent(raw='$raw', _value='$_value', dirty=$dirty, cache=$cache)"
    }

    companion object {

        fun of(raw: String): PartialComponent {
            return PartialComponent(raw)
        }
    }

    object Serializer : TypeSerializer<PartialComponent> {

        override fun deserialize(
            type: Type,
            node: ConfigurationNode
        ): PartialComponent = node.string?.let(Companion::of) ?: throw SerializationException(type, "Null Partial Component: ${node.path()}")

        override fun serialize(
            type: Type,
            obj: PartialComponent?,
            node: ConfigurationNode
        ) {
            if (obj == null) { node.raw(null); return }
            node.set(obj.raw)
        }
    }
}
