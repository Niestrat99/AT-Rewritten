package io.papermc.paper.adventure;

import com.google.common.base.Suppliers;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AbstractChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.util.LazyPlayerSet;
import org.bukkit.craftbukkit.v1_19_R1.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

@DefaultQualifier(NonNull.class)
public final class ChatProcessor {
    static final String DEFAULT_LEGACY_FORMAT = "<%1$s> %2$s"; // copied from PlayerChatEvent/AsyncPlayerChatEvent
    final MinecraftServer server;
    final ServerPlayer player;
    final PlayerChatMessage message;
    final boolean async;
    final String craftbukkit$originalMessage;
    final Component paper$originalMessage;
    final OutgoingPlayerChatMessage outgoing;

    static final int MESSAGE_CHANGED = 1;
    static final int FORMAT_CHANGED = 2;
    static final int SENDER_CHANGED = 3; // Not used
    // static final int FORCE_PREVIEW_USE = 4; // TODO (future, maybe?)
    private final BitSet flags = new BitSet(3);

    public ChatProcessor(final MinecraftServer server, final ServerPlayer player, final PlayerChatMessage message, final boolean async) {
        this.server = server;
        this.player = player;
        /*
        CraftBukkit's preview/decoration system relies on both the "decorate" and chat event making the same modifications. If
        there is unsigned content in the legacyMessage, that is because the player sent the legacyMessage without it being
        previewed (probably by sending it too quickly). We can just ignore that because the same changes will
        happen in the chat event.

        If unsigned content is present, it will be the same as `this.legacyMessage.signedContent().previewResult().component()`.
         */
        this.message = message;
        this.async = async;
        if (this.message.signedContent().decorationResult().modernized()) {
            this.craftbukkit$originalMessage = this.message.signedContent().decorationResult().message().legacyMessage();
        } else {
            this.craftbukkit$originalMessage = message.signedContent().plain();
        }
        /*
        this.paper$originalMessage is the input to paper's chat events. This should be the decorated message component.
        Even if the legacy preview event modified the format, and the client signed the formatted message, this should
        still just be the message component.
         */
        this.paper$originalMessage = this.message.signedContent().decorationResult().message().component();
        this.outgoing = OutgoingPlayerChatMessage.create(this.message);
    }

    @SuppressWarnings("deprecated")
    public void process() {
        final boolean listenersOnAsyncEvent = canYouHearMe(AsyncPlayerChatEvent.getHandlerList());
        final boolean listenersOnSyncEvent = canYouHearMe(PlayerChatEvent.getHandlerList());
        if (listenersOnAsyncEvent || listenersOnSyncEvent) {
            final CraftPlayer player = this.player.getBukkitEntity();
            final AsyncPlayerChatEvent ae = new AsyncPlayerChatEvent(this.async, player, this.craftbukkit$originalMessage, new LazyPlayerSet(this.server));
            this.post(ae);
            if (listenersOnSyncEvent) {
                final PlayerChatEvent se = new PlayerChatEvent(player, ae.getMessage(), ae.getFormat(), ae.getRecipients());
                se.setCancelled(ae.isCancelled()); // propagate cancelled state
                this.queueIfAsyncOrRunImmediately(new Waitable<Void>() {
                    @Override
                    protected Void evaluate() {
                        ChatProcessor.this.post(se);
                        return null;
                    }
                });
                this.readLegacyModifications(se.getMessage(), se.getFormat(), se.getPlayer());
                this.processModern(
                    this.modernRenderer(se.getFormat()),
                    this.viewersFromLegacy(se.getRecipients()),
                    this.modernMessage(se.getMessage()),
                    se.getPlayer(),
                    se.isCancelled()
                );
            } else {
                this.readLegacyModifications(ae.getMessage(), ae.getFormat(), ae.getPlayer());
                this.processModern(
                    this.modernRenderer(ae.getFormat()),
                    this.viewersFromLegacy(ae.getRecipients()),
                    this.modernMessage(ae.getMessage()),
                    ae.getPlayer(),
                    ae.isCancelled()
                );
            }
        } else {
            this.processModern(
                defaultRenderer(),
                new LazyChatAudienceSet(this.server),
                this.paper$originalMessage,
                this.player.getBukkitEntity(),
                false
            );
        }
    }

    private ChatRenderer modernRenderer(final String format) {
        if (this.flags.get(FORMAT_CHANGED)) {
            return legacyRenderer(format);
        } else if (this.message.signedContent().decorationResult() instanceof ChatDecorator.LegacyResult legacyResult) {
            return legacyRenderer(legacyResult.format());
        } else {
            return defaultRenderer();
        }
    }

