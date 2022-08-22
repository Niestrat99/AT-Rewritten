package io.github.niestrat99.advancedteleport.commands.warp;

<<<<<<< HEAD
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
=======
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
>>>>>>> d6f3cc8 (Fix code errors and deprecation)
import io.github.niestrat99.advancedteleport.config.CustomMessages;
<<<<<<< HEAD
=======
import io.github.niestrat99.advancedteleport.config.NewConfig;
<<<<<<< HEAD
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
>>>>>>> 771f0be (Add config option to toggle forms)
=======
>>>>>>> d6f3cc8 (Fix code errors and deprecation)
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetWarpCommand extends AbstractWarpCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendSetWarpForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noWarpInput");
            }
            return true;
        }


        Location warp = player.getLocation();

        if (!AdvancedTeleportAPI.getWarps().containsKey(args[0])) {
            AdvancedTeleportAPI.setWarp(args[0], player, warp).thenAcceptAsync(result ->
                    CustomMessages.sendMessage(sender, "Info.setWarp", "{warp}", args[0]));
        } else {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.admin.setwarp";
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                                      @NotNull String[] args) {
        return null;
    }
}
