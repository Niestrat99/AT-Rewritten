package org.bukkit.craftbukkit.v1_19_R1.potion;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class CraftPotionBrewer implements PotionBrewer {
    private static final Map<Integer, Collection<PotionEffect>> cache = Maps.newHashMap(); // Paper

    @Override
    public Collection<PotionEffect> getEffects(PotionType damage, boolean upgraded, boolean extended) {
        // Paper start
        int key = damage.ordinal() << 2;
        key |= (upgraded ? 1 : 0) << 1;
        key |= extended ? 1 : 0;

        if (CraftPotionBrewer.cache.containsKey(key))
            return CraftPotionBrewer.cache.get(key);
        // Paper end

        List<MobEffectInstance> mcEffects = Potion.byName(CraftPotionUtil.fromBukkit(new PotionData(damage, extended, upgraded))).getEffects();

        ImmutableList.Builder<PotionEffect> builder = new ImmutableList.Builder<PotionEffect>();
        for (MobEffectInstance effect : mcEffects) {
            builder.add(CraftPotionUtil.toBukkit(effect));
        }

        CraftPotionBrewer.cache.put(key, builder.build()); // Paper

        return CraftPotionBrewer.cache.get(key); // Paper
    }

    @Override
    public Collection<PotionEffect> getEffectsFromDamage(int damage) {
        return new ArrayList<PotionEffect>();
    }

    @Override
    public PotionEffect createEffect(PotionEffectType potion, int duration, int amplifier) {
        return new PotionEffect(potion, potion.isInstant() ? 1 : (int) (duration * potion.getDurationModifier()), amplifier);
    }

    // Paper start
    @Override
    public void addPotionMix(io.papermc.paper.potion.PotionMix potionMix) {
        net.minecraft.world.item.alchemy.PotionBrewing.addPotionMix(potionMix);
    }

    @Override
    public void removePotionMix(org.bukkit.NamespacedKey key) {
        net.minecraft.world.item.alchemy.PotionBrewing.removePotionMix(key);
    }

    @Override
    public void resetPotionMixes() {
        net.minecraft.world.item.alchemy.PotionBrewing.reload();
    }
    // Paper end
}
