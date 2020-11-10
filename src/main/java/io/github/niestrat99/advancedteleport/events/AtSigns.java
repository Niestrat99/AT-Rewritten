package io.github.niestrat99.advancedteleport.events;

import io.github.niestrat99.advancedteleport.commands.home.Home;
import io.github.niestrat99.advancedteleport.commands.spawn.SpawnCommand;
import io.github.niestrat99.advancedteleport.commands.teleport.Tpr;
import io.github.niestrat99.advancedteleport.commands.warp.Warp;
import io.github.niestrat99.advancedteleport.commands.warp.WarpsCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Warps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

public class AtSigns implements Listener {

    private HashMap<String, ATSign> signRegistry;

    public AtSigns() {
        signRegistry = new HashMap<>();
        signRegistry.put("warps", new ATSign("Warps", "warps") {
            @Override
            public void onInteract(Sign sign, Player player) {
                WarpsCommand.sendWarps(player);
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return true;
            }
        });

        signRegistry.put("warp", new ATSign("Warp", "warps") {
            @Override
            public void onInteract(Sign sign, Player player) {
                if (Warps.getWarps().containsKey(sign.getLine(1))) {
                    Warp.warp(Warps.getWarps().get(sign.getLine(1)), player, sign.getLine(1));
                }
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                if (sign.getLine(1).isEmpty()) {
                    player.sendMessage(CustomMessages.getString("Error.noWarpInput"));
                    return false;
                } else {
                    if (Warps.getWarps().containsKey(sign.getLine(1))){
                        String warpName = sign.getLine(1);
                        sign.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[Warp]");
                        sign.setLine(1, warpName);
                        player.sendMessage(CustomMessages.getString("Info.createdWarpSign"));
                        return true;
                    } else {
                        player.sendMessage(CustomMessages.getString("Error.noSuchWarp"));
                        return false;
                    }
                }
            }
        });
        signRegistry.put("home", new ATSign("Home", "homes") {
            @Override
            public void onInteract(Sign sign, Player player) {

            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return false;
            }
        });

        signRegistry.put("homes", new ATSign("Homes", "homes") {
            @Override
            public void onInteract(Sign sign, Player player) {

            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return false;
            }
        });

        signRegistry.put("bed", new ATSign("Bed", "homes") {
            @Override
            public void onInteract(Sign sign, Player player) {
                Location bed = player.getBedSpawnLocation();
                if (bed != null) {
                    Home.teleport(player, bed, "bed");
                }
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return true;
            }
        });

        signRegistry.put("spawn", new ATSign("Spawn", "spawn") {
            @Override
            public void onInteract(Sign sign, Player player) {
                SpawnCommand.spawn(player);
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                return true;
            }
        });

        signRegistry.put("randomtp", new ATSign("RandomTP", "randomTP") {
            @Override
            public void onInteract(Sign sign, Player player) {
                if (!sign.getLine(1).isEmpty()) {
                    World otherWorld = Bukkit.getWorld(sign.getLine(1));
                    if (otherWorld != null) {
                        Tpr.randomTeleport(player, otherWorld);
                    } else {
                        player.sendMessage(CustomMessages.getString("Error.noSuchWorld"));
                    }
                } else {
                    Tpr.randomTeleport(player, player.getWorld());
                }
            }

            @Override
            public boolean canCreate(Sign sign, Player player) {
                if (!sign.getLine(1).isEmpty()) {
                    World otherWorld = Bukkit.getWorld(sign.getLine(1));
                    if (otherWorld == null) {
                        player.sendMessage(CustomMessages.getString("Error.noSuchWorld"));
                        return false;
                    }
                }
                return true;
            }
        });
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();
            BlockState blockState = clickedBlock.getState();
            if (blockState instanceof Sign) {
                Sign sign = (Sign) blockState;
                String command = ChatColor.stripColor(sign.getLine(0));
                if (command.length() > 2) {
                    command = command.substring(1, command.length() - 1).toLowerCase();
                }
                if (signRegistry.containsKey(command)) {
                    ATSign atSign = signRegistry.get(command);
                    if (atSign.isEnabled()) {
                        if (player.hasPermission(atSign.getRequiredPermission())) {
                            atSign.onInteract(sign, player);
                        }
                    }
                }
            }
        }
    }

  /*  @EventHandler
    public void onSignInteract(PlayerInteractEvent Sign){
        if (Sign.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = Sign.getPlayer();
            Block clickedBlock = Sign.getClickedBlock();
            BlockState state = clickedBlock.getState();
            if (state instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign sign = (Sign) state;
                String line1 = sign.getLine(0);
                switch (ChatColor.stripColor(line1).toLowerCase()) {
                    case "[randomtp]":
                        if (Config.isFeatureEnabled("randomTP")) {
                            if (player.hasPermission("at.member.tpr.use-sign")) {

                            } else {
                                player.sendMessage(CustomMessages.getString("Error.noPermission"));
                            }
                        }
                        break;
                    case "[warp]":
                        if (Config.isFeatureEnabled("warps")) {

                        }
                        break;
                    case "[spawn]":
                        if (Config.isFeatureEnabled("spawn")) {
                            if (player.hasPermission("at.member.spawn.use-sign")) {
                                SpawnCommand.spawn(player);
                            } else {
                                player.sendMessage(CustomMessages.getString("Error.noPermission"));
                            }
                        }
                        break;
                }
            }
        }
    } */

    @EventHandler
    public void onSignPlace(SignChangeEvent event){
        Block placeBlock = event.getBlock();
        BlockState state = placeBlock.getState();
        Player player = event.getPlayer();
        if (state instanceof Sign) {
            Sign sign = (Sign) state;
            String command = ChatColor.stripColor(event.getLine(0));
            if (command.length() > 2) {
                command = command.substring(1, command.length() - 1).toLowerCase();
            }
            if (signRegistry.containsKey(command)) {
                ATSign atSign = signRegistry.get(command);
                if (atSign.isEnabled()) {
                    if (player.hasPermission(atSign.getAdminPermission())) {
                        sign.setLine(1, event.getLine(1));
                        if (atSign.canCreate(sign, player)) {
                            event.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[" + atSign.getName() + "]");
                        }
                    } else {
                        player.sendMessage(CustomMessages.getString("Error.noPermissionSign"));
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
