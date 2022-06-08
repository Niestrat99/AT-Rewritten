package io.papermc.paper.configuration.type;

import org.apache.commons.lang3.math.NumberUtils;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.util.OptionalDouble;
import java.util.function.Predicate;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class DoubleOrDefault {
    private static final String DEFAULT_VALUE = "default";
    public static final DoubleOrDefault USE_DEFAULT = new DoubleOrDefault(OptionalDouble.empty());
    public static final ScalarSerializer<DoubleOrDefault> SERIALIZER = new Serializer();

    private OptionalDouble value;

    public DoubleOrDefault(final OptionalDouble value) {
        this.value = value;
    }

    public OptionalDouble value() {
        return this.value;
    }

    public void value(final OptionalDouble value) {
        this.value = value;
    }

    public double or(final double fallback) {
        return this.value.orElse(fallback);
    }

    private static final class Serializer extends ScalarSerializer<DoubleOrDefault> {
        Serializer() {
            super(DoubleOrDefault.class);
        }

        @Override
        public DoubleOrDefault deserialize(final Type type, final Object obj) throws SerializationException {
            if (obj instanceof String string) {
                if (DEFAULT_VALUE.equalsIgnoreCase(string)) {
                    return USE_DEFAULT;
                }
                if (NumberUtils.isParsable(string)) {
                    return new DoubleOrDefault(OptionalDouble.of(Double.parseDouble(string)));
                }
            } else if (obj instanceof Number num) {
                return new DoubleOrDefault(OptionalDouble.of(num.doubleValue()));
            }
            throw new SerializationException(obj + "(" + type + ") is not a double or '" + DEFAULT_VALUE + "'");
        }

        @Override
        protected Object serialize(final DoubleOrDefault item, final Predicate<Class<?>> typeSupported) {
            final OptionalDouble value = item.value();
            if (value.isPresent()) {
                return value.getAsDouble();
            } else {
                return DEFAULT_VALUE;
            }
        }
    }
}
