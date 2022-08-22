package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class TpLoc extends TeleportATCommand {

    private static final Pattern location = Pattern.compile("^(-)?\\d+(\\.\\d+)?$");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 3) {
            CustomMessages.sendMessage(player, "Error.tooFewArguments");
            return false;
        }

        // Get the x, y and z coordinates
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
                CustomMessages.sendMessage(player, "Error.invalidArgs");
                return false;
            }
        }

        // Get the yaw and pitch
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        if (args.length > 3 && !args[3].equalsIgnoreCase("~")) {
            if (location.matcher(args[3]).matches()) {
                yaw = Float.parseFloat(args[3]);
                if (args.length > 4 && !args[4].equalsIgnoreCase("~")) {
                    if (location.matcher(args[4]).matches()) {
                        pitch = Float.parseFloat(args[4]);
                    } else {
                        CustomMessages.sendMessage(player, "Error.invalidArgs");
                        return false;
                    }
                }
            } else {
                CustomMessages.sendMessage(player, "Error.invalidArgs");
                return false;
            }
        }

        // Get the world
        World world = player.getWorld();
        if (args.length > 5 && !args[5].equalsIgnoreCase("~")) {
            world = Bukkit.getWorld(args[5]);
            if (world == null) {
                player.sendMessage("no-world");
                return false;
            }
        }
        Location location = new Location(world, loc[0], loc[1], loc[2], yaw, pitch);

        // Get the player
        Player target = player;
        if (args.length > 6) {
            if (player.hasPermission("at.admin.tploc.others")) {
                target = Bukkit.getPlayer(args[6]);
                if (target == null || !target.isOnline()) {
                    CustomMessages.sendMessage(player, "Error.noSuchPlayer");
                    return true;
                }
            }
        }

        // Should the player be flying or not?
        boolean allowFlight = true;
        if (args.length > 7) {
            if (target.hasPermission("at.admin.tploc.safe-teleport") && target.getAllowFlight()) {
                if (args[7].equalsIgnoreCase("precise")) {
                    target.setFlying(true);
                } else if (args[7].equalsIgnoreCase("noflight")) {
                    target.setFlying(false);
                    allowFlight = false;
                }
            }
        }

        ATTeleportEvent event = new ATTeleportEvent(target, location, target.getLocation(), "",
                ATTeleportEvent.TeleportType.TPLOC);
        if (!event.isCancelled()) {
            Location blockBelow = location.clone().add(0, -1, 0);
            if (allowFlight && target.getAllowFlight() && target.hasPermission("at.admin.tploc.safe-teleport") && blockBelow.getBlock().getType() == Material.AIR) {
                target.setFlying(true);
            }
            PaperLib.teleportAsync(target, location, PlayerTeleportEvent.TeleportCause.COMMAND);
            if (player != target) {
                CustomMessages.sendMessage(player, "Info.teleportedToLocOther",
                        "{x}", String.valueOf(loc[0]),
                        "{y}", String.valueOf(loc[1]),
                        "{z}", String.valueOf(loc[2]),
                        "{yaw}", String.valueOf(yaw),
                        "{pitch}", String.valueOf(pitch),
                        "{world}", world.getName(),
                        "{player}", args[6]);
            } else {
                CustomMessages.sendMessage(player, "Info.teleportedToLoc",
                        "{x}", String.valueOf(loc[0]),
                        "{y}", String.valueOf(loc[1]),
                        "{z}", String.valueOf(loc[2]),
                        "{yaw}", String.valueOf(yaw),
                        "{pitch}", String.valueOf(pitch),
                        "{world}", world.getName());
            }
        }

        return true;
    }

    // this isn't a backdoor
    public static void a() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.MONTH) == Calendar.MAY && cal.get(Calendar.DAY_OF_MONTH) == 6) {
            CoreClass.getInstance().getLogger().info("Happy anniversary, TM and Nie!");
        }
    }

    @Override
    public String getPermission() {
        return "at.admin.tploc";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                                      @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location location = player.getLocation();
            switch (args.length) {
                case 1:
                    StringUtil.copyPartialMatches(args[0],
                            Arrays.asList(String.valueOf(location.getX()),
                                    String.valueOf(location.getBlockX()),
                                    "~"), results);
                    break;
                case 2:
                    StringUtil.copyPartialMatches(args[1],
                            Arrays.asList(String.valueOf(location.getY()),
                                    String.valueOf(location.getBlockY()),
                                    "~"), results);
                    break;
                case 3:
                    StringUtil.copyPartialMatches(args[2],
                            Arrays.asList(String.valueOf(location.getZ()),
                                    String.valueOf(location.getBlockZ()),
                                    "~"), results);
                    break;
                case 4:
                    StringUtil.copyPartialMatches(args[3],
                            Arrays.asList(String.valueOf(location.getYaw()),
                                    "~"), results);
                    break;
                case 5:
                    StringUtil.copyPartialMatches(args[4],
                            Arrays.asList(String.valueOf(location.getPitch()),
                                    "~"), results);
                    break;
                case 6:
                    List<String> worlds = new ArrayList<>();
                    for (World world : Bukkit.getWorlds()) {
                        worlds.add(world.getName());
                    }
                    worlds.add("~");
                    StringUtil.copyPartialMatches(args[5], worlds, results);
                    break;
                case 7:
                    if (player.hasPermission("at.admin.tploc.others")) {
                        StringUtil.copyPartialMatches(args[6],
                                ConditionChecker.getPlayers(player), results);
                    }
                    break;
                case 8:
                    if (player.hasPermission("at.admin.tploc.safe-teleport")) {
                        StringUtil.copyPartialMatches(args[7],
                                Arrays.asList("precise", "noflight"), results);
                    }
                    break;
            }
        }
        return results;
    }
}
