package io.papermc.paper.configuration.serializer;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public final class StringRepresentableSerializer extends ScalarSerializer<StringRepresentable> {
    private static final Map<Type, Function<String, StringRepresentable>> TYPES = Collections.synchronizedMap(Map.ofEntries(
        Map.entry(MobCategory.class, s -> {
            for (MobCategory value : MobCategory.values()) {
                if (value.getSerializedName().equals(s)) {
                    return value;
                }
            }
            return null;
        })
    ));

    public StringRepresentableSerializer() {
        super(StringRepresentable.class);
    }

    @Override
    public StringRepresentable deserialize(Type type, Object obj) throws SerializationException {
        Function<String, StringRepresentable> function = TYPES.get(type);
        if (function == null) {
            throw new SerializationException(type + " isn't registered");
        }
        return function.apply(obj.toString());
    }

    @Override
    protected Object serialize(StringRepresentable item, Predicate<Class<?>> typeSupported) {
        return item.getSerializedName();
    }
}
