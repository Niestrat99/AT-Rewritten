package org.bukkit.craftbukkit.v1_19_R1.inventory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.ImmutableSortedMap; // Paper
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator; // Paper
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap; // Paper
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.v1_19_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_19_R1.Overridden;
import org.bukkit.craftbukkit.v1_19_R1.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v1_19_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftMetaItem.ItemMetaKey.Specific;
import org.bukkit.craftbukkit.v1_19_R1.inventory.tags.DeprecatedCustomTagContainer;
import org.bukkit.craftbukkit.v1_19_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_19_R1.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNBTTagConfigSerializer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;

// Spigot start
import static org.spigotmc.ValidateUtils.*;
// Spigot end

// Paper start
import com.destroystokyo.paper.Namespaced;
import com.destroystokyo.paper.NamespacedTag;
import java.util.Collections;
// Paper end

/**
 * Children must include the following:
 *
 * <li> Constructor(CraftMetaItem meta)
 * <li> Constructor(NBTTagCompound tag)
 * <li> Constructor(Map&lt;String, Object&gt; map)
 * <br><br>
 * <li> void applyToItem(NBTTagCompound tag)
 * <li> boolean applicableTo(Material type)
 * <br><br>
 * <li> boolean equalsCommon(CraftMetaItem meta)
 * <li> boolean notUncommon(CraftMetaItem meta)
 * <br><br>
 * <li> boolean isEmpty()
 * <li> boolean is{Type}Empty()
 * <br><br>
 * <li> int applyHash()
 * <li> public Class clone()
 * <br><br>
 * <li> Builder&lt;String, Object&gt; serialize(Builder&lt;String, Object&gt; builder)
 * <li> SerializableMeta.Deserializers deserializer()
 */
@DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
class CraftMetaItem implements ItemMeta, Damageable, Repairable, BlockDataMeta {

    static class ItemMetaKey {

        @Retention(RetentionPolicy.SOURCE)
        @Target(ElementType.FIELD)
        @interface Specific {
            enum To {
                BUKKIT,
                NBT,
                ;
            }
            To value();
        }

        final String BUKKIT;
        final String NBT;

        ItemMetaKey(final String both) {
            this(both, both);
        }

        ItemMetaKey(final String nbt, final String bukkit) {
            this.NBT = nbt;
            this.BUKKIT = bukkit;
        }
    }

    @SerializableAs("ItemMeta")
    public static final class SerializableMeta implements ConfigurationSerializable {
        static final String TYPE_FIELD = "meta-type";

        static final ImmutableMap<Class<? extends CraftMetaItem>, String> classMap;
        static final ImmutableMap<String, Constructor<? extends CraftMetaItem>> constructorMap;

        static {
            classMap = ImmutableMap.<Class<? extends CraftMetaItem>, String>builder()
                    .put(CraftMetaArmorStand.class, "ARMOR_STAND")
                    .put(CraftMetaBanner.class, "BANNER")
                    .put(CraftMetaBlockState.class, "TILE_ENTITY")
                    .put(CraftMetaBook.class, "BOOK")
                    .put(CraftMetaBookSigned.class, "BOOK_SIGNED")
                    .put(CraftMetaSkull.class, "SKULL")
                    .put(CraftMetaLeatherArmor.class, "LEATHER_ARMOR")
                    .put(CraftMetaMap.class, "MAP")
                    .put(CraftMetaPotion.class, "POTION")
                    .put(CraftMetaSpawnEgg.class, "SPAWN_EGG")
                    .put(CraftMetaEnchantedBook.class, "ENCHANTED")
                    .put(CraftMetaFirework.class, "FIREWORK")
                    .put(CraftMetaCharge.class, "FIREWORK_EFFECT")
                    .put(CraftMetaKnowledgeBook.class, "KNOWLEDGE_BOOK")
                    .put(CraftMetaTropicalFishBucket.class, "TROPICAL_FISH_BUCKET")
                    .put(CraftMetaAxolotlBucket.class, "AXOLOTL_BUCKET")
                    .put(CraftMetaCrossbow.class, "CROSSBOW")
                    .put(CraftMetaSuspiciousStew.class, "SUSPICIOUS_STEW")
                    .put(CraftMetaEntityTag.class, "ENTITY_TAG")
                    .put(CraftMetaCompass.class, "COMPASS")
                    .put(CraftMetaBundle.class, "BUNDLE")
                    .put(CraftMetaItem.class, "UNSPECIFIC")
                    .build();

            final ImmutableMap.Builder<String, Constructor<? extends CraftMetaItem>> classConstructorBuilder = ImmutableMap.builder();
            for (Map.Entry<Class<? extends CraftMetaItem>, String> mapping : classMap.entrySet()) {
                try {
                    classConstructorBuilder.put(mapping.getValue(), mapping.getKey().getDeclaredConstructor(Map.class));
                } catch (NoSuchMethodException e) {
                    throw new AssertionError(e);
                }
            }
            constructorMap = classConstructorBuilder.build();
        }

        private SerializableMeta() {
        }

        public static ItemMeta deserialize(Map<String, Object> map) throws Throwable {
            Validate.notNull(map, "Cannot deserialize null map");

            String type = SerializableMeta.getString(map, SerializableMeta.TYPE_FIELD, false);
            Constructor<? extends CraftMetaItem> constructor = SerializableMeta.constructorMap.get(type);

            if (constructor == null) {
                throw new IllegalArgumentException(type + " is not a valid " + SerializableMeta.TYPE_FIELD);
            }

            try {
                return constructor.newInstance(map);
            } catch (final InstantiationException e) {
                throw new AssertionError(e);
            } catch (final IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (final InvocationTargetException e) {
                throw e.getCause();
            }
        }

        @Override
        public Map<String, Object> serialize() {
            throw new AssertionError();
        }

        static String getString(Map<?, ?> map, Object field, boolean nullable) {
            return SerializableMeta.getObject(String.class, map, field, nullable);
        }

        static boolean getBoolean(Map<?, ?> map, Object field) {
            Boolean value = SerializableMeta.getObject(Boolean.class, map, field, true);
            return value != null && value;
        }

        static <T> T getObject(Class<T> clazz, Map<?, ?> map, Object field, boolean nullable) {
            final Object object = map.get(field);

            if (clazz.isInstance(object)) {
                return clazz.cast(object);
            }
            if (object == null) {
                if (!nullable) {
                    throw new NoSuchElementException(map + " does not contain " + field);
                }
                return null;
            }
            throw new IllegalArgumentException(field + "(" + object + ") is not a valid " + clazz);
        }
    }

    static final ItemMetaKey NAME = new ItemMetaKey("Name", "display-name");
    static final ItemMetaKey LOCNAME = new ItemMetaKey("LocName", "loc-name");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey DISPLAY = new ItemMetaKey("display");
    static final ItemMetaKey LORE = new ItemMetaKey("Lore", "lore");
    static final ItemMetaKey CUSTOM_MODEL_DATA = new ItemMetaKey("CustomModelData", "custom-model-data");
    static final ItemMetaKey ENCHANTMENTS = new ItemMetaKey("Enchantments", "enchants");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ENCHANTMENTS_ID = new ItemMetaKey("id");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ENCHANTMENTS_LVL = new ItemMetaKey("lvl");
    static final ItemMetaKey REPAIR = new ItemMetaKey("RepairCost", "repair-cost");
    static final ItemMetaKey ATTRIBUTES = new ItemMetaKey("AttributeModifiers", "attribute-modifiers");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_IDENTIFIER = new ItemMetaKey("AttributeName");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_NAME = new ItemMetaKey("Name");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_VALUE = new ItemMetaKey("Amount");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_TYPE = new ItemMetaKey("Operation");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_UUID_HIGH = new ItemMetaKey("UUIDMost");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_UUID_LOW = new ItemMetaKey("UUIDLeast");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_SLOT = new ItemMetaKey("Slot");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey HIDEFLAGS = new ItemMetaKey("HideFlags", "ItemFlags");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey UNBREAKABLE = new ItemMetaKey("Unbreakable");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey DAMAGE = new ItemMetaKey("Damage");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey BLOCK_DATA = new ItemMetaKey("BlockStateTag");
    static final ItemMetaKey BUKKIT_CUSTOM_TAG = new ItemMetaKey("PublicBukkitValues");
    // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
    static final ItemMetaKey CAN_DESTROY = new ItemMetaKey("CanDestroy");
    static final ItemMetaKey CAN_PLACE_ON = new ItemMetaKey("CanPlaceOn");
    // Paper end

