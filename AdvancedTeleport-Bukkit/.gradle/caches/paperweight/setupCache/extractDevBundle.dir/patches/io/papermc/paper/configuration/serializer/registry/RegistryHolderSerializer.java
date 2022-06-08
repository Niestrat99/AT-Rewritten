package io.papermc.paper.configuration.serializer.registry;

import com.google.common.base.Preconditions;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.function.Function;

public final class RegistryHolderSerializer<T> extends RegistryEntrySerializer<Holder<T>, T> {

    @SuppressWarnings("unchecked")
    public RegistryHolderSerializer(TypeToken<T> typeToken, ResourceKey<? extends Registry<T>> registryKey, boolean omitMinecraftNamespace) {
        super((TypeToken<Holder<T>>) TypeToken.get(TypeFactory.parameterizedClass(Holder.class, typeToken.getType())), registryKey, omitMinecraftNamespace);
    }

    public RegistryHolderSerializer(Class<T> type, ResourceKey<? extends Registry<T>> registryKey, boolean omitMinecraftNamespace) {
        this(TypeToken.get(type), registryKey, omitMinecraftNamespace);
        Preconditions.checkArgument(type.getTypeParameters().length == 0, "%s must have 0 type parameters", type);
    }

    @Override
    protected Holder<T> convertFromResourceKey(ResourceKey<T> key) throws SerializationException {
        return this.registry().getHolder(key).orElseThrow(() -> new SerializationException("Missing holder in " + this.registry().key() + " with key " + key));
    }

    @Override
    protected ResourceKey<T> convertToResourceKey(Holder<T> value) {
        return value.unwrap().map(Function.identity(), r -> this.registry().getResourceKey(r).orElseThrow());
    }
}
