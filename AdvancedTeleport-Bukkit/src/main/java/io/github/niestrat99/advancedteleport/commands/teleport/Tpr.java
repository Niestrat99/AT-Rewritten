package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tpr implements ATCommand {

    private static List<UUID> searchingPlayers = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (NewConfig.get().USE_RANDOMTP.get()) {
                if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
                    CustomMessages.sendMessage(sender, "Error.onCountdown");
                    return true;
                }
                if (sender.hasPermission("at.member.tpr")) {
                    World world = player.getWorld();
                    if (args.length > 0) {
                        if (sender.hasPermission("at.member.tpr.other")) {
                            World otherWorld = Bukkit.getWorld(args[0]);
                            if (otherWorld != null) {
                                world = otherWorld;
                            } else {
                                CustomMessages.sendMessage(sender, "Error.noSuchWorld");
                                return true;
                            }
                        }
                    }
                    return randomTeleport(player, world);
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.featureDisabled");
                return true;
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
        }
        return true;
    }

    public static boolean randomTeleport(Player player, World world) {
        int cooldown = CooldownManager.secondsLeftOnCooldown("tpr", player);
        if (cooldown > 0) {
            CustomMessages.sendMessage(player, "Error.onCooldown", "{time}", String.valueOf(cooldown));
            return true;
        }
        if (NewConfig.get().WHITELIST_WORLD.get()) {
            List<String> allowedWorlds = NewConfig.get().ALLOWED_WORLDS.get();
            if (!allowedWorlds.contains(world.getName())) {
                if (!player.hasPermission("at.admin.rtp.bypass-world")) {
                    if (allowedWorlds.isEmpty()) {
                        CustomMessages.sendMessage(player, "Error.cantTPToWorld");
                        return true;
                    } else {
                        for (String worldName : allowedWorlds) {
                            world = Bukkit.getWorld(worldName);
                            if (world != null) break;
                        }
                        if (world == null) {
                            CustomMessages.sendMessage(player, "Error.cantTPToWorld");
                            return true;
                        }
                    }
                }
            }
        }
        if (searchingPlayers.contains(player.getUniqueId())) {
            CustomMessages.sendMessage(player, "Error.alreadySearching");
            return true;
        }
        if (!PaymentManager.getInstance().canPay("tpr", player)) return false;
        CustomMessages.sendMessage(player, "Info.searching");
        searchingPlayers.add(player.getUniqueId());
        RandomTPAlgorithms.getAlgorithms().get("binary").fire(player, world, location -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> {
            searchingPlayers.remove(player.getUniqueId());
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            ATTeleportEvent event = new ATTeleportEvent(player, location, player.getLocation(), "", ATTeleportEvent.TeleportType.TPR);
            atPlayer.teleport(event, "tpr", "Teleport.teleportingToRandomPlace", NewConfig.get().WARM_UPS.TPR.get());
        }));
        return true;
    }
}