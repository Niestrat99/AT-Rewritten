package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Homes;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.events.CooldownManager;
import io.github.niestrat99.advancedteleport.events.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class Home implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (NewConfig.getInstance().USE_HOMES.get()) {
            if (sender.hasPermission("at.member.home")) {
                if (sender instanceof Player) {
                    Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {

                    Player player = (Player)sender;
                    String uuid = player.getUniqueId().toString();
                    HashMap<String, Location> homes = Homes.getHomes(uuid);
                    if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
                        player.sendMessage(CustomMessages.getString("Error.onCountdown"));
                        return;
                    }
                    int cooldown = CooldownManager.secondsLeftOnCooldown("home", player);
                    if (cooldown > 0) {
                        sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(cooldown)));
                        return;
                    }

                    if (args.length>0) {
                        if (sender.hasPermission("at.admin.home")) {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                            if (target != null) {
                                if (args.length > 1) {
                                    String uuidOther = target.getUniqueId().toString();
                                    HashMap<String, Location> homesOther = Homes.getHomes(uuidOther);
                                    try {
                                        Location loc;
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
                                            PaperLib.teleportAsync(player, loc);
                                            sender.sendMessage(CustomMessages.getString("Teleport.teleportingToHomeOther")
                                                    .replaceAll("\\{player}", args[0])
                                                    .replaceAll("\\{home}", args[1]));
                                        });
                                    } catch (NullPointerException ex) {
                                        Location tlocation = Homes.getHomes(uuidOther).get(args[1]);
                                        PaperLib.teleportAsync(player, tlocation);
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
                            } else if (args[0].equalsIgnoreCase("bed")  && Config.addBedToHomes()) {
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
                    CooldownManager.addToCooldown("home", player);
                    if (Config.getTeleportTimer("home") > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                        BukkitRunnable movementtimer = new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.sendMessage(CustomMessages.getString("Teleport.teleportingToHome").replaceAll("\\{home}",name));
                                PaperLib.teleportAsync(player, loc);
                                MovementManager.getMovement().remove(player.getUniqueId());
                                PaymentManager.withdraw("home", player);
                            }
                        };
                        MovementManager.getMovement().put(player.getUniqueId(), movementtimer);
                        movementtimer.runTaskLater(CoreClass.getInstance(), Config.getTeleportTimer("home") * 20);
                        player.sendMessage(CustomMessages.getEventBeforeTPMessage().replaceAll("\\{countdown}", String.valueOf(Config.getTeleportTimer("home"))));

                    } else {
                        player.sendMessage(CustomMessages.getString("Teleport.teleportingToHome").replaceAll("\\{home}",name));
                        PaperLib.teleportAsync(player, loc);
                        PaymentManager.withdraw("home", player);
                    }
                }
            }
        });
    }
}
