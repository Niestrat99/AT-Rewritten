package io.github.niestrat99.advancedteleport.utilities.minimessage;

import io.github.niestrat99.advancedteleport.utilities.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.PreProcess;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class AdventureUtils {

    @SafeVarargs
    public static Component lazyPlaceholder(final @NotNull String input, Pair<String, Object>... template) {
        final TagResolver.Builder resolver = TagResolver.builder();
        for (Pair<String, ?> placeholder : template) {

            final String placeholderName = placeholder.fst();
            final Object value = placeholder.snd();

            final Tag tag;
            if (value instanceof Component component) {
                tag = Tag.selfClosingInserting(component);
            } else if (value instanceof Supplier<?> supplier) {
                Object suppliedValue = supplier.get();
                if (suppliedValue instanceof Component component) {
                    tag = Tag.selfClosingInserting(component);
                } else {
                    tag = new LazyStringReplacement(supplier);
                }
            } else {
                tag = Tag.preProcessParsed(value.toString());
            }

            resolver.tag(placeholderName, tag);
        }

        return MiniMessage.miniMessage().deserialize(input, resolver.build());
    }

    public static Component cleanDeserialise(String input, TagResolver... placeholders) {
        return MiniMessage.miniMessage().deserialize(input, placeholders).replaceText(
                TextReplacementConfig.builder().match("</[a-zA-Z-_]+>").replacement("").build()
        );
    }

    private record LazyStringReplacement(Supplier<?> supplier) implements PreProcess {

        @Override
        public @NotNull String value() {
            return this.supplier.get().toString();
        }
    }
}
