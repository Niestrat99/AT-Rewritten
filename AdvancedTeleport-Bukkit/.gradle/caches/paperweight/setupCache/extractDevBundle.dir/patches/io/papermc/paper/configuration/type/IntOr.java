package io.papermc.paper.configuration.type;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Type;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

public interface IntOr {

    Logger LOGGER = LogUtils.getLogger();

    default int or(final int fallback) {
        return this.value().orElse(fallback);
    }

    OptionalInt value();

    default int intValue() {
        return this.value().orElseThrow();
    }

    record Default(OptionalInt value) implements IntOr {
        private static final String DEFAULT_VALUE = "default";
        public static final Default USE_DEFAULT = new Default(OptionalInt.empty());
        public static final ScalarSerializer<Default> SERIALIZER = new Serializer<>(Default.class, Default::new, DEFAULT_VALUE, USE_DEFAULT);
    }

    record Disabled(OptionalInt value) implements IntOr {
        private static final String DISABLED_VALUE = "disabled";
        public static final Disabled DISABLED = new Disabled(OptionalInt.empty());
        public static final ScalarSerializer<Disabled> SERIALIZER = new Serializer<>(Disabled.class, Disabled::new, DISABLED_VALUE, DISABLED);

        public boolean test(IntPredicate predicate) {
            return this.value.isPresent() && predicate.test(this.value.getAsInt());
        }

        public boolean enabled() {
            return this.value.isPresent();
        }
    }

    final class Serializer<T extends IntOr> extends ScalarSerializer<T> {

        private final Function<OptionalInt, T> creator;
        private final String otherSerializedValue;
        private final T otherValue;

        public Serializer(Class<T> classOfT, Function<OptionalInt, T> creator, String otherSerializedValue, T otherValue) {
            super(classOfT);
            this.creator = creator;
            this.otherSerializedValue = otherSerializedValue;
            this.otherValue = otherValue;
        }

        @Override
        public T deserialize(Type type, Object obj) throws SerializationException {
            if (obj instanceof String string) {
                if (this.otherSerializedValue.equalsIgnoreCase(string)) {
                    return this.otherValue;
                }
                if (NumberUtils.isParsable(string)) {
                    return this.creator.apply(OptionalInt.of(Integer.parseInt(string)));
                }
            } else if (obj instanceof Number num) {
                if (num.intValue() != num.doubleValue() || num.intValue() != num.longValue()) {
                    LOGGER.error("{} cannot be converted to an integer without losing information", num);
                }
                return this.creator.apply(OptionalInt.of(num.intValue()));
            }
            throw new SerializationException(obj + "(" + type + ") is not a integer or '" + this.otherSerializedValue + "'");
        }

        @Override
        protected Object serialize(T item, Predicate<Class<?>> typeSupported) {
            final OptionalInt value = item.value();
            if (value.isPresent()) {
                return value.getAsInt();
            } else {
                return this.otherSerializedValue;
            }
        }
    }
}
