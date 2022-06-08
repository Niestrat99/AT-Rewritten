package io.papermc.paper;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.bukkit.GameEvent;
import org.bukkit.craftbukkit.v1_19_R1.tag.CraftTag;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CraftGameEventTag extends CraftTag<net.minecraft.world.level.gameevent.GameEvent, GameEvent> {

    public CraftGameEventTag(net.minecraft.core.Registry<net.minecraft.world.level.gameevent.GameEvent> registry, TagKey<net.minecraft.world.level.gameevent.GameEvent> tag) {
        super(registry, tag);
    }

    private static final Map<GameEvent, ResourceKey<net.minecraft.world.level.gameevent.GameEvent>> KEY_CACHE = Collections.synchronizedMap(new IdentityHashMap<>());
    @Override
    public boolean isTagged(@NotNull GameEvent gameEvent) {
        return registry.getHolderOrThrow(KEY_CACHE.computeIfAbsent(gameEvent, event -> ResourceKey.create(Registry.GAME_EVENT_REGISTRY, CraftNamespacedKey.toMinecraft(event.getKey())))).is(tag);
    }

    @Override
    public @NotNull Set<GameEvent> getValues() {
        return getHandle().stream().map((nms) -> Objects.requireNonNull(GameEvent.getByKey(CraftNamespacedKey.fromMinecraft(Registry.GAME_EVENT.getKey(nms.value()))), nms + " is not a recognized game event")).collect(Collectors.toUnmodifiableSet());
    }
}
