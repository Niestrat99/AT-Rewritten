package org.bukkit.craftbukkit.v1_19_R1.profile;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.configuration.ConfigSerializationUtil;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

@SerializableAs("PlayerProfile")
public final class CraftPlayerProfile implements PlayerProfile, com.destroystokyo.paper.profile.SharedPlayerProfile { // Paper

    @Nonnull
    public static GameProfile validateSkullProfile(@Nonnull GameProfile gameProfile) {
        // The GameProfile needs to contain either both a uuid and textures, or a name.
        // The GameProfile always has a name or a uuid, so checking if it has a name is sufficient.
        boolean isValidSkullProfile = (gameProfile.getName() != null)
                || gameProfile.getProperties().containsKey(CraftPlayerTextures.PROPERTY_NAME);
        Preconditions.checkArgument(isValidSkullProfile, "The skull profile is missing a name or textures!");
        return gameProfile;
    }

    @Nullable
    public static Property getProperty(@Nonnull GameProfile profile, String propertyName) {
        return Iterables.getFirst(profile.getProperties().get(propertyName), null);
    }

    private final UUID uniqueId;
    private final String name;

    private final PropertyMap properties = new PropertyMap();
    private final CraftPlayerTextures textures = new CraftPlayerTextures(this);

    public CraftPlayerProfile(UUID uniqueId, String name) {
        Preconditions.checkArgument((uniqueId != null) || !StringUtils.isBlank(name), "uniqueId is null or name is blank");
        this.uniqueId = uniqueId;
        this.name = name;
    }

    // The Map of properties of the given GameProfile is not immutable. This captures a snapshot of the properties of
    // the given GameProfile at the time this CraftPlayerProfile is created.
    public CraftPlayerProfile(@Nonnull GameProfile gameProfile) {
        this(gameProfile.getId(), gameProfile.getName());
        this.properties.putAll(gameProfile.getProperties());
    }

    private CraftPlayerProfile(@Nonnull CraftPlayerProfile other) {
        this(other.uniqueId, other.name);
        this.properties.putAll(other.properties);
        this.textures.copyFrom(other.textures);
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public @Nullable
    Property getProperty(String propertyName) {
        return Iterables.getFirst(this.properties.get(propertyName), null);
    }

    public void setProperty(String propertyName, @Nullable Property property) {
        // Assert: (property == null) || property.getName().equals(propertyName)
        this.removeProperty(propertyName);
        if (property != null) {
            this.properties.put(property.getName(), property);
        }
    }

    // Paper start - change return value for shared interface
    public boolean removeProperty(String propertyName) {
        return !this.properties.removeAll(propertyName).isEmpty();
        // Paper end
    }

    void rebuildDirtyProperties() {
        this.textures.rebuildPropertyIfDirty();
    }

    @Override
    public CraftPlayerTextures getTextures() {
        return this.textures;
    }

    @Override
    public void setTextures(@Nullable PlayerTextures textures) {
        if (textures == null) {
            this.textures.clear();
        } else {
            this.textures.copyFrom(textures);
        }
    }

    @Override
    public boolean isComplete() {
        return (this.uniqueId != null) && (this.name != null) && !this.textures.isEmpty();
    }

    @Override
    public CompletableFuture<PlayerProfile> update() {
        return CompletableFuture.supplyAsync(this::getUpdatedProfile, Util.PROFILE_EXECUTOR); // Paper - not a good idea to use BLOCKING OPERATIONS on the worldgen executor
    }

    private CraftPlayerProfile getUpdatedProfile() {
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile profile = this.buildGameProfile();

        // If missing, look up the uuid by name:
        if (profile.getId() == null) {
            profile = server.getProfileCache().get(profile.getName()).orElse(profile);
        }

        // Look up properties such as the textures:
        if (profile.getId() != null) {
            GameProfile newProfile = server.getSessionService().fillProfileProperties(profile, true);
            if (newProfile != null) {
                profile = newProfile;
            }
        }

        return new CraftPlayerProfile(profile);
    }

    // This always returns a new GameProfile instance to ensure that property changes to the original or previously
    // built GameProfiles don't affect the use of this profile in other contexts.
    @Nonnull
    public GameProfile buildGameProfile() {
        this.rebuildDirtyProperties();
        GameProfile profile = new GameProfile(this.uniqueId, this.name);
        profile.getProperties().putAll(properties);
        return profile;
    }

    @Override
    public String toString() {
        this.rebuildDirtyProperties();
        StringBuilder builder = new StringBuilder();
        builder.append("CraftPlayerProfile [uniqueId=");
        builder.append(uniqueId);
        builder.append(", name=");
        builder.append(name);
        builder.append(", properties=");
        builder.append(CraftPlayerProfile.toString(this.properties));
        builder.append("]");
        return builder.toString();
    }

    public static String toString(@Nonnull PropertyMap propertyMap) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        propertyMap.asMap().forEach((propertyName, properties) -> {
            builder.append(propertyName);
            builder.append("=");
            builder.append(properties.stream().map(CraftProfileProperty::toString).collect(Collectors.joining(",", "[", "]")));
        });
        builder.append("}");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CraftPlayerProfile)) return false;
        CraftPlayerProfile other = (CraftPlayerProfile) obj;
        if (!Objects.equals(uniqueId, other.uniqueId)) return false;
        if (!Objects.equals(name, other.name)) return false;

        this.rebuildDirtyProperties();
        other.rebuildDirtyProperties();
        if (!CraftPlayerProfile.equals(this.properties, other.properties)) return false;
        return true;
    }

    public static boolean equals(@Nonnull PropertyMap propertyMap, @Nonnull PropertyMap other) {
        if (propertyMap.size() != other.size()) return false;
        // We take the order of properties into account here, because it is
        // also relevant in the serialized and NBT forms of GameProfiles.
        Iterator<Property> iterator1 = propertyMap.values().iterator();
        Iterator<Property> iterator2 = other.values().iterator();
        while (iterator1.hasNext()) {
            if (!iterator2.hasNext()) return false;
            Property property1 = iterator1.next();
            Property property2 = iterator2.next();
            if (!CraftProfileProperty.equals(property1, property2)) {
                return false;
            }
        }
        return !iterator2.hasNext();
    }

    @Override
    public int hashCode() {
        this.rebuildDirtyProperties();
        int result = 1;
        result = 31 * result + Objects.hashCode(uniqueId);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + CraftPlayerProfile.hashCode(this.properties);
        return result;
    }

    public static int hashCode(PropertyMap propertyMap) {
        int result = 1;
        for (Property property : propertyMap.values()) {
            result = 31 * result + CraftProfileProperty.hashCode(property);
        }
        return result;
    }

    @Override
    public CraftPlayerProfile clone() {
        return new CraftPlayerProfile(this);
    }

    @Override
    public Map<String, Object> serialize() {
        // Paper - diff on change
        Map<String, Object> map = new LinkedHashMap<>();
        if (this.uniqueId != null) {
            map.put("uniqueId", this.uniqueId.toString());
        }
        if (this.name != null) {
            map.put("name", name);
        }
        this.rebuildDirtyProperties();
        if (!this.properties.isEmpty()) {
            List<Object> propertiesData = new ArrayList<>();
            this.properties.forEach((propertyName, property) -> {
                propertiesData.add(CraftProfileProperty.serialize(property));
            });
            map.put("properties", propertiesData);
        }
        // Paper - diff on change
        return map;
    }

    public static CraftPlayerProfile deserialize(Map<String, Object> map) {
        // Paper - diff on change
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
                profile.properties.put(property.getName(), property);
            }
        }
        // Paper - diff on change
        return profile;
    }
}