    // We store the raw original JSON representation of all text data. See SPIGOT-5063, SPIGOT-5656, SPIGOT-5304
    private String displayName;
    private String locName;
    private List<String> lore; // null and empty are two different states internally
    private Integer customModelData;
    private CompoundTag blockData;
    private EnchantmentMap enchantments; // Paper
    private Multimap<Attribute, AttributeModifier> attributeModifiers;
    private int repairCost;
    private int hideFlag;
    private boolean unbreakable;
    private int damage;
    // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
    private Set<Namespaced> placeableKeys = Sets.newHashSet();
    private Set<Namespaced> destroyableKeys = Sets.newHashSet();
    // Paper end

    private static final Set<String> HANDLED_TAGS = Sets.newHashSet();
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();

    private CompoundTag internalTag;
    final Map<String, Tag> unhandledTags = new TreeMap<String, Tag>(); // Visible for testing only // Paper
    private CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(CraftMetaItem.DATA_TYPE_REGISTRY);

    private int version = CraftMagicNumbers.INSTANCE.getDataVersion(); // Internal use only

    CraftMetaItem(CraftMetaItem meta) {
        if (meta == null) {
            return;
        }

        this.displayName = meta.displayName;
        this.locName = meta.locName;

        if (meta.lore != null) {
            this.lore = new ArrayList<String>(meta.lore);
        }

        this.customModelData = meta.customModelData;
        this.blockData = meta.blockData;

        if (meta.enchantments != null) { // Spigot
            this.enchantments = new EnchantmentMap(meta.enchantments); // Paper
        }

        if (meta.hasAttributeModifiers()) {
            this.attributeModifiers = LinkedHashMultimap.create(meta.attributeModifiers);
        }

        this.repairCost = meta.repairCost;
        this.hideFlag = meta.hideFlag;
        this.unbreakable = meta.unbreakable;
        this.damage = meta.damage;
        // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
        if (meta.hasPlaceableKeys()) {
            this.placeableKeys = new java.util.HashSet<>(meta.placeableKeys);
        }

        if (meta.hasDestroyableKeys()) {
            this.destroyableKeys = new java.util.HashSet<>(meta.destroyableKeys);
        }
        // Paper end
        this.unhandledTags.putAll(meta.unhandledTags);
        this.persistentDataContainer.putAll(meta.persistentDataContainer.getRaw());

        this.internalTag = meta.internalTag;
        if (this.internalTag != null) {
            this.deserializeInternal(this.internalTag, meta);
        }

        this.version = meta.version;
    }

    CraftMetaItem(CompoundTag tag) {
        if (tag.contains(DISPLAY.NBT)) {
            CompoundTag display = tag.getCompound(DISPLAY.NBT);

            if (display.contains(NAME.NBT)) {
                this.displayName = limit( display.getString(NAME.NBT), io.papermc.paper.configuration.GlobalConfiguration.get().itemValidation.displayName ); // Spigot // Paper - make configurable
            }

            if (display.contains(LOCNAME.NBT)) {
                this.locName = limit( display.getString(LOCNAME.NBT), 8192 ); // Spigot
            }

            if (display.contains(LORE.NBT)) {
                ListTag list = display.getList(LORE.NBT, CraftMagicNumbers.NBT.TAG_STRING);
                this.lore = new ArrayList<String>(list.size());
                for (int index = 0; index < list.size(); index++) {
                    String line = limit( list.getString(index), io.papermc.paper.configuration.GlobalConfiguration.get().itemValidation.loreLine ); // Spigot // Paper - make configurable
                    this.lore.add(line);
                }
            }
        }

        if (tag.contains(CUSTOM_MODEL_DATA.NBT, CraftMagicNumbers.NBT.TAG_INT)) {
            this.customModelData = tag.getInt(CUSTOM_MODEL_DATA.NBT);
        }
        if (tag.contains(BLOCK_DATA.NBT, CraftMagicNumbers.NBT.TAG_COMPOUND)) {
            this.blockData = tag.getCompound(BLOCK_DATA.NBT).copy();
        }

        this.enchantments = CraftMetaItem.buildEnchantments(tag, CraftMetaItem.ENCHANTMENTS);
        this.attributeModifiers = CraftMetaItem.buildModifiers(tag, CraftMetaItem.ATTRIBUTES);

        if (tag.contains(REPAIR.NBT)) {
            this.repairCost = tag.getInt(REPAIR.NBT);
        }

        if (tag.contains(HIDEFLAGS.NBT)) {
            this.hideFlag = tag.getInt(HIDEFLAGS.NBT);
        }
        if (tag.contains(UNBREAKABLE.NBT)) {
            this.unbreakable = tag.getBoolean(UNBREAKABLE.NBT);
        }
        if (tag.contains(DAMAGE.NBT)) {
            this.damage = tag.getInt(DAMAGE.NBT);
        }
        if (tag.contains(BUKKIT_CUSTOM_TAG.NBT)) {
            CompoundTag compound = tag.getCompound(BUKKIT_CUSTOM_TAG.NBT);
            Set<String> keys = compound.getAllKeys();
            for (String key : keys) {
                this.persistentDataContainer.put(key, compound.get(key).copy());
            }
        }
        // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
        if (tag.contains(CAN_DESTROY.NBT)) {
            ListTag list = tag.getList(CAN_DESTROY.NBT, CraftMagicNumbers.NBT.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                Namespaced namespaced = this.deserializeNamespaced(list.getString(i));
                if (namespaced == null) {
                    continue;
                }

                this.destroyableKeys.add(namespaced);
            }
        }

        if (tag.contains(CAN_PLACE_ON.NBT)) {
            ListTag list = tag.getList(CAN_PLACE_ON.NBT, CraftMagicNumbers.NBT.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                Namespaced namespaced = this.deserializeNamespaced(list.getString(i));
                if (namespaced == null) {
                    continue;
                }

                this.placeableKeys.add(namespaced);
            }
        }
        // Paper end

        Set<String> keys = tag.getAllKeys();
        for (String key : keys) {
            if (!CraftMetaItem.getHandledTags().contains(key)) {
                this.unhandledTags.put(key, tag.get(key).copy());
            }
        }
    }

    static EnchantmentMap buildEnchantments(CompoundTag tag, ItemMetaKey key) { // Paper
        if (!tag.contains(key.NBT)) {
            return null;
        }

        ListTag ench = tag.getList(key.NBT, CraftMagicNumbers.NBT.TAG_COMPOUND);
        EnchantmentMap enchantments = new EnchantmentMap(); // Paper

        for (int i = 0; i < ench.size(); i++) {
            String id = ((CompoundTag) ench.get(i)).getString(ENCHANTMENTS_ID.NBT);
            int level = 0xffff & ((CompoundTag) ench.get(i)).getShort(ENCHANTMENTS_LVL.NBT);

            Enchantment enchant = Enchantment.getByKey(CraftNamespacedKey.fromStringOrNull(id));
            if (enchant != null) {
                enchantments.put(enchant, level);
            }
        }

        return enchantments;
    }

    static Multimap<Attribute, AttributeModifier> buildModifiers(CompoundTag tag, ItemMetaKey key) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        if (!tag.contains(key.NBT, CraftMagicNumbers.NBT.TAG_LIST)) {
            return modifiers;
        }
        ListTag mods = tag.getList(key.NBT, CraftMagicNumbers.NBT.TAG_COMPOUND);
        int size = mods.size();

