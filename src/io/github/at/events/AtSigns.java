package io.github.at.events;

import io.github.at.config.Config;
import io.github.at.config.Warps;
import org.bukkit.ChatColor;
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
                    if (!sign.getLine(1).isEmpty()) {
                        player.performCommand("tpr " + sign.getLine(1));
                    } else {
                        player.performCommand("tpr");
                    }

                } else if (ChatColor.stripColor(line1).equalsIgnoreCase("[Warp]") && Warps.getWarps().containsKey(sign.getLine(1))){
                    player.performCommand("warp " + sign.getLine(1));
                } else if (ChatColor.stripColor(line1).equalsIgnoreCase("[Spawn]")) {
                    player.performCommand("spawn");
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
            Sign sign = (Sign) state;
            if (Place.getLine(0).equalsIgnoreCase("[RandomTP]")) {
                if (Config.featRTP()) {
                    if (!placer.hasPermission("tbh.tp.admin.tprsign")){
                        Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[RandomTP]");
                        placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You do not have permission to make this sign!");
                        Place.setCancelled(true);
                    } else {

                        Place.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[RandomTP]");
                        Place.setLine(1, ChatColor.ITALIC + "Click me!");
                        placer.sendMessage(ChatColor.GREEN + "Successfully created the RandomTP sign!");
                    }
                } else {
                    placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "RandomTP " + ChatColor.RED + "is disabled!");
                    Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[RandomTP]");
                    Place.setCancelled(true);
                }
            } else if (Place.getLine(0).equalsIgnoreCase("[Warp]")) {
                if (Config.featWarps()) {
                    if (!placer.hasPermission("tbh.tp.admin.warpsign")){
                        Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[Warp]");
                        placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You do not have permission to make this sign!");
                        Place.setCancelled(true);
                    } else {
                        if (Place.getLine(1).isEmpty()) {
                            Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[Warp]");
                            placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You need to include a warp name!");
                        } else {
                            if (Warps.getWarps().containsKey(Place.getLine(1))){
                                String warpName = Place.getLine(1);
                                Place.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[Warp]");
                                Place.setLine(1, warpName);
                                Place.setLine(2, ChatColor.ITALIC + "Click here to");
                                Place.setLine(3, ChatColor.ITALIC + "teleport!");
                                placer.sendMessage(ChatColor.GREEN + "Successfully created the Warp sign!");
                            } else {
                                Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[Warp]");
                                placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " That warp doesn't exist!");
                            }
                        }
                    }
                } else {
                    placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Warps " + ChatColor.RED + "is disabled!");
                    Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[Warp]");
                    Place.setCancelled(true);
                }
            } else if (Place.getLine(0).equalsIgnoreCase("[Spawn]")) {
                if (Config.featSpawn()) {
                    if (!placer.hasPermission("tbh.tp.admin.spawnsign")) {
                        Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[Spawn]");
                        placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You do not have permission to make this sign!");
                        Place.setCancelled(true);
                    } else {
                        Place.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[Spawn]");
                        placer.sendMessage(ChatColor.GREEN + "Successfully created the Spawn sign!");
                    }
                } else {
                    placer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Spawn " + ChatColor.RED + "is disabled!");
                    Place.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "[Spawn]");
                    Place.setCancelled(true);
                }
            }
        }
    }

}
