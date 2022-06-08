package org.bukkit.craftbukkit.v1_19_R1.inventory;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeType;
import org.bukkit.inventory.Recipe;

public class RecipeIterator implements Iterator<Recipe> {
    private final Iterator<Map.Entry<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, net.minecraft.world.item.crafting.Recipe<?>>>> recipes;
    private Iterator<net.minecraft.world.item.crafting.Recipe<?>> current;
    private Recipe currentRecipe; // Paper - fix removing recipes

    public RecipeIterator() {
        this.recipes = MinecraftServer.getServer().getRecipeManager().recipes.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
        if (this.current != null && this.current.hasNext()) {
            return true;
        }

        if (this.recipes.hasNext()) {
            this.current = this.recipes.next().getValue().values().iterator();
            return this.hasNext();
        }

        return false;
    }

    @Override
    public Recipe next() {
        if (this.current == null || !this.current.hasNext()) {
            this.current = this.recipes.next().getValue().values().iterator();
            // Paper start - fix removing recipes
            this.currentRecipe = this.next();
            return this.currentRecipe;
            // Paper end
        }

        // Paper start - fix removing recipes
        this.currentRecipe = this.current.next().toBukkitRecipe();
        return this.currentRecipe;
        // Paper end
    }

    @Override
    public void remove() {
        if (this.current == null) {
            throw new IllegalStateException("next() not yet called");
        }

        // Paper start - fix removing recipes
        if (this.currentRecipe instanceof org.bukkit.Keyed keyed) {
            MinecraftServer.getServer().getRecipeManager().byName.remove(org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey.toMinecraft(keyed.getKey()));
        }
        // Paper end
        this.current.remove();
    }
}