        for (int i = 0; i < size; i++) {
            CompoundTag entry = mods.getCompound(i);
            if (entry.isEmpty()) {
                // entry is not an actual NBTTagCompound. getCompound returns empty NBTTagCompound in that case
                continue;
            }
            net.minecraft.world.entity.ai.attributes.AttributeModifier nmsModifier = net.minecraft.world.entity.ai.attributes.AttributeModifier.load(entry);
            if (nmsModifier == null) {
                continue;
            }

            AttributeModifier attribMod = CraftAttributeInstance.convert(nmsModifier);

            String attributeName = CraftAttributeMap.convertIfNeeded(entry.getString(ATTRIBUTES_IDENTIFIER.NBT)); // Paper
            if (attributeName == null || attributeName.isEmpty()) {
                continue;
            }

            Attribute attribute = CraftAttributeMap.fromMinecraft(attributeName);
            if (attribute == null) {
                continue;
            }

            if (entry.contains(ATTRIBUTES_SLOT.NBT, CraftMagicNumbers.NBT.TAG_STRING)) {
                String slotName = entry.getString(ATTRIBUTES_SLOT.NBT);
                if (slotName == null || slotName.isEmpty()) {
                    modifiers.put(attribute, attribMod);
                    continue;
                }

                EquipmentSlot slot = null;
                try {
                    slot = CraftEquipmentSlot.getSlot(net.minecraft.world.entity.EquipmentSlot.byName(slotName.toLowerCase(Locale.ROOT)));
                } catch (IllegalArgumentException ex) {
                    // SPIGOT-4551 - Slot is invalid, should really match nothing but this is undefined behaviour anyway
                }

                if (slot == null) {
                    modifiers.put(attribute, attribMod);
                    continue;
                }

                attribMod = new AttributeModifier(attribMod.getUniqueId(), attribMod.getName(), attribMod.getAmount(), attribMod.getOperation(), slot);
            }
            modifiers.put(attribute, attribMod);
        }
        return modifiers;
    }