    private Component modernMessage(final String legacyMessage) {
        if (this.flags.get(MESSAGE_CHANGED)) {
            return legacySection().deserialize(legacyMessage);
        } else if (this.message.unsignedContent().isEmpty() && this.message.signedContent().decorationResult() instanceof ChatDecorator.LegacyResult legacyResult) {
            return legacyResult.message().component();
        } else {
            return this.paper$originalMessage;
        }
    }

    private void readLegacyModifications(final String message, final String format, final Player playerSender) {
        final ChatMessageContent content = this.message.signedContent();
        if (content.decorationResult() instanceof ChatDecorator.LegacyResult result) {
            if ((content.isDecorated() || this.message.unsignedContent().isPresent()) && !result.modernized()) {
                this.flags.set(MESSAGE_CHANGED, !message.equals(result.message().legacyMessage()));
            } else {
                this.flags.set(MESSAGE_CHANGED, !message.equals(this.craftbukkit$originalMessage));
            }
            this.flags.set(FORMAT_CHANGED, !format.equals(result.format()));
        } else {
            this.flags.set(MESSAGE_CHANGED, !message.equals(this.craftbukkit$originalMessage));
            this.flags.set(FORMAT_CHANGED, !format.equals(DEFAULT_LEGACY_FORMAT));
        }
        this.flags.set(SENDER_CHANGED, playerSender != this.player.getBukkitEntity());
    }

    private void processModern(final ChatRenderer renderer, final Set<Audience> viewers, final Component message, final Player player, final boolean cancelled) {
        final AsyncChatEvent ae = new AsyncChatEvent(this.async, player, viewers, renderer, message, this.paper$originalMessage);
        ae.setCancelled(cancelled); // propagate cancelled state
        this.post(ae);
        final boolean listenersOnSyncEvent = canYouHearMe(ChatEvent.getHandlerList());
        if (listenersOnSyncEvent) {
            this.queueIfAsyncOrRunImmediately(new Waitable<Void>() {
                @Override
                protected Void evaluate() {
                    final ChatEvent se = new ChatEvent(player, ae.viewers(), ae.renderer(), ae.message(), ChatProcessor.this.paper$originalMessage/*, ae.usePreviewComponent()*/);
                    se.setCancelled(ae.isCancelled()); // propagate cancelled state
                    ChatProcessor.this.post(se);
                    ChatProcessor.this.readModernModifications(se, renderer);
                    ChatProcessor.this.complete(se);
                    return null;
                }
            });
        } else {
            this.readModernModifications(ae, renderer);
            this.complete(ae);
        }
    }

    private void readModernModifications(final AbstractChatEvent chatEvent, final ChatRenderer originalRenderer) {
        if (this.message.signedContent().isDecorated()) {
            this.flags.set(MESSAGE_CHANGED, !chatEvent.message().equals(this.message.signedContent().decorationResult().message().component()));
        } else {
            this.flags.set(MESSAGE_CHANGED, !chatEvent.message().equals(this.paper$originalMessage));
        }
        if (originalRenderer != chatEvent.renderer()) { // don't set to false if it hasn't changed
            this.flags.set(FORMAT_CHANGED, true);
        }
        // this.flags.set(FORCE_PREVIEW_USE, chatEvent.usePreviewComponent()); // TODO (future, maybe?)
    }

