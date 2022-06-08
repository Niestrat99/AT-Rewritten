package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.destroystokyo.paper.Title;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang.Validate;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ManuallyAbandonedConversationCanceller;
import org.bukkit.craftbukkit.v1_19_R1.CraftEffect;
import org.bukkit.craftbukkit.v1_19_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_19_R1.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftSound;
import org.bukkit.craftbukkit.v1_19_R1.CraftStatistic;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorldBorder;
import org.bukkit.craftbukkit.v1_19_R1.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.v1_19_R1.advancement.CraftAdvancementProgress;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.conversations.ConversationTracker;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.map.CraftMapView;
import org.bukkit.craftbukkit.v1_19_R1.map.RenderData;
import org.bukkit.craftbukkit.v1_19_R1.profile.CraftPlayerProfile;
import org.bukkit.craftbukkit.v1_19_R1.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerHideEntityEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerShowEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerUnregisterChannelEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.chat.BaseComponent; // Spigot

@DelegateDeserialization(CraftOfflinePlayer.class)
public class CraftPlayer extends CraftHumanEntity implements Player {
    private long firstPlayed = 0;
    private long lastPlayed = 0;
    private boolean hasPlayedBefore = false;
    private final ConversationTracker conversationTracker = new ConversationTracker();
    private final Set<String> channels = new HashSet<String>();
    private final Map<UUID, Set<WeakReference<Plugin>>> hiddenEntities = new HashMap<>();
    private static final WeakHashMap<Plugin, WeakReference<Plugin>> pluginWeakReferences = new WeakHashMap<>();
    private int hash = 0;
    private double health = 20;
    private boolean scaledHealth = false;
    private double healthScale = 20;
    private CraftWorldBorder clientWorldBorder = null;
    private BorderChangeListener clientWorldBorderListener = this.createWorldBorderListener();
    // Paper start
    private org.bukkit.event.player.PlayerResourcePackStatusEvent.Status resourcePackStatus;
    private String resourcePackHash;
    private static final boolean DISABLE_CHANNEL_LIMIT = System.getProperty("paper.disableChannelLimit") != null; // Paper - add a flag to disable the channel limit
    private long lastSaveTime;
    // Paper end

    public CraftPlayer(CraftServer server, ServerPlayer entity) {
        super(server, entity);

        this.firstPlayed = System.currentTimeMillis();
    }

    // Paper start - implement view distances
    @Override
    public int getViewDistance() {
        net.minecraft.server.level.ChunkMap chunkMap = this.getHandle().getLevel().getChunkSource().chunkMap;
        io.papermc.paper.chunk.PlayerChunkLoader.PlayerLoaderData data = chunkMap.playerChunkManager.getData(this.getHandle());
        if (data == null) {
            return chunkMap.playerChunkManager.getTargetNoTickViewDistance();
        }
        return data.getTargetNoTickViewDistance();
    }

    @Override
    public void setViewDistance(int viewDistance) {
        net.minecraft.server.level.ChunkMap chunkMap = this.getHandle().getLevel().getChunkSource().chunkMap;
        io.papermc.paper.chunk.PlayerChunkLoader.PlayerLoaderData data = chunkMap.playerChunkManager.getData(this.getHandle());
        if (data == null) {
            throw new IllegalStateException("Player is not attached to world");
        }

        data.setTargetNoTickViewDistance(viewDistance);
    }

    @Override
    public int getSimulationDistance() {
        net.minecraft.server.level.ChunkMap chunkMap = this.getHandle().getLevel().getChunkSource().chunkMap;
        io.papermc.paper.chunk.PlayerChunkLoader.PlayerLoaderData data = chunkMap.playerChunkManager.getData(this.getHandle());
        if (data == null) {
            return chunkMap.playerChunkManager.getTargetTickViewDistance();
        }
        return data.getTargetTickViewDistance();
    }

    @Override
    public void setSimulationDistance(int simulationDistance) {
        net.minecraft.server.level.ChunkMap chunkMap = this.getHandle().getLevel().getChunkSource().chunkMap;
        io.papermc.paper.chunk.PlayerChunkLoader.PlayerLoaderData data = chunkMap.playerChunkManager.getData(this.getHandle());
        if (data == null) {
            throw new IllegalStateException("Player is not attached to world");
        }

        data.setTargetTickViewDistance(simulationDistance);
    }

    @Override
    public int getNoTickViewDistance() {
        return this.getViewDistance();
    }

    @Override
    public void setNoTickViewDistance(int viewDistance) {
        this.setViewDistance(viewDistance);
    }

    @Override
    public int getSendViewDistance() {
        net.minecraft.server.level.ChunkMap chunkMap = this.getHandle().getLevel().getChunkSource().chunkMap;
        io.papermc.paper.chunk.PlayerChunkLoader.PlayerLoaderData data = chunkMap.playerChunkManager.getData(this.getHandle());
        if (data == null) {
            return chunkMap.playerChunkManager.getTargetSendDistance();
        }
        return data.getTargetSendViewDistance();
    }

    @Override
    public void setSendViewDistance(int viewDistance) {
        net.minecraft.server.level.ChunkMap chunkMap = this.getHandle().getLevel().getChunkSource().chunkMap;
        io.papermc.paper.chunk.PlayerChunkLoader.PlayerLoaderData data = chunkMap.playerChunkManager.getData(this.getHandle());
        if (data == null) {
            throw new IllegalStateException("Player is not attached to world");
        }

        data.setTargetSendViewDistance(viewDistance);
    }
    // Paper end - implement view distances

    public GameProfile getProfile() {
        return this.getHandle().getGameProfile();
    }

    @Override
    public boolean isOp() {
        return server.getHandle().isOp(this.getProfile());
    }

    @Override
    public void setOp(boolean value) {
        if (value == this.isOp()) return;

        if (value) {
            server.getHandle().op(this.getProfile());
        } else {
            server.getHandle().deop(this.getProfile());
        }

        perm.recalculatePermissions();
    }

    @Override
    public boolean isOnline() {
        return server.getPlayer(getUniqueId()) != null;
    }

    @Override
    public InetSocketAddress getAddress() {
        if (this.getHandle().connection == null) return null;

        SocketAddress addr = this.getHandle().connection.connection.getRemoteAddress();
        if (addr instanceof InetSocketAddress) {
            return (InetSocketAddress) addr;
        } else {
            return null;
        }
    }

    // Paper start - Implement NetworkClient
    @Override
    public int getProtocolVersion() {
        if (getHandle().connection == null) return -1;
        return getHandle().connection.connection.protocolVersion;
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        if (getHandle().connection == null) return null;
        return getHandle().connection.connection.virtualHost;
    }
    // Paper end

    @Override
    public double getEyeHeight(boolean ignorePose) {
        if (ignorePose) {
            return 1.62D;
        } else {
            return getEyeHeight();
        }
    }

    @Override
    public void sendRawMessage(String message) {
        if (this.getHandle().connection == null) return;

        for (Component component : CraftChatMessage.fromString(message)) {
            this.getHandle().sendSystemMessage(component);
        }
    }

    @Override
    public void sendRawMessage(UUID sender, String message) {
        if (this.getHandle().connection == null) return;

        for (Component component : CraftChatMessage.fromString(message)) {
            this.getHandle().sendSystemMessage(component);
        }
    }

    @Override
    public void sendMessage(String message) {
        if (!this.conversationTracker.isConversingModaly()) {
            this.sendRawMessage(message);
        }
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            this.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(UUID sender, String message) {
        if (!this.conversationTracker.isConversingModaly()) {
            this.sendRawMessage(sender, message);
        }
    }

    @Override
    public void sendMessage(UUID sender, String... messages) {
        for (String message : messages) {
            this.sendMessage(sender, message);
        }
    }

    // Paper start
    @Override
    public void sendActionBar(BaseComponent[] message) {
        if (getHandle().connection == null) return;
        net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket packet = new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket((net.minecraft.network.chat.Component) null);
        packet.components = message;
        getHandle().connection.send(packet);
    }

    @Override
    public void sendActionBar(String message) {
        if (getHandle().connection == null || message == null || message.isEmpty()) return;
        getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(CraftChatMessage.fromStringOrNull(message)));
    }

    @Override
    public void sendActionBar(char alternateChar, String message) {
        if (message == null || message.isEmpty()) return;
        sendActionBar(org.bukkit.ChatColor.translateAlternateColorCodes(alternateChar, message));
    }

    @Override
    public void setPlayerListHeaderFooter(BaseComponent[] header, BaseComponent[] footer) {
         if (header != null) {
             String headerJson = net.md_5.bungee.chat.ComponentSerializer.toString(header);
             playerListHeader = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(headerJson);
         } else {
             playerListHeader = null;
         }

        if (footer != null) {
             String footerJson = net.md_5.bungee.chat.ComponentSerializer.toString(footer);
             playerListFooter = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(footerJson);
        } else {
             playerListFooter = null;
         }

         updatePlayerListHeaderFooter();
    }

    @Override
    public void setPlayerListHeaderFooter(BaseComponent header, BaseComponent footer) {
        this.setPlayerListHeaderFooter(header == null ? null : new BaseComponent[]{header},
                footer == null ? null : new BaseComponent[]{footer});
    }


    @Override
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        getHandle().connection.send(new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
    }

    @Override
    public void setSubtitle(BaseComponent[] subtitle) {
        final ClientboundSetSubtitleTextPacket packet = new ClientboundSetSubtitleTextPacket((net.minecraft.network.chat.Component) null);
        packet.components = subtitle;
        getHandle().connection.send(packet);
    }

    @Override
    public void setSubtitle(BaseComponent subtitle) {
        setSubtitle(new BaseComponent[]{subtitle});
    }

    @Override
    public void showTitle(BaseComponent[] title) {
        final ClientboundSetTitleTextPacket packet = new ClientboundSetTitleTextPacket((net.minecraft.network.chat.Component) null);
        packet.components = title;
        getHandle().connection.send(packet);
    }

    @Override
    public void showTitle(BaseComponent title) {
        showTitle(new BaseComponent[]{title});
    }

    @Override
    public void showTitle(BaseComponent[] title, BaseComponent[] subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        setTitleTimes(fadeInTicks, stayTicks, fadeOutTicks);
        setSubtitle(subtitle);
        showTitle(title);
    }

    @Override
    public void showTitle(BaseComponent title, BaseComponent subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        setTitleTimes(fadeInTicks, stayTicks, fadeOutTicks);
        setSubtitle(subtitle);
        showTitle(title);
    }

    @Override
    public void sendTitle(Title title) {
        Preconditions.checkNotNull(title, "Title is null");
        setTitleTimes(title.getFadeIn(), title.getStay(), title.getFadeOut());
        setSubtitle(title.getSubtitle() == null ? new BaseComponent[0] : title.getSubtitle());
        showTitle(title.getTitle());
    }

    @Override
    public void updateTitle(Title title) {
        Preconditions.checkNotNull(title, "Title is null");
        setTitleTimes(title.getFadeIn(), title.getStay(), title.getFadeOut());
        if (title.getSubtitle() != null) {
            setSubtitle(title.getSubtitle());
        }
        showTitle(title.getTitle());
    }

    @Override
    public void hideTitle() {
        getHandle().connection.send(new ClientboundClearTitlesPacket(false));
    }
    // Paper end

    @Override
    public String getDisplayName() {
        if(true) return io.papermc.paper.adventure.DisplayNames.getLegacy(this); // Paper
        return this.getHandle().displayName;
    }

    @Override
    public void setDisplayName(final String name) {
        this.getHandle().adventure$displayName = name != null ? net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(name) : net.kyori.adventure.text.Component.text(this.getName()); // Paper
        this.getHandle().displayName = name == null ? getName() : name;
    }