    CraftMetaItem(Map<String, Object> map) {
        this.displayName = CraftChatMessage.fromJSONOrStringOrNullToJSON(SerializableMeta.getString(map, NAME.BUKKIT, true));

        this.locName = CraftChatMessage.fromJSONOrStringOrNullToJSON(SerializableMeta.getString(map, LOCNAME.BUKKIT, true));

        Iterable<?> lore = SerializableMeta.getObject(Iterable.class, map, LORE.BUKKIT, true);
        if (lore != null) {
            CraftMetaItem.safelyAdd(lore, this.lore = new ArrayList<String>(), true);
        }

        Integer customModelData = SerializableMeta.getObject(Integer.class, map, CUSTOM_MODEL_DATA.BUKKIT, true);
        if (customModelData != null) {
            this.setCustomModelData(customModelData);
        }

        Map blockData = SerializableMeta.getObject(Map.class, map, BLOCK_DATA.BUKKIT, true);
        if (blockData != null) {
            this.blockData = (CompoundTag) CraftNBTTagConfigSerializer.deserialize(blockData);
        }

        this.enchantments = CraftMetaItem.buildEnchantments(map, CraftMetaItem.ENCHANTMENTS);
        this.attributeModifiers = CraftMetaItem.buildModifiers(map, CraftMetaItem.ATTRIBUTES);

        Integer repairCost = SerializableMeta.getObject(Integer.class, map, REPAIR.BUKKIT, true);
        if (repairCost != null) {
            this.setRepairCost(repairCost);
        }

        Iterable<?> hideFlags = SerializableMeta.getObject(Iterable.class, map, HIDEFLAGS.BUKKIT, true);
        if (hideFlags != null) {
            for (Object hideFlagObject : hideFlags) {
                String hideFlagString = (String) hideFlagObject;
                try {
                    ItemFlag hideFlatEnum = ItemFlag.valueOf(hideFlagString);
                    this.addItemFlags(hideFlatEnum);
                } catch (IllegalArgumentException ex) {
                    // Ignore when we got a old String which does not map to a Enum value anymore
                }
            }
        }

        Boolean unbreakable = SerializableMeta.getObject(Boolean.class, map, UNBREAKABLE.BUKKIT, true);
        if (unbreakable != null) {
            this.setUnbreakable(unbreakable);
        }

        Integer damage = SerializableMeta.getObject(Integer.class, map, DAMAGE.BUKKIT, true);
        if (damage != null) {
            this.setDamage(damage);
        }

        // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
        Iterable<?> canPlaceOnSerialized = SerializableMeta.getObject(Iterable.class, map, CAN_PLACE_ON.BUKKIT, true);
        if (canPlaceOnSerialized != null) {
            for (Object canPlaceOnElement : canPlaceOnSerialized) {
                String canPlaceOnRaw = (String) canPlaceOnElement;
                Namespaced value = this.deserializeNamespaced(canPlaceOnRaw);
                if (value == null) {
                    continue;
                }

                this.placeableKeys.add(value);
            }
        }

        Iterable<?> canDestroySerialized = SerializableMeta.getObject(Iterable.class, map, CAN_DESTROY.BUKKIT, true);
        if (canDestroySerialized != null) {
            for (Object canDestroyElement : canDestroySerialized) {
                String canDestroyRaw = (String) canDestroyElement;
                Namespaced value = this.deserializeNamespaced(canDestroyRaw);
                if (value == null) {
                    continue;
                }

                this.destroyableKeys.add(value);
            }
        }
        // Paper end

        String internal = SerializableMeta.getString(map, "internal", true);
        if (internal != null) {
            ByteArrayInputStream buf = new ByteArrayInputStream(Base64.getDecoder().decode(internal));
            try {
                this.internalTag = NbtIo.readCompressed(buf);
                this.deserializeInternal(this.internalTag, map);
                Set<String> keys = this.internalTag.getAllKeys();
                for (String key : keys) {
                    if (!CraftMetaItem.getHandledTags().contains(key)) {
                        this.unhandledTags.put(key, this.internalTag.get(key));
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Map nbtMap = SerializableMeta.getObject(Map.class, map, BUKKIT_CUSTOM_TAG.BUKKIT, true);
        if (nbtMap != null) {
            this.persistentDataContainer.putAll((CompoundTag) CraftNBTTagConfigSerializer.deserialize(nbtMap));
        }
    }

    void deserializeInternal(CompoundTag tag, Object context) {
        // SPIGOT-4576: Need to migrate from internal to proper data
        if (tag.contains(ATTRIBUTES.NBT, CraftMagicNumbers.NBT.TAG_LIST)) {
            this.attributeModifiers = CraftMetaItem.buildModifiers(tag, CraftMetaItem.ATTRIBUTES);
        }
    }

    static EnchantmentMap buildEnchantments(Map<String, Object> map, ItemMetaKey key) { // Paper
        Map<?, ?> ench = SerializableMeta.getObject(Map.class, map, key.BUKKIT, true);
        if (ench == null) {
            return null;
        }

        EnchantmentMap enchantments = new EnchantmentMap(); // Paper
        for (Map.Entry<?, ?> entry : ench.entrySet()) {
            // Doctor older enchants
            String enchantKey = entry.getKey().toString();
            if (enchantKey.equals("SWEEPING")) {
                enchantKey = "SWEEPING_EDGE";
            }

            Enchantment enchantment = Enchantment.getByName(enchantKey);
            if ((enchantment != null) && (entry.getValue() instanceof Integer)) {
                enchantments.put(enchantment, (Integer) entry.getValue());
            }
        }

        return enchantments;
    }

    static Multimap<Attribute, AttributeModifier> buildModifiers(Map<String, Object> map, ItemMetaKey key) {
        Map<?, ?> mods = SerializableMeta.getObject(Map.class, map, key.BUKKIT, true);
        Multimap<Attribute, AttributeModifier> result = LinkedHashMultimap.create();
        if (mods == null) {
            return result;
        }

        for (Object obj : mods.keySet()) {
            if (!(obj instanceof String)) {
                continue;
            }
            String attributeName = (String) obj;
            if (Strings.isNullOrEmpty(attributeName)) {
                continue;
            }
            List<?> list = SerializableMeta.getObject(List.class, mods, attributeName, true);
            if (list == null || list.isEmpty()) {
                return result;
            }

            for (Object o : list) {
                if (!(o instanceof AttributeModifier)) { // this catches null
                    continue;
                }
                AttributeModifier modifier = (AttributeModifier) o;
                Attribute attribute = EnumUtils.getEnum(Attribute.class, attributeName.toUpperCase(Locale.ROOT));
                if (attribute == null) {
                    continue;
                }

                result.put(attribute, modifier);
            }
        }
        return result;
    }

    @Overridden
    void applyToItem(CompoundTag itemTag) {
        if (this.hasDisplayName()) {
            this.setDisplayTag(itemTag, NAME.NBT, StringTag.valueOf(displayName));
        }
        if (this.hasLocalizedName()) {
            this.setDisplayTag(itemTag, LOCNAME.NBT, StringTag.valueOf(locName));
        }

        if (this.lore != null) {
            this.setDisplayTag(itemTag, LORE.NBT, this.createStringList(this.lore));
        }

        if (this.hasCustomModelData()) {
            itemTag.putInt(CUSTOM_MODEL_DATA.NBT, customModelData);
        }

        if (this.hasBlockData()) {
            itemTag.put(BLOCK_DATA.NBT, blockData);
        }

        if (this.hideFlag != 0) {
            itemTag.putInt(HIDEFLAGS.NBT, hideFlag);
        }

        CraftMetaItem.applyEnchantments(this.enchantments, itemTag, CraftMetaItem.ENCHANTMENTS);
        CraftMetaItem.applyModifiers(this.attributeModifiers, itemTag, CraftMetaItem.ATTRIBUTES);

        if (this.hasRepairCost()) {
            itemTag.putInt(REPAIR.NBT, repairCost);
        }

        if (this.isUnbreakable()) {
            itemTag.putBoolean(UNBREAKABLE.NBT, unbreakable);
        }

        if (this.hasDamage()) {
            itemTag.putInt(DAMAGE.NBT, damage);
        }
        // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
        if (hasPlaceableKeys()) {
            List<String> items = this.placeableKeys.stream()
                .map(this::serializeNamespaced)
                .collect(java.util.stream.Collectors.toList());

            itemTag.put(CAN_PLACE_ON.NBT, createNonComponentStringList(items));
        }

        if (hasDestroyableKeys()) {
            List<String> items = this.destroyableKeys.stream()
                .map(this::serializeNamespaced)
                .collect(java.util.stream.Collectors.toList());

            itemTag.put(CAN_DESTROY.NBT, createNonComponentStringList(items));
        }
        // Paper end

        for (Map.Entry<String, Tag> e : this.unhandledTags.entrySet()) {
            itemTag.put(e.getKey(), e.getValue());
        }

        if (!this.persistentDataContainer.isEmpty()) {
            CompoundTag bukkitCustomCompound = new CompoundTag();
            Map<String, Tag> rawPublicMap = this.persistentDataContainer.getRaw();

            for (Map.Entry<String, Tag> nbtBaseEntry : rawPublicMap.entrySet()) {
                bukkitCustomCompound.put(nbtBaseEntry.getKey(), nbtBaseEntry.getValue());
            }
            itemTag.put(BUKKIT_CUSTOM_TAG.NBT, bukkitCustomCompound);
        }
    }

    // Paper start
    static ListTag createNonComponentStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        ListTag tagList = new ListTag();
        for (String value : list) {
            tagList.add(StringTag.valueOf(value)); // Paper - NBTTagString.of(String str)
        }

        return tagList;
    }
    // Paper end

    ListTag createStringList(List<String> list) {
        if (list == null) {
            return null;
        }

        ListTag tagList = new ListTag();
        for (String value : list) {
            // SPIGOT-5342 - horrible hack as 0 version does not go through the Mojang updater
            tagList.add(StringTag.valueOf(this.version <= 0 || this.version >= 1803 ? value : CraftChatMessage.fromJSONComponent(value))); // SPIGOT-4935
        }

        return tagList;
    }

    static void applyEnchantments(Map<Enchantment, Integer> enchantments, CompoundTag tag, ItemMetaKey key) {
        if (enchantments == null /*|| enchantments.size() == 0*/) { // Spigot - remove size check
            return;
        }

        ListTag list = new ListTag();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            CompoundTag subtag = new CompoundTag();

            subtag.putString(ENCHANTMENTS_ID.NBT, entry.getKey().getKey().toString());
            subtag.putShort(ENCHANTMENTS_LVL.NBT, entry.getValue().shortValue());

            list.add(subtag);
        }

        tag.put(key.NBT, list);
    }

    static void applyModifiers(Multimap<Attribute, AttributeModifier> modifiers, CompoundTag tag, ItemMetaKey key) {
        if (modifiers == null || modifiers.isEmpty()) {
            return;
        }

        ListTag list = new ListTag();
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            net.minecraft.world.entity.ai.attributes.AttributeModifier nmsModifier = CraftAttributeInstance.convert(entry.getValue());
            CompoundTag sub = nmsModifier.save();
            if (sub.isEmpty()) {
                continue;
            }

            String name = entry.getKey().getKey().toString();
            if (name == null || name.isEmpty()) {
                continue;
            }

            sub.putString(ATTRIBUTES_IDENTIFIER.NBT, name); // Attribute Name
            if (entry.getValue().getSlot() != null) {
                net.minecraft.world.entity.EquipmentSlot slot = CraftEquipmentSlot.getNMS(entry.getValue().getSlot());
                if (slot != null) {
                    sub.putString(ATTRIBUTES_SLOT.NBT, slot.getName());
                }
            }
            list.add(sub);
        }
        tag.put(key.NBT, list);
    }

    void setDisplayTag(CompoundTag tag, String key, Tag value) {
        final CompoundTag display = tag.getCompound(DISPLAY.NBT);

        if (!tag.contains(DISPLAY.NBT)) {
            tag.put(DISPLAY.NBT, display);
        }

        display.put(key, value);
    }

    @Overridden
    boolean applicableTo(Material type) {
        return type != Material.AIR;
    }

    @Overridden
    boolean isEmpty() {
        return !(this.hasDisplayName() || this.hasLocalizedName() || this.hasEnchants() || (this.lore != null) || this.hasCustomModelData() || this.hasBlockData() || this.hasRepairCost() || !this.unhandledTags.isEmpty() || !this.persistentDataContainer.isEmpty() || this.hideFlag != 0 || this.isUnbreakable() || this.hasDamage() || this.hasAttributeModifiers() || this.hasPlaceableKeys() || this.hasDestroyableKeys()); // Paper - Implement an API for CanPlaceOn and CanDestroy NBT values
    }

    // Paper start
    @Override
    public net.kyori.adventure.text.Component displayName() {
        return displayName == null ? null : net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(displayName);
    }

    @Override
    public void displayName(final net.kyori.adventure.text.Component displayName) {
        this.displayName = displayName == null ? null : net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(displayName);
    }
    // Paper end

    @Override
    public String getDisplayName() {
        return CraftChatMessage.fromJSONComponent(displayName);
    }

    // Paper start
    @Override
    public net.md_5.bungee.api.chat.BaseComponent[] getDisplayNameComponent() {
        return displayName == null ? new net.md_5.bungee.api.chat.BaseComponent[0] : net.md_5.bungee.chat.ComponentSerializer.parse(displayName);
    }
    // Paper end
    @Override
    public final void setDisplayName(String name) {
        this.displayName = CraftChatMessage.fromStringOrNullToJSON(name);
    }

    // Paper start
    @Override
    public void setDisplayNameComponent(net.md_5.bungee.api.chat.BaseComponent[] component) {
        this.displayName = net.md_5.bungee.chat.ComponentSerializer.toString(component);
    }
    // Paper end
    @Override
    public boolean hasDisplayName() {
        return this.displayName != null;
    }

    @Override
    public String getLocalizedName() {
        return CraftChatMessage.fromJSONComponent(locName);
    }

    @Override
    public void setLocalizedName(String name) {
        this.locName = CraftChatMessage.fromStringOrNullToJSON(name);
    }

    @Override
    public boolean hasLocalizedName() {
        return this.locName != null;
    }

    @Override
    public boolean hasLore() {
        return this.lore != null && !this.lore.isEmpty();
    }

    // Paper start
    @Override
    public List<net.kyori.adventure.text.Component> lore() {
        return this.lore != null ? io.papermc.paper.adventure.PaperAdventure.asAdventureFromJson(this.lore) : null;
    }

    @Override
    public void lore(final List<net.kyori.adventure.text.Component> lore) {
        this.lore = lore != null ? io.papermc.paper.adventure.PaperAdventure.asJson(lore) : null;
    }
    // Paper end

    @Override
    public boolean hasRepairCost() {
        return this.repairCost > 0;
    }

    @Override
    public boolean hasEnchant(Enchantment ench) {
        Validate.notNull(ench, "Enchantment cannot be null");
        return this.hasEnchants() && this.enchantments.containsKey(ench);
    }

    @Override
    public int getEnchantLevel(Enchantment ench) {
        Validate.notNull(ench, "Enchantment cannot be null");
        Integer level = this.hasEnchants() ? this.enchantments.get(ench) : null;
        if (level == null) {
            return 0;
        }
        return level;
    }

    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return this.hasEnchants() ? ImmutableSortedMap.copyOfSorted(this.enchantments) : ImmutableMap.<Enchantment, Integer>of(); // Paper
    }