    private void complete(final AbstractChatEvent event) {
        if (event.isCancelled()) {
            this.outgoing.sendHeadersToRemainingPlayers(this.server.getPlayerList());
            return;
        }

        final CraftPlayer player = ((CraftPlayer) event.getPlayer());
        final Component displayName = displayName(player);
        final Component message = event.message();
        final ChatRenderer renderer = event.renderer();

        final Set<Audience> viewers = event.viewers();
        final ResourceKey<ChatType> chatTypeKey = renderer instanceof ChatRenderer.Default ? ChatType.CHAT : ChatType.RAW;
        final ChatType.Bound chatType = ChatType.bind(chatTypeKey, this.player.level.registryAccess(), PaperAdventure.asVanilla(displayName(player)));

        OutgoingChat outgoingChat = viewers instanceof LazyChatAudienceSet lazyAudienceSet && lazyAudienceSet.isLazy() ? new ServerOutgoingChat() : new ViewersOutgoingChat();
        /* if (this.flags.get(FORCE_PREVIEW_USE)) { // TODO (future, maybe?)
            outgoingChat.sendOriginal(player, viewers, chatType);
        } else */
        if (this.flags.get(FORMAT_CHANGED)) {
            if (renderer instanceof ChatRenderer.ViewerUnaware unaware) {
                outgoingChat.sendFormatChangedViewerUnaware(player, PaperAdventure.asVanilla(unaware.render(player, displayName, message)), viewers, chatType);
            } else {
                outgoingChat.sendFormatChangedViewerAware(player, displayName, message, renderer, viewers, chatType);
            }
        } else if (this.flags.get(MESSAGE_CHANGED)) {
            if (!(renderer instanceof ChatRenderer.ViewerUnaware unaware)) {
                throw new IllegalStateException("BUG: There should not be a non-legacy renderer at this point");
            }
            final Component renderedComponent = chatTypeKey == ChatType.CHAT ? message : unaware.render(player, displayName, message);
            outgoingChat.sendMessageChanged(player, PaperAdventure.asVanilla(renderedComponent), viewers, chatType);
        } else {
            outgoingChat.sendOriginal(player, viewers, chatType);
        }
    }

    interface OutgoingChat {
        default void sendFormatChangedViewerUnaware(CraftPlayer player, net.minecraft.network.chat.Component renderedMessage, Set<Audience> viewers, ChatType.Bound chatType) {
            this.sendMessageChanged(player, renderedMessage, viewers, chatType);
        }

        void sendFormatChangedViewerAware(CraftPlayer player, Component displayName, Component message, ChatRenderer renderer, Set<Audience> viewers, ChatType.Bound chatType);

        void sendMessageChanged(CraftPlayer player, net.minecraft.network.chat.Component renderedMessage, Set<Audience> viewers, ChatType.Bound chatType);

        void sendOriginal(CraftPlayer player, Set<Audience> viewers, ChatType.Bound chatType);
    }

    final class ServerOutgoingChat implements OutgoingChat {
        @Override
        public void sendFormatChangedViewerAware(CraftPlayer player, Component displayName, Component message, ChatRenderer renderer, Set<Audience> viewers, ChatType.Bound chatType) {
            ChatProcessor.this.server.getPlayerList().broadcastChatMessage(ChatProcessor.this.message, ChatProcessor.this.player, chatType, viewer -> PaperAdventure.asVanilla(renderer.render(player, displayName, message, viewer)));
        }

        @Override
        public void sendMessageChanged(CraftPlayer player, net.minecraft.network.chat.Component renderedMessage, Set<Audience> viewers, ChatType.Bound chatType) {
            ChatProcessor.this.server.getPlayerList().broadcastChatMessage(ChatProcessor.this.message.withUnsignedContent(renderedMessage), ChatProcessor.this.player, chatType);
        }

        @Override
        public void sendOriginal(CraftPlayer player, Set<Audience> viewers, ChatType.Bound chatType) {
            ChatProcessor.this.server.getPlayerList().broadcastChatMessage(ChatProcessor.this.message, ChatProcessor.this.player, chatType);
        }
    }

    final class ViewersOutgoingChat implements OutgoingChat {
        @Override
        public void sendFormatChangedViewerAware(CraftPlayer player, Component displayName, Component message, ChatRenderer renderer, Set<Audience> viewers, ChatType.Bound chatType) {
            this.broadcastToViewers(viewers, player, chatType, v -> PaperAdventure.asVanilla(renderer.render(player, displayName, message, v)));
        }

        @Override
        public void sendMessageChanged(CraftPlayer player, net.minecraft.network.chat.Component renderedMessage, Set<Audience> viewers, ChatType.Bound chatType) {
            this.broadcastToViewers(viewers, player, chatType, new ConstantFunction(renderedMessage));
        }

        @Override
        public void sendOriginal(CraftPlayer player, Set<Audience> viewers, ChatType.Bound chatType) {
            this.broadcastToViewers(viewers, player, chatType, null);
        }

