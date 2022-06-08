package io.papermc.paper.configuration;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.FieldDiscoverer;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedSupplier;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static io.leangen.geantyref.GenericTypeReflector.erase;

final class InnerClassFieldDiscoverer implements FieldDiscoverer<Map<Field, Object>> {

    private final Map<Class<?>, Object> instanceMap = new HashMap<>();
    private final Map<Class<?>, Object> overrides;
    @SuppressWarnings("unchecked")
    private final FieldDiscoverer<Map<Field, Object>> delegate = (FieldDiscoverer<Map<Field, Object>>) FieldDiscoverer.object(target -> {
        final Class<?> type = erase(target.getType());
        if (this.overrides().containsKey(type)) {
            this.instanceMap.put(type, this.overrides().get(type));
            return () -> this.overrides().get(type);
        }
        if (ConfigurationPart.class.isAssignableFrom(type) && !this.instanceMap.containsKey(type)) {
            try {
                final Constructor<?> constructor;
                final CheckedSupplier<Object, ReflectiveOperationException> instanceSupplier;
                if (type.getEnclosingClass() != null && !Modifier.isStatic(type.getModifiers())) {
                    final @Nullable Object instance = this.instanceMap.get(type.getEnclosingClass());
                    if (instance == null) {
                        throw new SerializationException("Cannot create a new instance of an inner class " + type.getName() + " without an instance of its enclosing class " + type.getEnclosingClass().getName());
                    }
                    constructor = type.getDeclaredConstructor(type.getEnclosingClass());
                    instanceSupplier = () -> constructor.newInstance(instance);
                } else {
                    constructor = type.getDeclaredConstructor();
                    instanceSupplier = constructor::newInstance;
                }
                constructor.setAccessible(true);
                final Object instance = instanceSupplier.get();
                this.instanceMap.put(type, instance);
                return () -> instance;
            } catch (ReflectiveOperationException e) {
                throw new SerializationException(ConfigurationPart.class, target + " must be a valid ConfigurationPart", e);
            }
        } else {
            throw new SerializationException(target + " must be a valid ConfigurationPart");
        }
    }, "Object must be a unique ConfigurationPart");

    InnerClassFieldDiscoverer(Map<Class<?>, Object> overrides) {
        this.overrides = overrides;
    }

    @Override
    public @Nullable <V> InstanceFactory<Map<Field, Object>> discover(AnnotatedType target, FieldCollector<Map<Field, Object>, V> collector) throws SerializationException {
        final Class<?> clazz = erase(target.getType());
        if (ConfigurationPart.class.isAssignableFrom(clazz)) {
            final FieldDiscoverer.@Nullable InstanceFactory<Map<Field, Object>> instanceFactoryDelegate = this.delegate.<V>discover(target, (name, type, annotations, deserializer, serializer) -> {
                if (!erase(type.getType()).equals(clazz.getEnclosingClass())) { // don't collect synth fields for inner classes
                    collector.accept(name, type, annotations, deserializer, serializer);
                }
            });
            if (instanceFactoryDelegate instanceof FieldDiscoverer.MutableInstanceFactory<Map<Field, Object>> mutableInstanceFactoryDelegate) {
                return new MutableInstanceFactory<>() {
                    @Override
                    public Map<Field, Object> begin() {
                        return mutableInstanceFactoryDelegate.begin();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void complete(Object instance, Map<Field, Object> intermediate) throws SerializationException {
                        final Iterator<Map.Entry<Field, Object>> iter = intermediate.entrySet().iterator();
                        try {
                            while (iter.hasNext()) { // manually merge any mergeable maps
                                Map.Entry<Field, Object> entry = iter.next();
                                if (entry.getKey().isAnnotationPresent(MergeMap.class) && Map.class.isAssignableFrom(entry.getKey().getType()) && intermediate.get(entry.getKey()) instanceof Map<?, ?> map) {
                                    iter.remove();
                                    @Nullable Map<Object, Object> existingMap = (Map<Object, Object>) entry.getKey().get(instance);
                                    if (existingMap != null) {
                                        existingMap.putAll(map);
                                    } else {
                                        entry.getKey().set(instance, entry.getValue());
                                    }
                                }
                            }
                        } catch (final IllegalAccessException e) {
                            throw new SerializationException(target.getType(), e);
                        }
                        mutableInstanceFactoryDelegate.complete(instance, intermediate);
                    }

                    @Override
                    public Object complete(Map<Field, Object> intermediate) throws SerializationException {
                        @Nullable Object targetInstance = InnerClassFieldDiscoverer.this.instanceMap.get(GenericTypeReflector.erase(target.getType()));
                        if (targetInstance != null) {
                            this.complete(targetInstance, intermediate);
                        } else {
                            targetInstance = mutableInstanceFactoryDelegate.complete(intermediate);
                        }
                        if (targetInstance instanceof ConfigurationPart.Post post) {
                            post.postProcess();
                        }
                        return targetInstance;
                    }

                    @Override
                    public boolean canCreateInstances() {
                        return mutableInstanceFactoryDelegate.canCreateInstances();
                    }
                };
            }
        }
        return null;
    }

    private Map<Class<?>, Object> overrides() {
        return this.overrides;
    }

    static FieldDiscoverer<?> worldConfig(Configurations.ContextMap contextMap) {
        final Map<Class<?>, Object> overrides = Map.of(
            WorldConfiguration.class, new WorldConfiguration(
                contextMap.require(PaperConfigurations.SPIGOT_WORLD_CONFIG_CONTEXT_KEY).get(),
                contextMap.require(Configurations.WORLD_KEY)
            )
        );
        return new InnerClassFieldDiscoverer(overrides);
    }

    static FieldDiscoverer<?> globalConfig() {
        return new InnerClassFieldDiscoverer(Collections.emptyMap());
    }
}