    @Override
    public boolean addEnchant(Enchantment ench, int level, boolean ignoreRestrictions) {
        Validate.notNull(ench, "Enchantment cannot be null");
        if (this.enchantments == null) {
            this.enchantments = new EnchantmentMap(); // Paper
        }

        if (ignoreRestrictions || level >= ench.getStartLevel() && level <= ench.getMaxLevel()) {
            Integer old = this.enchantments.put(ench, level);
            return old == null || old != level;
        }
        return false;
    }

    @Override
    public boolean removeEnchant(Enchantment ench) {
        Validate.notNull(ench, "Enchantment cannot be null");
        // Spigot start
        boolean b = this.hasEnchants() && this.enchantments.remove( ench ) != null;
        if ( this.enchantments != null && this.enchantments.isEmpty() )
        {
            this.enchantments = null;
        }
        return b;
        // Spigot end
    }

    @Override
    public boolean hasEnchants() {
        return !(this.enchantments == null || this.enchantments.isEmpty());
    }

    @Override
    public boolean hasConflictingEnchant(Enchantment ench) {
        return CraftMetaItem.checkConflictingEnchants(this.enchantments, ench);
    }

    @Override
    public void addItemFlags(ItemFlag... hideFlags) {
        for (ItemFlag f : hideFlags) {
            this.hideFlag |= this.getBitModifier(f);
        }
    }

    @Override
    public void removeItemFlags(ItemFlag... hideFlags) {
        for (ItemFlag f : hideFlags) {
            this.hideFlag &= ~this.getBitModifier(f);
        }
    }

    @Override
    public Set<ItemFlag> getItemFlags() {
        Set<ItemFlag> currentFlags = EnumSet.noneOf(ItemFlag.class);

        for (ItemFlag f : ItemFlag.values()) {
            if (this.hasItemFlag(f)) {
                currentFlags.add(f);
            }
        }

        return currentFlags;
    }

    @Override
    public boolean hasItemFlag(ItemFlag flag) {
        int bitModifier = this.getBitModifier(flag);
        return (this.hideFlag & bitModifier) == bitModifier;
    }

    private byte getBitModifier(ItemFlag hideFlag) {
        return (byte) (1 << hideFlag.ordinal());
    }

    @Override
    public List<String> getLore() {
        return this.lore == null ? null : new ArrayList<String>(Lists.transform(this.lore, CraftChatMessage::fromJSONComponent));
    }

