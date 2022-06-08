package org.bukkit.craftbukkit.v1_19_R1.advancement;

import java.util.Collection;
import java.util.Collections;
import net.minecraft.advancements.Advancement;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;

public class CraftAdvancement implements org.bukkit.advancement.Advancement {

    private final Advancement handle;

    public CraftAdvancement(Advancement handle) {
        this.handle = handle;
    }

    public Advancement getHandle() {
        return this.handle;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(this.handle.getId());
    }

    @Override
    public Collection<String> getCriteria() {
        return Collections.unmodifiableCollection(this.handle.getCriteria().keySet());
    }

    // Paper start
    @Override
    public io.papermc.paper.advancement.AdvancementDisplay getDisplay() {
        return this.handle.getDisplay() == null ? null : this.handle.getDisplay().paper;
    }

    @Override
    public org.bukkit.advancement.Advancement getParent() {
        return this.handle.getParent() == null ? null : this.handle.getParent().bukkit;
    }

    @Override
    public Collection<org.bukkit.advancement.Advancement> getChildren() {
        final var children = com.google.common.collect.ImmutableList.<org.bukkit.advancement.Advancement>builder();
        for (Advancement advancement : this.handle.getChildren()) {
            children.add(advancement.bukkit);
        }
        return children.build();
    }

    @Override
    public org.bukkit.advancement.Advancement getRoot() {
        Advancement advancement = this.handle;
        while (advancement.getParent() != null) {
            advancement = advancement.getParent();
        }
        return advancement.bukkit;
    }
    // Paper end
}
