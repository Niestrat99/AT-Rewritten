package org.bukkit.craftbukkit.v1_19_R1.enchantments;

import net.minecraft.core.Registry;
import net.minecraft.world.item.enchantment.BindingCurseEnchantment;
import net.minecraft.world.item.enchantment.VanishingCurseEnchantment;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

public class CraftEnchantment extends Enchantment {
    private final net.minecraft.world.item.enchantment.Enchantment target;

    public CraftEnchantment(net.minecraft.world.item.enchantment.Enchantment target) {
        super(CraftNamespacedKey.fromMinecraft(Registry.ENCHANTMENT.getKey(target)));
        this.target = target;
    }

    @Override
    public int getMaxLevel() {
        return this.target.getMaxLevel();
    }

    @Override
    public int getStartLevel() {
        return this.target.getMinLevel();
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        switch (target.category) {
        case ARMOR:
            return EnchantmentTarget.ARMOR;
        case ARMOR_FEET:
            return EnchantmentTarget.ARMOR_FEET;
        case ARMOR_HEAD:
            return EnchantmentTarget.ARMOR_HEAD;
        case ARMOR_LEGS:
            return EnchantmentTarget.ARMOR_LEGS;
        case ARMOR_CHEST:
            return EnchantmentTarget.ARMOR_TORSO;
        case DIGGER:
            return EnchantmentTarget.TOOL;
        case WEAPON:
            return EnchantmentTarget.WEAPON;
        case BOW:
            return EnchantmentTarget.BOW;
        case FISHING_ROD:
            return EnchantmentTarget.FISHING_ROD;
        case BREAKABLE:
            return EnchantmentTarget.BREAKABLE;
        case WEARABLE:
            return EnchantmentTarget.WEARABLE;
        case TRIDENT:
            return EnchantmentTarget.TRIDENT;
        case CROSSBOW:
            return EnchantmentTarget.CROSSBOW;
        case VANISHABLE:
            return EnchantmentTarget.VANISHABLE;
        default:
            return null;
        }
    }

    @Override
    public boolean isTreasure() {
        return this.target.isTreasureOnly();
    }

    @Override
    public boolean isCursed() {
        return this.target.isCurse(); // Paper
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return this.target.canEnchant(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public String getName() {
        // PAIL: migration paths
        switch (Registry.ENCHANTMENT.getId(target)) {
        case 0:
            return "PROTECTION_ENVIRONMENTAL";
        case 1:
            return "PROTECTION_FIRE";
        case 2:
            return "PROTECTION_FALL";
        case 3:
            return "PROTECTION_EXPLOSIONS";
        case 4:
            return "PROTECTION_PROJECTILE";
        case 5:
            return "OXYGEN";
        case 6:
            return "WATER_WORKER";
        case 7:
            return "THORNS";
        case 8:
            return "DEPTH_STRIDER";
        case 9:
            return "FROST_WALKER";
        case 10:
            return "BINDING_CURSE";
        case 11:
            return "SOUL_SPEED";
        case 12:
            return "SWIFT_SNEAK";
        case 13:
            return "DAMAGE_ALL";
        case 14:
            return "DAMAGE_UNDEAD";
        case 15:
            return "DAMAGE_ARTHROPODS";
        case 16:
            return "KNOCKBACK";
        case 17:
            return "FIRE_ASPECT";
        case 18:
            return "LOOT_BONUS_MOBS";
        case 19:
            return "SWEEPING_EDGE";
        case 20:
            return "DIG_SPEED";
        case 21:
            return "SILK_TOUCH";
        case 22:
            return "DURABILITY";
        case 23:
            return "LOOT_BONUS_BLOCKS";
        case 24:
            return "ARROW_DAMAGE";
        case 25:
            return "ARROW_KNOCKBACK";
        case 26:
            return "ARROW_FIRE";
        case 27:
            return "ARROW_INFINITE";
        case 28:
            return "LUCK";
        case 29:
            return "LURE";
        case 30:
            return "LOYALTY";
        case 31:
            return "IMPALING";
        case 32:
            return "RIPTIDE";
        case 33:
            return "CHANNELING";
        case 34:
            return "MULTISHOT";
        case 35:
            return "QUICK_CHARGE";
        case 36:
            return "PIERCING";
        case 37:
            return "MENDING";
        case 38:
            return "VANISHING_CURSE";
        default:
            return "UNKNOWN_ENCHANT_" + Registry.ENCHANTMENT.getId(target);
        }
    }

    public static net.minecraft.world.item.enchantment.Enchantment getRaw(Enchantment enchantment) {
        if (enchantment instanceof EnchantmentWrapper) {
            enchantment = ((EnchantmentWrapper) enchantment).getEnchantment();
        }

        if (enchantment instanceof CraftEnchantment) {
            return ((CraftEnchantment) enchantment).target;
        }

        return null;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        if (other instanceof EnchantmentWrapper) {
            other = ((EnchantmentWrapper) other).getEnchantment();
        }
        if (!(other instanceof CraftEnchantment)) {
            return false;
        }
        CraftEnchantment ench = (CraftEnchantment) other;
        return !this.target.isCompatibleWith(ench.target);
    }
    // Paper start
    @Override
    public net.kyori.adventure.text.Component displayName(int level) {
        return io.papermc.paper.adventure.PaperAdventure.asAdventure(getHandle().getFullname(level));
    }

    @Override
    public String translationKey() {
        return this.target.getDescriptionId();
    }

    @Override
    public boolean isTradeable() {
        return target.isTradeable();
    }

    @Override
    public boolean isDiscoverable() {
        return target.isDiscoverable();
    }

    @Override
    public io.papermc.paper.enchantments.EnchantmentRarity getRarity() {
        return fromNMSRarity(target.getRarity());
    }

    @Override
    public float getDamageIncrease(int level, org.bukkit.entity.EntityCategory entityCategory) {
        return target.getDamageBonus(level, org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity.fromBukkitEntityCategory(entityCategory));
    }

    @Override
    public java.util.Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
        return java.util.stream.Stream.of(target.slots).map(org.bukkit.craftbukkit.v1_19_R1.CraftEquipmentSlot::getSlot).collect(java.util.stream.Collectors.toSet());
    }

    public static io.papermc.paper.enchantments.EnchantmentRarity fromNMSRarity(net.minecraft.world.item.enchantment.Enchantment.Rarity nmsRarity) {
        if (nmsRarity == net.minecraft.world.item.enchantment.Enchantment.Rarity.COMMON) {
            return io.papermc.paper.enchantments.EnchantmentRarity.COMMON;
        } else if (nmsRarity == net.minecraft.world.item.enchantment.Enchantment.Rarity.UNCOMMON) {
            return io.papermc.paper.enchantments.EnchantmentRarity.UNCOMMON;
        } else if (nmsRarity == net.minecraft.world.item.enchantment.Enchantment.Rarity.RARE) {
            return io.papermc.paper.enchantments.EnchantmentRarity.RARE;
        } else if (nmsRarity == net.minecraft.world.item.enchantment.Enchantment.Rarity.VERY_RARE) {
            return io.papermc.paper.enchantments.EnchantmentRarity.VERY_RARE;
        }

        throw new IllegalArgumentException(String.format("Unable to convert %s to a enum value of %s.", nmsRarity, io.papermc.paper.enchantments.EnchantmentRarity.class));
    }
    // Paper end

    public net.minecraft.world.item.enchantment.Enchantment getHandle() {
        return this.target;
    }
}
