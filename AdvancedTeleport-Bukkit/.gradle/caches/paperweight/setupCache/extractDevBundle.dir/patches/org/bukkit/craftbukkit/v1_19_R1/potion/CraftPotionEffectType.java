package org.bukkit.craftbukkit.v1_19_R1.potion;

import net.minecraft.world.effect.MobEffect;
import org.bukkit.Color;
import org.bukkit.potion.PotionEffectType;

public class CraftPotionEffectType extends PotionEffectType {
    private final MobEffect handle;

    public CraftPotionEffectType(MobEffect handle) {
        super(MobEffect.getId(handle), org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey.fromMinecraft(net.minecraft.core.Registry.MOB_EFFECT.getKey(handle)));
        this.handle = handle;
    }

    @Override
    public double getDurationModifier() {
        return 1.0D;
    }

    public MobEffect getHandle() {
        return this.handle;
    }

    @Override
    public String getName() {
        switch (getId()) {
        case 1:
            return "SPEED";
        case 2:
            return "SLOW";
        case 3:
            return "FAST_DIGGING";
        case 4:
            return "SLOW_DIGGING";
        case 5:
            return "INCREASE_DAMAGE";
        case 6:
            return "HEAL";
        case 7:
            return "HARM";
        case 8:
            return "JUMP";
        case 9:
            return "CONFUSION";
        case 10:
            return "REGENERATION";
        case 11:
            return "DAMAGE_RESISTANCE";
        case 12:
            return "FIRE_RESISTANCE";
        case 13:
            return "WATER_BREATHING";
        case 14:
            return "INVISIBILITY";
        case 15:
            return "BLINDNESS";
        case 16:
            return "NIGHT_VISION";
        case 17:
            return "HUNGER";
        case 18:
            return "WEAKNESS";
        case 19:
            return "POISON";
        case 20:
            return "WITHER";
        case 21:
            return "HEALTH_BOOST";
        case 22:
            return "ABSORPTION";
        case 23:
            return "SATURATION";
        case 24:
            return "GLOWING";
        case 25:
            return "LEVITATION";
        case 26:
            return "LUCK";
        case 27:
            return "UNLUCK";
        case 28:
            return "SLOW_FALLING";
        case 29:
            return "CONDUIT_POWER";
        case 30:
            return "DOLPHINS_GRACE";
        case 31:
            return "BAD_OMEN";
        case 32:
            return "HERO_OF_THE_VILLAGE";
        case 33:
            return "DARKNESS";
        default:
            return "UNKNOWN_EFFECT_TYPE_" + getId();
        }
    }

    @Override
    public boolean isInstant() {
        return this.handle.isInstantenous();
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(this.handle.getColor());
    }
    // Paper start
    @Override
    public org.bukkit.NamespacedKey getKey() {
        return org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey.fromMinecraft(net.minecraft.core.Registry.MOB_EFFECT.getKey(this.handle));
    }

    @Override
    public java.util.Map<org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier> getEffectAttributes() {
        // re-create map each time because a nms MobEffect can have its attributes modified
        final java.util.Map<org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier> attributeMap = new java.util.HashMap<>();
        this.handle.getAttributeModifiers().forEach((attribute, attributeModifier) -> {
            attributeMap.put(org.bukkit.craftbukkit.v1_19_R1.attribute.CraftAttributeMap.fromMinecraft(attribute.toString()), org.bukkit.craftbukkit.v1_19_R1.attribute.CraftAttributeInstance.convert(attributeModifier));
        });
        return java.util.Map.copyOf(attributeMap);
    }

    @Override
    public double getAttributeModifierAmount(org.bukkit.attribute.Attribute attribute, int effectAmplifier) {
        com.google.common.base.Preconditions.checkArgument(effectAmplifier >= 0, "effectAmplifier must be greater than or equal to 0");
        net.minecraft.world.entity.ai.attributes.Attribute nmsAttribute = org.bukkit.craftbukkit.v1_19_R1.attribute.CraftAttributeMap.toMinecraft(attribute);
        com.google.common.base.Preconditions.checkArgument(this.handle.getAttributeModifiers().containsKey(nmsAttribute), attribute + " is not present on " + this.getKey());
        return this.handle.getAttributeModifierValue(effectAmplifier, this.handle.getAttributeModifiers().get(nmsAttribute));
    }

    @Override
    public PotionEffectType.Category getEffectCategory() {
        return fromNMS(handle.getCategory());
    }

    @Override
    public String translationKey() {
        return this.handle.getDescriptionId();
    }

    public static PotionEffectType.Category fromNMS(net.minecraft.world.effect.MobEffectCategory mobEffectInfo) {
        return switch (mobEffectInfo) {
            case BENEFICIAL -> PotionEffectType.Category.BENEFICIAL;
            case HARMFUL -> PotionEffectType.Category.HARMFUL;
            case NEUTRAL -> PotionEffectType.Category.NEUTRAL;
        };
    }
    // Paper end
}
