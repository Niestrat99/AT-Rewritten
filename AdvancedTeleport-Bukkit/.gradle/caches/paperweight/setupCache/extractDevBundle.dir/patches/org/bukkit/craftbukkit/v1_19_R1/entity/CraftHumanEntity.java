package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.memory.CraftMemoryMapper;
import org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryLectern;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftMerchantCustom;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class CraftHumanEntity extends CraftLivingEntity implements HumanEntity {
    private CraftInventoryPlayer inventory;
    private final CraftInventory enderChest;
    protected final PermissibleBase perm = new PermissibleBase(this);
    private boolean op;
    private GameMode mode;

    public CraftHumanEntity(final CraftServer server, final Player entity) {
        super(server, entity);
        this.mode = server.getDefaultGameMode();
        this.inventory = new CraftInventoryPlayer(entity.getInventory());
        this.enderChest = new CraftInventory(entity.getEnderChestInventory());
    }

    @Override
    public PlayerInventory getInventory() {
        return this.inventory;
    }

    @Override
    public EntityEquipment getEquipment() {
        return this.inventory;
    }

    @Override
    public Inventory getEnderChest() {
        return this.enderChest;
    }

    @Override
    public MainHand getMainHand() {
        return this.getHandle().getMainArm() == HumanoidArm.LEFT ? MainHand.LEFT : MainHand.RIGHT;
    }

    @Override
    public ItemStack getItemInHand() {
        return this.getInventory().getItemInHand();
    }

    @Override
    public void setItemInHand(ItemStack item) {
        this.getInventory().setItemInHand(item);
    }

    @Override
    public ItemStack getItemOnCursor() {
        return CraftItemStack.asCraftMirror(this.getHandle().containerMenu.getCarried());
    }

    @Override
    public void setItemOnCursor(ItemStack item) {
        net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
        this.getHandle().containerMenu.setCarried(stack);
        if (this instanceof CraftPlayer) {
            this.getHandle().containerMenu.broadcastCarriedItem(); // Send set slot for cursor
        }
    }

    // Paper start
    @Override
    public boolean isDeeplySleeping() {
        return getHandle().isSleepingLongEnough();
    }
    // Paper end

    @Override
    public int getSleepTicks() {
        return this.getHandle().sleepCounter;
    }

    // Paper start - Potential bed api
    @Override
    public Location getPotentialBedLocation() {
        ServerPlayer handle = (ServerPlayer) getHandle();
        BlockPos bed = handle.getRespawnPosition();
        if (bed == null) {
            return null;
        }

        ServerLevel worldServer = handle.server.getLevel(handle.getRespawnDimension());
        if (worldServer == null) {
            return null;
        }
        return new Location(worldServer.getWorld(), bed.getX(), bed.getY(), bed.getZ());
    }
    // Paper end
    // Paper start
    @Override
    public org.bukkit.entity.FishHook getFishHook() {
        if (getHandle().fishing == null) {
            return null;
        }
        return (org.bukkit.entity.FishHook) getHandle().fishing.getBukkitEntity();
    }
    // Paper end
    @Override
    public boolean sleep(Location location, boolean force) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "Location needs to be in a world");
        Preconditions.checkArgument(location.getWorld().equals(getWorld()), "Cannot sleep across worlds");

        BlockPos blockposition = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockState iblockdata = this.getHandle().level.getBlockState(blockposition);
        if (!(iblockdata.getBlock() instanceof BedBlock)) {
            return false;
        }

        if (this.getHandle().startSleepInBed(blockposition, force).left().isPresent()) {
            return false;
        }

        // From BlockBed
        iblockdata = (BlockState) iblockdata.setValue(BedBlock.OCCUPIED, true);
        this.getHandle().level.setBlock(blockposition, iblockdata, 4);

        return true;
    }

    @Override
    public void wakeup(boolean setSpawnLocation) {
        Preconditions.checkState(isSleeping(), "Cannot wakeup if not sleeping");

        this.getHandle().stopSleepInBed(true, setSpawnLocation);
    }

    @Override
    public Location getBedLocation() {
        Preconditions.checkState(isSleeping(), "Not sleeping");

        BlockPos bed = this.getHandle().getSleepingPos().get();
        return new Location(getWorld(), bed.getX(), bed.getY(), bed.getZ());
    }

    @Override
    public String getName() {
        return this.getHandle().getScoreboardName();
    }

    @Override
    public boolean isOp() {
        return this.op;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return this.perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return this.perm.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return this.perm.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return this.perm.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return this.perm.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return this.perm.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return this.perm.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return this.perm.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        this.perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        this.perm.recalculatePermissions();
    }

    @Override
    public void setOp(boolean value) {
        this.op = value;
        this.perm.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return this.perm.getEffectivePermissions();
    }

    @Override
    public GameMode getGameMode() {
        return this.mode;
    }

    @Override
    public void setGameMode(GameMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Mode cannot be null");
        }

        this.mode = mode;
    }

    @Override
    public Player getHandle() {
        return (Player) entity;
    }

    public void setHandle(final Player entity) {
        super.setHandle(entity);
        this.inventory = new CraftInventoryPlayer(entity.getInventory());
    }

    @Override
    public String toString() {
        return "CraftHumanEntity{" + "id=" + getEntityId() + "name=" + this.getName() + '}';
    }

    @Override
    public InventoryView getOpenInventory() {
        return this.getHandle().containerMenu.getBukkitView();
    }

    @Override
    public InventoryView openInventory(Inventory inventory) {
        if (!(this.getHandle() instanceof ServerPlayer)) return null;
        ServerPlayer player = (ServerPlayer) this.getHandle();
        AbstractContainerMenu formerContainer = this.getHandle().containerMenu;

        MenuProvider iinventory = null;
        if (inventory instanceof CraftInventoryDoubleChest) {
            iinventory = ((CraftInventoryDoubleChest) inventory).tile;
        } else if (inventory instanceof CraftInventoryLectern) {
            iinventory = ((CraftInventoryLectern) inventory).tile;
        } else if (inventory instanceof CraftInventory) {
            CraftInventory craft = (CraftInventory) inventory;
            if (craft.getInventory() instanceof MenuProvider) {
                iinventory = (MenuProvider) craft.getInventory();
            }
        }

        if (iinventory instanceof MenuProvider) {
            if (iinventory instanceof BlockEntity) {
                BlockEntity te = (BlockEntity) iinventory;
                if (!te.hasLevel()) {
                    te.setLevel(this.getHandle().level);
                }
            }
        }

        MenuType<?> container = CraftContainer.getNotchInventoryType(inventory);
        if (iinventory instanceof MenuProvider) {
            this.getHandle().openMenu(iinventory);
        } else {
            CraftHumanEntity.openCustomInventory(inventory, player, container);
        }

        if (this.getHandle().containerMenu == formerContainer) {
            return null;
        }
        this.getHandle().containerMenu.checkReachable = false;
        return this.getHandle().containerMenu.getBukkitView();
    }

    private static void openCustomInventory(Inventory inventory, ServerPlayer player, MenuType<?> windowType) {
        if (player.connection == null) return;
        Preconditions.checkArgument(windowType != null, "Unknown windowType");
        AbstractContainerMenu container = new CraftContainer(inventory, player, player.nextContainerCounter());

        container = CraftEventFactory.callInventoryOpenEvent(player, container);
        if (container == null) return;

        //String title = container.getBukkitView().getTitle(); // Paper - comment
        net.kyori.adventure.text.Component adventure$title = container.getBukkitView().title(); // Paper
        if (adventure$title == null) adventure$title = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(container.getBukkitView().getTitle()); // Paper

        //player.connection.send(new ClientboundOpenScreenPacket(container.containerId, windowType, CraftChatMessage.fromString(title)[0])); // Paper - comment
        if (!player.isImmobile()) player.connection.send(new ClientboundOpenScreenPacket(container.containerId, windowType, io.papermc.paper.adventure.PaperAdventure.asVanilla(adventure$title))); // Paper
        player.containerMenu = container;
        player.initMenu(container);
    }

    @Override
    public InventoryView openWorkbench(Location location, boolean force) {
        if (location == null) {
            location = getLocation();
        }
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != Material.CRAFTING_TABLE) {
                return null;
            }
        }
        this.getHandle().openMenu(((CraftingTableBlock) Blocks.CRAFTING_TABLE).getMenuProvider(null, this.getHandle().level, new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
        if (force) {
            this.getHandle().containerMenu.checkReachable = false;
        }
        return this.getHandle().containerMenu.getBukkitView();
    }

    @Override
    public InventoryView openEnchanting(Location location, boolean force) {
        if (location == null) {
            location = getLocation();
        }
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != Material.ENCHANTING_TABLE) {
                return null;
            }
        }

        // If there isn't an enchant table we can force create one, won't be very useful though.
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.getHandle().openMenu(((EnchantmentTableBlock) Blocks.ENCHANTING_TABLE).getMenuProvider(null, this.getHandle().level, pos));

        if (force) {
            this.getHandle().containerMenu.checkReachable = false;
        }
        return this.getHandle().containerMenu.getBukkitView();
    }

    @Override
    public void openInventory(InventoryView inventory) {
        if (!(this.getHandle() instanceof ServerPlayer)) return; // TODO: NPC support?
        if (((ServerPlayer) this.getHandle()).connection == null) return;
        if (this.getHandle().containerMenu != this.getHandle().inventoryMenu) {
            // fire INVENTORY_CLOSE if one already open
            ((ServerPlayer) this.getHandle()).connection.handleContainerClose(new ServerboundContainerClosePacket(this.getHandle().containerMenu.containerId), org.bukkit.event.inventory.InventoryCloseEvent.Reason.OPEN_NEW); // Paper
        }
        ServerPlayer player = (ServerPlayer) this.getHandle();
        AbstractContainerMenu container;
        if (inventory instanceof CraftInventoryView) {
            container = ((CraftInventoryView) inventory).getHandle();
        } else {
            container = new CraftContainer(inventory, this.getHandle(), player.nextContainerCounter());
        }

        // Trigger an INVENTORY_OPEN event
        container = CraftEventFactory.callInventoryOpenEvent(player, container);
        if (container == null) {
            return;
        }

        // Now open the window
        MenuType<?> windowType = CraftContainer.getNotchInventoryType(inventory.getTopInventory());

        //String title = inventory.getTitle(); // Paper - comment
        net.kyori.adventure.text.Component adventure$title = inventory.title(); // Paper
        if (adventure$title == null) adventure$title = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(inventory.getTitle()); // Paper
        //player.connection.send(new ClientboundOpenScreenPacket(container.containerId, windowType, CraftChatMessage.fromString(title)[0])); // Paper - comment
        if (!player.isImmobile()) player.connection.send(new ClientboundOpenScreenPacket(container.containerId, windowType, io.papermc.paper.adventure.PaperAdventure.asVanilla(adventure$title))); // Paper
        player.containerMenu = container;
        player.initMenu(container);
    }

    @Override
    public InventoryView openMerchant(Villager villager, boolean force) {
        Preconditions.checkNotNull(villager, "villager cannot be null");

        return this.openMerchant((Merchant) villager, force);
    }

    @Override
    public InventoryView openMerchant(Merchant merchant, boolean force) {
        Preconditions.checkNotNull(merchant, "merchant cannot be null");

        if (!force && merchant.isTrading()) {
            return null;
        } else if (merchant.isTrading()) {
            // we're not supposed to have multiple people using the same merchant, so we have to close it.
            merchant.getTrader().closeInventory();
        }

        net.minecraft.world.item.trading.Merchant mcMerchant;
        Component name;
        int level = 1; // note: using level 0 with active 'is-regular-villager'-flag allows hiding the name suffix
        if (merchant instanceof CraftAbstractVillager) {
            mcMerchant = ((CraftAbstractVillager) merchant).getHandle();
            name = ((CraftAbstractVillager) merchant).getHandle().getDisplayName();
            if (merchant instanceof CraftVillager) {
                level = ((CraftVillager) merchant).getHandle().getVillagerData().getLevel();
            }
        } else if (merchant instanceof CraftMerchantCustom) {
            mcMerchant = ((CraftMerchantCustom) merchant).getMerchant();
            name = ((CraftMerchantCustom) merchant).getMerchant().getScoreboardDisplayName();
        } else {
            throw new IllegalArgumentException("Can't open merchant " + merchant.toString());
        }

        mcMerchant.setTradingPlayer(this.getHandle());
        mcMerchant.openTradingScreen(this.getHandle(), name, level);

        return this.getHandle().containerMenu.getBukkitView();
    }

    // Paper start - Add additional containers
    @Override
    public InventoryView openAnvil(Location location, boolean force) {
        return openInventory(location, force, Material.ANVIL);
    }

    @Override
    public InventoryView openCartographyTable(Location location, boolean force) {
        return openInventory(location, force, Material.CARTOGRAPHY_TABLE);
    }

    @Override
    public InventoryView openGrindstone(Location location, boolean force) {
        return openInventory(location, force, Material.GRINDSTONE);
    }

    @Override
    public InventoryView openLoom(Location location, boolean force) {
        return openInventory(location, force, Material.LOOM);
    }

    @Override
    public InventoryView openSmithingTable(Location location, boolean force) {
        return openInventory(location, force, Material.SMITHING_TABLE);
    }

    @Override
    public InventoryView openStonecutter(Location location, boolean force) {
        return openInventory(location, force, Material.STONECUTTER);
    }

    private InventoryView openInventory(Location location, boolean force, Material material) {
        org.spigotmc.AsyncCatcher.catchOp("open" + material);
        if (location == null) {
            location = getLocation();
        }
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != material) {
                return null;
            }
        }
        net.minecraft.world.level.block.Block block;
        if (material == Material.ANVIL) {
            block = Blocks.ANVIL;
        } else if (material == Material.CARTOGRAPHY_TABLE) {
            block = Blocks.CARTOGRAPHY_TABLE;
        } else if (material == Material.GRINDSTONE) {
            block = Blocks.GRINDSTONE;
        } else if (material == Material.LOOM) {
            block = Blocks.LOOM;
        } else if (material == Material.SMITHING_TABLE) {
            block = Blocks.SMITHING_TABLE;
        } else if (material == Material.STONECUTTER) {
            block = Blocks.STONECUTTER;
        } else {
            throw new IllegalArgumentException("Unsupported inventory type: " + material);
        }
        getHandle().openMenu(block.getMenuProvider(null, getHandle().level, new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
        getHandle().containerMenu.checkReachable = !force;
        return getHandle().containerMenu.getBukkitView();
    }
    // Paper end

    @Override
    public void closeInventory() {
        // Paper start
        this.getHandle().closeContainer(org.bukkit.event.inventory.InventoryCloseEvent.Reason.PLUGIN);
    }
    @Override
    public void closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason reason) {
        getHandle().closeContainer(reason);
    }
    // Paper end

    @Override
    public boolean isBlocking() {
        return this.getHandle().isBlocking();
    }

    @Override
    public boolean isHandRaised() {
        return this.getHandle().isUsingItem();
    }

    @Override
    public ItemStack getItemInUse() {
        net.minecraft.world.item.ItemStack item = this.getHandle().getUseItem();
        return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
    }

    @Override
    public boolean setWindowProperty(InventoryView.Property prop, int value) {
        return false;
    }

    @Override
    public int getExpToLevel() {
        return this.getHandle().getXpNeededForNextLevel();
    }

    @Override
    public float getAttackCooldown() {
        return this.getHandle().getAttackStrengthScale(0.5f);
    }

    @Override
    public boolean hasCooldown(Material material) {
        Preconditions.checkArgument(material != null, "Material cannot be null");
        Preconditions.checkArgument(material.isItem(), "Material %s is not an item", material);

        return this.getHandle().getCooldowns().isOnCooldown(CraftMagicNumbers.getItem(material));
    }

    @Override
    public int getCooldown(Material material) {
        Preconditions.checkArgument(material != null, "Material cannot be null");
        Preconditions.checkArgument(material.isItem(), "Material %s is not an item", material);

        ItemCooldowns.CooldownInstance cooldown = this.getHandle().getCooldowns().cooldowns.get(CraftMagicNumbers.getItem(material));
        return (cooldown == null) ? 0 : Math.max(0, cooldown.endTime - this.getHandle().getCooldowns().tickCount);
    }

    @Override
    public void setCooldown(Material material, int ticks) {
        Preconditions.checkArgument(material != null, "Material cannot be null");
        Preconditions.checkArgument(material.isItem(), "Material %s is not an item", material);
        Preconditions.checkArgument(ticks >= 0, "Cannot have negative cooldown");

        this.getHandle().getCooldowns().addCooldown(CraftMagicNumbers.getItem(material), ticks);
    }

    // Paper start
    @Override
    public org.bukkit.entity.Entity releaseLeftShoulderEntity() {
        if (!getHandle().getShoulderEntityLeft().isEmpty()) {
            Entity entity = getHandle().releaseLeftShoulderEntity();
            if (entity != null) {
                return entity.getBukkitEntity();
            }
        }

        return null;
    }

    @Override
    public org.bukkit.entity.Entity releaseRightShoulderEntity() {
        if (!getHandle().getShoulderEntityRight().isEmpty()) {
            Entity entity = getHandle().releaseRightShoulderEntity();
            if (entity != null) {
                return entity.getBukkitEntity();
            }
        }

        return null;
    }
    // Paper end

    @Override
    public boolean discoverRecipe(NamespacedKey recipe) {
        return this.discoverRecipes(Arrays.asList(recipe)) != 0;
    }

    @Override
    public int discoverRecipes(Collection<NamespacedKey> recipes) {
        return this.getHandle().awardRecipes(this.bukkitKeysToMinecraftRecipes(recipes));
    }

    @Override
    public boolean undiscoverRecipe(NamespacedKey recipe) {
        return this.undiscoverRecipes(Arrays.asList(recipe)) != 0;
    }

    @Override
    public int undiscoverRecipes(Collection<NamespacedKey> recipes) {
        return this.getHandle().resetRecipes(this.bukkitKeysToMinecraftRecipes(recipes));
    }

    @Override
    public boolean hasDiscoveredRecipe(NamespacedKey recipe) {
        return false;
    }

    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        return ImmutableSet.of();
    }

    private Collection<Recipe<?>> bukkitKeysToMinecraftRecipes(Collection<NamespacedKey> recipeKeys) {
        Collection<Recipe<?>> recipes = new ArrayList<>();
        RecipeManager manager = this.getHandle().level.getServer().getRecipeManager();

        for (NamespacedKey recipeKey : recipeKeys) {
            Optional<? extends Recipe<?>> recipe = manager.byKey(CraftNamespacedKey.toMinecraft(recipeKey));
            if (!recipe.isPresent()) {
                continue;
            }

            recipes.add(recipe.get());
        }

        return recipes;
    }

    @Override
    public org.bukkit.entity.Entity getShoulderEntityLeft() {
        if (!this.getHandle().getShoulderEntityLeft().isEmpty()) {
            Optional<Entity> shoulder = EntityType.create(this.getHandle().getShoulderEntityLeft(), this.getHandle().level);

            return (!shoulder.isPresent()) ? null : shoulder.get().getBukkitEntity();
        }

        return null;
    }

    @Override
    public void setShoulderEntityLeft(org.bukkit.entity.Entity entity) {
        this.getHandle().setShoulderEntityLeft(entity == null ? new CompoundTag() : ((CraftEntity) entity).save());
        if (entity != null) {
            entity.remove();
        }
    }

    @Override
    public org.bukkit.entity.Entity getShoulderEntityRight() {
        if (!this.getHandle().getShoulderEntityRight().isEmpty()) {
            Optional<Entity> shoulder = EntityType.create(this.getHandle().getShoulderEntityRight(), this.getHandle().level);

            return (!shoulder.isPresent()) ? null : shoulder.get().getBukkitEntity();
        }

        return null;
    }

    @Override
    public void setShoulderEntityRight(org.bukkit.entity.Entity entity) {
        this.getHandle().setShoulderEntityRight(entity == null ? new CompoundTag() : ((CraftEntity) entity).save());
        if (entity != null) {
            entity.remove();
        }
    }

    // Paper start - move open sign method to HumanEntity
    @Override
    public void openSign(org.bukkit.block.Sign sign) {
        org.bukkit.craftbukkit.v1_19_R1.block.CraftSign.openSign(sign, this);
    }
    // Paper end
    @Override
    public boolean dropItem(boolean dropAll) {
        if (!(this.getHandle() instanceof ServerPlayer)) return false;
        return ((ServerPlayer) this.getHandle()).drop(dropAll);
    }

    @Override
    public float getExhaustion() {
        return this.getHandle().getFoodData().exhaustionLevel;
    }

    @Override
    public void setExhaustion(float value) {
        this.getHandle().getFoodData().exhaustionLevel = value;
    }

    @Override
    public float getSaturation() {
        return this.getHandle().getFoodData().saturationLevel;
    }

    @Override
    public void setSaturation(float value) {
        this.getHandle().getFoodData().saturationLevel = value;
    }

    @Override
    public int getFoodLevel() {
        return this.getHandle().getFoodData().foodLevel;
    }

    @Override
    public void setFoodLevel(int value) {
        this.getHandle().getFoodData().foodLevel = value;
    }

    @Override
    public int getSaturatedRegenRate() {
        return this.getHandle().getFoodData().saturatedRegenRate;
    }

    @Override
    public void setSaturatedRegenRate(int i) {
        this.getHandle().getFoodData().saturatedRegenRate = i;
    }

    @Override
    public int getUnsaturatedRegenRate() {
        return this.getHandle().getFoodData().unsaturatedRegenRate;
    }

    @Override
    public void setUnsaturatedRegenRate(int i) {
        this.getHandle().getFoodData().unsaturatedRegenRate = i;
    }

    @Override
    public int getStarvationRate() {
        return this.getHandle().getFoodData().starvationRate;
    }

    @Override
    public void setStarvationRate(int i) {
        this.getHandle().getFoodData().starvationRate = i;
    }

    @Override
    public Location getLastDeathLocation() {
        return this.getHandle().getLastDeathLocation().map(CraftMemoryMapper::fromNms).orElse(null);
    }

    @Override
    public void setLastDeathLocation(Location location) {
        if (location == null) {
            this.getHandle().setLastDeathLocation(Optional.empty());
        } else {
            this.getHandle().setLastDeathLocation(Optional.of(CraftMemoryMapper.toNms(location)));
        }
    }

    @Override
    public Firework fireworkBoost(ItemStack fireworkItemStack) {
        Preconditions.checkArgument(fireworkItemStack != null, "fireworkItemStack must not be null");
        Preconditions.checkArgument(fireworkItemStack.getType() == Material.FIREWORK_ROCKET, "fireworkItemStack must be of type %s", Material.FIREWORK_ROCKET);

        FireworkRocketEntity fireworks = new FireworkRocketEntity(this.getHandle().level, CraftItemStack.asNMSCopy(fireworkItemStack), this.getHandle());
        boolean success = this.getHandle().level.addFreshEntity(fireworks, SpawnReason.CUSTOM);
        return success ? (Firework) fireworks.getBukkitEntity() : null;
    }
}
