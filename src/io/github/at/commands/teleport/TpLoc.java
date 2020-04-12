package io.github.at.commands.teleport;

import io.github.at.api.ATTeleportEvent;
import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.main.CoreClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.regex.Pattern;

public class TpLoc implements CommandExecutor {

    private static Pattern location = Pattern.compile("^(-)?\\d+(\\.\\d+)?$");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (Config.isFeatureEnabled("teleport")) {
            if (sender.hasPermission("at.admin.tploc")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length > 2) {
                        double[] loc = new double[3];
                        for (int i = 0; i < 3; i++) {
                            if (args[i].equalsIgnoreCase("~")) {
                                switch (i) {
                                    case 0:
                                        loc[i] = player.getLocation().getX();
                                        break;
                                    case 1:
                                        loc[i] = player.getLocation().getY();
                                        break;
                                    case 2:
                                        loc[i] = player.getLocation().getZ();
                                        break;
                                }
                            } else if (location.matcher(args[i]).matches()) {
                                loc[i] = Double.parseDouble(args[i]);
                            } else {
                                player.sendMessage(CustomMessages.getString("Error.invalidArgs"));
                                return false;
                            }
                        }

                        float yaw = player.getLocation().getYaw();
                        float pitch = player.getLocation().getPitch();
                        if (args.length > 3 && !args[3].equalsIgnoreCase("~") ) {
                            if (location.matcher(args[3]).matches()) {
                                yaw = Float.parseFloat(args[3]);
                                if (args.length > 4 && !args[4].equalsIgnoreCase("~")) {
                                    if (location.matcher(args[4]).matches()) {
                                        pitch = Float.parseFloat(args[4]);
                                    } else {
                                        player.sendMessage(CustomMessages.getString("Error.invalidArgs"));
                                        return false;
                                    }
                                }
                            } else {
                                player.sendMessage(CustomMessages.getString("Error.invalidArgs"));
                                return false;
                            }
                        }

                        World world = player.getWorld();
                        if (args.length > 5 && !args[5].equalsIgnoreCase("~")) {
                            world = Bukkit.getWorld(args[5]);
                            if (world == null) {
                                player.sendMessage("no-world");
                                return false;
                            }
                        }
                        Location location = new Location(world, loc[0], loc[1], loc[2], yaw, pitch);
                        Player target = player;
                        if (args.length > 6) {
                            if (player.hasPermission("at.admin.tploc.others")) {
                                target = Bukkit.getPlayer(args[6]);
                                if (target == null || !target.isOnline()) {
                                    player.sendMessage(CustomMessages.getString("Error.noSuchPlayer"));
                                    return true;
                                }
                            }
                        }
                        ATTeleportEvent event = new ATTeleportEvent(target, location, target.getLocation(), "", ATTeleportEvent.TeleportType.TPLOC);
                        if (!event.isCancelled()) {
                            target.teleport(location);
                            if (player != target) {
                                player.sendMessage(CustomMessages.getString("Info.teleportedToLocOther")
                                        .replaceAll("\\{x}", String.valueOf(loc[0]))
                                        .replaceAll("\\{y}", String.valueOf(loc[1]))
                                        .replaceAll("\\{z}", String.valueOf(loc[2]))
                                        .replaceAll("\\{yaw}", String.valueOf(yaw))
                                        .replaceAll("\\{pitch}", String.valueOf(pitch))
                                        .replaceAll("\\{world}", world.getName()
                                        .replaceAll("\\{player}", args[6])));
                            } else {
                                player.sendMessage(CustomMessages.getString("Info.teleportedToLoc")
                                        .replaceAll("\\{x}", String.valueOf(loc[0]))
                                        .replaceAll("\\{y}", String.valueOf(loc[1]))
                                        .replaceAll("\\{z}", String.valueOf(loc[2]))
                                        .replaceAll("\\{yaw}", String.valueOf(yaw))
                                        .replaceAll("\\{pitch}", String.valueOf(pitch))
                                        .replaceAll("\\{world}", world.getName()));
                            }
                        }
                    } else {
                        player.sendMessage(CustomMessages.getString("Error.tooFewArguments"));
                        return false;
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            }
        }
        return true;
    }

    public static void a() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.MONTH) == Calendar.MAY && cal.get(Calendar.DAY_OF_MONTH) == 6) {
            CoreClass.getInstance().getLogger().info("Happy anniversary, TM and Nie!");
        }
    }
}
