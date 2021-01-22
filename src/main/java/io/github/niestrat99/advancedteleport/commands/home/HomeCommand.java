package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Homes;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (NewConfig.getInstance().USE_HOMES.get()) {
            if (sender.hasPermission("at.member.home")) {
                if (sender instanceof Player) {
                    Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {

                    ATPlayer atPlayer = ATPlayer.getPlayer((Player)sender);
                    Player player = atPlayer.getPlayer();

                    HashMap<String, Home> homes = atPlayer.getHomes();
                    if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
                        player.sendMessage(CustomMessages.getString("Error.onCountdown"));
                        return;
                    }
                    int cooldown = CooldownManager.secondsLeftOnCooldown("home", player);
                    if (cooldown > 0) {
                        sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(cooldown)));
                        return;
                    }

                    if (args.length > 0) {
                        if (sender.hasPermission("at.admin.home")) {
                            ATPlayer target = ATPlayer.getPlayer(args[0]);
                            if (target != null) {
                                if (args.length > 1) {
                                    OfflinePlayer targetPlayer = target.getOfflinePlayer();
                                    String uuidOther = targetPlayer.getUniqueId().toString();

                                    HashMap<String, Home> homesOther = target.getHomes();
                                    try {
                                        Home home;
                                        switch (args[1].toLowerCase()) {
                                            case "bed":
                                                if (NewConfig.getInstance().ADD_BED_TO_HOMES.get()) {
                                                    loc = player.getBedSpawnLocation();
                                                    if (loc == null) {
                                                        player.sendMessage(CustomMessages.getString("Error.noBedHomeOther").replaceAll("\\{player}", args[0]));
                                                        return;
                                                    }
                                                } else {
                                                    if (homesOther.containsKey(args[1])) {
                                                        loc = homesOther.get(args[1]);
                                                    } else {
                                                        sender.sendMessage(CustomMessages.getString("Error.noSuchHome"));
                                                        return;
                                                    }
                                                }

                                                break;
                                            case "list":
                                                Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> Bukkit.dispatchCommand(sender, "advancedteleport:homes " + args[0]));
                                                return;
                                            default:
                                                if (homesOther.containsKey(args[1])) {
                                                    loc = homesOther.get(args[1]);
                                                } else {
                                                    sender.sendMessage(CustomMessages.getString("Error.noSuchHome"));
                                                    return;
                                                }
                                        }
                                        Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> {
                                            PaperLib.teleportAsync(player, loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                                            sender.sendMessage(CustomMessages.getString("Teleport.teleportingToHomeOther")
                                                    .replaceAll("\\{player}", args[0])
                                                    .replaceAll("\\{home}", args[1]));
                                        });
                                    } catch (NullPointerException ex) {
                                        Location tlocation = Homes.getHomes(uuidOther).get(args[1]);
                                        PaperLib.teleportAsync(player, tlocation, PlayerTeleportEvent.TeleportCause.COMMAND);
                                        sender.sendMessage(CustomMessages.getString("Teleport.teleportingToHomeOther")
                                                .replaceAll("\\{player}", args[0]).replaceAll("\\{home}", args[1]));
                                    }
                                    return;
                                }
                            }
                        }
                    } else {
                        if (homes.size() == 1) {
                            String name = homes.keySet().iterator().next();
                            teleport(player, homes.get(name), name);
                        } else {
                            sender.sendMessage(CustomMessages.getString("Error.noHomeInput"));
                        }
                        return;
                    }

                    if (PaymentManager.getInstance().canPay("home", player)) {
                        try {
                            if (Homes.getHomes(uuid).containsKey(args[0])) {
                                Location location = Homes.getHomes(uuid).get(args[0]);
                                teleport(player, location, args[0]);
                            } else if (args[0].equalsIgnoreCase("bed")  && NewConfig.getInstance().ADD_BED_TO_HOMES.get()) {
                                Location location = player.getBedSpawnLocation();
                                if (location == null) {
                                    player.sendMessage(CustomMessages.getString("Error.noBedHome"));
                                    return;
                                }
                                teleport(player, location, args[0]);

                            } else if (args[0].equalsIgnoreCase("list")) {
                                Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> Bukkit.dispatchCommand(sender, "advancedteleport:homes " + args[0]));
                            } else {
                                sender.sendMessage(CustomMessages.getString("Error.noSuchHome"));
                            }
                        } catch (NullPointerException ex) {
                            Location location = Homes.getHomes(uuid).get(args[0]);
                            teleport(player, location, args[0]);
                        }
                    }
                    });
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return true;
        }
        return true;
    }

    public static void teleport(Player player, Location loc, String name) {
        Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> {
            ATTeleportEvent event = new ATTeleportEvent(player, loc, player.getLocation(), name, ATTeleportEvent.TeleportType.HOME);
            if (!event.isCancelled()) {
                if (PaymentManager.getInstance().canPay("home", player)) {
                    // If the cooldown is to be applied after request or accept (they are the same in the case of /home), apply it now
                    String cooldownConfig = NewConfig.getInstance().APPLY_COOLDOWN_AFTER.get();
                    if (cooldownConfig.equalsIgnoreCase("request") || cooldownConfig.equalsIgnoreCase("accept")) {
                        CooldownManager.addToCooldown("home", player);
                    }
                    int warmUp = NewConfig.getInstance().WARM_UPS.HOME.get();
                    if (warmUp > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                        MovementManager.createMovementTimer(player, loc, "home", "Teleport.teleportingToHome", warmUp, "\\{home}", name);
                    } else {
                        player.sendMessage(CustomMessages.getString("Teleport.teleportingToHome").replaceAll("\\{home}",name));
                        PaperLib.teleportAsync(player, loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                        PaymentManager.getInstance().withdraw("home", player);
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("at.admin.home")) {
                if (!args[0].isEmpty() && args.length == 2) {
                    ATPlayer target = ATPlayer.getPlayer(args[0]);
                    if (target == null) return new ArrayList<>();
                    StringUtil.copyPartialMatches(args[1], target.getHomes().keySet(), results);
                }
            }
            if (args.length == 1) {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                StringUtil.copyPartialMatches(args[0], atPlayer.getHomes().keySet(), results);
            }
        }
        return results;
    }
}
