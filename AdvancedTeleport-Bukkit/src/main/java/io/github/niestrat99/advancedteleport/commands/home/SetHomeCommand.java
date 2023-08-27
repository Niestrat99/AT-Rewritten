package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SetHomeCommand extends AbstractHomeCommand {

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (!canProceed(sender)) return true;

        // If a location has been specified, use that
        if (args.length > 5 && sender.hasPermission("at.admin.sethome.location")) {

            // Set initial variables
            final var targetName = args[0];
            final var homeName = args[1];
            final var worldName = args[2];

            final var xStr = args[3];
            final var yStr = args[4];
            final var zStr = args[5];

            final var yawStr = args.length > 6 ? args[6] : "0";
            final var pitchStr = args.length > 7 ? args[7] : "0";

            // Parse results, including world
            final World world = Bukkit.getWorld(worldName);
            if (world == null) {
                CustomMessages.sendMessage(sender, "Error.noSuchWorld");
                return false;
            }

            try {
                final var x = Double.parseDouble(xStr);
                final var y = Double.parseDouble(yStr);
                final var z = Double.parseDouble(zStr);
                final var yaw = Float.parseFloat(yawStr);
                final var pitch = Float.parseFloat(pitchStr);

                Location location = new Location(world, x, y, z, yaw, pitch);

                AdvancedTeleportAPI.getOfflinePlayer(targetName)
                        .whenCompleteAsync((target, err) -> setHome(sender, target, location, homeName, targetName), CoreClass.sync);
                return true;
            } catch (NumberFormatException ex) {
                CustomMessages.sendMessage(sender, "Error.invalidCoords");
                return false;
            }
        } else if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        // Specify player variables
        final var player = (Player) sender;
        final var atPlayer = ATPlayer.getPlayer(player);

        // If no arguments have been specified, just use the information from that
        if (args.length == 0) {

            // Get the player's homes limit
            final int limit = atPlayer.getHomesLimit();

            // If the homes list is empty, set a new home called "home".
            if (atPlayer.getHomes().size() == 0 && (limit > 0 || limit == -1)) {
                setHome(player, "home");
                return true;
            }

            // If the player is a floodgate player, send them a form, otherwise tell the player to
            // enter some arguments
            if (atPlayer instanceof ATFloodgatePlayer
                    && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendSetHomeForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noHomeInput");
            }

            return true;
        }

        // We'll just assume that the admin command overrides the homes limit.
        if (args.length > 1 && sender.hasPermission("at.admin.sethome")) {

            // Get the player to be targeted.
            AdvancedTeleportAPI.getOfflinePlayer(args[0])
                    .whenCompleteAsync((target, err) -> setHome(player, target, player.getLocation(), args[1], args[0]));
            return true;
        }

        // If the player can set more homes,
        if (atPlayer.canSetMoreHomes()
                || (MainConfig.get().OVERWRITE_SETHOME.get() && atPlayer.hasHome(args[0]))) {
            setHome(player, args[0]);

        } else {
            CustomMessages.sendMessage(sender, "Error.reachedHomeLimit");
        }
        return true;
    }

    private void setHome(Player sender, String name) {
        setHome(sender, sender, sender.getLocation(), name, sender.getName());
    }

    // Separated this into a separate method so that the code is easier to read.
    // Player player - the player which is having the home set.
    // String name - the name of the home.
    private void setHome(CommandSender sender, OfflinePlayer target, Location location, String homeName, String playerName) {

        ATPlayer atPlayer = ATPlayer.getPlayer(target);

        // If the home with that name already exists, and we can't overwrite homes, let the player
        // know
        if (!MainConfig.get().OVERWRITE_SETHOME.get() && atPlayer.hasHome(homeName)) {
            CustomMessages.sendMessage(
                    sender, "Error.homeAlreadySet", Placeholder.unparsed("home", homeName));
            return;
        }

        // Attempt to add the home.
        atPlayer.addHome(homeName, location, sender)
                .whenComplete(
                        (ignored, err) ->
                                CustomMessages.failableContextualPath(
                                        sender,
                                        target,
                                        "Info.setHome",
                                        "Error.setHomeFail",
                                        err,
                                        Placeholder.unparsed("home", homeName),
                                        Placeholder.unparsed("player", playerName)));
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_HOMES.get();
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.sethome";
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {

        List<String> results = new ArrayList<>();

        // If the player can set locations, then check that
        if (sender.hasPermission("at.admin.sethome.location")) {

            // World
            if (args.length == 3) {
                StringUtil.copyPartialMatches(args[2], Bukkit.getWorlds().stream().map(World::getName).toList(), results);
                return results;
            }

            // Coordinates
            if (sender instanceof Player player && args.length <= 8) {
                final Location location = player.getLocation();
                double[] coords = new double[]{
                        location.getBlockX() + 0.5,
                        location.getBlockY(),
                        location.getBlockZ() + 0.5,
                        location.getYaw(),
                        location.getPitch()
                };

                StringUtil.copyPartialMatches(args[args.length - 1], Collections.singleton(String.valueOf(coords[args.length - 3])), results);
                return results;
            }
        }
        return new ArrayList<>();
    }
}
