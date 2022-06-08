package io.papermc.paper.registry;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

@DefaultQualifier(NonNull.class)
public abstract class PaperRegistry<API extends Keyed, MINECRAFT> implements org.bukkit.Registry<API> {

    @SuppressWarnings("FieldMayBeFinal") // non-final for testing
    private static Supplier<RegistryAccess> REGISTRY_ACCESS = Suppliers.memoize(() -> MinecraftServer.getServer().registryAccess());
    private static final Map<RegistryKey<?, ?>, PaperRegistry<?, ?>> INTERNAL_REGISTRIES = new HashMap<>();
    public static final Map<RegistryKey<?, ?>, PaperRegistry<?, ?>> REGISTRIES = Collections.unmodifiableMap(INTERNAL_REGISTRIES);
    private static final Map<Class<?>, PaperRegistry<?, ?>> REGISTRY_BY_API_CLASS = new HashMap<>();
    private static final Map<ResourceKey<? extends Registry<?>>, PaperRegistry<?, ?>> REGISTRY_BY_RES_KEY = new HashMap<>();

    private boolean registered;
    private final RegistryKey<API, MINECRAFT> registryKey;
    private final Supplier<Registry<MINECRAFT>> registry;
    private final Map<NamespacedKey, API> cache = new ConcurrentHashMap<>();
    private final Map<NamespacedKey, ResourceKey<MINECRAFT>> resourceKeyCache = new ConcurrentHashMap<>();

    public PaperRegistry(RegistryKey<API, MINECRAFT> registryKey) {
        this.registryKey = registryKey;
        this.registry = Suppliers.memoize(() -> REGISTRY_ACCESS.get().registryOrThrow(this.registryKey.resourceKey()));
    }

    @Override
    public @Nullable API get(NamespacedKey key) {
        return this.cache.computeIfAbsent(key, k -> {
            final @Nullable MINECRAFT nms = this.registry.get().get(CraftNamespacedKey.toMinecraft(k));
            if (nms != null) {
                return this.convertToApi(k, nms);
            }
            return null;
        });
    }

    public abstract @Nullable API convertToApi(NamespacedKey key, MINECRAFT nms);

    public API convertToApiOrThrow(ResourceLocation resourceLocation, MINECRAFT nms) {
        return Objects.requireNonNull(this.convertToApi(resourceLocation, nms), resourceLocation + " has a null api representation");
    }

    public @Nullable API convertToApi(ResourceLocation resourceLocation, MINECRAFT nms) {
        return this.convertToApi(CraftNamespacedKey.fromMinecraft(resourceLocation), nms);
    }

    public API convertToApiOrThrow(Holder<MINECRAFT> nmsHolder) {
        return Objects.requireNonNull(this.convertToApi(nmsHolder), nmsHolder + " has a null api representation");
    }

    public @Nullable API convertToApi(Holder<MINECRAFT> nmsHolder) {
        final Optional<ResourceKey<MINECRAFT>> key = nmsHolder.unwrapKey();
        if (nmsHolder.isBound() && key.isPresent()) {
            return this.convertToApi(key.get().location(), nmsHolder.value());
        } else if (!nmsHolder.isBound() && key.isPresent()) {
            return this.convertToApi(key.get().location(), this.registry.get().getOrThrow(key.get()));
        } else if (nmsHolder.isBound() && key.isEmpty()) {
            final @Nullable ResourceLocation loc = this.registry.get().getKey(nmsHolder.value());
            if (loc != null) {
                return this.convertToApi(loc, nmsHolder.value());
            }
        }
        throw new IllegalStateException("Cannot convert " + nmsHolder + " to an API type in: " + this.registryKey);
    }

    public void convertToApi(Iterable<Holder<MINECRAFT>> holders, Consumer<API> apiConsumer, boolean throwOnNull) {
        for (Holder<MINECRAFT> holder : holders) {
            final @Nullable API api = this.convertToApi(holder);
            if (api == null && throwOnNull) {
                throw new NullPointerException(holder + " has a null api representation");
            } else if (api != null) {
                apiConsumer.accept(api);
            }
        }
    }

    public MINECRAFT getMinecraftValue(API apiValue) {
        return this.registry.get().getOptional(CraftNamespacedKey.toMinecraft(apiValue.getKey())).orElseThrow();
    }

    public Holder<MINECRAFT> getMinecraftHolder(API apiValue) {
        return this.registry.get().getHolderOrThrow(this.resourceKeyCache.computeIfAbsent(apiValue.getKey(), key -> ResourceKey.create(this.registryKey.resourceKey(), CraftNamespacedKey.toMinecraft(key))));
    }

    @Override
    public Iterator<API> iterator() {
        return this.registry.get().keySet().stream().map(key -> this.get(CraftNamespacedKey.fromMinecraft(key))).iterator();
    }

    public void clearCache() {
        this.cache.clear();
    }

    public void register() {
        if (this.registered) {
            throw new IllegalStateException("Already registered: " + this.registryKey.apiClass());
        }
        INTERNAL_REGISTRIES.put(this.registryKey, this);
        REGISTRY_BY_API_CLASS.put(this.registryKey.apiClass(), this);
        REGISTRY_BY_RES_KEY.put(this.registryKey.resourceKey(), this);
        this.registered = true;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || !PaperRegistry.class.isAssignableFrom(o.getClass())) return false;
        PaperRegistry<?, ?> that = (PaperRegistry<?, ?>) o;
        return this.registryKey.equals(that.registryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.registryKey);
    }

    protected static <T> Supplier<Registry<T>> registryFor(ResourceKey<? extends Registry<T>> registryKey) {
        return Suppliers.memoize(() -> REGISTRY_ACCESS.get().registryOrThrow(registryKey));
    }

    public static void clearCaches() {
        for (PaperRegistry<?, ?> registry : INTERNAL_REGISTRIES.values()) {
            registry.clearCache();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Keyed> PaperRegistry<T, ?> getRegistry(Class<T> classOfT) {
        Preconditions.checkArgument(REGISTRY_BY_API_CLASS.containsKey(classOfT), "No registry for that type");
        return (PaperRegistry<T, ?>) REGISTRY_BY_API_CLASS.get(classOfT);
    }

    @SuppressWarnings("unchecked")
    public static <T> PaperRegistry<?, T> getRegistry(ResourceKey<? extends Registry<T>> resourceKey) {
        Preconditions.checkArgument(REGISTRY_BY_RES_KEY.containsKey(resourceKey));
        return (PaperRegistry<?, T>) REGISTRY_BY_RES_KEY.get(resourceKey);
    }

    @SuppressWarnings("unchecked")
    public static <A extends Keyed, M> PaperRegistry<A, M> getRegistry(RegistryKey<A, M> registryKey) {
        Preconditions.checkArgument(INTERNAL_REGISTRIES.containsKey(registryKey));
        return (PaperRegistry<A, M>) INTERNAL_REGISTRIES.get(registryKey);
    }
}
