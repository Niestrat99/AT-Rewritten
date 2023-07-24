@file:JvmName("ExAdventure")

package io.github.niestrat99.advancedteleport.extensions // ktlint-disable filename

import io.github.niestrat99.advancedteleport.data.LazyStringReplacement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.function.Supplier

private val components by lazy {
    val parent = Component::class.qualifiedName!!.split(".").dropLast(1).joinToString(".")
    val path = "() -> $parent"
    arrayOf(
        "$path.Component",
        "$path.MiniMessage",
        "$path.TextComponent",
        "$path.KeybindComponent",
        "$path.TranslatableComponent"
    )
}

/**
 * Based on [link](https://github.com/DaRacci/Minix/blob/master/Minix-API/src/main/kotlin/dev/racci/minix/api/extensions/ExAdventure.kt#L34)
 *
 * @author Racci
 */
fun MiniMessage.lazyPlaceholder(
    input: String,
    vararg template: Pair<String, *>
): Component {
    val resolver = TagResolver.builder()
    template.map { (placeholder, value) ->
        placeholder to when (value) {
            is Component -> Tag.selfClosingInserting(value)
            is Supplier<*> -> when (value.toString()) {
                in components -> Tag.selfClosingInserting { value.get() as Component }
                else -> LazyStringReplacement { value.get().toString() }
            }

            else -> Tag.preProcessParsed(value.toString())
        }
    }.forEach { (placeholder, tag) -> resolver.tag(placeholder, tag) }

    return deserialize(input, resolver.build())
}

fun MiniMessage.cleanDeserialize(
    input: String,
    vararg placeholders: TagResolver
): Component {

    //
    return deserialize(input, *placeholders).replaceText(
        TextReplacementConfig.builder().match("</[a-zA-Z-_]+>").replacement("").build()
    )
}
