package io.papermc.paper.attribute;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Callables;
import com.google.common.util.concurrent.Runnables;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_19_R1.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v1_19_R1.attribute.CraftAttributeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class UnmodifiableAttributeMap implements Attributable {


    private final Map<Attribute, AttributeInstance> attributes = Maps.newHashMap();
    private final AttributeSupplier handle;

    public UnmodifiableAttributeMap(@NotNull AttributeSupplier handle) {
        this.handle = handle;
    }

    @Override
    public @Nullable AttributeInstance getAttribute(@NotNull Attribute attribute) {
        var nmsAttribute = CraftAttributeMap.toMinecraft(attribute);
        var nmsAttributeInstance = this.handle.instances.get(nmsAttribute);
        if (nmsAttribute == null) {
            return null;
        }
        return new UnmodifiableAttributeInstance(nmsAttributeInstance, attribute);
    }

    @Override
    public void registerAttribute(@NotNull Attribute attribute) {
        throw new UnsupportedOperationException("Cannot register new attributes here");
    }
}
