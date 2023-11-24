package io.github.niestrat99.advancedteleport.commands.home;

import com.google.common.collect.ImmutableCollection;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.extensions.ExPermission;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class HomesCommand extends AbstractHomeCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;

        if (args.length > 0 && sender.hasPermission("at.admin.homes")) {
            ATPlayer.getPlayerFuture(args[0])
                    .thenAccept(
                            player ->
                                    player.getHomesAsync()
                                            .thenAcceptAsync(
                                                    homes ->
                                                            getHomes(
                                                                    sender,
                                                                    player.getOfflinePlayer(),
                                                                    homes.values())));
            return true;
        }

        if (!(sender instanceof Player player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        atPlayer.getHomesAsync().thenAcceptAsync(homes -> getHomes(sender, player, homes.values()));
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.homes";
    }

    @Override
    public @NotNull String getAdminPermission() {
        return "at.admin.homes";
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_HOMES.get();
    }

    private void getHomes(
            CommandSender sender, OfflinePlayer target, ImmutableCollection<Home> homes) {
        ATPlayer atPlayer = ATPlayer.getPlayer(target);

        if (sender == target
                && atPlayer instanceof ATFloodgatePlayer atFloodgatePlayer
                && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
            atFloodgatePlayer.sendHomeForm();
            return;
        }

        final TextComponent body =
                (TextComponent)
                        Component.join(
                                JoinConfiguration.commas(true),
                                atPlayer.getHomes().values().stream()
                                        .map(
                                                home ->
                                                        new Object[] {
                                                            home,
                                                            atPlayer.canAccessHome(home)
                                                                    || ExPermission
                                                                            .hasPermissionOrStar(
                                                                                    sender,
                                                                                    "at.admin.homes")
                                                        }) // How the fuck do you associate a value
                                                           // like a pair in java?
                                        .map(
                                                pair -> {
                                                    final var home = (Home) pair[0];
                                                    final var canAccess = (boolean) pair[1];
                                                    final var baseComponent =
                                                            Component.text(home.getName())
                                                                    .hoverEvent(
                                                                            CustomMessages
                                                                                    .locationBasedTooltip(
                                                                                            sender,
                                                                                            home,
                                                                                            "homes"));

                                                    if (!canAccess) {
                                                        if (!MainConfig.get()
                                                                .HIDE_HOMES_IF_DENIED
                                                                .get())
                                                            return Component
                                                                    .empty(); // TODO: Make sure
                                                                              // this doesn't cause
                                                                              // an extra comma.
                                                        return baseComponent
                                                                .color(NamedTextColor.GRAY)
                                                                .decorate(TextDecoration.ITALIC);
                                                    }

                                                    return baseComponent.clickEvent(
                                                            ClickEvent.runCommand(
                                                                    "/advancedteleport:home "
                                                                            + (sender == target
                                                                                    ? ""
                                                                                    : target
                                                                                                    .getName()
                                                                                            + " ")
                                                                            + home.getName()));
                                                })
                                        .toList());

        if (!body.content().isEmpty() || !body.children().isEmpty()) {
            String text = CustomMessages.config.getString("Info.homes") + "<homes>";
            CustomMessages.asAudience(sender).sendMessage(CustomMessages.translate(text, Placeholder.component("homes", body)));
        } else
            CustomMessages.sendMessage(
                    sender,
                    CustomMessages.contextualPath(sender, target, "Error.noHomes"),
                    Placeholder.unparsed("player", target.getName()) // TODO: DisplayName
                    );
    }
}
