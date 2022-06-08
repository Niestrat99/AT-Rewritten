package io.papermc.paper.configuration.constraint;

import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.configuration.type.DoubleOrDefault;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.objectmapping.meta.Constraint;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.OptionalDouble;

public final class Constraints {
    private Constraints() {
    }

    public static final class Velocity implements Constraint<GlobalConfiguration.Proxies.Velocity> {

        private static final Logger LOGGER = LogUtils.getLogger();

        @Override
        public void validate(final GlobalConfiguration.Proxies.@Nullable Velocity value) throws SerializationException {
            if (value != null && value.enabled && value.secret.isEmpty()) {
                LOGGER.error("Velocity is enabled, but no secret key was specified. A secret key is required. Disabling velocity...");
                value.enabled = false;
            }
        }
    }

    public static final class Positive implements Constraint<Number> {
        @Override
        public void validate(@Nullable Number value) throws SerializationException {
            if (value != null && value.doubleValue() <= 0) {
                throw new SerializationException(value + " should be positive");
            }
        }
    }

    public static final class BelowZeroDoubleToDefault implements Constraint<DoubleOrDefault> {
        @Override
        public void validate(final @Nullable DoubleOrDefault container) {
            if (container != null) {
                final OptionalDouble value = container.value();
                if (value.isPresent() && value.getAsDouble() < 0) {
                    container.value(OptionalDouble.empty());
                }
            }
        }
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Min {
        int value();

        final class Factory implements Constraint.Factory<Min, Number> {
            @Override
            public Constraint<Number> make(Min data, Type type) {
                return value -> {
                    if (value != null && value.intValue() < data.value()) {
                        throw new SerializationException(value + " is less than the min " + data.value());
                    }
                };
            }
        }
    }
}