        private void broadcastToViewers(Collection<Audience> viewers, final Player source, final ChatType.Bound chatType, final @Nullable Function<Audience, net.minecraft.network.chat.Component> msgFunction) {
            final Supplier<Component> fallbackSupplier = Suppliers.memoize(() -> PaperAdventure.asAdventure(msgFunction instanceof ConstantFunction constantFunction ? constantFunction.component : ChatProcessor.this.message.serverContent()));
            final Function<Audience, Component> audienceMsgFunction = !(msgFunction instanceof ConstantFunction || msgFunction == null) ? msgFunction.andThen(PaperAdventure::asAdventure) : viewer -> fallbackSupplier.get();
            for (Audience viewer : viewers) {
                if (viewer instanceof Player || viewer instanceof ConsoleCommandSender) {
                    // players and console have builtin PlayerChatMessage sending support while other audiences do not
                    this.sendToViewer((CommandSender) viewer, chatType, msgFunction);
                } else {
                    viewer.sendMessage(source, audienceMsgFunction.apply(viewer), MessageType.CHAT);
                }
            }

            // Make sure to send remaining headers
            ChatProcessor.this.outgoing.sendHeadersToRemainingPlayers(ChatProcessor.this.server.getPlayerList());
        }

        private void sendToViewer(final CommandSender viewer, final ChatType.Bound chatType, final @Nullable Function<Audience, net.minecraft.network.chat.Component> msgFunction) {
            if (viewer instanceof ConsoleCommandSender) {
                this.sendToServer(chatType, msgFunction);
            } else if (viewer instanceof CraftPlayer craftPlayer) {
                craftPlayer.getHandle().sendChatMessage(ChatProcessor.this.outgoing, ChatProcessor.this.player.shouldFilterMessageTo(craftPlayer.getHandle()), chatType, Util.mapNullable(msgFunction, f -> f.apply(viewer)));
            } else {
                throw new IllegalStateException("Should only be a Player or Console");
            }
        }

        private void sendToServer(final ChatType.Bound chatType, final @Nullable Function<Audience, net.minecraft.network.chat.Component> msgFunction) {
            final PlayerChatMessage toConsoleMessage = msgFunction == null ? ChatProcessor.this.message : ChatProcessor.this.message.withUnsignedContent(msgFunction.apply(ChatProcessor.this.server.console));
            ChatProcessor.this.server.logChatMessage(toConsoleMessage.serverContent(), chatType, ChatProcessor.this.server.getPlayerList().verifyChatTrusted(toConsoleMessage, ChatProcessor.this.player.asChatSender()) ? null : "Not Secure");
        }

        record ConstantFunction(net.minecraft.network.chat.Component component) implements Function<Audience, net.minecraft.network.chat.Component> {
            @Override
            public net.minecraft.network.chat.Component apply(Audience audience) {
                return this.component;
            }
        }
    }

    private Set<Audience> viewersFromLegacy(final Set<Player> recipients) {
        if (recipients instanceof LazyPlayerSet lazyPlayerSet && lazyPlayerSet.isLazy()) {
            return new LazyChatAudienceSet(this.server);
        }
        final HashSet<Audience> viewers = new HashSet<>(recipients);
        viewers.add(this.server.console);
        return viewers;
    }

    static String legacyDisplayName(final CraftPlayer player) {
        if (((org.bukkit.craftbukkit.v1_19_R1.CraftWorld) player.getWorld()).getHandle().paperConfig().scoreboards.useVanillaWorldScoreboardNameColoring) {
            return legacySection().serialize(player.teamDisplayName()) + ChatColor.RESET;
        }
        return player.getDisplayName();
    }

    static Component displayName(final CraftPlayer player) {
        if (((CraftWorld) player.getWorld()).getHandle().paperConfig().scoreboards.useVanillaWorldScoreboardNameColoring) {
            return player.teamDisplayName();
        }
        return player.displayName();
    }

    private static ChatRenderer.Default defaultRenderer() {
        return (ChatRenderer.Default) ChatRenderer.defaultRenderer();
    }

    private static ChatRenderer legacyRenderer(final String format) {
        if (DEFAULT_LEGACY_FORMAT.equals(format)) {
            return defaultRenderer();
        }
        return ChatRenderer.viewerUnaware((player, sourceDisplayName, message) -> legacySection().deserialize(legacyFormat(format, player, legacySection().serialize(message))));
    }

    static String legacyFormat(final String format, Player player, String message) {
        return String.format(format, legacyDisplayName((CraftPlayer) player), message);
    }

    private void queueIfAsyncOrRunImmediately(final Waitable<Void> waitable) {
        if (this.async) {
            this.server.processQueue.add(waitable);
        } else {
            waitable.run();
        }
        try {
            waitable.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt(); // tag, you're it
        } catch (final ExecutionException e) {
            throw new RuntimeException("Exception processing chat", e.getCause());
        }
    }

    private void post(final Event event) {
        this.server.server.getPluginManager().callEvent(event);
    }

    static boolean canYouHearMe(final HandlerList handlers) {
        return handlers.getRegisteredListeners().length > 0;
    }
}
