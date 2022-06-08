package com.destroystokyo.paper.profile;

import io.papermc.paper.configuration.GlobalConfiguration;
import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.v1_19_R1.configuration.ConfigSerializationUtil;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.profile.CraftPlayerTextures;
import org.bukkit.craftbukkit.v1_19_R1.profile.CraftProfileProperty;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SerializableAs("PlayerProfile")
public class CraftPlayerProfile implements PlayerProfile, SharedPlayerProfile {

    private GameProfile profile;
    private final PropertySet properties = new PropertySet();

    public CraftPlayerProfile(CraftPlayer player) {
        this.profile = player.getHandle().getGameProfile();
    }

    public CraftPlayerProfile(UUID id, String name) {
        this.profile = new GameProfile(id, name);
    }

    public CraftPlayerProfile(GameProfile profile) {
        Validate.notNull(profile, "GameProfile cannot be null!");
        this.profile = profile;
    }

    @Override
    public boolean hasProperty(String property) {
        return profile.getProperties().containsKey(property);
    }

    @Override
    public void setProperty(ProfileProperty property) {
        String name = property.getName();
        PropertyMap properties = profile.getProperties();
        properties.removeAll(name);
        properties.put(name, new Property(name, property.getValue(), property.getSignature()));
    }

    @Override
    public CraftPlayerTextures getTextures() {
        return new CraftPlayerTextures(this);
    }

    @Override
    public void setTextures(@Nullable PlayerTextures textures) {
        if (textures == null) {
            this.removeProperty("textures");
        } else {
            CraftPlayerTextures craftPlayerTextures = new CraftPlayerTextures(this);
            craftPlayerTextures.copyFrom(textures);
            craftPlayerTextures.rebuildPropertyIfDirty();
        }
    }

    public GameProfile getGameProfile() {
        return profile;
    }

    @Nullable
    @Override
    public UUID getId() {
        return profile.getId();
    }

    @Override
    @Deprecated(forRemoval = true)
    public UUID setId(@Nullable UUID uuid) {
        GameProfile prev = this.profile;
        this.profile = new GameProfile(uuid, prev.getName());
        copyProfileProperties(prev, this.profile);
        return prev.getId();
    }

    @Override
    public UUID getUniqueId() {
        return getId();
    }

    @Nullable
    @Override
    public String getName() {
        return profile.getName();
    }

    @Override
    @Deprecated(forRemoval = true)
    public String setName(@Nullable String name) {
        GameProfile prev = this.profile;
        this.profile = new GameProfile(prev.getId(), name);
        copyProfileProperties(prev, this.profile);
        return prev.getName();
    }

