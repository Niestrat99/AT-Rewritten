package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.extensions.ExPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class HomesCommand extends AbstractHomeCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        if (args.length > 0 && sender.hasPermission("at.admin.homes")) {
            ATPlayer.getPlayerFuture(args[0]).thenAccept(player -> {
                if (player.getHomes() == null || player.getHomes().size() == 0) {
                    CustomMessages.sendMessage(sender, "Error.homesNotLoaded");
                    return;
                }
                getHomes(sender, player.getOfflinePlayer());
            });
            return true;
        }

        getHomes(sender, (Player) sender);
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.homes";
    }

    @Override
    public boolean getRequiredFeature() {
        return NewConfig.get().USE_HOMES.get();
    }

    private void getHomes(CommandSender sender, OfflinePlayer target) {
        ATPlayer atPlayer = ATPlayer.getPlayer(target);

        if (atPlayer instanceof ATFloodgatePlayer atFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
            atFloodgatePlayer.sendHomeForm();
            return;
        }

        final var body = Component.join(
            JoinConfiguration.commas(true),
            atPlayer.getHomes().values().stream()
                .map(home -> new Object[]{home, atPlayer.canAccessHome(home) || ExPermission.hasPermissionOrStar(sender, "at.admin.homes")}) // How the fuck do you associate a value like a pair in java?
                .map(pair -> {
                    final var home = (Home) pair[0];
                    final var canAccess = (boolean) pair[1];
                    final var baseComponent = Component.text(home.getName())
                        .hoverEvent(CustomMessages.locationBasedTooltip(sender, home.getLocation(), "homes"));

                    if (!canAccess) {
                        if (!NewConfig.get().HIDE_HOMES_IF_DENIED.get()) return Component.empty(); // TODO: Make sure this doesn't cause an extra comma.
                        return baseComponent.color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC);
                    }

                    return baseComponent.clickEvent(ClickEvent.runCommand("/home " + (sender == target ? "" : target.getName() + " ") + home.getName()));
                }).toList()
        );

        if (!body.children().isEmpty()) {
            CustomMessages.sendMessage(sender, "Info.warps");
            CustomMessages.asAudience(sender).sendMessage(body);
        } else CustomMessages.sendMessage(
            sender,
            CustomMessages.contextualPath(sender, target, "Error.noHomes"),
            "player", (Supplier<String>) target::getName // TODO: DisplayName
        );
    }
}
