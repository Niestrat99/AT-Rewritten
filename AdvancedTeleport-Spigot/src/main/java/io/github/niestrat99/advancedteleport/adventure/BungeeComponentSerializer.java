package io.github.niestrat99.advancedteleport.adventure;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ChatVersion;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * A component serializer for BungeeCord's {@link BaseComponent}.
 *
 * @since 4.0.0
 * @link <a href="https://github.com/PaperMC/adventure-platform/blob/main/text-serializer-bungeecord/src/main/java/net/kyori/adventure/text/serializer/bungeecord/BungeeComponentSerializer.java">BungeeComponentSerializer </a>
 */
public final class BungeeComponentSerializer implements ComponentSerializer<Component, Component, BaseComponent[]> {
    private static boolean SUPPORTED = true;

    static {
        bind();
    }

    private static final BungeeComponentSerializer MODERN = new BungeeComponentSerializer(GsonComponentSerializer.gson(), LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build());

    /**
     * Gets whether the component serializer has native support.
     *
     * <p>Even if this is {@code false}, the serializer will still work.</p>
     *
     * @return if there is native support
     * @since 4.0.0
     */
    public static boolean isNative() {
        return SUPPORTED;
    }

    /**
     * Gets a component serializer.
     *
     * @return a component serializer
     * @since 4.0.0
     */
    public static BungeeComponentSerializer get() {
        return MODERN;
    }

    /**
     * Create a component serializer with custom serialization properties.
     *
     * @param serializer The serializer creating a JSON representation of the component
     * @param legacySerializer The serializer creating a representation of the component with legacy formatting codes
     * @return a new serializer
     * @since 4.0.0
     */
    public static BungeeComponentSerializer of(final GsonComponentSerializer serializer, final LegacyComponentSerializer legacySerializer) {
        if (serializer == null || legacySerializer == null) return null;
        return new BungeeComponentSerializer(serializer, legacySerializer);
    }

    /**
     * Inject Adventure's adapter serializer into an existing Gson instance.
     *
     * <p>This is primarily for internal use, but may be useful if interfacing
     * with existing libraries that maintain their own Gson instances.</p>
     *
     * @param existing gson instance
     * @return true if injection was successful
     * @since 4.0.0
     */
    public static boolean inject(final Gson existing) {
        final boolean result = GsonInjections.injectGson(requireNonNull(existing, "existing"), builder -> {
            GsonComponentSerializer.gson().populator().apply(builder); // TODO: this might be unused?
            builder.registerTypeAdapterFactory(new SelfSerializable.AdapterFactory());
        });
        SUPPORTED &= result;
        return result;
    }

    private final GsonComponentSerializer serializer;
    private final LegacyComponentSerializer legacySerializer;

    private BungeeComponentSerializer(final GsonComponentSerializer serializer, final LegacyComponentSerializer legacySerializer) {
        this.serializer = serializer;
        this.legacySerializer = legacySerializer;
    }

    private static void bind() {
        try {
            final Field gsonField = GsonInjections.field(net.md_5.bungee.chat.VersionedComponentSerializer.class, "gson");
            inject((Gson) gsonField.get(VersionedComponentSerializer.forVersion(ChatVersion.V1_21_5)));
        } catch (final Throwable error) {
            SUPPORTED = false;
        }
    }

    @Override
    public @NotNull Component deserialize(final @NotNull BaseComponent@NotNull[] input) {
        requireNonNull(input, "input");

        if (input.length == 1 && input[0] instanceof BungeeComponentSerializer.AdapterComponent) {
            return ((BungeeComponentSerializer.AdapterComponent) input[0]).component;
        } else {
            return this.serializer.deserialize(net.md_5.bungee.chat.ComponentSerializer.toString(input));
        }
    }

    @Override
    public @NotNull BaseComponent@NotNull[] serialize(final @NotNull Component component) {
        requireNonNull(component, "component");

        if (SUPPORTED) {
            return new BaseComponent[]{new BungeeComponentSerializer.AdapterComponent(component)};
        } else {
            return net.md_5.bungee.chat.ComponentSerializer.parse(this.serializer.serialize(component));
        }
    }

    class AdapterComponent extends BaseComponent implements SelfSerializable {
        private final Component component;
        private volatile String legacy;

        @SuppressWarnings("deprecation") // TODO: when/if bungee removes this, ???
        AdapterComponent(final Component component) {
            this.component = component;
        }

        @Override
        public String toLegacyText() {
            if (this.legacy == null) {
                this.legacy = BungeeComponentSerializer.this.legacySerializer.serialize(this.component);
            }
            return this.legacy;
        }

        @Override
        public @NotNull BaseComponent duplicate() {
            return this;
        }

        @Override
        public void write(final JsonWriter out) throws IOException {
            BungeeComponentSerializer.this.serializer.serializer().getAdapter(Component.class).write(out, this.component);
        }
    }
}
