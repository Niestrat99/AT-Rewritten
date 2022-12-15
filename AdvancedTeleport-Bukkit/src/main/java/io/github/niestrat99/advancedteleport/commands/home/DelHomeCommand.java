package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DelHomeCommand extends AbstractHomeCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        final var atPlayer = ATPlayer.getPlayer(player);
        if (PluginHookManager.get().floodgateEnabled() && atPlayer instanceof ATFloodgatePlayer atFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
            atFloodgatePlayer.sendDeleteHomeForm();
        } else CustomMessages.sendMessage(sender, "Error.noHomeInput");

        return true;
    }

    private void delHome(OfflinePlayer player, Player sender, String name) {
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        if (!atPlayer.hasHome(name)) {
            CustomMessages.sendMessage(sender, "Error.noSuchHome");
            return;
        }

        atPlayer.removeHome(name, sender).whenComplete((ignored, err) -> CustomMessages.failableContextualPath(
            sender,
            player,
            "Info.homeDeleted",
            "Error.deleteHomeFailed",
            () -> err == null,
            "{home}", name, "{player}", player.getName()
        ));
    }

    private void delHome(Player player, String name) {
        delHome(player, player, name);
    }

    @Override
    public String getPermission() {
        return "at.member.delhome";
    }
}
