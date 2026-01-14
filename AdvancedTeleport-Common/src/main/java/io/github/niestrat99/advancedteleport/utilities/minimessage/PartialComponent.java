package io.github.niestrat99.advancedteleport.utilities.minimessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.SortedSet;

/**
 * A partial component is a wrapper around the MiniMessage format,
 * which allows for the initial parsing of placeholders such as
 * prefixes and other static elements, which is then cached and can be
 * retrieved as is or with the addition of more placeholder resolutions.
 *
 * Based on <a href=https://github.com/DaRacci/Minix/blob/1cba4a243bd227f96203a107f37b8b61ca66f7aa/minix-modules/module-common/src/main/kotlin/dev/racci/minix/api/utils/adventure/PartialComponent.kt#L14>Kotlin version</a>
 *
 * @author Racci, Holly
 */
public class PartialComponent {

    private String value;
    private @Nullable Component cache;

    PartialComponent(final String raw) {
        this.value = raw;
        this.cache = null;
    }

    public static PartialComponent of(final @NotNull String value) {
        return new PartialComponent(value);
    }

    public Component get(final TagResolver... placeholders) {
        if (placeholders.length == 0) {
            if (this.cache == null) this.cache = AdventureUtils.cleanDeserialise(this.value);
            return this.cache;
        } else {
            return AdventureUtils.cleanDeserialise(this.value, placeholders);
        }
    }

    public void formatRaw(SortedSet<String> placeholders) {

        String tempValue = this.value;
        int i = 0;
        for (String placeholder : placeholders) {
            String prefix = prefix(i);
            tempValue = tempValue.replace(prefix, placeholder);

            this.cache = null;
            i++;
        }

        this.value = tempValue;
    }

    private String prefix(int index) {
        return index == 0 ? "<prefix>" : String.format("<prefix:%s>", index);
    }
}
