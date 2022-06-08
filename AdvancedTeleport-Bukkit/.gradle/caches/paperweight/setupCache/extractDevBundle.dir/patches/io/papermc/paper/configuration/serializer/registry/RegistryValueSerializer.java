package io.papermc.paper.configuration.serializer.registry;

import io.leangen.geantyref.TypeToken;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Use {@link RegistryHolderSerializer} for datapack-configurable things.
 */
public final class RegistryValueSerializer<T> extends RegistryEntrySerializer<T, T> {

    public RegistryValueSerializer(TypeToken<T> type, ResourceKey<? extends Registry<T>> registryKey, boolean omitMinecraftNamespace) {
        super(type, registryKey, omitMinecraftNamespace);
    }

    public RegistryValueSerializer(Class<T> type, ResourceKey<? extends Registry<T>> registryKey, boolean omitMinecraftNamespace) {
        super(type, registryKey, omitMinecraftNamespace);
    }

    @Override
    protected T convertFromResourceKey(ResourceKey<T> key) throws SerializationException {
        final T value = this.registry().get(key);
        if (value == null) {
            throw new SerializationException("Missing value in " + this.registry() + " with key " + key.location());
        }
        return value;
    }

    @Override
    protected ResourceKey<T> convertToResourceKey(T value) {
        return this.registry().getResourceKey(value).orElseThrow();
    }
}
