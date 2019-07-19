package io.github.at.events;

import io.github.at.commands.spawn.SpawnCommand;
import io.github.at.commands.teleport.Tpr;
import io.github.at.commands.warp.Warp;
import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Warps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class AtSigns implements Listener {

    @EventHandler
    public void atSigns (PlayerInteractEvent Sign){
        if (Sign.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = Sign.getPlayer();
            Block clickedBlock = Sign.getClickedBlock();
            BlockState state = clickedBlock.getState();
            if (state instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign sign = (Sign) state;
                String line1 = sign.getLine(0);
                if (ChatColor.stripColor(line1).equalsIgnoreCase("[RandomTP]")){
                    if (Config.isFeatureEnabled("randomTP")) {
                        if (player.hasPermission("at.member.tpr.use-sign")) {
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
                        } else {
                            player.sendMessage(CustomMessages.getString("Error.noPermission"));
                        }
                    }
                } else if (ChatColor.stripColor(line1).equalsIgnoreCase("[Warp]") && Warps.getWarps().containsKey(sign.getLine(1))){
                    if (Config.isFeatureEnabled("warps")) {
                        if (player.hasPermission("at.member.warp.use-sign")) {
                            Warp.warp(Warps.getWarps().get(sign.getLine(1)), player, sign.getLine(1));
                        } else {
                            player.sendMessage(CustomMessages.getString("Error.noPermission"));
                        }
                    }
                } else if (ChatColor.stripColor(line1).equalsIgnoreCase("[Spawn]")) {
                    if (Config.isFeatureEnabled("spawn")) {
                        if (player.hasPermission("at.member.spawn.use-sign")) {
                            SpawnCommand.spawn(player);
                        } else {
                            player.sendMessage(CustomMessages.getString("Error.noPermission"));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void placeSign (SignChangeEvent Place){
        Block placeBlock = Place.getBlock();
        BlockState state = placeBlock.getState();
        Player placer = Place.getPlayer();
        if (state instanceof Sign) {
            if (Place.getLine(0).equalsIgnoreCase("[RandomTP]")) {
                if (Config.isFeatureEnabled("randomTP")) {
                    if (!placer.hasPermission("at.admin.tprsign")){
                        Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "![RandomTP]!");
                        placer.sendMessage(CustomMessages.getString("Error.noPermissionSign"));
                        Place.setCancelled(true);
                    } else {

                        Place.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[RandomTP]");

                        placer.sendMessage(CustomMessages.getString("Info.createdRTPSign"));
                    }
                } else {
                    placer.sendMessage(CustomMessages.getString("Error.featureDisabled"));
                    Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "![RandomTP]!");
                    Place.setCancelled(true);
                }
            } else if (Place.getLine(0).equalsIgnoreCase("[Warp]")) {
                if (Config.isFeatureEnabled("warps")) {
                    if (!placer.hasPermission("at.admin.warpsign")){
                        Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "![Warp]!");
                        placer.sendMessage(CustomMessages.getString("Error.noPermissionSign"));
                        Place.setCancelled(true);
                    } else {
                        if (Place.getLine(1).isEmpty()) {
                            Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "![Warp]!");
                            placer.sendMessage(CustomMessages.getString("Error.noWarpInput"));
                        } else {
                            if (Warps.getWarps().containsKey(Place.getLine(1))){
                                String warpName = Place.getLine(1);
                                Place.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[Warp]");
                                Place.setLine(1, warpName);
                                placer.sendMessage(CustomMessages.getString("Info.createdWarpSign"));
                            } else {
                                Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "![Warp]!");
                                placer.sendMessage(CustomMessages.getString("Error.noSuchWarp"));
                            }
                        }
                    }
                }
                // Let's assume that if we disable the warps feature that it's to avoid plugin conflicts.

                // else {
                //    placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Warps " + ChatColor.RED + "is disabled!");
                //    Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[Warp]");
                //    Place.setCancelled(true);
                // }
            } else if (Place.getLine(0).equalsIgnoreCase("[Spawn]")) {
                if (Config.isFeatureEnabled("spawn")) {
                    if (!placer.hasPermission("at.admin.spawnsign")) {
                        Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "![Spawn]!");
                        placer.sendMessage(CustomMessages.getString("Error.noPermissionSign"));
                        Place.setCancelled(true);
                    } else {
                        Place.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[Spawn]");
                        placer.sendMessage(CustomMessages.getString("Info.createdSpawnSign"));
                    }
                } //else {
                 //   placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Spawn " + ChatColor.RED + "is disabled!");
                 //   Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[Spawn]");
                 //   Place.setCancelled(true);
              //  }
            }
        }
    }

}