    @Nonnull
    @Override
    public Set<ProfileProperty> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Collection<ProfileProperty> properties) {
        properties.forEach(this::setProperty);
    }

    @Override
    public void clearProperties() {
        profile.getProperties().clear();
    }

    @Override
    public boolean removeProperty(String property) {
        return !profile.getProperties().removeAll(property).isEmpty();
    }

    @Nullable
    @Override
    public Property getProperty(String property) {
        return Iterables.getFirst(this.profile.getProperties().get(property), null);
    }

    @Nullable
    @Override
    public void setProperty(@NotNull String propertyName, @Nullable Property property) {
        PropertyMap properties = profile.getProperties();
        properties.removeAll(propertyName);
        if (property != null) {
            properties.put(propertyName, property);
        }
    }

    @Override
    public @NotNull GameProfile buildGameProfile() {
        GameProfile profile = new GameProfile(this.profile.getId(), this.profile.getName());
        profile.getProperties().putAll(this.profile.getProperties());
        return profile;
    }

    @Override
    public CraftPlayerProfile clone() {
        CraftPlayerProfile clone = new CraftPlayerProfile(this.getId(), this.getName());
        clone.setProperties(getProperties());
        return clone;
    }

    @Override
    public boolean isComplete() {
        return profile.isComplete();
    }

    @Override
    public @NotNull CompletableFuture<PlayerProfile> update() {
        return CompletableFuture.supplyAsync(() -> {
            final CraftPlayerProfile clone = clone();
            clone.complete(true);
            return clone;
        }, Util.PROFILE_EXECUTOR);
    }

    @Override
    public boolean completeFromCache() {
        return completeFromCache(false, GlobalConfiguration.get().proxies.isProxyOnlineMode());
    }

    public boolean completeFromCache(boolean onlineMode) {
        return completeFromCache(false, onlineMode);
    }

    public boolean completeFromCache(boolean lookupUUID, boolean onlineMode) {
        MinecraftServer server = MinecraftServer.getServer();
        String name = profile.getName();
        GameProfileCache userCache = server.getProfileCache();
        if (profile.getId() == null) {
            final GameProfile profile;
            if (onlineMode) {
                profile = lookupUUID ? userCache.get(name).orElse(null) : userCache.getProfileIfCached(name);
            } else {
                // Make an OfflinePlayer using an offline mode UUID since the name has no profile
                profile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name);
            }
            if (profile != null) {
                // if old has it, assume its newer, so overwrite, else use cached if it was set and ours wasn't
                copyProfileProperties(this.profile, profile);
                this.profile = profile;
            }
        }

        if ((profile.getName() == null || !hasTextures()) && profile.getId() != null) {
            Optional<GameProfile> optProfile = userCache.get(this.profile.getId());
            if (optProfile.isPresent()) {
                GameProfile profile = optProfile.get();
                if (this.profile.getName() == null) {
                    // if old has it, assume its newer, so overwrite, else use cached if it was set and ours wasn't
                    copyProfileProperties(this.profile, profile);
                    this.profile = profile;
                } else {
                    copyProfileProperties(profile, this.profile);
                }
            }
        }
        return this.profile.isComplete();
    }

    public boolean complete(boolean textures) {
        return complete(textures, GlobalConfiguration.get().proxies.isProxyOnlineMode());
    }
    public boolean complete(boolean textures, boolean onlineMode) {
        MinecraftServer server = MinecraftServer.getServer();
        boolean isCompleteFromCache = this.completeFromCache(true, onlineMode);
        if (onlineMode && (!isCompleteFromCache || textures && !hasTextures())) {
            GameProfile result = server.getSessionService().fillProfileProperties(profile, true);
            if (result != null) {
                copyProfileProperties(result, this.profile, true);
            }
            if (this.profile.isComplete()) {
                server.getProfileCache().add(this.profile);
            }
        }
        return profile.isComplete() && (!onlineMode || !textures || hasTextures());
    }

    private static void copyProfileProperties(GameProfile source, GameProfile target) {
        copyProfileProperties(source, target, false);
    }

    private static void copyProfileProperties(GameProfile source, GameProfile target, boolean clearTarget) {
        PropertyMap sourceProperties = source.getProperties();
        PropertyMap targetProperties = target.getProperties();
        if (clearTarget) targetProperties.clear();
        if (sourceProperties.isEmpty()) {
            return;
        }

        for (Property property : sourceProperties.values()) {
            targetProperties.removeAll(property.getName());
            targetProperties.put(property.getName(), property);
        }
    }

    private static ProfileProperty toBukkit(Property property) {
        return new ProfileProperty(property.getName(), property.getValue(), property.getSignature());
    }

    public static PlayerProfile asBukkitCopy(GameProfile gameProfile) {
        CraftPlayerProfile profile = new CraftPlayerProfile(gameProfile.getId(), gameProfile.getName());
        copyProfileProperties(gameProfile, profile.profile);
        return profile;
    }

    public static PlayerProfile asBukkitMirror(GameProfile profile) {
        return new CraftPlayerProfile(profile);
    }

    public static Property asAuthlib(ProfileProperty property) {
        return new Property(property.getName(), property.getValue(), property.getSignature());
    }

    public static GameProfile asAuthlibCopy(PlayerProfile profile) {
        CraftPlayerProfile craft = ((CraftPlayerProfile) profile);
        return asAuthlib(craft.clone());
    }

    public static GameProfile asAuthlib(PlayerProfile profile) {
        CraftPlayerProfile craft = ((CraftPlayerProfile) profile);
        return craft.getGameProfile();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (this.getId() != null) {
            map.put("uniqueId", this.getId().toString());
        }
        if (this.getName() != null) {
            map.put("name", getName());
        }
        if (!this.properties.isEmpty()) {
            List<Object> propertiesData = new ArrayList<>();
            for (ProfileProperty property : properties) {
                propertiesData.add(CraftProfileProperty.serialize(new Property(property.getName(), property.getValue(), property.getSignature())));
            }
            map.put("properties", propertiesData);
        }
        return map;
    }

    public static CraftPlayerProfile deserialize(Map<String, Object> map) {
        UUID uniqueId = ConfigSerializationUtil.getUuid(map, "uniqueId", true);
        String name = ConfigSerializationUtil.getString(map, "name", true);

        // This also validates the deserialized unique id and name (ensures that not both are null):
        CraftPlayerProfile profile = new CraftPlayerProfile(uniqueId, name);

        if (map.containsKey("properties")) {
            for (Object propertyData : (List<?>) map.get("properties")) {
                if (!(propertyData instanceof Map)) {
                    throw new IllegalArgumentException("Property data (" + propertyData + ") is not a valid Map");
                }
                Property property = CraftProfileProperty.deserialize((Map<?, ?>) propertyData);
                profile.profile.getProperties().put(property.getName(), property);
            }
        }

        return profile;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CraftPlayerProfile otherProfile)) return false;
        return Objects.equals(this.profile, otherProfile.profile);
    }

    @Override
    public String toString() {
        return "CraftPlayerProfile [uniqueId=" + getId() +
            ", name=" + getName() +
            ", properties=" + org.bukkit.craftbukkit.v1_19_R1.profile.CraftPlayerProfile.toString(this.profile.getProperties()) +
            "]";
    }

    @Override
    public int hashCode() {
        return this.profile.hashCode();
    }

    private class PropertySet extends AbstractSet<ProfileProperty> {

        @Override
        @Nonnull
        public Iterator<ProfileProperty> iterator() {
            return new ProfilePropertyIterator(profile.getProperties().values().iterator());
        }

        @Override
        public int size() {
            return profile.getProperties().size();
        }

        @Override
        public boolean add(ProfileProperty property) {
            setProperty(property);
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends ProfileProperty> c) {
            //noinspection unchecked
            setProperties((Collection<ProfileProperty>) c);
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof ProfileProperty && profile.getProperties().containsKey(((ProfileProperty) o).getName());
        }

        private class ProfilePropertyIterator implements Iterator<ProfileProperty> {
            private final Iterator<Property> iterator;

            ProfilePropertyIterator(Iterator<Property> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ProfileProperty next() {
                return toBukkit(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        }
    }
}