    // Paper start
    @Override
    public List<net.md_5.bungee.api.chat.BaseComponent[]> getLoreComponents() {
        return this.lore == null ? null : new ArrayList<>(this.lore.stream().map(entry ->
            net.md_5.bungee.chat.ComponentSerializer.parse(entry)
        ).collect(java.util.stream.Collectors.toList()));
    }
    // Paper end
    @Override
    public void setLore(List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            this.lore = null;
        } else {
            if (this.lore == null) {
                this.lore = new ArrayList<String>(lore.size());
            } else {
                this.lore.clear();
            }
            CraftMetaItem.safelyAdd(lore, this.lore, false);
        }
    }

    // Paper start
    @Override
    public void setLoreComponents(List<net.md_5.bungee.api.chat.BaseComponent[]> lore) {
        if (lore == null) {
            this.lore = null;
        } else {
            if (this.lore == null) {
                safelyAdd(lore, this.lore = new ArrayList<>(lore.size()), false);
            } else {
                this.lore.clear();
                safelyAdd(lore, this.lore, false);
            }
        }
    }
    // Paper end
    @Override
    public boolean hasCustomModelData() {
        return this.customModelData != null;
    }

    @Override
    public int getCustomModelData() {
        Preconditions.checkState(this.hasCustomModelData(), "We don't have CustomModelData! Check hasCustomModelData first!");
        return this.customModelData;
    }

    @Override
    public void setCustomModelData(Integer data) {
        this.customModelData = data;
    }

    @Override
    public boolean hasBlockData() {
       return this.blockData != null;
    }

    @Override
    public BlockData getBlockData(Material material) {
        // Paper start - fix NPE if this.blockData is null
        final net.minecraft.world.level.block.state.BlockState defaultBlockState = CraftMagicNumbers.getBlock(material).defaultBlockState();
        return CraftBlockData.fromData(this.blockData == null ? defaultBlockState : BlockItem.getBlockState(defaultBlockState, blockData));
        // Paper end
    }

    @Override
    public void setBlockData(BlockData blockData) {
        this.blockData = (blockData == null) ? null : ((CraftBlockData) blockData).toStates();
    }

    @Override
    public int getRepairCost() {
        return this.repairCost;
    }

    @Override
    public void setRepairCost(int cost) { // TODO: Does this have limits?
        this.repairCost = cost;
    }

    @Override
    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    @Override
    public boolean hasAttributeModifiers() {
        return this.attributeModifiers != null && !this.attributeModifiers.isEmpty();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        return this.hasAttributeModifiers() ? ImmutableMultimap.copyOf(attributeModifiers) : null;
    }

    private void checkAttributeList() {
        if (this.attributeModifiers == null) {
            this.attributeModifiers = LinkedHashMultimap.create();
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nullable EquipmentSlot slot) {
        this.checkAttributeList();
        SetMultimap<Attribute, AttributeModifier> result = LinkedHashMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entries()) {
            if (entry.getValue().getSlot() == null || entry.getValue().getSlot() == slot) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public Collection<AttributeModifier> getAttributeModifiers(@Nonnull Attribute attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        return this.attributeModifiers.containsKey(attribute) ? ImmutableList.copyOf(this.attributeModifiers.get(attribute)) : null;
    }

    @Override
    public boolean addAttributeModifier(@Nonnull Attribute attribute, @Nonnull AttributeModifier modifier) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        Preconditions.checkNotNull(modifier, "AttributeModifier cannot be null");
        this.checkAttributeList();
        for (Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entries()) {
            Preconditions.checkArgument(!entry.getValue().getUniqueId().equals(modifier.getUniqueId()), "Cannot register AttributeModifier. Modifier is already applied! %s", modifier);
        }
        return this.attributeModifiers.put(attribute, modifier);
    }

    @Override
    public void setAttributeModifiers(@Nullable Multimap<Attribute, AttributeModifier> attributeModifiers) {
        if (attributeModifiers == null || attributeModifiers.isEmpty()) {
            this.attributeModifiers = LinkedHashMultimap.create();
            return;
        }

        this.checkAttributeList();
        this.attributeModifiers.clear();

        Iterator<Map.Entry<Attribute, AttributeModifier>> iterator = attributeModifiers.entries().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Attribute, AttributeModifier> next = iterator.next();

            if (next.getKey() == null || next.getValue() == null) {
                iterator.remove();
                continue;
            }
            this.attributeModifiers.put(next.getKey(), next.getValue());
        }
    }

    @Override
    public boolean removeAttributeModifier(@Nonnull Attribute attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        this.checkAttributeList();
        return !this.attributeModifiers.removeAll(attribute).isEmpty();
    }

    @Override
    public boolean removeAttributeModifier(@Nullable EquipmentSlot slot) {
        this.checkAttributeList();
        int removed = 0;
        Iterator<Map.Entry<Attribute, AttributeModifier>> iter = this.attributeModifiers.entries().iterator();

        while (iter.hasNext()) {
            Map.Entry<Attribute, AttributeModifier> entry = iter.next();
            // Explicitly match against null because (as of MC 1.13) AttributeModifiers without a -
            // set slot are active in any slot.
            if (entry.getValue().getSlot() == null || entry.getValue().getSlot() == slot) {
                iter.remove();
                ++removed;
            }
        }
        return removed > 0;
    }

    @Override
    public boolean removeAttributeModifier(@Nonnull Attribute attribute, @Nonnull AttributeModifier modifier) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        Preconditions.checkNotNull(modifier, "AttributeModifier cannot be null");
        this.checkAttributeList();
        int removed = 0;
        Iterator<Map.Entry<Attribute, AttributeModifier>> iter = this.attributeModifiers.entries().iterator();

        while (iter.hasNext()) {
            Map.Entry<Attribute, AttributeModifier> entry = iter.next();
            if (entry.getKey() == null || entry.getValue() == null) {
                iter.remove();
                ++removed;
                continue; // remove all null values while we are here
            }

            if (entry.getKey() == attribute && entry.getValue().getUniqueId().equals(modifier.getUniqueId())) {
                iter.remove();
                ++removed;
            }
        }
        return removed > 0;
    }

    @Override
    public String getAsString() {
        CompoundTag tag = new CompoundTag();
        this.applyToItem(tag);
        return tag.toString();
    }

    @Override
    public CustomItemTagContainer getCustomTagContainer() {
        return new DeprecatedCustomTagContainer(this.getPersistentDataContainer());
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return this.persistentDataContainer;
    }

    private static boolean compareModifiers(Multimap<Attribute, AttributeModifier> first, Multimap<Attribute, AttributeModifier> second) {
        if (first == null || second == null) {
            return false;
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : first.entries()) {
            if (!second.containsEntry(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : second.entries()) {
            if (!first.containsEntry(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasDamage() {
        return this.damage > 0;
    }

    @Override
    public int getDamage() {
        return this.damage;
    }

    @Override
    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public final boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (!(object instanceof CraftMetaItem)) {
            return false;
        }
        return CraftItemFactory.instance().equals(this, (ItemMeta) object);
    }

    /**
     * This method is almost as weird as notUncommon.
     * Only return false if your common internals are unequal.
     * Checking your own internals is redundant if you are not common, as notUncommon is meant for checking those 'not common' variables.
     */
    @Overridden
    boolean equalsCommon(CraftMetaItem that) {
        return ((this.hasDisplayName() ? that.hasDisplayName() && this.displayName.equals(that.displayName) : !that.hasDisplayName()))
                && (this.hasLocalizedName() ? that.hasLocalizedName() && this.locName.equals(that.locName) : !that.hasLocalizedName())
                && (this.hasEnchants() ? that.hasEnchants() && this.enchantments.equals(that.enchantments) : !that.hasEnchants())
                && (Objects.equals(this.lore, that.lore))
                && (this.hasCustomModelData() ? that.hasCustomModelData() && this.customModelData.equals(that.customModelData) : !that.hasCustomModelData())
                && (this.hasBlockData() ? that.hasBlockData() && this.blockData.equals(that.blockData) : !that.hasBlockData())
                && (this.hasRepairCost() ? that.hasRepairCost() && this.repairCost == that.repairCost : !that.hasRepairCost())
                && (this.hasAttributeModifiers() ? that.hasAttributeModifiers() && CraftMetaItem.compareModifiers(this.attributeModifiers, that.attributeModifiers) : !that.hasAttributeModifiers())
                && (this.unhandledTags.equals(that.unhandledTags))
                && (this.persistentDataContainer.equals(that.persistentDataContainer))
                && (this.hideFlag == that.hideFlag)
                && (this.isUnbreakable() == that.isUnbreakable())
                && (this.hasDamage() ? that.hasDamage() && this.damage == that.damage : !that.hasDamage())
                && (this.version == that.version)
                // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
                && (this.hasPlaceableKeys() ? that.hasPlaceableKeys() && this.placeableKeys.equals(that.placeableKeys) : !that.hasPlaceableKeys())
                && (this.hasDestroyableKeys() ? that.hasDestroyableKeys() && this.destroyableKeys.equals(that.destroyableKeys) : !that.hasDestroyableKeys());
                // Paper end
    }

    /**
     * This method is a bit weird...
     * Return true if you are a common class OR your uncommon parts are empty.
     * Empty uncommon parts implies the NBT data would be equivalent if both were applied to an item
     */
    @Overridden
    boolean notUncommon(CraftMetaItem meta) {
        return true;
    }

    @Override
    public final int hashCode() {
        return this.applyHash();
    }

    @Overridden
    int applyHash() {
        int hash = 3;
        hash = 61 * hash + (this.hasDisplayName() ? this.displayName.hashCode() : 0);
        hash = 61 * hash + (this.hasLocalizedName() ? this.locName.hashCode() : 0);
        hash = 61 * hash + ((this.lore != null) ? this.lore.hashCode() : 0);
        hash = 61 * hash + (this.hasCustomModelData() ? this.customModelData.hashCode() : 0);
        hash = 61 * hash + (this.hasBlockData() ? this.blockData.hashCode() : 0);
        hash = 61 * hash + (this.hasEnchants() ? this.enchantments.hashCode() : 0);
        hash = 61 * hash + (this.hasRepairCost() ? this.repairCost : 0);
        hash = 61 * hash + this.unhandledTags.hashCode();
        hash = 61 * hash + (!this.persistentDataContainer.isEmpty() ? this.persistentDataContainer.hashCode() : 0);
        hash = 61 * hash + this.hideFlag;
        hash = 61 * hash + (this.isUnbreakable() ? 1231 : 1237);
        hash = 61 * hash + (this.hasDamage() ? this.damage : 0);
        hash = 61 * hash + (this.hasAttributeModifiers() ? this.attributeModifiers.hashCode() : 0);
        hash = 61 * hash + this.version;
        // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
        hash = 61 * hash + (this.hasPlaceableKeys() ? this.placeableKeys.hashCode() : 0);
        hash = 61 * hash + (this.hasDestroyableKeys() ? this.destroyableKeys.hashCode() : 0);
        // Paper end
        return hash;
    }

    @Overridden
    @Override
    public CraftMetaItem clone() {
        try {
            CraftMetaItem clone = (CraftMetaItem) super.clone();
            if (this.lore != null) {
                clone.lore = new ArrayList<String>(this.lore);
            }
            clone.customModelData = this.customModelData;
            clone.blockData = this.blockData;
            if (this.enchantments != null) {
                clone.enchantments = new EnchantmentMap(this.enchantments); // Paper
            }
            if (this.hasAttributeModifiers()) {
                clone.attributeModifiers = LinkedHashMultimap.create(this.attributeModifiers);
            }
            clone.persistentDataContainer = new CraftPersistentDataContainer(this.persistentDataContainer.getRaw(), CraftMetaItem.DATA_TYPE_REGISTRY);
            clone.hideFlag = this.hideFlag;
            clone.unbreakable = this.unbreakable;
            clone.damage = this.damage;
            clone.version = this.version;
            // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
            if (this.placeableKeys != null) {
                clone.placeableKeys = Sets.newHashSet(this.placeableKeys);
            }
            if (this.destroyableKeys != null) {
                clone.destroyableKeys = Sets.newHashSet(this.destroyableKeys);
            }
            // Paper end
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @Override
    public final Map<String, Object> serialize() {
        ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
        map.put(SerializableMeta.TYPE_FIELD, SerializableMeta.classMap.get(getClass()));
        this.serialize(map);
        return map.build();
    }

    @Overridden
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        if (this.hasDisplayName()) {
            builder.put(NAME.BUKKIT, displayName);
        }
        if (this.hasLocalizedName()) {
            builder.put(LOCNAME.BUKKIT, locName);
        }

        if (this.lore != null) {
            builder.put(LORE.BUKKIT, ImmutableList.copyOf(lore));
        }

        if (this.hasCustomModelData()) {
            builder.put(CUSTOM_MODEL_DATA.BUKKIT, customModelData);
        }
        if (this.hasBlockData()) {
            builder.put(BLOCK_DATA.BUKKIT, CraftNBTTagConfigSerializer.serialize(blockData));
        }

        CraftMetaItem.serializeEnchantments(this.enchantments, builder, CraftMetaItem.ENCHANTMENTS);
        CraftMetaItem.serializeModifiers(this.attributeModifiers, builder, CraftMetaItem.ATTRIBUTES);

        if (this.hasRepairCost()) {
            builder.put(REPAIR.BUKKIT, repairCost);
        }

        List<String> hideFlags = new ArrayList<String>();
        for (ItemFlag hideFlagEnum : this.getItemFlags()) {
            hideFlags.add(hideFlagEnum.name());
        }
        if (!hideFlags.isEmpty()) {
            builder.put(HIDEFLAGS.BUKKIT, hideFlags);
        }

        if (this.isUnbreakable()) {
            builder.put(UNBREAKABLE.BUKKIT, unbreakable);
        }

        if (this.hasDamage()) {
            builder.put(DAMAGE.BUKKIT, damage);
        }

        // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
        if (this.hasPlaceableKeys()) {
            List<String> cerealPlaceable = this.placeableKeys.stream()
                .map(this::serializeNamespaced)
                .collect(java.util.stream.Collectors.toList());

            builder.put(CAN_PLACE_ON.BUKKIT, cerealPlaceable);
        }

        if (this.hasDestroyableKeys()) {
            List<String> cerealDestroyable = this.destroyableKeys.stream()
                .map(this::serializeNamespaced)
                .collect(java.util.stream.Collectors.toList());

            builder.put(CAN_DESTROY.BUKKIT, cerealDestroyable);
        }
        // Paper end
        final Map<String, Tag> internalTags = new HashMap<String, Tag>(this.unhandledTags);
        this.serializeInternal(internalTags);
        if (!internalTags.isEmpty()) {
            CompoundTag internal = new CompoundTag();
            for (Map.Entry<String, Tag> e : internalTags.entrySet()) {
                internal.put(e.getKey(), e.getValue());
            }
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                NbtIo.writeCompressed(internal, buf);
                builder.put("internal", Base64.getEncoder().encodeToString(buf.toByteArray()));
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!this.persistentDataContainer.isEmpty()) { // Store custom tags, wrapped in their compound
            builder.put(BUKKIT_CUSTOM_TAG.BUKKIT, this.persistentDataContainer.serialize());
        }

        return builder;
    }

    void serializeInternal(final Map<String, Tag> unhandledTags) {
    }

    Material updateMaterial(Material material) {
        return material;
    }

    static void serializeEnchantments(Map<Enchantment, Integer> enchantments, ImmutableMap.Builder<String, Object> builder, ItemMetaKey key) {
        if (enchantments == null || enchantments.isEmpty()) {
            return;
        }

        ImmutableMap.Builder<String, Integer> enchants = ImmutableMap.builder();
        for (Map.Entry<? extends Enchantment, Integer> enchant : enchantments.entrySet()) {
            enchants.put(enchant.getKey().getName(), enchant.getValue());
        }

        builder.put(key.BUKKIT, enchants.build());
    }

    static void serializeModifiers(Multimap<Attribute, AttributeModifier> modifiers, ImmutableMap.Builder<String, Object> builder, ItemMetaKey key) {
        if (modifiers == null || modifiers.isEmpty()) {
            return;
        }

        Map<String, List<Object>> mods = new LinkedHashMap<>();
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            if (entry.getKey() == null) {
                continue;
            }
            Collection<AttributeModifier> modCollection = modifiers.get(entry.getKey());
            if (modCollection == null || modCollection.isEmpty()) {
                continue;
            }
            mods.put(entry.getKey().name(), new ArrayList<>(modCollection));
        }
        builder.put(key.BUKKIT, mods);
    }

    static void safelyAdd(Iterable<?> addFrom, Collection<String> addTo, boolean possiblyJsonInput) {
        if (addFrom == null) {
            return;
        }

        for (Object object : addFrom) {
            // Paper start - support components
            if(object instanceof net.md_5.bungee.api.chat.BaseComponent[]) {
                addTo.add(net.md_5.bungee.chat.ComponentSerializer.toString((net.md_5.bungee.api.chat.BaseComponent[]) object));
            } else
            // Paper end
            if (!(object instanceof String)) {
                if (object != null) {
                    throw new IllegalArgumentException(addFrom + " cannot contain non-string " + object.getClass().getName());
                }

                addTo.add(CraftChatMessage.toJSON(Component.empty()));
            } else {
                String entry = object.toString();

                if (possiblyJsonInput) {
                    addTo.add(CraftChatMessage.fromJSONOrStringToJSON(entry));
                } else {
                    addTo.add(CraftChatMessage.fromStringToJSON(entry));
                }
            }
        }
    }

    static boolean checkConflictingEnchants(Map<Enchantment, Integer> enchantments, Enchantment ench) {
        if (enchantments == null || enchantments.isEmpty()) {
            return false;
        }

        for (Enchantment enchant : enchantments.keySet()) {
            if (enchant.conflictsWith(ench)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public final String toString() {
        return SerializableMeta.classMap.get(getClass()) + "_META:" + this.serialize(); // TODO: cry
    }

    public int getVersion() {
        return this.version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    public static Set<String> getHandledTags() {
        synchronized (CraftMetaItem.HANDLED_TAGS) {
            if (CraftMetaItem.HANDLED_TAGS.isEmpty()) {
                CraftMetaItem.HANDLED_TAGS.addAll(Arrays.asList(
                        DISPLAY.NBT,
                        CUSTOM_MODEL_DATA.NBT,
                        BLOCK_DATA.NBT,
                        REPAIR.NBT,
                        ENCHANTMENTS.NBT,
                        HIDEFLAGS.NBT,
                        UNBREAKABLE.NBT,
                        DAMAGE.NBT,
                        BUKKIT_CUSTOM_TAG.NBT,
                        ATTRIBUTES.NBT,
                        ATTRIBUTES_IDENTIFIER.NBT,
                        ATTRIBUTES_NAME.NBT,
                        ATTRIBUTES_VALUE.NBT,
                        ATTRIBUTES_UUID_HIGH.NBT,
                        ATTRIBUTES_UUID_LOW.NBT,
                        ATTRIBUTES_SLOT.NBT,
                        CraftMetaMap.MAP_SCALING.NBT,
                        CraftMetaMap.MAP_COLOR.NBT,
                        CraftMetaMap.MAP_ID.NBT,
                        CraftMetaPotion.POTION_EFFECTS.NBT,
                        CraftMetaPotion.DEFAULT_POTION.NBT,
                        CraftMetaPotion.POTION_COLOR.NBT,
                        CraftMetaSkull.SKULL_OWNER.NBT,
                        CraftMetaSkull.SKULL_PROFILE.NBT,
                        CraftMetaSpawnEgg.ENTITY_TAG.NBT,
                        CraftMetaBlockState.BLOCK_ENTITY_TAG.NBT,
                        CraftMetaBook.BOOK_TITLE.NBT,
                        CraftMetaBook.BOOK_AUTHOR.NBT,
                        CraftMetaBook.BOOK_PAGES.NBT,
                        CraftMetaBook.RESOLVED.NBT,
                        CraftMetaBook.GENERATION.NBT,
                        CraftMetaFirework.FIREWORKS.NBT,
                        CraftMetaEnchantedBook.STORED_ENCHANTMENTS.NBT,
                        CraftMetaCharge.EXPLOSION.NBT,
                        CraftMetaBlockState.BLOCK_ENTITY_TAG.NBT,
                        CraftMetaKnowledgeBook.BOOK_RECIPES.NBT,
                        CraftMetaTropicalFishBucket.VARIANT.NBT,
                        CraftMetaAxolotlBucket.VARIANT.NBT,
                        CraftMetaCrossbow.CHARGED.NBT,
                        CraftMetaCrossbow.CHARGED_PROJECTILES.NBT,
                        CraftMetaSuspiciousStew.EFFECTS.NBT,
                        // Paper start
                        CraftMetaArmorStand.ENTITY_TAG.NBT,
                        CraftMetaArmorStand.INVISIBLE.NBT,
                        CraftMetaArmorStand.NO_BASE_PLATE.NBT,
                        CraftMetaArmorStand.SHOW_ARMS.NBT,
                        CraftMetaArmorStand.SMALL.NBT,
                        CraftMetaArmorStand.MARKER.NBT,
                        CAN_DESTROY.NBT,
                        CAN_PLACE_ON.NBT,
                        // Paper end
                        CraftMetaCompass.LODESTONE_DIMENSION.NBT,
                        CraftMetaCompass.LODESTONE_POS.NBT,
                        CraftMetaCompass.LODESTONE_TRACKED.NBT,
                        CraftMetaBundle.ITEMS.NBT
                ));
            }
            return CraftMetaItem.HANDLED_TAGS;
        }
    }

    // Paper start
    private static class EnchantmentMap extends TreeMap<Enchantment, Integer> {
        private EnchantmentMap(Map<Enchantment, Integer> enchantments) {
            this();
            putAll(enchantments);
        }

        private EnchantmentMap() {
            super(Comparator.comparing(o -> o.getKey().toString()));
        }

        public EnchantmentMap clone() {
            return (EnchantmentMap) super.clone();
        }
    }
    // Paper end

    // Paper start - Implement an API for CanPlaceOn and CanDestroy NBT values
    @Override
    @SuppressWarnings("deprecation")
    public Set<Material> getCanDestroy() {
        return !hasDestroyableKeys() ? Collections.emptySet() : legacyGetMatsFromKeys(this.destroyableKeys);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setCanDestroy(Set<Material> canDestroy) {
        Validate.notNull(canDestroy, "Cannot replace with null set!");
        legacyClearAndReplaceKeys(this.destroyableKeys, canDestroy);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Set<Material> getCanPlaceOn() {
        return !hasPlaceableKeys() ? Collections.emptySet() : legacyGetMatsFromKeys(this.placeableKeys);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setCanPlaceOn(Set<Material> canPlaceOn) {
        Validate.notNull(canPlaceOn, "Cannot replace with null set!");
        legacyClearAndReplaceKeys(this.placeableKeys, canPlaceOn);
    }

    @Override
    public Set<Namespaced> getDestroyableKeys() {
        return !hasDestroyableKeys() ? Collections.emptySet() : Sets.newHashSet(this.destroyableKeys);
    }

    @Override
    public void setDestroyableKeys(Collection<Namespaced> canDestroy) {
        Validate.notNull(canDestroy, "Cannot replace with null collection!");
        Validate.isTrue(ofAcceptableType(canDestroy), "Can only use NamespacedKey or NamespacedTag objects!");
        this.destroyableKeys.clear();
        this.destroyableKeys.addAll(canDestroy);
    }

    @Override
    public Set<Namespaced> getPlaceableKeys() {
        return !hasPlaceableKeys() ? Collections.emptySet() : Sets.newHashSet(this.placeableKeys);
    }

    @Override
    public void setPlaceableKeys(Collection<Namespaced> canPlaceOn) {
        Validate.notNull(canPlaceOn, "Cannot replace with null collection!");
        Validate.isTrue(ofAcceptableType(canPlaceOn), "Can only use NamespacedKey or NamespacedTag objects!");
        this.placeableKeys.clear();
        this.placeableKeys.addAll(canPlaceOn);
    }

    @Override
    public boolean hasPlaceableKeys() {
        return this.placeableKeys != null && !this.placeableKeys.isEmpty();
    }

    @Override
    public boolean hasDestroyableKeys() {
        return this.destroyableKeys != null && !this.destroyableKeys.isEmpty();
    }

    @Deprecated
    private void legacyClearAndReplaceKeys(Collection<Namespaced> toUpdate, Collection<Material> beingSet) {
        if (beingSet.stream().anyMatch(Material::isLegacy)) {
            throw new IllegalArgumentException("Set must not contain any legacy materials!");
        }

        toUpdate.clear();
        toUpdate.addAll(beingSet.stream().map(Material::getKey).collect(java.util.stream.Collectors.toSet()));
    }

    @Deprecated
    private Set<Material> legacyGetMatsFromKeys(Collection<Namespaced> names) {
        Set<Material> mats = Sets.newHashSet();
        for (Namespaced key : names) {
            if (!(key instanceof org.bukkit.NamespacedKey)) {
                continue;
            }

            Material material = Material.matchMaterial(key.toString(), false);
            if (material != null) {
                mats.add(material);
            }
        }

        return mats;
    }

    private @Nullable Namespaced deserializeNamespaced(String raw) {
        boolean isTag = raw.length() > 0 && raw.codePointAt(0) == '#';
        com.mojang.datafixers.util.Either<net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult, net.minecraft.commands.arguments.blocks.BlockStateParser.TagResult> result;
        try {
            result = net.minecraft.commands.arguments.blocks.BlockStateParser.parseForTesting(net.minecraft.core.Registry.BLOCK, raw, false);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            return null;
        }

        net.minecraft.resources.ResourceLocation key = null;
        if (isTag && result.right().isPresent() && result.right().get().tag() instanceof net.minecraft.core.HolderSet.Named<net.minecraft.world.level.block.Block> namedSet) {
            key = namedSet.key().location();
        } else if (result.left().isPresent()) {
            key = net.minecraft.core.Registry.BLOCK.getKey(result.left().get().blockState().getBlock());
        }

        if (key == null) {
            return null;
        }

        // don't DC the player if something slips through somehow
        Namespaced resource = null;
        try {
            if (isTag) {
                resource = new NamespacedTag(key.getNamespace(), key.getPath());
            } else {
                resource = CraftNamespacedKey.fromMinecraft(key);
            }
        } catch (IllegalArgumentException ex) {
            org.bukkit.Bukkit.getLogger().warning("Namespaced resource does not validate: " + key.toString());
            ex.printStackTrace();
        }

        return resource;
    }

    private @Nonnull String serializeNamespaced(Namespaced resource) {
        return resource.toString();
    }

    // not a fan of this
    private boolean ofAcceptableType(Collection<Namespaced> namespacedResources) {
        
        for (Namespaced resource : namespacedResources) {
            if (!(resource instanceof org.bukkit.NamespacedKey || resource instanceof com.destroystokyo.paper.NamespacedTag)) {
                return false;
            }
        }

        return true;
    }
    // Paper end
}