    // Paper start
    @Override
    public void playerListName(net.kyori.adventure.text.Component name) {
        getHandle().listName = name == null ? null : io.papermc.paper.adventure.PaperAdventure.asVanilla(name);
        for (ServerPlayer player : server.getHandle().players) {
            if (player.getBukkitEntity().canSee(this)) {
                player.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME, getHandle()));
            }
        }
    }
    @Override
    public net.kyori.adventure.text.Component playerListName() {
        return getHandle().listName == null ? net.kyori.adventure.text.Component.text(getName()) : io.papermc.paper.adventure.PaperAdventure.asAdventure(getHandle().listName);
    }
    @Override
    public net.kyori.adventure.text.Component playerListHeader() {
        return playerListHeader;
    }
    @Override
    public net.kyori.adventure.text.Component playerListFooter() {
        return playerListFooter;
    }
    // Paper end
    @Override
    public String getPlayerListName() {
        return this.getHandle().listName == null ? getName() : CraftChatMessage.fromComponent(this.getHandle().listName);
    }

    @Override
    public void setPlayerListName(String name) {
        if (name == null) {
            name = getName();
        }
        this.getHandle().listName = name.equals(getName()) ? null : CraftChatMessage.fromStringOrNull(name);
        for (ServerPlayer player : (List<ServerPlayer>) server.getHandle().players) {
            if (player.getBukkitEntity().canSee(this)) {
                player.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME, this.getHandle()));
            }
        }
    }

    private net.kyori.adventure.text.Component playerListHeader; // Paper - Adventure
    private net.kyori.adventure.text.Component playerListFooter; // Paper - Adventure

    @Override
    public String getPlayerListHeader() {
        return (this.playerListHeader == null) ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(playerListHeader);
    }

    @Override
    public String getPlayerListFooter() {
        return (this.playerListFooter == null) ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(playerListFooter); // Paper - Adventure
    }

    @Override
    public void setPlayerListHeader(String header) {
        this.playerListHeader = header == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(header); // Paper - Adventure
        this.updatePlayerListHeaderFooter();
    }

    @Override
    public void setPlayerListFooter(String footer) {
        this.playerListFooter = footer == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(footer); // Paper - Adventure
        this.updatePlayerListHeaderFooter();
    }

    @Override
    public void setPlayerListHeaderFooter(String header, String footer) {
        this.playerListHeader = header == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(header); // Paper - Adventure
        this.playerListFooter = footer == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(footer); // Paper - Adventure
        this.updatePlayerListHeaderFooter();
    }

    private void updatePlayerListHeaderFooter() {
        if (this.getHandle().connection == null) return;

        ClientboundTabListPacket packet = new ClientboundTabListPacket((this.playerListHeader == null) ? Component.empty() : io.papermc.paper.adventure.PaperAdventure.asVanilla(this.playerListHeader), (this.playerListFooter == null) ? Component.empty() : io.papermc.paper.adventure.PaperAdventure.asVanilla(this.playerListFooter)); // Paper - adventure
        this.getHandle().connection.send(packet);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OfflinePlayer)) {
            return false;
        }
        OfflinePlayer other = (OfflinePlayer) obj;
        if ((this.getUniqueId() == null) || (other.getUniqueId() == null)) {
            return false;
        }

        boolean uuidEquals = this.getUniqueId().equals(other.getUniqueId());
        boolean idEquals = true;

        if (other instanceof CraftPlayer) {
            idEquals = this.getEntityId() == ((CraftPlayer) other).getEntityId();
        }

        return uuidEquals && idEquals;
    }

    @Override
    public void kickPlayer(String message) {
        org.spigotmc.AsyncCatcher.catchOp("player kick"); // Spigot
        if (this.getHandle().connection == null) return;

        this.getHandle().connection.disconnect(message == null ? "" : message, org.bukkit.event.player.PlayerKickEvent.Cause.PLUGIN); // Paper - kick event cause
    }

    // Paper start
    private static final net.kyori.adventure.text.Component DEFAULT_KICK_COMPONENT = net.kyori.adventure.text.Component.translatable("multiplayer.disconnect.kicked");
    @Override
    public void kick() {
        this.kick(DEFAULT_KICK_COMPONENT);
    }

    @Override
    public void kick(final net.kyori.adventure.text.Component message) {
        kick(message, org.bukkit.event.player.PlayerKickEvent.Cause.PLUGIN);
    }

    @Override
    public void kick(net.kyori.adventure.text.Component message, org.bukkit.event.player.PlayerKickEvent.Cause cause) {
        org.spigotmc.AsyncCatcher.catchOp("player kick");
        final ServerGamePacketListenerImpl connection = this.getHandle().connection;
        if (connection != null) {
            connection.disconnect(message == null ? net.kyori.adventure.text.Component.empty() : message, cause);
        }
    }

    @Override
    public <T> T getClientOption(com.destroystokyo.paper.ClientOption<T> type) {
        if (com.destroystokyo.paper.ClientOption.SKIN_PARTS == type) {
            return type.getType().cast(new com.destroystokyo.paper.PaperSkinParts(getHandle().getEntityData().get(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION)));
        } else if (com.destroystokyo.paper.ClientOption.CHAT_COLORS_ENABLED == type) {
            return type.getType().cast(getHandle().canChatInColor());
        } else if (com.destroystokyo.paper.ClientOption.CHAT_VISIBILITY == type) {
            return type.getType().cast(getHandle().getChatVisibility() == null ? com.destroystokyo.paper.ClientOption.ChatVisibility.UNKNOWN : com.destroystokyo.paper.ClientOption.ChatVisibility.valueOf(getHandle().getChatVisibility().name()));
        } else if (com.destroystokyo.paper.ClientOption.LOCALE == type) {
            return type.getType().cast(getLocale());
        } else if (com.destroystokyo.paper.ClientOption.MAIN_HAND == type) {
            return type.getType().cast(getMainHand());
        } else if (com.destroystokyo.paper.ClientOption.VIEW_DISTANCE == type) {
            return type.getType().cast(getClientViewDistance());
        } else if (com.destroystokyo.paper.ClientOption.ALLOW_SERVER_LISTINGS == type) {
            return type.getType().cast(getHandle().allowsListing());
        } else if (com.destroystokyo.paper.ClientOption.TEXT_FILTERING_ENABLED == type) {
            return type.getType().cast(getHandle().isTextFilteringEnabled());
        }
        throw new RuntimeException("Unknown settings type");
    }

    @Override
    public org.bukkit.entity.Firework boostElytra(ItemStack firework) {
        Validate.isTrue(isGliding(), "Player must be gliding");
        Validate.isTrue(firework != null, "firework == null");
        Validate.isTrue(firework.getType() == Material.FIREWORK_ROCKET, "Firework must be Material.FIREWORK_ROCKET");

        net.minecraft.world.item.ItemStack item = org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack.asNMSCopy(firework);
        net.minecraft.world.level.Level world = ((CraftWorld) getWorld()).getHandle();
        net.minecraft.world.entity.projectile.FireworkRocketEntity entity = new net.minecraft.world.entity.projectile.FireworkRocketEntity(world, item, getHandle());
        return world.addFreshEntity(entity)
            ? (org.bukkit.entity.Firework) entity.getBukkitEntity()
            : null;
    }

    @Override
    public void sendOpLevel(byte level) {
        Preconditions.checkArgument(level >= 0 && level <= 4, "Level must be within [0, 4]");

        this.getHandle().getServer().getPlayerList().sendPlayerPermissionLevel(this.getHandle(), level, false);
    }

    @Override
    public void addAdditionalChatCompletions(@NotNull Collection<String> completions) {
        this.getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket(
            net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket.Action.ADD,
            new ArrayList<>(completions)
        ));
    }

    @Override
    public void removeAdditionalChatCompletions(@NotNull Collection<String> completions) {
        this.getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket(
            net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket.Action.REMOVE,
            new ArrayList<>(completions)
        ));
    }
    // Paper end

    @Override
    public void setCompassTarget(Location loc) {
        if (this.getHandle().connection == null) return;

        // Do not directly assign here, from the packethandler we'll assign it.
        this.getHandle().connection.send(new ClientboundSetDefaultSpawnPositionPacket(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), loc.getYaw()));
    }

    @Override
    public Location getCompassTarget() {
        return this.getHandle().compassTarget;
    }

    @Override
    public void chat(String msg) {
        if (this.getHandle().connection == null) return;

        // Paper start - improve chat handling
        if (ServerGamePacketListenerImpl.isChatMessageIllegal(msg)) {
            this.getHandle().connection.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"), org.bukkit.event.player.PlayerKickEvent.Cause.ILLEGAL_CHARACTERS);
        } else {
            if (msg.startsWith("/")) {
                this.getHandle().connection.handleCommand(msg);
            } else {
                // TODO text filtering
                // TODO chat decorating
                this.getHandle().connection.chat(msg, PlayerChatMessage.system(new net.minecraft.network.chat.ChatMessageContent(msg)), false);
            }
        }
        // Paper end
    }

    @Override
    public boolean performCommand(String command) {
        return server.dispatchCommand(this, command);
    }

    @Override
    public void playNote(Location loc, byte instrument, byte note) {
        if (this.getHandle().connection == null) return;

        String instrumentName = null;
        switch (instrument) {
        case 0:
            instrumentName = "harp";
            break;
        case 1:
            instrumentName = "basedrum";
            break;
        case 2:
            instrumentName = "snare";
            break;
        case 3:
            instrumentName = "hat";
            break;
        case 4:
            instrumentName = "bass";
            break;
        case 5:
            instrumentName = "flute";
            break;
        case 6:
            instrumentName = "bell";
            break;
        case 7:
            instrumentName = "guitar";
            break;
        case 8:
            instrumentName = "chime";
            break;
        case 9:
            instrumentName = "xylophone";
            break;
        }

        float f = (float) Math.pow(2.0D, (note - 12.0D) / 12.0D);
        this.getHandle().connection.send(new ClientboundSoundPacket(CraftSound.getSoundEffect("block.note_block." + instrumentName), net.minecraft.sounds.SoundSource.RECORDS, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 3.0f, f, this.getHandle().getRandom().nextLong()));
    }

    @Override
    public void playNote(Location loc, Instrument instrument, Note note) {
        if (this.getHandle().connection == null) return;

        String instrumentName = null;
        switch (instrument.ordinal()) {
            case 0:
                instrumentName = "harp";
                break;
            case 1:
                instrumentName = "basedrum";
                break;
            case 2:
                instrumentName = "snare";
                break;
            case 3:
                instrumentName = "hat";
                break;
            case 4:
                instrumentName = "bass";
                break;
            case 5:
                instrumentName = "flute";
                break;
            case 6:
                instrumentName = "bell";
                break;
            case 7:
                instrumentName = "guitar";
                break;
            case 8:
                instrumentName = "chime";
                break;
            case 9:
                instrumentName = "xylophone";
                break;
            case 10:
                instrumentName = "iron_xylophone";
                break;
            case 11:
                instrumentName = "cow_bell";
                break;
            case 12:
                instrumentName = "didgeridoo";
                break;
            case 13:
                instrumentName = "bit";
                break;
            case 14:
                instrumentName = "banjo";
                break;
            case 15:
                instrumentName = "pling";
                break;
            case 16:
                instrumentName = "xylophone";
                break;
        }
        float f = (float) Math.pow(2.0D, (note.getId() - 12.0D) / 12.0D);
        this.getHandle().connection.send(new ClientboundSoundPacket(CraftSound.getSoundEffect("block.note_block." + instrumentName), net.minecraft.sounds.SoundSource.RECORDS, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 3.0f, f, this.getHandle().getRandom().nextLong()));
    }

    @Override
    public void playSound(Location loc, Sound sound, float volume, float pitch) {
        this.playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, String sound, float volume, float pitch) {
        this.playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (loc == null || sound == null || category == null || this.getHandle().connection == null) return;

        ClientboundSoundPacket packet = new ClientboundSoundPacket(CraftSound.getSoundEffect(sound), net.minecraft.sounds.SoundSource.valueOf(category.name()), loc.getX(), loc.getY(), loc.getZ(), volume, pitch, this.getHandle().getRandom().nextLong());
        this.getHandle().connection.send(packet);
    }

    @Override
    public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (loc == null || sound == null || category == null || this.getHandle().connection == null) return;

        ClientboundCustomSoundPacket packet = new ClientboundCustomSoundPacket(new ResourceLocation(sound), net.minecraft.sounds.SoundSource.valueOf(category.name()), new Vec3(loc.getX(), loc.getY(), loc.getZ()), volume, pitch, this.getHandle().getRandom().nextLong());
        this.getHandle().connection.send(packet);
    }

    @Override
    public void playSound(org.bukkit.entity.Entity entity, Sound sound, float volume, float pitch) {
        this.playSound(entity, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(org.bukkit.entity.Entity entity, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (!(entity instanceof CraftEntity craftEntity) || sound == null || category == null || this.getHandle().connection == null) return;

        ClientboundSoundEntityPacket packet = new ClientboundSoundEntityPacket(CraftSound.getSoundEffect(sound), net.minecraft.sounds.SoundSource.valueOf(category.name()), craftEntity.getHandle(), volume, pitch, this.getHandle().getRandom().nextLong());
        this.getHandle().connection.send(packet);
    }

    @Override
    public void stopSound(Sound sound) {
        this.stopSound(sound, null);
    }

    @Override
    public void stopSound(String sound) {
        this.stopSound(sound, null);
    }

    @Override
    public void stopSound(Sound sound, org.bukkit.SoundCategory category) {
        this.stopSound(sound.getKey().getKey(), category);
    }

    @Override
    public void stopSound(String sound, org.bukkit.SoundCategory category) {
        if (this.getHandle().connection == null) return;

        this.getHandle().connection.send(new ClientboundStopSoundPacket(new ResourceLocation(sound), category == null ? net.minecraft.sounds.SoundSource.MASTER : net.minecraft.sounds.SoundSource.valueOf(category.name())));
    }

    @Override
    public void stopSound(org.bukkit.SoundCategory category) {
        if (this.getHandle().connection == null) return;

        this.getHandle().connection.send(new ClientboundStopSoundPacket(null, net.minecraft.sounds.SoundSource.valueOf(category.name())));
    }

    @Override
    public void stopAllSounds() {
        if (this.getHandle().connection == null) return;

        this.getHandle().connection.send(new ClientboundStopSoundPacket(null, null));
    }

    @Override
    public void playEffect(Location loc, Effect effect, int data) {
        if (this.getHandle().connection == null) return;

        int packetData = effect.getId();
        ClientboundLevelEventPacket packet = new ClientboundLevelEventPacket(packetData, new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), data, false);
        this.getHandle().connection.send(packet);
    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data) {
        if (data != null) {
            Validate.isTrue(effect.getData() != null && effect.getData().isAssignableFrom(data.getClass()), "Wrong kind of data for this effect!");
        } else {
            // Special case: the axis is optional for ELECTRIC_SPARK
            Validate.isTrue(effect.getData() == null || effect == Effect.ELECTRIC_SPARK, "Wrong kind of data for this effect!");
        }

        int datavalue = CraftEffect.getDataValue(effect, data);
        this.playEffect(loc, effect, datavalue);
    }

    @Override
    public boolean breakBlock(Block block) {
        Preconditions.checkArgument(block != null, "Block cannot be null");
        Preconditions.checkArgument(block.getWorld().equals(getWorld()), "Cannot break blocks across worlds");

        return this.getHandle().gameMode.destroyBlock(new BlockPos(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public void sendBlockChange(Location loc, Material material, byte data) {
        if (this.getHandle().connection == null) return;

        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), CraftMagicNumbers.getBlock(material, data));
        this.getHandle().connection.send(packet);
    }

    @Override
    public void sendBlockChange(Location loc, BlockData block) {
        if (this.getHandle().connection == null) return;

        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), ((CraftBlockData) block).getState());
        this.getHandle().connection.send(packet);
    }

    // Paper start
    @Override
    public void sendMultiBlockChange(Map<Location, BlockData> blockChanges, boolean suppressLightUpdates) {
        if (this.getHandle().connection == null) return;

        Map<SectionPos, it.unimi.dsi.fastutil.shorts.Short2ObjectMap<net.minecraft.world.level.block.state.BlockState>> sectionMap = new HashMap<>();

        for (Map.Entry<Location, BlockData> entry : blockChanges.entrySet()) {
            Location location = entry.getKey();
            if (!location.getWorld().equals(this.getWorld())) continue;

            BlockData blockData = entry.getValue();
            BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            SectionPos sectionPos = SectionPos.of(blockPos);

            it.unimi.dsi.fastutil.shorts.Short2ObjectMap<net.minecraft.world.level.block.state.BlockState> sectionData = sectionMap.computeIfAbsent(sectionPos, key -> new it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap<>());
            sectionData.put(SectionPos.sectionRelativePos(blockPos), ((CraftBlockData) blockData).getState());
        }

        for (Map.Entry<SectionPos, it.unimi.dsi.fastutil.shorts.Short2ObjectMap<net.minecraft.world.level.block.state.BlockState>> entry : sectionMap.entrySet()) {
            SectionPos sectionPos = entry.getKey();
            it.unimi.dsi.fastutil.shorts.Short2ObjectMap<net.minecraft.world.level.block.state.BlockState> blockData = entry.getValue();

            net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket packet = new net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket(sectionPos, blockData, suppressLightUpdates);
            this.getHandle().connection.send(packet);
        }
    }
    // Paper end

    @Override
    public void sendBlockChanges(Collection<BlockState> blocks, boolean suppressLightUpdates) {
        Preconditions.checkArgument(blocks != null, "blocks must not be null");

        if (this.getHandle().connection == null || blocks.isEmpty()) {
            return;
        }

        Map<SectionPos, ChunkSectionChanges> changes = new HashMap<>();
        for (BlockState state : blocks) {
            CraftBlockState cstate = (CraftBlockState) state;
            BlockPos blockPosition = cstate.getPosition();

            // The coordinates of the chunk section in which the block is located, aka chunk x, y, and z
            SectionPos sectionPosition = SectionPos.of(blockPosition);

            // Push the block change position and block data to the final change map
            ChunkSectionChanges sectionChanges = changes.computeIfAbsent(sectionPosition, (ignore) -> new ChunkSectionChanges());

            sectionChanges.positions().add(SectionPos.sectionRelativePos(blockPosition));
            sectionChanges.blockData().add(cstate.getHandle());
        }

        // Construct the packets using the data allocated above and send then to the players
        for (Map.Entry<SectionPos, ChunkSectionChanges> entry : changes.entrySet()) {
            ChunkSectionChanges chunkChanges = entry.getValue();
            ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(entry.getKey(), chunkChanges.positions(), chunkChanges.blockData().toArray(net.minecraft.world.level.block.state.BlockState[]::new), suppressLightUpdates);
            this.getHandle().connection.send(packet);
        }
    }

    private record ChunkSectionChanges(ShortSet positions, List<net.minecraft.world.level.block.state.BlockState> blockData) {

        public ChunkSectionChanges() {
            this(new ShortArraySet(), new ArrayList<>());
        }
    }

    @Override
    public void sendBlockDamage(Location loc, float progress) {
        // Paper start - customBlockDamage identity
        sendBlockDamage(loc, progress, this.getHandle().getId());
    }

    @Override
    public void sendBlockDamage(Location loc, float progress, int destroyerIdentity) {
        // Paper end - customBlockDamage identity
        Preconditions.checkArgument(loc != null, "loc must not be null");
        Preconditions.checkArgument(progress >= 0.0 && progress <= 1.0, "progress must be between 0.0 and 1.0 (inclusive)");

        if (this.getHandle().connection == null) return;

        int stage = (int) (9 * progress); // There are 0 - 9 damage states
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(destroyerIdentity, new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), stage);  // Paper - customBlockDamage identity
        this.getHandle().connection.send(packet);
    }

    // Paper start
    @Override
    public void sendSignChange(Location loc, @Nullable List<net.kyori.adventure.text.Component> lines, DyeColor dyeColor, boolean hasGlowingText) {
        if (getHandle().connection == null) {
            return;
        }
        if (lines == null) {
            lines = new java.util.ArrayList<>(4);
        }
        Validate.notNull(loc, "Location cannot be null");
        Validate.notNull(dyeColor, "DyeColor cannot be null");
        if (lines.size() < 4) {
            throw new IllegalArgumentException("Must have at least 4 lines");
        }
        Component[] components = CraftSign.sanitizeLines(lines);
        this.sendSignChange0(components, loc, dyeColor, hasGlowingText);
    }

    private void sendSignChange0(Component[] components, Location loc, DyeColor dyeColor, boolean hasGlowingText) {
        SignBlockEntity sign = new SignBlockEntity(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), Blocks.OAK_SIGN.defaultBlockState());
        sign.setColor(net.minecraft.world.item.DyeColor.byId(dyeColor.getWoolData()));
        sign.setHasGlowingText(hasGlowingText);
        for (int i = 0; i < components.length; i++) {
            sign.setMessage(i, components[i]);
        }

        getHandle().connection.send(sign.getUpdatePacket());
    }
    // Paper end
    @Override
    public void sendSignChange(Location loc, String[] lines) {
        this.sendSignChange(loc, lines, DyeColor.BLACK);
    }

    @Override
    public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor) {
        this.sendSignChange(loc, lines, dyeColor, false);
    }

    @Override
    public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor, boolean hasGlowingText) {
        if (this.getHandle().connection == null) {
            return;
        }

        if (lines == null) {
            lines = new String[4];
        }

        Validate.notNull(loc, "Location can not be null");
        Validate.notNull(dyeColor, "DyeColor can not be null");
        if (lines.length < 4) {
            throw new IllegalArgumentException("Must have at least 4 lines");
        }

        Component[] components = CraftSign.sanitizeLines(lines);
        /*SignBlockEntity sign = new SignBlockEntity(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), Blocks.OAK_SIGN.defaultBlockState());
        sign.setColor(net.minecraft.world.item.DyeColor.byId(dyeColor.getWoolData()));
        sign.setHasGlowingText(hasGlowingText);
        for (int i = 0; i < components.length; i++) {
            sign.setMessage(i, components[i]);
        }

        this.getHandle().connection.send(sign.getUpdatePacket());*/ // Paper
        this.sendSignChange0(components, loc, dyeColor, hasGlowingText); // Paper
    }

    @Override
    public void sendEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack item) {
        Preconditions.checkArgument(entity != null, "entity must not be null");
        Preconditions.checkArgument(slot != null, "slot must not be null");
        Preconditions.checkArgument(item != null, "item must not be null");

        if (this.getHandle().connection == null) return;

        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> equipment = Arrays.asList(
                new Pair<>(CraftEquipmentSlot.getNMS(slot), CraftItemStack.asNMSCopy(item))
        );

        this.getHandle().connection.send(new ClientboundSetEquipmentPacket(entity.getEntityId(), equipment));
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.clientWorldBorder;
    }

    @Override
    public void setWorldBorder(WorldBorder border) {
        CraftWorldBorder craftBorder = (CraftWorldBorder) border;

        if (border != null && !craftBorder.isVirtual() && !craftBorder.getWorld().equals(getWorld())) {
            throw new UnsupportedOperationException("Cannot set player world border to that of another world");
        }

        // Nullify the old client-sided world border listeners so that calls to it will not affect this player
        if (this.clientWorldBorder != null) {
            this.clientWorldBorder.getHandle().removeListener(clientWorldBorderListener);
        }

        net.minecraft.world.level.border.WorldBorder newWorldBorder;
        if (craftBorder == null || !craftBorder.isVirtual()) {
            this.clientWorldBorder = null;
            newWorldBorder = ((CraftWorldBorder) getWorld().getWorldBorder()).getHandle();
        } else {
            this.clientWorldBorder = craftBorder;
            this.clientWorldBorder.getHandle().addListener(clientWorldBorderListener);
            newWorldBorder = this.clientWorldBorder.getHandle();
        }

        // Send all world border update packets to the player
        ServerGamePacketListenerImpl connection = this.getHandle().connection;
        connection.send(new ClientboundSetBorderSizePacket(newWorldBorder));
        connection.send(new ClientboundSetBorderLerpSizePacket(newWorldBorder));
        connection.send(new ClientboundSetBorderCenterPacket(newWorldBorder));
        connection.send(new ClientboundSetBorderWarningDelayPacket(newWorldBorder));
        connection.send(new ClientboundSetBorderWarningDistancePacket(newWorldBorder));
    }

    private BorderChangeListener createWorldBorderListener() {
        return new BorderChangeListener() {
            @Override
            public void onBorderSizeSet(net.minecraft.world.level.border.WorldBorder border, double size) {
                CraftPlayer.this.getHandle().connection.send(new ClientboundSetBorderSizePacket(border));
            }

            @Override
            public void onBorderSizeLerping(net.minecraft.world.level.border.WorldBorder border, double fromSize, double toSize, long time) {
                CraftPlayer.this.getHandle().connection.send(new ClientboundSetBorderLerpSizePacket(border));
            }

            @Override
            public void onBorderCenterSet(net.minecraft.world.level.border.WorldBorder border, double centerX, double centerZ) {
                CraftPlayer.this.getHandle().connection.send(new ClientboundSetBorderCenterPacket(border));
            }

            @Override
            public void onBorderSetWarningTime(net.minecraft.world.level.border.WorldBorder border, int warningTime) {
                CraftPlayer.this.getHandle().connection.send(new ClientboundSetBorderWarningDelayPacket(border));
            }

            @Override
            public void onBorderSetWarningBlocks(net.minecraft.world.level.border.WorldBorder border, int warningBlockDistance) {
                CraftPlayer.this.getHandle().connection.send(new ClientboundSetBorderWarningDistancePacket(border));
            }

            @Override
            public void onBorderSetDamagePerBlock(net.minecraft.world.level.border.WorldBorder border, double damagePerBlock) {} // NO OP

            @Override
            public void onBorderSetDamageSafeZOne(net.minecraft.world.level.border.WorldBorder border, double safeZoneRadius) {} // NO OP
        };
    }

    public boolean hasClientWorldBorder() {
        return this.clientWorldBorder != null;
    }

    @Override
    public void sendMap(MapView map) {
        if (this.getHandle().connection == null) return;

        RenderData data = ((CraftMapView) map).render(this);
        Collection<MapDecoration> icons = new ArrayList<MapDecoration>();
        for (MapCursor cursor : data.cursors) {
            if (cursor.isVisible()) {
                icons.add(new MapDecoration(MapDecoration.Type.byIcon(cursor.getRawType()), cursor.getX(), cursor.getY(), cursor.getDirection(), CraftChatMessage.fromStringOrNull(cursor.getCaption())));
            }
        }

        ClientboundMapItemDataPacket packet = new ClientboundMapItemDataPacket(map.getId(), map.getScale().getValue(), map.isLocked(), icons, new MapItemSavedData.MapPatch(0, 0, 128, 128, data.buffer));
        this.getHandle().connection.send(packet);
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        // Paper start - Teleport API
        Location targetLocation = this.getEyeLocation();
        targetLocation.setYaw(yaw);
        targetLocation.setPitch(pitch);

        org.bukkit.util.Vector direction = targetLocation.getDirection();
        targetLocation.add(direction);
        this.lookAt(targetLocation, io.papermc.paper.entity.LookAnchor.EYES);
        // Paper end
    }

    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
        // Paper start - Teleport API
        return this.teleport(location, cause, false);
    }

    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause, boolean ignorePassengers, boolean dismount) {
        return this.teleport(location, cause, ignorePassengers, dismount, new io.papermc.paper.entity.RelativeTeleportFlag[0]);
    }

    @Override
    public void lookAt(@NotNull org.bukkit.entity.Entity entity, @NotNull io.papermc.paper.entity.LookAnchor playerAnchor, @NotNull io.papermc.paper.entity.LookAnchor entityAnchor) {
        this.getHandle().lookAt(toNmsAnchor(playerAnchor), ((CraftEntity) entity).getHandle(), toNmsAnchor(entityAnchor));
    }

    @Override
    public void lookAt(double x, double y, double z, @NotNull io.papermc.paper.entity.LookAnchor playerAnchor) {
        this.getHandle().lookAt(toNmsAnchor(playerAnchor), new Vec3(x, y, z));
    }

    public static net.minecraft.commands.arguments.EntityAnchorArgument.Anchor toNmsAnchor(io.papermc.paper.entity.LookAnchor nmsAnchor) {
        return switch (nmsAnchor) {
            case EYES -> net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES;
            case FEET -> net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.FEET;
        };
    }

    public static io.papermc.paper.entity.LookAnchor toApiAnchor(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor playerAnchor) {
        return switch (playerAnchor) {
            case EYES -> io.papermc.paper.entity.LookAnchor.EYES;
            case FEET -> io.papermc.paper.entity.LookAnchor.FEET;
        };
    }

    public static net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument toNmsRelativeFlag(io.papermc.paper.entity.RelativeTeleportFlag apiFlag) {
        return switch (apiFlag) {
            case X -> net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument.X;
            case Y -> net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument.Y;
            case Z -> net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument.Z;
            case PITCH -> net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument.X_ROT;
            case YAW -> net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT;
        };
    }

    public static io.papermc.paper.entity.RelativeTeleportFlag toApiRelativeFlag(net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument nmsFlag) {
        return switch (nmsFlag) {
            case X -> io.papermc.paper.entity.RelativeTeleportFlag.X;
            case Y -> io.papermc.paper.entity.RelativeTeleportFlag.Y;
            case Z -> io.papermc.paper.entity.RelativeTeleportFlag.Z;
            case X_ROT -> io.papermc.paper.entity.RelativeTeleportFlag.PITCH;
            case Y_ROT -> io.papermc.paper.entity.RelativeTeleportFlag.YAW;
        };
    }

    @Override
    public boolean teleport(Location location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause, boolean ignorePassengers, boolean dismount, io.papermc.paper.entity.RelativeTeleportFlag... flags) {
        var relativeArguments = java.util.EnumSet.noneOf(net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument.class);
        for (io.papermc.paper.entity.RelativeTeleportFlag flag : flags) {
            relativeArguments.add(toNmsRelativeFlag(flag));
        }
        // Paper end - Teleport API
        Preconditions.checkArgument(location != null, "location");
        Preconditions.checkArgument(location.getWorld() != null, "location.world");
        // Paper start - Teleport passenger API
        // Don't allow teleporting between worlds while keeping passengers
        if (ignorePassengers && entity.isVehicle() && location.getWorld() != this.getWorld()) {
            return false;
        }

        // Don't allow to teleport between worlds if remaining on vehicle
        if (!dismount && entity.isPassenger() && location.getWorld() != this.getWorld()) {
            return false;
        }
        // Paper end
        location.checkFinite();

        ServerPlayer entity = this.getHandle();

        if (this.getHealth() == 0 || entity.isRemoved()) {
            return false;
        }

        if (entity.connection == null) {
           return false;
        }

        if (entity.isVehicle() && !ignorePassengers) { // Paper - Teleport API
            return false;
        }

        // From = Players current Location
        Location from = this.getLocation();
        // To = Players new Location if Teleport is Successful
        Location to = location;
        // Create & Call the Teleport Event.
        PlayerTeleportEvent event = new PlayerTeleportEvent(this, from, to, cause);
        server.getPluginManager().callEvent(event);

        // Return False to inform the Plugin that the Teleport was unsuccessful/cancelled.
        if (event.isCancelled()) {
            return false;
        }

        // If this player is riding another entity, we must dismount before teleporting.
        if (dismount) entity.stopRiding(); // Paper - Teleport API

        // SPIGOT-5509: Wakeup, similar to riding
        if (this.isSleeping()) {
            this.wakeup(false);
        }

        // Update the From Location
        from = event.getFrom();
        // Grab the new To Location dependent on whether the event was cancelled.
        to = event.getTo();
        // Grab the To and From World Handles.
        ServerLevel fromWorld = ((CraftWorld) from.getWorld()).getHandle();
        ServerLevel toWorld = ((CraftWorld) to.getWorld()).getHandle();

        // Close any foreign inventory
        if (this.getHandle().containerMenu != this.getHandle().inventoryMenu) {
            this.getHandle().closeContainer(org.bukkit.event.inventory.InventoryCloseEvent.Reason.TELEPORT); // Paper
        }

        // Check if the fromWorld and toWorld are the same.
        if (fromWorld == toWorld) {
            entity.connection.internalTeleport(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch(), relativeArguments, dismount); // Paper - Teleport API
        } else {
            server.getHandle().respawn(entity, toWorld, true, to, !toWorld.paperConfig().environment.disableTeleportationSuffocationCheck); // Paper
        }
        return true;
    }

    @Override
    public void setSneaking(boolean sneak) {
        this.getHandle().setShiftKeyDown(sneak);
    }

    @Override
    public boolean isSneaking() {
        return this.getHandle().isShiftKeyDown();
    }

    @Override
    public boolean isSprinting() {
        return this.getHandle().isSprinting();
    }

    @Override
    public void setSprinting(boolean sprinting) {
        this.getHandle().setSprinting(sprinting);
    }

    @Override
    public void loadData() {
        server.getHandle().playerIo.load(this.getHandle());
    }

    @Override
    public void saveData() {
        server.getHandle().playerIo.save(this.getHandle());
    }

    @Deprecated
    @Override
    public void updateInventory() {
        this.getHandle().containerMenu.sendAllDataToRemote();
    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {
        this.getHandle().fauxSleeping = isSleeping;
        ((CraftWorld) getWorld()).getHandle().updateSleepingPlayerList();
    }

    @Override
    public boolean isSleepingIgnored() {
        return this.getHandle().fauxSleeping;
    }

    @Override
    public Location getBedSpawnLocation() {
        ServerLevel world = this.getHandle().server.getLevel(this.getHandle().getRespawnDimension());
        BlockPos bed = this.getHandle().getRespawnPosition();

        if (world != null && bed != null) {
            Optional<Vec3> spawnLoc = net.minecraft.world.entity.player.Player.findRespawnPositionAndUseSpawnBlock(world, bed, this.getHandle().getRespawnAngle(), this.getHandle().isRespawnForced(), true);
            if (spawnLoc.isPresent()) {
                Vec3 vec = spawnLoc.get();
                return new Location(world.getWorld(), vec.x, vec.y, vec.z, this.getHandle().getRespawnAngle(), 0);
            }
        }
        return null;
    }

    @Override
    public void setBedSpawnLocation(Location location) {
        this.setBedSpawnLocation(location, false);
    }

    @Override
    public void setBedSpawnLocation(Location location, boolean override) {
        if (location == null) {
            this.getHandle().setRespawnPosition(null, null, 0.0F, override, false, com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.Cause.PLUGIN); // Paper - PlayerSetSpawnEvent
        } else {
            this.getHandle().setRespawnPosition(((CraftWorld) location.getWorld()).getHandle().dimension(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), location.getYaw(), override, false, com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.Cause.PLUGIN); // Paper - PlayerSetSpawnEvent
        }
    }

    @Override
    public Location getBedLocation() {
        Preconditions.checkState(isSleeping(), "Not sleeping");

        BlockPos bed = this.getHandle().getRespawnPosition();
        return new Location(getWorld(), bed.getX(), bed.getY(), bed.getZ());
    }

    @Override
    public boolean hasDiscoveredRecipe(NamespacedKey recipe) {
        Preconditions.checkArgument(recipe != null, "recipe cannot be null");
        return this.getHandle().getRecipeBook().contains(CraftNamespacedKey.toMinecraft(recipe));
    }

    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        ImmutableSet.Builder<NamespacedKey> bukkitRecipeKeys = ImmutableSet.builder();
        this.getHandle().getRecipeBook().known.forEach(key -> bukkitRecipeKeys.add(CraftNamespacedKey.fromMinecraft(key)));
        return bukkitRecipeKeys.build();
    }

    @Override
    public void incrementStatistic(Statistic statistic) {
        CraftStatistic.incrementStatistic(this.getHandle().getStats(), statistic);
    }

    @Override
    public void decrementStatistic(Statistic statistic) {
        CraftStatistic.decrementStatistic(this.getHandle().getStats(), statistic);
    }

    @Override
    public int getStatistic(Statistic statistic) {
        return CraftStatistic.getStatistic(this.getHandle().getStats(), statistic);
    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) {
        CraftStatistic.incrementStatistic(this.getHandle().getStats(), statistic, amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, int amount) {
        CraftStatistic.decrementStatistic(this.getHandle().getStats(), statistic, amount);
    }

    @Override
    public void setStatistic(Statistic statistic, int newValue) {
        CraftStatistic.setStatistic(this.getHandle().getStats(), statistic, newValue);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) {
        CraftStatistic.incrementStatistic(this.getHandle().getStats(), statistic, material);
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) {
        CraftStatistic.decrementStatistic(this.getHandle().getStats(), statistic, material);
    }

    @Override
    public int getStatistic(Statistic statistic, Material material) {
        return CraftStatistic.getStatistic(this.getHandle().getStats(), statistic, material);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int amount) {
        CraftStatistic.incrementStatistic(this.getHandle().getStats(), statistic, material, amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int amount) {
        CraftStatistic.decrementStatistic(this.getHandle().getStats(), statistic, material, amount);
    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int newValue) {
        CraftStatistic.setStatistic(this.getHandle().getStats(), statistic, material, newValue);
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) {
        CraftStatistic.incrementStatistic(this.getHandle().getStats(), statistic, entityType);
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) {
        CraftStatistic.decrementStatistic(this.getHandle().getStats(), statistic, entityType);
    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) {
        return CraftStatistic.getStatistic(this.getHandle().getStats(), statistic, entityType);
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        CraftStatistic.incrementStatistic(this.getHandle().getStats(), statistic, entityType, amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        CraftStatistic.decrementStatistic(this.getHandle().getStats(), statistic, entityType, amount);
    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
        CraftStatistic.setStatistic(this.getHandle().getStats(), statistic, entityType, newValue);
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        this.getHandle().timeOffset = time;
        this.getHandle().relativeTime = relative;
    }

    @Override
    public long getPlayerTimeOffset() {
        return this.getHandle().timeOffset;
    }

    @Override
    public long getPlayerTime() {
        return this.getHandle().getPlayerTime();
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return this.getHandle().relativeTime;
    }

    @Override
    public void resetPlayerTime() {
        this.setPlayerTime(0, true);
    }

    @Override
    public void setPlayerWeather(WeatherType type) {
        this.getHandle().setPlayerWeather(type, true);
    }

    @Override
    public WeatherType getPlayerWeather() {
        return this.getHandle().getPlayerWeather();
    }

    @Override
    public void resetPlayerWeather() {
        this.getHandle().resetPlayerWeather();
    }

    @Override
    public boolean isBanned() {
        return server.getBanList(BanList.Type.NAME).isBanned(getName());
    }

    @Override
    public boolean isWhitelisted() {
        return server.getHandle().getWhiteList().isWhiteListed(this.getProfile());
    }

    @Override
    public void setWhitelisted(boolean value) {
        if (value) {
            server.getHandle().getWhiteList().add(new UserWhiteListEntry(this.getProfile()));
        } else {
            server.getHandle().getWhiteList().remove(this.getProfile());
        }
    }

    @Override
    public void setGameMode(GameMode mode) {
        if (this.getHandle().connection == null) return;

        if (mode == null) {
            throw new IllegalArgumentException("Mode cannot be null");
        }

        this.getHandle().setGameMode(GameType.byId(mode.getValue()), org.bukkit.event.player.PlayerGameModeChangeEvent.Cause.PLUGIN, null); // Paper
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.getByValue(this.getHandle().gameMode.getGameModeForPlayer().getId());
    }

    @Override
    public GameMode getPreviousGameMode() {
        GameType previousGameMode = this.getHandle().gameMode.getPreviousGameModeForPlayer();

        return (previousGameMode == null) ? null : GameMode.getByValue(previousGameMode.getId());
    }

    @Override
    // Paper start
    public int applyMending(int amount) {
        ServerPlayer handle = this.getHandle();
        // Logic copied from EntityExperienceOrb and remapped to unobfuscated methods/properties
        final var stackEntry = net.minecraft.world.item.enchantment.EnchantmentHelper
            .getRandomItemWith(net.minecraft.world.item.enchantment.Enchantments.MENDING, handle);
        final net.minecraft.world.item.ItemStack itemstack = stackEntry != null ? stackEntry.getValue() : net.minecraft.world.item.ItemStack.EMPTY;
        if (!itemstack.isEmpty() && itemstack.getItem().canBeDepleted()) {
            net.minecraft.world.entity.ExperienceOrb orb = net.minecraft.world.entity.EntityType.EXPERIENCE_ORB.create(handle.level);
            orb.value = amount;
            orb.spawnReason = org.bukkit.entity.ExperienceOrb.SpawnReason.CUSTOM;
            orb.setPosRaw(handle.getX(), handle.getY(), handle.getZ());

            int i = Math.min(orb.xpToDurability(amount), itemstack.getDamageValue());
            org.bukkit.event.player.PlayerItemMendEvent event = org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory.callPlayerItemMendEvent(handle, orb, itemstack, stackEntry.getKey(), i);
            i = event.getRepairAmount();
            orb.discard();
            if (!event.isCancelled()) {
                amount -= orb.durabilityToXp(i);
                itemstack.setDamageValue(itemstack.getDamageValue() - i);
            }
        }
        return amount;
    }

    @Override
    public void giveExp(int exp, boolean applyMending) {
        if (applyMending) {
            exp = this.applyMending(exp);
        }
        // Paper end
        this.getHandle().giveExperiencePoints(exp);
    }

    @Override
    public void giveExpLevels(int levels) {
        this.getHandle().giveExperienceLevels(levels);
    }

    @Override
    public float getExp() {
        return this.getHandle().experienceProgress;
    }

    @Override
    public void setExp(float exp) {
        Preconditions.checkArgument(exp >= 0.0 && exp <= 1.0, "Experience progress must be between 0.0 and 1.0 (%s)", exp);
        this.getHandle().experienceProgress = exp;
        this.getHandle().lastSentExp = -1;
    }

    @Override
    public int getLevel() {
        return this.getHandle().experienceLevel;
    }

    @Override
    public void setLevel(int level) {
        Preconditions.checkArgument(level >= 0, "Experience level must not be negative (%s)", level);
        this.getHandle().experienceLevel = level;
        this.getHandle().lastSentExp = -1;
    }

    @Override
    public int getTotalExperience() {
        return this.getHandle().totalExperience;
    }

    @Override
    public void setTotalExperience(int exp) {
        Preconditions.checkArgument(exp >= 0, "Total experience points must not be negative (%s)", exp);
        this.getHandle().totalExperience = exp;
    }

    @Override
    public void sendExperienceChange(float progress) {
        this.sendExperienceChange(progress, this.getLevel());
    }

    @Override
    public void sendExperienceChange(float progress, int level) {
        Preconditions.checkArgument(progress >= 0.0 && progress <= 1.0, "Experience progress must be between 0.0 and 1.0 (%s)", progress);
        Preconditions.checkArgument(level >= 0, "Experience level must not be negative (%s)", level);

        if (this.getHandle().connection == null) {
            return;
        }

        ClientboundSetExperiencePacket packet = new ClientboundSetExperiencePacket(progress, this.getTotalExperience(), level);
        this.getHandle().connection.send(packet);
    }

    @Nullable
    private static WeakReference<Plugin> getPluginWeakReference(@Nullable Plugin plugin) {
        return (plugin == null) ? null : CraftPlayer.pluginWeakReferences.computeIfAbsent(plugin, WeakReference::new);
    }

    @Override
    @Deprecated
    public void hidePlayer(Player player) {
        this.hideEntity0(null, player);
    }

    @Override
    public void hidePlayer(Plugin plugin, Player player) {
        this.hideEntity(plugin, player);
    }

    @Override
    public void hideEntity(Plugin plugin, org.bukkit.entity.Entity entity) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.isTrue(plugin.isEnabled(), "Plugin attempted to hide player while disabled");

        this.hideEntity0(plugin, entity);
    }

    private void hideEntity0(@Nullable Plugin plugin, org.bukkit.entity.Entity entity) {
        Validate.notNull(entity, "hidden entity cannot be null");
        if (this.getHandle().connection == null) return;
        if (this.equals(entity)) return;

        Set<WeakReference<Plugin>> hidingPlugins = this.hiddenEntities.get(entity.getUniqueId());
        if (hidingPlugins != null) {
            // Some plugins are already hiding the entity. Just mark that this
            // plugin wants the entity hidden too and end.
            hidingPlugins.add(CraftPlayer.getPluginWeakReference(plugin));
            return;
        }
        hidingPlugins = new HashSet<>();
        hidingPlugins.add(CraftPlayer.getPluginWeakReference(plugin));
        this.hiddenEntities.put(entity.getUniqueId(), hidingPlugins);

        // Remove this entity from the hidden player's EntityTrackerEntry
        // Paper start
        Entity other = ((CraftEntity) entity).getHandle();
        unregisterEntity(other);

        server.getPluginManager().callEvent(new PlayerHideEntityEvent(this, entity));
    }
    private void unregisterEntity(Entity other) {
        // Paper end
        ChunkMap tracker = ((ServerLevel) this.getHandle().level).getChunkSource().chunkMap;
        ChunkMap.TrackedEntity entry = tracker.entityMap.get(other.getId());
        if (entry != null) {
            entry.removePlayer(this.getHandle());
        }

        // Remove the hidden entity from this player user list, if they're on it
        if (other instanceof ServerPlayer) {
            ServerPlayer otherPlayer = (ServerPlayer) other;
            if (otherPlayer.sentListPacket) {
                this.getHandle().connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, otherPlayer));
            }
        }
    }

    @Override
    @Deprecated
    public void showPlayer(Player player) {
        this.showEntity0(null, player);
    }

    @Override
    public void showPlayer(Plugin plugin, Player player) {
        this.showEntity(plugin, player);
    }

    @Override
    public void showEntity(Plugin plugin, org.bukkit.entity.Entity entity) {
        Validate.notNull(plugin, "Plugin cannot be null");
        // Don't require that plugin be enabled. A plugin must be allowed to call
        // showPlayer during its onDisable() method.
        this.showEntity0(plugin, entity);
    }

    private void showEntity0(@Nullable Plugin plugin, org.bukkit.entity.Entity entity) {
        Validate.notNull(entity, "shown entity cannot be null");
        if (this.getHandle().connection == null) return;
        if (this.equals(entity)) return;

        Set<WeakReference<Plugin>> hidingPlugins = this.hiddenEntities.get(entity.getUniqueId());
        if (hidingPlugins == null) {
            return; // Entity isn't hidden
        }
        hidingPlugins.remove(CraftPlayer.getPluginWeakReference(plugin));
        if (!hidingPlugins.isEmpty()) {
            return; // Some other plugins still want the entity hidden
        }
        this.hiddenEntities.remove(entity.getUniqueId());

        // Paper start
        Entity other = ((CraftEntity) entity).getHandle();
        registerEntity(other);

        server.getPluginManager().callEvent(new PlayerShowEntityEvent(this, entity));
    }
    private void registerEntity(Entity other) {
        ChunkMap tracker = ((ServerLevel) this.getHandle().level).getChunkSource().chunkMap;
        // Paper end

        if (other instanceof ServerPlayer) {
            ServerPlayer otherPlayer = (ServerPlayer) other;
            this.getHandle().connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, otherPlayer));
        }

        ChunkMap.TrackedEntity entry = tracker.entityMap.get(other.getId());
        if (entry != null && !entry.seenBy.contains(this.getHandle().connection)) {
            entry.updatePlayer(this.getHandle());
        }
    }
    // Paper start
    private void reregisterPlayer(ServerPlayer player) {
        if (!hiddenEntities.containsKey(player.getUUID())) {
            unregisterEntity(player);
            registerEntity(player);
        }
    }
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile profile) {
        ServerPlayer self = getHandle();
        self.gameProfile = com.destroystokyo.paper.profile.CraftPlayerProfile.asAuthlibCopy(profile);
        if (!self.sentListPacket) {
            return;
        }
        List<ServerPlayer> players = server.getServer().getPlayerList().players;
        for (ServerPlayer player : players) {
            player.getBukkitEntity().reregisterPlayer(self);
        }
        refreshPlayer();
    }
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        return new com.destroystokyo.paper.profile.CraftPlayerProfile(this).clone();
    }

    private void refreshPlayer() {
        ServerPlayer handle = getHandle();

        Location loc = getLocation();

        ServerGamePacketListenerImpl connection = handle.connection;
        reregisterPlayer(handle);

        //Respawn the player then update their position and selected slot
        ServerLevel worldserver = handle.getLevel();
        connection.send(new net.minecraft.network.protocol.game.ClientboundRespawnPacket(worldserver.dimensionTypeId(), worldserver.dimension(), BiomeManager.obfuscateSeed(worldserver.getSeed()), handle.gameMode.getGameModeForPlayer(), handle.gameMode.getPreviousGameModeForPlayer(), worldserver.isDebug(), worldserver.isFlat(), true, this.getHandle().getLastDeathLocation()));
        handle.onUpdateAbilities();
        connection.internalTeleport(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), java.util.Collections.emptySet(), false);
        net.minecraft.server.MinecraftServer.getServer().getPlayerList().sendAllPlayerInfo(handle);

        if (this.isOp()) {
            this.setOp(false);
            this.setOp(true);
        }
    }
    // Paper end

    public void onEntityRemove(Entity entity) {
        this.hiddenEntities.remove(entity.getUUID());
    }

    @Override
    public boolean canSee(Player player) {
        return this.canSee((org.bukkit.entity.Entity) player);
    }

    @Override
    public boolean canSee(org.bukkit.entity.Entity entity) {
        return !this.hiddenEntities.containsKey(entity.getUniqueId());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("name", getName());

        return result;
    }

    @Override
    public Player getPlayer() {
        return this;
    }

    @Override
    public ServerPlayer getHandle() {
        return (ServerPlayer) entity;
    }

    public void setHandle(final ServerPlayer entity) {
        super.setHandle(entity);
    }

    @Override
    public String toString() {
        return "CraftPlayer{" + "name=" + getName() + '}';
    }

    @Override
    public int hashCode() {
        if (this.hash == 0 || this.hash == 485) {
            this.hash = 97 * 5 + (this.getUniqueId() != null ? this.getUniqueId().hashCode() : 0);
        }
        return this.hash;
    }

    @Override
    public long getFirstPlayed() {
        return this.firstPlayed;
    }

    @Override
    public long getLastPlayed() {
        return this.lastPlayed;
    }

    @Override
    public boolean hasPlayedBefore() {
        return this.hasPlayedBefore;
    }

    public void setFirstPlayed(long firstPlayed) {
        this.firstPlayed = firstPlayed;
    }

    // Paper start
    @Override
    public long getLastLogin() {
        return getHandle().loginTime;
    }

    @Override
    public long getLastSeen() {
        return isOnline() ? System.currentTimeMillis() : this.lastSaveTime;
    }
    // Paper end

    public void readExtraData(CompoundTag nbttagcompound) {
        this.hasPlayedBefore = true;
        if (nbttagcompound.contains("bukkit")) {
            CompoundTag data = nbttagcompound.getCompound("bukkit");

            if (data.contains("firstPlayed")) {
                this.firstPlayed = data.getLong("firstPlayed");
                this.lastPlayed = data.getLong("lastPlayed");
            }

            if (data.contains("newExp")) {
                ServerPlayer handle = this.getHandle();
                handle.newExp = data.getInt("newExp");
                handle.newTotalExp = data.getInt("newTotalExp");
                handle.newLevel = data.getInt("newLevel");
                handle.expToDrop = data.getInt("expToDrop");
                handle.keepLevel = data.getBoolean("keepLevel");
            }
        }
    }

    public void setExtraData(CompoundTag nbttagcompound) {
        this.lastSaveTime = System.currentTimeMillis(); // Paper

        if (!nbttagcompound.contains("bukkit")) {
            nbttagcompound.put("bukkit", new CompoundTag());
        }

        CompoundTag data = nbttagcompound.getCompound("bukkit");
        ServerPlayer handle = this.getHandle();
        data.putInt("newExp", handle.newExp);
        data.putInt("newTotalExp", handle.newTotalExp);
        data.putInt("newLevel", handle.newLevel);
        data.putInt("expToDrop", handle.expToDrop);
        data.putBoolean("keepLevel", handle.keepLevel);
        data.putLong("firstPlayed", this.getFirstPlayed());
        data.putLong("lastPlayed", System.currentTimeMillis());
        data.putString("lastKnownName", handle.getScoreboardName());

        // Paper start - persist for use in offline save data
        if (!nbttagcompound.contains("Paper")) {
            nbttagcompound.put("Paper", new CompoundTag());
        }

        CompoundTag paper = nbttagcompound.getCompound("Paper");
        paper.putLong("LastLogin", handle.loginTime);
        paper.putLong("LastSeen", System.currentTimeMillis());
        // Paper end
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return this.conversationTracker.beginConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        this.conversationTracker.abandonConversation(conversation, new ConversationAbandonedEvent(conversation, new ManuallyAbandonedConversationCanceller()));
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        this.conversationTracker.abandonConversation(conversation, details);
    }

    @Override
    public void acceptConversationInput(String input) {
        this.conversationTracker.acceptConversationInput(input);
    }

    @Override
    public boolean isConversing() {
        return this.conversationTracker.isConversing();
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(server.getMessenger(), source, channel, message);
        if (this.getHandle().connection == null) return;

        if (this.channels.contains(channel)) {
            channel = StandardMessenger.validateAndCorrectChannel(channel);
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(new ResourceLocation(channel), new FriendlyByteBuf(Unpooled.wrappedBuffer(message)));
            this.getHandle().connection.send(packet);
        }
    }

    @Override
    public void setTexturePack(String url) {
        this.setResourcePack(url);
    }

    @Override
    public void setResourcePack(String url) {
        this.setResourcePack(url, (byte[]) null);
    }

    @Override
    public void setResourcePack(String url, byte[] hash) {
        this.setResourcePack(url, hash, false);
    }

    @Override
    public void setResourcePack(String url, byte[] hash, String prompt) {
        this.setResourcePack(url, hash, prompt, false);
    }

    @Override
    public void setResourcePack(String url, byte[] hash, boolean force) {
        this.setResourcePack(url, hash, (String) null, force);
    }

    @Override
    public void setResourcePack(String url, byte[] hash, String prompt, boolean force) {
        Validate.notNull(url, "Resource pack URL cannot be null");

        if (hash != null) {
            Validate.isTrue(hash.length == 20, "Resource pack hash should be 20 bytes long but was " + hash.length);

            this.getHandle().sendTexturePack(url, BaseEncoding.base16().lowerCase().encode(hash), force, CraftChatMessage.fromStringOrNull(prompt, true));
        } else {
            this.getHandle().sendTexturePack(url, "", force, CraftChatMessage.fromStringOrNull(prompt, true));
        }
    }

    // Paper start
    @Override
    public void setResourcePack(String url, byte[] hashBytes, net.kyori.adventure.text.Component prompt, boolean force) {
        Validate.notNull(url, "Resource pack URL cannot be null");
        final String hash;
        if (hashBytes != null) {
            Validate.isTrue(hashBytes.length == 20, "Resource pack hash should be 20 bytes long but was " + hashBytes.length);
            hash = BaseEncoding.base16().lowerCase().encode(hashBytes);
        } else {
            hash = "";
        }
        this.getHandle().sendTexturePack(url, hash, force, io.papermc.paper.adventure.PaperAdventure.asVanilla(prompt));
    }
    // Paper end

    public void addChannel(String channel) {
        Preconditions.checkState(DISABLE_CHANNEL_LIMIT || this.channels.size() < 128, "Cannot register channel '%s'. Too many channels registered!", channel); // Paper - flag to disable channel limit
        channel = StandardMessenger.validateAndCorrectChannel(channel);
        if (this.channels.add(channel)) {
            server.getPluginManager().callEvent(new PlayerRegisterChannelEvent(this, channel));
        }
    }

    public void removeChannel(String channel) {
        channel = StandardMessenger.validateAndCorrectChannel(channel);
        if (this.channels.remove(channel)) {
            server.getPluginManager().callEvent(new PlayerUnregisterChannelEvent(this, channel));
        }
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        return ImmutableSet.copyOf(channels);
    }

    public void sendSupportedChannels() {
        if (this.getHandle().connection == null) return;
        Set<String> listening = server.getMessenger().getIncomingChannels();

        if (!listening.isEmpty()) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            for (String channel : listening) {
                try {
                    stream.write(channel.getBytes("UTF8"));
                    stream.write((byte) 0);
                } catch (IOException ex) {
                    Logger.getLogger(CraftPlayer.class.getName()).log(Level.SEVERE, "Could not send Plugin Channel REGISTER to " + getName(), ex);
                }
            }

            this.getHandle().connection.send(new ClientboundCustomPayloadPacket(new ResourceLocation("register"), new FriendlyByteBuf(Unpooled.wrappedBuffer(stream.toByteArray()))));
        }
    }

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        server.getPlayerMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return server.getPlayerMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return server.getPlayerMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        server.getPlayerMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public boolean setWindowProperty(Property prop, int value) {
        AbstractContainerMenu container = this.getHandle().containerMenu;
        if (container.getBukkitView().getType() != prop.getType()) {
            return false;
        }
        container.setData(prop.getId(), value);
        return true;
    }

    public void disconnect(String reason) {
        this.conversationTracker.abandonAllConversations();
        perm.clearPermissions();
    }

    @Override
    public boolean isFlying() {
        return this.getHandle().getAbilities().flying;
    }

    @Override
    public void setFlying(boolean value) {
        boolean needsUpdate = getHandle().getAbilities().flying != value; // Paper - Only refresh abilities if needed
        if (!this.getAllowFlight() && value) {
            throw new IllegalArgumentException("Cannot make player fly if getAllowFlight() is false");
        }

        this.getHandle().getAbilities().flying = value;
        if (needsUpdate) this.getHandle().onUpdateAbilities(); // Paper - Only refresh abilities if needed
    }

    @Override
    public boolean getAllowFlight() {
        return this.getHandle().getAbilities().mayfly;
    }

    @Override
    public void setAllowFlight(boolean value) {
        if (this.isFlying() && !value) {
            this.getHandle().getAbilities().flying = false;
        }

        this.getHandle().getAbilities().mayfly = value;
        this.getHandle().onUpdateAbilities();
    }

    @Override
    public int getNoDamageTicks() {
        if (this.getHandle().spawnInvulnerableTime > 0) {
            return Math.max(this.getHandle().spawnInvulnerableTime, this.getHandle().invulnerableTime);
        } else {
            return this.getHandle().invulnerableTime;
        }
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        super.setNoDamageTicks(ticks);
        this.getHandle().spawnInvulnerableTime = ticks; // SPIGOT-5921: Update both for players, like the getter above
    }

    @Override
    public void setFlySpeed(float value) {
        this.validateSpeed(value);
        ServerPlayer player = this.getHandle();
        player.getAbilities().flyingSpeed = value / 2f;
        player.onUpdateAbilities();

    }

    @Override
    public void setWalkSpeed(float value) {
        this.validateSpeed(value);
        ServerPlayer player = this.getHandle();
        player.getAbilities().walkingSpeed = value / 2f;
        player.onUpdateAbilities();
        this.getHandle().getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(player.getAbilities().walkingSpeed); // SPIGOT-5833: combination of the two in 1.16+
    }

    @Override
    public float getFlySpeed() {
        return (float) this.getHandle().getAbilities().flyingSpeed * 2f;
    }

    @Override
    public float getWalkSpeed() {
        return this.getHandle().getAbilities().walkingSpeed * 2f;
    }

    private void validateSpeed(float value) {
        if (value < 0) {
            if (value < -1f) {
                throw new IllegalArgumentException(value + " is too low");
            }
        } else {
            if (value > 1f) {
                throw new IllegalArgumentException(value + " is too high");
            }
        }
    }

    @Override
    public void setMaxHealth(double amount) {
        super.setMaxHealth(amount);
        this.health = Math.min(this.health, health);
        this.getHandle().resetSentInfo();
    }

    @Override
    public void resetMaxHealth() {
        super.resetMaxHealth();
        this.getHandle().resetSentInfo();
    }

    @Override
    public CraftScoreboard getScoreboard() {
        return this.server.getScoreboardManager().getPlayerBoard(this);
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
        Validate.notNull(scoreboard, "Scoreboard cannot be null");
        ServerGamePacketListenerImpl playerConnection = this.getHandle().connection;
        if (playerConnection == null) {
            throw new IllegalStateException("Cannot set scoreboard yet");
        }
        if (playerConnection.isDisconnected()) {
            // throw new IllegalStateException("Cannot set scoreboard for invalid CraftPlayer"); // Spigot - remove this as Mojang's semi asynchronous Netty implementation can lead to races
        }

        this.server.getScoreboardManager().setPlayerBoard(this, scoreboard);
    }

    @Override
    public void setHealthScale(double value) {
        Validate.isTrue((float) value > 0F, "Must be greater than 0");
        this.healthScale = value;
        this.scaledHealth = true;
        this.updateScaledHealth();
    }

    @Override
    public double getHealthScale() {
        return this.healthScale;
    }

    @Override
    public void setHealthScaled(boolean scale) {
        if (this.scaledHealth != (this.scaledHealth = scale)) {
            this.updateScaledHealth();
        }
    }

    @Override
    public boolean isHealthScaled() {
        return this.scaledHealth;
    }

    public float getScaledHealth() {
        return (float) (this.isHealthScaled() ? this.getHealth() * this.getHealthScale() / getMaxHealth() : this.getHealth());
    }

    @Override
    public double getHealth() {
        return this.health;
    }

    public void setRealHealth(double health) {
        if (Double.isNaN(health)) {return;} // Paper
        this.health = health;
    }

    public void updateScaledHealth() {
        this.updateScaledHealth(true);
    }

    public void updateScaledHealth(boolean sendHealth) {
        AttributeMap attributemapserver = this.getHandle().getAttributes();
        Collection<AttributeInstance> set = attributemapserver.getSyncableAttributes();

        this.injectScaledMaxHealth(set, true);

        // SPIGOT-3813: Attributes before health
        if (this.getHandle().connection != null) {
            this.getHandle().connection.send(new ClientboundUpdateAttributesPacket(this.getHandle().getId(), set));
            if (sendHealth) {
                this.sendHealthUpdate();
            }
        }
        this.getHandle().getEntityData().set(net.minecraft.world.entity.LivingEntity.DATA_HEALTH_ID, (float) this.getScaledHealth());

        this.getHandle().maxHealthCache = getMaxHealth();
    }

    // Paper start
    @Override
    public void sendHealthUpdate(final double health, final int foodLevel, final float saturationLevel) {
        // Paper start - cancellable death event
        ClientboundSetHealthPacket packet = new ClientboundSetHealthPacket((float) health, foodLevel, saturationLevel);
        if (this.getHandle().queueHealthUpdatePacket) {
            this.getHandle().queuedHealthUpdatePacket = packet;
        } else {
            this.getHandle().connection.send(packet);
        }
        // Paper end
    }
    
    @Override
    public void sendHealthUpdate() {
        this.sendHealthUpdate(this.getScaledHealth(), this.getHandle().getFoodData().getFoodLevel(), this.getHandle().getFoodData().getSaturationLevel());
    }
    // Paper end
    
    public void injectScaledMaxHealth(Collection<AttributeInstance> collection, boolean force) {
        if (!this.scaledHealth && !force) {
            return;
        }
        for (AttributeInstance genericInstance : collection) {
            if (genericInstance.getAttribute() == Attributes.MAX_HEALTH) {
                collection.remove(genericInstance);
                break;
            }
        }
        AttributeInstance dummy = new AttributeInstance(Attributes.MAX_HEALTH, (attribute) -> { });
        // Spigot start
        double healthMod = this.scaledHealth ? this.healthScale : getMaxHealth();
        if ( healthMod >= Float.MAX_VALUE || healthMod <= 0 )
        {
            healthMod = 20; // Reset health
            getServer().getLogger().warning( getName() + " tried to crash the server with a large health attribute" );
        }
        dummy.setBaseValue(healthMod);
        // Spigot end
        collection.add(dummy);
    }

    @Override
    public org.bukkit.entity.Entity getSpectatorTarget() {
        Entity followed = this.getHandle().getCamera();
        return followed == this.getHandle() ? null : followed.getBukkitEntity();
    }

    @Override
    public void setSpectatorTarget(org.bukkit.entity.Entity entity) {
        Preconditions.checkArgument(this.getGameMode() == GameMode.SPECTATOR, "Player must be in spectator mode");
        this.getHandle().setCamera((entity == null) ? null : ((CraftEntity) entity).getHandle());
    }

    @Override
    public void sendTitle(String title, String subtitle) {
        this.sendTitle(title, subtitle, 10, 70, 20);
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        ClientboundSetTitlesAnimationPacket times = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
        this.getHandle().connection.send(times);

        if (title != null) {
            ClientboundSetTitleTextPacket packetTitle = new ClientboundSetTitleTextPacket(CraftChatMessage.fromString(title)[0]);
            this.getHandle().connection.send(packetTitle);
        }

        if (subtitle != null) {
            ClientboundSetSubtitleTextPacket packetSubtitle = new ClientboundSetSubtitleTextPacket(CraftChatMessage.fromString(subtitle)[0]);
            this.getHandle().connection.send(packetSubtitle);
        }
    }

    @Override
    public void resetTitle() {
        ClientboundClearTitlesPacket packetReset = new ClientboundClearTitlesPacket(true);
        this.getHandle().connection.send(packetReset);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count) {
        this.spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {
        this.spawnParticle(particle, x, y, z, count, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
        this.spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
        this.spawnParticle(particle, x, y, z, count, 0, 0, 0, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {
        this.spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
        this.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {
        this.spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {
        this.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 1, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        this.spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        this.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        this.spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        if (data != null && !particle.getDataType().isInstance(data)) {
            throw new IllegalArgumentException("data should be " + particle.getDataType() + " got " + data.getClass());
        }
        ClientboundLevelParticlesPacket packetplayoutworldparticles = new ClientboundLevelParticlesPacket(CraftParticle.toNMS(particle, data), true, x, y, z, (float) offsetX, (float) offsetY, (float) offsetZ, (float) extra, count); // Paper - Fix x/y/z coordinate precision loss
        this.getHandle().connection.send(packetplayoutworldparticles);

    }

    @Override
    public org.bukkit.advancement.AdvancementProgress getAdvancementProgress(org.bukkit.advancement.Advancement advancement) {
        Preconditions.checkArgument(advancement != null, "advancement");

        CraftAdvancement craft = (CraftAdvancement) advancement;
        PlayerAdvancements data = this.getHandle().getAdvancements();
        AdvancementProgress progress = data.getOrStartProgress(craft.getHandle());

        return new CraftAdvancementProgress(craft, data, progress);
    }

    @Override
    public int getClientViewDistance() {
        return (this.getHandle().clientViewDistance == null) ? Bukkit.getViewDistance() : this.getHandle().clientViewDistance;
    }

    // Paper start
    @Override
    public java.util.Locale locale() {
        return getHandle().adventure$locale;
    }
    // Paper end
    @Override
    public int getPing() {
        return this.getHandle().latency;
    }

    @Override
    public String getLocale() {
        // Paper start - Locale change event
        final String locale = this.getHandle().locale;
        return locale != null ? locale : "en_us";
        // Paper end
    }

    // Paper start
    public void setAffectsSpawning(boolean affects) {
        this.getHandle().affectsSpawning = affects;
    }

    @Override
    public boolean getAffectsSpawning() {
        return this.getHandle().affectsSpawning;
    }

    @Override
    public void setResourcePack(@NotNull String url, @NotNull String hash) {
        this.setResourcePack(url, hash, false, null);
    }

    @Override
    public void setResourcePack(@NotNull String url, @NotNull String hash, boolean required) {
        this.setResourcePack(url, hash, required, null);
    }

    @Override
    public void setResourcePack(@NotNull String url, @NotNull String hash, boolean required, net.kyori.adventure.text.Component resourcePackPrompt) {
        Validate.notNull(url, "Resource pack URL cannot be null");
        Validate.notNull(hash, "Hash cannot be null");
        net.minecraft.network.chat.Component promptComponent = resourcePackPrompt != null ?
                            io.papermc.paper.adventure.PaperAdventure.asVanilla(resourcePackPrompt) :
                           null;
        this.getHandle().sendTexturePack(url, hash, required, promptComponent);
    }

    @Override
    public org.bukkit.event.player.PlayerResourcePackStatusEvent.Status getResourcePackStatus() {
        return this.resourcePackStatus;
    }

    @Override
    public String getResourcePackHash() {
        return this.resourcePackHash;
    }

    @Override
    public boolean hasResourcePack() {
        return this.resourcePackStatus == org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED;
    }

    public void setResourcePackStatus(org.bukkit.event.player.PlayerResourcePackStatusEvent.Status status) {
        this.resourcePackStatus = status;
    }
    // Paper end

    @Override
    public void updateCommands() {
        if (this.getHandle().connection == null) return;

        this.getHandle().server.getCommands().sendCommands(this.getHandle());
    }

    @Override
    public void openBook(ItemStack book) {
        Validate.isTrue(book != null, "book == null");
        Validate.isTrue(book.getType() == Material.WRITTEN_BOOK, "Book must be Material.WRITTEN_BOOK");

        ItemStack hand = getInventory().getItemInMainHand();
        getInventory().setItemInMainHand(book);
        this.getHandle().openItemGui(org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack.asNMSCopy(book), net.minecraft.world.InteractionHand.MAIN_HAND);
        getInventory().setItemInMainHand(hand);
    }

    @Override
    public void openSign(Sign sign) {
        CraftSign.openSign(sign, this);
    }

    @Override
    public void showDemoScreen() {
        if (this.getHandle().connection == null) return;

        this.getHandle().connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, ClientboundGameEventPacket.DEMO_PARAM_INTRO));
    }

    @Override
    public boolean isAllowingServerListings() {
        return this.getHandle().allowsListing();
    }

    // Paper start
    @Override
    public net.kyori.adventure.text.Component displayName() {
        return this.getHandle().adventure$displayName;
    }

    @Override
    public void displayName(final net.kyori.adventure.text.Component displayName) {
        this.getHandle().adventure$displayName = displayName != null ? displayName : net.kyori.adventure.text.Component.text(this.getName());
        this.getHandle().displayName = null;
    }

    @Override
    public void sendMessage(final net.kyori.adventure.identity.Identity identity, final net.kyori.adventure.text.Component message, final net.kyori.adventure.audience.MessageType type) {
        if (getHandle().connection == null) return;
        final net.minecraft.core.Registry<net.minecraft.network.chat.ChatType> chatTypeRegistry = this.getHandle().level.registryAccess().registryOrThrow(net.minecraft.core.Registry.CHAT_TYPE_REGISTRY);
        this.getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundSystemChatPacket(message, false));
    }

    @Override
    public void sendActionBar(final net.kyori.adventure.text.Component message) {
        final net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket packet = new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket((net.minecraft.network.chat.Component) null);
        packet.adventure$text = message;
        this.getHandle().connection.send(packet);
    }

    @Override
    public void sendPlayerListHeader(final net.kyori.adventure.text.Component header) {
        this.playerListHeader = header;
        this.adventure$sendPlayerListHeaderAndFooter();
    }

    @Override
    public void sendPlayerListFooter(final net.kyori.adventure.text.Component footer) {
        this.playerListFooter = footer;
        this.adventure$sendPlayerListHeaderAndFooter();
    }

    @Override
    public void sendPlayerListHeaderAndFooter(final net.kyori.adventure.text.Component header, final net.kyori.adventure.text.Component footer) {
        this.playerListHeader = header;
        this.playerListFooter = footer;
        this.adventure$sendPlayerListHeaderAndFooter();
    }

    private void adventure$sendPlayerListHeaderAndFooter() {
        final ServerGamePacketListenerImpl connection = this.getHandle().connection;
        if (connection == null) return;
        final ClientboundTabListPacket packet = new ClientboundTabListPacket(null, null);
        packet.adventure$header = (this.playerListHeader == null) ? net.kyori.adventure.text.Component.empty() : this.playerListHeader;
        packet.adventure$footer = (this.playerListFooter == null) ? net.kyori.adventure.text.Component.empty() : this.playerListFooter;
        connection.send(packet);
    }

    @Override
    public void showTitle(final net.kyori.adventure.title.Title title) {
        final ServerGamePacketListenerImpl connection = this.getHandle().connection;
        final net.kyori.adventure.title.Title.Times times = title.times();
        if (times != null) {
            connection.send(new ClientboundSetTitlesAnimationPacket(ticks(times.fadeIn()), ticks(times.stay()), ticks(times.fadeOut())));
        }
        final ClientboundSetSubtitleTextPacket sp = new ClientboundSetSubtitleTextPacket((net.minecraft.network.chat.Component) null);
        sp.adventure$text = title.subtitle();
        connection.send(sp);
        final ClientboundSetTitleTextPacket tp = new ClientboundSetTitleTextPacket((net.minecraft.network.chat.Component) null);
        tp.adventure$text = title.title();
        connection.send(tp);
    }

    @Override
    public <T> void sendTitlePart(final net.kyori.adventure.title.TitlePart<T> part, T value) {
        java.util.Objects.requireNonNull(part, "part");
        java.util.Objects.requireNonNull(value, "value");
        if (part == net.kyori.adventure.title.TitlePart.TITLE) {
            final ClientboundSetTitleTextPacket tp = new ClientboundSetTitleTextPacket((net.minecraft.network.chat.Component) null);
            tp.adventure$text = (net.kyori.adventure.text.Component) value;
            this.getHandle().connection.send(tp);
        } else if (part == net.kyori.adventure.title.TitlePart.SUBTITLE) {
            final ClientboundSetSubtitleTextPacket sp = new ClientboundSetSubtitleTextPacket((net.minecraft.network.chat.Component) null);
            sp.adventure$text = (net.kyori.adventure.text.Component) value;
            this.getHandle().connection.send(sp);
        } else if (part == net.kyori.adventure.title.TitlePart.TIMES) {
            final net.kyori.adventure.title.Title.Times times = (net.kyori.adventure.title.Title.Times) value;
            this.getHandle().connection.send(new ClientboundSetTitlesAnimationPacket(ticks(times.fadeIn()), ticks(times.stay()), ticks(times.fadeOut())));
        } else {
            throw new IllegalArgumentException("Unknown TitlePart");
        }
    }

    private static int ticks(final java.time.Duration duration) {
        if (duration == null) {
            return -1;
        }
        return (int) (duration.toMillis() / 50L);
    }

    @Override
    public void clearTitle() {
        this.getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundClearTitlesPacket(false));
    }

    // resetTitle implemented above

    @Override
    public void showBossBar(final net.kyori.adventure.bossbar.BossBar bar) {
        ((net.kyori.adventure.bossbar.HackyBossBarPlatformBridge) bar).paper$playerShow(this);
    }

    @Override
    public void hideBossBar(final net.kyori.adventure.bossbar.BossBar bar) {
        ((net.kyori.adventure.bossbar.HackyBossBarPlatformBridge) bar).paper$playerHide(this);
    }

    @Override
    public void playSound(final net.kyori.adventure.sound.Sound sound) {
        final Vec3 pos = this.getHandle().position();
        this.playSound(sound, pos.x, pos.y, pos.z);
    }

    @Override
    public void playSound(final net.kyori.adventure.sound.Sound sound, final double x, final double y, final double z) {
        final ResourceLocation name = io.papermc.paper.adventure.PaperAdventure.asVanilla(sound.name());
        final java.util.Optional<net.minecraft.sounds.SoundEvent> event = net.minecraft.core.Registry.SOUND_EVENT.getOptional(name);
        if (event.isPresent()) {
            this.getHandle().connection.send(new ClientboundSoundPacket(event.get(), io.papermc.paper.adventure.PaperAdventure.asVanilla(sound.source()), x, y, z, sound.volume(), sound.pitch(), this.getHandle().getRandom().nextLong())); // TODO adventure sound seed
        } else {
            this.getHandle().connection.send(new ClientboundCustomSoundPacket(name, io.papermc.paper.adventure.PaperAdventure.asVanilla(sound.source()), new Vec3(x, y, z), sound.volume(), sound.pitch(), this.getHandle().getRandom().nextLong())); // TODO adventure sound seed
        }
    }

    @Override
    public void playSound(final net.kyori.adventure.sound.Sound sound, final net.kyori.adventure.sound.Sound.Emitter emitter) {
        final Entity entity;
        if (emitter == net.kyori.adventure.sound.Sound.Emitter.self()) {
            entity = this.getHandle();
        } else if (emitter instanceof org.bukkit.entity.Entity) {
            entity = ((CraftEntity) emitter).getHandle();
        } else {
            throw new IllegalArgumentException("Sound emitter must be an Entity or self(), but was: " + emitter);
        }

        final ResourceLocation name = io.papermc.paper.adventure.PaperAdventure.asVanilla(sound.name());
        final java.util.Optional<net.minecraft.sounds.SoundEvent> event = net.minecraft.core.Registry.SOUND_EVENT.getOptional(name);
        if (event.isPresent()) {
            this.getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundSoundEntityPacket(event.get(), io.papermc.paper.adventure.PaperAdventure.asVanilla(sound.source()), entity, sound.volume(), sound.pitch(), this.getHandle().getRandom().nextLong())); // TODO adventure sound seed
        } else {
            this.getHandle().connection.send(new ClientboundCustomSoundPacket(name, io.papermc.paper.adventure.PaperAdventure.asVanilla(sound.source()), entity.position(), sound.volume(), sound.pitch(), this.getHandle().getRandom().nextLong())); // TODO adventure sound seed
        }
    }

    @Override
    public void stopSound(final net.kyori.adventure.sound.SoundStop stop) {
        this.getHandle().connection.send(new ClientboundStopSoundPacket(
            io.papermc.paper.adventure.PaperAdventure.asVanillaNullable(stop.sound()),
            io.papermc.paper.adventure.PaperAdventure.asVanillaNullable(stop.source())
        ));
    }

    @Override
    public void openBook(final net.kyori.adventure.inventory.Book book) {
        final java.util.Locale locale = this.getHandle().adventure$locale;
        final net.minecraft.world.item.ItemStack item = io.papermc.paper.adventure.PaperAdventure.asItemStack(book, locale);
        final ServerPlayer player = this.getHandle();
        final ServerGamePacketListenerImpl connection = player.connection;
        final net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
        final int slot = inventory.items.size() + inventory.selected;
        final int stateId = getHandle().containerMenu.getStateId();
        connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(0, stateId, slot, item));
        connection.send(new net.minecraft.network.protocol.game.ClientboundOpenBookPacket(net.minecraft.world.InteractionHand.MAIN_HAND));
        connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(0, stateId, slot, inventory.getSelected()));
    }

    @Override
    public net.kyori.adventure.pointer.Pointers pointers() {
        if (this.adventure$pointers == null) {
            this.adventure$pointers = net.kyori.adventure.pointer.Pointers.builder()
                .withDynamic(net.kyori.adventure.identity.Identity.DISPLAY_NAME, this::displayName)
                .withDynamic(net.kyori.adventure.identity.Identity.NAME, this::getName)
                .withDynamic(net.kyori.adventure.identity.Identity.UUID, this::getUniqueId)
                .withStatic(net.kyori.adventure.permission.PermissionChecker.POINTER, this::permissionValue)
                .withDynamic(net.kyori.adventure.identity.Identity.LOCALE, this::locale)
                .build();
        }

        return this.adventure$pointers;
    }

    @Override
    public float getCooldownPeriod() {
        return getHandle().getCurrentItemAttackStrengthDelay();
    }

    @Override
    public float getCooledAttackStrength(float adjustTicks) {
        return getHandle().getAttackStrengthScale(adjustTicks);
    }

    @Override
    public void resetCooldown() {
        getHandle().resetAttackStrengthTicker();
    }

    @Override
    public void remove() {
        if (this.getHandle().getClass().equals(ServerPlayer.class)) { // special case for NMS plugins inheriting
            throw new UnsupportedOperationException("Calling Entity#remove on players produces undefined (bad) behavior");
        } else {
            super.remove();
        }
    }
    // Paper end
    // Spigot start
    private final Player.Spigot spigot = new Player.Spigot()
    {

        @Override
        public InetSocketAddress getRawAddress()
        {
            return (InetSocketAddress) CraftPlayer.this.getHandle().connection.connection.getRawAddress();
        }

        @Override
        public boolean getCollidesWithEntities() {
            return CraftPlayer.this.isCollidable();
        }

        @Override
        public void setCollidesWithEntities(boolean collides) {
            CraftPlayer.this.setCollidable(collides);
        }

        @Override
        public void respawn()
        {
            if ( CraftPlayer.this.getHealth() <= 0 && CraftPlayer.this.isOnline() )
            {
                server.getServer().getPlayerList().respawn( CraftPlayer.this.getHandle(), false );
            }
        }

        @Override
        public Set<Player> getHiddenPlayers()
        {
            Set<Player> ret = new HashSet<Player>();
            for ( UUID u : CraftPlayer.this.hiddenEntities.keySet() )
            {
                Player p = getServer().getPlayer( u );
                if ( p != null )
                {
                    ret.add( p );
                }
            }

            return java.util.Collections.unmodifiableSet( ret );
        }

        @Override
        public void sendMessage(BaseComponent component) {
          sendMessage( new BaseComponent[] { component } );
        }

        @Override
        public void sendMessage(BaseComponent... components) {
           this.sendMessage(net.md_5.bungee.api.ChatMessageType.SYSTEM, components);
        }

        @Override
        public void sendMessage(UUID sender, BaseComponent component) {
            this.sendMessage(net.md_5.bungee.api.ChatMessageType.CHAT, sender, component);
        }

        @Override
        public void sendMessage(UUID sender, BaseComponent... components) {
            this.sendMessage(net.md_5.bungee.api.ChatMessageType.CHAT, sender, components);
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.ChatMessageType position, BaseComponent component) {
            sendMessage( position, new BaseComponent[] { component } );
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.ChatMessageType position, BaseComponent... components) {
            this.sendMessage(position, null, components);
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.ChatMessageType position, UUID sender, BaseComponent component) {
            sendMessage( position, sender, new BaseComponent[] { component } );
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.ChatMessageType position, UUID sender, BaseComponent... components) {
            if ( CraftPlayer.this.getHandle().connection == null ) return;

            CraftPlayer.this.getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundSystemChatPacket(components, position == net.md_5.bungee.api.ChatMessageType.ACTION_BAR));
        }

        // Paper start
        @Override
        public int getPing()
        {
            return getHandle().latency;
        }
        // Paper end
    };

    // Paper start - brand support
    @Override
    public String getClientBrandName() {
        return getHandle().connection != null ? getHandle().connection.getClientBrandName() : null;
    }
    // Paper end

    // Paper start
    @Override
    public void showElderGuardian(boolean silent) {
        if (getHandle().connection != null) getHandle().connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, silent ? 0F : 1F));
    }
    // Paper end

    public Player.Spigot spigot()
    {
        return this.spigot;
    }
    // Spigot end
}
