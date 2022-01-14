package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.utilities.AcceptRequest;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import io.github.niestrat99.advancedteleport.utilities.TeleportTests;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpYes implements ATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (!NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }

        if (!sender.hasPermission("at.member.yes")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }

        Player player = (Player) sender;
        TPRequest request = TeleportTests.teleportTests(player, args, "tpayes");
        if (request != null) {
            // It's not null, we've already run the tests to make sure it isn't
            AcceptRequest.acceptRequest(request);
            // If the cooldown is to be applied after the request is accepted, apply it now
            if (NewConfig.get().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("accept")) {
                CooldownManager.addToCooldown(request.getType() == TPRequest.TeleportType.TPAHERE ? "tpahere" : "tpa", request.getRequester());
            }
        }

        return true;
    }
}
