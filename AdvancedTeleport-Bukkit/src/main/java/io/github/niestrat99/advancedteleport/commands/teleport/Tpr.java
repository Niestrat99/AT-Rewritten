package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.commands.TimedATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.RTPManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Tpr extends ATCommand implements TimedATCommand {

    private static final List<UUID> searchingPlayers = new ArrayList<>();

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;
        Player player;
        if (args.length > 1 && sender.hasPermission("at.admin.tpr.other")) {
            player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                return true;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        World world = player.getWorld();
        if (args.length > 0 && sender.hasPermission("at.member.tpr.other")) {
            World otherWorld = Bukkit.getWorld(args[0]);
            if (otherWorld != null) {
                world = otherWorld;
            } else {
                CustomMessages.sendMessage(sender, "Error.noSuchWorld");
                return true;
            }
        }

        return randomTeleport(player, sender, world);
    }

    public static boolean randomTeleport(Player player, World world) {
        return randomTeleport(player, player, world);
    }

    public static boolean randomTeleport(Player player, CommandSender sender, World world) {
        int cooldown = CooldownManager.secondsLeftOnCooldown("tpr", player);
        if (cooldown > 0) {
            CustomMessages.sendMessage(sender, "Error.onCooldown", "{time}", String.valueOf(cooldown));
            return true;
        }
        if (MainConfig.get().WHITELIST_WORLD.get() && !sender.hasPermission("at.admin.rtp.bypass-world")) {
            List<String> allowedWorlds = MainConfig.get().ALLOWED_WORLDS.get();
            if (!allowedWorlds.contains(world.getName())) {
                if (allowedWorlds.isEmpty() || !MainConfig.get().REDIRECT_TO_WORLD.get()) {
                    CustomMessages.sendMessage(sender, "Error.cantTPToWorld");
                    return true;
                } else {
                    for (String worldName : allowedWorlds) {
                        world = Bukkit.getWorld(worldName);
                        String conditionResult = ConditionChecker.canTeleport(new Location(player.getWorld(), 0,
                                0, 0), new Location(world, 0, 0, 0), "tpr", player);
                        if (world != null && conditionResult == null) break;
                    }
                    if (world == null) {
                        CustomMessages.sendMessage(sender, "Error.cantTPToWorld");
                        return true;
                    }
                }
            }
        }

        if (searchingPlayers.contains(player.getUniqueId())) {
            CustomMessages.sendMessage(sender, "Error.alreadySearching");
            return true;
        }

        String conditionResult = ConditionChecker.canTeleport(new Location(player.getWorld(), 0, 0, 0),
                new Location(world, 0, 0, 0), "tpr", player);
        if (conditionResult != null) {
            CustomMessages.sendMessage(player, conditionResult, "{world}", world.getName());
            return true;
        }

        if (!PaymentManager.getInstance().canPay("tpr", player)) return false;

        if (MainConfig.get().RAPID_RESPONSE.get() && PaperLib.isPaper()) {
            Location nextLoc = RTPManager.getLocationUrgently(world);
            if (nextLoc != null) {
                processLocation(player, nextLoc);
            } else {
                CustomMessages.sendMessage(player, "Info.searching");
                searchingPlayers.add(player.getUniqueId());
                RTPManager.getNextAvailableLocation(world).thenAccept(location -> processLocation(player, location));
            }
        } else {
            CustomMessages.sendMessage(player, "Info.searching");
            searchingPlayers.add(player.getUniqueId());
            RandomTPAlgorithms.getAlgorithms().get("binary").fire(player, world,
                    location -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> processLocation(player, location)));
        }

        return true;
    }

    private static void processLocation(Player player, Location location) {
        searchingPlayers.remove(player.getUniqueId());
        ATPlayer atPlayer = ATPlayer.getPlayer(player);
        ATTeleportEvent event = new ATTeleportEvent(player, location, player.getLocation(), "", ATTeleportEvent.TeleportType.TPR);
        Bukkit.getPluginManager().callEvent(event);
        atPlayer.teleport(event, "tpr", "Teleport.teleportingToRandomPlace");
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_RANDOMTP.get();
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.tpr";
    }

    @Override
    public String getSection() {
        return "tpr";
    }

    @Override
    public boolean canProceed(@NotNull CommandSender sender) {

        // Get the
        boolean priorResult = TimedATCommand.super.canProceed(sender);

        if (!priorResult && !(sender instanceof Player) && !sender.hasPermission("at.admin.tpr.other")) return false;

        return priorResult;
    }
}
