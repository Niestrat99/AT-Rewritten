package io.github.niestrat99.advancedteleport.events;

import io.github.niestrat99.advancedteleport.commands.spawn.SpawnCommand;
import io.github.niestrat99.advancedteleport.commands.teleport.Tpr;
import io.github.niestrat99.advancedteleport.commands.warp.Warp;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Warps;
import io.github.niestrat99.advancedteleport.config.Config;
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
                        break;
                    case "[warp]":
                        if (Config.isFeatureEnabled("warps")) {
                            if (Warps.getWarps().containsKey(sign.getLine(1))) {
                                if (player.hasPermission("at.member.warp.use-sign")) {
                                    Warp.warp(Warps.getWarps().get(sign.getLine(1)), player, sign.getLine(1));
                                } else {
                                    player.sendMessage(CustomMessages.getString("Error.noPermission"));
                                }
                            }
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
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event){
        Block placeBlock = event.getBlock();
        BlockState state = placeBlock.getState();
        Player placer = event.getPlayer();
        if (state instanceof Sign) {
            switch (event.getLine(0).toLowerCase()) {
                case "[randomtp]":
                    if (Config.isFeatureEnabled("randomTP")) {
                        if (!placer.hasPermission("at.admin.tprsign")){
                            placer.sendMessage(CustomMessages.getString("Error.noPermissionSign"));
                            event.setCancelled(true);
                        } else {
                            event.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[RandomTP]");
                            placer.sendMessage(CustomMessages.getString("Info.createdRTPSign"));
                        }
                    }
                    break;
                case "[warp]":
                    if (Config.isFeatureEnabled("warps")) {
                        if (!placer.hasPermission("at.admin.warpsign")){
                            placer.sendMessage(CustomMessages.getString("Error.noPermissionSign"));
                            event.setCancelled(true);
                        } else {
                            if (event.getLine(1).isEmpty()) {
                                placer.sendMessage(CustomMessages.getString("Error.noWarpInput"));
                                event.setCancelled(true);
                            } else {
                                if (Warps.getWarps().containsKey(event.getLine(1))){
                                    String warpName = event.getLine(1);
                                    event.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[Warp]");
                                    event.setLine(1, warpName);
                                    placer.sendMessage(CustomMessages.getString("Info.createdWarpSign"));
                                } else {
                                    event.setCancelled(true);
                                    placer.sendMessage(CustomMessages.getString("Error.noSuchWarp"));
                                }
                            }
                        }
                    }
                    break;
                case "[spawn]":
                    if (Config.isFeatureEnabled("spawn")) {
                        if (!placer.hasPermission("at.admin.spawnsign")) {
                            placer.sendMessage(CustomMessages.getString("Error.noPermissionSign"));
                            event.setCancelled(true);
                        } else {
                            event.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "[Spawn]");
                            placer.sendMessage(CustomMessages.getString("Info.createdSpawnSign"));
                        }
                    }
            }
        }
    }

}
